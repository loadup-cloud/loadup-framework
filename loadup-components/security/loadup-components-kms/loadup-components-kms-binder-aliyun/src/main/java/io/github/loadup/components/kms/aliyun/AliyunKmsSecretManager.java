package io.github.loadup.components.kms.aliyun;

import static io.github.loadup.components.kms.aliyun.AliyunKmsRequests.cloud;
import static io.github.loadup.components.kms.aliyun.AliyunKmsRequests.requireResponse;

import com.aliyun.kms20160120.Client;
import com.aliyun.kms20160120.models.CreateSecretRequest;
import com.aliyun.kms20160120.models.GetSecretValueRequest;
import com.aliyun.kms20160120.models.PutSecretValueRequest;
import io.github.loadup.components.kms.KmsException;
import io.github.loadup.components.kms.KmsSecretManager;
import io.github.loadup.components.kms.aliyun.config.AliyunKmsProperties;
import java.util.UUID;

public final class AliyunKmsSecretManager implements KmsSecretManager {
    private final Client client;
    private final AliyunKmsRequests requests;

    public AliyunKmsSecretManager(Client client, AliyunKmsProperties properties) {
        this.client = client;
        this.requests = new AliyunKmsRequests(properties);
    }

    @Override
    public void create(String name, String value) {
        KmsSecretManager.validateName(name);
        KmsSecretManager.validateValue(value);
        String version = UUID.randomUUID().toString();
        var request = new CreateSecretRequest()
                .setSecretName(name)
                .setSecretData(value)
                .setSecretDataType("text")
                .setSecretType("Generic")
                .setVersionId(version);
        var body = requireResponse(cloud(() -> client.createSecretWithOptions(request, requests.runtime()))
                .getBody());
        checkIdentity(name, version, body.getSecretName(), body.getVersionId());
    }

    @Override
    public void update(String name, String value) {
        KmsSecretManager.validateName(name);
        KmsSecretManager.validateValue(value);
        String version = UUID.randomUUID().toString();
        var request = new PutSecretValueRequest()
                .setSecretName(name)
                .setSecretData(value)
                .setSecretDataType("text")
                .setVersionId(version);
        var body = requireResponse(cloud(() -> client.putSecretValueWithOptions(request, requests.runtime()))
                .getBody());
        checkIdentity(name, version, body.getSecretName(), body.getVersionId());
    }

    @Override
    public String get(String name) {
        KmsSecretManager.validateName(name);
        var request = new GetSecretValueRequest().setSecretName(name);
        var body = requireResponse(cloud(() -> client.getSecretValueWithOptions(request, requests.runtime()))
                .getBody());
        if (!name.equals(body.getSecretName())
                || !"Generic".equals(body.getSecretType())
                || !"text".equals(body.getSecretDataType())) {
            throw new KmsException(KmsException.Code.INVALID_RESPONSE);
        }
        String value = body.getSecretData();
        try {
            KmsSecretManager.validateValue(value);
        } catch (IllegalArgumentException invalid) {
            throw new KmsException(KmsException.Code.INVALID_RESPONSE);
        }
        return value;
    }

    private static void checkIdentity(String name, String version, String actualName, String actualVersion) {
        if (!name.equals(actualName) || !version.equals(actualVersion)) {
            throw new KmsException(KmsException.Code.INVALID_RESPONSE);
        }
    }
}

package io.github.loadup.components.signature.example;

import io.github.loadup.components.signature.enums.KeyAlgorithm;
import io.github.loadup.components.signature.enums.SignatureAlgorithm;
import io.github.loadup.components.signature.model.KeyPairInfo;
import io.github.loadup.components.signature.service.KeyPairService;
import io.github.loadup.components.signature.service.SignatureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 签名服务使用示例
 *
 * 本示例展示了如何在实际项目中使用 Signature 组件：
 * 1. 应用启动时生成密钥对
 * 2. 缓存密钥对象避免重复加载
 * 3. 提供签名和验签方法
 *
 * ⚠️ 注意：
 * - 本示例将密钥存储在内存中，仅适用于开发/测试环境
 * - 生产环境应将密钥持久化到数据库、配置中心或 KMS
 * - 私钥必须加密存储，禁止明文存储
 *
 * @author loadup
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SignatureServiceExample {

    private final SignatureService signatureService;
    private final KeyPairService keyPairService;

    // 缓存应用的密钥对（仅示例，生产环境不应这样做）
    private PrivateKey applicationPrivateKey;
    private PublicKey applicationPublicKey;

    // 缓存多个租户的公钥（用于验签）
    private final Map<String, PublicKey> tenantPublicKeys = new ConcurrentHashMap<>();

    /**
     * 应用启动时初始化密钥
     */
    @PostConstruct
    public void init() {
        log.info("初始化应用签名密钥...");

        // 生成应用自身的密钥对
        KeyPairInfo keyPair = keyPairService.generateKeyPair(
                KeyAlgorithm.RSA,
                2048
        );

        // 加载为 Java 对象并缓存
        this.applicationPrivateKey = keyPairService.loadPrivateKey(
                keyPair.getPrivateKey(),
                KeyAlgorithm.RSA
        );
        this.applicationPublicKey = keyPairService.loadPublicKey(
                keyPair.getPublicKey(),
                KeyAlgorithm.RSA
        );

        log.info("应用签名密钥初始化完成");
        log.info("公钥: {}", keyPair.getPublicKey().substring(0, 50) + "...");

        // ⚠️ 生产环境应该：
        // 1. 将密钥对保存到数据库（私钥加密存储）
        // 2. 或者保存到配置中心
        // 3. 或者保存到 KMS（推荐）
        // saveKeyPairToDatabase(keyPair);
    }

    /**
     * 签名数据（使用应用私钥）
     *
     * @param data 待签名数据
     * @return Base64 编码的签名
     */
    public String sign(String data) {
        return signatureService.sign(
                data.getBytes(),
                applicationPrivateKey,
                SignatureAlgorithm.SHA256_WITH_RSA
        );
    }

    /**
     * 验证签名（使用应用公钥）
     *
     * @param data      原始数据
     * @param signature Base64 编码的签名
     * @return true=验证通过, false=验证失败
     */
    public boolean verify(String data, String signature) {
        return signatureService.verify(
                data.getBytes(),
                signature,
                applicationPublicKey,
                SignatureAlgorithm.SHA256_WITH_RSA
        );
    }

    /**
     * 验证租户签名（使用租户公钥）
     *
     * @param tenantId         租户ID
     * @param data             原始数据
     * @param signature        Base64 编码的签名
     * @param tenantPublicKey  租户公钥（Base64）
     * @return true=验证通过, false=验证失败
     */
    public boolean verifyTenantSignature(String tenantId, String data, String signature, String tenantPublicKey) {
        // 从缓存获取或加载租户公钥
        PublicKey publicKey = tenantPublicKeys.computeIfAbsent(tenantId, id -> {
            log.debug("加载租户 {} 的公钥", id);
            return keyPairService.loadPublicKey(tenantPublicKey, KeyAlgorithm.RSA);
        });

        return signatureService.verify(
                data.getBytes(),
                signature,
                publicKey,
                SignatureAlgorithm.SHA256_WITH_RSA
        );
    }

    /**
     * 获取应用公钥（供第三方验签使用）
     *
     * @return Base64 编码的公钥
     */
    public String getApplicationPublicKey() {
        // ⚠️ 实际项目应从数据库或配置中心读取
        byte[] encoded = applicationPublicKey.getEncoded();
        return java.util.Base64.getEncoder().encodeToString(encoded);
    }

    /**
     * 清除租户公钥缓存（密钥轮换时调用）
     *
     * @param tenantId 租户ID
     */
    public void evictTenantPublicKey(String tenantId) {
        tenantPublicKeys.remove(tenantId);
        log.info("已清除租户 {} 的公钥缓存", tenantId);
    }
}


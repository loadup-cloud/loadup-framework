package io.github.loadup.components.signature.service.impl;

/*-
 * #%L
 * LoadUp Components :: Signature
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import io.github.loadup.components.signature.enums.KeyAlgorithm;
import io.github.loadup.components.signature.enums.SignatureAlgorithm;
import io.github.loadup.components.signature.exception.SignatureException;
import io.github.loadup.components.signature.service.KeyPairService;
import io.github.loadup.components.signature.service.SignatureService;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 签名服务实现
 *
 * @author loadup
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SignatureServiceImpl implements SignatureService {

    private final KeyPairService keyPairService;

    @Override
    public String sign(byte[] data, PrivateKey privateKey, SignatureAlgorithm algorithm) {
        try {
            Signature signature = Signature.getInstance(algorithm.getJcaName());
            signature.initSign(privateKey);
            signature.update(data);
            byte[] signatureBytes = signature.sign();
            return Base64.getEncoder().encodeToString(signatureBytes);
        } catch (Exception e) {
            log.error("签名失败: algorithm={}, error={}", algorithm, e.getMessage(), e);
            throw new SignatureException(
                    SignatureException.SignatureErrorCode.SIGN_FAILED, "签名失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String sign(String data, String privateKeyBase64, SignatureAlgorithm algorithm) {
        try {
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            PrivateKey privateKey =
                    keyPairService.loadPrivateKey(privateKeyBase64, KeyAlgorithm.valueOf(algorithm.getKeyAlgorithm()));
            return sign(dataBytes, privateKey, algorithm);
        } catch (SignatureException e) {
            throw e;
        } catch (Exception e) {
            log.error("签名失败: algorithm={}, error={}", algorithm, e.getMessage(), e);
            throw new SignatureException(
                    SignatureException.SignatureErrorCode.SIGN_FAILED, "签名失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean verify(byte[] data, String signatureBase64, PublicKey publicKey, SignatureAlgorithm algorithm) {
        try {
            Signature signature = Signature.getInstance(algorithm.getJcaName());
            signature.initVerify(publicKey);
            signature.update(data);
            byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);
            return signature.verify(signatureBytes);
        } catch (Exception e) {
            log.error("验签失败: algorithm={}, error={}", algorithm, e.getMessage(), e);
            throw new SignatureException(
                    SignatureException.SignatureErrorCode.VERIFY_FAILED, "验签失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean verify(String data, String signatureBase64, String publicKeyBase64, SignatureAlgorithm algorithm) {
        try {
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            PublicKey publicKey =
                    keyPairService.loadPublicKey(publicKeyBase64, KeyAlgorithm.valueOf(algorithm.getKeyAlgorithm()));
            return verify(dataBytes, signatureBase64, publicKey, algorithm);
        } catch (SignatureException e) {
            throw e;
        } catch (Exception e) {
            log.error("验签失败: algorithm={}, error={}", algorithm, e.getMessage(), e);
            throw new SignatureException(
                    SignatureException.SignatureErrorCode.VERIFY_FAILED, "验签失败: " + e.getMessage(), e);
        }
    }
}

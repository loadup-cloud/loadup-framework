package io.github.loadup.components.signature.example;

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
import io.github.loadup.components.signature.model.KeyPairInfo;
import io.github.loadup.components.signature.service.KeyPairService;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 密钥管理服务示例
 *
 * 展示如何在实际项目中管理密钥：
 * 1. 生成密钥对并存储到数据库
 * 2. 从数据库加载密钥
 * 3. 缓存密钥对象提升性能
 * 4. 密钥轮换
 *
 * ⚠️ 本示例需要配合数据库使用，数据库表结构见 README.md
 *
 * @author loadup
 */
@Slf4j
@RequiredArgsConstructor
public class KeyManagementServiceExample {

    private final KeyPairService keyPairService;
    // private final KeyRepository keyRepository;  // 需要自行实现

    // 缓存已加载的密钥对象
    private final Map<String, PrivateKey> privateKeyCache = new ConcurrentHashMap<>();
    private final Map<String, PublicKey> publicKeyCache = new ConcurrentHashMap<>();

    /**
     * 为租户生成并保存密钥对
     *
     * @param tenantId 租户ID
     * @return 密钥对信息
     */
    public KeyPairInfo generateAndSaveKeyPair(String tenantId) {
        log.info("为租户 {} 生成密钥对...", tenantId);

        // 1. 生成密钥对
        KeyPairInfo keyPair = keyPairService.generateKeyPair(KeyAlgorithm.RSA, 2048);

        // 2. 加密私钥
        String encryptedPrivateKey = encryptPrivateKey(keyPair.getPrivateKey());

        // 3. 保存到数据库
        // KeyEntity entity = KeyEntity.builder()
        //         .tenantId(tenantId)
        //         .publicKey(keyPair.getPublicKey())
        //         .privateKey(encryptedPrivateKey)
        //         .algorithm(keyPair.getAlgorithm())
        //         .keySize(keyPair.getKeySize())
        //         .version(1)
        //         .status("ACTIVE")
        //         .createdAt(LocalDateTime.now())
        //         .build();
        //
        // keyRepository.save(entity);

        log.info("租户 {} 密钥对已保存", tenantId);

        return keyPair;
    }

    /**
     * 获取租户的私钥（带缓存）
     *
     * @param tenantId 租户ID
     * @return 私钥对象
     */
    public PrivateKey getPrivateKey(String tenantId) {
        return privateKeyCache.computeIfAbsent(tenantId, id -> {
            log.debug("从数据库加载租户 {} 的私钥", id);

            // 从数据库查询
            // KeyEntity entity = keyRepository.findActiveKeyByTenantId(id)
            //         .orElseThrow(() -> new BusinessException("租户密钥不存在"));

            // 解密私钥
            // String decryptedPrivateKey = decryptPrivateKey(entity.getPrivateKey());

            // 加载为 Java 对象
            // return keyPairService.loadPrivateKey(decryptedPrivateKey, KeyAlgorithm.RSA);

            // 临时返回 null（实际使用时删除此行）
            return null;
        });
    }

    /**
     * 获取租户的公钥（带缓存）
     *
     * @param tenantId 租户ID
     * @return 公钥对象
     */
    public PublicKey getPublicKey(String tenantId) {
        return publicKeyCache.computeIfAbsent(tenantId, id -> {
            log.debug("从数据库加载租户 {} 的公钥", id);

            // 从数据库查询
            // KeyEntity entity = keyRepository.findActiveKeyByTenantId(id)
            //         .orElseThrow(() -> new BusinessException("租户密钥不存在"));

            // 加载为 Java 对象
            // return keyPairService.loadPublicKey(entity.getPublicKey(), KeyAlgorithm.RSA);

            // 临时返回 null（实际使用时删除此行）
            return null;
        });
    }

    /**
     * 轮换租户密钥
     *
     * @param tenantId 租户ID
     */
    public void rotateKey(String tenantId) {
        log.info("开始轮换租户 {} 的密钥...", tenantId);

        // 1. 生成新密钥对
        KeyPairInfo newKeyPair = keyPairService.generateKeyPair(KeyAlgorithm.RSA, 2048);

        // 2. 获取当前最新版本号
        // int currentVersion = keyRepository.getLatestVersion(tenantId);

        // 3. 保存新密钥
        // KeyEntity newKey = KeyEntity.builder()
        //         .tenantId(tenantId)
        //         .publicKey(newKeyPair.getPublicKey())
        //         .privateKey(encryptPrivateKey(newKeyPair.getPrivateKey()))
        //         .algorithm(newKeyPair.getAlgorithm())
        //         .keySize(newKeyPair.getKeySize())
        //         .version(currentVersion + 1)
        //         .status("ACTIVE")
        //         .createdAt(LocalDateTime.now())
        //         .build();
        //
        // keyRepository.save(newKey);

        // 4. 归档旧密钥（保留一段时间用于验证历史签名）
        // keyRepository.archiveOldKeys(tenantId, currentVersion);

        // 5. 清除缓存
        evictCache(tenantId);

        log.info("租户 {} 密钥轮换完成", tenantId);
    }

    /**
     * 清除密钥缓存
     *
     * @param tenantId 租户ID
     */
    public void evictCache(String tenantId) {
        privateKeyCache.remove(tenantId);
        publicKeyCache.remove(tenantId);
        log.info("已清除租户 {} 的密钥缓存", tenantId);
    }

    /**
     * 加密私钥（示例：使用 AES）
     *
     * @param privateKey Base64 编码的私钥
     * @return 加密后的私钥
     */
    private String encryptPrivateKey(String privateKey) {
        // ⚠️ 实际项目应使用 AES、SM4 等算法加密
        // 密钥可从环境变量或 KMS 获取
        // return AesUtils.encrypt(privateKey, getMasterKey());

        // 临时返回原值（实际使用时应加密）
        log.warn("⚠️ 私钥未加密，仅用于示例！生产环境必须加密存储！");
        return privateKey;
    }

    /**
     * 解密私钥
     *
     * @param encryptedPrivateKey 加密的私钥
     * @return 解密后的私钥
     */
    private String decryptPrivateKey(String encryptedPrivateKey) {
        // return AesUtils.decrypt(encryptedPrivateKey, getMasterKey());

        // 临时返回原值
        return encryptedPrivateKey;
    }

    /**
     * 获取主密钥（用于加密私钥）
     *
     * 建议从以下来源获取：
     * 1. 环境变量
     * 2. KMS（推荐）
     * 3. 配置中心（加密存储）
     *
     * @return 主密钥
     */
    private String getMasterKey() {
        // return System.getenv("MASTER_KEY");
        return "master-key-from-env-or-kms";
    }
}

/**
 * 密钥实体类示例（需要配合 MyBatis 或 JPA 使用）
 */
// @Data
// @Builder
// @NoArgsConstructor
// @AllArgsConstructor
// @Table(name = "signature_keys")
// class KeyEntity {
//     private Long id;
//     private String tenantId;
//     private String publicKey;
//     private String privateKey;
//     private String algorithm;
//     private Integer keySize;
//     private Integer version;
//     private String status;
//     private LocalDateTime createdAt;
//     private LocalDateTime expiredAt;
// }

/**
 * 密钥仓库接口示例
 */
// interface KeyRepository {
//     Optional<KeyEntity> findActiveKeyByTenantId(String tenantId);
//     int getLatestVersion(String tenantId);
//     void archiveOldKeys(String tenantId, int currentVersion);
//     List<KeyEntity> findKeysOlderThan(int days);
// }

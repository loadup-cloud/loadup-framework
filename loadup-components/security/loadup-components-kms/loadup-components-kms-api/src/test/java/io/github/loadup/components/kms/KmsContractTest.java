package io.github.loadup.components.kms;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class KmsContractTest {
    @Test
    void keepsNativeAndLegacyKeyReferencesPinnedToTheirOriginalVersion() {
        var nativeVersion = new KmsProviderVersion("master-id", "version-4");
        var nativeCiphertext = new KmsCiphertext("payments", 0, "aliyun:AQID", nativeVersion);
        var legacyCiphertext = new KmsCiphertext("payments", 4, "aliyun:v4:AQID");
        assertEquals(new KmsKeyRef("payments", 0, nativeVersion), nativeCiphertext.keyRef());
        assertEquals(new KmsKeyRef("payments", 4), legacyCiphertext.keyRef());
        assertEquals(new KmsKeyRef("payments", 0), KmsKeyRef.latest("payments"));

        assertThrows(IllegalArgumentException.class, () -> new KmsKeyRef("payments", 4, nativeVersion));
        assertThrows(IllegalArgumentException.class, () -> new KmsCiphertext("payments", 3, "vault:v4:AQID"));
        assertThrows(IllegalArgumentException.class, () -> new KmsCiphertext("payments", 0, "aliyun:AQID"));
        assertThrows(IllegalArgumentException.class, () -> new KmsProviderVersion("alias/payments", "version-4"));
        assertFalse(nativeCiphertext.toString().contains("AQID"));
    }

    @Test
    void signatureDoesNotShareItsByteArrayOrExposeItInDiagnostics() {
        var algorithm = KmsSignatureAlgorithm.RSA_SHA256_PSS;
        byte[] source = {1, 2, 3};
        var signature = new KmsSignature("payments", 1, algorithm, source);
        source[0] = 0;
        signature.bytes()[1] = 0;

        assertArrayEquals(new byte[] {1, 2, 3}, signature.bytes());
        assertEquals("AQID", signature.base64());
        assertEquals(new KmsKeyRef("payments", 1), signature.keyRef());
        assertFalse(signature.toString().contains(signature.base64()));
        assertThrows(IllegalArgumentException.class, () -> new KmsSignature("payments", 0, algorithm, source));
        assertThrows(IllegalArgumentException.class, () -> new KmsSignature("payments", 1, algorithm, new byte[513]));
    }

    @Test
    void validatesSecretNamesAndMeasuresValuesInUtf8Bytes() {
        KmsSecretManager.validateName("a".repeat(128));
        assertThrows(IllegalArgumentException.class, () -> KmsSecretManager.validateName("a".repeat(129)));
        assertThrows(IllegalArgumentException.class, () -> KmsSecretManager.validateName("../payments"));

        String maxUtf8Value = "界".repeat(10_240);
        KmsSecretManager.validateValue(maxUtf8Value);
        assertThrows(IllegalArgumentException.class, () -> KmsSecretManager.validateValue(maxUtf8Value + "x"));
        assertThrows(IllegalArgumentException.class, () -> KmsSecretManager.validateValue(""));
        assertThrows(
                IllegalArgumentException.class,
                () -> KmsSecretManager.validateValue(Character.toString((char) 0xD800)));
    }
}

/*
 * Copyright (C) 2026 LoadUp Cloud
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.loadup.components.kms;

import io.github.loadup.components.kms.keystore.KeyStoreKmsSecretManager;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;

final class LocalKmsSecretFiles implements KeyStoreKmsSecretManager.Storage {
    private static final Object WRITE_MONITOR = new Object();
    private final Path directory;

    LocalKmsSecretFiles(Path directory) {
        this.directory = directory.toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.directory);
        } catch (IOException failure) {
            throw new KmsException(KmsException.Code.UNAVAILABLE);
        }
    }

    @Override
    public boolean create(String id, String ciphertext) {
        return write(id, ciphertext, false);
    }

    @Override
    public boolean update(String id, String ciphertext) {
        return write(id, ciphertext, true);
    }

    @Override
    public Optional<String> get(String id) {
        Path path = path(id);
        try {
            if (!exists(path)) {
                return Optional.empty();
            }
            try (var input = Files.newInputStream(path, LinkOption.NOFOLLOW_LINKS)) {
                byte[] bytes = input.readNBytes(KeyStoreKmsSecretManager.MAX_CIPHERTEXT_LENGTH + 1);
                if (bytes.length > KeyStoreKmsSecretManager.MAX_CIPHERTEXT_LENGTH) {
                    throw new KmsException(KmsException.Code.INVALID_RESPONSE);
                }
                return Optional.of(new String(bytes, StandardCharsets.US_ASCII));
            }
        } catch (NoSuchFileException missing) {
            return Optional.empty();
        } catch (IOException failure) {
            throw new KmsException(KmsException.Code.UNAVAILABLE);
        }
    }

    private boolean write(String id, String ciphertext, boolean update) {
        Path target = path(id);
        synchronized (WRITE_MONITOR) {
            try (var channel = FileChannel.open(
                            directory.resolve(".write-lock"),
                            StandardOpenOption.CREATE,
                            StandardOpenOption.WRITE,
                            LinkOption.NOFOLLOW_LINKS);
                    var ignored = channel.lock()) {
                if (exists(target) != update) {
                    return false;
                }
                storeAtomically(target, ciphertext);
                return true;
            } catch (IOException failure) {
                throw new KmsException(KmsException.Code.UNAVAILABLE);
            }
        }
    }

    private void storeAtomically(Path target, String ciphertext) throws IOException {
        Path pending = Files.createTempFile(directory, ".secret-", ".tmp");
        try {
            try (var file = FileChannel.open(pending, StandardOpenOption.WRITE)) {
                var data = ByteBuffer.wrap(ciphertext.getBytes(StandardCharsets.US_ASCII));
                while (data.hasRemaining()) {
                    file.write(data);
                }
                file.force(true);
            }
            Files.move(pending, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(pending);
        }
    }

    private Path path(String id) {
        return directory.resolve(id + ".enc");
    }

    private static boolean exists(Path path) throws IOException {
        try {
            if (!Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS)
                    .isRegularFile()) {
                throw new KmsException(KmsException.Code.INVALID_RESPONSE);
            }
            return true;
        } catch (NoSuchFileException missing) {
            return false;
        }
    }
}

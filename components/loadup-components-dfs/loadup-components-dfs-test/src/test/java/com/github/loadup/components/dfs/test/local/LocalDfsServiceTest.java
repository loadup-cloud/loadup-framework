package com.github.loadup.components.dfs.test.local;

/*-
 * #%L
 * loadup-components-dfs-binder-local
 * %%
 * Copyright (C) 2026 LoadUp Cloud
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

import static org.junit.jupiter.api.Assertions.*;

import com.github.loadup.components.dfs.binding.AbstractDfsBinding;
import com.github.loadup.components.dfs.local.binding.LocalDfsBinding;
import com.github.loadup.components.dfs.manager.DfsBindingManager;
import com.github.loadup.components.dfs.model.FileMetadata;
import com.github.loadup.components.dfs.model.FileUploadRequest;
import com.github.loadup.components.dfs.test.DfsTestApplication;
import com.github.loadup.framework.api.annotation.BindingClient;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration test for DFS Service using LOCAL provider.
 *
 * <p>This test is located in the local binder module, ensuring complete isolation from other
 * providers. No need to exclude any auto-configurations - the classpath only contains the local
 * binder.
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@SpringBootTest(classes = DfsTestApplication.class)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LocalDfsServiceTest {

  @BindingClient("user-avatar")
  AbstractDfsBinding localBinding;

  @BindingClient("order-pdf")
  AbstractDfsBinding orderBinding;

  private static final String TEST_CONTENT = "DFS Service test content.";
  private static final String TEST_FILENAME = "service-test.txt";

  @BeforeEach
  void setManager() {
    // this.localBinding = manager.getBinding("user-avatar");
  }

  @Autowired private DfsBindingManager manager;

  @Test
  void testDynamicBindingSelection() {
    // 1. 验证通过 Manager 获取到的实例
    AbstractDfsBinding binding = manager.getBinding("user-avatar");
    assertNotNull(binding);

    // 2. 验证注入注解是否生效
    assertNotNull(localBinding);
    assertNotNull(orderBinding);
    assertEquals(binding, localBinding);

    // 3. 验证 Binder 是否被正确装配 (S3 类型)
    // 假设 S3DfsBinding 暴露了获取类型的方法
    assertTrue(binding instanceof LocalDfsBinding);
  }

  @Test
  @Order(1)
  @DisplayName("Should upload file using local provider")
  void testUploadWithDefaultProvider() {
    // Given
    FileUploadRequest request =
        FileUploadRequest.builder()
            .filename(TEST_FILENAME)
            .inputStream(new ByteArrayInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8)))
            .contentType("text/plain")
            .bizType("service-test")
            .build();

    // When
    FileMetadata metadata = localBinding.upload(request);

    // Then
    assertNotNull(metadata);
    assertNotNull(metadata.getFileId());
    assertEquals(TEST_FILENAME, metadata.getFilename());
    assertEquals("local", metadata.getProvider());
  }
}

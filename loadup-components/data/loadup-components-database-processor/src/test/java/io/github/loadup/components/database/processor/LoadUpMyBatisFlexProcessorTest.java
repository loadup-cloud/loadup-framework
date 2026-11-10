/*
 * Copyright (C) 2025 LoadUp Cloud
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
package io.github.loadup.components.database.processor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class LoadUpMyBatisFlexProcessorTest {

    @TempDir
    Path project;

    @ParameterizedTest
    @ValueSource(strings = {"classes", "test-classes"})
    void generatesCompilableSourcesWithoutLocalConfigurationOverrides(String outputDirectory) throws IOException {
        Files.writeString(project.resolve("pom.xml"), "<project/>");
        String conflictingPolicy = "processor.enable=false\nprocessor.mapper.generateEnable=false\n";
        Files.writeString(project.resolve("mybatis-flex.config"), conflictingPolicy);
        Path entity = project.resolve("src/sample/infrastructure/dataobject/AccountDO.java");
        Files.createDirectories(entity.getParent());
        Files.writeString(entity, """
                package sample.infrastructure.dataobject;
                @com.mybatisflex.annotation.Table("account")
                public class AccountDO {
                    public Long id;
                }
                """);
        Path output = Files.createDirectories(project.resolve("target/" + outputDirectory));
        Path generated = Files.createDirectories(project.resolve("target/generated-" + outputDirectory));
        var compiler = ToolProvider.getSystemJavaCompiler();
        assertThat(compiler).as("Run the processor tests with a JDK").isNotNull();
        var diagnostics = new DiagnosticCollector<JavaFileObject>();
        try (var files = compiler.getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8)) {
            var task = compiler.getTask(
                    null,
                    files,
                    diagnostics,
                    List.of(
                            "-classpath",
                            System.getProperty("surefire.test.class.path", System.getProperty("java.class.path")),
                            "-d",
                            output.toString(),
                            "-s",
                            generated.toString()),
                    null,
                    files.getJavaFileObjects(entity.toFile()));
            // The parent POM explicitly selects this entry, not the upstream SPI entry.
            task.setProcessors(List.of(new LoadUpMyBatisFlexProcessor()));
            assertThat(task.call())
                    .as("Compiler diagnostics: %s", diagnostics.getDiagnostics())
                    .isTrue();
        }
        Path mapper = generated.resolve("sample/infrastructure/mapper/AccountMapper.java");
        assertThat(mapper).content().contains("@Mapper", "BaseMapper<AccountDO>");
        assertThat(generated.resolve("sample/infrastructure/table/AccountTableDef.java"))
                .exists();
        assertThat(generated.resolve("sample/infrastructure/table/Tables.java")).exists();
        assertThat(output.resolve("sample/infrastructure/mapper/AccountMapper.class"))
                .exists();
        assertThat(output.resolve("sample/infrastructure/table/AccountTableDef.class"))
                .exists();
        assertThat(output.resolve("sample/infrastructure/table/Tables.class")).exists();
        assertThat(output.resolve("mybatis-flex.config")).content().contains("processor.stopBubbling = true");
        assertThat(project.resolve("mybatis-flex.config")).content().isEqualTo(conflictingPolicy);
    }

    @Test
    void failsCompilationWhenTheConfigurationCannotBeWritten() throws IOException {
        ProcessingEnvironment environment = mock(ProcessingEnvironment.class);
        Filer filer = mock(Filer.class);
        Messager messager = mock(Messager.class);
        IOException failure = new IOException("read-only compiler output");
        when(environment.getFiler()).thenReturn(filer);
        when(environment.getMessager()).thenReturn(messager);
        when(filer.createResource(StandardLocation.CLASS_OUTPUT, "", "mybatis-flex.config"))
                .thenThrow(failure);

        assertThatThrownBy(() -> new LoadUpMyBatisFlexProcessor().init(environment))
                .isInstanceOf(IllegalStateException.class)
                .hasCause(failure);
        verify(messager)
                .printMessage(
                        Diagnostic.Kind.ERROR,
                        "Cannot initialize LoadUp database processor: read-only compiler output");
    }
}

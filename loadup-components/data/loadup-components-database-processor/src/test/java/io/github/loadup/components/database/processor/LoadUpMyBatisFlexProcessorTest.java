/*
 * #%L
 * LoadUp Database Processor
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
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
 * #L%
 */
package io.github.loadup.components.database.processor;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LoadUpMyBatisFlexProcessorTest {
    @TempDir
    Path project;

    @Test
    void generatesCompilablePersistenceSourcesWithFixedDefaults() throws Exception {
        Files.writeString(project.resolve("pom.xml"), "<project/>");
        Files.writeString(project.resolve("mybatis-flex.config"), """
                processor.enable=false
                processor.mapper.generateEnable=false
                processor.mapper.package=external.mapper
                processor.tableDef.ignoreEntitySuffixes=DO
                """);
        Path source = project.resolve("src/main/java/sample/dataobject/PaymentDO.java");
        Files.createDirectories(source.getParent());
        Files.writeString(source, """
                package sample.dataobject;
                import com.mybatisflex.annotation.Table;
                @Table("payment")
                public class PaymentDO {
                    private String id;
                    public String getId() { return id; }
                    public void setId(String id) { this.id = id; }
                }
                """);
        Path output = Files.createDirectories(project.resolve("target/classes"));
        Path generated = Files.createDirectories(project.resolve("target/generated-sources/annotations"));
        var compiler = ToolProvider.getSystemJavaCompiler();
        var diagnostics = new DiagnosticCollector<JavaFileObject>();
        try (var manager = compiler.getStandardFileManager(diagnostics, null, null)) {
            var task = compiler.getTask(
                    null,
                    manager,
                    diagnostics,
                    List.of(
                            "-classpath",
                            System.getProperty("java.class.path"),
                            "-d",
                            output.toString(),
                            "-s",
                            generated.toString()),
                    null,
                    manager.getJavaFileObjects(source));
            task.setProcessors(List.of(new LoadUpMyBatisFlexProcessor()));
            assertThat(task.call()).as(diagnostics.getDiagnostics().toString()).isTrue();
        }
        assertThat(Files.readString(generated.resolve("sample/mapper/PaymentDOMapper.java")))
                .contains("extends BaseMapper<PaymentDO>", "@Mapper");
        assertThat(Files.readString(generated.resolve("sample/dataobject/table/Tables.java")))
                .contains("PAYMENT_DO");
        assertThat(output.resolve("sample/mapper/PaymentDOMapper.class")).exists();
        assertThat(generated.resolve("external/mapper")).doesNotExist();
    }
}

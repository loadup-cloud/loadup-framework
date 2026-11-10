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

import com.mybatisflex.processor.MybatisFlexProcessor;
import com.mybatisflex.processor.util.FileUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

/** Applies LoadUp's persistence conventions before invoking the official generator. */
public final class LoadUpMyBatisFlexProcessor extends MybatisFlexProcessor {

    private static final String CONFIGURATION = "/META-INF/loadup/database/mybatis-flex.config";

    @Override
    public synchronized void init(ProcessingEnvironment environment) {
        try (InputStream source = LoadUpMyBatisFlexProcessor.class.getResourceAsStream(CONFIGURATION)) {
            if (source == null) {
                throw new IOException("Missing bundled database processor configuration: " + CONFIGURATION);
            }
            FileObject configuration =
                    environment.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", "mybatis-flex.config");
            // Flex 1.11.8 searches from CLASS_OUTPUT, bounded by a Maven/Gradle project root.
            Path output = Path.of(configuration.toUri()).getParent();
            if (FileUtil.getProjectRootPath(output.toFile(), 10) == null) {
                throw new IOException("MyBatis-Flex requires a Maven or Gradle project above " + output);
            }
            try (OutputStream destination = configuration.openOutputStream()) {
                source.transferTo(destination);
            }
        } catch (IOException | RuntimeException failure) {
            environment
                    .getMessager()
                    .printMessage(
                            Diagnostic.Kind.ERROR,
                            "Cannot initialize LoadUp database processor: " + failure.getMessage());
            throw new IllegalStateException("Cannot load the fixed LoadUp database generation policy", failure);
        }
        super.init(environment);
    }
}

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

import com.mybatisflex.processor.MybatisFlexProcessor;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.StandardLocation;

/** Applies database-owned defaults before delegating to the upstream generator. */
public final class LoadUpMyBatisFlexProcessor extends AbstractProcessor {
    private static final String CONFIG_RESOURCE = "mybatis-flex.config";
    private static final String CONFIG_FILE = "mybatis-flex.config";

    private final MybatisFlexProcessor delegate = new MybatisFlexProcessor();

    @Override
    public synchronized void init(ProcessingEnvironment environment) {
        super.init(environment);
        // Upstream reads files only. Copying the jar-owned defaults into CLASS_OUTPUT makes them
        // the nearest configuration; stopBubbling keeps ancestor files out of the merge.
        try (InputStream defaults = LoadUpMyBatisFlexProcessor.class.getResourceAsStream(CONFIG_RESOURCE)) {
            if (defaults == null) {
                throw new IllegalStateException("Missing " + CONFIG_RESOURCE + " in the database processor jar");
            }
            try (Reader reader = new InputStreamReader(defaults, StandardCharsets.UTF_8);
                    Writer writer = environment
                            .getFiler()
                            .createResource(StandardLocation.CLASS_OUTPUT, "", CONFIG_FILE)
                            .openWriter()) {
                reader.transferTo(writer);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot configure MyBatis-Flex annotation processing", exception);
        }
        delegate.init(environment);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return delegate.getSupportedAnnotationTypes();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
        return delegate.process(annotations, roundEnvironment);
    }
}

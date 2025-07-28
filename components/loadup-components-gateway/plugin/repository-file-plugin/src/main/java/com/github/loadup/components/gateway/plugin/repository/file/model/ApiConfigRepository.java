/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.gateway.plugin.repository.file.model;

/*-
 * #%L
 * repository-file-plugin
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import com.opencsv.bean.CsvBindByName;
import lombok.Getter;
import lombok.Setter;


/**
 * <p>
 * ApiConfigRepository.java
 * </p>
 */
@Getter
@Setter
public class ApiConfigRepository {

    /**
     * openURl
     */
    @CsvBindByName(column = "open_url")
    private String openURl;
    /**
     * integrationUri
     */
    @CsvBindByName(column = "integration_uri")
    private String integrationUri;
    /**
     * securityStrategyCode
     */
    @CsvBindByName(column = "security_strategy_code")
    private String securityStrategyCode;
    /**
     * headerAssembler
     */
    @CsvBindByName(column = "request_header_assembler")
    private String headerAssembler;
    /**
     * bodyAssembler
     */
    @CsvBindByName(column = "request_body_assembler")
    private String bodyAssembler;
    /**
     * responseParser
     */
    @CsvBindByName(column = "response_parser")
    private String responseParser;
    /**
     * properties
     */
    @CsvBindByName(column = "properties")
    private String communicationProperties;


}

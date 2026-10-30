/*
 * #%L
 * LoadUp Web MVC
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
package io.github.loadup.components.webmvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.loadup.commons.domain.PageResult;
import io.github.loadup.commons.dto.PageQuery;
import io.github.loadup.commons.error.CommonException;
import io.github.loadup.commons.result.PageDTO;
import io.github.loadup.commons.result.PageResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class ApiPaginationContractTest {
    @Test
    void defaultsAndRejectsExplicitInvalidBounds() {
        assertThat(new PageQuery(null, null)).isEqualTo(PageQuery.of(1, 20));
        assertThatThrownBy(() -> PageQuery.of(0, 20)).isInstanceOf(CommonException.class);
        assertThatThrownBy(() -> PageQuery.of(1, 101)).isInstanceOf(CommonException.class);
        assertThatThrownBy(() -> PageQuery.of(1, 0)).isInstanceOf(CommonException.class);
    }

    @Test
    void keepsFlatQueryJsonFields() {
        var mapper = JsonMapper.builder().build();
        PageQuery query = mapper.readValue("{\"page\":2,\"size\":30}", PageQuery.class);
        assertThat(query.pageNum()).isEqualTo(2);
        assertThat(query.pageSize()).isEqualTo(30);
        assertThat(mapper.writeValueAsString(query))
                .contains("\"page\":2", "\"size\":30")
                .doesNotContain("pageNum", "pageSize");
    }

    @Test
    void mappingPreservesMetadataAndResponseEnvelope() {
        var original = PageResult.of(List.of(10, 20), 101L, 3, 20);
        var view = PageDTO.of(original.map(Object::toString));
        assertThat(view.getData()).containsExactly("10", "20");
        assertThat(view.getPageInfo().totalCount()).isEqualTo(101L);
        assertThat(view.getPageInfo().pageIndex()).isEqualTo(3L);
        var tree = JsonMapper.builder().build().valueToTree(PageResponse.of(view));
        assertThat(tree.get("data").isArray()).isTrue();
        assertThat(tree.get("result")).isNotNull();
        assertThat(tree.get("pageInfo").get("pageIndex").asInt()).isEqualTo(3);
        assertThat(tree.has("records")).isFalse();
        assertThat(tree.has("items")).isFalse();
    }

    @Test
    void emptyPageContainsArrayAndDefaults() {
        var page = new PageDTO<String>();
        assertThat(page.getData()).isEmpty();
        assertThat(page.getPageInfo().pageSize()).isEqualTo(20L);
        assertThat(JsonMapper.builder().build().writeValueAsString(PageResponse.of(page)))
                .contains("\"data\":[]");
    }
}

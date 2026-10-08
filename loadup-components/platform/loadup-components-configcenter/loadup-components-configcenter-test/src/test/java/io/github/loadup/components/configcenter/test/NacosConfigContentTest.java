package io.github.loadup.components.configcenter.test;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.components.configcenter.nacos.NacosConfigContent;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class NacosConfigContentTest {

    @Test
    void parseProperties_flattensEntries() {
        Map<String, String> configs =
                NacosConfigContent.parse("feature.enabled=true\nserver.port=8080\n", "properties");

        assertThat(configs).containsEntry("feature.enabled", "true").containsEntry("server.port", "8080");
    }

    @Test
    void parseYaml_flattensNestedMapsAndCollections() {
        String yaml = """
                app:
                  name: loadup
                  replicas: 2
                tags:
                  - a
                  - b
                """;

        Map<String, String> configs = NacosConfigContent.parse(yaml, "yaml");

        assertThat(configs).containsEntry("app.name", "loadup").containsEntry("app.replicas", "2");
        assertThat(configs).containsKey("tags");
        assertThat(configs.get("tags")).contains("\"a\"", "\"b\"");
    }

    @Test
    void renderProperties_roundTrips() {
        Map<String, String> configs = new LinkedHashMap<>();
        configs.put("feature.enabled", "true");
        configs.put("feature.desc", "a=b\nc");

        String rendered = NacosConfigContent.render(configs, "properties");
        Map<String, String> parsed = NacosConfigContent.parse(rendered, "properties");

        assertThat(parsed).isEqualTo(configs);
    }

    @Test
    void renderYaml_roundTrips() {
        Map<String, String> configs = new LinkedHashMap<>();
        configs.put("app.name", "loadup");
        configs.put("app.replicas", "2");
        configs.put("tags", "[\"a\",\"b\"]");

        String rendered = NacosConfigContent.render(configs, "yaml");
        Map<String, String> parsed = NacosConfigContent.parse(rendered, "yaml");

        assertThat(parsed).isEqualTo(configs);
    }

    @Test
    void parse_blankContent_returnsEmptyMap() {
        assertThat(NacosConfigContent.parse(null, "properties")).isEmpty();
        assertThat(NacosConfigContent.parse("  ", "yaml")).isEmpty();
    }
}

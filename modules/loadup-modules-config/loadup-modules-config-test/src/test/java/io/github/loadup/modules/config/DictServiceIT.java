package io.github.loadup.modules.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;
import java.util.List;

import io.github.loadup.modules.config.app.service.DictService;
import io.github.loadup.modules.config.client.command.DictItemCreateCommand;
import io.github.loadup.modules.config.client.command.DictTypeCreateCommand;
import io.github.loadup.modules.config.client.dto.DictItemDTO;
import io.github.loadup.modules.config.client.dto.DictTypeDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(classes = ConfigTestApplication.class)
@EnableTestContainers(ContainerType.MYSQL)
class DictServiceIT {

    @Autowired
    private DictService dictService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM dict_item WHERE dict_code LIKE 'test_%'");
        jdbcTemplate.execute("DELETE FROM dict_type WHERE dict_code LIKE 'test_%'");
    }

    @Test
    void createType_shouldPersist_whenValidCommand() {
        DictTypeCreateCommand cmd = new DictTypeCreateCommand();
        cmd.setDictCode("test_status");
        cmd.setDictName("测试状态");

        String id = dictService.createType(cmd);
        assertThat(id).isNotBlank();

        List<DictTypeDTO> types = dictService.listAllTypes();
        assertThat(types).anyMatch(t -> "test_status".equals(t.getDictCode()));
    }

    @Test
    void createType_shouldThrow_whenCodeDuplicated() {
        DictTypeCreateCommand cmd = new DictTypeCreateCommand();
        cmd.setDictCode("test_dup");
        cmd.setDictName("dup");
        dictService.createType(cmd);

        assertThatThrownBy(() -> dictService.createType(cmd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void createItem_shouldPersist_whenTypeExists() {
        DictTypeCreateCommand typeCmd = new DictTypeCreateCommand();
        typeCmd.setDictCode("test_color");
        typeCmd.setDictName("颜色");
        dictService.createType(typeCmd);

        DictItemCreateCommand itemCmd = new DictItemCreateCommand();
        itemCmd.setDictCode("test_color");
        itemCmd.setItemLabel("红色");
        itemCmd.setItemValue("red");

        String id = dictService.createItem(itemCmd);
        assertThat(id).isNotBlank();

        List<DictItemDTO> items = dictService.getDictData("test_color");
        assertThat(items).anyMatch(i -> "red".equals(i.getItemValue()));
    }

    @Test
    void getDictLabel_shouldReturnLabel_whenExists() {
        DictTypeCreateCommand typeCmd = new DictTypeCreateCommand();
        typeCmd.setDictCode("test_label");
        typeCmd.setDictName("label test");
        dictService.createType(typeCmd);

        DictItemCreateCommand itemCmd = new DictItemCreateCommand();
        itemCmd.setDictCode("test_label");
        itemCmd.setItemLabel("启用");
        itemCmd.setItemValue("1");
        dictService.createItem(itemCmd);

        String label = dictService.getDictLabel("test_label", "1");
        assertThat(label).isEqualTo("启用");
    }

    @Test
    void getDictLabel_shouldReturnNull_whenValueAbsent() {
        assertThat(dictService.getDictLabel("user_status", "999")).isNull();
    }

    @Test
    void deleteType_shouldRemoveTypeAndItems() {
        DictTypeCreateCommand typeCmd = new DictTypeCreateCommand();
        typeCmd.setDictCode("test_to_delete");
        typeCmd.setDictName("to delete");
        dictService.createType(typeCmd);

        DictItemCreateCommand itemCmd = new DictItemCreateCommand();
        itemCmd.setDictCode("test_to_delete");
        itemCmd.setItemLabel("item");
        itemCmd.setItemValue("v1");
        dictService.createItem(itemCmd);

        dictService.deleteType("test_to_delete");

        assertThat(dictService.getDictData("test_to_delete")).isEmpty();
    }
}


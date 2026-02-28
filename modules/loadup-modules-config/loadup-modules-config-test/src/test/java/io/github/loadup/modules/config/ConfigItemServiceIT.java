package io.github.loadup.modules.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.loadup.modules.config.app.service.ConfigItemService;
import io.github.loadup.modules.config.client.command.ConfigItemCreateCommand;
import io.github.loadup.modules.config.client.command.ConfigItemUpdateCommand;
import io.github.loadup.modules.config.client.dto.ConfigItemDTO;
import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;
import io.github.loadup.components.testcontainers.annotation.ContainerType;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(classes = ConfigTestApplication.class)
@EnableTestContainers(ContainerType.MYSQL)
class ConfigItemServiceIT {

    @Autowired
    private ConfigItemService configItemService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM config_item WHERE system_defined = FALSE");
    }

    @Test
    void create_shouldPersist_whenValidCommand() {
        ConfigItemCreateCommand cmd = new ConfigItemCreateCommand();
        cmd.setConfigKey("test.key.create");
        cmd.setConfigValue("hello");
        cmd.setValueType("STRING");
        cmd.setCategory("test");

        String id = configItemService.create(cmd);

        assertThat(id).isNotBlank();
        ConfigItemDTO dto = configItemService.getByKey("test.key.create");
        assertThat(dto).isNotNull();
        assertThat(dto.getConfigValue()).isEqualTo("hello");
    }

    @Test
    void create_shouldThrow_whenKeyDuplicated() {
        ConfigItemCreateCommand cmd = new ConfigItemCreateCommand();
        cmd.setConfigKey("test.key.dup");
        cmd.setConfigValue("v1");
        cmd.setValueType("STRING");
        cmd.setCategory("test");

        configItemService.create(cmd);

        assertThatThrownBy(() -> configItemService.create(cmd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void update_shouldChangeValue_whenEditable() {
        ConfigItemCreateCommand cmd = new ConfigItemCreateCommand();
        cmd.setConfigKey("test.key.update");
        cmd.setConfigValue("original");
        cmd.setValueType("STRING");
        cmd.setCategory("test");
        configItemService.create(cmd);

        ConfigItemUpdateCommand updateCmd = new ConfigItemUpdateCommand();
        updateCmd.setConfigKey("test.key.update");
        updateCmd.setConfigValue("updated");
        configItemService.update(updateCmd);

        assertThat(configItemService.getValue("test.key.update")).isEqualTo("updated");
    }

    @Test
    void delete_shouldRemove_whenNotSystemDefined() {
        ConfigItemCreateCommand cmd = new ConfigItemCreateCommand();
        cmd.setConfigKey("test.key.delete");
        cmd.setConfigValue("v");
        cmd.setValueType("STRING");
        cmd.setCategory("test");
        configItemService.create(cmd);

        configItemService.delete("test.key.delete");

        assertThat(configItemService.getByKey("test.key.delete")).isNull();
    }

    @Test
    void listAll_shouldReturnAllItems() {
        ConfigItemCreateCommand cmd = new ConfigItemCreateCommand();
        cmd.setConfigKey("test.key.list");
        cmd.setConfigValue("v");
        cmd.setValueType("STRING");
        cmd.setCategory("test");
        configItemService.create(cmd);

        List<ConfigItemDTO> list = configItemService.listAll();
        assertThat(list).isNotEmpty();
    }

    @Test
    void getTypedValue_shouldReturnInteger() {
        ConfigItemCreateCommand cmd = new ConfigItemCreateCommand();
        cmd.setConfigKey("test.key.integer");
        cmd.setConfigValue("42");
        cmd.setValueType("INTEGER");
        cmd.setCategory("test");
        configItemService.create(cmd);

        Integer value = configItemService.getTypedValue("test.key.integer", Integer.class, 0);
        assertThat(value).isEqualTo(42);
    }

    @Test
    void getTypedValue_shouldReturnDefault_whenKeyAbsent() {
        Long value = configItemService.getTypedValue("non.existing.key", Long.class, 99L);
        assertThat(value).isEqualTo(99L);
    }
}


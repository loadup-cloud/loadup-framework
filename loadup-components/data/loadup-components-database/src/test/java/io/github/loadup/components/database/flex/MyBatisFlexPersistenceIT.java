package io.github.loadup.components.database.flex;

import static org.assertj.core.api.Assertions.assertThat;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import io.github.loadup.components.database.flex.dataobject.FlexProbeDO;
import io.github.loadup.components.database.flex.mapper.FlexProbeMapper;
import io.github.loadup.components.database.flex.table.Tables;
import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = MyBatisFlexPersistenceIT.Application.class)
@ActiveProfiles("test")
@EnableTestContainers(ContainerType.POSTGRESQL)
class MyBatisFlexPersistenceIT {
    @Autowired
    FlexProbeMapper mapper;

    @Autowired
    JdbcTemplate jdbc;

    private static FlexProbeDO probe(String id, String name) {
        var now = LocalDateTime.of(2026, 1, 1, 0, 0);
        var row = new FlexProbeDO();
        row.setId(id);
        row.setName(name);
        row.setCreatedAt(now);
        row.setUpdatedAt(now);
        row.setDeleted(false);
        return row;
    }

    @BeforeEach
    void resetProbeTable() {
        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS flex_probe (
                id VARCHAR(64) NOT NULL PRIMARY KEY,
                name VARCHAR(64),
                created_at TIMESTAMP(6) NOT NULL,
                updated_at TIMESTAMP(6) NOT NULL,
                deleted BOOLEAN NOT NULL DEFAULT FALSE
            )
            """);
        jdbc.update("DELETE FROM flex_probe");
    }

    @Test
    void generatedMapperReadsAndLogicallyDeletesRows() {
        mapper.insert(probe("probe-1", "first"));

        assertThat(mapper.selectOneById("probe-1")).isNotNull();
        assertThat(mapper.selectListByQuery(QueryWrapper.create().where(Tables.FLEX_PROBE.NAME.eq("first"))))
                .hasSize(1);

        assertThat(mapper.deleteById("probe-1")).isEqualTo(1);

        assertThat(mapper.selectOneById("probe-1")).isNull();
        assertThat(mapper.selectListByQuery(QueryWrapper.create().where(Tables.FLEX_PROBE.NAME.eq("first"))))
                .isEmpty();
        assertThat(jdbc.queryForObject("SELECT deleted FROM flex_probe WHERE id = ?", Boolean.class, "probe-1"))
                .isTrue();
    }

    @Test
    void generatedMapperPaginatesWithOneBasedPageNumbers() {
        mapper.insert(probe("probe-1", "first"));
        mapper.insert(probe("probe-2", "second"));
        mapper.insert(probe("probe-3", "third"));

        var page = mapper.paginate(new Page<>(2, 2), QueryWrapper.create().orderBy(Tables.FLEX_PROBE.ID.asc()));

        assertThat(page.getTotalRow()).isEqualTo(3);
        assertThat(page.getRecords()).extracting(FlexProbeDO::getId).containsExactly("probe-3");
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class Application {}
}

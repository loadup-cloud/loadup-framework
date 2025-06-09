package com.github.loadup.components.testify.executor;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class SqlFileExecutor {

    public static void execute(String filePath, DataSource dataSource) {
        InputStream is = SqlFileExecutor.class.getClassLoader().getResourceAsStream(filePath);
        if (is == null) throw new IllegalArgumentException("SQL file not found: " + filePath);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) lines.add(line);

            List<String> sqls = parseSqlTextToSqls(lines);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            sqls.forEach(sql -> {
                if (StringUtils.isNotBlank(sql)) {
                    log.info("Executing SQL: {}", sql);
                    jdbcTemplate.execute(sql);
                }
            });

        } catch (IOException e) {
            log.error("Failed to read SQL file", e);
            throw new RuntimeException("Error reading SQL file: " + filePath, e);
        }
    }

    private static List<String> parseSqlTextToSqls(List<String> textLines) {
        List<String> sqls = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (String line : textLines) {
            sb.append(line).append(" ");
            if (line.trim().endsWith(";")) {
                sqls.add(sb.toString());
                sb = new StringBuilder();
            }
        }
        return sqls;
    }
}

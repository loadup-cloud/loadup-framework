package io.github.loadup.components.globalunique;

import io.github.loadup.components.globalunique.service.GlobalUniqueService;
import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * GlobalUniqueService 集成测试
 *
 * @author loadup
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@EnableTestContainers(ContainerType.MYSQL)
@DisplayName("GlobalUniqueService 集成测试")
class GlobalUniqueServiceTest {

    @Autowired
    private GlobalUniqueService globalUniqueService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // 清理测试数据，避免脏数据影响测试结果
        jdbcTemplate.execute("DELETE FROM global_unique");
        log.debug("已清理 global_unique 表数据");
    }

    @Test
    @DisplayName("首次插入应返回 true")
    void insertAndCheck_shouldReturnTrue_whenFirstTime() {
        // given
        String uniqueKey = "TEST_ORDER_CREATE:user123:order456";
        String bizType = "ORDER";

        // when
        boolean result = globalUniqueService.insertAndCheck(uniqueKey, bizType);

        // then
        assertThat(result).isTrue();

        // 验证数据库中已存在
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM global_unique WHERE unique_key = ?",
                Integer.class,
                uniqueKey
        );
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("重复插入应返回 false（幂等拦截）")
    void insertAndCheck_shouldReturnFalse_whenDuplicate() {
        // given
        String uniqueKey = "TEST_ORDER_CREATE:user123:order789";
        String bizType = "ORDER";

        // 第一次插入
        boolean firstResult = globalUniqueService.insertAndCheck(uniqueKey, bizType);
        assertThat(firstResult).isTrue();

        // when - 第二次插入（重复）
        boolean secondResult = globalUniqueService.insertAndCheck(uniqueKey, bizType);

        // then
        assertThat(secondResult).isFalse();

        // 验证数据库中只有一条记录
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM global_unique WHERE unique_key = ?",
                Integer.class,
                uniqueKey
        );
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("带业务ID插入应成功")
    void insertAndCheck_shouldSucceed_withBizId() {
        // given
        String uniqueKey = "TEST_PAYMENT:user456:payment123";
        String bizType = "PAYMENT";
        String bizId = "payment123";

        // when
        boolean result = globalUniqueService.insertAndCheck(uniqueKey, bizType, bizId);

        // then
        assertThat(result).isTrue();

        // 验证 biz_id 字段
        String savedBizId = jdbcTemplate.queryForObject(
                "SELECT biz_id FROM global_unique WHERE unique_key = ?",
                String.class,
                uniqueKey
        );
        assertThat(savedBizId).isEqualTo(bizId);
    }

    @Test
    @DisplayName("带请求数据插入应成功")
    void insertAndCheck_shouldSucceed_withRequestData() {
        // given
        String uniqueKey = "TEST_REFUND:user789:refund456";
        String bizType = "REFUND";
        String bizId = "refund456";
        String requestData = "{\"userId\":\"user789\",\"amount\":100.00}";

        // when
        boolean result = globalUniqueService.insertAndCheck(uniqueKey, bizType, bizId, requestData);

        // then
        assertThat(result).isTrue();

        // 验证 request_data 字段
        String savedRequestData = jdbcTemplate.queryForObject(
                "SELECT request_data FROM global_unique WHERE unique_key = ?",
                String.class,
                uniqueKey
        );
        assertThat(savedRequestData).isEqualTo(requestData);
    }

    @Test
    @DisplayName("并发插入同一唯一键只有一个成功")
    void insertAndCheck_shouldOnlyOneSucceed_whenConcurrent() throws InterruptedException {
        // given
        String uniqueKey = "TEST_CONCURRENT:user999:order999";
        String bizType = "ORDER";

        int[] successCount = {0};
        int[] failureCount = {0};

        // when - 模拟并发插入
        Thread t1 = new Thread(() -> {
            if (globalUniqueService.insertAndCheck(uniqueKey, bizType)) {
                successCount[0]++;
            } else {
                failureCount[0]++;
            }
        });

        Thread t2 = new Thread(() -> {
            if (globalUniqueService.insertAndCheck(uniqueKey, bizType)) {
                successCount[0]++;
            } else {
                failureCount[0]++;
            }
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        // then - 只有一个成功
        assertThat(successCount[0]).isEqualTo(1);
        assertThat(failureCount[0]).isEqualTo(1);

        // 验证数据库中只有一条记录
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM global_unique WHERE unique_key = ?",
                Integer.class,
                uniqueKey
        );
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("不同业务类型可以使用相同的唯一键（如果业务允许）")
    void insertAndCheck_shouldAllowSameUniqueKey_forDifferentBizType() {
        // given
        String uniqueKey1 = "TEST_COMMON_KEY:user111";
        String uniqueKey2 = "TEST_COMMON_KEY:user222";
        String bizType1 = "ORDER";
        String bizType2 = "PAYMENT";

        // when
        boolean result1 = globalUniqueService.insertAndCheck(uniqueKey1, bizType1);
        boolean result2 = globalUniqueService.insertAndCheck(uniqueKey2, bizType2);

        // then
        assertThat(result1).isTrue();
        assertThat(result2).isTrue();

        // 验证数据库中有两条记录
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM global_unique WHERE unique_key LIKE 'TEST_COMMON_KEY%'",
                Integer.class
        );
        assertThat(count).isEqualTo(2);
    }
}


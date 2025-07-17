package com.github.loadup.components.retrytask.repository;

/*-
 * #%L
 * loadup-components-retrytask
 * %%
 * Copyright (C) 2022 - 2023 loadup_cloud
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

import com.github.loadup.components.retrytask.model.RetryTaskDO;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository of retry task
 */
@Repository
public interface RetryTaskRepository extends CrudRepository<RetryTaskDO, String> {

    @Query("select * from retry_task where business_id=:businessId and business_type=:businessType for update")
    RetryTaskDO lockByBizId(@Param("businessId") String businessId, @Param("businessType") String businessType);

    @Query("select * from retry_task where business_id=:businessId and business_type=:businessType")
    RetryTaskDO findByBizId(@Param("businessId") String businessId, @Param("businessType") String businessType);

    @Query("delete from retry_task where business_id=:businessId and business_type=:businessType")
    @Modifying
    void delete(@Param("businessId") String businessId, @Param("businessType") String businessType);

    @Query("select * from retry_task where business_type=:businessType and reached_max_retries='0' and processing='0' and next_retry_time < "
            + "now() order by priority desc limit :rowNum")
    List<RetryTaskDO> load(@Param("businessType") String businessType, @Param("rowNum") int rowNum);

    @Query("select * from retry_task where business_type=:businessType and reached_max_retries='0' and processing='0' and next_retry_time < "
            + "now() and priority=:priority limit :rowNum")
    List<RetryTaskDO> loadByPriority(@Param("businessType") String businessType, @Param("priority") String priority, @Param("rowNum") int rowNum);

    @Query("select * from retry_task where business_type=:businessType and reached_max_retries='0' and processing='1' and updated_time < DATE_SUB"
            + "(NOW()" + ", INTERVAL :extremeRetryTime MINUTE) limit :rowNum")
    List<RetryTaskDO> loadUnusualTask(@Param("businessType") String businessType, @Param("extremeRetryTime") int extremeRetryTime,
                                      @Param("rowNum") int rowNum);


}

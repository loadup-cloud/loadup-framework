package io.github.loadup.components.gotone.repository;

/*-
 * #%L
 * loadup-components-gotone-api
 * %%
 * Copyright (C) 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import io.github.loadup.components.gotone.dataobject.ChannelMappingDO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** 渠道映射仓储 */
@Repository
public interface ChannelMappingRepository extends CrudRepository<ChannelMappingDO, String> {

    @Query(
            "SELECT * FROM gotone_channel_mapping WHERE business_code = :businessCode AND enabled = true ORDER BY priority DESC")
    List<ChannelMappingDO> findByBusinessCodeAndEnabled(@Param("businessCode") String businessCode);

    @Query(
            "SELECT * FROM gotone_channel_mapping WHERE business_code = :businessCode AND channel = :channel AND enabled = true")
    Optional<ChannelMappingDO> findByBusinessCodeAndChannelAndEnabled(
            @Param("businessCode") String businessCode, @Param("channel") String channel);
}

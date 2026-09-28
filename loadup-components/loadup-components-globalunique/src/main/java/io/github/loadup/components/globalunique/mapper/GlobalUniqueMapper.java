package io.github.loadup.components.globalunique.mapper;

import com.mybatisflex.core.BaseMapper;
import io.github.loadup.components.globalunique.dataobject.GlobalUniqueDO;
import org.apache.ibatis.annotations.Mapper;

/** MyBatis-Flex mapper for global unique claims. */
@Mapper
public interface GlobalUniqueMapper extends BaseMapper<GlobalUniqueDO> {}

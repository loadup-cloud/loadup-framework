package com.github.loadup.modules.upms.infrastructure.mapper;

import com.github.loadup.modules.upms.domain.entity.User;
import com.github.loadup.modules.upms.infrastructure.dataobject.UserDO;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * User Entity <-> UserDO Mapper
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

  UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

  /**
   * Convert UserDO to User Entity
   *
   * @param userDO data object
   * @return domain entity
   */
  User toEntity(UserDO userDO);

  /**
   * Convert User Entity to UserDO
   *
   * @param user domain entity
   * @return data object
   */
  UserDO toDO(User user);

  /**
   * Convert UserDO list to User Entity list
   *
   * @param userDOList data object list
   * @return domain entity list
   */
  List<User> toEntityList(List<UserDO> userDOList);

  /**
   * Convert User Entity list to UserDO list
   *
   * @param userList domain entity list
   * @return data object list
   */
  List<UserDO> toDOList(List<User> userList);
}

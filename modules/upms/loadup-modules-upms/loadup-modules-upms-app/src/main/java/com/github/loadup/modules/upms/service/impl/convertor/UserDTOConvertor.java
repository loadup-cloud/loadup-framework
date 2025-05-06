package com.github.loadup.modules.upms.service.impl.convertor;

/*-
 * #%L
 * loadup-modules-upms-app
 * %%
 * Copyright (C) 2022 - 2024 loadup_cloud
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

import com.github.loadup.modules.upms.client.dto.SimpleUserDTO;
import com.github.loadup.modules.upms.client.dto.UserDTO;
import com.github.loadup.modules.upms.convertor.UserNameConvertor;
import com.github.loadup.modules.upms.domain.UpmsUser;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(uses = {UserNameConvertor.class, DepartDTOConvertor.class, PositionDTOConvertor.class})
public interface UserDTOConvertor {
    UserDTOConvertor INSTANCE = Mappers.getMapper(UserDTOConvertor.class);

    UpmsUser toUser(UserDTO dto);

    UpmsUser toUser(SimpleUserDTO dto);

    List<UpmsUser> toUserList(List<UserDTO> dtoList);

    UserDTO toUserDTO(UpmsUser domain);

    List<UserDTO> toUserDTOList(List<UpmsUser> domainList);

    SimpleUserDTO toSimpleUserDTO(UpmsUser domain);

    List<SimpleUserDTO> toSimpleUserDTOList(List<UpmsUser> domainList);

}

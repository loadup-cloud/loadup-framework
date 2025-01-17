package com.github.loadup.components.upms.dal.dataobject;

/*-
 * #%L
 * loadup-components-upms-infrastructure
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

import com.github.loadup.commons.dataobject.BaseDO;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serial;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Table("upms_user")
public class UserDO extends BaseDO {
	@Serial
	private static final long          serialVersionUID = 3152323013685154107L;
	@Id
	private              String        id;
	private              String        nickname;
	private              String        englishName;
	private              String        officialName;
	private              String        password;
	private              String        salt;
	private              String        avatar;
	private              LocalDate     birthday;
	private              LocalDateTime registeredTime;
	private              String        userType;

}

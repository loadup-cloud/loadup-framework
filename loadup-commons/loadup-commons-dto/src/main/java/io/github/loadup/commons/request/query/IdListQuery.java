package io.github.loadup.commons.request.query;

/*-
 * #%L
 * loadup-commons-dto
 * %%
 * Copyright (C) 2022 - 2024 loadup_cloud
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

import io.github.loadup.commons.dto.DTO;
import jakarta.validation.constraints.NotBlank;
import java.io.Serial;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IdListQuery extends DTO {
    @Serial
    private static final long serialVersionUID = 7157141921495739675L;

    private List<String> idList;

    public static IdListQuery of(@NotBlank List<String> idList) {
        IdListQuery idQuery = new IdListQuery();
        idQuery.setIdList(idList);
        return idQuery;
    }
}

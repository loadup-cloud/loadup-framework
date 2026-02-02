package io.github.loadup.modules.upms.domain.gateway;

/*-
 * #%L
 * Loadup Modules UPMS Domain Layer
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
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

import io.github.loadup.modules.upms.domain.entity.Department;
import java.util.List;
import java.util.Optional;

/**
 * Department Repository Interface
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public interface DepartmentGateway {

    /** Save department */
    Department save(Department department);

    /** Update department */
    Department update(Department department);

    /** Delete department by ID */
    void deleteById(String id);

    /** Find department by ID */
    Optional<Department> findById(String id);

    /** Find department by code */
    Optional<Department> findByDeptCode(String deptCode);

    /** Find departments by parent ID */
    List<Department> findByParentId(String parentId);

    /** Find all departments */
    List<Department> findAll();

    /** Find enabled departments */
    List<Department> findAllEnabled();

    /** Find root departments */
    List<Department> findRootDepartments();

    /** Check if department code exists */
    boolean existsByDeptCode(String deptCode);

    /** Check if department has children */
    boolean hasChildren(String deptId);

    /** Check if department has users */
    boolean hasUsers(String deptId);

    /** Build department tree */
    List<Department> buildTree();
}

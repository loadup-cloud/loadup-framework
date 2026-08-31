package io.github.loadup.modules.upms.domain.gateway;

/*-
 * #%L
 * Loadup Modules UPMS Domain Layer
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

    /**
     * Save department
     */
    Department save(Department department);

    /**
     * Update department
     */
    Department update(Department department);

    /**
     * Delete department by ID
     */
    void deleteById(String id);

    /**
     * Find department by ID
     */
    Optional<Department> findById(String id);

    /**
     * Find department by code
     */
    Optional<Department> findByDeptCode(String deptCode);

    /**
     * Find departments by parent ID
     */
    List<Department> findByParentId(String parentId);

    /**
     * Find all departments
     */
    List<Department> findAll();

    /**
     * Find enabled departments
     */
    List<Department> findAllEnabled();

    /**
     * Find root departments
     */
    List<Department> findRootDepartments();

    /**
     * Check if department code exists
     */
    boolean existsByDeptCode(String deptCode);

    /**
     * Check if department has children
     */
    boolean hasChildren(String deptId);

    /**
     * Check if department has users
     */
    boolean hasUsers(String deptId);

    /**
     * Build department tree
     */
    List<Department> buildTree();
}

package io.github.loadup.modules.upms.infrastructure.gateway;

/*-
 * #%L
 * loadup-modules-upms-infrastructure
 * %%
 * Copyright (C) 2022 - 2026 loadup_cloud
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

import static io.github.loadup.modules.upms.infrastructure.dataobject.table.Tables.DEPARTMENT_DO;
import static io.github.loadup.modules.upms.infrastructure.dataobject.table.Tables.USER_DO;

import com.mybatisflex.core.query.QueryWrapper;
import io.github.loadup.modules.upms.domain.entity.Department;
import io.github.loadup.modules.upms.domain.gateway.DepartmentGateway;
import io.github.loadup.modules.upms.infrastructure.converter.DepartmentConverter;
import io.github.loadup.modules.upms.infrastructure.dataobject.DepartmentDO;
import io.github.loadup.modules.upms.infrastructure.mapper.DepartmentDOMapper;
import io.github.loadup.modules.upms.infrastructure.mapper.UserDOMapper;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * Department Repository Implementation using MyBatis-Flex
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class DepartmentGatewayImpl implements DepartmentGateway {

    private final DepartmentDOMapper departmentDOMapper;
    private final UserDOMapper userDOMapper;
    private final DepartmentConverter departmentConverter;

    @Override
    public Department save(Department department) {
        DepartmentDO departmentDO = departmentConverter.toDataObject(department);
        departmentDOMapper.insert(departmentDO);
        department = departmentConverter.toEntity(departmentDO);
        return department;
    }

    @Override
    public Department update(Department department) {
        DepartmentDO departmentDO = departmentConverter.toDataObject(department);
        departmentDOMapper.update(departmentDO);
        department = departmentConverter.toEntity(departmentDO);
        return department;
    }

    @Override
    public void deleteById(String id) {
        departmentDOMapper.deleteById(id);
    }

    @Override
    public Optional<Department> findById(String id) {
        DepartmentDO departmentDO = departmentDOMapper.selectOneById(id);
        return Optional.ofNullable(departmentDO).map(departmentConverter::toEntity);
    }

    @Override
    public Optional<Department> findByDeptCode(String deptCode) {
        QueryWrapper query = QueryWrapper.create().where(DEPARTMENT_DO.DEPT_CODE.eq(deptCode));
        DepartmentDO departmentDO = departmentDOMapper.selectOneByQuery(query);
        return Optional.ofNullable(departmentDO).map(departmentConverter::toEntity);
    }

    @Override
    public List<Department> findByParentId(String parentId) {
        QueryWrapper query = QueryWrapper.create().where(DEPARTMENT_DO.PARENT_ID.eq(parentId));
        List<DepartmentDO> departmentDOs = departmentDOMapper.selectListByQuery(query);
        return departmentDOs.stream().map(departmentConverter::toEntity).collect(Collectors.toList());
    }

    @Override
    public List<Department> findAll() {
        List<DepartmentDO> departmentDOs = departmentDOMapper.selectAll();
        return departmentDOs.stream().map(departmentConverter::toEntity).collect(Collectors.toList());
    }

    @Override
    public List<Department> findAllEnabled() {
        QueryWrapper query = QueryWrapper.create().where(DEPARTMENT_DO.STATUS.eq((short) 1));
        List<DepartmentDO> departmentDOs = departmentDOMapper.selectListByQuery(query);
        return departmentDOs.stream().map(departmentConverter::toEntity).collect(Collectors.toList());
    }

    @Override
    public List<Department> findRootDepartments() {
        QueryWrapper query =
                QueryWrapper.create().where(DEPARTMENT_DO.PARENT_ID.isNull().or(DEPARTMENT_DO.PARENT_ID.eq("0")));
        List<DepartmentDO> departmentDOs = departmentDOMapper.selectListByQuery(query);
        return departmentDOs.stream().map(departmentConverter::toEntity).collect(Collectors.toList());
    }

    @Override
    public boolean existsByDeptCode(String deptCode) {
        QueryWrapper query = QueryWrapper.create().where(DEPARTMENT_DO.DEPT_CODE.eq(deptCode));
        return departmentDOMapper.selectCountByQuery(query) > 0;
    }

    @Override
    public boolean hasChildren(String deptId) {
        QueryWrapper query = QueryWrapper.create().where(DEPARTMENT_DO.PARENT_ID.eq(deptId));
        return departmentDOMapper.selectCountByQuery(query) > 0;
    }

    @Override
    public boolean hasUsers(String deptId) {
        QueryWrapper query = QueryWrapper.create().where(USER_DO.DEPT_ID.eq(deptId));
        return userDOMapper.selectCountByQuery(query) > 0;
    }

    @Override
    public List<Department> buildTree() {
        // Get all departments
        List<DepartmentDO> allDepartments = departmentDOMapper.selectAll();
        List<Department> departments =
                allDepartments.stream().map(departmentConverter::toEntity).collect(Collectors.toList());

        // Build tree structure: find root nodes and recursively build children
        return departments.stream()
                .filter(dept -> dept.getParentId() == null || "0".equals(dept.getParentId()))
                .peek(dept -> buildChildren(dept, departments))
                .collect(Collectors.toList());
    }

    /** Recursively build children for a department */
    private void buildChildren(Department parent, List<Department> allDepartments) {
        List<Department> children = allDepartments.stream()
                .filter(dept -> parent.getId().equals(dept.getParentId()))
                .peek(child -> buildChildren(child, allDepartments))
                .collect(Collectors.toList());
        parent.setChildren(children);
    }
}

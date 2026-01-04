-- Test Data for UPMS Module

-- Insert test departments
INSERT INTO upms_department (id, parent_id, dept_name, dept_code, dept_level, sort_order, status, deleted, tenant_id, created_by,
                             create_time)
VALUES ('1', '0', 'LoadUp科技', 'LOADUP', 1, 0, 1, FALSE, '1', '1', CURRENT_TIMESTAMP),
       ('2', '1', '研发部', 'RD', 2, 1, 1, FALSE, '1', '1', CURRENT_TIMESTAMP),
       ('3', '1', '产品部', 'PD', 2, 2, 1, FALSE, '1', '1', CURRENT_TIMESTAMP),
       ('4', '2', '后端组', 'RD_BACKEND', 3, 1, 1, FALSE, '1', '1', CURRENT_TIMESTAMP),
       ('5', '2', '前端组', 'RD_FRONTEND', 3, 2, 1, FALSE, '1', '1', CURRENT_TIMESTAMP);

-- Insert test users
INSERT INTO upms_user (id, username, password, nickname, email, phone, status, dept_id, deleted, tenant_id, created_by, create_time)
VALUES ('1', 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '管理员', 'admin@loadup.com', '13800138000', 1, '1',
        FALSE, '1', '1', CURRENT_TIMESTAMP),
       ('2', 'developer', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '开发者', 'dev@loadup.com', '13800138001', 1, '4',
        FALSE, '1', '1', CURRENT_TIMESTAMP),
       ('3', 'tester', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '测试员', 'test@loadup.com', '13800138002', 1, '2',
        FALSE, '1', '1', CURRENT_TIMESTAMP);

-- Insert test roles
INSERT INTO upms_role (id, role_name, role_code, role_level, data_scope, status, deleted, tenant_id, created_by, create_time)
VALUES ('1', '超级管理员', 'SUPER_ADMIN', 1, 1, 1, FALSE, '1', '1', CURRENT_TIMESTAMP),
       ('2', '部门管理员', 'DEPT_ADMIN', 2, 2, 1, FALSE, '1', '1', CURRENT_TIMESTAMP),
       ('3', '普通用户', 'USER', 3, 3, 1, FALSE, '1', '1', CURRENT_TIMESTAMP);

-- Insert test permissions
INSERT INTO upms_permission (id, parent_id, permission_name, permission_code, permission_type, sort_order, status, deleted, tenant_id,
                             created_by, create_time)
VALUES ('1', '0', '系统管理', 'system', 1, 1, 1, FALSE, '1', '1', CURRENT_TIMESTAMP),
       ('2', '1', '用户管理', 'system:user', 2, 1, 1, FALSE, '1', '1', CURRENT_TIMESTAMP),
       ('3', '2', '用户查询', 'system:user:query', 3, 1, 1, FALSE, '1', '1', CURRENT_TIMESTAMP),
       ('4', '2', '用户新增', 'system:user:add', 3, 2, 1, FALSE, '1', '1', CURRENT_TIMESTAMP),
       ('5', '1', '角色管理', 'system:role', 2, 2, 1, FALSE, '1', '1', CURRENT_TIMESTAMP);

-- Insert user-role relations
INSERT INTO upms_user_role (id, user_id, role_id, tenant_id, created_by, create_time)
VALUES ('1', '1', '1', '1', '1', CURRENT_TIMESTAMP),
       ('2', '2', '3', '1', '1', CURRENT_TIMESTAMP),
       ('3', '3', '3', '1', '1', CURRENT_TIMESTAMP);

-- Insert role-permission relations
INSERT INTO upms_role_permission (id, role_id, permission_id, tenant_id, created_by, create_time)
VALUES ('1', '1', '1', '1', '1', CURRENT_TIMESTAMP),
       ('2', '1', '2', '1', '1', CURRENT_TIMESTAMP),
       ('3', '1', '3', '1', '1', CURRENT_TIMESTAMP),
       ('4', '1', '4', '1', '1', CURRENT_TIMESTAMP),
       ('5', '1', '5', '1', '1', CURRENT_TIMESTAMP),
       ('6', '3', '3', '1', '1', CURRENT_TIMESTAMP);


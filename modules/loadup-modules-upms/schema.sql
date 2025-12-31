-- ============================================================================
-- LoadUp UPMS (User Permission Management System) Database Schema
-- RBAC3 Model: Role-Based Access Control with Role Hierarchy and Constraints
-- Database: PostgreSQL 14+
-- Encoding: UTF-8
-- ============================================================================

-- ============================================================================
-- 1. Department (组织架构/部门表)
-- 支持无限层级树状结构
-- ============================================================================
CREATE TABLE IF NOT EXISTS upms_department (
    id BIGSERIAL PRIMARY KEY,
    parent_id BIGINT DEFAULT 0 COMMENT '父部门ID，0表示根部门',
    dept_name VARCHAR(100) NOT NULL COMMENT '部门名称',
    dept_code VARCHAR(50) NOT NULL UNIQUE COMMENT '部门编码',
    dept_level INT NOT NULL DEFAULT 1 COMMENT '部门层级',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序序号',
    leader_user_id BIGINT COMMENT '部门负责人ID',
    phone VARCHAR(20) COMMENT '联系电话',
    email VARCHAR(100) COMMENT '联系邮箱',
    status SMALLINT NOT NULL DEFAULT 1 COMMENT '状态：1-正常 0-停用',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '逻辑删除标记',
    remark VARCHAR(500) COMMENT '备注',
    created_by BIGINT NOT NULL COMMENT '创建人ID',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT COMMENT '更新人ID',
    updated_time TIMESTAMP COMMENT '更新时间',
    CONSTRAINT fk_dept_parent FOREIGN KEY (parent_id) REFERENCES upms_department(id) ON DELETE CASCADE
);

CREATE INDEX idx_dept_parent_id ON upms_department(parent_id);
CREATE INDEX idx_dept_code ON upms_department(dept_code);
CREATE INDEX idx_dept_status ON upms_department(status);

COMMENT ON TABLE upms_department IS '部门表-支持树状层级结构';

-- ============================================================================
-- 2. User (用户表)
-- 核心用户信息
-- ============================================================================
CREATE TABLE IF NOT EXISTS upms_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(200) NOT NULL COMMENT '密码(BCrypt加密)',
    nickname VARCHAR(50) COMMENT '昵称',
    real_name VARCHAR(50) COMMENT '真实姓名',
    dept_id BIGINT NOT NULL COMMENT '所属部门ID',
    email VARCHAR(100) UNIQUE COMMENT '邮箱',
    email_verified BOOLEAN NOT NULL DEFAULT FALSE COMMENT '邮箱是否验证',
    phone VARCHAR(20) UNIQUE COMMENT '手机号',
    phone_verified BOOLEAN NOT NULL DEFAULT FALSE COMMENT '手机是否验证',
    avatar_url VARCHAR(500) COMMENT '头像地址(DFS文件ID)',
    gender SMALLINT DEFAULT 0 COMMENT '性别：0-未知 1-男 2-女',
    birthday DATE COMMENT '生日',
    status SMALLINT NOT NULL DEFAULT 1 COMMENT '状态：1-正常 0-停用 2-锁定',
    account_non_expired BOOLEAN NOT NULL DEFAULT TRUE COMMENT '账号是否未过期',
    account_non_locked BOOLEAN NOT NULL DEFAULT TRUE COMMENT '账号是否未锁定',
    credentials_non_expired BOOLEAN NOT NULL DEFAULT TRUE COMMENT '密码是否未过期',
    last_login_time TIMESTAMP COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) COMMENT '最后登录IP',
    login_fail_count INT NOT NULL DEFAULT 0 COMMENT '登录失败次数',
    locked_time TIMESTAMP COMMENT '锁定时间',
    password_update_time TIMESTAMP COMMENT '密码更新时间',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '逻辑删除标记',
    remark VARCHAR(500) COMMENT '备注',
    created_by BIGINT NOT NULL COMMENT '创建人ID',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT COMMENT '更新人ID',
    updated_time TIMESTAMP COMMENT '更新时间',
    CONSTRAINT fk_user_dept FOREIGN KEY (dept_id) REFERENCES upms_department(id)
);

CREATE INDEX idx_user_username ON upms_user(username);
CREATE INDEX idx_user_email ON upms_user(email);
CREATE INDEX idx_user_phone ON upms_user(phone);
CREATE INDEX idx_user_dept_id ON upms_user(dept_id);
CREATE INDEX idx_user_status ON upms_user(status);

COMMENT ON TABLE upms_user IS '用户表';

-- ============================================================================
-- 3. Role (角色表)
-- 支持角色继承（RBAC3特性）
-- ============================================================================
CREATE TABLE IF NOT EXISTS upms_role (
    id BIGSERIAL PRIMARY KEY,
    role_name VARCHAR(100) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    parent_role_id BIGINT COMMENT '父角色ID，用于角色继承',
    role_level INT NOT NULL DEFAULT 1 COMMENT '角色层级',
    data_scope SMALLINT NOT NULL DEFAULT 1 COMMENT '数据权限范围：1-全部 2-自定义 3-本部门 4-本部门及子部门 5-仅本人',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序序号',
    status SMALLINT NOT NULL DEFAULT 1 COMMENT '状态：1-正常 0-停用',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '逻辑删除标记',
    remark VARCHAR(500) COMMENT '备注',
    created_by BIGINT NOT NULL COMMENT '创建人ID',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT COMMENT '更新人ID',
    updated_time TIMESTAMP COMMENT '更新时间',
    CONSTRAINT fk_role_parent FOREIGN KEY (parent_role_id) REFERENCES upms_role(id) ON DELETE SET NULL
);

CREATE INDEX idx_role_code ON upms_role(role_code);
CREATE INDEX idx_role_parent_id ON upms_role(parent_role_id);
CREATE INDEX idx_role_status ON upms_role(status);

COMMENT ON TABLE upms_role IS '角色表-支持角色继承';

-- ============================================================================
-- 4. Permission (权限表)
-- 资源权限定义
-- ============================================================================
CREATE TABLE IF NOT EXISTS upms_permission (
    id BIGSERIAL PRIMARY KEY,
    parent_id BIGINT DEFAULT 0 COMMENT '父权限ID，0表示根权限',
    permission_name VARCHAR(100) NOT NULL COMMENT '权限名称',
    permission_code VARCHAR(100) NOT NULL UNIQUE COMMENT '权限编码',
    permission_type SMALLINT NOT NULL COMMENT '权限类型：1-菜单 2-按钮 3-接口',
    resource_path VARCHAR(200) COMMENT '资源路径/URL',
    http_method VARCHAR(10) COMMENT 'HTTP方法：GET/POST/PUT/DELETE等',
    icon VARCHAR(100) COMMENT '图标',
    component_path VARCHAR(200) COMMENT '前端组件路径',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序序号',
    visible BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否可见',
    status SMALLINT NOT NULL DEFAULT 1 COMMENT '状态：1-正常 0-停用',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '逻辑删除标记',
    remark VARCHAR(500) COMMENT '备注',
    created_by BIGINT NOT NULL COMMENT '创建人ID',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT COMMENT '更新人ID',
    updated_time TIMESTAMP COMMENT '更新时间'
);

CREATE INDEX idx_perm_parent_id ON upms_permission(parent_id);
CREATE INDEX idx_perm_code ON upms_permission(permission_code);
CREATE INDEX idx_perm_type ON upms_permission(permission_type);
CREATE INDEX idx_perm_status ON upms_permission(status);

COMMENT ON TABLE upms_permission IS '权限表';

-- ============================================================================
-- 5. User-Role Relation (用户角色关联表)
-- 多对多关系
-- ============================================================================
CREATE TABLE IF NOT EXISTS upms_user_role (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    created_by BIGINT NOT NULL COMMENT '创建人ID',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES upms_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES upms_role(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_role UNIQUE (user_id, role_id)
);

CREATE INDEX idx_ur_user_id ON upms_user_role(user_id);
CREATE INDEX idx_ur_role_id ON upms_user_role(role_id);

COMMENT ON TABLE upms_user_role IS '用户角色关联表';

-- ============================================================================
-- 6. Role-Permission Relation (角色权限关联表)
-- 多对多关系
-- ============================================================================
CREATE TABLE IF NOT EXISTS upms_role_permission (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    created_by BIGINT NOT NULL COMMENT '创建人ID',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    CONSTRAINT fk_rp_role FOREIGN KEY (role_id) REFERENCES upms_role(id) ON DELETE CASCADE,
    CONSTRAINT fk_rp_perm FOREIGN KEY (permission_id) REFERENCES upms_permission(id) ON DELETE CASCADE,
    CONSTRAINT uk_role_permission UNIQUE (role_id, permission_id)
);

CREATE INDEX idx_rp_role_id ON upms_role_permission(role_id);
CREATE INDEX idx_rp_perm_id ON upms_role_permission(permission_id);

COMMENT ON TABLE upms_role_permission IS '角色权限关联表';

-- ============================================================================
-- 7. Role-Department Relation (角色部门关联表)
-- 支持按部门维度授权
-- ============================================================================
CREATE TABLE IF NOT EXISTS upms_role_department (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL COMMENT '角色ID',
    dept_id BIGINT NOT NULL COMMENT '部门ID',
    created_by BIGINT NOT NULL COMMENT '创建人ID',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    CONSTRAINT fk_rd_role FOREIGN KEY (role_id) REFERENCES upms_role(id) ON DELETE CASCADE,
    CONSTRAINT fk_rd_dept FOREIGN KEY (dept_id) REFERENCES upms_department(id) ON DELETE CASCADE,
    CONSTRAINT uk_role_dept UNIQUE (role_id, dept_id)
);

CREATE INDEX idx_rd_role_id ON upms_role_department(role_id);
CREATE INDEX idx_rd_dept_id ON upms_role_department(dept_id);

COMMENT ON TABLE upms_role_department IS '角色部门关联表-数据权限';

-- ============================================================================
-- 8. Operation Log (操作日志表)
-- AOP异步记录用户操作行为
-- ============================================================================
CREATE TABLE IF NOT EXISTS upms_operation_log (
    id BIGSERIAL PRIMARY KEY,
    trace_id VARCHAR(64) COMMENT '链路追踪ID',
    user_id BIGINT COMMENT '操作用户ID',
    username VARCHAR(50) COMMENT '操作用户名',
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型：CREATE/UPDATE/DELETE/QUERY/LOGIN/LOGOUT等',
    operation_module VARCHAR(100) NOT NULL COMMENT '操作模块',
    operation_desc VARCHAR(500) COMMENT '操作描述',
    request_method VARCHAR(10) COMMENT 'HTTP方法',
    request_url VARCHAR(500) COMMENT '请求URL',
    request_params TEXT COMMENT '请求参数(JSON)',
    response_result TEXT COMMENT '响应结果(JSON)',
    ip_address VARCHAR(50) COMMENT '操作IP',
    user_agent VARCHAR(500) COMMENT '用户代理',
    execution_time BIGINT COMMENT '执行耗时(毫秒)',
    status SMALLINT NOT NULL DEFAULT 1 COMMENT '执行状态：1-成功 0-失败',
    error_message TEXT COMMENT '错误信息',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
);

CREATE INDEX idx_log_user_id ON upms_operation_log(user_id);
CREATE INDEX idx_log_operation_type ON upms_operation_log(operation_type);
CREATE INDEX idx_log_module ON upms_operation_log(operation_module);
CREATE INDEX idx_log_created_time ON upms_operation_log(created_time);
CREATE INDEX idx_log_trace_id ON upms_operation_log(trace_id);
CREATE INDEX idx_log_status ON upms_operation_log(status);

COMMENT ON TABLE upms_operation_log IS '操作日志表';

-- ============================================================================
-- 9. Login Log (登录日志表)
-- 记录用户登录行为
-- ============================================================================
CREATE TABLE IF NOT EXISTS upms_login_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    login_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    logout_time TIMESTAMP COMMENT '登出时间',
    ip_address VARCHAR(50) COMMENT '登录IP',
    login_location VARCHAR(100) COMMENT '登录地点',
    browser VARCHAR(50) COMMENT '浏览器类型',
    os VARCHAR(50) COMMENT '操作系统',
    login_status SMALLINT NOT NULL DEFAULT 1 COMMENT '登录状态：1-成功 0-失败',
    login_message VARCHAR(500) COMMENT '登录信息/失败原因',
    CONSTRAINT fk_login_user FOREIGN KEY (user_id) REFERENCES upms_user(id) ON DELETE CASCADE
);

CREATE INDEX idx_login_user_id ON upms_login_log(user_id);
CREATE INDEX idx_login_time ON upms_login_log(login_time);
CREATE INDEX idx_login_status ON upms_login_log(login_status);

COMMENT ON TABLE upms_login_log IS '登录日志表';

-- ============================================================================
-- 10. User Social Account (用户第三方社交账号表)
-- 支持第三方登录（微信、QQ、GitHub等）
-- ============================================================================
CREATE TABLE IF NOT EXISTS upms_user_social (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    provider VARCHAR(50) NOT NULL COMMENT '第三方平台：wechat/qq/github/gitee等',
    open_id VARCHAR(200) NOT NULL COMMENT '第三方平台OpenID',
    union_id VARCHAR(200) COMMENT '第三方平台UnionID',
    nickname VARCHAR(100) COMMENT '第三方昵称',
    avatar_url VARCHAR(500) COMMENT '第三方头像',
    access_token VARCHAR(500) COMMENT '访问令牌',
    refresh_token VARCHAR(500) COMMENT '刷新令牌',
    expires_in BIGINT COMMENT '令牌过期时间(秒)',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time TIMESTAMP COMMENT '更新时间',
    CONSTRAINT fk_social_user FOREIGN KEY (user_id) REFERENCES upms_user(id) ON DELETE CASCADE,
    CONSTRAINT uk_provider_openid UNIQUE (provider, open_id)
);

CREATE INDEX idx_social_user_id ON upms_user_social(user_id);
CREATE INDEX idx_social_provider ON upms_user_social(provider);
CREATE INDEX idx_social_openid ON upms_user_social(open_id);

COMMENT ON TABLE upms_user_social IS '用户第三方社交账号表';

-- ============================================================================
-- 11. Password Reset Token (密码重置令牌表)
-- 用于找回密码功能
-- ============================================================================
CREATE TABLE IF NOT EXISTS upms_password_reset_token (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    token VARCHAR(200) NOT NULL UNIQUE COMMENT '重置令牌',
    expire_time TIMESTAMP NOT NULL COMMENT '过期时间',
    used BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已使用',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    CONSTRAINT fk_reset_user FOREIGN KEY (user_id) REFERENCES upms_user(id) ON DELETE CASCADE
);

CREATE INDEX idx_reset_token ON upms_password_reset_token(token);
CREATE INDEX idx_reset_user_id ON upms_password_reset_token(user_id);
CREATE INDEX idx_reset_expire ON upms_password_reset_token(expire_time);

COMMENT ON TABLE upms_password_reset_token IS '密码重置令牌表';

-- ============================================================================
-- 12. User Config (用户配置表)
-- 用户个性化配置
-- ============================================================================
CREATE TABLE IF NOT EXISTS upms_user_config (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    theme VARCHAR(50) DEFAULT 'light' COMMENT '主题：light/dark',
    language VARCHAR(20) DEFAULT 'zh_CN' COMMENT '语言：zh_CN/en_US等',
    timezone VARCHAR(50) DEFAULT 'Asia/Shanghai' COMMENT '时区',
    notification_enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用通知',
    config_json TEXT COMMENT '其他配置(JSON格式)',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time TIMESTAMP COMMENT '更新时间',
    CONSTRAINT fk_config_user FOREIGN KEY (user_id) REFERENCES upms_user(id) ON DELETE CASCADE
);

CREATE INDEX idx_config_user_id ON upms_user_config(user_id);

COMMENT ON TABLE upms_user_config IS '用户配置表';

-- ============================================================================
-- Initialize Data (初始化数据)
-- ============================================================================

-- 1. 初始化根部门
INSERT INTO upms_department (id, parent_id, dept_name, dept_code, dept_level, sort_order, status, created_by, created_time)
VALUES (1, 0, '总公司', 'ROOT', 1, 0, 1, 1, CURRENT_TIMESTAMP);

-- 2. 初始化超级管理员角色
INSERT INTO upms_role (id, role_name, role_code, parent_role_id, role_level, data_scope, sort_order, status, created_by, created_time)
VALUES (1, '超级管理员', 'ROLE_SUPER_ADMIN', NULL, 1, 1, 0, 1, 1, CURRENT_TIMESTAMP);

-- 3. 初始化超级管理员用户 (默认密码: admin123，需要BCrypt加密)
-- BCrypt($2a$10$) of "admin123" = $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iYS5TuIG
INSERT INTO upms_user (id, username, password, nickname, real_name, dept_id, email, phone, status, created_by, created_time)
VALUES (1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iYS5TuIG', '超级管理员', 'Admin', 1, 'admin@loadup.com', '13800138000', 1, 1, CURRENT_TIMESTAMP);

-- 4. 关联超级管理员和角色
INSERT INTO upms_user_role (user_id, role_id, created_by, created_time)
VALUES (1, 1, 1, CURRENT_TIMESTAMP);

-- 5. 初始化权限（示例）
INSERT INTO upms_permission (id, parent_id, permission_name, permission_code, permission_type, resource_path, sort_order, status, created_by, created_time)
VALUES 
(1, 0, '系统管理', 'system', 1, '/system', 1, 1, 1, CURRENT_TIMESTAMP),
(2, 1, '用户管理', 'system:user', 1, '/system/user', 1, 1, 1, CURRENT_TIMESTAMP),
(3, 2, '用户查询', 'system:user:query', 2, NULL, 1, 1, 1, CURRENT_TIMESTAMP),
(4, 2, '用户新增', 'system:user:create', 2, NULL, 2, 1, 1, CURRENT_TIMESTAMP),
(5, 2, '用户修改', 'system:user:update', 2, NULL, 3, 1, 1, CURRENT_TIMESTAMP),
(6, 2, '用户删除', 'system:user:delete', 2, NULL, 4, 1, 1, CURRENT_TIMESTAMP),
(7, 1, '角色管理', 'system:role', 1, '/system/role', 2, 1, 1, CURRENT_TIMESTAMP),
(8, 1, '部门管理', 'system:dept', 1, '/system/dept', 3, 1, 1, CURRENT_TIMESTAMP),
(9, 1, '权限管理', 'system:permission', 1, '/system/permission', 4, 1, 1, CURRENT_TIMESTAMP);

-- 6. 关联超级管理员角色和所有权限
INSERT INTO upms_role_permission (role_id, permission_id, created_by, created_time)
SELECT 1, id, 1, CURRENT_TIMESTAMP FROM upms_permission;

-- ============================================================================
-- Sequence Reset (序列重置)
-- ============================================================================
SELECT setval('upms_department_id_seq', (SELECT MAX(id) FROM upms_department));
SELECT setval('upms_user_id_seq', (SELECT MAX(id) FROM upms_user));
SELECT setval('upms_role_id_seq', (SELECT MAX(id) FROM upms_role));
SELECT setval('upms_permission_id_seq', (SELECT MAX(id) FROM upms_permission));

-- ============================================================================
-- End of Schema
-- ============================================================================

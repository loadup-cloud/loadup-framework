export interface Department {
  id: string
  parentId: string | null
  deptName: string
  deptCode: string
  sortOrder: number | null
  leaderUserId: string | null
  mobile: string | null
  email: string | null
  status: number
  remark: string | null
  children?: Department[]
}

export interface Permission {
  id: string
  parentId: string | null
  permissionName: string
  permissionCode: string
  permissionType: number
  resourcePath: string | null
  httpMethod: string | null
  icon: string | null
  componentPath: string | null
  sortOrder: number | null
  visible: boolean
  status: number
  remark: string | null
  children?: Permission[]
}

export interface Role {
  id: string
  roleName: string
  roleCode: string
  parentId: string | null
  dataScope: number
  sortOrder: number | null
  status: number
  permissions?: Permission[]
  departmentIds?: string[]
  children?: Role[]
  remark: string | null
}

export interface User {
  id: string
  username: string
  nickname: string
  realName: string | null
  deptId: string | null
  deptName: string | null
  email: string | null
  mobile: string | null
  gender: number | null
  status: number
  roles: Role[]
  remark: string | null
}

export interface UserQuery {
  username?: string
  status?: number
  page: number
  size: number
}

export interface RoleQuery {
  roleName?: string
  status?: number
  page: number
  size: number
}

export interface UserForm {
  username: string
  password: string
  nickname: string
  realName: string
  deptId: string
  email: string
  mobile: string
  status: number
  roleIds: string[]
  remark: string
}

export interface RoleForm {
  roleName: string
  roleCode: string
  parentId: string
  dataScope: number
  sortOrder: number
  status: number
  permissionIds: string[]
  departmentIds: string[]
  remark: string
}

export interface DepartmentForm {
  parentId: string
  deptName: string
  deptCode: string
  sortOrder: number
  mobile: string
  email: string
  status: number
  remark: string
}

export interface PermissionForm {
  parentId: string
  permissionName: string
  permissionCode: string
  permissionType: number
  resourcePath: string
  icon: string
  componentPath: string
  sortOrder: number
  visible: boolean
  status: number
  remark: string
}

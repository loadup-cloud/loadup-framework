import request from '@/request'
import type {
  Department,
  DepartmentForm,
  Permission,
  PermissionForm,
  Role,
  RoleForm,
  RoleQuery,
  User,
  UserForm,
  UserQuery
} from './types'

const post = <T>(path: string, data: object) => request.post<T>({ url: `/api/upms/${path}`, data })
const byId = (id: string) => ({ id })

export const listUsers = (query: UserQuery) => post<User[]>('user/list', query)
export const getUser = (id: string) => post<User>('user/detail', byId(id))
export const createUser = (form: UserForm) =>
  post<User>('user/create', {
    ...form,
    deptId: form.deptId || null,
    email: form.email || null,
    mobile: form.mobile || null
  })
export const updateUser = (id: string, form: UserForm) =>
  post<User>('user/update', {
    id,
    nickname: form.nickname,
    realName: form.realName || undefined,
    deptId: form.deptId,
    email: form.email || undefined,
    mobile: form.mobile || undefined,
    status: form.status,
    roleIds: form.roleIds,
    remark: form.remark
  })
export const deleteUser = (id: string) => post<void>('user/delete', byId(id))

export const listRoles = (query: RoleQuery) => post<Role[]>('role/list', query)
export const getRole = (id: string) => post<Role>('role/detail', byId(id))
export const roleTree = () => post<Role[]>('role/tree', {})
export const createRole = (form: RoleForm) =>
  post<Role>('role/create', { ...form, parentId: form.parentId || null })
export const updateRole = (id: string, form: RoleForm) =>
  post<Role>('role/update', {
    id,
    roleName: form.roleName,
    ...(form.parentId ? { parentId: form.parentId } : {}),
    dataScope: form.dataScope,
    sortOrder: form.sortOrder,
    status: form.status,
    permissionIds: form.permissionIds,
    departmentIds: form.dataScope === 2 ? form.departmentIds : [],
    remark: form.remark
  })
export const deleteRole = (id: string) => post<void>('role/delete', byId(id))

export const departmentTree = () => post<Department[]>('department/tree', {})
export const getDepartment = (id: string) => post<Department>('department/detail', byId(id))
export const createDepartment = (form: DepartmentForm) =>
  post<Department>('department/create', {
    ...form,
    parentId: form.parentId || '0',
    mobile: form.mobile || null,
    email: form.email || null
  })
export const updateDepartment = (id: string, form: DepartmentForm) =>
  post<Department>('department/update', {
    id,
    deptName: form.deptName,
    sortOrder: form.sortOrder,
    mobile: form.mobile,
    email: form.email,
    status: form.status,
    remark: form.remark
  })
export const moveDepartment = (deptId: string, newParentId: string) =>
  post<void>('department/move', { deptId, newParentId: newParentId || '0' })
export const deleteDepartment = (id: string) => post<void>('department/delete', byId(id))

export const permissionTree = () => post<Permission[]>('permission/tree', {})
export const getPermission = (id: string) => post<Permission>('permission/detail', byId(id))
export const createPermission = (form: PermissionForm) =>
  post<Permission>('permission/create', {
    ...form,
    parentId: form.parentId || '0',
    resourcePath: form.resourcePath || null,
    icon: form.icon || null,
    componentPath: form.componentPath || null,
    httpMethod: form.permissionType === 3 ? 'POST' : null
  })
export const updatePermission = (id: string, form: PermissionForm) =>
  post<Permission>('permission/update', {
    id,
    parentId: form.parentId || '0',
    permissionName: form.permissionName,
    permissionType: form.permissionType,
    resourcePath: form.resourcePath,
    icon: form.icon,
    componentPath: form.componentPath,
    httpMethod: form.permissionType === 3 ? 'POST' : '',
    sortOrder: form.sortOrder,
    visible: form.visible,
    status: form.status,
    remark: form.remark
  })
export const deletePermission = (id: string) => post<void>('permission/delete', byId(id))

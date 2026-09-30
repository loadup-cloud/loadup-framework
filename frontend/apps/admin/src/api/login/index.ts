import request from '@/request'
import type { LoginParams, LoginResult, PermissionMenu, TokenData } from './types'

const useMock = import.meta.env.VITE_USE_MOCK === 'true'
const upmsPages: Record<string, string> = {
  '/system/user': 'views/System/User',
  '/system/role': 'views/System/Role',
  '/system/dept': 'views/System/Department',
  '/system/perm': 'views/System/Permission'
}

export const loginApi = async (credentials: LoginParams) => {
  if (useMock) return request.post<LoginResult>({ url: '/mock/user/login', data: credentials })

  const response = await request.post<TokenData>({ url: '/api/auth/login', data: credentials })
  return {
    data: {
      accessToken: response.data.accessToken,
      user: { username: credentials.username }
    }
  }
}

export const logoutApi = () =>
  useMock ? request.post({ url: '/mock/user/loginOut', data: {} }) : Promise.resolve()

const menuToRoutes = (menus: PermissionMenu[], parentPath = ''): AppCustomRouteRecordRaw[] =>
  [...menus]
    .filter((menu) => Boolean(menu.resourcePath))
    .sort((a, b) => (a.sortOrder ?? 0) - (b.sortOrder ?? 0))
    .map((menu) => {
      const absolutePath = menu.resourcePath!
      const path =
        parentPath && absolutePath.startsWith(`${parentPath}/`)
          ? absolutePath.slice(parentPath.length + 1)
          : absolutePath
      const children = menuToRoutes(menu.children ?? [], absolutePath)
      const component = children.length
        ? parentPath
          ? '##'
          : '#'
        : upmsPages[absolutePath] || menu.componentPath || 'views/System/Placeholder'
      const firstChild = children[0]
      const redirect = firstChild
        ? firstChild.path.startsWith('/')
          ? firstChild.path
          : `${absolutePath}/${firstChild.path}`
        : undefined
      return {
        path,
        name: `Permission_${menu.id}`,
        component,
        redirect,
        meta: {
          title: menu.permissionName,
          icon: menu.icon || 'mdi:folder-outline',
          noCache: Boolean(upmsPages[absolutePath])
        },
        children: children.length ? children : undefined
      }
    })

export const getRouteListApi = async () => {
  if (useMock) return request.post<AppCustomRouteRecordRaw[]>({ url: '/mock/role/list', data: {} })

  const response = await request.post<PermissionMenu[]>({
    url: '/api/upms/permission/user-menu',
    data: {}
  })
  return { data: menuToRoutes(response.data) }
}

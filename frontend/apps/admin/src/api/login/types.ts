export interface LoginParams {
  username: string
  password: string
}

export interface UserInfo {
  username: string
}

export interface LoginResult {
  accessToken: string
  user: UserInfo
}

export interface TokenData {
  accessToken: string
  tokenType: string
  expiresIn: number
}

export interface PermissionMenu {
  id: string
  permissionName: string
  resourcePath: string | null
  componentPath: string | null
  icon: string | null
  sortOrder: number | null
  children: PermissionMenu[] | null
}

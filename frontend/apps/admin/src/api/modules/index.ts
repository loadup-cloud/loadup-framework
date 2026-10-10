import request from '@/request'

const post = <T>(url: string, data: object) => request.post<T>({ url, data })

export interface PageQuery {
  page: number
  size: number
}
export interface AuditEvent {
  id: string
  actorId: string | null
  action: string
  method: string
  path: string
  outcome: string
  traceId: string | null
  occurredAt: string
}
export interface DictionaryType {
  id: string
  code: string
  name: string
  description: string | null
  enabled: boolean
}
export interface DictionaryItem {
  id: string
  value: string
  label: string
  description: string | null
  sortOrder: number
  enabled: boolean
}
export interface FileResource {
  id: string
  ownerId: string
  filename: string
  contentType: string | null
  size: number
  provider: string
  createdAt: string
  updatedAt: string
}
export interface FileReference {
  id: string
  fileId: string
  referenceType: string
  referenceId: string
}
export interface Notification {
  id: string
  senderId: string | null
  category: string
  title: string
  body: string
  actionUrl: string | null
  readAt: string | null
  createdAt: string
}
export interface TransferTask {
  id: string
  ownerId: string
  kind: 'IMPORT' | 'EXPORT'
  handlerKey: string
  sourceFileId: string | null
  resultFileId: string | null
  status: string
  processedCount: number
  totalCount: number
  errorMessage: string | null
  createdAt: string
  startedAt: string | null
  finishedAt: string | null
}
export interface SecurityOverview {
  userId: string
  username: string
  active: boolean
  accountNonLocked: boolean
  loginFailCount: number
  passwordUpdatedAt: string | null
  lastLoginAt: string | null
  lastLoginIp: string | null
}
export interface LoginEntry {
  id: string
  loginAt: string
  ipAddress: string | null
  success: boolean
  loginType: string
}

export const queryAudits = (
  data: PageQuery & {
    actorId?: string
    action?: string
    outcome?: string
    from?: string
    to?: string
  }
) => post<AuditEvent[]>('/api/audit/events/query', data)

export const listDictionaryTypes = (data: PageQuery) =>
  post<DictionaryType[]>('/api/dictionaries/types/list', data)
export const createDictionaryType = (data: object) =>
  post<DictionaryType>('/api/dictionaries/types/create', data)
export const updateDictionaryType = (id: string, command: object) =>
  post<DictionaryType>('/api/dictionaries/types/update', { ...command, id })
export const deleteDictionaryType = (id: string) =>
  post<void>('/api/dictionaries/types/delete', { id })
export const listDictionaryItems = (typeCode: string, data: PageQuery) =>
  post<DictionaryItem[]>('/api/dictionaries/items/list', { typeCode, ...data })
export const createDictionaryItem = (typeCode: string, command: object) =>
  post<DictionaryItem>('/api/dictionaries/items/create', { ...command, typeCode })
export const updateDictionaryItem = (id: string, command: object) =>
  post<DictionaryItem>('/api/dictionaries/items/update', { ...command, id })
export const deleteDictionaryItem = (id: string) =>
  post<void>('/api/dictionaries/items/delete', { id })

export const listFiles = (data: PageQuery & { ownerId?: string }) =>
  post<FileResource[]>('/api/files/list', data)
export const fileReferences = (id: string) => post<FileReference[]>('/api/files/references', { id })
export const deleteFile = (id: string) => post<void>('/api/files/delete', { id })
export const retryFileCleanup = (limit = 100) => post<number>('/api/files/cleanup', { limit })
export const uploadFile = (file: File) => {
  const data = new FormData()
  data.append('file', file)
  return request.upload<FileResource>({ url: '/api/files', data })
}
export const downloadFile = (id: string) =>
  request.download({ url: `/api/files/${encodeURIComponent(id)}/content` })

export const listNotifications = (data: PageQuery & { unreadOnly: boolean }) =>
  post<Notification[]>('/api/notifications/list', data)
export const unreadCount = () => post<{ count: number }>('/api/notifications/unread-count', {})
export const markNotificationRead = (id: string) => post<void>('/api/notifications/read', { id })
export const markAllNotificationsRead = () =>
  post<{ updated: number }>('/api/notifications/read-all', {})
export const archiveNotification = (id: string) => post<void>('/api/notifications/archive', { id })
export const publishNotification = (data: object) =>
  post<{ delivered: number }>('/api/notifications/publish', data)

export const listTransferTasks = (data: PageQuery & { ownerId?: string }) =>
  post<TransferTask[]>('/api/transfer-tasks/list', data)
export const submitImport = (data: object) =>
  post<TransferTask>('/api/transfer-tasks/imports', data)
export const submitExport = (data: object) =>
  post<TransferTask>('/api/transfer-tasks/exports', data)
export const retryTransferTask = (id: string) =>
  post<TransferTask>('/api/transfer-tasks/retry', { id })

export const securityOverview = () => post<SecurityOverview>('/api/account/security/overview', {})
export const loginHistory = (data: PageQuery) =>
  post<LoginEntry[]>('/api/account/security/logins', data)
export const changePassword = (data: {
  oldPassword: string
  newPassword: string
  confirmPassword: string
}) => post<void>('/api/account/security/password', data)

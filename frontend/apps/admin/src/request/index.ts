import { ElMessage } from 'element-plus'
import { createRequest, isCancel, type AxiosResponse, type RequestConfig } from '@vea/request'
import { CONTENT_TYPE, REQUEST_TIMEOUT, SUCCESS_CODE } from '@/constants'
import { useUserStoreWithOut } from '@/store/modules/user'

export interface ApiResponse<Data = unknown> {
  code?: number
  result?: { code: string; status: string; message?: string }
  data: Data
  pageInfo?: { totalCount: number; pageSize: number; pageIndex: number }
  message?: string
}

const normalizeRequestKey = (url = '') => url.replace(/^\/mock(?=\/)/, '')
const useBrowserMock = import.meta.env.PROD && import.meta.env.VITE_USE_MOCK === 'true'

const client = createRequest({
  axiosConfig: {
    baseURL: import.meta.env.VITE_API_BASE_PATH,
    adapter: useBrowserMock ? 'fetch' : undefined,
    timeout: REQUEST_TIMEOUT
  },
  beforeRequest(config) {
    const userStore = useUserStoreWithOut()
    return {
      ...config,
      headers: {
        'Content-Type': CONTENT_TYPE,
        ...(userStore.token ? { Authorization: `Bearer ${userStore.token}` } : {}),
        ...config.headers
      }
    }
  },
  transformResponse(response: AxiosResponse) {
    if (response.config.responseType === 'blob') return response

    const result = response.data as ApiResponse
    if (result.code === SUCCESS_CODE || result.result?.status === 'S') return result

    if (result.code === 401 || result.result?.code === 'UNAUTHENTICATED') {
      void useUserStoreWithOut().logout()
    }
    const code = result.result?.code ?? result.code
    throw Object.assign(
      new Error(result.result?.message || result.message || `Request failed (${code})`),
      {
        code
      }
    )
  },
  onError(error) {
    if (!isCancel(error)) {
      ElMessage.error(error instanceof Error ? error.message : String(error))
    }
  },
  getRequestKey: (config) => normalizeRequestKey(config.url)
})

export default {
  post: <Data = unknown>(config: RequestConfig) => client.post<ApiResponse<Data>>(config),
  cancelRequest: (url: string | string[]) => {
    const keys = (Array.isArray(url) ? url : [url]).map(normalizeRequestKey)
    client.cancelRequest(keys)
  },
  cancelAllRequest: client.cancelAllRequest
}

import analysisMocks from './analysis/index.mock'
import roleMocks from './role/index.mock'
import userMocks from './user/index.mock'

const mockList = [...analysisMocks, ...roleMocks, ...userMocks]
type BrowserMock = (typeof mockList)[number]
const originalFetch = window.fetch.bind(window)

const wait = (timeout = 0, signal?: AbortSignal | null) => {
  if (!timeout) return Promise.resolve()

  return new Promise<void>((resolve, reject) => {
    const abort = () => {
      clearTimeout(timer)
      reject(signal?.reason ?? new DOMException('The request was aborted', 'AbortError'))
    }
    const timer = window.setTimeout(() => {
      signal?.removeEventListener('abort', abort)
      resolve()
    }, timeout)

    if (signal?.aborted) abort()
    else signal?.addEventListener('abort', abort, { once: true })
  })
}

const parseBody = async (request: Request) => {
  if (request.method === 'GET' || request.method === 'HEAD') return {}

  const text = await request.clone().text()
  if (!text) return {}

  try {
    return JSON.parse(text)
  } catch {
    return text
  }
}

const handleMockRequest = async (request: Request, mock: BrowserMock) => {
  await wait('delay' in mock ? mock.delay : 0, request.signal)

  const body = await parseBody(request)
  const data = typeof mock.body === 'function' ? await mock.body({ body }) : mock.body

  return new Response(JSON.stringify(data), {
    status: 200,
    headers: { 'Content-Type': 'application/json' }
  })
}

export const setupProdMockServer = () => {
  window.fetch = async (input, init) => {
    const request = new Request(input, init)
    const url = new URL(request.url)
    const method = request.method.toUpperCase()
    const mock = mockList.find(
      (item) => item.url === url.pathname && (item.method ?? 'GET') === method
    )

    return mock ? handleMockRequest(request, mock) : originalFetch(request)
  }
}

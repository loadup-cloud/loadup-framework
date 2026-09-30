const modules = import.meta.glob('../views/**/*.{vue,tsx}')

/* Layout */
export const Layout = () => import('@/layout/Layout.vue')

export const isUrl = (path: string) => /^(?:https?:|mailto:|tel:)/.test(path)

// 将接口返回的组件路径转换为可注册的路由组件
export const generateRoutesByServer = (routes: AppCustomRouteRecordRaw[]): AppRouteRecordRaw[] => {
  const res: AppRouteRecordRaw[] = []

  for (const route of routes) {
    const data: AppRouteRecordRaw = {
      path: route.path,
      name: route.name,
      redirect: route.redirect,
      meta: route.meta
    }
    if (route.component) {
      const comModule = modules[`../${route.component}.vue`] || modules[`../${route.component}.tsx`]
      const component = route.component as string
      if (component === '#') {
        data.component = Layout
      } else if (component !== '##' && !comModule) {
        data.component = modules['../views/System/Placeholder.vue']
      } else if (component !== '##') {
        data.component = comModule
      }
    }
    // recursive child routes
    if (route.children) {
      data.children = generateRoutesByServer(route.children)
    }
    res.push(data as AppRouteRecordRaw)
  }
  return res
}

export const pathResolve = (parentPath: string, path: string) => {
  if (isUrl(path) || path.startsWith('/')) return path
  const childPath = path ? `/${path}` : path
  return `${parentPath}${childPath}`.replace(/\/\//g, '/').trim()
}

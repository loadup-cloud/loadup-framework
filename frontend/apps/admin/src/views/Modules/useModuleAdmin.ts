import { computed } from 'vue'
import { usePermissionStore } from '@/store/modules/permission'

export const useModuleAdmin = () => {
  const permissionStore = usePermissionStore()
  return computed(() =>
    permissionStore.addRouters.some(
      (route) => route.path === '/system' && route.children?.some((child) => child.path === 'audit')
    )
  )
}

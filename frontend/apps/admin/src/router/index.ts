import { createRouter, createWebHashHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import type { App } from 'vue'
import { Layout } from '@/utils/routerHelper'
import { NO_RESET_WHITE_LIST } from '@/constants'

export const constantRouterMap: AppRouteRecordRaw[] = [
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard/analysis',
    name: 'Root',
    meta: {
      hidden: true
    }
  },
  {
    path: '/dashboard',
    component: Layout,
    redirect: '/dashboard/analysis',
    name: 'Dashboard',
    meta: { title: 'router.dashboard', icon: 'mdi:view-dashboard' },
    children: [
      {
        path: 'analysis',
        component: () => import('@/views/Dashboard/Analysis.vue'),
        name: 'Analysis',
        meta: { title: 'router.dashboard', icon: 'mdi:view-dashboard', affix: true }
      }
    ]
  },
  {
    path: '/workspace',
    component: Layout,
    redirect: '/workspace/notifications',
    name: 'Workspace',
    meta: { title: '工作台', icon: 'mdi:briefcase-outline' },
    children: [
      {
        path: 'notifications',
        component: () => import('@/views/Modules/Notifications.vue'),
        name: 'Notifications',
        meta: { title: '站内通知', icon: 'mdi:bell-outline', noCache: true }
      },
      {
        path: 'files',
        component: () => import('@/views/Modules/Files.vue'),
        name: 'Files',
        meta: { title: '文件资源', icon: 'mdi:folder-outline', noCache: true }
      },
      {
        path: 'transfers',
        component: () => import('@/views/Modules/Transfers.vue'),
        name: 'Transfers',
        meta: { title: '导入导出', icon: 'mdi:swap-horizontal', noCache: true }
      },
      {
        path: 'security',
        component: () => import('@/views/Modules/AccountSecurity.vue'),
        name: 'AccountSecurity',
        meta: { title: '账号安全', icon: 'mdi:shield-account-outline', noCache: true }
      }
    ]
  },
  {
    path: '/merchants',
    component: Layout,
    redirect: '/merchants/list',
    name: 'MerchantManagement',
    meta: { title: '商户管理', icon: 'mdi:store-outline' },
    children: [
      {
        path: 'list',
        component: () => import('@/views/Merchant/Index.vue'),
        name: 'MerchantList',
        meta: { title: '商户信息', icon: 'mdi:store-outline', noCache: true }
      }
    ]
  },
  {
    path: '/contract',
    component: Layout,
    redirect: '/contract/products',
    name: 'ContractDrafts',
    meta: { title: '合约管理', icon: 'mdi:file-document-edit-outline' },
    children: [
      {
        path: 'products',
        component: () => import('@/views/Contract/Products.vue'),
        name: 'ContractProducts',
        meta: { title: '产品目录', icon: 'mdi:package-variant', noCache: true }
      },
      {
        path: 'conditions',
        component: () => import('@/views/Contract/Conditions.vue'),
        name: 'ContractConditions',
        meta: { title: '条件配置', icon: 'mdi:source-branch', noCache: true }
      },
      {
        path: 'bundles',
        component: () => import('@/views/Contract/Bundles.vue'),
        name: 'ContractBundles',
        meta: { title: '产品组合', icon: 'mdi:layers-outline', noCache: true }
      },
      {
        path: 'sales-plans',
        component: () => import('@/views/Contract/SalesPlans.vue'),
        name: 'ContractSalesPlans',
        meta: { title: '销售方案', icon: 'mdi:clipboard-text-outline', noCache: true }
      },
      {
        path: 'merchant-contracts',
        component: () => import('@/views/Contract/MerchantContracts.vue'),
        name: 'ContractMerchantContracts',
        meta: { title: '商户合约', icon: 'mdi:file-sign', noCache: true }
      }
    ]
  },
  {
    path: '/redirect',
    component: Layout,
    name: 'RedirectWrap',
    children: [
      {
        path: '/redirect/:path(.*)',
        name: 'Redirect',
        component: () => import('@/views/Redirect/Redirect.vue'),
        meta: {}
      }
    ],
    meta: {
      hidden: true,
      noTagsView: true
    }
  },
  {
    path: '/login',
    component: () => import('@/views/Login/Login.vue'),
    name: 'Login',
    meta: {
      hidden: true,
      title: 'router.login',
      noTagsView: true
    }
  },
  {
    path: '/404',
    component: () => import('@/views/Error/404.vue'),
    name: 'NoFind',
    meta: {
      hidden: true,
      title: '404',
      noTagsView: true
    }
  },
  {
    path: '/:pathMatch(.*)*',
    component: () => import('@/views/Error/404.vue'),
    name: 'Fallback',
    meta: {
      hidden: true,
      breadcrumb: false,
      noTagsView: true
    }
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  strict: true,
  routes: constantRouterMap as RouteRecordRaw[],
  scrollBehavior: () => ({ left: 0, top: 0 })
})

export const resetRouter = (): void => {
  router.getRoutes().forEach((route) => {
    const { name } = route
    if (name && !NO_RESET_WHITE_LIST.includes(name as string) && router.hasRoute(name)) {
      router.removeRoute(name)
    }
  })
}

export const setupRouter = (app: App<Element>) => {
  app.use(router)
}

export default router

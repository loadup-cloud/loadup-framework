<script setup lang="ts">
  import { ElCard } from 'element-plus'
  import { useUserStore } from '@/store/modules/user'

  const userStore = useUserStore()
</script>

<template>
  <div class="p-20px">
    <ElCard shadow="never">
      <h1 class="m-0 text-22px">LoadUp Console</h1>
      <p class="mb-0 mt-12px text-14px">
        欢迎，{{ userStore.userInfo?.username }}。系统菜单由 UPMS 权限接口提供。
      </p>
    </ElCard>
  </div>
</template>

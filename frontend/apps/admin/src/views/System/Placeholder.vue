<script setup lang="ts">
  import { computed } from 'vue'
  import { ElCard, ElEmpty } from 'element-plus'
  import { useRoute } from 'vue-router'

  const route = useRoute()
  const title = computed(() => String(route.meta.title || '功能页面'))
</script>

<template>
  <div class="p-20px">
    <ElCard shadow="never">
      <h1 class="m-0 text-20px">{{ title }}</h1>
      <ElEmpty description="页面待开发" />
    </ElCard>
  </div>
</template>

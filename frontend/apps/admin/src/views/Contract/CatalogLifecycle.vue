<script setup lang="ts">
  import { computed, ref } from 'vue'
  import { ElButton, ElMessage, ElMessageBox, ElTag } from 'element-plus'
  import type { CatalogKind } from '@/api/contract'
  import { useContractDrafts, type CatalogDraft } from './drafts'

  const props = defineProps<{ kind: CatalogKind; item: CatalogDraft }>()
  const drafts = useContractDrafts()
  const busy = ref(false)
  const stored = computed(() => {
    const records =
      props.kind === 'PRODUCT'
        ? drafts.products
        : props.kind === 'CONDITION'
          ? drafts.conditions
          : props.kind === 'BUNDLE'
            ? drafts.bundles
            : drafts.plans
    return records.find((item) => item.id === props.item.id)
  })
  const comparable = (value: unknown): unknown => {
    if (Array.isArray(value)) return value.map(comparable)
    if (value !== null && typeof value === 'object')
      return Object.fromEntries(
        Object.entries(value)
          .filter(([key]) => key !== 'id')
          .sort(([left], [right]) => left.localeCompare(right))
          .map(([key, entry]) => [key, comparable(entry)])
      )
    return value
  }
  const dirty = computed(
    () =>
      JSON.stringify(comparable(props.item)) !== JSON.stringify(comparable(stored.value))
  )
  const change = async (action: 'publish' | 'retire') => {
    try {
      await ElMessageBox.confirm(
        action === 'publish'
          ? '发布后此版本不可编辑，确定发布？'
          : '下架后此版本不可再用于新的引用，确定下架？',
        '确认目录状态',
        { type: 'warning' }
      )
    } catch {
      return
    }
    busy.value = true
    try {
      await drafts.transition(props.kind, props.item, action)
      ElMessage.success(action === 'publish' ? '已发布' : '已下架')
    } catch {
      // The request client displays the server error. Reload to pick up a newer row version.
      await drafts.loadAll().catch(() => undefined)
    } finally {
      busy.value = false
    }
  }
</script>

<template>
  <span v-if="item.status" class="inline-flex items-center gap-8px">
    <ElTag
      :type="
        item.status === 'PUBLISHED' ? 'success' : item.status === 'RETIRED' ? 'info' : 'warning'
      "
      >{{
        item.status === 'DRAFT' ? '未发布' : item.status === 'PUBLISHED' ? '已发布' : '已下架'
      }}</ElTag
    >
    <ElButton
      v-if="item.status === 'DRAFT'"
      link
      type="primary"
      :loading="busy"
      :disabled="dirty"
      title="请先保存修改后发布"
      @click="change('publish')"
      >发布</ElButton
    >
    <ElButton
      v-if="item.status === 'PUBLISHED'"
      link
      type="danger"
      :loading="busy"
      @click="change('retire')"
      >下架</ElButton
    >
  </span>
</template>

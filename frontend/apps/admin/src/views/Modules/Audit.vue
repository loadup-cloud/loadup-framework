<script setup lang="ts">
  import { onMounted, reactive, ref } from 'vue'
  import {
    ElButton,
    ElCard,
    ElDatePicker,
    ElInput,
    ElPagination,
    ElSelect,
    ElOption,
    ElTable,
    ElTableColumn,
    ElTag
  } from 'element-plus'
  import { queryAudits, type AuditEvent } from '@/api/modules'

  const rows = ref<AuditEvent[]>([])
  const total = ref(0)
  const loading = ref(false)
  const query = reactive({
    actorId: '',
    action: '',
    outcome: '',
    range: [] as string[] | null,
    page: 1,
    size: 20
  })
  const load = async () => {
    loading.value = true
    try {
      const response = await queryAudits({
        actorId: query.actorId.trim() || undefined,
        action: query.action.trim() || undefined,
        outcome: query.outcome || undefined,
        from: query.range?.[0],
        to: query.range?.[1],
        page: query.page,
        size: query.size
      })
      rows.value = response.data
      total.value = response.pageInfo?.totalCount ?? rows.value.length
    } catch {
      rows.value = []
      total.value = 0
    } finally {
      loading.value = false
    }
  }
  const search = () => {
    query.page = 1
    void load()
  }
  onMounted(() => {
    void load()
  })
</script>

<template>
  <div class="p-20px">
    <ElCard shadow="never">
      <div class="mb-18px flex items-center justify-between"
        ><h1 class="m-0 text-20px">审计中心</h1><ElButton @click="load">刷新</ElButton></div
      >
      <div class="mb-16px flex flex-wrap gap-10px">
        <ElInput
          v-model="query.actorId"
          placeholder="操作者 ID"
          clearable
          class="!w-180px"
          @keyup.enter="search"
        />
        <ElInput
          v-model="query.action"
          placeholder="操作"
          clearable
          class="!w-180px"
          @keyup.enter="search"
        />
        <ElSelect v-model="query.outcome" placeholder="全部结果" clearable class="!w-140px"
          ><ElOption label="成功" value="SUCCESS" /><ElOption label="失败" value="FAILURE"
        /></ElSelect>
        <ElDatePicker
          v-model="query.range"
          type="datetimerange"
          value-format="YYYY-MM-DDTHH:mm:ss"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
        />
        <ElButton type="primary" @click="search">查询</ElButton>
      </div>
      <ElTable v-loading="loading" :data="rows" stripe>
        <ElTableColumn prop="occurredAt" label="时间" min-width="170" />
        <ElTableColumn prop="actorId" label="操作者" min-width="130" />
        <ElTableColumn prop="action" label="操作" min-width="140" />
        <ElTableColumn prop="method" label="方法" width="90" />
        <ElTableColumn prop="path" label="路径" min-width="230" show-overflow-tooltip />
        <ElTableColumn label="结果" width="100"
          ><template #default="{ row }"
            ><ElTag :type="row.outcome === 'SUCCESS' ? 'success' : 'danger'">{{
              row.outcome
            }}</ElTag></template
          ></ElTableColumn
        >
        <ElTableColumn prop="traceId" label="Trace ID" min-width="190" show-overflow-tooltip />
      </ElTable>
      <div class="mt-18px flex justify-end"
        ><ElPagination
          :current-page="query.page"
          :page-size="query.size"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="
            (page: number) => {
              query.page = page
              load()
            }
          "
      /></div>
    </ElCard>
  </div>
</template>

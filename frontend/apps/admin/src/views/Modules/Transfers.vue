<script setup lang="ts">
  import { onMounted, reactive, ref } from 'vue'
  import {
    ElButton,
    ElCard,
    ElDialog,
    ElForm,
    ElFormItem,
    ElInput,
    ElMessage,
    ElPagination,
    ElTable,
    ElTableColumn,
    ElTag,
    ElUpload,
    type UploadInstance,
    type UploadRawFile
  } from 'element-plus'
  import {
    downloadFile,
    listTransferTasks,
    retryTransferTask,
    submitExport,
    submitImport,
    uploadFile,
    type TransferTask
  } from '@/api/modules'

  const rows = ref<TransferTask[]>([])
  const loading = ref(false)
  const saving = ref(false)
  const uploadRef = ref<UploadInstance>()
  const dialog = ref(false)
  const kind = ref<'IMPORT' | 'EXPORT'>('IMPORT')
  const query = reactive({ ownerId: '', page: 1, size: 20, total: 0 })
  const form = reactive({ handlerKey: '', sourceFileId: '', options: '{}' })
  const statusType = (status: string): 'success' | 'danger' | 'primary' | 'info' =>
    status === 'SUCCEEDED'
      ? 'success'
      : status === 'FAILED'
        ? 'danger'
        : status === 'RUNNING'
          ? 'primary'
          : 'info'
  const load = async () => {
    loading.value = true
    try {
      const response = await listTransferTasks({
        ownerId: query.ownerId.trim() || undefined,
        page: query.page,
        size: query.size
      })
      rows.value = response.data
      query.total = response.pageInfo?.totalCount ?? rows.value.length
    } catch {
      rows.value = []
      query.total = 0
    } finally {
      loading.value = false
    }
  }
  const open = (value: 'IMPORT' | 'EXPORT') => {
    kind.value = value
    form.handlerKey = value === 'IMPORT' ? 'demo-csv-import' : 'demo-csv-export'
    form.sourceFileId = ''
    form.options = '{}'
    dialog.value = true
  }
  const selectFile = async (file: UploadRawFile) => {
    saving.value = true
    try {
      const response = await uploadFile(file)
      form.sourceFileId = response.data.id
      ElMessage.success('源文件上传成功')
    } catch {
      /* request layer shows errors */
    } finally {
      saving.value = false
      uploadRef.value?.clearFiles()
    }
  }
  const submit = async () => {
    if (!form.handlerKey.trim()) {
      ElMessage.warning('请输入处理器标识')
      return
    }
    if (kind.value === 'IMPORT' && !form.sourceFileId.trim()) {
      ElMessage.warning('请先上传源文件')
      return
    }
    let options: Record<string, string>
    try {
      const parsed: unknown = JSON.parse(form.options || '{}')
      if (
        !parsed ||
        Array.isArray(parsed) ||
        typeof parsed !== 'object' ||
        Object.values(parsed).some((value) => typeof value !== 'string')
      )
        throw new Error()
      options = parsed as Record<string, string>
    } catch {
      ElMessage.warning('选项须为字符串值的 JSON 对象')
      return
    }
    saving.value = true
    try {
      const data = {
        handlerKey: form.handlerKey.trim(),
        sourceFileId: form.sourceFileId || null,
        options
      }
      if (kind.value === 'IMPORT') await submitImport(data)
      else await submitExport(data)
      ElMessage.success('任务已提交')
      dialog.value = false
      await load()
    } catch {
      /* request layer shows errors */
    } finally {
      saving.value = false
    }
  }
  const retry = async (row: TransferTask) => {
    try {
      await retryTransferTask(row.id)
      ElMessage.success('已重新派发')
      await load()
    } catch {
      /* request layer shows errors */
    }
  }
  const download = async (row: TransferTask) => {
    if (!row.resultFileId) return
    try {
      const response = await downloadFile(row.resultFileId)
      const url = URL.createObjectURL(response.data)
      const link = document.createElement('a')
      link.href = url
      link.download = `${row.handlerKey}-${row.id}.csv`
      link.click()
      setTimeout(() => URL.revokeObjectURL(url), 60000)
    } catch {
      /* request layer shows errors */
    }
  }
  onMounted(() => {
    void load()
  })
</script>

<template>
  <div class="p-20px"
    ><ElCard shadow="never"
      ><div class="mb-18px flex flex-wrap items-center justify-between gap-10px"
        ><h1 class="m-0 text-20px">导入导出任务</h1
        ><div class="flex gap-10px"
          ><ElButton @click="load">刷新进度</ElButton
          ><ElButton type="primary" @click="open('IMPORT')">新建导入</ElButton
          ><ElButton type="primary" @click="open('EXPORT')">新建导出</ElButton></div
        ></div
      >
      <div class="mb-16px flex gap-10px"
        ><ElInput
          v-model="query.ownerId"
          placeholder="所有者 ID（管理员可用）"
          clearable
          class="!w-250px"
          @keyup.enter="
            () => {
              query.page = 1
              load()
            }
          "
        /><ElButton
          @click="
            () => {
              query.page = 1
              load()
            }
          "
          >查询</ElButton
        ></div
      >
      <ElTable v-loading="loading" :data="rows" stripe
        ><ElTableColumn
          prop="id"
          label="任务 ID"
          min-width="180"
          show-overflow-tooltip
        /><ElTableColumn label="类型" width="90"
          ><template #default="{ row }">{{
            row.kind === 'IMPORT' ? '导入' : '导出'
          }}</template></ElTableColumn
        ><ElTableColumn prop="handlerKey" label="处理器" min-width="150" /><ElTableColumn
          label="状态"
          width="105"
          ><template #default="{ row }"
            ><ElTag :type="statusType(row.status)">{{ row.status }}</ElTag></template
          ></ElTableColumn
        ><ElTableColumn label="进度" width="130"
          ><template #default="{ row }"
            >{{ row.processedCount }} / {{ row.totalCount || '?' }}</template
          ></ElTableColumn
        ><ElTableColumn
          prop="errorMessage"
          label="错误信息"
          min-width="170"
          show-overflow-tooltip
        /><ElTableColumn prop="createdAt" label="创建时间" min-width="170" /><ElTableColumn
          label="操作"
          width="130"
          ><template #default="{ row }"
            ><ElButton
              v-if="row.status === 'FAILED' || row.status === 'QUEUED'"
              link
              type="primary"
              @click="retry(row)"
              >重试</ElButton
            ><ElButton
              v-if="row.resultFileId && row.status === 'SUCCEEDED'"
              link
              type="primary"
              @click="download(row)"
              >下载结果</ElButton
            ></template
          ></ElTableColumn
        ></ElTable
      >
      <div class="mt-18px flex justify-end"
        ><ElPagination
          :current-page="query.page"
          :page-size="query.size"
          :total="query.total"
          layout="total, prev, pager, next"
          @current-change="
            (page: number) => {
              query.page = page
              load()
            }
          "
      /></div>
    </ElCard>
    <ElDialog
      v-model="dialog"
      :title="kind === 'IMPORT' ? '新建导入任务' : '新建导出任务'"
      width="560px"
      destroy-on-close
      ><ElForm :model="form" label-width="100px"
        ><ElFormItem label="处理器标识" required><ElInput v-model="form.handlerKey" /></ElFormItem
        ><ElFormItem v-if="kind === 'IMPORT'" label="源文件" required
          ><ElUpload
            ref="uploadRef"
            :show-file-list="false"
            :auto-upload="false"
            :on-change="(file) => file.raw && selectFile(file.raw)"
            ><ElButton :loading="saving">上传文件</ElButton></ElUpload
          ><span v-if="form.sourceFileId" class="ml-10px text-sm">{{
            form.sourceFileId
          }}</span></ElFormItem
        ><ElFormItem label="选项 JSON"
          ><ElInput v-model="form.options" type="textarea" :rows="4" /></ElFormItem></ElForm
      ><template #footer
        ><ElButton @click="dialog = false">取消</ElButton
        ><ElButton type="primary" :loading="saving" @click="submit">提交</ElButton></template
      ></ElDialog
    >
  </div>
</template>

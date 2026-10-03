<script setup lang="ts">
  import { onMounted, reactive, ref } from 'vue'
  import {
    ElButton,
    ElCard,
    ElDialog,
    ElInput,
    ElMessage,
    ElMessageBox,
    ElPagination,
    ElTable,
    ElTableColumn,
    ElUpload,
    type UploadInstance,
    type UploadRawFile
  } from 'element-plus'
  import {
    deleteFile,
    downloadFile,
    fileReferences,
    listFiles,
    retryFileCleanup,
    uploadFile,
    type FileReference,
    type FileResource
  } from '@/api/modules'
  import { useModuleAdmin } from './useModuleAdmin'

  const rows = ref<FileResource[]>([])
  const references = ref<FileReference[]>([])
  const selected = ref<FileResource>()
  const referenceDialog = ref(false)
  const loading = ref(false)
  const uploading = ref(false)
  const uploadRef = ref<UploadInstance>()
  const canManage = useModuleAdmin()
  const query = reactive({ ownerId: '', page: 1, size: 20, total: 0 })
  const formatSize = (size: number) =>
    size < 1024
      ? `${size} B`
      : size < 1048576
        ? `${(size / 1024).toFixed(1)} KB`
        : `${(size / 1048576).toFixed(1)} MB`
  const load = async () => {
    loading.value = true
    try {
      const response = await listFiles({
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
  const selectFile = async (file: UploadRawFile) => {
    uploading.value = true
    try {
      await uploadFile(file)
      ElMessage.success('上传成功')
      await load()
    } catch {
      /* request layer shows errors */
    } finally {
      uploading.value = false
      uploadRef.value?.clearFiles()
    }
  }
  const download = async (row: FileResource) => {
    try {
      const response = await downloadFile(row.id)
      const url = URL.createObjectURL(response.data)
      const link = document.createElement('a')
      link.href = url
      link.download = row.filename
      link.click()
      setTimeout(() => URL.revokeObjectURL(url), 60000)
    } catch {
      /* request layer shows errors */
    }
  }
  const showReferences = async (row: FileResource) => {
    try {
      const response = await fileReferences(row.id)
      selected.value = row
      references.value = response.data
      referenceDialog.value = true
    } catch {
      /* request layer shows errors */
    }
  }
  const remove = async (row: FileResource) => {
    try {
      await ElMessageBox.confirm(
        `删除文件「${row.filename}」？存在业务引用时将无法删除。`,
        '确认删除',
        { type: 'warning' }
      )
      await deleteFile(row.id)
      ElMessage.success('删除请求已提交')
      await load()
    } catch {
      /* cancelled or request error */
    }
  }
  const cleanup = async () => {
    try {
      const response = await retryFileCleanup()
      ElMessage.success(`已处理 ${response.data} 个待清理文件`)
      await load()
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
    ><ElCard shadow="never">
      <div class="mb-18px flex flex-wrap items-center justify-between gap-10px"
        ><h1 class="m-0 text-20px">文件资源</h1
        ><div class="flex gap-10px"
          ><ElUpload
            ref="uploadRef"
            :show-file-list="false"
            :auto-upload="false"
            :on-change="(file) => file.raw && selectFile(file.raw)"
            ><ElButton type="primary" :loading="uploading">上传文件</ElButton></ElUpload
          ><ElButton v-if="canManage" @click="cleanup">重试清理</ElButton></div
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
          prop="filename"
          label="文件名"
          min-width="200"
          show-overflow-tooltip
        /><ElTableColumn prop="ownerId" label="所有者" min-width="130" /><ElTableColumn
          label="大小"
          width="110"
          ><template #default="{ row }">{{ formatSize(row.size) }}</template></ElTableColumn
        ><ElTableColumn prop="contentType" label="类型" min-width="150" /><ElTableColumn
          prop="provider"
          label="存储"
          width="100"
        /><ElTableColumn prop="createdAt" label="上传时间" min-width="170" /><ElTableColumn
          label="操作"
          width="190"
          ><template #default="{ row }"
            ><ElButton link type="primary" @click="download(row)">下载</ElButton
            ><ElButton link @click="showReferences(row)">引用</ElButton
            ><ElButton link type="danger" @click="remove(row)">删除</ElButton></template
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
      v-model="referenceDialog"
      :title="`业务引用 · ${selected?.filename ?? ''}`"
      width="600px"
      ><ElTable :data="references"
        ><ElTableColumn prop="referenceType" label="业务类型" /><ElTableColumn
          prop="referenceId"
          label="业务 ID" /><ElTableColumn prop="id" label="引用 ID" /></ElTable
    ></ElDialog>
  </div>
</template>

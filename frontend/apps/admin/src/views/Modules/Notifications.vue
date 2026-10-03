<script setup lang="ts">
  import { onMounted, reactive, ref } from 'vue'
  import { useRouter } from 'vue-router'
  import {
    ElButton,
    ElCard,
    ElCheckbox,
    ElDialog,
    ElForm,
    ElFormItem,
    ElInput,
    ElMessage,
    ElMessageBox,
    ElPagination,
    ElTable,
    ElTableColumn,
    ElTag,
    type FormInstance
  } from 'element-plus'
  import {
    archiveNotification,
    listNotifications,
    markAllNotificationsRead,
    markNotificationRead,
    publishNotification,
    unreadCount,
    type Notification
  } from '@/api/modules'
  import { useModuleAdmin } from './useModuleAdmin'

  const rows = ref<Notification[]>([])
  const router = useRouter()
  const canManage = useModuleAdmin()
  const unread = ref(0)
  const loading = ref(false)
  const publishing = ref(false)
  const publishDialog = ref(false)
  const formRef = ref<FormInstance>()
  const query = reactive({ unreadOnly: false, page: 1, size: 20, total: 0 })
  const form = reactive({
    recipients: '',
    category: 'SYSTEM',
    title: '',
    body: '',
    actionUrl: '',
    requestKey: ''
  })
  const rules = {
    recipients: [{ required: true, message: '请输入收件人 ID', trigger: 'blur' }],
    title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
    body: [{ required: true, message: '请输入内容', trigger: 'blur' }]
  }
  const load = async () => {
    loading.value = true
    try {
      const [messages, count] = await Promise.all([
        listNotifications({ unreadOnly: query.unreadOnly, page: query.page, size: query.size }),
        unreadCount()
      ])
      rows.value = messages.data
      query.total = messages.pageInfo?.totalCount ?? rows.value.length
      unread.value = count.data.count
    } catch {
      rows.value = []
      query.total = 0
    } finally {
      loading.value = false
    }
  }
  const read = async (row: Notification) => {
    try {
      await markNotificationRead(row.id)
      await load()
    } catch {
      /* request layer shows errors */
    }
  }
  const openAction = async (row: Notification) => {
    if (!row.actionUrl?.startsWith('/') || row.actionUrl.startsWith('//')) return
    if (!row.readAt) await read(row)
    await router.push(row.actionUrl)
  }
  const readAll = async () => {
    try {
      await markAllNotificationsRead()
      await load()
    } catch {
      /* request layer shows errors */
    }
  }
  const archive = async (row: Notification) => {
    try {
      await ElMessageBox.confirm(`归档通知「${row.title}」？`, '确认归档', { type: 'warning' })
      await archiveNotification(row.id)
      await load()
    } catch {
      /* cancelled or request error */
    }
  }
  const publish = async () => {
    if (!formRef.value || !(await formRef.value.validate().catch(() => false))) return
    const recipients = [
      ...new Set(
        form.recipients
          .split(/[\s,，]+/)
          .map((value) => value.trim())
          .filter(Boolean)
      )
    ]
    if (!recipients.length) {
      ElMessage.warning('请输入收件人 ID')
      return
    }
    publishing.value = true
    try {
      const response = await publishNotification({
        recipients,
        category: form.category,
        title: form.title,
        body: form.body,
        actionUrl: form.actionUrl || null,
        requestKey: form.requestKey || null
      })
      ElMessage.success(`已投递 ${response.data.delivered} 条通知`)
      publishDialog.value = false
      await load()
    } catch {
      /* request layer shows errors */
    } finally {
      publishing.value = false
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
        ><h1 class="m-0 text-20px"
          >站内通知 <ElTag v-if="unread" type="danger" round>{{ unread }} 未读</ElTag></h1
        ><div class="flex gap-10px"
          ><ElButton @click="readAll">全部已读</ElButton
          ><ElButton v-if="canManage" type="primary" @click="publishDialog = true"
            >发布通知</ElButton
          ></div
        ></div
      >
      <div class="mb-16px"
        ><ElCheckbox
          v-model="query.unreadOnly"
          @change="
            () => {
              query.page = 1
              load()
            }
          "
          >只看未读</ElCheckbox
        ></div
      >
      <ElTable v-loading="loading" :data="rows" stripe
        ><ElTableColumn label="状态" width="90"
          ><template #default="{ row }"
            ><ElTag :type="row.readAt ? 'info' : 'primary'">{{
              row.readAt ? '已读' : '未读'
            }}</ElTag></template
          ></ElTableColumn
        ><ElTableColumn prop="title" label="标题" min-width="170" /><ElTableColumn
          prop="body"
          label="内容"
          min-width="260"
          show-overflow-tooltip
        /><ElTableColumn prop="category" label="分类" width="110" /><ElTableColumn
          prop="createdAt"
          label="时间"
          min-width="170"
        /><ElTableColumn label="操作" width="220"
          ><template #default="{ row }"
            ><ElButton
              v-if="row.actionUrl?.startsWith('/') && !row.actionUrl.startsWith('//')"
              link
              type="primary"
              @click="openAction(row)"
              >查看</ElButton
            ><ElButton v-if="!row.readAt" link type="primary" @click="read(row)">标为已读</ElButton
            ><ElButton link type="danger" @click="archive(row)">归档</ElButton></template
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
    <ElDialog v-model="publishDialog" title="发布通知（管理员）" width="580px" destroy-on-close
      ><ElForm ref="formRef" :model="form" :rules="rules" label-width="100px"
        ><ElFormItem label="收件人 ID" prop="recipients"
          ><ElInput v-model="form.recipients" placeholder="多个 ID 用逗号分隔" /></ElFormItem
        ><ElFormItem label="分类"><ElInput v-model="form.category" /></ElFormItem
        ><ElFormItem label="标题" prop="title"><ElInput v-model="form.title" /></ElFormItem
        ><ElFormItem label="内容" prop="body"
          ><ElInput v-model="form.body" type="textarea" :rows="4" /></ElFormItem
        ><ElFormItem label="跳转链接"><ElInput v-model="form.actionUrl" /></ElFormItem
        ><ElFormItem label="请求键"
          ><ElInput
            v-model="form.requestKey"
            placeholder="可选，用于避免重复投递" /></ElFormItem></ElForm
      ><template #footer
        ><ElButton @click="publishDialog = false">取消</ElButton
        ><ElButton type="primary" :loading="publishing" @click="publish">发布</ElButton></template
      ></ElDialog
    >
  </div>
</template>

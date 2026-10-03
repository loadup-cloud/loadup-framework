<script setup lang="ts">
  import { onMounted, reactive, ref } from 'vue'
  import {
    ElButton,
    ElCard,
    ElDescriptions,
    ElDescriptionsItem,
    ElForm,
    ElFormItem,
    ElInput,
    ElMessage,
    ElPagination,
    ElTable,
    ElTableColumn,
    ElTag,
    type FormInstance
  } from 'element-plus'
  import {
    changePassword,
    loginHistory,
    securityOverview,
    type LoginEntry,
    type SecurityOverview
  } from '@/api/modules'

  const overview = ref<SecurityOverview>()
  const logins = ref<LoginEntry[]>([])
  const page = reactive({ page: 1, size: 20, total: 0 })
  const form = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
  const formRef = ref<FormInstance>()
  const saving = ref(false)
  const load = async () => {
    try {
      const [security, history] = await Promise.all([
        securityOverview(),
        loginHistory({ page: page.page, size: page.size })
      ])
      overview.value = security.data
      logins.value = history.data
      page.total = history.pageInfo?.totalCount ?? logins.value.length
    } catch {
      logins.value = []
      page.total = 0
    }
  }
  const save = async () => {
    if (!formRef.value || !(await formRef.value.validate().catch(() => false))) return
    if (form.newPassword !== form.confirmPassword) {
      ElMessage.warning('两次输入的新密码不一致')
      return
    }
    saving.value = true
    try {
      await changePassword({ ...form })
      ElMessage.success('密码已修改')
      Object.assign(form, { oldPassword: '', newPassword: '', confirmPassword: '' })
      await load()
    } catch {
      /* request layer shows errors */
    } finally {
      saving.value = false
    }
  }
  const rules = {
    oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
    newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
    confirmPassword: [{ required: true, message: '请确认新密码', trigger: 'blur' }]
  }
  onMounted(() => {
    void load()
  })
</script>

<template>
  <div class="grid gap-20px p-20px xl:grid-cols-2">
    <ElCard shadow="never"
      ><h1 class="mt-0 text-20px">账号安全</h1
      ><ElDescriptions v-if="overview" :column="1" border
        ><ElDescriptionsItem label="账号">{{ overview.username }}</ElDescriptionsItem
        ><ElDescriptionsItem label="账号状态"
          ><ElTag :type="overview.active ? 'success' : 'danger'">{{
            overview.active ? '正常' : '停用'
          }}</ElTag></ElDescriptionsItem
        ><ElDescriptionsItem label="锁定状态">{{
          overview.accountNonLocked ? '未锁定' : '已锁定'
        }}</ElDescriptionsItem
        ><ElDescriptionsItem label="登录失败次数">{{ overview.loginFailCount }}</ElDescriptionsItem
        ><ElDescriptionsItem label="上次登录">{{ overview.lastLoginAt || '—' }}</ElDescriptionsItem
        ><ElDescriptionsItem label="上次登录 IP">{{
          overview.lastLoginIp || '—'
        }}</ElDescriptionsItem
        ><ElDescriptionsItem label="密码更新时间">{{
          overview.passwordUpdatedAt || '—'
        }}</ElDescriptionsItem></ElDescriptions
      ></ElCard
    >
    <ElCard shadow="never"
      ><h2 class="mt-0 text-18px">修改密码</h2
      ><ElForm ref="formRef" :model="form" :rules="rules" label-width="100px" class="max-w-520px"
        ><ElFormItem label="当前密码" prop="oldPassword"
          ><ElInput
            v-model="form.oldPassword"
            type="password"
            show-password
            autocomplete="current-password" /></ElFormItem
        ><ElFormItem label="新密码" prop="newPassword"
          ><ElInput
            v-model="form.newPassword"
            type="password"
            show-password
            autocomplete="new-password" /></ElFormItem
        ><ElFormItem label="确认密码" prop="confirmPassword"
          ><ElInput
            v-model="form.confirmPassword"
            type="password"
            show-password
            autocomplete="new-password" /></ElFormItem
        ><ElFormItem
          ><ElButton type="primary" :loading="saving" @click="save"
            >保存新密码</ElButton
          ></ElFormItem
        ></ElForm
      ></ElCard
    >
    <ElCard shadow="never" class="xl:col-span-2"
      ><h2 class="mt-0 text-18px">登录记录</h2
      ><ElTable :data="logins" stripe
        ><ElTableColumn prop="loginAt" label="登录时间" min-width="180" /><ElTableColumn
          prop="ipAddress"
          label="IP 地址"
          min-width="150"
        /><ElTableColumn prop="loginType" label="方式" min-width="120" /><ElTableColumn
          label="结果"
          width="100"
          ><template #default="{ row }"
            ><ElTag :type="row.success ? 'success' : 'danger'">{{
              row.success ? '成功' : '失败'
            }}</ElTag></template
          ></ElTableColumn
        ></ElTable
      ><div class="mt-18px flex justify-end"
        ><ElPagination
          :current-page="page.page"
          :page-size="page.size"
          :total="page.total"
          layout="total, prev, pager, next"
          @current-change="
            (value: number) => {
              page.page = value
              load()
            }
          " /></div
    ></ElCard>
  </div>
</template>

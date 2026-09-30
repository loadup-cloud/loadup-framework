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
    ElMessageBox,
    ElOption,
    ElPagination,
    ElSelect,
    ElTable,
    ElTableColumn,
    ElTag,
    ElTreeSelect,
    type FormInstance
  } from 'element-plus'
  import {
    createUser,
    deleteUser,
    departmentTree,
    getUser,
    listUsers,
    roleTree,
    updateUser
  } from '@/api/upms'
  import type { Department, Role, User, UserForm } from '@/api/upms/types'

  const rows = ref<User[]>([])
  const total = ref(0)
  const loading = ref(false)
  const saving = ref(false)
  const dialogVisible = ref(false)
  const editingId = ref('')
  const formRef = ref<FormInstance>()
  const departments = ref<Department[]>([])
  const roles = ref<Role[]>([])
  const query = reactive({
    username: '',
    status: undefined as number | undefined,
    page: 1,
    size: 20
  })
  const emptyForm = (): UserForm => ({
    username: '',
    password: '',
    nickname: '',
    realName: '',
    deptId: '',
    email: '',
    mobile: '',
    status: 1,
    roleIds: [],
    remark: ''
  })
  const form = reactive<UserForm>(emptyForm())
  const rules = {
    username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
    nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }]
  }
  const flattenRoles = (items: Role[]): Role[] =>
    items.flatMap((item) => [item, ...flattenRoles(item.children ?? [])])

  const load = async () => {
    loading.value = true
    try {
      const response = await listUsers({
        username: query.username.trim() || undefined,
        status: typeof query.status === 'number' ? query.status : undefined,
        page: query.page,
        size: query.size
      })
      rows.value = response.data
      total.value = response.pageInfo?.totalCount ?? response.data.length
    } catch {
      rows.value = []
      total.value = 0
    } finally {
      loading.value = false
    }
  }
  const loadOptions = async () => {
    try {
      const [deptResponse, roleResponse] = await Promise.all([departmentTree(), roleTree()])
      departments.value = deptResponse.data
      roles.value = flattenRoles(roleResponse.data)
    } catch {
      departments.value = []
      roles.value = []
    }
  }
  const search = () => {
    query.page = 1
    void load()
  }
  const changePage = (page: number) => {
    query.page = page
    void load()
  }
  const openCreate = () => {
    editingId.value = ''
    Object.assign(form, emptyForm())
    dialogVisible.value = true
  }
  const openEdit = async (row: User) => {
    try {
      const { data } = await getUser(row.id)
      editingId.value = data.id
      Object.assign(form, {
        username: data.username,
        password: '',
        nickname: data.nickname,
        realName: data.realName ?? '',
        deptId: data.deptId ?? '',
        email: data.email ?? '',
        mobile: data.mobile ?? '',
        status: data.status,
        roleIds: data.roles?.map((role) => role.id) ?? [],
        remark: data.remark ?? ''
      })
      dialogVisible.value = true
    } catch {
      /* request layer displays the error */
    }
  }
  const save = async () => {
    if (!formRef.value || !(await formRef.value.validate().catch(() => false))) return
    if (!editingId.value && (form.password.length < 6 || form.password.length > 20)) {
      ElMessage.warning('密码长度须为 6 到 20 位')
      return
    }
    saving.value = true
    try {
      if (editingId.value) await updateUser(editingId.value, form)
      else await createUser(form)
      ElMessage.success('保存成功')
      dialogVisible.value = false
      await load()
    } catch {
      /* request layer displays the error */
    } finally {
      saving.value = false
    }
  }
  const remove = async (row: User) => {
    try {
      await ElMessageBox.confirm(`确定删除用户「${row.username}」？`, '确认删除', {
        type: 'warning'
      })
      await deleteUser(row.id)
      ElMessage.success('删除成功')
      await load()
    } catch {
      /* cancelled or request error */
    }
  }

  onMounted(() => {
    void load()
    void loadOptions()
  })
</script>

<template>
  <div class="p-20px">
    <ElCard shadow="never">
      <div class="mb-18px flex flex-wrap items-center justify-between gap-12px">
        <h1 class="m-0 text-20px">用户管理</h1>
        <ElButton type="primary" @click="openCreate">新增用户</ElButton>
      </div>
      <div class="mb-16px flex flex-wrap gap-10px">
        <ElInput
          v-model="query.username"
          placeholder="用户名"
          clearable
          class="!w-220px"
          @keyup.enter="search"
        />
        <ElSelect v-model="query.status" placeholder="全部状态" clearable class="!w-140px">
          <ElOption label="正常" :value="1" /><ElOption label="停用" :value="0" />
        </ElSelect>
        <ElButton @click="search">查询</ElButton>
      </div>
      <ElTable :data="rows" v-loading="loading" row-key="id" stripe>
        <ElTableColumn prop="username" label="用户名" min-width="140" />
        <ElTableColumn prop="nickname" label="昵称" min-width="130" />
        <ElTableColumn prop="realName" label="姓名" min-width="120" />
        <ElTableColumn prop="deptName" label="部门" min-width="130" />
        <ElTableColumn label="角色" min-width="180">
          <template #default="{ row }">{{
            row.roles?.map((role: Role) => role.roleName).join('、') || '—'
          }}</template>
        </ElTableColumn>
        <ElTableColumn label="状态" width="90">
          <template #default="{ row }"
            ><ElTag :type="row.status === 1 ? 'success' : 'info'">{{
              row.status === 1 ? '正常' : '停用'
            }}</ElTag></template
          >
        </ElTableColumn>
        <ElTableColumn label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <ElButton link type="primary" @click="openEdit(row)">编辑</ElButton>
            <ElButton link type="danger" @click="remove(row)">删除</ElButton>
          </template>
        </ElTableColumn>
      </ElTable>
      <div class="mt-18px flex justify-end">
        <ElPagination
          :current-page="query.page"
          :page-size="query.size"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="changePage"
        />
      </div>
    </ElCard>

    <ElDialog
      v-model="dialogVisible"
      :title="editingId ? '编辑用户' : '新增用户'"
      width="620px"
      destroy-on-close
    >
      <ElForm ref="formRef" :model="form" :rules="rules" label-width="90px">
        <ElFormItem label="用户名" prop="username"
          ><ElInput v-model="form.username" :disabled="!!editingId"
        /></ElFormItem>
        <ElFormItem v-if="!editingId" label="密码" required
          ><ElInput
            v-model="form.password"
            type="password"
            show-password
            autocomplete="new-password"
        /></ElFormItem>
        <ElFormItem label="昵称" prop="nickname"><ElInput v-model="form.nickname" /></ElFormItem>
        <ElFormItem label="姓名"><ElInput v-model="form.realName" /></ElFormItem>
        <ElFormItem label="部门">
          <ElTreeSelect
            v-model="form.deptId"
            :data="departments"
            node-key="id"
            :props="{ label: 'deptName', children: 'children' }"
            check-strictly
            clearable
            class="w-full"
          />
        </ElFormItem>
        <ElFormItem label="角色">
          <ElSelect v-model="form.roleIds" multiple clearable class="w-full">
            <ElOption
              v-for="role in roles"
              :key="role.id"
              :label="role.roleName"
              :value="role.id"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="邮箱"><ElInput v-model="form.email" /></ElFormItem>
        <ElFormItem label="手机号"><ElInput v-model="form.mobile" /></ElFormItem>
        <ElFormItem label="状态"
          ><ElSelect v-model="form.status" class="w-full"
            ><ElOption label="正常" :value="1" /><ElOption label="停用" :value="0" /></ElSelect
        ></ElFormItem>
        <ElFormItem label="备注"
          ><ElInput v-model="form.remark" type="textarea" :rows="2"
        /></ElFormItem>
      </ElForm>
      <template #footer
        ><ElButton @click="dialogVisible = false">取消</ElButton
        ><ElButton type="primary" :loading="saving" @click="save">保存</ElButton></template
      >
    </ElDialog>
  </div>
</template>

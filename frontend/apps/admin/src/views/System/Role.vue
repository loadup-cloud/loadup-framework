<script setup lang="ts">
  import { onMounted, reactive, ref } from 'vue'
  import {
    ElButton,
    ElCard,
    ElDialog,
    ElForm,
    ElFormItem,
    ElInput,
    ElInputNumber,
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
    createRole,
    deleteRole,
    departmentTree,
    getRole,
    listRoles,
    permissionTree,
    roleTree,
    updateRole
  } from '@/api/upms'
  import type { Department, Permission, Role, RoleForm } from '@/api/upms/types'

  const rows = ref<Role[]>([])
  const total = ref(0)
  const loading = ref(false)
  const saving = ref(false)
  const dialogVisible = ref(false)
  const editingId = ref('')
  const formRef = ref<FormInstance>()
  const roleOptions = ref<Role[]>([])
  const permissionOptions = ref<Permission[]>([])
  const departmentOptions = ref<Department[]>([])
  const query = reactive({
    roleName: '',
    status: undefined as number | undefined,
    page: 1,
    size: 20
  })
  const emptyForm = (): RoleForm => ({
    roleName: '',
    roleCode: '',
    parentId: '',
    dataScope: 1,
    sortOrder: 0,
    status: 1,
    permissionIds: [],
    departmentIds: [],
    remark: ''
  })
  const form = reactive<RoleForm>(emptyForm())
  const rules = { roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }] }
  const scopes: Record<number, string> = {
    1: '全部数据',
    2: '指定部门',
    3: '本部门',
    4: '本部门及下级',
    5: '仅本人'
  }

  const load = async () => {
    loading.value = true
    try {
      const response = await listRoles({
        roleName: query.roleName.trim() || undefined,
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
      const [roleResponse, permissionResponse, deptResponse] = await Promise.all([
        roleTree(),
        permissionTree(),
        departmentTree()
      ])
      roleOptions.value = roleResponse.data
      permissionOptions.value = permissionResponse.data
      departmentOptions.value = deptResponse.data
    } catch {
      roleOptions.value = []
      permissionOptions.value = []
      departmentOptions.value = []
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
  const openEdit = async (row: Role) => {
    try {
      const { data } = await getRole(row.id)
      editingId.value = data.id
      Object.assign(form, {
        roleName: data.roleName,
        roleCode: data.roleCode,
        parentId: data.parentId ?? '',
        dataScope: data.dataScope,
        sortOrder: data.sortOrder ?? 0,
        status: data.status,
        permissionIds: data.permissions?.map((item) => item.id) ?? [],
        departmentIds: data.departmentIds ?? [],
        remark: data.remark ?? ''
      })
      dialogVisible.value = true
    } catch {
      /* request layer displays the error */
    }
  }
  const save = async () => {
    if (!formRef.value || !(await formRef.value.validate().catch(() => false))) return
    if (!editingId.value && !/^[A-Z_]+$/.test(form.roleCode)) {
      ElMessage.warning('角色编码只能包含大写字母和下划线')
      return
    }
    if (form.dataScope === 2 && form.departmentIds.length === 0) {
      ElMessage.warning('请至少选择一个指定部门')
      return
    }
    saving.value = true
    try {
      if (editingId.value) await updateRole(editingId.value, form)
      else await createRole(form)
      ElMessage.success('保存成功')
      dialogVisible.value = false
      await Promise.all([load(), loadOptions()])
    } catch {
      /* request layer displays the error */
    } finally {
      saving.value = false
    }
  }
  const remove = async (row: Role) => {
    try {
      await ElMessageBox.confirm(`确定删除角色「${row.roleName}」？`, '确认删除', {
        type: 'warning'
      })
      await deleteRole(row.id)
      ElMessage.success('删除成功')
      await Promise.all([load(), loadOptions()])
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
        <h1 class="m-0 text-20px">角色管理</h1>
        <ElButton type="primary" @click="openCreate">新增角色</ElButton>
      </div>
      <div class="mb-16px flex flex-wrap gap-10px">
        <ElInput
          v-model="query.roleName"
          placeholder="角色名称"
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
        <ElTableColumn prop="roleName" label="角色名称" min-width="150" />
        <ElTableColumn prop="roleCode" label="角色编码" min-width="160" />
        <ElTableColumn label="数据范围" min-width="130">
          <template #default="{ row }">{{ scopes[row.dataScope] ?? '—' }}</template>
        </ElTableColumn>
        <ElTableColumn prop="sortOrder" label="排序" width="80" />
        <ElTableColumn label="状态" width="90">
          <template #default="{ row }"
            ><ElTag :type="row.status === 1 ? 'success' : 'info'">{{
              row.status === 1 ? '正常' : '停用'
            }}</ElTag></template
          >
        </ElTableColumn>
        <ElTableColumn label="操作" width="140" fixed="right">
          <template #default="{ row }"
            ><ElButton link type="primary" @click="openEdit(row)">编辑</ElButton
            ><ElButton link type="danger" @click="remove(row)">删除</ElButton></template
          >
        </ElTableColumn>
      </ElTable>
      <div class="mt-18px flex justify-end"
        ><ElPagination
          :current-page="query.page"
          :page-size="query.size"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="changePage"
      /></div>
    </ElCard>

    <ElDialog
      v-model="dialogVisible"
      :title="editingId ? '编辑角色' : '新增角色'"
      width="620px"
      destroy-on-close
    >
      <ElForm ref="formRef" :model="form" :rules="rules" label-width="100px">
        <ElFormItem label="角色名称" prop="roleName"
          ><ElInput v-model="form.roleName"
        /></ElFormItem>
        <ElFormItem label="角色编码" required
          ><ElInput v-model="form.roleCode" :disabled="!!editingId" placeholder="例如 SYSTEM_ADMIN"
        /></ElFormItem>
        <ElFormItem label="上级角色">
          <ElTreeSelect
            v-model="form.parentId"
            :data="roleOptions"
            node-key="id"
            :props="{ label: 'roleName', children: 'children' }"
            check-strictly
            :clearable="!editingId"
            class="w-full"
          />
        </ElFormItem>
        <ElFormItem label="数据范围"
          ><ElSelect v-model="form.dataScope" class="w-full"
            ><ElOption
              v-for="(label, value) in scopes"
              :key="value"
              :label="label"
              :value="Number(value)" /></ElSelect
        ></ElFormItem>
        <ElFormItem v-if="form.dataScope === 2" label="指定部门">
          <ElTreeSelect
            v-model="form.departmentIds"
            :data="departmentOptions"
            node-key="id"
            :props="{ label: 'deptName', children: 'children' }"
            show-checkbox
            multiple
            check-strictly
            clearable
            class="w-full"
          />
        </ElFormItem>
        <ElFormItem label="权限">
          <ElTreeSelect
            v-model="form.permissionIds"
            :data="permissionOptions"
            node-key="id"
            :props="{ label: 'permissionName', children: 'children' }"
            show-checkbox
            multiple
            check-strictly
            clearable
            class="w-full"
          />
        </ElFormItem>
        <ElFormItem label="排序"><ElInputNumber v-model="form.sortOrder" :min="0" /></ElFormItem>
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

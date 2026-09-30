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
    ElSelect,
    ElSwitch,
    ElTable,
    ElTableColumn,
    ElTag,
    type FormInstance
  } from 'element-plus'
  import {
    createPermission,
    deletePermission,
    getPermission,
    permissionTree,
    updatePermission
  } from '@/api/upms'
  import type { Permission, PermissionForm } from '@/api/upms/types'

  const rows = ref<Permission[]>([])
  const loading = ref(false)
  const saving = ref(false)
  const dialogVisible = ref(false)
  const editingId = ref('')
  const formRef = ref<FormInstance>()
  const emptyForm = (): PermissionForm => ({
    parentId: '',
    permissionName: '',
    permissionCode: '',
    permissionType: 1,
    resourcePath: '',
    icon: '',
    componentPath: '',
    sortOrder: 0,
    visible: true,
    status: 1,
    remark: ''
  })
  const form = reactive<PermissionForm>(emptyForm())
  const rules = { permissionName: [{ required: true, message: '请输入权限名称', trigger: 'blur' }] }
  const types: Record<number, string> = { 1: '菜单', 2: '按钮', 3: '接口' }
  const flatten = (items: Permission[]): Permission[] =>
    items.flatMap((item) => [item, ...flatten(item.children ?? [])])

  const load = async () => {
    loading.value = true
    try {
      rows.value = (await permissionTree()).data
    } catch {
      rows.value = []
    } finally {
      loading.value = false
    }
  }
  const openCreate = (parent?: Permission) => {
    editingId.value = ''
    Object.assign(form, emptyForm(), { parentId: parent?.id ?? '' })
    dialogVisible.value = true
  }
  const openEdit = async (row: Permission) => {
    try {
      const { data } = await getPermission(row.id)
      editingId.value = data.id
      Object.assign(form, {
        parentId: data.parentId ?? '',
        permissionName: data.permissionName,
        permissionCode: data.permissionCode,
        permissionType: data.permissionType,
        resourcePath: data.resourcePath ?? '',
        icon: data.icon ?? '',
        componentPath: data.componentPath ?? '',
        sortOrder: data.sortOrder ?? 0,
        visible: data.visible,
        status: data.status,
        remark: data.remark ?? ''
      })
      dialogVisible.value = true
    } catch {
      /* request layer displays the error */
    }
  }
  const save = async () => {
    if (!formRef.value || !(await formRef.value.validate().catch(() => false))) return
    if (!editingId.value && !/^[a-z:]+$/.test(form.permissionCode)) {
      ElMessage.warning('权限编码只能包含小写字母和冒号')
      return
    }
    if (form.permissionType === 1 && !form.resourcePath.trim()) {
      ElMessage.warning('菜单权限需要路由路径')
      return
    }
    saving.value = true
    try {
      if (editingId.value) await updatePermission(editingId.value, form)
      else await createPermission(form)
      ElMessage.success('保存成功，刷新页面后导航菜单生效')
      dialogVisible.value = false
      await load()
    } catch {
      /* request layer displays the error */
    } finally {
      saving.value = false
    }
  }
  const remove = async (row: Permission) => {
    try {
      await ElMessageBox.confirm(`确定删除权限「${row.permissionName}」？`, '确认删除', {
        type: 'warning'
      })
      await deletePermission(row.id)
      ElMessage.success('删除成功，刷新页面后导航菜单生效')
      await load()
    } catch {
      /* cancelled or request error */
    }
  }

  onMounted(() => {
    void load()
  })
</script>

<template>
  <div class="p-20px">
    <ElCard shadow="never">
      <div class="mb-18px flex flex-wrap items-center justify-between gap-12px">
        <h1 class="m-0 text-20px">权限管理</h1>
        <ElButton type="primary" @click="openCreate()">新增权限</ElButton>
      </div>
      <ElTable :data="rows" v-loading="loading" row-key="id" default-expand-all stripe>
        <ElTableColumn prop="permissionName" label="权限名称" min-width="190" />
        <ElTableColumn prop="permissionCode" label="权限编码" min-width="180" />
        <ElTableColumn label="类型" width="90">
          <template #default="{ row }">{{ types[row.permissionType] ?? '—' }}</template>
        </ElTableColumn>
        <ElTableColumn prop="resourcePath" label="路由 / 资源路径" min-width="170" />
        <ElTableColumn label="状态" width="90">
          <template #default="{ row }"
            ><ElTag :type="row.status === 1 ? 'success' : 'info'">{{
              row.status === 1 ? '正常' : '停用'
            }}</ElTag></template
          >
        </ElTableColumn>
        <ElTableColumn label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <ElButton link type="primary" @click="openCreate(row)">新增下级</ElButton>
            <ElButton link type="primary" @click="openEdit(row)">编辑</ElButton>
            <ElButton link type="danger" @click="remove(row)">删除</ElButton>
          </template>
        </ElTableColumn>
      </ElTable>
    </ElCard>

    <ElDialog
      v-model="dialogVisible"
      :title="editingId ? '编辑权限' : '新增权限'"
      width="640px"
      destroy-on-close
    >
      <ElForm ref="formRef" :model="form" :rules="rules" label-width="110px">
        <ElFormItem label="上级权限">
          <ElSelect
            v-model="form.parentId"
            clearable
            filterable
            class="w-full"
            placeholder="根节点"
          >
            <ElOption
              v-for="item in flatten(rows)"
              :key="item.id"
              :label="`${item.permissionName} (${item.permissionCode})`"
              :value="item.id"
              :disabled="item.id === editingId"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="权限名称" prop="permissionName"
          ><ElInput v-model="form.permissionName"
        /></ElFormItem>
        <ElFormItem label="权限编码" required
          ><ElInput v-model="form.permissionCode" :disabled="!!editingId"
        /></ElFormItem>
        <ElFormItem label="类型"
          ><ElSelect v-model="form.permissionType" class="w-full"
            ><ElOption label="菜单" :value="1" /><ElOption label="按钮" :value="2" /><ElOption
              label="接口"
              :value="3" /></ElSelect
        ></ElFormItem>
        <ElFormItem label="路由 / 资源路径"
          ><ElInput v-model="form.resourcePath" placeholder="菜单示例：/system/user"
        /></ElFormItem>
        <ElFormItem v-if="form.permissionType === 1" label="组件路径"
          ><ElInput
            v-model="form.componentPath"
            placeholder="例如 views/System/User；留空使用占位页"
        /></ElFormItem>
        <ElFormItem v-if="form.permissionType === 1" label="图标"
          ><ElInput v-model="form.icon" placeholder="例如 mdi:folder-outline"
        /></ElFormItem>
        <ElFormItem label="排序"><ElInputNumber v-model="form.sortOrder" :min="0" /></ElFormItem>
        <ElFormItem label="显示"><ElSwitch v-model="form.visible" /></ElFormItem>
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

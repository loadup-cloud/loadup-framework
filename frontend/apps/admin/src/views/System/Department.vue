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
    ElTable,
    ElTableColumn,
    ElTag,
    ElTreeSelect,
    type FormInstance
  } from 'element-plus'
  import {
    createDepartment,
    deleteDepartment,
    departmentTree,
    getDepartment,
    updateDepartment
  } from '@/api/upms'
  import type { Department, DepartmentForm } from '@/api/upms/types'

  const rows = ref<Department[]>([])
  const loading = ref(false)
  const saving = ref(false)
  const dialogVisible = ref(false)
  const editingId = ref('')
  const formRef = ref<FormInstance>()
  const emptyForm = (): DepartmentForm => ({
    parentId: '',
    deptName: '',
    deptCode: '',
    sortOrder: 0,
    mobile: '',
    email: '',
    status: 1,
    remark: ''
  })
  const form = reactive<DepartmentForm>(emptyForm())
  const rules = { deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }] }

  const load = async () => {
    loading.value = true
    try {
      rows.value = (await departmentTree()).data
    } catch {
      rows.value = []
    } finally {
      loading.value = false
    }
  }
  const openCreate = (parent?: Department) => {
    editingId.value = ''
    Object.assign(form, emptyForm(), { parentId: parent?.id ?? '' })
    dialogVisible.value = true
  }
  const openEdit = async (row: Department) => {
    try {
      const { data } = await getDepartment(row.id)
      editingId.value = data.id
      Object.assign(form, {
        parentId: data.parentId ?? '',
        deptName: data.deptName,
        deptCode: data.deptCode,
        sortOrder: data.sortOrder ?? 0,
        mobile: data.mobile ?? '',
        email: data.email ?? '',
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
    if (!editingId.value && !form.deptCode.trim()) {
      ElMessage.warning('请输入部门编码')
      return
    }
    saving.value = true
    try {
      if (editingId.value) await updateDepartment(editingId.value, form)
      else await createDepartment(form)
      ElMessage.success('保存成功')
      dialogVisible.value = false
      await load()
    } catch {
      /* request layer displays the error */
    } finally {
      saving.value = false
    }
  }
  const remove = async (row: Department) => {
    try {
      await ElMessageBox.confirm(`确定删除部门「${row.deptName}」？`, '确认删除', {
        type: 'warning'
      })
      await deleteDepartment(row.id)
      ElMessage.success('删除成功')
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
        <h1 class="m-0 text-20px">部门管理</h1>
        <ElButton type="primary" @click="openCreate()">新增部门</ElButton>
      </div>
      <ElTable :data="rows" v-loading="loading" row-key="id" default-expand-all stripe>
        <ElTableColumn prop="deptName" label="部门名称" min-width="200" />
        <ElTableColumn prop="deptCode" label="部门编码" min-width="170" />
        <ElTableColumn prop="sortOrder" label="排序" width="90" />
        <ElTableColumn prop="mobile" label="联系电话" min-width="130" />
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
      :title="editingId ? '编辑部门' : '新增部门'"
      width="600px"
      destroy-on-close
    >
      <ElForm ref="formRef" :model="form" :rules="rules" label-width="100px">
        <ElFormItem label="上级部门">
          <ElTreeSelect
            v-model="form.parentId"
            :data="rows"
            node-key="id"
            :props="{ label: 'deptName', children: 'children' }"
            check-strictly
            clearable
            class="w-full"
            :disabled="!!editingId"
          />
        </ElFormItem>
        <ElFormItem label="部门名称" prop="deptName"
          ><ElInput v-model="form.deptName"
        /></ElFormItem>
        <ElFormItem label="部门编码" required
          ><ElInput v-model="form.deptCode" :disabled="!!editingId"
        /></ElFormItem>
        <ElFormItem label="排序"><ElInputNumber v-model="form.sortOrder" :min="0" /></ElFormItem>
        <ElFormItem label="联系电话"><ElInput v-model="form.mobile" /></ElFormItem>
        <ElFormItem label="邮箱"><ElInput v-model="form.email" /></ElFormItem>
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

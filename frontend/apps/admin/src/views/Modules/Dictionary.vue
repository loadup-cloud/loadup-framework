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
    ElPagination,
    ElSwitch,
    ElTable,
    ElTableColumn,
    type FormInstance
  } from 'element-plus'
  import {
    createDictionaryItem,
    createDictionaryType,
    deleteDictionaryItem,
    deleteDictionaryType,
    listDictionaryItems,
    listDictionaryTypes,
    updateDictionaryItem,
    updateDictionaryType,
    type DictionaryItem,
    type DictionaryType
  } from '@/api/modules'

  const types = ref<DictionaryType[]>([])
  const items = ref<DictionaryItem[]>([])
  const selected = ref<DictionaryType>()
  const typePage = reactive({ page: 1, size: 20, total: 0 })
  const itemPage = reactive({ page: 1, size: 20, total: 0 })
  const loading = ref(false)
  const saving = ref(false)
  const dialog = ref(false)
  const mode = ref<'type' | 'item'>('type')
  const editingId = ref('')
  const formRef = ref<FormInstance>()
  const form = reactive({
    code: '',
    name: '',
    value: '',
    label: '',
    description: '',
    sortOrder: 0,
    enabled: true
  })
  const rules = {
    code: [{ required: true, message: '请输入类型编码', trigger: 'blur' }],
    name: [{ required: true, message: '请输入类型名称', trigger: 'blur' }],
    value: [{ required: true, message: '请输入条目值', trigger: 'blur' }],
    label: [{ required: true, message: '请输入条目标签', trigger: 'blur' }]
  }
  const loadTypes = async () => {
    loading.value = true
    try {
      const response = await listDictionaryTypes({ page: typePage.page, size: typePage.size })
      types.value = response.data
      typePage.total = response.pageInfo?.totalCount ?? types.value.length
      if (selected.value)
        selected.value = types.value.find((type) => type.id === selected.value?.id)
      if (!selected.value) selected.value = types.value[0]
      await loadItems()
    } catch {
      types.value = []
      items.value = []
      typePage.total = 0
    } finally {
      loading.value = false
    }
  }
  const loadItems = async () => {
    if (!selected.value) {
      items.value = []
      itemPage.total = 0
      return
    }
    try {
      const response = await listDictionaryItems(selected.value.code, {
        page: itemPage.page,
        size: itemPage.size
      })
      items.value = response.data
      itemPage.total = response.pageInfo?.totalCount ?? items.value.length
    } catch {
      items.value = []
      itemPage.total = 0
    }
  }
  const selectType = (row: DictionaryType) => {
    selected.value = row
    itemPage.page = 1
    void loadItems()
  }
  const openType = (row?: DictionaryType) => {
    mode.value = 'type'
    editingId.value = row?.id ?? ''
    Object.assign(form, {
      code: row?.code ?? '',
      name: row?.name ?? '',
      value: '',
      label: '',
      description: row?.description ?? '',
      sortOrder: 0,
      enabled: row?.enabled ?? true
    })
    dialog.value = true
  }
  const openItem = (row?: DictionaryItem) => {
    mode.value = 'item'
    editingId.value = row?.id ?? ''
    Object.assign(form, {
      code: '',
      name: '',
      value: row?.value ?? '',
      label: row?.label ?? '',
      description: row?.description ?? '',
      sortOrder: row?.sortOrder ?? 0,
      enabled: row?.enabled ?? true
    })
    dialog.value = true
  }
  const save = async () => {
    if (!formRef.value || !(await formRef.value.validate().catch(() => false))) return
    saving.value = true
    try {
      if (mode.value === 'type') {
        const command = { name: form.name, description: form.description, enabled: form.enabled }
        if (editingId.value) await updateDictionaryType(editingId.value, command)
        else await createDictionaryType({ code: form.code, ...command })
        await loadTypes()
      } else if (selected.value) {
        const command = {
          label: form.label,
          description: form.description,
          sortOrder: form.sortOrder,
          enabled: form.enabled
        }
        if (editingId.value) await updateDictionaryItem(editingId.value, command)
        else await createDictionaryItem(selected.value.code, { value: form.value, ...command })
        await loadItems()
      }
      dialog.value = false
      ElMessage.success('保存成功')
    } catch {
      /* request layer shows errors */
    } finally {
      saving.value = false
    }
  }
  const removeType = async (row: DictionaryType) => {
    try {
      await ElMessageBox.confirm(`删除字典类型「${row.name}」？需先清空条目。`, '确认删除', {
        type: 'warning'
      })
      await deleteDictionaryType(row.id)
      ElMessage.success('删除成功')
      await loadTypes()
    } catch {
      /* cancelled or request error */
    }
  }
  const removeItem = async (row: DictionaryItem) => {
    try {
      await ElMessageBox.confirm(`删除条目「${row.label}」？`, '确认删除', { type: 'warning' })
      await deleteDictionaryItem(row.id)
      ElMessage.success('删除成功')
      await loadItems()
    } catch {
      /* cancelled or request error */
    }
  }
  onMounted(() => {
    void loadTypes()
  })
</script>

<template>
  <div class="grid gap-20px p-20px lg:grid-cols-[minmax(320px,1fr)_minmax(480px,2fr)]">
    <ElCard shadow="never">
      <div class="mb-16px flex items-center justify-between"
        ><h1 class="m-0 text-20px">字典类型</h1
        ><ElButton type="primary" @click="openType()">新增类型</ElButton></div
      >
      <ElTable
        v-loading="loading"
        :data="types"
        highlight-current-row
        :current-row-key="selected?.id"
        row-key="id"
        @current-change="(row: DictionaryType | null) => row && selectType(row)"
      >
        <ElTableColumn prop="name" label="名称" min-width="130" /><ElTableColumn
          prop="code"
          label="编码"
          min-width="140"
        />
        <ElTableColumn label="启用" width="70"
          ><template #default="{ row }">{{ row.enabled ? '是' : '否' }}</template></ElTableColumn
        >
        <ElTableColumn label="操作" width="110"
          ><template #default="{ row }"
            ><ElButton link type="primary" @click.stop="openType(row)">编辑</ElButton
            ><ElButton link type="danger" @click.stop="removeType(row)">删除</ElButton></template
          ></ElTableColumn
        >
      </ElTable>
      <div class="mt-16px flex justify-end"
        ><ElPagination
          :current-page="typePage.page"
          :page-size="typePage.size"
          :total="typePage.total"
          layout="prev, pager, next"
          @current-change="
            (page: number) => {
              typePage.page = page
              loadTypes()
            }
          "
      /></div>
    </ElCard>
    <ElCard shadow="never">
      <div class="mb-16px flex items-center justify-between"
        ><h2 class="m-0 text-18px">{{ selected ? `${selected.name} · 条目` : '字典条目' }}</h2
        ><ElButton type="primary" :disabled="!selected" @click="openItem()">新增条目</ElButton></div
      >
      <ElTable :data="items" stripe
        ><ElTableColumn prop="label" label="标签" min-width="130" /><ElTableColumn
          prop="value"
          label="值"
          min-width="120"
        /><ElTableColumn prop="sortOrder" label="排序" width="70" /><ElTableColumn
          label="启用"
          width="70"
          ><template #default="{ row }">{{ row.enabled ? '是' : '否' }}</template></ElTableColumn
        ><ElTableColumn label="操作" width="110"
          ><template #default="{ row }"
            ><ElButton link type="primary" @click="openItem(row)">编辑</ElButton
            ><ElButton link type="danger" @click="removeItem(row)">删除</ElButton></template
          ></ElTableColumn
        ></ElTable
      >
      <div class="mt-16px flex justify-end"
        ><ElPagination
          :current-page="itemPage.page"
          :page-size="itemPage.size"
          :total="itemPage.total"
          layout="total, prev, pager, next"
          @current-change="
            (page: number) => {
              itemPage.page = page
              loadItems()
            }
          "
      /></div>
    </ElCard>
    <ElDialog
      v-model="dialog"
      :title="`${editingId ? '编辑' : '新增'}字典${mode === 'type' ? '类型' : '条目'}`"
      width="520px"
      destroy-on-close
    >
      <ElForm ref="formRef" :model="form" :rules="rules" label-width="90px">
        <template v-if="mode === 'type'"
          ><ElFormItem label="编码" prop="code"
            ><ElInput v-model="form.code" :disabled="!!editingId" /></ElFormItem
          ><ElFormItem label="名称" prop="name"><ElInput v-model="form.name" /></ElFormItem
        ></template>
        <template v-else
          ><ElFormItem label="值" prop="value"
            ><ElInput v-model="form.value" :disabled="!!editingId" /></ElFormItem
          ><ElFormItem label="标签" prop="label"><ElInput v-model="form.label" /></ElFormItem
          ><ElFormItem label="排序"><ElInputNumber v-model="form.sortOrder" :min="0" /></ElFormItem
        ></template>
        <ElFormItem label="描述"
          ><ElInput v-model="form.description" type="textarea" :rows="2" /></ElFormItem
        ><ElFormItem label="启用"><ElSwitch v-model="form.enabled" /></ElFormItem>
      </ElForm>
      <template #footer
        ><ElButton @click="dialog = false">取消</ElButton
        ><ElButton type="primary" :loading="saving" @click="save">保存</ElButton></template
      >
    </ElDialog>
  </div>
</template>

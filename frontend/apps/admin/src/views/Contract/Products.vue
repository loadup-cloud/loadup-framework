<script setup lang="ts">
  import { computed, onMounted, reactive, ref } from 'vue'
  import {
    ElButton,
    ElCard,
    ElCheckbox,
    ElCheckboxGroup,
    ElEmpty,
    ElInput,
    ElInputNumber,
    ElMessage,
    ElOption,
    ElSelect,
    ElSwitch,
    ElTable,
    ElTableColumn
  } from 'element-plus'
  import DraftNotice from './DraftNotice.vue'
  import CatalogLifecycle from './CatalogLifecycle.vue'
  import {
    codePattern,
    draftId,
    useContractDrafts,
    type OverrideLayer,
    type ParameterDraft,
    type ProductDraft,
    type ValueType
  } from './drafts'

  const drafts = useContractDrafts()
  const activeId = ref('')
  const emptyProduct = (): ProductDraft => ({
    id: draftId(),
    productCode: '',
    version: 1,
    capabilityCode: '',
    parameters: [],
    conditionId: '',
    dependencies: [],
    exclusions: []
  })
  const form = reactive<ProductDraft>(emptyProduct())
  const isNew = computed(() => !drafts.products.some((item) => item.id === activeId.value))
  const conditions = computed(() =>
    drafts.conditions
      .filter((item) => item.status === 'PUBLISHED')
      .map((item) => ({ value: item.id, label: `${item.code} v${item.version}` }))
  )
  onMounted(() => {
    void drafts.loadAll().catch(() => undefined)
  })
  const copyVersion = () => {
    activeId.value = ''
    form.id = draftId()
    form.status = undefined
    form.rowVersion = undefined
    form.version =
      Math.max(
        0,
        ...drafts.products
          .filter((item) => item.productCode === form.productCode)
          .map((item) => item.version)
      ) + 1
  }
  const newProduct = () => {
    activeId.value = ''
    Object.assign(form, emptyProduct())
    form.status = undefined
    form.rowVersion = undefined
  }
  const selectProduct = (item: ProductDraft) => {
    activeId.value = item.id
    Object.assign(form, JSON.parse(JSON.stringify(item)) as ProductDraft)
  }
  const addParameter = () =>
    form.parameters.push({
      key: '',
      type: 'STRING',
      required: false,
      defaultValue: '',
      minimum: '',
      maximum: '',
      allowedValues: [],
      editableLayers: []
    })
  const removeParameter = (index: number) => form.parameters.splice(index, 1)
  const changeType = (row: ParameterDraft, type: ValueType) => {
    row.type = type
    row.defaultValue = ''
    row.minimum = ''
    row.maximum = ''
    row.allowedValues = []
  }
  const save = async () => {
    if (!codePattern.test(form.productCode) || !codePattern.test(form.capabilityCode)) {
      ElMessage.warning('产品编码和能力编码只能使用字母、数字、点、下划线或连字符')
      return
    }
    if (!Number.isInteger(form.version) || form.version < 1) {
      ElMessage.warning('版本必须为正整数')
      return
    }
    if (
      drafts.products.some(
        (item) =>
          item.id !== form.id &&
          item.productCode === form.productCode &&
          item.version === form.version
      )
    ) {
      ElMessage.warning('该产品版本已存在')
      return
    }
    const keys = form.parameters.map((item) => item.key)
    if (keys.some((key) => !codePattern.test(key)) || new Set(keys).size !== keys.length) {
      ElMessage.warning('参数键必须合法且不能重复')
      return
    }
    for (const item of form.parameters) {
      if (
        item.type === 'INTEGER' &&
        [item.defaultValue, item.minimum, item.maximum, ...item.allowedValues]
          .filter(Boolean)
          .some((value) => !/^-?\d+$/.test(value))
      ) {
        ElMessage.warning(`${item.key} 需要整数值`)
        return
      }
      if (
        item.type === 'DECIMAL' &&
        [item.defaultValue, item.minimum, item.maximum, ...item.allowedValues]
          .filter(Boolean)
          .some((value) => !/^-?\d+(\.\d+)?$/.test(value))
      ) {
        ElMessage.warning(`${item.key} 需要十进制字符串`)
        return
      }
      if (
        item.type === 'BOOLEAN' &&
        [item.defaultValue, ...item.allowedValues]
          .filter(Boolean)
          .some((value) => value !== 'true' && value !== 'false')
      ) {
        ElMessage.warning(`${item.key} 只能使用 true 或 false`)
        return
      }
      if (item.minimum && item.maximum && Number(item.minimum) > Number(item.maximum)) {
        ElMessage.warning(`${item.key} 的下限大于上限`)
        return
      }
    }
    try {
      await drafts.saveProduct(form)
      activeId.value = form.id
      ElMessage.success('已保存到服务端')
    } catch {
      /* Request client shows the error. */
    }
  }
  const layers: { value: OverrideLayer; label: string }[] = [
    { value: 'BUNDLE', label: '组合' },
    { value: 'SALES_PLAN', label: '方案' },
    { value: 'MERCHANT', label: '商户' }
  ]
</script>

<template>
  <div class="p-20px"
    ><DraftNotice /><div class="grid gap-20px xl:grid-cols-[280px_minmax(0,1fr)]">
      <ElCard shadow="never"
        ><div class="mb-14px flex items-center justify-between"
          ><h1 class="m-0 text-20px">产品目录</h1
          ><ElButton type="primary" @click="newProduct">新建</ElButton></div
        ><ElEmpty v-if="!drafts.products.length" description="暂无产品版本" /><button
          v-for="item in drafts.products"
          :key="item.id"
          type="button"
          class="mb-8px block w-full rounded border border-solid border-[var(--el-border-color)] p-10px text-left"
          :class="activeId === item.id ? 'bg-[var(--el-color-primary-light-9)]' : ''"
          @click="selectProduct(item)"
          ><strong>{{ item.productCode }}</strong
          ><span class="ml-8px text-sm">v{{ item.version }}</span
          ><div class="mt-4px text-xs opacity-70">{{ item.capabilityCode }}</div></button
        ></ElCard
      >
      <ElCard shadow="never"
        ><div class="mb-18px flex items-center justify-between"
          ><h2 class="m-0 text-18px">{{ isNew ? '新建产品版本' : '产品版本' }}</h2
          ><div class="flex items-center gap-8px"
            ><CatalogLifecycle kind="PRODUCT" :item="form" /><ElButton
              v-if="form.status"
              @click="copyVersion"
              >复制为新版本</ElButton
            ><ElButton
              type="primary"
              :disabled="!!form.status && form.status !== 'DRAFT'"
              @click="save"
              >保存版本</ElButton
            ></div
          ></div
        >
        <div class="mb-18px grid gap-12px md:grid-cols-3"
          ><label
            >产品编码<ElInput
              v-model="form.productCode"
              :disabled="!!form.status"
              placeholder="WECHAT_SCAN" /></label
          ><label
            >版本<ElInputNumber
              v-model="form.version"
              :disabled="!!form.status"
              :min="1"
              :precision="0"
              class="!w-full" /></label
          ><label
            >能力编码<ElInput v-model="form.capabilityCode" placeholder="payment.collect" /></label
        ></div>
        <div class="mb-18px grid gap-12px md:grid-cols-3"
          ><label
            >使用条件<ElSelect
              v-model="form.conditionId"
              clearable
              placeholder="始终允许"
              class="w-full"
              ><ElOption
                v-for="item in conditions"
                :key="item.value"
                :label="item.label"
                :value="item.value" /></ElSelect></label
          ><label
            >依赖产品编码<ElSelect
              v-model="form.dependencies"
              multiple
              allow-create
              filterable
              default-first-option
              class="w-full"
              placeholder="输入编码后回车"
              ><ElOption
                v-for="item in drafts.products"
                :key="item.id"
                :label="item.productCode"
                :value="item.productCode" /></ElSelect></label
          ><label
            >互斥产品编码<ElSelect
              v-model="form.exclusions"
              multiple
              allow-create
              filterable
              default-first-option
              class="w-full"
              placeholder="输入编码后回车"
              ><ElOption
                v-for="item in drafts.products"
                :key="item.id"
                :label="item.productCode"
                :value="item.productCode" /></ElSelect></label
        ></div>
        <div class="mb-12px flex items-center justify-between"
          ><h3 class="m-0 text-16px">参数定义</h3
          ><ElButton @click="addParameter">添加参数</ElButton></div
        >
        <ElTable :data="form.parameters" border
          ><ElTableColumn label="键 / 类型" min-width="170"
            ><template #default="{ row }"
              ><ElInput v-model="row.key" placeholder="fee.rate" class="mb-8px" /><ElSelect
                :model-value="row.type"
                class="w-full"
                @update:model-value="(value: ValueType) => changeType(row, value)"
                ><ElOption label="字符串" value="STRING" /><ElOption
                  label="整数"
                  value="INTEGER" /><ElOption label="十进制" value="DECIMAL" /><ElOption
                  label="布尔"
                  value="BOOLEAN" /></ElSelect></template
          ></ElTableColumn>
          <ElTableColumn label="约束" min-width="170"
            ><template #default="{ row }"
              ><div class="mb-8px flex items-center gap-8px"
                ><ElSwitch v-model="row.required" />必填</div
              ><ElInput v-model="row.defaultValue" placeholder="默认值（空表示未设置）" /><div
                v-if="row.type === 'INTEGER' || row.type === 'DECIMAL'"
                class="mt-8px flex gap-5px"
                ><ElInput v-model="row.minimum" placeholder="最小" /><ElInput
                  v-model="row.maximum"
                  placeholder="最大" /></div></template
          ></ElTableColumn>
          <ElTableColumn label="允许值 / 可覆盖层" min-width="220"
            ><template #default="{ row }"
              ><ElSelect
                v-model="row.allowedValues"
                multiple
                allow-create
                filterable
                default-first-option
                class="w-full"
                placeholder="输入允许值后回车"
              /><ElCheckboxGroup v-model="row.editableLayers" class="mt-8px"
                ><ElCheckbox v-for="layer in layers" :key="layer.value" :value="layer.value">{{
                  layer.label
                }}</ElCheckbox></ElCheckboxGroup
              ></template
            ></ElTableColumn
          >
          <ElTableColumn label="操作" width="75"
            ><template #default="{ $index }"
              ><ElButton link type="danger" @click="removeParameter($index)"
                >移除</ElButton
              ></template
            ></ElTableColumn
          ></ElTable
        >
      </ElCard>
    </div></div
  >
</template>

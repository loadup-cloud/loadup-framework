<script setup lang="ts">
  import { computed, onMounted, reactive, ref } from 'vue'
  import {
    ElButton,
    ElCard,
    ElEmpty,
    ElInput,
    ElInputNumber,
    ElMessage,
    ElOption,
    ElSelect,
    ElSwitch,
    ElTag
  } from 'element-plus'
  import DraftNotice from './DraftNotice.vue'
  import CatalogLifecycle from './CatalogLifecycle.vue'
  import { codePattern, draftId, useContractDrafts, type SalesPlanDraft } from './drafts'

  const drafts = useContractDrafts()
  const activeId = ref('')
  const blank = (): SalesPlanDraft => ({
    id: draftId(),
    planCode: '',
    version: 1,
    bundles: [],
    values: {},
    merchantPolicies: {},
    eligibilityConditionId: '',
    usageConditionId: '',
    saleStartsAt: '',
    saleEndsAt: ''
  })
  const form = reactive<SalesPlanDraft>(blank())
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
        ...drafts.plans
          .filter((item) => item.planCode === form.planCode)
          .map((item) => item.version)
      ) + 1
  }
  const conditions = computed(() =>
    drafts.conditions
      .filter((item) => item.status === 'PUBLISHED')
      .map((item) => ({ value: item.id, label: `${item.code} v${item.version}` }))
  )
  const items = computed(() =>
    form.bundles.flatMap((selection) => {
      const bundle = drafts.bundles.find((item) => item.id === selection.bundleId)
      return (bundle?.items ?? []).map((item) => ({
        key: `${selection.alias}.${item.itemKey}`,
        item,
        bundle,
        product: drafts.products.find((product) => product.id === item.productId)
      }))
    })
  )
  const selectPlan = (item: SalesPlanDraft) => {
    activeId.value = item.id
    Object.assign(form, JSON.parse(JSON.stringify(item)) as SalesPlanDraft)
  }
  const newPlan = () => {
    activeId.value = ''
    Object.assign(form, blank())
    form.status = undefined
    form.rowVersion = undefined
  }
  const addBundle = () => form.bundles.push({ alias: '', bundleId: '' })
  const clearOverrides = () => {
    form.values = {}
    form.merchantPolicies = {}
  }
  const removeBundle = (index: number) => {
    form.bundles.splice(index, 1)
    clearOverrides()
  }
  const planValue = (key: string, parameter: string) => form.values[key]?.[parameter] ?? ''
  const setPlanValue = (key: string, parameter: string, value: string) => {
    ;(form.values[key] ??= {})[parameter] = value
  }
  const policy = (key: string, parameter: string) =>
    ((form.merchantPolicies[key] ??= {})[parameter] ??= {
      minimum: '',
      maximum: '',
      allowedValues: []
    })
  const setPolicy = (key: string, parameter: string, enabled: boolean) => {
    if (enabled) policy(key, parameter)
    else if (form.merchantPolicies[key]) {
      delete form.merchantPolicies[key][parameter]
      if (!Object.keys(form.merchantPolicies[key]).length) delete form.merchantPolicies[key]
    }
  }
  const save = async () => {
    if (!form.saleStartsAt || !Number.isFinite(Date.parse(form.saleStartsAt))) {
      ElMessage.warning('请填写销售开始时间')
      return
    }
    if (form.saleEndsAt && !Number.isFinite(Date.parse(form.saleEndsAt))) {
      ElMessage.warning('销售结束时间无效')
      return
    }
    if (!codePattern.test(form.planCode) || !Number.isInteger(form.version) || form.version < 1) {
      ElMessage.warning('请填写合法的方案编码和正整数版本')
      return
    }
    if (
      drafts.plans.some(
        (item) =>
          item.id !== form.id && item.planCode === form.planCode && item.version === form.version
      )
    ) {
      ElMessage.warning('该方案版本已存在')
      return
    }
    if (
      !form.bundles.length ||
      form.bundles.length > 64 ||
      form.bundles.some(
        (item) =>
          !codePattern.test(item.alias) ||
          !drafts.bundles.some(
            (bundle) => bundle.id === item.bundleId && bundle.status === 'PUBLISHED'
          )
      )
    ) {
      ElMessage.warning('请添加有效的固定组合版本与别名')
      return
    }
    if (new Set(form.bundles.map((item) => item.alias)).size !== form.bundles.length) {
      ElMessage.warning('组合别名不能重复')
      return
    }
    if (form.saleStartsAt && form.saleEndsAt && form.saleStartsAt >= form.saleEndsAt) {
      ElMessage.warning('销售结束时间需晚于开始时间')
      return
    }
    const known = new Map(items.value.map((item) => [item.key, item.product]))
    for (const [key, values] of Object.entries(form.values)) {
      if (
        Object.keys(values).some(
          (name) =>
            !known
              .get(key)
              ?.parameters.some(
                (parameter) =>
                  parameter.key === name && parameter.editableLayers.includes('SALES_PLAN')
              )
        )
      ) {
        ElMessage.warning('方案值包含不可覆盖的参数')
        return
      }
    }
    for (const [key, policies] of Object.entries(form.merchantPolicies)) {
      if (
        Object.keys(policies).some(
          (name) =>
            !known
              .get(key)
              ?.parameters.some(
                (parameter) =>
                  parameter.key === name && parameter.editableLayers.includes('MERCHANT')
              )
        )
      ) {
        ElMessage.warning('商户协商范围包含不可协商的参数')
        return
      }
    }
    try {
      await drafts.savePlan(form)
      activeId.value = form.id
      ElMessage.success('已保存到服务端')
    } catch {
      /* Request client shows the error. */
    }
  }
</script>

<template>
  <div class="p-20px"
    ><DraftNotice /><div class="grid gap-20px xl:grid-cols-[280px_minmax(0,1fr)]">
      <ElCard shadow="never"
        ><div class="mb-14px flex items-center justify-between"
          ><h1 class="m-0 text-20px">销售方案</h1
          ><ElButton type="primary" @click="newPlan">新建</ElButton></div
        ><ElEmpty v-if="!drafts.plans.length" description="暂无方案版本" /><button
          v-for="item in drafts.plans"
          :key="item.id"
          type="button"
          class="mb-8px block w-full rounded border border-solid border-[var(--el-border-color)] p-10px text-left"
          :class="activeId === item.id ? 'bg-[var(--el-color-primary-light-9)]' : ''"
          @click="selectPlan(item)"
          ><strong>{{ item.planCode }}</strong
          ><span class="ml-8px text-sm">v{{ item.version }}</span
          ><div class="mt-4px text-xs opacity-70">{{ item.bundles.length }} 个组合</div></button
        ></ElCard
      >
      <ElCard shadow="never"
        ><div class="mb-18px flex items-center justify-between"
          ><h2 class="m-0 text-18px">方案版本</h2
          ><div class="flex items-center gap-8px"
            ><CatalogLifecycle kind="SALES_PLAN" :item="form" /><ElButton
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
        <div class="grid gap-12px md:grid-cols-2"
          ><label
            >方案编码<ElInput
              v-model="form.planCode"
              :disabled="!!form.status"
              placeholder="STANDARD" /></label
          ><label
            >版本<ElInputNumber
              v-model="form.version"
              :disabled="!!form.status"
              :min="1"
              :precision="0"
              class="!w-full" /></label
          ><label
            >销售开始时间（本地时间）<ElInput
              v-model="form.saleStartsAt"
              type="datetime-local" /></label
          ><label
            >销售结束时间（本地时间）<ElInput
              v-model="form.saleEndsAt"
              type="datetime-local"
            /><small class="opacity-60">留空表示无限制</small></label
          ><label
            >准入条件<ElSelect
              v-model="form.eligibilityConditionId"
              clearable
              placeholder="无"
              class="w-full"
              ><ElOption
                v-for="item in conditions"
                :key="item.value"
                :label="item.label"
                :value="item.value" /></ElSelect></label
          ><label
            >使用条件<ElSelect
              v-model="form.usageConditionId"
              clearable
              placeholder="无"
              class="w-full"
              ><ElOption
                v-for="item in conditions"
                :key="item.value"
                :label="item.label"
                :value="item.value" /></ElSelect></label
        ></div>
        <div class="mb-10px mt-22px flex items-center justify-between"
          ><h3 class="m-0 text-16px">固定组合版本</h3
          ><ElButton
            :disabled="!drafts.bundles.some((item) => item.status === 'PUBLISHED')"
            @click="addBundle"
            >添加组合</ElButton
          ></div
        ><ElEmpty
          v-if="!drafts.bundles.some((item) => item.status === 'PUBLISHED')"
          description="请先发布产品组合版本"
        /><div
          v-for="(selection, index) in form.bundles"
          :key="index"
          class="mb-10px grid items-end gap-10px rounded border border-solid border-[var(--el-border-color)] p-12px md:grid-cols-[1fr_1fr_auto]"
          ><label
            >别名<ElInput
              v-model="selection.alias"
              placeholder="main"
              @change="clearOverrides" /></label
          ><label
            >组合版本<ElSelect v-model="selection.bundleId" class="w-full" @change="clearOverrides"
              ><ElOption
                v-for="bundle in drafts.bundles.filter((item) => item.status === 'PUBLISHED')"
                :key="bundle.id"
                :label="`${bundle.bundleCode} v${bundle.version}`"
                :value="bundle.id" /></ElSelect></label
          ><ElButton type="danger" link @click="removeBundle(index)">移除</ElButton></div
        >
        <h3 class="mt-22px text-16px">产品项与参数协商</h3
        ><p class="text-sm opacity-70"
          >参数预览按产品默认值、组合覆盖值、方案覆盖值的顺序展示；最终结果以服务端校验为准。</p
        ><div
          v-for="entry in items"
          :key="entry.key"
          class="mb-12px rounded border border-solid border-[var(--el-border-color)] p-14px"
          ><div class="mb-8px flex items-center gap-8px"
            ><strong>{{ entry.key }}</strong
            ><ElTag size="small"
              >{{ entry.product?.productCode }} v{{ entry.product?.version }}</ElTag
            ></div
          ><div
            v-for="parameter in entry.product?.parameters ?? []"
            :key="parameter.key"
            class="mb-12px grid gap-8px md:grid-cols-2"
            ><div
              ><strong>{{ parameter.key }}</strong> <small>{{ parameter.type }}</small
              ><div class="text-xs opacity-65"
                >产品默认：{{ parameter.defaultValue || '（空）' }} · 组合：{{
                  entry.item.values[parameter.key] || '（继承）'
                }}</div
              ></div
            ><label v-if="parameter.editableLayers.includes('SALES_PLAN')"
              >方案覆盖值<ElInput
                :model-value="planValue(entry.key, parameter.key)"
                placeholder="留空则继承"
                @update:model-value="setPlanValue(entry.key, parameter.key, $event)" /></label
            ><div v-if="parameter.editableLayers.includes('MERCHANT')" class="md:col-span-2"
              ><div class="mb-6px flex items-center gap-8px text-sm"
                >允许商户协商
                <ElSwitch
                  :model-value="!!form.merchantPolicies[entry.key]?.[parameter.key]"
                  @update:model-value="setPolicy(entry.key, parameter.key, !!$event)" /></div
              ><div
                v-if="form.merchantPolicies[entry.key]?.[parameter.key]"
                class="grid gap-8px md:grid-cols-3"
                ><ElInput
                  v-model="policy(entry.key, parameter.key).minimum"
                  placeholder="最小值" /><ElInput
                  v-model="policy(entry.key, parameter.key).maximum"
                  placeholder="最大值" /><ElSelect
                  v-model="policy(entry.key, parameter.key).allowedValues"
                  multiple
                  allow-create
                  filterable
                  default-first-option
                  placeholder="允许值（可选）" /></div></div></div
        ></div>
      </ElCard> </div
  ></div>
</template>

<script setup lang="ts">
  import { computed, onMounted, reactive, ref, watch } from 'vue'
  import {
    ElButton,
    ElCard,
    ElCheckbox,
    ElEmpty,
    ElInput,
    ElMessage,
    ElMessageBox,
    ElOption,
    ElPagination,
    ElSelect,
    ElTag
  } from 'element-plus'
  import {
    changeContractStatus,
    getContract,
    newSigningRequestKey,
    previewContract,
    queryContracts,
    signContract,
    signingCommand,
    type ContractStatus,
    type MerchantContract
  } from '@/api/contract'
  import { queryMerchants, type Merchant } from '@/api/merchant'
  import DraftNotice from './DraftNotice.vue'
  import { codePattern, draftId, useContractDrafts, type ContractIntentDraft } from './drafts'

  const merchantOptions = ref<Merchant[]>([])
  const merchantLoading = ref(false)
  let merchantSearch = 0
  const searchMerchants = async (name = '') => {
    const request = ++merchantSearch
    merchantLoading.value = true
    try {
      const { data } = await queryMerchants({
        name: name || undefined,
        status: 'ACTIVE',
        page: 1,
        size: 20
      })
      if (request === merchantSearch) merchantOptions.value = data.items
    } catch {
      /* The shared request client displays errors. */
    } finally {
      if (request === merchantSearch) merchantLoading.value = false
    }
  }
  onMounted(() => {
    void searchMerchants()
  })
  const drafts = useContractDrafts()
  const blank = (): ContractIntentDraft => ({
    id: draftId(),
    merchantId: '',
    scopeKey: '',
    planId: '',
    selectedItems: [],
    values: {},
    effectiveFrom: '',
    effectiveTo: ''
  })
  const form = reactive<ContractIntentDraft>(blank())
  const requestKey = ref(newSigningRequestKey())
  const serverPreview = ref<MerchantContract | null>(null)
  const selected = ref<MerchantContract | null>(null)
  const contracts = ref<MerchantContract[]>([])
  const page = ref(1)
  const total = ref(0)
  const merchantFilter = ref('')
  const busy = ref(false)
  const plan = computed(() =>
    drafts.plans.find((item) => item.id === form.planId && item.status === 'PUBLISHED')
  )
  const items = computed(() =>
    (plan.value?.bundles ?? []).flatMap((selection) => {
      const bundle = drafts.bundles.find((item) => item.id === selection.bundleId)
      return (bundle?.items ?? []).map((item) => ({
        key: `${selection.alias}.${item.itemKey}`,
        item,
        product: drafts.products.find((product) => product.id === item.productId)
      }))
    })
  )
  const previewRows = computed(() =>
    Object.entries(serverPreview.value?.items ?? {}).flatMap(([itemKey, item]) =>
      Object.entries(item.configuration.values).map(([key, value]) => ({
        itemKey,
        key,
        value: value.value,
        source: item.configuration.origins[key]
      }))
    )
  )
  watch(
    form,
    () => {
      serverPreview.value = null
      requestKey.value = newSigningRequestKey()
    },
    { deep: true }
  )
  const loadContracts = async () => {
    const { data } = await queryContracts({
      page: page.value,
      size: 20,
      merchantId: merchantFilter.value || undefined
    })
    contracts.value = data.items
    total.value = data.total
  }
  const searchContracts = async () => {
    page.value = 1
    try {
      await loadContracts()
    } catch {
      /* Request client shows the error. */
    }
  }
  onMounted(() => {
    void drafts.loadAll().catch(() => undefined)
    void loadContracts().catch(() => undefined)
  })
  const changePlan = () => {
    form.selectedItems = items.value
      .filter((entry) => entry.item.required || entry.item.defaultSelected)
      .map((entry) => entry.key)
    form.values = {}
  }
  const toggleItem = (key: string, checked: boolean) => {
    form.selectedItems = checked
      ? [...form.selectedItems, key]
      : form.selectedItems.filter((item) => item !== key)
    if (!checked) delete form.values[key]
  }
  const merchantValue = (key: string, parameter: string) => form.values[key]?.[parameter] ?? ''
  const setMerchantValue = (key: string, parameter: string, value: string) => {
    ;(form.values[key] ??= {})[parameter] = value
  }
  const valid = () => {
    if (
      [form.effectiveFrom, form.effectiveTo]
        .filter(Boolean)
        .some((value) => !Number.isFinite(Date.parse(value)))
    ) {
      ElMessage.warning('生效时间格式无效')
      return false
    }
    if (!codePattern.test(form.merchantId) || !codePattern.test(form.scopeKey) || !plan.value) {
      ElMessage.warning('请填写商户、范围并选择已发布的方案版本')
      return false
    }
    if (
      !form.selectedItems.length ||
      items.value.some((entry) => entry.item.required && !form.selectedItems.includes(entry.key))
    ) {
      ElMessage.warning('至少选择一项，且必选项不能取消')
      return false
    }
    if (form.selectedItems.some((key) => !items.value.some((entry) => entry.key === key))) {
      ElMessage.warning('选择包含未知产品项')
      return false
    }
    if (form.effectiveFrom && form.effectiveTo && form.effectiveFrom >= form.effectiveTo) {
      ElMessage.warning('结束时间需晚于开始时间')
      return false
    }
    for (const [key, values] of Object.entries(form.values)) {
      const product = items.value.find((entry) => entry.key === key)?.product
      if (
        !form.selectedItems.includes(key) ||
        Object.entries(values).some(
          ([name, value]) =>
            value !== '' &&
            !product?.parameters.some(
              (parameter) =>
                parameter.key === name &&
                parameter.editableLayers.includes('MERCHANT') &&
                !!plan.value?.merchantPolicies[key]?.[name]
            )
        )
      ) {
        ElMessage.warning('商户值超出可协商参数范围')
        return false
      }
    }
    return true
  }
  const preview = async () => {
    if (!valid()) return
    busy.value = true
    try {
      serverPreview.value = (await previewContract(signingCommand(form, requestKey.value))).data
    } catch {
      /* Request client shows the error. */
    } finally {
      busy.value = false
    }
  }
  const sign = async () => {
    if (!valid()) return
    try {
      await ElMessageBox.confirm('将按当前条款正式签约并持久化，确定继续？', '确认签约', {
        type: 'warning'
      })
    } catch {
      return
    }
    busy.value = true
    try {
      const signed = (await signContract(signingCommand(form, requestKey.value))).data
      selected.value = signed
      ElMessage.success('签约成功')
      Object.assign(form, blank())
      page.value = 1
      await loadContracts()
    } catch {
      /* Keep the same request key for a retry after a transport failure. */
    } finally {
      busy.value = false
    }
  }
  const openContract = async (id: string) => {
    try {
      selected.value = (await getContract(id)).data
    } catch {
      /* Request client shows the error. */
    }
  }
  const changeStatus = async (status: ContractStatus) => {
    if (!selected.value) return
    try {
      await ElMessageBox.confirm(`确定将合约状态改为 ${status}？`, '确认状态变更', {
        type: 'warning'
      })
    } catch {
      return
    }
    busy.value = true
    try {
      selected.value = (
        await changeContractStatus(selected.value.id, selected.value.generation, status)
      ).data
      await loadContracts()
      ElMessage.success('状态已更新')
    } catch {
      /* Request client shows the error. */
    } finally {
      busy.value = false
    }
  }
</script>

<template>
  <div class="p-20px"
    ><DraftNotice /><div class="grid gap-20px xl:grid-cols-[320px_minmax(0,1fr)]">
      <ElCard shadow="never"
        ><h1 class="mt-0 text-20px">已签商户合约</h1
        ><div class="mb-12px flex gap-8px"
          ><ElInput
            v-model="merchantFilter"
            placeholder="商户标识"
            clearable
            @keyup.enter="searchContracts"
          /><ElButton @click="searchContracts">查询</ElButton></div
        ><ElEmpty v-if="!contracts.length" description="暂无合约" /><button
          v-for="item in contracts"
          :key="item.id"
          type="button"
          class="mb-8px block w-full rounded border border-solid border-[var(--el-border-color)] p-10px text-left"
          @click="openContract(item.id)"
          ><strong>{{ item.merchantId }}</strong
          ><ElTag class="ml-8px" size="small">{{ item.status }}</ElTag
          ><div class="mt-5px text-xs opacity-70"
            >{{ item.scopeKey }} · 修订 {{ item.revision }}</div
          ></button
        ><ElPagination
          v-if="total > 20"
          v-model:current-page="page"
          :page-size="20"
          :total="total"
          layout="prev, pager, next"
          @current-change="loadContracts"
      /></ElCard>
      <div class="grid gap-20px"
        ><ElCard shadow="never"
          ><div class="mb-14px flex items-center justify-between"
            ><h2 class="m-0 text-18px">拟签条款</h2
            ><div class="flex gap-8px"
              ><ElButton :loading="busy" @click="preview">服务端预览</ElButton
              ><ElButton type="primary" :loading="busy" @click="sign">正式签约</ElButton></div
            ></div
          ><p class="text-sm opacity-70"
            >预览与签约都会校验可信商户资料及已发布方案；预览结果不保证之后的条件和时间仍然相同。</p
          >
          <div class="grid gap-12px md:grid-cols-2"
            ><label
              >签约商户<ElSelect
                v-model="form.merchantId"
                filterable
                remote
                :remote-method="searchMerchants"
                :loading="merchantLoading"
                placeholder="选择已启用商户（按名称搜索）"
                ><ElOption
                  v-for="merchant in merchantOptions"
                  :key="merchant.id"
                  :label="`${merchant.name} (${merchant.merchantCode})`"
                  :value="merchant.id" /></ElSelect></label
            ><label>业务范围<ElInput v-model="form.scopeKey" placeholder="store01" /></label
            ><label class="md:col-span-2"
              >已发布销售方案<ElSelect
                v-model="form.planId"
                class="w-full"
                placeholder="选择方案"
                @change="changePlan"
                ><ElOption
                  v-for="item in drafts.plans.filter((entry) => entry.status === 'PUBLISHED')"
                  :key="item.id"
                  :label="`${item.planCode} v${item.version}`"
                  :value="item.id" /></ElSelect></label
            ><label
              >拟生效时间（本地时间）<ElInput
                v-model="form.effectiveFrom"
                type="datetime-local" /></label
            ><label
              >拟结束时间（本地时间）<ElInput
                v-model="form.effectiveTo"
                type="datetime-local" /></label
          ></div>
          <h3 class="mt-20px text-16px">选择产品项</h3
          ><ElEmpty v-if="!plan" description="请先选择已发布方案" /><div
            v-for="entry in items"
            :key="entry.key"
            class="mb-10px rounded border border-solid border-[var(--el-border-color)] p-12px"
            ><ElCheckbox
              :model-value="form.selectedItems.includes(entry.key)"
              :disabled="entry.item.required"
              @update:model-value="toggleItem(entry.key, !!$event)"
              >{{ entry.key }}</ElCheckbox
            ><ElTag v-if="entry.item.required" size="small" class="ml-6px">必选</ElTag
            ><span class="ml-8px text-xs opacity-65"
              >{{ entry.product?.productCode }} v{{ entry.product?.version }}</span
            ><div
              v-if="form.selectedItems.includes(entry.key)"
              class="mt-10px grid gap-10px md:grid-cols-2"
              ><template v-for="parameter in entry.product?.parameters ?? []" :key="parameter.key"
                ><label
                  v-if="
                    parameter.editableLayers.includes('MERCHANT') &&
                    plan?.merchantPolicies[entry.key]?.[parameter.key]
                  "
                  >商户覆盖 {{ parameter.key }} <small>{{ parameter.type }}</small
                  ><ElInput
                    :model-value="merchantValue(entry.key, parameter.key)"
                    placeholder="留空则继承方案值"
                    @update:model-value="setMerchantValue(entry.key, parameter.key, $event)"
                  /><small class="opacity-60"
                    >范围：{{
                      plan?.merchantPolicies[entry.key]?.[parameter.key]?.minimum || '无下限'
                    }}
                    ~
                    {{
                      plan?.merchantPolicies[entry.key]?.[parameter.key]?.maximum || '无上限'
                    }}</small
                  ></label
                ></template
              ></div
            ></div
          >
          <div v-if="serverPreview" class="mt-18px"
            ><h3 class="text-16px">服务端预览 · {{ serverPreview.snapshotHash }}</h3
            ><div
              v-for="row in previewRows"
              :key="`${row.itemKey}.${row.key}`"
              class="grid gap-6px border-b border-solid border-[var(--el-border-color)] py-8px text-sm md:grid-cols-[1fr_1fr_1fr_auto]"
              ><span>{{ row.itemKey }}</span
              ><span>{{ row.key }}</span
              ><strong>{{ row.value }}</strong
              ><ElTag size="small" type="info"
                >{{ row.source?.layer }} {{ row.source?.sourceCode }} v{{
                  row.source?.sourceVersion
                }}</ElTag
              ></div
            ></div
          >
        </ElCard>
        <ElCard v-if="selected" shadow="never"
          ><div class="flex items-center justify-between"
            ><h2 class="m-0 text-18px">已签合约详情</h2><ElTag>{{ selected.status }}</ElTag></div
          ><p class="text-sm"
            >{{ selected.merchantId }} / {{ selected.scopeKey }} · 修订 {{ selected.revision }} ·
            代数 {{ selected.generation }}</p
          ><p class="break-all text-xs"
            >ID：{{ selected.id }}<br />快照摘要：{{ selected.snapshotHash }}</p
          ><p class="text-sm"
            >生效：{{ selected.effectiveFrom }} 至 {{ selected.effectiveTo || '无限制' }}</p
          ><div class="flex gap-8px"
            ><ElButton
              v-if="selected.status === 'NORMAL'"
              :loading="busy"
              @click="changeStatus('SUSPENDED')"
              >暂停</ElButton
            ><ElButton
              v-if="selected.status === 'SUSPENDED'"
              :loading="busy"
              @click="changeStatus('NORMAL')"
              >恢复</ElButton
            ><ElButton
              v-if="selected.status !== 'TERMINATED'"
              type="danger"
              :loading="busy"
              @click="changeStatus('TERMINATED')"
              >终止</ElButton
            ></div
          ><div
            v-for="(item, key) in selected.items"
            :key="key"
            class="mt-12px rounded border border-solid border-[var(--el-border-color)] p-12px"
            ><strong>{{ key }}</strong> · {{ item.productCode }} v{{ item.productVersion
            }}<div
              v-for="(value, name) in item.configuration.values"
              :key="name"
              class="mt-5px text-sm"
              >{{ name }}：{{ value.value }}
              <small class="opacity-60">{{ item.configuration.origins[name]?.layer }}</small></div
            ></div
          ></ElCard
        ></div
      >
    </div></div
  >
</template>

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
  import {
    codePattern,
    draftId,
    useContractDrafts,
    type BundleDraft,
    type BundleItemDraft
  } from './drafts'

  const drafts = useContractDrafts()
  const activeId = ref('')
  const form = reactive<BundleDraft>({ id: draftId(), bundleCode: '', version: 1, items: [] })
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
        ...drafts.bundles
          .filter((item) => item.bundleCode === form.bundleCode)
          .map((item) => item.version)
      ) + 1
  }
  const products = computed(() =>
    drafts.products
      .filter((item) => item.status === 'PUBLISHED')
      .map((item) => ({
        value: item.id,
        label: `${item.productCode} v${item.version}`
      }))
  )
  const conditions = computed(() =>
    drafts.conditions
      .filter((item) => item.status === 'PUBLISHED')
      .map((item) => ({ value: item.id, label: `${item.code} v${item.version}` }))
  )
  const productFor = (item: BundleItemDraft) =>
    drafts.products.find((product) => product.id === item.productId)
  const editableParameters = (item: BundleItemDraft) =>
    productFor(item)?.parameters.filter((parameter) =>
      parameter.editableLayers.includes('BUNDLE')
    ) ?? []
  const selectBundle = (item: BundleDraft) => {
    activeId.value = item.id
    Object.assign(form, JSON.parse(JSON.stringify(item)) as BundleDraft)
  }
  const newBundle = () => {
    activeId.value = ''
    Object.assign(form, { id: draftId(), bundleCode: '', version: 1, items: [] })
    form.status = undefined
    form.rowVersion = undefined
  }
  const addItem = () =>
    form.items.push({
      itemKey: '',
      productId: '',
      required: false,
      defaultSelected: false,
      values: {},
      conditionId: ''
    })
  const changeProduct = (item: BundleItemDraft) => {
    item.values = {}
  }
  const save = async () => {
    if (!codePattern.test(form.bundleCode) || !Number.isInteger(form.version) || form.version < 1) {
      ElMessage.warning('请填写合法的组合编码和正整数版本')
      return
    }
    if (
      drafts.bundles.some(
        (item) =>
          item.id !== form.id &&
          item.bundleCode === form.bundleCode &&
          item.version === form.version
      )
    ) {
      ElMessage.warning('该组合版本已存在')
      return
    }
    if (!form.items.length || form.items.length > 128) {
      ElMessage.warning('组合需要 1–128 个产品项')
      return
    }
    const keys = form.items.map((item) => item.itemKey)
    if (keys.some((key) => !codePattern.test(key)) || new Set(keys).size !== keys.length) {
      ElMessage.warning('组合项键必须合法且不能重复')
      return
    }
    if (form.items.some((item) => !productFor(item) || (item.required && !item.defaultSelected))) {
      ElMessage.warning('每项都要选择产品版本；必选项必须默认选中')
      return
    }
    if (
      form.items.some((item) =>
        Object.keys(item.values).some(
          (key) => !editableParameters(item).some((parameter) => parameter.key === key)
        )
      )
    ) {
      ElMessage.warning('组合值包含不可覆盖的参数')
      return
    }
    try {
      await drafts.saveBundle(form)
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
          ><h1 class="m-0 text-20px">产品组合</h1
          ><ElButton type="primary" @click="newBundle">新建</ElButton></div
        ><ElEmpty v-if="!drafts.bundles.length" description="暂无组合版本" /><button
          v-for="item in drafts.bundles"
          :key="item.id"
          type="button"
          class="mb-8px block w-full rounded border border-solid border-[var(--el-border-color)] p-10px text-left"
          :class="activeId === item.id ? 'bg-[var(--el-color-primary-light-9)]' : ''"
          @click="selectBundle(item)"
          ><strong>{{ item.bundleCode }}</strong
          ><span class="ml-8px text-sm">v{{ item.version }}</span
          ><div class="mt-4px text-xs opacity-70">{{ item.items.length }} 个产品项</div></button
        ></ElCard
      >
      <ElCard shadow="never"
        ><div class="mb-18px flex items-center justify-between"
          ><h2 class="m-0 text-18px">组合版本</h2
          ><div class="flex items-center gap-8px"
            ><CatalogLifecycle kind="BUNDLE" :item="form" /><ElButton
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
        ><div class="mb-18px grid gap-12px md:grid-cols-2"
          ><label
            >组合编码<ElInput
              v-model="form.bundleCode"
              :disabled="!!form.status"
              placeholder="BASIC" /></label
          ><label
            >版本<ElInputNumber
              v-model="form.version"
              :disabled="!!form.status"
              :min="1"
              :precision="0"
              class="!w-full" /></label
        ></div>
        <div class="mb-12px flex items-center justify-between"
          ><h3 class="m-0 text-16px">固定产品版本</h3
          ><ElButton :disabled="!products.length" @click="addItem">添加产品项</ElButton></div
        ><ElEmpty v-if="!products.length" description="请先发布产品版本" />
        <div
          v-for="(item, index) in form.items"
          :key="index"
          class="mb-12px rounded border border-solid border-[var(--el-border-color)] p-14px"
          ><div class="mb-10px flex items-center justify-between"
            ><div class="flex items-center gap-8px"
              ><ElTag>{{ index + 1 }}</ElTag
              ><strong>{{ item.itemKey || '新产品项' }}</strong></div
            ><ElButton link type="danger" @click="form.items.splice(index, 1)">移除</ElButton></div
          >
          <div class="grid gap-10px md:grid-cols-3"
            ><label>项键<ElInput v-model="item.itemKey" placeholder="wechat" /></label
            ><label
              >产品版本<ElSelect
                v-model="item.productId"
                class="w-full"
                @change="changeProduct(item)"
                ><ElOption
                  v-for="product in products"
                  :key="product.value"
                  :label="product.label"
                  :value="product.value" /></ElSelect></label
            ><label
              >使用条件<ElSelect
                v-model="item.conditionId"
                clearable
                placeholder="沿用产品条件"
                class="w-full"
                ><ElOption
                  v-for="condition in conditions"
                  :key="condition.value"
                  :label="condition.label"
                  :value="condition.value" /></ElSelect></label
          ></div>
          <div class="mt-10px flex gap-20px"
            ><label class="flex items-center gap-8px"
              >必选<ElSwitch
                v-model="item.required"
                @change="item.required && (item.defaultSelected = true)" /></label
            ><label class="flex items-center gap-8px"
              >默认选中<ElSwitch v-model="item.defaultSelected" :disabled="item.required" /></label
          ></div>
          <div v-if="editableParameters(item).length" class="mt-14px"
            ><h4 class="mb-8px text-sm">组合覆盖值</h4
            ><div class="grid gap-10px md:grid-cols-2"
              ><label v-for="parameter in editableParameters(item)" :key="parameter.key"
                >{{ parameter.key }} <small class="opacity-60">{{ parameter.type }}</small
                ><ElInput
                  v-model="item.values[parameter.key]"
                  :placeholder="`继承产品默认值 ${parameter.defaultValue || '（空）'}`" /></label></div
          ></div>
        </div>
      </ElCard> </div
  ></div>
</template>

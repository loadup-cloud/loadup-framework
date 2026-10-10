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
    type ConditionClause,
    type ConditionDraft,
    type ValueType
  } from './drafts'

  const drafts = useContractDrafts()
  const activeId = ref('')
  const emptyCondition = (): ConditionDraft => ({
    id: draftId(),
    code: '',
    version: 1,
    mode: 'ALL',
    clauses: []
  })
  const form = reactive<ConditionDraft>(emptyCondition())
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
        ...drafts.conditions.filter((item) => item.code === form.code).map((item) => item.version)
      ) + 1
  }
  const parameters = computed(() => [
    ...new Set(drafts.products.flatMap((product) => product.parameters.map((item) => item.key)))
  ])
  const selectCondition = (item: ConditionDraft) => {
    activeId.value = item.id
    Object.assign(form, JSON.parse(JSON.stringify(item)) as ConditionDraft)
  }
  const newCondition = () => {
    activeId.value = ''
    Object.assign(form, emptyCondition())
    form.status = undefined
    form.rowVersion = undefined
  }
  const addClause = (kind: ConditionClause['kind']) =>
    form.clauses.push({
      id: draftId(),
      kind,
      negated: false,
      field: '',
      operator: 'EQ',
      valueType: 'STRING',
      expected: [''],
      configurationKey: ''
    })
  const removeClause = (id: string) => {
    form.clauses = form.clauses.filter((item) => item.id !== id)
  }
  const valueCount = (item: ConditionClause) =>
    item.operator === 'BETWEEN' ? 2 : item.operator === 'IN' || item.operator === 'NOT_IN' ? -1 : 1
  const changeOperator = (item: ConditionClause) => {
    item.configurationKey = ''
    item.expected = valueCount(item) === 2 ? ['', ''] : ['']
  }
  const validValue = (type: ValueType, value: string) =>
    type === 'INTEGER'
      ? /^-?\d+$/.test(value)
      : type === 'DECIMAL'
        ? /^-?\d+(\.\d+)?$/.test(value)
        : type === 'BOOLEAN'
          ? value === 'true' || value === 'false'
          : value.length <= 1024
  const save = async () => {
    if (!codePattern.test(form.code) || !Number.isInteger(form.version) || form.version < 1) {
      ElMessage.warning('请填写合法的条件编码和正整数版本')
      return
    }
    if (
      drafts.conditions.some(
        (item) => item.id !== form.id && item.code === form.code && item.version === form.version
      )
    ) {
      ElMessage.warning('该条件版本已存在')
      return
    }
    if (form.mode === 'ANY' && !form.clauses.length) {
      ElMessage.warning('任一满足条件至少需要一项')
      return
    }
    for (const item of form.clauses) {
      if (!codePattern.test(item.field) || !/^(merchant|transaction)\./.test(item.field)) {
        ElMessage.warning('事实字段必须填写合法路径')
        return
      }
      if (item.kind === 'EXISTS') continue
      if (item.configurationKey) {
        if (
          !codePattern.test(item.configurationKey) ||
          ['IN', 'NOT_IN', 'BETWEEN'].includes(item.operator)
        ) {
          ElMessage.warning('配置项引用只支持单值比较')
          return
        }
      } else if (
        (valueCount(item) >= 0 && item.expected.length !== valueCount(item)) ||
        !item.expected.length ||
        item.expected.some((value) => !validValue(item.valueType, value))
      ) {
        ElMessage.warning(`${item.field} 的比较值与类型或操作符不匹配`)
        return
      }
      if (item.valueType === 'BOOLEAN' && !['EQ', 'NE', 'IN', 'NOT_IN'].includes(item.operator)) {
        ElMessage.warning('布尔值不支持大小或范围比较')
        return
      }
      if (item.operator === 'BETWEEN' && Number(item.expected[0]) > Number(item.expected[1])) {
        ElMessage.warning('范围下限不能大于上限')
        return
      }
    }
    try {
      await drafts.saveCondition(form)
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
          ><h1 class="m-0 text-20px">条件规则</h1
          ><ElButton type="primary" @click="newCondition">新建</ElButton></div
        ><ElEmpty v-if="!drafts.conditions.length" description="暂无条件版本" /><button
          v-for="item in drafts.conditions"
          :key="item.id"
          type="button"
          class="mb-8px block w-full rounded border border-solid border-[var(--el-border-color)] p-10px text-left"
          :class="activeId === item.id ? 'bg-[var(--el-color-primary-light-9)]' : ''"
          @click="selectCondition(item)"
          ><strong>{{ item.code }}</strong
          ><span class="ml-8px text-sm">v{{ item.version }}</span
          ><div class="mt-4px text-xs opacity-70"
            >{{ item.mode }} · {{ item.clauses.length }} 条</div
          ></button
        ></ElCard
      >
      <ElCard shadow="never"
        ><div class="mb-18px flex items-center justify-between"
          ><h2 class="m-0 text-18px">条件版本</h2
          ><div class="flex items-center gap-8px"
            ><CatalogLifecycle kind="CONDITION" :item="form" /><ElButton
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
            >条件编码<ElInput
              v-model="form.code"
              :disabled="!!form.status"
              placeholder="merchant.eligible" /></label
          ><label
            >版本<ElInputNumber
              v-model="form.version"
              :disabled="!!form.status"
              :min="1"
              :precision="0"
              class="!w-full" /></label
          ><label
            >分组逻辑<ElSelect v-model="form.mode" class="w-full"
              ><ElOption value="ALL" label="全部满足（AND）" /><ElOption
                value="ANY"
                label="任一满足（OR）" /></ElSelect></label
        ></div>
        <div class="mb-12px flex flex-wrap items-center justify-between gap-10px"
          ><div
            ><h3 class="m-0 text-16px">规则项</h3
            ><p class="m-0 mt-5px text-sm opacity-70"
              >字段应由未来可信事实目录约束；当前只编辑一层规则树。</p
            ></div
          ><div class="flex gap-8px"
            ><ElButton @click="addClause('EXISTS')">添加存在判断</ElButton
            ><ElButton @click="addClause('COMPARE')">添加比较</ElButton></div
          ></div
        >
        <ElEmpty v-if="!form.clauses.length" description="ALL 空规则表示始终满足" />
        <div
          v-for="(item, index) in form.clauses"
          :key="item.id"
          class="mb-12px rounded border border-solid border-[var(--el-border-color)] p-14px"
          ><div class="mb-10px flex items-center justify-between"
            ><div class="flex items-center gap-10px"
              ><ElTag>{{ index + 1 }}</ElTag
              ><strong>{{ item.kind === 'EXISTS' ? '字段存在' : '字段比较' }}</strong
              ><span>取反</span><ElSwitch v-model="item.negated" /></div
            ><ElButton link type="danger" @click="removeClause(item.id)">移除</ElButton></div
          >
          <div class="grid gap-10px md:grid-cols-3"
            ><label
              >可信事实字段<ElInput v-model="item.field" placeholder="merchant.industry" /></label
            ><template v-if="item.kind === 'COMPARE'"
              ><label
                >操作符<ElSelect
                  v-model="item.operator"
                  class="w-full"
                  @change="changeOperator(item)"
                  ><ElOption
                    v-for="operator in [
                      'EQ',
                      'NE',
                      'GT',
                      'GE',
                      'LT',
                      'LE',
                      'IN',
                      'NOT_IN',
                      'BETWEEN'
                    ]"
                    :key="operator"
                    :label="operator"
                    :value="operator" /></ElSelect></label
              ><label
                >值类型<ElSelect v-model="item.valueType" class="w-full"
                  ><ElOption value="STRING" label="字符串" /><ElOption
                    value="INTEGER"
                    label="整数" /><ElOption value="DECIMAL" label="十进制" /><ElOption
                    value="BOOLEAN"
                    label="布尔" /></ElSelect></label></template
          ></div>
          <div v-if="item.kind === 'COMPARE'" class="mt-10px grid gap-10px md:grid-cols-2"
            ><label
              >固定比较值<ElSelect
                v-if="item.operator === 'IN' || item.operator === 'NOT_IN'"
                v-model="item.expected"
                multiple
                allow-create
                filterable
                default-first-option
                :disabled="!!item.configurationKey"
                placeholder="输入后回车"
                class="w-full" /><div v-else class="flex gap-8px"
                ><ElInput
                  v-model="item.expected[0]"
                  :disabled="!!item.configurationKey"
                  placeholder="比较值 / 下限" /><ElInput
                  v-if="item.operator === 'BETWEEN'"
                  v-model="item.expected[1]"
                  placeholder="上限" /></div></label
            ><label
              >或引用最终配置字段<ElSelect
                v-model="item.configurationKey"
                clearable
                filterable
                :disabled="['IN', 'NOT_IN', 'BETWEEN'].includes(item.operator)"
                placeholder="不引用"
                class="w-full"
                ><ElOption
                  v-for="key in parameters"
                  :key="key"
                  :label="key"
                  :value="key" /></ElSelect></label
          ></div>
        </div>
      </ElCard> </div
  ></div>
</template>

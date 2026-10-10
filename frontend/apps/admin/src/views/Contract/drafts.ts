import { defineStore } from 'pinia'
import {
  bundleDefinition,
  conditionDefinition,
  productDefinition,
  publishCatalog,
  queryCatalog,
  retireCatalog,
  salesPlanDefinition,
  saveCatalog,
  type CatalogKind,
  type CatalogStatus,
  type CatalogVersion,
  type OverrideLayer,
  type ValueType
} from '@/api/contract'

export type { OverrideLayer, ValueType }
export interface CatalogDraft {
  id: string
  status?: CatalogStatus
  rowVersion?: number
}
export interface ParameterDraft {
  key: string
  type: ValueType
  required: boolean
  defaultValue: string
  minimum: string
  maximum: string
  allowedValues: string[]
  editableLayers: OverrideLayer[]
}
export interface ProductDraft extends CatalogDraft {
  productCode: string
  version: number
  capabilityCode: string
  parameters: ParameterDraft[]
  conditionId: string
  dependencies: string[]
  exclusions: string[]
}
export interface ConditionClause {
  id: string
  kind: 'EXISTS' | 'COMPARE'
  negated: boolean
  field: string
  operator: 'EQ' | 'NE' | 'GT' | 'GE' | 'LT' | 'LE' | 'IN' | 'NOT_IN' | 'BETWEEN'
  valueType: ValueType
  expected: string[]
  configurationKey: string
}
export interface ConditionDraft extends CatalogDraft {
  code: string
  version: number
  mode: 'ALL' | 'ANY'
  clauses: ConditionClause[]
}
export interface BundleItemDraft {
  itemKey: string
  productId: string
  required: boolean
  defaultSelected: boolean
  values: Record<string, string>
  conditionId: string
}
export interface BundleDraft extends CatalogDraft {
  bundleCode: string
  version: number
  items: BundleItemDraft[]
}
export interface PlanBundleDraft {
  alias: string
  bundleId: string
}
export interface MerchantPolicyDraft {
  minimum: string
  maximum: string
  allowedValues: string[]
}
export interface SalesPlanDraft extends CatalogDraft {
  planCode: string
  version: number
  bundles: PlanBundleDraft[]
  values: Record<string, Record<string, string>>
  merchantPolicies: Record<string, Record<string, MerchantPolicyDraft>>
  eligibilityConditionId: string
  usageConditionId: string
  saleStartsAt: string
  saleEndsAt: string
}
export interface ContractIntentDraft {
  id: string
  merchantId: string
  scopeKey: string
  planId: string
  selectedItems: string[]
  values: Record<string, Record<string, string>>
  effectiveFrom: string
  effectiveTo: string
}

export const draftId = () => crypto.randomUUID()
export const codePattern = /^[A-Za-z0-9][A-Za-z0-9_.-]{0,127}$/
const copy = <T>(value: T): T => JSON.parse(JSON.stringify(value)) as T
export const localDateTime = (value: string | null): string => {
  if (!value) return ''
  const date = new Date(value)
  if (!Number.isFinite(date.getTime())) return ''
  const parts = [
    date.getFullYear(),
    date.getMonth() + 1,
    date.getDate(),
    date.getHours(),
    date.getMinutes()
  ]
  return `${parts[0]}-${String(parts[1]).padStart(2, '0')}-${String(parts[2]).padStart(2, '0')}T${String(parts[3]).padStart(2, '0')}:${String(parts[4]).padStart(2, '0')}`
}

const productFrom = (record: CatalogVersion<'PRODUCT'>): ProductDraft => ({
  id: record.id,
  status: record.status,
  rowVersion: record.rowVersion,
  productCode: record.code,
  version: record.version,
  capabilityCode: record.definition.capabilityCode,
  parameters: record.definition.parameters.map((parameter) => ({
    ...parameter,
    defaultValue: parameter.defaultValue ?? '',
    minimum: parameter.minimum ?? '',
    maximum: parameter.maximum ?? ''
  })),
  conditionId: record.definition.conditionId ?? '',
  dependencies: [...record.definition.dependencies],
  exclusions: [...record.definition.exclusions]
})
const conditionFrom = (record: CatalogVersion<'CONDITION'>): ConditionDraft => ({
  id: record.id,
  status: record.status,
  rowVersion: record.rowVersion,
  code: record.code,
  version: record.version,
  mode: record.definition.mode,
  clauses: record.definition.clauses.map((clause) => ({
    ...clause,
    id: draftId(),
    configurationKey: clause.configurationKey ?? ''
  }))
})
const bundleFrom = (record: CatalogVersion<'BUNDLE'>): BundleDraft => ({
  id: record.id,
  status: record.status,
  rowVersion: record.rowVersion,
  bundleCode: record.code,
  version: record.version,
  items: record.definition.items.map((item) => ({
    ...item,
    values: { ...item.values },
    conditionId: item.conditionId ?? ''
  }))
})
const planFrom = (record: CatalogVersion<'SALES_PLAN'>): SalesPlanDraft => ({
  id: record.id,
  status: record.status,
  rowVersion: record.rowVersion,
  planCode: record.code,
  version: record.version,
  bundles: copy(record.definition.bundles),
  values: copy(record.definition.values),
  merchantPolicies: Object.fromEntries(
    Object.entries(record.definition.merchantPolicies).map(([key, fields]) => [
      key,
      Object.fromEntries(
        Object.entries(fields).map(([name, policy]) => [
          name,
          {
            minimum: policy.minimum ?? '',
            maximum: policy.maximum ?? '',
            allowedValues: [...policy.allowedValues]
          }
        ])
      )
    ])
  ),
  eligibilityConditionId: record.definition.eligibilityConditionId ?? '',
  usageConditionId: record.definition.usageConditionId ?? '',
  saleStartsAt: localDateTime(record.definition.saleStartsAt),
  saleEndsAt: localDateTime(record.definition.saleEndsAt)
})

export const useContractDrafts = defineStore('contract-catalog', {
  state: () => ({
    products: [] as ProductDraft[],
    conditions: [] as ConditionDraft[],
    bundles: [] as BundleDraft[],
    plans: [] as SalesPlanDraft[],
    loading: false
  }),
  actions: {
    async loadAll() {
      this.loading = true
      try {
        const load = async <K extends CatalogKind>(kind: K): Promise<CatalogVersion<K>[]> => {
          const results: CatalogVersion<K>[] = []
          for (let page = 1; ; page++) {
            const response = await queryCatalog({ kind, page, size: 100 })
            results.push(...response.data)
            if (results.length >= (response.pageInfo?.totalCount ?? 0) || !response.data.length) return results
          }
        }
        const [products, conditions, bundles, plans] = await Promise.all([
          load('PRODUCT'),
          load('CONDITION'),
          load('BUNDLE'),
          load('SALES_PLAN')
        ])
        this.products = products.map(productFrom)
        this.conditions = conditions.map(conditionFrom)
        this.bundles = bundles.map(bundleFrom)
        this.plans = plans.map(planFrom)
      } finally {
        this.loading = false
      }
    },
    async saveProduct(value: ProductDraft) {
      const { data } = await saveCatalog({
        id: value.status ? value.id : undefined,
        expectedRowVersion: value.rowVersion,
        kind: 'PRODUCT',
        code: value.productCode,
        version: value.version,
        definition: productDefinition(value)
      })
      const saved = productFrom(data)
      this.products = [...this.products.filter((item) => item.id !== saved.id), saved]
      Object.assign(value, copy(saved))
    },
    async saveCondition(value: ConditionDraft) {
      const { data } = await saveCatalog({
        id: value.status ? value.id : undefined,
        expectedRowVersion: value.rowVersion,
        kind: 'CONDITION',
        code: value.code,
        version: value.version,
        definition: conditionDefinition(value)
      })
      const saved = conditionFrom(data)
      this.conditions = [...this.conditions.filter((item) => item.id !== saved.id), saved]
      Object.assign(value, copy(saved))
    },
    async saveBundle(value: BundleDraft) {
      const { data } = await saveCatalog({
        id: value.status ? value.id : undefined,
        expectedRowVersion: value.rowVersion,
        kind: 'BUNDLE',
        code: value.bundleCode,
        version: value.version,
        definition: bundleDefinition(value)
      })
      const saved = bundleFrom(data)
      this.bundles = [...this.bundles.filter((item) => item.id !== saved.id), saved]
      Object.assign(value, copy(saved))
    },
    async savePlan(value: SalesPlanDraft) {
      const { data } = await saveCatalog({
        id: value.status ? value.id : undefined,
        expectedRowVersion: value.rowVersion,
        kind: 'SALES_PLAN',
        code: value.planCode,
        version: value.version,
        definition: salesPlanDefinition(value)
      })
      const saved = planFrom(data)
      this.plans = [...this.plans.filter((item) => item.id !== saved.id), saved]
      Object.assign(value, copy(saved))
    },
    async transition(kind: CatalogKind, item: CatalogDraft, action: 'publish' | 'retire') {
      if (!item.status || item.rowVersion === undefined) throw new Error('请先保存目录版本')
      const { data } = await (action === 'publish' ? publishCatalog : retireCatalog)(
        item.id,
        item.rowVersion
      )
      if (kind === 'PRODUCT')
        this.products = this.products.map((entry) =>
          entry.id === data.id ? productFrom(data as CatalogVersion<'PRODUCT'>) : entry
        )
      if (kind === 'CONDITION')
        this.conditions = this.conditions.map((entry) =>
          entry.id === data.id ? conditionFrom(data as CatalogVersion<'CONDITION'>) : entry
        )
      if (kind === 'BUNDLE')
        this.bundles = this.bundles.map((entry) =>
          entry.id === data.id ? bundleFrom(data as CatalogVersion<'BUNDLE'>) : entry
        )
      if (kind === 'SALES_PLAN')
        this.plans = this.plans.map((entry) =>
          entry.id === data.id ? planFrom(data as CatalogVersion<'SALES_PLAN'>) : entry
        )
      item.status = data.status
      item.rowVersion = data.rowVersion
    }
  }
})

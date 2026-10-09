import type {
  ProductDefinition,
  ConditionDefinition,
  BundleDefinition,
  SalesPlanDefinition,
  SignContract
} from './types'

const optional = (value: string) => (value === '' ? null : value)

// Structural inputs keep API code independent of the existing view/store implementation.
export const productDefinition = (
  draft: Omit<ProductDefinition, 'parameters'> & {
    parameters: {
      key: string
      type: ProductDefinition['parameters'][number]['type']
      required: boolean
      defaultValue: string
      minimum: string
      maximum: string
      allowedValues: string[]
      editableLayers: ProductDefinition['parameters'][number]['editableLayers']
    }[]
  }
): ProductDefinition => ({
  capabilityCode: draft.capabilityCode,
  conditionId: draft.conditionId || null,
  dependencies: [...draft.dependencies],
  exclusions: [...draft.exclusions],
  parameters: draft.parameters.map((p) => ({
    ...p,
    defaultValue: optional(p.defaultValue),
    minimum: optional(p.minimum),
    maximum: optional(p.maximum)
  }))
})
export const conditionDefinition = (draft: ConditionDefinition): ConditionDefinition => ({
  mode: draft.mode,
  clauses: draft.clauses.map((c) => ({
    kind: c.kind,
    negated: c.negated,
    field: c.field,
    operator: c.operator,
    valueType: c.valueType,
    expected: c.configurationKey ? [] : [...c.expected],
    configurationKey: c.configurationKey || null
  }))
})
export const bundleDefinition = (draft: BundleDefinition): BundleDefinition => ({
  items: draft.items.map((i) => ({
    itemKey: i.itemKey,
    productId: i.productId,
    required: i.required,
    defaultSelected: i.defaultSelected,
    values: Object.fromEntries(Object.entries(i.values).filter(([, value]) => value !== '')),
    conditionId: i.conditionId || null
  }))
})

const instant = (value: string): string => {
  if (!Number.isFinite(Date.parse(value))) {
    throw new Error('请输入有效的日期和时间')
  }
  return new Date(value).toISOString()
}

export const salesPlanDefinition = (draft: SalesPlanDefinition): SalesPlanDefinition => ({
  bundles: draft.bundles.map((b) => ({ alias: b.alias, bundleId: b.bundleId })),
  values: Object.fromEntries(
    Object.entries(draft.values).map(([key, values]) => [
      key,
      Object.fromEntries(Object.entries(values).filter(([, value]) => value !== ''))
    ])
  ),
  merchantPolicies: Object.fromEntries(
    Object.entries(draft.merchantPolicies).map(([item, fields]) => [
      item,
      Object.fromEntries(
        Object.entries(fields).map(([field, policy]) => [
          field,
          {
            minimum: policy.minimum || null,
            maximum: policy.maximum || null,
            allowedValues: [...policy.allowedValues]
          }
        ])
      )
    ])
  ),
  eligibilityConditionId: draft.eligibilityConditionId || null,
  usageConditionId: draft.usageConditionId || null,
  saleStartsAt: instant(draft.saleStartsAt),
  saleEndsAt: draft.saleEndsAt ? instant(draft.saleEndsAt) : null
})

export const signingCommand = (
  draft: {
    merchantId: string
    scopeKey: string
    planId: string
    selectedItems: string[]
    values: Record<string, Record<string, string>>
    effectiveFrom: string
    effectiveTo: string
  },
  requestKey: string
): SignContract => ({
  merchantId: draft.merchantId,
  scopeKey: draft.scopeKey,
  planVersionId: draft.planId,
  requestKey,
  selectedItems: [...draft.selectedItems],
  values: Object.fromEntries(
    Object.entries(draft.values).map(([key, values]) => [
      key,
      Object.fromEntries(Object.entries(values).filter(([, value]) => value !== ''))
    ])
  ),
  effectiveFrom: draft.effectiveFrom ? instant(draft.effectiveFrom) : null,
  effectiveTo: draft.effectiveTo ? instant(draft.effectiveTo) : null
})

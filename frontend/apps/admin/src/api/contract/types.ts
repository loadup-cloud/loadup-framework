export type CatalogKind = 'PRODUCT' | 'CONDITION' | 'BUNDLE' | 'SALES_PLAN'
export type CatalogStatus = 'DRAFT' | 'PUBLISHED' | 'RETIRED'
export type ValueType = 'STRING' | 'INTEGER' | 'DECIMAL' | 'BOOLEAN'
export type OverrideLayer = 'BUNDLE' | 'SALES_PLAN' | 'MERCHANT'
export interface ParameterDefinition {
  key: string
  type: ValueType
  required: boolean
  defaultValue: string | null
  minimum: string | null
  maximum: string | null
  allowedValues: string[]
  editableLayers: OverrideLayer[]
}
export interface ProductDefinition {
  capabilityCode: string
  parameters: ParameterDefinition[]
  conditionId: string | null
  dependencies: string[]
  exclusions: string[]
}
export interface ConditionClause {
  kind: 'EXISTS' | 'COMPARE'
  negated: boolean
  field: string
  operator: 'EQ' | 'NE' | 'GT' | 'GE' | 'LT' | 'LE' | 'IN' | 'NOT_IN' | 'BETWEEN'
  valueType: ValueType
  expected: string[]
  configurationKey: string | null
}
export interface ConditionDefinition {
  mode: 'ALL' | 'ANY'
  clauses: ConditionClause[]
}
export interface BundleItem {
  itemKey: string
  productId: string
  required: boolean
  defaultSelected: boolean
  values: Record<string, string>
  conditionId: string | null
}
export interface BundleDefinition {
  items: BundleItem[]
}
export interface MerchantPolicy {
  minimum: string | null
  maximum: string | null
  allowedValues: string[]
}
export interface SalesPlanDefinition {
  bundles: { alias: string; bundleId: string }[]
  values: Record<string, Record<string, string>>
  merchantPolicies: Record<string, Record<string, MerchantPolicy>>
  eligibilityConditionId: string | null
  usageConditionId: string | null
  saleStartsAt: string
  saleEndsAt: string | null
}
export interface DefinitionByKind {
  PRODUCT: ProductDefinition
  CONDITION: ConditionDefinition
  BUNDLE: BundleDefinition
  SALES_PLAN: SalesPlanDefinition
}
export interface CatalogVersion<K extends CatalogKind> {
  id: string
  kind: K
  code: string
  version: number
  status: CatalogStatus
  rowVersion: number
  definition: DefinitionByKind[K]
  updatedBy: string
  createdAt: string
  updatedAt: string
}
export interface CatalogSave<K extends CatalogKind> {
  id?: string
  expectedRowVersion?: number
  kind: K
  code: string
  version: number
  definition: DefinitionByKind[K]
}
export interface PageQuery {
  page: number
  size: number
}
export interface CatalogQuery<K extends CatalogKind> extends PageQuery {
  kind: K
  status?: CatalogStatus
  code?: string
}
export interface Value {
  type: ValueType
  value: string
}
export interface Configuration {
  values: Record<string, Value>
  origins: Record<
    string,
    { layer: 'PRODUCT' | OverrideLayer; sourceCode: string; sourceVersion: number }
  >
}
export interface ContractItem {
  productCode: string
  productVersion: number
  capabilityCode: string
  configuration: Configuration
}
export type ContractStatus = 'NORMAL' | 'SUSPENDED' | 'TERMINATED'
export interface MerchantContract {
  id: string
  merchantId: string
  scopeKey: string
  planVersionId: string
  status: ContractStatus
  generation: number
  createdAt: string
  updatedAt: string
  revision: number
  effectiveFrom: string
  effectiveTo: string | null
  snapshotHash: string
  items: Record<string, ContractItem>
}
export interface SignContract {
  merchantId: string
  scopeKey: string
  planVersionId: string
  requestKey: string
  selectedItems: string[]
  values: Record<string, Record<string, string>>
  effectiveFrom: string | null
  effectiveTo: string | null
}
export interface ResolveQuery {
  merchantId: string
  scopeKey: string
  itemKey: string
  transactionFacts: Record<string, Value>
}
export interface ContractDecision {
  allowed: boolean
  reason:
    | 'ALLOWED'
    | 'SUSPENDED'
    | 'TERMINATED'
    | 'NO_EFFECTIVE_REVISION'
    | 'PRODUCT_NOT_SIGNED'
    | 'CONDITION_REJECTED'
    | 'FACTS_INDETERMINATE'
  contractId: string
  revision: number | null
  snapshotHash: string | null
  configuration: Configuration | null
}

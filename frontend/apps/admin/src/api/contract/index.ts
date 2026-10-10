import request from '@/request'
import type {
  CatalogKind,
  CatalogQuery,
  CatalogSave,
  CatalogVersion,
  ContractDecision,
  ContractStatus,
  MerchantContract,
  PageQuery,
  ResolveQuery,
  SignContract
} from './types'
export * from './types'
export * from './draft-adapter'

const post = <T>(path: string, data: object) =>
  request.post<T>({ url: `/api/contract/${path}`, data })
export const saveCatalog = <K extends CatalogKind>(data: CatalogSave<K>) =>
  post<CatalogVersion<K>>('catalog/save', data)
export const queryCatalog = <K extends CatalogKind>(data: CatalogQuery<K>) =>
  post<CatalogVersion<K>[]>('catalog/page', data)
export const getCatalog = <K extends CatalogKind>(id: string) =>
  post<CatalogVersion<K>>('catalog/detail', { id })
export const publishCatalog = <K extends CatalogKind>(id: string, expectedRowVersion: number) =>
  post<CatalogVersion<K>>('catalog/publish', { id, expectedRowVersion })
export const retireCatalog = <K extends CatalogKind>(id: string, expectedRowVersion: number) =>
  post<CatalogVersion<K>>('catalog/retire', { id, expectedRowVersion })
export const previewContract = (data: SignContract) =>
  post<MerchantContract>('merchant-contracts/preview', data)
export const signContract = (data: SignContract) =>
  post<MerchantContract>('merchant-contracts/sign', data)
export const queryContracts = (data: PageQuery & { merchantId?: string }) =>
  post<MerchantContract[]>('merchant-contracts/page', data)
export const getContract = (id: string) =>
  post<MerchantContract>('merchant-contracts/detail', { id })
export const changeContractStatus = (
  id: string,
  expectedGeneration: number,
  status: ContractStatus
) => post<MerchantContract>('merchant-contracts/status', { id, expectedGeneration, status })
export const resolveContract = (data: ResolveQuery) =>
  post<ContractDecision>('runtime/resolve', data)

// Generate once when starting a signing action; reuse on transport retries.
export const newSigningRequestKey = () => crypto.randomUUID()

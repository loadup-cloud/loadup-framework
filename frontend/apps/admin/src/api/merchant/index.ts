import request from '@/request'

export type MerchantType = 'ENTERPRISE' | 'SELF_EMPLOYED' | 'INDIVIDUAL'
export type MerchantStatus = 'ACTIVE' | 'INACTIVE'
export interface MerchantInfo {
  merchantCode: string
  name: string
  shortName: string | null
  type: MerchantType
  industry: string
  country: string
  province: string | null
  city: string | null
  address: string | null
  registrationNo: string | null
  contactName: string | null
  contactPhone: string | null
  contactEmail: string | null
}
export interface Merchant extends MerchantInfo {
  id: string
  status: MerchantStatus
  rowVersion: number
  createdBy: string
  updatedBy: string
  createdAt: string
  updatedAt: string
}
export interface MerchantQuery {
  merchantCode?: string
  name?: string
  status?: MerchantStatus
  page: number
  size: number
}
const post = <T>(path: string, data: object) =>
  request.post<T>({ url: `/api/merchants/${path}`, data })
export const createMerchant = (data: MerchantInfo) => post<Merchant>('create', data)
export const updateMerchant = (data: MerchantInfo & { id: string; expectedRowVersion: number }) =>
  post<Merchant>('update', data)
export const getMerchant = (id: string) => post<Merchant>('detail', { id })
export const queryMerchants = (data: MerchantQuery) => post<Merchant[]>('page', data)
export const changeMerchantStatus = (
  id: string,
  expectedRowVersion: number,
  status: MerchantStatus
) => post<Merchant>('status', { id, expectedRowVersion, status })

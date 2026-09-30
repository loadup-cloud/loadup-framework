import request from '@/request'
import type { AnalysisTotal, UserAccessSource, WeeklyUserActivity, MonthlySales } from './types'

export const getAnalysisTotalApi = () => {
  return request.post<AnalysisTotal>({ url: '/mock/analysis/total', data: {} })
}

export const getUserAccessSourceApi = () => {
  return request.post<UserAccessSource[]>({ url: '/mock/analysis/userAccessSource', data: {} })
}

export const getWeeklyUserActivityApi = () => {
  return request.post<WeeklyUserActivity[]>({ url: '/mock/analysis/weeklyUserActivity', data: {} })
}

export const getMonthlySalesApi = () => {
  return request.post<MonthlySales[]>({ url: '/mock/analysis/monthlySales', data: {} })
}

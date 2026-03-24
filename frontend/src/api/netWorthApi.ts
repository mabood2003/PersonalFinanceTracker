import api from './axiosConfig'

export interface NetWorthSnapshot {
  date: string
  totalAssets: number
  totalLiabilities: number
  netWorth: number
}

export const netWorthApi = {
  getHistory: async (days = 90): Promise<NetWorthSnapshot[]> => {
    const { data } = await api.get('/net-worth/history', { params: { days } })
    return data
  },
}

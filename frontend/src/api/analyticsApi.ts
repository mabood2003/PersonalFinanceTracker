import api from './axiosConfig'

export interface MonthlySummary {
  year: number
  month: number
  totalIncome: number
  totalExpenses: number
  netSavings: number
  savingsRate: number
  transactionCount: number
  topCategory: { name: string; amount: number } | null
}

export interface CategoryBreakdown {
  categoryId: number
  categoryName: string
  categoryIcon: string
  categoryColor: string
  totalAmount: number
  percentage: number
  transactionCount: number
}

export interface SpendingTrend {
  year: number
  month: number
  totalIncome: number
  totalExpenses: number
}

export const analyticsApi = {
  getMonthlySummary: async (year: number, month: number): Promise<MonthlySummary> => {
    const { data } = await api.get('/analytics/monthly-summary', { params: { year, month } })
    return data
  },
  getCategoryBreakdown: async (startDate: string, endDate: string, type = 'EXPENSE'): Promise<CategoryBreakdown[]> => {
    const { data } = await api.get('/analytics/category-breakdown', { params: { startDate, endDate, type } })
    return data
  },
  getSpendingTrend: async (months = 6): Promise<SpendingTrend[]> => {
    const { data } = await api.get('/analytics/spending-trend', { params: { months } })
    return data
  },
}

import api from './axiosConfig'
import type { Category } from './transactionApi'

export interface BudgetProgress {
  id: number
  category: Category
  amountLimit: number
  amountSpent: number
  amountRemaining: number
  percentUsed: number
  period: 'MONTHLY' | 'WEEKLY'
  startDate: string
  status: 'ON_TRACK' | 'WARNING' | 'EXCEEDED'
}

export interface CreateBudgetRequest {
  categoryId: number
  amountLimit: number
  period: 'MONTHLY' | 'WEEKLY'
  startDate: string
}

export const budgetApi = {
  getAll: async (): Promise<BudgetProgress[]> => {
    const { data } = await api.get('/budgets')
    return data
  },
  create: async (req: CreateBudgetRequest): Promise<BudgetProgress> => {
    const { data } = await api.post('/budgets', req)
    return data
  },
  update: async (id: number, req: CreateBudgetRequest): Promise<BudgetProgress> => {
    const { data } = await api.put(`/budgets/${id}`, req)
    return data
  },
  delete: async (id: number): Promise<void> => {
    await api.delete(`/budgets/${id}`)
  },
}

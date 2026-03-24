import api from './axiosConfig'
import type { Category } from './transactionApi'

export type RecurringFrequency = 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'YEARLY'
export type TransactionType = 'INCOME' | 'EXPENSE' | 'TRANSFER'

export interface RecurringTransaction {
  id: number
  accountId: number
  accountName: string
  destinationAccountId?: number
  destinationAccountName?: string
  category?: Category
  type: TransactionType
  amount: number
  description?: string
  merchant?: string
  frequency: RecurringFrequency
  startDate: string
  nextRunDate: string
  endDate?: string
  lastRunDate?: string
  active: boolean
  createdAt: string
}

export interface CreateRecurringTransactionRequest {
  accountId: number
  destinationAccountId?: number
  categoryId?: number
  type: TransactionType
  amount: number
  description?: string
  merchant?: string
  frequency: RecurringFrequency
  startDate: string
  endDate?: string
  active?: boolean
}

export const recurringApi = {
  getAll: async (): Promise<RecurringTransaction[]> => {
    const { data } = await api.get('/recurring-transactions')
    return data
  },
  create: async (req: CreateRecurringTransactionRequest): Promise<RecurringTransaction> => {
    const { data } = await api.post('/recurring-transactions', req)
    return data
  },
  update: async (id: number, req: CreateRecurringTransactionRequest): Promise<RecurringTransaction> => {
    const { data } = await api.put(`/recurring-transactions/${id}`, req)
    return data
  },
  delete: async (id: number): Promise<void> => {
    await api.delete(`/recurring-transactions/${id}`)
  },
}

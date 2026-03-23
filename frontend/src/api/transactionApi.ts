import api from './axiosConfig'

export interface Category {
  id: number
  name: string
  icon: string
  color: string
  isDefault: boolean
}

export interface Transaction {
  id: number
  accountId: number
  accountName: string
  category: Category | null
  type: 'INCOME' | 'EXPENSE' | 'TRANSFER'
  transferLeg?: 'OUT' | 'IN' | null
  transferGroupId?: string | null
  amount: number
  description: string | null
  merchant: string | null
  transactionDate: string
  createdAt: string
}

export interface CreateTransactionRequest {
  accountId: number
  destinationAccountId?: number
  categoryId?: number
  type: Transaction['type']
  amount: number
  description?: string
  merchant?: string
  idempotencyKey?: string
  transactionDate: string
}

export interface TransactionFilter {
  page?: number
  size?: number
  sort?: string
  type?: string
  categoryId?: number
  accountId?: number
  startDate?: string
  endDate?: string
  minAmount?: number
  maxAmount?: number
  search?: string
}

export interface PagedResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  last: boolean
}

export const transactionApi = {
  getAll: async (filter: TransactionFilter = {}): Promise<PagedResponse<Transaction>> => {
    const { data } = await api.get('/transactions', { params: filter })
    return data
  },
  getById: async (id: number): Promise<Transaction> => {
    const { data } = await api.get(`/transactions/${id}`)
    return data
  },
  create: async (req: CreateTransactionRequest): Promise<Transaction> => {
    const { data } = await api.post('/transactions', req)
    return data
  },
  update: async (id: number, req: CreateTransactionRequest): Promise<Transaction> => {
    const { data } = await api.put(`/transactions/${id}`, req)
    return data
  },
  delete: async (id: number): Promise<void> => {
    await api.delete(`/transactions/${id}`)
  },
}

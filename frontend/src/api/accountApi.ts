import api from './axiosConfig'

export interface Account {
  id: number
  name: string
  accountType: 'CHECKING' | 'SAVINGS' | 'CREDIT_CARD' | 'CASH' | 'INVESTMENT'
  balance: number
  currency: string
  createdAt: string
}

export interface CreateAccountRequest {
  name: string
  accountType: Account['accountType']
  balance: number
  currency?: string
}

export const accountApi = {
  getAll: async (): Promise<Account[]> => {
    const { data } = await api.get('/accounts')
    return data
  },
  getById: async (id: number): Promise<Account> => {
    const { data } = await api.get(`/accounts/${id}`)
    return data
  },
  create: async (req: CreateAccountRequest): Promise<Account> => {
    const { data } = await api.post('/accounts', req)
    return data
  },
  update: async (id: number, req: CreateAccountRequest): Promise<Account> => {
    const { data } = await api.put(`/accounts/${id}`, req)
    return data
  },
  delete: async (id: number): Promise<void> => {
    await api.delete(`/accounts/${id}`)
  },
}

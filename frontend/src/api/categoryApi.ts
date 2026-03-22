import api from './axiosConfig'
import type { Category } from './transactionApi'

export interface CreateCategoryRequest {
  name: string
  icon?: string
  color?: string
}

export const categoryApi = {
  getAll: async (): Promise<Category[]> => {
    const { data } = await api.get('/categories')
    return data
  },
  create: async (req: CreateCategoryRequest): Promise<Category> => {
    const { data } = await api.post('/categories', req)
    return data
  },
  update: async (id: number, req: CreateCategoryRequest): Promise<Category> => {
    const { data } = await api.put(`/categories/${id}`, req)
    return data
  },
  delete: async (id: number): Promise<void> => {
    await api.delete(`/categories/${id}`)
  },
}

import api from './axiosConfig'

export type GoalStatus = 'IN_PROGRESS' | 'ACHIEVED' | 'ABANDONED'

export interface Goal {
  id: number
  name: string
  description?: string
  targetAmount: number
  currentAmount: number
  percentComplete: number
  amountRemaining: number
  targetDate?: string
  status: GoalStatus
  icon: string
  color: string
  createdAt: string
}

export interface CreateGoalRequest {
  name: string
  description?: string
  targetAmount: number
  currentAmount?: number
  targetDate?: string
  icon?: string
  color?: string
  status?: GoalStatus
}

export const goalApi = {
  getAll: async (): Promise<Goal[]> => {
    const { data } = await api.get('/goals')
    return data
  },
  create: async (req: CreateGoalRequest): Promise<Goal> => {
    const { data } = await api.post('/goals', req)
    return data
  },
  update: async (id: number, req: CreateGoalRequest): Promise<Goal> => {
    const { data } = await api.put(`/goals/${id}`, req)
    return data
  },
  delete: async (id: number): Promise<void> => {
    await api.delete(`/goals/${id}`)
  },
}

import api from './axiosConfig'

export interface UpdateProfileRequest {
  firstName: string
  lastName: string
  email: string
}

export interface ChangePasswordRequest {
  currentPassword: string
  newPassword: string
}

export interface UserProfile {
  id: number
  email: string
  firstName: string
  lastName: string
}

export const userApi = {
  getProfile: async (): Promise<UserProfile> => {
    const { data } = await api.get('/users/me')
    return data
  },
  updateProfile: async (req: UpdateProfileRequest): Promise<UserProfile> => {
    const { data } = await api.put('/users/me', req)
    return data
  },
  changePassword: async (req: ChangePasswordRequest): Promise<void> => {
    await api.put('/users/me/password', req)
  },
}

import axios from 'axios'

const baseUrl = 'http://localhost:8080/api/v1/auth'

export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  email: string
  password: string
  firstName: string
  lastName: string
}

export interface AuthResponse {
  token: string
  email: string
  firstName: string
  lastName: string
}

export const authApi = {
  login: async (req: LoginRequest): Promise<AuthResponse> => {
    const { data } = await axios.post(`${baseUrl}/login`, req)
    return data
  },
  register: async (req: RegisterRequest): Promise<AuthResponse> => {
    const { data } = await axios.post(`${baseUrl}/register`, req)
    return data
  },
}

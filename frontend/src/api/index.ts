import http from './http'

export interface Page<T> {
  count: number
  results: T[]
}

export interface UserInfo {
  id: number
  username: string
  name: string
  role: 'admin' | 'teacher' | 'student'
  role_display: string
  phone: string
  is_active: boolean
  date_joined?: string
}

// 认证
export const authApi = {
  login: (username: string, password: string) =>
    http.post('/auth/login', { username, password }),
  me: () => http.get<UserInfo>('/auth/me'),
  logout: (refresh: string) => http.post('/auth/logout', { refresh }),
}

// 通用 CRUD 工厂
function crud<T>(resource: string) {
  return {
    list: (params?: Record<string, unknown>) =>
      http.get<Page<T>>(`/${resource}/`, { params }),
    get: (id: number) => http.get<T>(`/${resource}/${id}/`),
    create: (data: Partial<T>) => http.post<T>(`/${resource}/`, data),
    update: (id: number, data: Partial<T>) =>
      http.patch<T>(`/${resource}/${id}/`, data),
    remove: (id: number) => http.delete(`/${resource}/${id}/`),
  }
}

export const usersApi = crud<UserInfo>('users')
export const roomsApi = crud<Record<string, unknown>>('rooms')
export const seatsApi = crud<Record<string, unknown>>('seats')
export const timeslotsApi = crud<Record<string, unknown>>('timeslots')
export const schedulesApi = crud<Record<string, unknown>>('schedules')
export const bookingsApi = {
  ...crud<Record<string, unknown>>('bookings'),
  approve: (id: number, comment = '') =>
    http.post(`/bookings/${id}/approve/`, { comment }),
  reject: (id: number, comment = '') =>
    http.post(`/bookings/${id}/reject/`, { comment }),
  cancel: (id: number) => http.post(`/bookings/${id}/cancel/`),
}

export const statsApi = {
  dashboard: () => http.get('/stats/dashboard'),
  calendar: (params: Record<string, unknown>) =>
    http.get('/calendar', { params }),
}

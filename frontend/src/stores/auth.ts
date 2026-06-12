import { defineStore } from 'pinia'
import { authApi, type UserInfo } from '@/api'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: JSON.parse(localStorage.getItem('user') || 'null') as UserInfo | null,
    access: localStorage.getItem('access') || '',
  }),
  getters: {
    isLoggedIn: (s) => !!s.access,
    role: (s) => s.user?.role || '',
    isAdmin: (s) => s.user?.role === 'admin',
    isAdminOrTeacher: (s) =>
      s.user?.role === 'admin' || s.user?.role === 'teacher',
  },
  actions: {
    async login(username: string, password: string) {
      const { data } = await authApi.login(username, password)
      localStorage.setItem('access', data.access)
      localStorage.setItem('refresh', data.refresh)
      localStorage.setItem('user', JSON.stringify(data.user))
      this.access = data.access
      this.user = data.user
      return data.user as UserInfo
    },
    async fetchMe() {
      const { data } = await authApi.me()
      this.user = data
      localStorage.setItem('user', JSON.stringify(data))
    },
    async logout() {
      const refresh = localStorage.getItem('refresh')
      try {
        if (refresh) await authApi.logout(refresh)
      } catch {
        /* ignore */
      }
      localStorage.clear()
      this.access = ''
      this.user = null
    },
  },
})

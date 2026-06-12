import axios from 'axios'
import { ElMessage } from 'element-plus'

const http = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

let isRefreshing = false

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('access')
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (resp) => resp,
  async (error) => {
    const { response, config } = error
    if (!response) {
      ElMessage.error('无法连接服务器，请检查后端是否启动')
      return Promise.reject(error)
    }

    // access 过期：尝试用 refresh 换新 token 重试一次
    if (response.status === 401 && !config._retry) {
      const refresh = localStorage.getItem('refresh')
      if (refresh && !isRefreshing) {
        isRefreshing = true
        try {
          const r = await axios.post('/api/auth/refresh', { refresh })
          localStorage.setItem('access', r.data.access)
          if (r.data.refresh) localStorage.setItem('refresh', r.data.refresh)
          isRefreshing = false
          config._retry = true
          config.headers.Authorization = `Bearer ${r.data.access}`
          return http(config)
        } catch (e) {
          isRefreshing = false
          localStorage.clear()
          window.location.href = '/login'
          return Promise.reject(e)
        }
      }
      localStorage.clear()
      window.location.href = '/login'
    }

    const detail =
      response.data?.detail ||
      response.data?.non_field_errors?.[0] ||
      (typeof response.data === 'object'
        ? Object.values(response.data)[0]
        : null)
    if (detail && response.status !== 401) {
      ElMessage.error(String(detail))
    }
    return Promise.reject(error)
  },
)

export default http

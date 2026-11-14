import axios, { AxiosError, type AxiosInstance, type InternalAxiosRequestConfig } from 'axios'
import type { Result } from './types'
import { useToastStore } from '../stores/toast'

/** token 的存取统一走这里，http 拦截器与 auth store 共用，避免循环依赖 */
export const TOKEN_KEY = 'campus_mall_token'

export function getToken(): string {
  return localStorage.getItem(TOKEN_KEY) ?? ''
}

export function setToken(token: string): void {
  if (token) {
    localStorage.setItem(TOKEN_KEY, token)
  } else {
    localStorage.removeItem(TOKEN_KEY)
  }
}

/** 401 跳登录时记录回跳地址，避免循环 import router，这里用动态引入 */
async function redirectToLogin(): Promise<void> {
  const { default: router } = await import('../router')
  const current = router.currentRoute.value
  if (current.path === '/login') return
  await router.push({ path: '/login', query: { redirect: current.fullPath } })
}

const http: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 10000
})

http.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    const body = response.data as Result<unknown>
    // 后端约定：HTTP 状态码与业务 code 一致，成功统一 200
    if (body && typeof body.code === 'number' && body.code !== 200) {
      const toast = useToastStore()
      toast.error(body.message || '操作失败')
      if (body.code === 401) {
        setToken('')
        void redirectToLogin()
      }
      return Promise.reject(new Error(body.message || '操作失败'))
    }
    // 成功：解包 Result，把 data 载荷直接交给业务层（类型断言以适配 axios 拦截器签名）
    return body.data as unknown as typeof response
  },
  (error: AxiosError<Result<unknown>>) => {
    const toast = useToastStore()
    const body = error.response?.data
    if (body && typeof body.code === 'number') {
      // 后端已返回统一错误结构（可能伴随非 200 HTTP 状态码）
      toast.error(body.message || '操作失败')
      if (body.code === 401) {
        setToken('')
        void redirectToLogin()
      }
      return Promise.reject(new Error(body.message || '操作失败'))
    }
    toast.error('网络异常，请稍后重试')
    return Promise.reject(new Error('网络异常，请稍后重试'))
  }
)

export default http

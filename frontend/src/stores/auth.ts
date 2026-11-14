import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { authApi } from '../api'
import type { LoginDTO, RegisterDTO, UserVO } from '../api/types'
import { getToken, setToken } from '../api/http'

const USER_KEY = 'campus_mall_user'

function loadUser(): UserVO | null {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw) as UserVO
  } catch {
    return null
  }
}

function saveUser(user: UserVO | null): void {
  if (user) {
    localStorage.setItem(USER_KEY, JSON.stringify(user))
  } else {
    localStorage.removeItem(USER_KEY)
  }
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(getToken())
  const user = ref<UserVO | null>(loadUser())

  const isLoggedIn = computed<boolean>(() => Boolean(token.value))
  const isAdmin = computed<boolean>(() => user.value?.role === 0)
  const displayName = computed<string>(() => user.value?.username ?? '')

  async function login(dto: LoginDTO): Promise<UserVO> {
    const vo = await authApi.login(dto)
    token.value = vo.token
    user.value = vo.user
    setToken(vo.token)
    saveUser(vo.user)
    return vo.user
  }

  async function register(dto: RegisterDTO): Promise<UserVO> {
    return authApi.register(dto)
  }

  async function logout(): Promise<void> {
    try {
      await authApi.logout()
    } finally {
      // 无论登出接口是否成功，本地登录态都清空
      token.value = ''
      user.value = null
      setToken('')
      saveUser(null)
    }
  }

  function clearLocal(): void {
    token.value = ''
    user.value = null
    setToken('')
    saveUser(null)
  }

  return { token, user, isLoggedIn, isAdmin, displayName, login, register, logout, clearLocal }
})

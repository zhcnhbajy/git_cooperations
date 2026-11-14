<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useToastStore } from '../stores/toast'

const router = useRouter()
const auth = useAuthStore()
const toast = useToastStore()

const form = reactive({ username: '', password: '', confirmPassword: '' })
const submitting = ref(false)
const formErrors = reactive({ username: '', password: '', confirmPassword: '' })

function validate(): boolean {
  const username = form.username.trim()
  formErrors.username =
    username === '' ? '请输入用户名' : username.length < 3 || username.length > 50 ? '用户名长度需在 3~50 位之间' : ''
  formErrors.password =
    form.password === '' ? '请输入密码' : form.password.length < 6 || form.password.length > 32 ? '密码长度需在 6~32 位之间' : ''
  formErrors.confirmPassword = form.confirmPassword !== form.password ? '两次输入的密码不一致' : ''
  return formErrors.username === '' && formErrors.password === '' && formErrors.confirmPassword === ''
}

async function onSubmit(): Promise<void> {
  if (!validate() || submitting.value) return
  submitting.value = true
  try {
    await auth.register({ username: form.username.trim(), password: form.password })
    toast.success('注册成功，请使用新账号登录')
    await router.push('/login')
  } catch {
    // 失败已由拦截器统一 toast（如"用户名已被注册"）
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="mx-auto max-w-md">
    <div class="card p-8">
      <h1 class="mb-6 text-center text-xl font-bold text-gray-900">注册 · 文具优选</h1>

      <form class="space-y-4" novalidate @submit.prevent="onSubmit">
        <div>
          <label class="form-label" for="reg-username">用户名</label>
          <input id="reg-username" v-model="form.username" class="form-input" type="text" placeholder="3~50 位字符" autocomplete="username" />
          <p v-if="formErrors.username" class="mt-1 text-xs text-red-500">{{ formErrors.username }}</p>
        </div>
        <div>
          <label class="form-label" for="reg-password">密码</label>
          <input id="reg-password" v-model="form.password" class="form-input" type="password" placeholder="6~32 位字符" autocomplete="new-password" />
          <p v-if="formErrors.password" class="mt-1 text-xs text-red-500">{{ formErrors.password }}</p>
        </div>
        <div>
          <label class="form-label" for="reg-confirm">确认密码</label>
          <input id="reg-confirm" v-model="form.confirmPassword" class="form-input" type="password" placeholder="再次输入密码" autocomplete="new-password" />
          <p v-if="formErrors.confirmPassword" class="mt-1 text-xs text-red-500">{{ formErrors.confirmPassword }}</p>
        </div>
        <button class="btn-primary w-full" type="submit" :disabled="submitting">
          {{ submitting ? '注册中…' : '注 册' }}
        </button>
      </form>

      <p class="mt-4 text-center text-sm text-gray-500">
        已有账号？
        <router-link to="/login" class="text-primary-600 hover:underline">去登录</router-link>
      </p>
      <p class="mt-2 text-center text-xs text-gray-400">注册仅创建普通用户账号</p>
    </div>
  </div>
</template>

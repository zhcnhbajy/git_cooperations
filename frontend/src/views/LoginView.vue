<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useToastStore } from '../stores/toast'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const toast = useToastStore()

const form = reactive({ username: '', password: '' })
const submitting = ref(false)
const formErrors = reactive({ username: '', password: '' })

function validate(): boolean {
  formErrors.username = form.username.trim() === '' ? '请输入用户名' : ''
  formErrors.password = form.password === '' ? '请输入密码' : ''
  return formErrors.username === '' && formErrors.password === ''
}

async function onSubmit(): Promise<void> {
  if (!validate() || submitting.value) return
  submitting.value = true
  try {
    const user = await auth.login({ username: form.username.trim(), password: form.password })
    toast.success(`登录成功，欢迎回来，${user.username}`)
    const redirect = route.query.redirect
    await router.push(typeof redirect === 'string' && redirect.startsWith('/') ? redirect : '/')
  } catch {
    // 失败已由拦截器统一 toast（如"用户名或密码错误"）
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="mx-auto max-w-md">
    <div class="card p-8">
      <h1 class="mb-6 text-center text-xl font-bold text-gray-900">登录 · 文具优选</h1>

      <form class="space-y-4" novalidate @submit.prevent="onSubmit">
        <div>
          <label class="form-label" for="username">用户名</label>
          <input id="username" v-model="form.username" class="form-input" type="text" placeholder="请输入用户名" autocomplete="username" />
          <p v-if="formErrors.username" class="mt-1 text-xs text-red-500">{{ formErrors.username }}</p>
        </div>
        <div>
          <label class="form-label" for="password">密码</label>
          <input id="password" v-model="form.password" class="form-input" type="password" placeholder="请输入密码" autocomplete="current-password" />
          <p v-if="formErrors.password" class="mt-1 text-xs text-red-500">{{ formErrors.password }}</p>
        </div>
        <button class="btn-primary w-full" type="submit" :disabled="submitting">
          {{ submitting ? '登录中…' : '登 录' }}
        </button>
      </form>

      <p class="mt-4 text-center text-sm text-gray-500">
        还没有账号？
        <router-link :to="{ path: '/register', query: route.query }" class="text-primary-600 hover:underline">立即注册</router-link>
      </p>
    </div>

    <div class="card mt-4 p-4 text-center text-xs text-gray-400">
      演示账号：管理员 admin / 123456 · 用户 user1 / 123456
    </div>
  </div>
</template>

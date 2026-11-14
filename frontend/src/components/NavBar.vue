<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { categoryApi } from '../api'
import type { CategoryVO } from '../api/types'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()

const categories = ref<CategoryVO[]>([])
const categoryOpen = ref(false)
const userMenuOpen = ref(false)

const isAdmin = computed(() => auth.isAdmin)

async function loadCategories(): Promise<void> {
  try {
    categories.value = await categoryApi.tree()
  } catch {
    // 分类加载失败不阻塞导航，具体错误已由拦截器统一提示
  }
}

function goCategory(id: number): void {
  categoryOpen.value = false
  void router.push({ path: '/products', query: { categoryId: String(id) } })
}

async function onLogout(): Promise<void> {
  userMenuOpen.value = false
  await auth.logout()
  await router.push('/')
}

onMounted(loadCategories)
</script>

<template>
  <header class="sticky top-0 z-40 border-b border-gray-200 bg-white">
    <div class="mx-auto flex h-14 w-full max-w-6xl items-center gap-6 px-4">
      <router-link to="/" class="flex items-center gap-2 text-lg font-bold text-primary-700">
        <svg class="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
          <path stroke-linecap="round" stroke-linejoin="round" d="M4.26 10.147a60.436 60.436 0 00-.491 6.347A48.627 48.627 0 0112 20.904a48.627 48.627 0 018.232-4.41 60.46 60.46 0 00-.491-6.347m-15.482 0a50.57 50.57 0 00-2.658-.813A59.905 59.905 0 0112 3.493a59.902 59.902 0 0110.399 5.84c-.896.248-1.783.52-2.658.814m-15.482 0A50.697 50.697 0 0112 13.489a50.702 50.702 0 017.74-3.342M6.75 15a.75.75 0 100-1.5.75.75 0 000 1.5zm0 0v-3.675A55.378 55.378 0 0112 8.443m-7.007 11.55A5.981 5.981 0 006.75 15.75v-1.5" />
        </svg>
        文具优选
      </router-link>

      <nav class="hidden items-center gap-1 text-sm md:flex">
        <router-link to="/" class="rounded-lg px-3 py-2 text-gray-700 hover:bg-gray-100">首页</router-link>

        <div class="relative">
          <button
            class="flex items-center gap-1 rounded-lg px-3 py-2 text-gray-700 hover:bg-gray-100"
            @click="categoryOpen = !categoryOpen"
          >
            全部分类
            <svg class="h-3.5 w-3.5" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M5.23 7.21a.75.75 0 011.06.02L10 11.168l3.71-3.938a.75.75 0 111.08 1.04l-4.25 4.5a.75.75 0 01-1.08 0l-4.25-4.5a.75.75 0 01.02-1.06z" clip-rule="evenodd" /></svg>
          </button>
          <div
            v-if="categoryOpen"
            class="absolute left-0 top-11 w-72 rounded-xl border border-gray-200 bg-white p-3 shadow-lg"
            @mouseleave="categoryOpen = false"
          >
            <div v-if="categories.length === 0" class="py-4 text-center text-sm text-gray-400">分类加载中…</div>
            <div v-for="cat in categories" :key="cat.id" class="mb-2 last:mb-0">
              <button class="font-medium text-gray-800 hover:text-primary-600" @click="goCategory(cat.id)">
                {{ cat.name }}
              </button>
              <div class="mt-1 flex flex-wrap gap-1 pl-3">
                <button
                  v-for="child in cat.children"
                  :key="child.id"
                  class="rounded-full bg-gray-100 px-2.5 py-1 text-xs text-gray-600 hover:bg-primary-50 hover:text-primary-700"
                  @click="goCategory(child.id)"
                >
                  {{ child.name }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </nav>

      <div class="ml-auto flex items-center gap-1 text-sm">
        <router-link v-if="!isAdmin" to="/cart" class="rounded-lg px-3 py-2 text-gray-700 hover:bg-gray-100">购物车</router-link>
        <router-link v-if="!isAdmin" to="/orders" class="rounded-lg px-3 py-2 text-gray-700 hover:bg-gray-100">我的订单</router-link>

        <template v-if="auth.isLoggedIn">
          <router-link
            v-if="isAdmin"
            to="/admin/orders"
            class="rounded-lg px-3 py-2 font-medium text-primary-700 hover:bg-primary-50"
          >
            后台管理
          </router-link>
          <div class="relative">
            <button
              class="flex items-center gap-1 rounded-lg px-3 py-2 text-gray-700 hover:bg-gray-100"
              @click="userMenuOpen = !userMenuOpen"
            >
              {{ auth.displayName }}
              <svg class="h-3.5 w-3.5" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M5.23 7.21a.75.75 0 011.06.02L10 11.168l3.71-3.938a.75.75 0 111.08 1.04l-4.25 4.5a.75.75 0 01-1.08 0l-4.25-4.5a.75.75 0 01.02-1.06z" clip-rule="evenodd" /></svg>
            </button>
            <div
              v-if="userMenuOpen"
              class="absolute right-0 top-11 w-40 rounded-xl border border-gray-200 bg-white py-1 shadow-lg"
            >
              <div class="border-b border-gray-100 px-4 py-2 text-xs text-gray-400">
                {{ isAdmin ? '管理员' : '普通用户' }}
              </div>
              <button
                v-if="!isAdmin"
                class="w-full px-4 py-2 text-left text-sm text-gray-700 hover:bg-gray-50"
                @click="userMenuOpen = false; router.push('/orders')"
              >
                我的订单
              </button>
              <button class="w-full px-4 py-2 text-left text-sm text-red-600 hover:bg-red-50" @click="onLogout">
                退出登录
              </button>
            </div>
          </div>
        </template>
        <template v-else>
          <router-link to="/login" class="btn-primary !py-1.5">登录</router-link>
          <router-link to="/register" class="btn-secondary !py-1.5">注册</router-link>
        </template>
      </div>
    </div>
  </header>
</template>

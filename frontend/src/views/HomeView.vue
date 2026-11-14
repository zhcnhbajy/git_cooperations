<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { categoryApi, productApi } from '../api'
import type { CategoryVO, ProductVO } from '../api/types'
import { formatPrice } from '../utils/format'
import AppLoading from '../components/AppLoading.vue'
import EmptyState from '../components/EmptyState.vue'
import ProductImage from '../components/ProductImage.vue'

const categories = ref<CategoryVO[]>([])
const recommends = ref<ProductVO[]>([])
const loading = ref(true)
const error = ref(false)

const recommendList = computed(() => recommends.value.slice(0, 6))

async function load(): Promise<void> {
  loading.value = true
  error.value = false
  try {
    const [cats, recs] = await Promise.all([categoryApi.tree(), productApi.recommend(6)])
    categories.value = cats
    recommends.value = recs
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div>
    <!-- 顶部横幅 -->
    <section class="card mb-6 overflow-hidden bg-gradient-to-r from-primary-600 to-blue-500 !border-0">
      <div class="px-8 py-10 text-white">
        <h1 class="mb-2 text-2xl font-bold md:text-3xl">校园文具，一站购齐</h1>
        <p class="text-sm text-primary-100">笔类本册、办公设备应有尽有 · 全场正品保障 · 满件即发</p>
      </div>
    </section>

    <AppLoading v-if="loading" />

    <div v-else-if="error" class="card flex flex-col items-center gap-3 py-12">
      <p class="text-sm text-gray-500">页面加载失败，请稍后重试</p>
      <button class="btn-primary" @click="load">重新加载</button>
    </div>

    <template v-else>
      <!-- 分类入口 -->
      <section class="mb-8">
        <h2 class="mb-3 text-lg font-bold text-gray-800">商品分类</h2>
        <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          <div v-for="cat in categories" :key="cat.id" class="card p-4 transition hover:shadow-md">
            <div class="mb-2 flex items-center justify-between">
              <span class="font-medium text-gray-800">{{ cat.name }}</span>
              <router-link
                :to="{ path: '/products', query: { categoryId: String(cat.id) } }"
                class="text-xs text-primary-600 hover:underline"
              >
                查看全部 →
              </router-link>
            </div>
            <div class="flex flex-wrap gap-1.5">
              <router-link
                v-for="child in cat.children"
                :key="child.id"
                :to="{ path: '/products', query: { categoryId: String(child.id) } }"
                class="rounded-full bg-gray-100 px-2.5 py-1 text-xs text-gray-600 transition hover:bg-primary-50 hover:text-primary-700"
              >
                {{ child.name }}
              </router-link>
            </div>
          </div>
        </div>
      </section>

      <!-- 推荐商品 -->
      <section>
        <h2 class="mb-3 text-lg font-bold text-gray-800">推荐商品</h2>
        <EmptyState v-if="recommendList.length === 0" text="暂无推荐商品" />
        <div class="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-6">
          <router-link
            v-for="product in recommendList"
            :key="product.id"
            :to="`/products/${product.id}`"
            class="card overflow-hidden transition hover:shadow-md"
          >
            <div class="aspect-square">
              <ProductImage :src="product.imageUrl" :name="product.name" />
            </div>
            <div class="p-3">
              <p class="mb-1 truncate text-sm text-gray-800" :title="product.name">{{ product.name }}</p>
              <p class="font-bold text-red-600">{{ formatPrice(product.priceCents) }}</p>
            </div>
          </router-link>
        </div>
      </section>
    </template>
  </div>
</template>

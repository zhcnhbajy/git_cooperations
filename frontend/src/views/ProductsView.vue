<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { categoryApi, productApi } from '../api'
import type { CategoryVO, PageResult, ProductVO } from '../api/types'
import { formatPrice } from '../utils/format'
import AppLoading from '../components/AppLoading.vue'
import EmptyState from '../components/EmptyState.vue'
import ProductImage from '../components/ProductImage.vue'
import AppPagination from '../components/AppPagination.vue'

const route = useRoute()
const router = useRouter()

const categories = ref<CategoryVO[]>([])
const pageData = ref<PageResult<ProductVO> | null>(null)
const loading = ref(true)
const error = ref(false)

const PAGE_SIZE = 8

const currentCategoryId = computed<number | undefined>(() => {
  const raw = route.query.categoryId
  if (typeof raw !== 'string' || raw === '') return undefined
  const num = Number(raw)
  return Number.isFinite(num) ? num : undefined
})

const currentPage = computed<number>(() => {
  const raw = route.query.page
  if (typeof raw !== 'string') return 1
  const num = Number(raw)
  return Number.isFinite(num) && num >= 1 ? num : 1
})

function navigate(categoryId: number | undefined, page = 1): void {
  const query: Record<string, string> = {}
  if (categoryId !== undefined) query.categoryId = String(categoryId)
  if (page > 1) query.page = String(page)
  void router.push({ path: '/products', query })
}

function onPageChange(page: number): void {
  navigate(currentCategoryId.value, page)
}

async function load(): Promise<void> {
  loading.value = true
  error.value = false
  try {
    const [cats, page] = await Promise.all([
      categoryApi.tree(),
      productApi.page({ categoryId: currentCategoryId.value, pageNum: currentPage.value, pageSize: PAGE_SIZE })
    ])
    categories.value = cats
    pageData.value = page
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

function isSelected(id: number): boolean {
  return currentCategoryId.value === id
}

function isChildOfSelected(cat: CategoryVO): boolean {
  return currentCategoryId.value !== undefined && cat.children.some((c) => c.id === currentCategoryId.value)
}

watch(
  () => [route.query.categoryId, route.query.page],
  () => {
    if (route.path === '/products') void load()
  }
)

onMounted(load)
</script>

<template>
  <div class="flex flex-col gap-6 lg:flex-row">
    <!-- 分类侧栏 -->
    <aside class="card h-fit w-full shrink-0 p-4 lg:w-56">
      <h3 class="mb-3 font-bold text-gray-800">商品分类</h3>
      <button
        class="mb-1 block w-full rounded-lg px-3 py-2 text-left text-sm transition"
        :class="currentCategoryId === undefined ? 'bg-primary-50 font-medium text-primary-700' : 'text-gray-700 hover:bg-gray-50'"
        @click="navigate(undefined)"
      >
        全部商品
      </button>
      <div v-for="cat in categories" :key="cat.id" class="mb-2">
        <button
          class="block w-full rounded-lg px-3 py-2 text-left text-sm transition"
          :class="isSelected(cat.id) || isChildOfSelected(cat) ? 'bg-primary-50 font-medium text-primary-700' : 'text-gray-700 hover:bg-gray-50'"
          @click="navigate(cat.id)"
        >
          {{ cat.name }}
        </button>
        <div v-if="isSelected(cat.id) || isChildOfSelected(cat)" class="ml-3 border-l border-gray-200 pl-2">
          <button
            v-for="child in cat.children"
            :key="child.id"
            class="block w-full rounded-lg px-3 py-1.5 text-left text-xs transition"
            :class="isSelected(child.id) ? 'font-medium text-primary-700' : 'text-gray-500 hover:text-gray-800'"
            @click="navigate(child.id)"
          >
            {{ child.name }}
          </button>
        </div>
      </div>
    </aside>

    <!-- 商品区 -->
    <div class="min-w-0 flex-1">
      <AppLoading v-if="loading" />
      <div v-else-if="error" class="card flex flex-col items-center gap-3 py-12">
        <p class="text-sm text-gray-500">商品加载失败，请稍后重试</p>
        <button class="btn-primary" @click="load">重新加载</button>
      </div>
      <template v-else-if="pageData">
        <EmptyState v-if="pageData.list.length === 0" text="该分类下暂无在售商品" />
        <template v-else>
          <div class="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-4">
            <router-link
              v-for="product in pageData.list"
              :key="product.id"
              :to="`/products/${product.id}`"
              class="card overflow-hidden transition hover:shadow-md"
            >
              <div class="aspect-square">
                <ProductImage :src="product.imageUrl" :name="product.name" />
              </div>
              <div class="p-3">
                <p class="mb-1 truncate text-sm text-gray-800" :title="product.name">{{ product.name }}</p>
                <p class="text-xs text-gray-400">{{ product.categoryName }}</p>
                <div class="mt-1 flex items-center justify-between">
                  <span class="font-bold text-red-600">{{ formatPrice(product.priceCents) }}</span>
                  <span class="text-xs text-gray-400">库存 {{ product.stock }}</span>
                </div>
              </div>
            </router-link>
          </div>
          <AppPagination
            :page-num="pageData.pageNum"
            :pages="pageData.pages"
            :total="pageData.total"
            @change="onPageChange"
          />
        </template>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { cartApi, productApi } from '../api'
import type { ProductDetailVO } from '../api/types'
import { useAuthStore } from '../stores/auth'
import { useToastStore } from '../stores/toast'
import { formatPrice } from '../utils/format'
import AppLoading from '../components/AppLoading.vue'
import EmptyState from '../components/EmptyState.vue'
import ProductImage from '../components/ProductImage.vue'
import StarRating from '../components/StarRating.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const toast = useToastStore()

const product = ref<ProductDetailVO | null>(null)
const loading = ref(true)
const error = ref(false)
const quantity = ref(1)
/** 数量输入框文本，支持键盘直接录入 */
const quantityText = ref('1')
const submitting = ref(false)

const productId = computed<number>(() => Number(route.params.id))

/** 缺货禁用购买按钮；管理员始终可点，由后端统一返回「管理员账号不能购买商品」 */
const stockLocked = computed<boolean>(() => (product.value?.stock ?? 0) <= 0 && !auth.isAdmin)

async function load(): Promise<void> {
  loading.value = true
  error.value = false
  try {
    product.value = await productApi.detail(productId.value)
    quantity.value = 1
    quantityText.value = '1'
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

/** 未登录统一提示并跳登录（携带回跳地址） */
function requireLogin(): boolean {
  if (auth.isLoggedIn) return true
  toast.error('未登录，请先登录后再购买')
  void router.push({ path: '/login', query: { redirect: route.fullPath } })
  return false
}

/** 数量加减：限制在 1 ~ 库存 之间 */
function stepQuantity(delta: number): void {
  applyQuantity(quantity.value + delta)
}

/** 输入框只允许数字，失焦或回车时校正到 1 ~ 库存 */
function onQuantityInput(event: Event): void {
  const input = event.target as HTMLInputElement
  const digits = input.value.replace(/\D/g, '').slice(0, 4)
  quantityText.value = digits
  input.value = digits
}

function onQuantityBlur(): void {
  applyQuantity(Number(quantityText.value), true)
}

/** 校正数量到 1 ~ 库存；warnWhenOverStock 为 true 时超出库存会弹提示 */
function applyQuantity(value: number, warnWhenOverStock = false): void {
  const max = product.value?.stock ?? 0
  let next = Number.isFinite(value) ? Math.floor(value) : 1
  if (next < 1) next = 1
  if (max > 0 && next > max) {
    if (warnWhenOverStock) {
      toast.error(`库存不足，当前仅剩 ${max} 件`)
    }
    next = max
  }
  quantity.value = next
  quantityText.value = String(next)
}

async function addToCart(): Promise<void> {
  if (!requireLogin() || !product.value || submitting.value) return
  submitting.value = true
  try {
    await cartApi.add({ productId: product.value.id, quantity: quantity.value })
    toast.success('加入购物车成功')
  } catch {
    // 失败已由拦截器统一 toast
  } finally {
    submitting.value = false
  }
}

async function buyNow(): Promise<void> {
  if (!requireLogin() || !product.value || submitting.value) return
  submitting.value = true
  try {
    const row = await cartApi.add({ productId: product.value.id, quantity: quantity.value })
    await router.push({ path: '/checkout', query: { ids: String(row.id) } })
  } catch {
    // 失败已由拦截器统一 toast
  } finally {
    submitting.value = false
  }
}

watch(productId, load)
onMounted(load)
</script>

<template>
  <div>
    <AppLoading v-if="loading" />

    <div v-else-if="error" class="card flex flex-col items-center gap-3 py-12">
      <p class="text-sm text-gray-500">商品不存在、已下架或加载失败</p>
      <router-link to="/products" class="btn-primary">去逛逛其他商品</router-link>
    </div>

    <template v-else-if="product">
      <nav class="mb-4 text-sm text-gray-400">
        <router-link to="/" class="hover:text-primary-600">首页</router-link>
        <span class="mx-1">/</span>
        <router-link :to="{ path: '/products', query: { categoryId: String(product.categoryId) } }" class="hover:text-primary-600">
          {{ product.categoryName }}
        </router-link>
        <span class="mx-1">/</span>
        <span class="text-gray-600">{{ product.name }}</span>
      </nav>

      <div class="card mb-6 flex flex-col gap-6 p-6 md:flex-row">
        <div class="aspect-square w-full shrink-0 overflow-hidden rounded-xl md:w-80">
          <ProductImage :src="product.imageUrl" :name="product.name" />
        </div>
        <div class="flex min-w-0 flex-1 flex-col">
          <h1 class="mb-2 text-xl font-bold text-gray-900">{{ product.name }}</h1>
          <p class="mb-4 text-sm text-gray-400">分类：{{ product.categoryName }}</p>
          <p class="mb-4 text-3xl font-bold text-red-600">{{ formatPrice(product.priceCents) }}</p>
          <p class="mb-4 text-sm text-gray-600">{{ product.description }}</p>
          <p class="mb-6 text-sm" :class="product.stock > 0 ? 'text-gray-500' : 'text-red-500'">
            {{ product.stock > 0 ? `库存：${product.stock} 件` : '暂时缺货' }}
          </p>

          <div class="mb-6 flex items-center gap-3">
            <span class="text-sm text-gray-600">数量</span>
            <div class="flex items-center rounded-lg border border-gray-300">
              <button
                class="px-3 py-1.5 text-gray-600 hover:bg-gray-50 disabled:opacity-40"
                :disabled="quantity <= 1"
                @click="stepQuantity(-1)"
              >
                −
              </button>
              <input
                v-model="quantityText"
                type="text"
                inputmode="numeric"
                maxlength="4"
                aria-label="购买数量"
                class="w-14 border-x border-gray-200 py-1.5 text-center text-sm outline-none focus:bg-primary-50"
                @input="onQuantityInput"
                @blur="onQuantityBlur"
                @keyup.enter="onQuantityBlur"
              />
              <button
                class="px-3 py-1.5 text-gray-600 hover:bg-gray-50 disabled:opacity-40"
                :disabled="product.stock > 0 && quantity >= product.stock"
                @click="stepQuantity(1)"
              >
                +
              </button>
            </div>
          </div>

          <div class="mt-auto flex gap-3">
            <button class="btn-primary flex-1 md:flex-none md:px-8" :disabled="stockLocked || submitting" @click="addToCart">
              加入购物车
            </button>
            <button class="btn-danger flex-1 md:flex-none md:px-8" :disabled="stockLocked || submitting" @click="buyNow">
              立即购买
            </button>
          </div>
        </div>
      </div>

      <!-- 评价区 -->
      <section class="card p-6">
        <h2 class="mb-4 font-bold text-gray-800">商品评价（{{ product.reviews.length }}）</h2>
        <EmptyState v-if="product.reviews.length === 0" text="暂无评价，快来抢沙发" />
        <ul v-else class="divide-y divide-gray-100">
          <li v-for="review in product.reviews" :key="review.id" class="py-3">
            <div class="mb-1 flex items-center gap-3">
              <span class="text-sm font-medium text-gray-800">{{ review.username }}</span>
              <StarRating :model-value="review.rating" />
              <span class="ml-auto text-xs text-gray-400">{{ review.createTime }}</span>
            </div>
            <p class="text-sm text-gray-600">{{ review.content }}</p>
          </li>
        </ul>
      </section>
    </template>
  </div>
</template>

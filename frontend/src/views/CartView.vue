<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { cartApi } from '../api'
import type { CartVO } from '../api/types'
import { useToastStore } from '../stores/toast'
import { formatPrice } from '../utils/format'
import AppLoading from '../components/AppLoading.vue'
import EmptyState from '../components/EmptyState.vue'
import ProductImage from '../components/ProductImage.vue'

const router = useRouter()
const toast = useToastStore()

const cart = ref<CartVO | null>(null)
const loading = ref(true)
const error = ref(false)
const operating = ref(false)

/** 勾选的购物车行 id（下架行不可勾选，库存不足行允许提交由后端拦截） */
const selectedIds = ref<Set<number>>(new Set())

const selectableItems = computed(() => cart.value?.items.filter((i) => i.invalidReason !== 'OFF_SHELF') ?? [])
const allSelected = computed(() => selectableItems.value.length > 0 && selectableItems.value.every((i) => selectedIds.value.has(i.id)))
const selectedItems = computed(() => cart.value?.items.filter((i) => selectedIds.value.has(i.id)) ?? [])
const selectedTotalCents = computed(() => selectedItems.value.reduce((sum, i) => sum + i.priceCents * i.quantity, 0))
const selectedQuantity = computed(() => selectedItems.value.reduce((sum, i) => sum + i.quantity, 0))

async function load(): Promise<void> {
  loading.value = true
  error.value = false
  try {
    cart.value = await cartApi.list()
    // 清掉已不存在的行，以及已下架（不可勾选）的行
    const selectableIds = new Set(
      cart.value.items.filter((i) => i.invalidReason !== 'OFF_SHELF').map((i) => i.id)
    )
    selectedIds.value = new Set([...selectedIds.value].filter((id) => selectableIds.has(id)))
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

function toggleSelect(id: number, selectable: boolean): void {
  if (!selectable) return
  const next = new Set(selectedIds.value)
  if (next.has(id)) {
    next.delete(id)
  } else {
    next.add(id)
  }
  selectedIds.value = next
}

function toggleAll(): void {
  if (allSelected.value) {
    selectedIds.value = new Set()
  } else {
    selectedIds.value = new Set(selectableItems.value.map((i) => i.id))
  }
}

async function changeQuantity(id: number, delta: number): Promise<void> {
  const item = cart.value?.items.find((i) => i.id === id)
  if (!item || operating.value) return
  const target = item.quantity + delta
  if (target < 1) return
  if (target > item.stock) {
    toast.error(`库存不足，当前库存 ${item.stock}`)
    return
  }
  operating.value = true
  try {
    await cartApi.updateQuantity(id, { quantity: target })
    await load()
  } catch {
    // 失败已由拦截器统一 toast
  } finally {
    operating.value = false
  }
}

async function removeItem(id: number): Promise<void> {
  if (operating.value) return
  operating.value = true
  try {
    await cartApi.remove(id)
    toast.success('已从购物车删除')
    await load()
  } catch {
    // 失败已由拦截器统一 toast
  } finally {
    operating.value = false
  }
}

async function clearCart(): Promise<void> {
  if (operating.value || !cart.value || cart.value.items.length === 0) return
  operating.value = true
  try {
    await cartApi.clear()
    toast.success('购物车已清空')
    selectedIds.value = new Set()
    await load()
  } catch {
    // 失败已由拦截器统一 toast
  } finally {
    operating.value = false
  }
}

function goCheckout(): void {
  if (selectedIds.value.size === 0) {
    toast.info('请先勾选要结算的商品')
    return
  }
  void router.push({ path: '/checkout', query: { ids: [...selectedIds.value].join(',') } })
}

function invalidText(reason: string | null): string {
  if (reason === 'OFF_SHELF') return '商品已下架，不可结算'
  if (reason === 'OUT_OF_STOCK') return '库存不足，不可结算'
  return ''
}

onMounted(load)
</script>

<template>
  <div>
    <h1 class="mb-4 text-xl font-bold text-gray-900">购物车</h1>

    <AppLoading v-if="loading" />

    <div v-else-if="error" class="card flex flex-col items-center gap-3 py-12">
      <p class="text-sm text-gray-500">购物车加载失败，请稍后重试</p>
      <button class="btn-primary" @click="load">重新加载</button>
    </div>

    <template v-else-if="cart">
      <EmptyState v-if="cart.items.length === 0" text="购物车还是空的，快去挑选文具吧">
        <router-link to="/products" class="btn-primary mt-2">去逛逛</router-link>
      </EmptyState>

      <template v-else>
        <div class="card divide-y divide-gray-100">
          <div v-for="item in cart.items" :key="item.id" class="flex items-center gap-4 p-4">
            <input
              type="checkbox"
              class="h-4 w-4 shrink-0 cursor-pointer accent-primary-600 disabled:cursor-not-allowed"
              :checked="selectedIds.has(item.id)"
              :disabled="item.invalidReason === 'OFF_SHELF'"
              @change="toggleSelect(item.id, item.invalidReason !== 'OFF_SHELF')"
            />
            <router-link :to="`/products/${item.productId}`" class="h-16 w-16 shrink-0 overflow-hidden rounded-lg">
              <ProductImage :src="item.imageUrl" :name="item.productName" />
            </router-link>
            <div class="min-w-0 flex-1">
              <router-link :to="`/products/${item.productId}`" class="block truncate text-sm text-gray-800 hover:text-primary-600">
                {{ item.productName }}
              </router-link>
              <p class="mt-0.5 text-xs text-gray-400">单价 {{ formatPrice(item.priceCents) }}</p>
              <p v-if="!item.available" class="mt-0.5 text-xs font-medium text-red-500">{{ invalidText(item.invalidReason) }}</p>
            </div>
            <div class="flex items-center rounded-lg border border-gray-300">
              <button
                class="px-2 py-1 text-gray-600 hover:bg-gray-50 disabled:opacity-40"
                :disabled="item.invalidReason === 'OFF_SHELF' || item.quantity <= 1 || operating"
                @click="changeQuantity(item.id, -1)"
              >
                −
              </button>
              <span class="w-10 text-center text-sm">{{ item.quantity }}</span>
              <button
                class="px-2 py-1 text-gray-600 hover:bg-gray-50 disabled:opacity-40"
                :disabled="!item.available || operating"
                @click="changeQuantity(item.id, 1)"
              >
                +
              </button>
            </div>
            <span class="w-20 shrink-0 text-right text-sm font-bold text-red-600">
              {{ formatPrice(item.priceCents * item.quantity) }}
            </span>
            <button class="shrink-0 text-sm text-gray-400 hover:text-red-500" :disabled="operating" @click="removeItem(item.id)">
              删除
            </button>
          </div>
        </div>

        <!-- 底部结算栏 -->
        <div class="card sticky bottom-4 mt-4 flex items-center gap-4 p-4">
          <label class="flex cursor-pointer items-center gap-2 text-sm text-gray-600">
            <input
              type="checkbox"
              class="h-4 w-4 cursor-pointer accent-primary-600"
              :checked="allSelected"
              :disabled="selectableItems.length === 0"
              @change="toggleAll"
            />
            全选可结算商品
          </label>
          <button class="text-sm text-gray-400 hover:text-red-500" @click="clearCart">清空购物车</button>
          <div class="ml-auto flex items-center gap-4">
            <span class="text-sm text-gray-600">
              已选 <b class="text-primary-700">{{ selectedQuantity }}</b> 件，合计
              <span class="text-lg font-bold text-red-600">{{ formatPrice(selectedTotalCents) }}</span>
            </span>
            <button class="btn-danger px-8" @click="goCheckout">去结算</button>
          </div>
        </div>
      </template>
    </template>
  </div>
</template>

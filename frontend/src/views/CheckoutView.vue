<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { cartApi, orderApi } from '../api'
import type { CartItemVO } from '../api/types'
import { useToastStore } from '../stores/toast'
import { formatPrice } from '../utils/format'
import AppLoading from '../components/AppLoading.vue'
import EmptyState from '../components/EmptyState.vue'
import ProductImage from '../components/ProductImage.vue'

const route = useRoute()
const router = useRouter()
const toast = useToastStore()

const items = ref<CartItemVO[]>([])
const loading = ref(true)
const submitting = ref(false)
const address = ref('')

/** 从 query 解析要结算的购物车行 id */
const requestedIds = computed<number[]>(() => {
  const raw = route.query.ids
  if (typeof raw !== 'string') return []
  return raw
    .split(',')
    .map((s) => Number(s))
    .filter((n) => Number.isFinite(n) && n > 0)
})

const totalCents = computed(() => items.value.reduce((sum, i) => sum + i.priceCents * i.quantity, 0))
const totalQuantity = computed(() => items.value.reduce((sum, i) => sum + i.quantity, 0))

async function load(): Promise<void> {
  loading.value = true
  try {
    const cart = await cartApi.list()
    const idSet = new Set(requestedIds.value)
    // 下架商品不可结算；库存不足的商品保留，提交时由后端返回库存不足错误
    items.value = cart.items.filter((i) => idSet.has(i.id) && i.invalidReason !== 'OFF_SHELF')
  } catch {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}

async function onSubmit(): Promise<void> {
  if (submitting.value) return
  if (address.value.trim() === '') {
    toast.error('请填写收货地址')
    return
  }
  submitting.value = true
  try {
    const order = await orderApi.create({
      cartItemIds: items.value.map((i) => i.id),
      address: address.value.trim()
    })
    toast.success(`下单成功，订单号 ${order.orderNo}`)
    await router.push(`/orders/${order.orderNo}`)
  } catch {
    // 失败（库存不足 / 商品下架 / 参数错误）已由拦截器统一 toast
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>

<template>
  <div>
    <h1 class="mb-4 text-xl font-bold text-gray-900">确认订单</h1>

    <AppLoading v-if="loading" />

    <template v-else>
      <EmptyState v-if="items.length === 0" text="没有可结算的商品，请回购物车重新勾选">
        <router-link to="/cart" class="btn-primary mt-2">返回购物车</router-link>
      </EmptyState>

      <template v-else>
        <div class="card mb-4 divide-y divide-gray-100">
          <div v-for="item in items" :key="item.id" class="flex items-center gap-4 p-4">
            <div class="h-14 w-14 shrink-0 overflow-hidden rounded-lg">
              <ProductImage :src="item.imageUrl" :name="item.productName" />
            </div>
            <div class="min-w-0 flex-1">
              <p class="truncate text-sm text-gray-800">{{ item.productName }}</p>
              <p class="mt-0.5 text-xs text-gray-400">单价 {{ formatPrice(item.priceCents) }} × {{ item.quantity }}</p>
            </div>
            <span class="text-sm font-bold text-red-600">{{ formatPrice(item.priceCents * item.quantity) }}</span>
          </div>
        </div>

        <form class="card p-6" novalidate @submit.prevent="onSubmit">
          <label class="form-label" for="address">收货地址</label>
          <textarea
            id="address"
            v-model="address"
            class="form-input min-h-20"
            placeholder="请输入收货地址，如：北京市海淀区中关村大街1号"
            maxlength="255"
          ></textarea>
          <p class="mb-4 mt-1 text-xs text-gray-400">必填，最长 255 字</p>

          <div class="flex items-center justify-between border-t border-gray-100 pt-4">
            <span class="text-sm text-gray-600">
              共 {{ totalQuantity }} 件，应付金额
              <span class="text-xl font-bold text-red-600">{{ formatPrice(totalCents) }}</span>
            </span>
            <button class="btn-danger px-10" type="submit" :disabled="submitting">
              {{ submitting ? '提交中…' : '提交订单' }}
            </button>
          </div>
        </form>
      </template>
    </template>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { orderApi, reviewApi } from '../api'
import type { OrderVO, PageResult } from '../api/types'
import { useToastStore } from '../stores/toast'
import { formatPrice } from '../utils/format'
import AppLoading from '../components/AppLoading.vue'
import EmptyState from '../components/EmptyState.vue'
import AppPagination from '../components/AppPagination.vue'

const route = useRoute()
const router = useRouter()
const toast = useToastStore()

const STATUS_TABS: Array<{ value: number | undefined; label: string }> = [
  { value: undefined, label: '全部' },
  { value: 0, label: '待支付' },
  { value: 1, label: '已支付' },
  { value: 2, label: '已发货' },
  { value: 3, label: '已完成' },
  { value: 4, label: '已取消' }
]

const pageData = ref<PageResult<OrderVO> | null>(null)
const loading = ref(true)
const error = ref(false)
const operating = ref(false)

const PAGE_SIZE = 5

const currentStatus = ref<number | undefined>(undefined)
const currentPage = ref(1)

function statusFromQuery(): void {
  const raw = route.query.status
  currentStatus.value = typeof raw === 'string' && raw !== '' && Number.isFinite(Number(raw)) ? Number(raw) : undefined
  currentPage.value = 1
}

function switchTab(status: number | undefined): void {
  const query: Record<string, string> = {}
  if (status !== undefined) query.status = String(status)
  void router.push({ path: '/orders', query })
}

function onPageChange(page: number): void {
  currentPage.value = page
  void load()
}

async function load(): Promise<void> {
  loading.value = true
  error.value = false
  try {
    pageData.value = await orderApi.myOrders({ status: currentStatus.value, pageNum: currentPage.value, pageSize: PAGE_SIZE })
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

/** 未完成订单（已支付/已发货）点「去评价」：直接请求评价接口，由后端拒绝 */
async function tryReview(order: OrderVO): Promise<void> {
  const first = order.items[0]
  if (!first || operating.value) return
  operating.value = true
  try {
    await reviewApi.create({ orderId: order.id, productId: first.productId, rating: 5, content: '默认好评' })
  } catch {
    // 失败已由拦截器统一 toast（订单未完成不能评价）
  } finally {
    operating.value = false
  }
}

async function act(order: OrderVO, action: 'pay' | 'cancel' | 'confirm'): Promise<void> {
  if (operating.value) return
  operating.value = true
  try {
    const updated = await orderApi[action](order.orderNo)
    if (action === 'pay') toast.success(`订单 ${order.orderNo} 付款成功`)
    if (action === 'cancel') toast.success(`订单 ${order.orderNo} 已取消，库存已回补`)
    if (action === 'confirm') toast.success(`订单 ${order.orderNo} 已确认收货`)
    Object.assign(order, updated)
  } catch {
    // 失败已由拦截器统一 toast（状态不允许 / 越权等）
  } finally {
    operating.value = false
  }
}

watch(() => route.query.status, () => {
  if (route.path === '/orders') {
    statusFromQuery()
    void load()
  }
})

onMounted(() => {
  statusFromQuery()
  void load()
})
</script>

<template>
  <div>
    <h1 class="mb-4 text-xl font-bold text-gray-900">我的订单</h1>

    <!-- 状态筛选 -->
    <div class="mb-4 flex flex-wrap gap-2">
      <button
        v-for="tab in STATUS_TABS"
        :key="tab.label"
        class="rounded-full px-4 py-1.5 text-sm transition"
        :class="currentStatus === tab.value ? 'bg-primary-600 text-white' : 'bg-white text-gray-600 hover:bg-gray-100 border border-gray-300'"
        @click="switchTab(tab.value)"
      >
        {{ tab.label }}
      </button>
    </div>

    <AppLoading v-if="loading" />

    <div v-else-if="error" class="card flex flex-col items-center gap-3 py-12">
      <p class="text-sm text-gray-500">订单加载失败，请稍后重试</p>
      <button class="btn-primary" @click="load">重新加载</button>
    </div>

    <template v-else-if="pageData">
      <EmptyState v-if="pageData.list.length === 0" text="暂无相关订单">
        <router-link to="/products" class="btn-primary mt-2">去下单</router-link>
      </EmptyState>

      <template v-else>
        <div class="space-y-4">
          <div v-for="order in pageData.list" :key="order.id" class="card p-4">
            <div class="mb-3 flex flex-wrap items-center gap-3 border-b border-gray-100 pb-3 text-sm">
              <span class="text-gray-400">下单时间：{{ order.createTime }}</span>
              <span class="text-gray-500">订单号：{{ order.orderNo }}</span>
              <span
                class="ml-auto rounded-full px-2.5 py-0.5 text-xs font-medium"
                :class="{
                  'bg-amber-100 text-amber-700': order.status === 0,
                  'bg-blue-100 text-blue-700': order.status === 1,
                  'bg-indigo-100 text-indigo-700': order.status === 2,
                  'bg-green-100 text-green-700': order.status === 3,
                  'bg-gray-100 text-gray-500': order.status === 4
                }"
              >
                {{ order.statusText }}
              </span>
            </div>

            <div class="flex items-center gap-3">
              <div class="min-w-0 flex-1">
                <p v-for="(item, idx) in order.items.slice(0, 2)" :key="idx" class="truncate text-sm text-gray-700">
                  {{ item.productName }} × {{ item.quantity }}
                </p>
                <p v-if="order.items.length > 2" class="text-xs text-gray-400">… 共 {{ order.items.length }} 种商品</p>
              </div>
              <span class="shrink-0 text-sm">
                合计 <b class="text-red-600">{{ formatPrice(order.totalAmountCents) }}</b>
              </span>
            </div>

            <div class="mt-3 flex flex-wrap justify-end gap-2 border-t border-gray-100 pt-3">
              <button v-if="order.status === 0" class="btn-danger !px-4 !py-1.5" :disabled="operating" @click="act(order, 'pay')">
                模拟付款
              </button>
              <button
                v-if="order.status === 0 || order.status === 3"
                class="btn-secondary !px-4 !py-1.5"
                :disabled="operating"
                @click="act(order, 'cancel')"
              >
                取消订单
              </button>
              <button
                v-if="order.status === 1 || order.status === 2"
                class="btn-secondary !px-4 !py-1.5"
                :disabled="operating"
                @click="tryReview(order)"
              >
                去评价
              </button>
              <button v-if="order.status === 2" class="btn-primary !px-4 !py-1.5" :disabled="operating" @click="act(order, 'confirm')">
                确认收货
              </button>
              <router-link :to="`/orders/${order.orderNo}`" class="btn-secondary !px-4 !py-1.5">订单详情</router-link>
            </div>
          </div>
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
</template>

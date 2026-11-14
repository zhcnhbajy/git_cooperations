<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { adminApi } from '../../api'
import type { OrderVO, PageResult } from '../../api/types'
import { useToastStore } from '../../stores/toast'
import { formatPrice } from '../../utils/format'
import AppLoading from '../../components/AppLoading.vue'
import EmptyState from '../../components/EmptyState.vue'
import AppPagination from '../../components/AppPagination.vue'

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

const PAGE_SIZE = 10

const currentStatus = ref<number | undefined>(undefined)
const currentPage = ref(1)

/* 明细弹窗 */
const detailOrder = ref<OrderVO | null>(null)
const detailLoading = ref(false)

function statusFromQuery(): void {
  const raw = route.query.status
  currentStatus.value = typeof raw === 'string' && raw !== '' && Number.isFinite(Number(raw)) ? Number(raw) : undefined
  currentPage.value = 1
}

function switchTab(status: number | undefined): void {
  const query: Record<string, string> = {}
  if (status !== undefined) query.status = String(status)
  void router.push({ path: '/admin/orders', query })
}

function onPageChange(page: number): void {
  currentPage.value = page
  void load()
}

async function load(): Promise<void> {
  loading.value = true
  error.value = false
  try {
    pageData.value = await adminApi.orders({ status: currentStatus.value, pageNum: currentPage.value, pageSize: PAGE_SIZE })
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

async function ship(order: OrderVO): Promise<void> {
  if (operating.value) return
  operating.value = true
  try {
    await adminApi.ship(order.orderNo)
    toast.success(`订单 ${order.orderNo} 已标记发货`)
    await load()
  } catch {
    // 失败已由拦截器统一 toast（状态不允许等）
  } finally {
    operating.value = false
  }
}

async function openDetail(order: OrderVO): Promise<void> {
  detailLoading.value = true
  detailOrder.value = order
  try {
    detailOrder.value = await adminApi.orderDetail(order.orderNo)
  } catch {
    detailOrder.value = null
  } finally {
    detailLoading.value = false
  }
}

function closeDetail(): void {
  detailOrder.value = null
}

watch(() => route.query.status, () => {
  if (route.path === '/admin/orders') {
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
    <div class="mb-4 flex flex-wrap items-center gap-3">
      <h1 class="text-xl font-bold text-gray-900">后台 · 订单管理</h1>
      <div class="ml-auto flex gap-2">
        <router-link to="/admin/orders" class="rounded-full bg-primary-600 px-4 py-1.5 text-sm text-white">
          订单管理
        </router-link>
        <router-link to="/admin/products" class="rounded-full border border-gray-300 bg-white px-4 py-1.5 text-sm text-gray-600 hover:bg-gray-100">
          商品管理
        </router-link>
      </div>
    </div>

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
      <EmptyState v-if="pageData.list.length === 0" text="暂无相关订单" />

      <template v-else>
        <div class="card overflow-x-auto">
          <table class="w-full min-w-[720px] text-sm">
            <thead class="bg-gray-50 text-left text-xs text-gray-500">
              <tr>
                <th class="px-4 py-3">订单号</th>
                <th class="px-4 py-3">下单时间</th>
                <th class="px-4 py-3">商品</th>
                <th class="px-4 py-3 text-right">金额</th>
                <th class="px-4 py-3 text-center">状态</th>
                <th class="px-4 py-3 text-center">操作</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-100">
              <tr v-for="order in pageData.list" :key="order.id" class="hover:bg-gray-50">
                <td class="px-4 py-3 font-mono text-xs text-gray-700">{{ order.orderNo }}</td>
                <td class="px-4 py-3 text-gray-500">{{ order.createTime }}</td>
                <td class="max-w-56 px-4 py-3">
                  <p class="truncate text-gray-700">{{ order.items[0]?.productName }}</p>
                  <p v-if="order.items.length > 1" class="text-xs text-gray-400">… 共 {{ order.items.length }} 种商品</p>
                </td>
                <td class="px-4 py-3 text-right font-bold text-red-600">{{ formatPrice(order.totalAmountCents) }}</td>
                <td class="px-4 py-3 text-center">
                  <span
                    class="rounded-full px-2.5 py-0.5 text-xs font-medium"
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
                </td>
                <td class="px-4 py-3 text-center">
                  <div class="flex justify-center gap-2">
                    <button class="text-primary-600 hover:underline" @click="openDetail(order)">明细</button>
                    <button
                      v-if="order.status === 1"
                      class="text-green-600 hover:underline"
                      :disabled="operating"
                      @click="ship(order)"
                    >
                      标记发货
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <AppPagination :page-num="pageData.pageNum" :pages="pageData.pages" :total="pageData.total" @change="onPageChange" />
      </template>
    </template>

    <!-- 订单明细弹窗 -->
    <div v-if="detailOrder" class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4" @click.self="closeDetail">
      <div class="card w-full max-w-2xl p-6">
        <div class="mb-4 flex items-center justify-between">
          <h2 class="font-bold text-gray-900">订单明细</h2>
          <button class="text-lg text-gray-400 hover:text-gray-600" @click="closeDetail">×</button>
        </div>
        <div class="mb-4 space-y-1 text-sm text-gray-600">
          <p>订单号：<span class="font-mono">{{ detailOrder.orderNo }}</span></p>
          <p>下单时间：{{ detailOrder.createTime }}</p>
          <p>收货地址：{{ detailOrder.address }}</p>
        </div>
        <table class="w-full text-sm">
          <thead class="bg-gray-50 text-left text-xs text-gray-500">
            <tr>
              <th class="px-3 py-2">商品名称（下单快照）</th>
              <th class="px-3 py-2 text-right">单价</th>
              <th class="px-3 py-2 text-right">数量</th>
              <th class="px-3 py-2 text-right">小计</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-100">
            <tr v-for="(item, idx) in detailOrder.items" :key="idx">
              <td class="px-3 py-2 text-gray-800">{{ item.productName }}</td>
              <td class="px-3 py-2 text-right text-gray-600">{{ formatPrice(item.priceCents) }}</td>
              <td class="px-3 py-2 text-right text-gray-600">{{ item.quantity }}</td>
              <td class="px-3 py-2 text-right font-medium text-red-600">{{ formatPrice(item.subtotalCents) }}</td>
            </tr>
          </tbody>
          <tfoot class="bg-gray-50">
            <tr>
              <td colspan="3" class="px-3 py-2 text-right text-gray-500">总金额</td>
              <td class="px-3 py-2 text-right font-bold text-red-600">{{ formatPrice(detailOrder.totalAmountCents) }}</td>
            </tr>
          </tfoot>
        </table>
      </div>
    </div>

    <!-- 明细加载中 -->
    <div v-if="detailLoading" class="fixed inset-0 z-40 flex items-center justify-center bg-black/20">
      <AppLoading />
    </div>
  </div>
</template>

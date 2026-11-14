<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { orderApi, reviewApi } from '../api'
import type { OrderVO, OrderItemVO } from '../api/types'
import { useToastStore } from '../stores/toast'
import { formatPrice } from '../utils/format'
import AppLoading from '../components/AppLoading.vue'
import StarRating from '../components/StarRating.vue'

const route = useRoute()
const router = useRouter()
const toast = useToastStore()

const order = ref<OrderVO | null>(null)
const loading = ref(true)
const error = ref(false)
const operating = ref(false)

const orderNo = computed(() => String(route.params.orderNo))

async function load(): Promise<void> {
  loading.value = true
  error.value = false
  try {
    order.value = await orderApi.detail(orderNo.value)
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

async function act(action: 'pay' | 'cancel' | 'confirm'): Promise<void> {
  if (!order.value || operating.value) return
  operating.value = true
  try {
    const updated = await orderApi[action](order.value.orderNo)
    if (action === 'pay') toast.success('付款成功')
    if (action === 'cancel') toast.success('订单已取消，库存已回补')
    if (action === 'confirm') toast.success('已确认收货')
    order.value = updated
  } catch {
    // 失败已由拦截器统一 toast
  } finally {
    operating.value = false
  }
}

/* ---------- 评价弹窗 ---------- */

const reviewTarget = ref<OrderItemVO | null>(null)

/** 未完成订单（已支付/已发货）点「去评价」：直接请求评价接口，由后端拒绝 */
async function tryReview(item: OrderItemVO): Promise<void> {
  if (!order.value || operating.value) return
  operating.value = true
  try {
    await reviewApi.create({
      orderId: order.value.id,
      productId: item.productId,
      rating: 5,
      content: '默认好评'
    })
  } catch {
    // 失败已由拦截器统一 toast（订单未完成不能评价）
  } finally {
    operating.value = false
  }
}
const reviewRating = ref(5)
const reviewContent = ref('')
const reviewSubmitting = ref(false)

function openReview(item: OrderItemVO): void {
  reviewTarget.value = item
  reviewRating.value = 5
  reviewContent.value = ''
}

function closeReview(): void {
  reviewTarget.value = null
}

async function submitReview(): Promise<void> {
  if (!order.value || !reviewTarget.value || reviewSubmitting.value) return
  if (reviewContent.value.trim() === '') {
    toast.error('请填写评价内容')
    return
  }
  reviewSubmitting.value = true
  try {
    await reviewApi.create({
      orderId: order.value.id,
      productId: reviewTarget.value.productId,
      rating: reviewRating.value,
      content: reviewContent.value.trim()
    })
    toast.success('评价提交成功，可在商品详情页查看')
    closeReview()
  } catch {
    // 失败已由拦截器统一 toast（重复评价返回 409）
  } finally {
    reviewSubmitting.value = false
  }
}

onMounted(load)
</script>

<template>
  <div>
    <AppLoading v-if="loading" />

    <div v-else-if="error" class="card flex flex-col items-center gap-3 py-12">
      <p class="text-sm text-gray-500">订单不存在、无权查看或加载失败</p>
      <button class="btn-primary" @click="router.push('/orders')">返回我的订单</button>
    </div>

    <template v-else-if="order">
      <div class="mb-4 flex items-center justify-between">
        <h1 class="text-xl font-bold text-gray-900">订单详情</h1>
        <button class="btn-secondary !px-4 !py-1.5" @click="router.push('/orders')">返回列表</button>
      </div>

      <!-- 状态卡片 -->
      <div class="card mb-4 p-5">
        <div class="flex flex-wrap items-center gap-3">
          <span
            class="rounded-full px-3 py-1 text-sm font-medium"
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
          <span class="text-sm text-gray-500">订单号：{{ order.orderNo }}</span>
          <span class="text-sm text-gray-400">下单时间：{{ order.createTime }}</span>
          <div class="ml-auto flex gap-2">
            <button v-if="order.status === 0" class="btn-danger !px-4 !py-1.5" :disabled="operating" @click="act('pay')">
              模拟付款
            </button>
            <button
              v-if="order.status === 0 || order.status === 3"
              class="btn-secondary !px-4 !py-1.5"
              :disabled="operating"
              @click="act('cancel')"
            >
              取消订单
            </button>
            <button v-if="order.status === 2" class="btn-primary !px-4 !py-1.5" :disabled="operating" @click="act('confirm')">
              确认收货
            </button>
          </div>
        </div>
        <p class="mt-3 text-sm text-gray-600">收货地址：{{ order.address }}</p>
      </div>

      <!-- 商品明细 -->
      <div class="card overflow-hidden">
        <table class="w-full text-sm">
          <thead class="bg-gray-50 text-left text-xs text-gray-500">
            <tr>
              <th class="px-4 py-3">商品</th>
              <th class="px-4 py-3 text-right">下单时单价</th>
              <th class="px-4 py-3 text-right">数量</th>
              <th class="px-4 py-3 text-right">小计</th>
              <th class="px-4 py-3 text-center">评价</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-100">
            <tr v-for="(item, idx) in order.items" :key="idx">
              <td class="px-4 py-3 text-gray-800">{{ item.productName }}</td>
              <td class="px-4 py-3 text-right text-gray-600">{{ formatPrice(item.priceCents) }}</td>
              <td class="px-4 py-3 text-right text-gray-600">{{ item.quantity }}</td>
              <td class="px-4 py-3 text-right font-medium text-red-600">{{ formatPrice(item.subtotalCents) }}</td>
              <td class="px-4 py-3 text-center">
                <button v-if="order.status === 3" class="text-primary-600 hover:underline" @click="openReview(item)">写评价</button>
                <button
                  v-else-if="order.status === 1 || order.status === 2"
                  class="text-primary-600 hover:underline disabled:opacity-40"
                  :disabled="operating"
                  @click="tryReview(item)"
                >
                  去评价
                </button>
                <span v-else class="text-xs text-gray-300">—</span>
              </td>
            </tr>
          </tbody>
          <tfoot class="bg-gray-50">
            <tr>
              <td colspan="3" class="px-4 py-3 text-right text-gray-500">订单总金额</td>
              <td colspan="2" class="px-4 py-3 text-right text-lg font-bold text-red-600">{{ formatPrice(order.totalAmountCents) }}</td>
            </tr>
          </tfoot>
        </table>
      </div>

      <!-- 评价弹窗 -->
      <div v-if="reviewTarget" class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4" @click.self="closeReview">
        <div class="card w-full max-w-md p-6">
          <div class="mb-4 flex items-center justify-between">
            <h2 class="font-bold text-gray-900">评价商品</h2>
            <button class="text-lg text-gray-400 hover:text-gray-600" @click="closeReview">×</button>
          </div>
          <p class="mb-2 text-sm text-gray-600">{{ reviewTarget.productName }}</p>
          <div class="mb-4">
            <span class="form-label">评分</span>
            <StarRating v-model="reviewRating" editable />
          </div>
          <label class="form-label" for="review-content">评价内容</label>
          <textarea
            id="review-content"
            v-model="reviewContent"
            class="form-input min-h-24"
            placeholder="说说这件文具的使用体验吧（1~200 字）"
            maxlength="200"
          ></textarea>
          <div class="mt-4 flex justify-end gap-2">
            <button class="btn-secondary" @click="closeReview">取消</button>
            <button class="btn-primary" :disabled="reviewSubmitting" @click="submitReview">
              {{ reviewSubmitting ? '提交中…' : '提交评价' }}
            </button>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

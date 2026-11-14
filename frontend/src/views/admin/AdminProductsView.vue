<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { adminApi, categoryApi } from '../../api'
import type { CategoryVO, PageResult, ProductVO } from '../../api/types'
import { useToastStore } from '../../stores/toast'
import { formatPrice } from '../../utils/format'
import AppLoading from '../../components/AppLoading.vue'
import EmptyState from '../../components/EmptyState.vue'
import ProductImage from '../../components/ProductImage.vue'
import AppPagination from '../../components/AppPagination.vue'

const toast = useToastStore()

/** public/img/ 目录下可用的静态商品图片（不含文件扩展名） */
const STATIC_IMAGES: readonly string[] = [
  'notebook', 'a4paper', 'folder', 'stapler', 'sticky', 'calculator', 'shredder',
  'keyboard', 'mouse', 'pen1', 'pencil', 'marker', 'printer', 'bookshelf'
]

function pickImage(name: string): void {
  form.imageUrl = `/img/${name}.jpg`
}

const categories = ref<CategoryVO[]>([])
const pageData = ref<PageResult<ProductVO> | null>(null)
const loading = ref(true)
const error = ref(false)
const operating = ref(false)

const PAGE_SIZE = 10

const filter = reactive({ categoryId: '', status: '' })
const currentPage = ref(1)

/** 编辑/新增弹窗：editingId 为 null 表示新增 */
const dialogOpen = ref(false)
const editingId = ref<number | null>(null)
const submitting = ref(false)
const form = reactive({
  name: '',
  priceYuan: '', // 以元为单位的输入，提交时转分
  stock: '',
  categoryId: '',
  imageUrl: '',
  description: ''
})
const formErrors = reactive({ name: '', priceYuan: '', stock: '', categoryId: '' })

async function load(): Promise<void> {
  loading.value = true
  error.value = false
  try {
    const [cats, page] = await Promise.all([
      categoryApi.tree(),
      adminApi.products({
        categoryId: filter.categoryId === '' ? undefined : Number(filter.categoryId),
        status: filter.status === '' ? undefined : Number(filter.status),
        pageNum: currentPage.value,
        pageSize: PAGE_SIZE
      })
    ])
    categories.value = cats
    pageData.value = page
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

function onFilterChange(): void {
  currentPage.value = 1
  void load()
}

function onPageChange(page: number): void {
  currentPage.value = page
  void load()
}

function openCreate(): void {
  editingId.value = null
  form.name = ''
  form.priceYuan = ''
  form.stock = ''
  form.categoryId = ''
  form.imageUrl = ''
  form.description = ''
  clearErrors()
  dialogOpen.value = true
}

function openEdit(product: ProductVO): void {
  editingId.value = product.id
  form.name = product.name
  form.priceYuan = (product.priceCents / 100).toFixed(2)
  form.stock = String(product.stock)
  form.categoryId = String(product.categoryId)
  form.imageUrl = product.imageUrl
  form.description = product.description
  clearErrors()
  dialogOpen.value = true
}

function clearErrors(): void {
  formErrors.name = ''
  formErrors.priceYuan = ''
  formErrors.stock = ''
  formErrors.categoryId = ''
}

function validate(): boolean {
  clearErrors()
  formErrors.name = form.name.trim() === '' ? '请输入商品名称' : ''
  const yuan = Number(form.priceYuan)
  formErrors.priceYuan =
    form.priceYuan.trim() === '' || !Number.isFinite(yuan) || yuan <= 0 ? '请输入大于 0 的价格（元）' : ''
  const stock = Number(form.stock)
  formErrors.stock =
    form.stock.trim() === '' || !Number.isInteger(stock) || stock < 0 ? '库存需为不小于 0 的整数' : ''
  formErrors.categoryId = form.categoryId === '' ? '请选择所属分类' : ''
  return formErrors.name === '' && formErrors.priceYuan === '' && formErrors.stock === '' && formErrors.categoryId === ''
}

async function onSubmit(): Promise<void> {
  if (!validate() || submitting.value) return
  submitting.value = true
  const payload = {
    name: form.name.trim(),
    priceCents: Math.round(Number(form.priceYuan) * 100),
    stock: Number(form.stock),
    categoryId: Number(form.categoryId),
    description: form.description.trim(),
    imageUrl: form.imageUrl.trim()
  }
  try {
    if (editingId.value === null) {
      await adminApi.createProduct(payload)
      toast.success(`商品「${payload.name}」新增成功`)
    } else {
      await adminApi.updateProduct(editingId.value, payload)
      toast.success(`商品「${payload.name}」修改成功`)
    }
    dialogOpen.value = false
    await load()
  } catch {
    // 失败已由拦截器统一 toast
  } finally {
    submitting.value = false
  }
}

async function toggleStatus(product: ProductVO): Promise<void> {
  if (operating.value) return
  const target = product.status === 1 ? 0 : 1
  operating.value = true
  try {
    await adminApi.updateProductStatus(product.id, target)
    toast.success(target === 1 ? `商品「${product.name}」已上架` : `商品「${product.name}」已下架`)
    await load()
  } catch {
    // 失败已由拦截器统一 toast
  } finally {
    operating.value = false
  }
}

onMounted(load)
</script>

<template>
  <div>
    <div class="mb-4 flex flex-wrap items-center gap-3">
      <h1 class="text-xl font-bold text-gray-900">后台 · 商品管理</h1>
      <div class="ml-auto flex gap-2">
        <router-link to="/admin/orders" class="rounded-full border border-gray-300 bg-white px-4 py-1.5 text-sm text-gray-600 hover:bg-gray-100">
          订单管理
        </router-link>
        <router-link to="/admin/products" class="rounded-full bg-primary-600 px-4 py-1.5 text-sm text-white">
          商品管理
        </router-link>
      </div>
      <button class="btn-primary" @click="openCreate">+ 新增商品</button>
    </div>

    <!-- 筛选栏 -->
    <div class="card mb-4 flex flex-wrap items-center gap-3 p-4">
      <label class="flex items-center gap-2 text-sm text-gray-600">
        分类
        <select v-model="filter.categoryId" class="form-input !w-44" @change="onFilterChange">
          <option value="">全部分类</option>
          <optgroup v-for="cat in categories" :key="cat.id" :label="cat.name">
            <option :value="String(cat.id)">{{ cat.name }}（全部）</option>
            <option v-for="child in cat.children" :key="child.id" :value="String(child.id)">{{ child.name }}</option>
          </optgroup>
        </select>
      </label>
      <label class="flex items-center gap-2 text-sm text-gray-600">
        状态
        <select v-model="filter.status" class="form-input !w-32" @change="onFilterChange">
          <option value="">全部状态</option>
          <option value="1">已上架</option>
          <option value="0">已下架</option>
        </select>
      </label>
    </div>

    <AppLoading v-if="loading" />

    <div v-else-if="error" class="card flex flex-col items-center gap-3 py-12">
      <p class="text-sm text-gray-500">商品加载失败，请稍后重试</p>
      <button class="btn-primary" @click="load">重新加载</button>
    </div>

    <template v-else-if="pageData">
      <EmptyState v-if="pageData.list.length === 0" text="暂无符合条件的商品" />

      <template v-else>
        <div class="card overflow-x-auto">
          <table class="w-full min-w-[760px] text-sm">
            <thead class="bg-gray-50 text-left text-xs text-gray-500">
              <tr>
                <th class="px-4 py-3">ID</th>
                <th class="px-4 py-3">商品</th>
                <th class="px-4 py-3">分类</th>
                <th class="px-4 py-3 text-right">价格</th>
                <th class="px-4 py-3 text-right">库存</th>
                <th class="px-4 py-3 text-center">状态</th>
                <th class="px-4 py-3 text-center">操作</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-100">
              <tr v-for="product in pageData.list" :key="product.id" class="hover:bg-gray-50">
                <td class="px-4 py-3 text-gray-400">{{ product.id }}</td>
                <td class="px-4 py-3">
                  <div class="flex items-center gap-3">
                    <div class="h-10 w-10 shrink-0 overflow-hidden rounded-lg">
                      <ProductImage :src="product.imageUrl" :name="product.name" />
                    </div>
                    <div class="min-w-0">
                      <p class="truncate text-gray-800">{{ product.name }}</p>
                      <p class="truncate text-xs text-gray-400">{{ product.description }}</p>
                    </div>
                  </div>
                </td>
                <td class="px-4 py-3 text-gray-600">{{ product.categoryName }}</td>
                <td class="px-4 py-3 text-right font-bold text-red-600">{{ formatPrice(product.priceCents) }}</td>
                <td class="px-4 py-3 text-right text-gray-600">{{ product.stock }}</td>
                <td class="px-4 py-3 text-center">
                  <span
                    class="rounded-full px-2.5 py-0.5 text-xs font-medium"
                    :class="product.status === 1 ? 'bg-green-100 text-green-700' : 'bg-gray-200 text-gray-500'"
                  >
                    {{ product.status === 1 ? '已上架' : '已下架' }}
                  </span>
                </td>
                <td class="px-4 py-3 text-center">
                  <div class="flex justify-center gap-3">
                    <button class="text-primary-600 hover:underline" @click="openEdit(product)">编辑</button>
                    <button
                      class="hover:underline"
                      :class="product.status === 1 ? 'text-red-500' : 'text-green-600'"
                      :disabled="operating"
                      @click="toggleStatus(product)"
                    >
                      {{ product.status === 1 ? '下架' : '上架' }}
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

    <!-- 新增 / 编辑弹窗 -->
    <div v-if="dialogOpen" class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4" @click.self="dialogOpen = false">
      <div class="card max-h-[90vh] w-full max-w-lg overflow-y-auto p-6">
        <div class="mb-4 flex items-center justify-between">
          <h2 class="font-bold text-gray-900">{{ editingId === null ? '新增商品' : '编辑商品' }}</h2>
          <button class="text-lg text-gray-400 hover:text-gray-600" @click="dialogOpen = false">×</button>
        </div>

        <form class="space-y-4" novalidate @submit.prevent="onSubmit">
          <div>
            <label class="form-label" for="p-name">商品名称</label>
            <input id="p-name" v-model="form.name" class="form-input" type="text" placeholder="如：晨光中性笔 0.5mm 黑色" />
            <p v-if="formErrors.name" class="mt-1 text-xs text-red-500">{{ formErrors.name }}</p>
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="form-label" for="p-price">价格（元）</label>
              <input id="p-price" v-model="form.priceYuan" class="form-input" type="text" inputmode="decimal" placeholder="如 2.50" />
              <p v-if="formErrors.priceYuan" class="mt-1 text-xs text-red-500">{{ formErrors.priceYuan }}</p>
            </div>
            <div>
              <label class="form-label" for="p-stock">库存</label>
              <input id="p-stock" v-model="form.stock" class="form-input" type="text" inputmode="numeric" placeholder="如 100" />
              <p v-if="formErrors.stock" class="mt-1 text-xs text-red-500">{{ formErrors.stock }}</p>
            </div>
          </div>
          <div>
            <label class="form-label" for="p-category">所属分类</label>
            <select id="p-category" v-model="form.categoryId" class="form-input">
              <option value="">请选择二级分类</option>
              <optgroup v-for="cat in categories" :key="cat.id" :label="cat.name">
                <option v-for="child in cat.children" :key="child.id" :value="String(child.id)">{{ child.name }}</option>
              </optgroup>
            </select>
            <p v-if="formErrors.categoryId" class="mt-1 text-xs text-red-500">{{ formErrors.categoryId }}</p>
          </div>
          <div>
            <label class="form-label" for="p-image">图片地址</label>
            <div class="flex items-start gap-3">
              <div class="h-20 w-20 shrink-0 overflow-hidden rounded-lg border border-gray-200 bg-gray-50">
                <ProductImage
                  v-if="form.imageUrl"
                  :src="form.imageUrl"
                  :name="form.name || '商品预览'"
                />
                <div v-else class="flex h-full w-full items-center justify-center text-xs text-gray-400">未选择</div>
              </div>
              <input id="p-image" v-model="form.imageUrl" class="form-input flex-1" type="text" placeholder="点击下方缩略图快速选择，或手动输入 /img/xxx.jpg" />
            </div>
            <p class="mt-2 text-xs text-gray-500">可点击下方图片快速填入路径：</p>
            <div class="mt-2 grid grid-cols-7 gap-2">
              <button
                v-for="name in STATIC_IMAGES"
                :key="name"
                type="button"
                class="h-14 w-full overflow-hidden rounded-md border-2 transition"
                :class="form.imageUrl === `/img/${name}.jpg` ? 'border-primary-500 ring-2 ring-primary-200' : 'border-gray-200 hover:border-gray-400'"
                :title="name"
                @click="pickImage(name)"
              >
                <img :src="`/img/${name}.jpg`" :alt="name" class="h-full w-full object-cover" />
              </button>
            </div>
          </div>
          <div>
            <label class="form-label" for="p-desc">商品描述</label>
            <textarea id="p-desc" v-model="form.description" class="form-input min-h-20" placeholder="简要介绍商品卖点" maxlength="255"></textarea>
          </div>
          <div class="flex justify-end gap-2 pt-2">
            <button class="btn-secondary" type="button" @click="dialogOpen = false">取消</button>
            <button class="btn-primary" type="submit" :disabled="submitting">
              {{ submitting ? '保存中…' : '保 存' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

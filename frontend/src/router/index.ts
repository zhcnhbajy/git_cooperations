import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useToastStore } from '../stores/toast'

const routes: RouteRecordRaw[] = [
  { path: '/', name: 'home', component: () => import('../views/HomeView.vue') },
  { path: '/products', name: 'products', component: () => import('../views/ProductsView.vue') },
  { path: '/products/:id', name: 'product-detail', component: () => import('../views/ProductDetailView.vue') },
  { path: '/login', name: 'login', component: () => import('../views/LoginView.vue') },
  { path: '/register', name: 'register', component: () => import('../views/RegisterView.vue') },
  { path: '/cart', name: 'cart', component: () => import('../views/CartView.vue'), meta: { requiresAuth: true } },
  { path: '/checkout', name: 'checkout', component: () => import('../views/CheckoutView.vue'), meta: { requiresAuth: true } },
  { path: '/orders', name: 'orders', component: () => import('../views/OrdersView.vue'), meta: { requiresAuth: true } },
  { path: '/orders/:orderNo', name: 'order-detail', component: () => import('../views/OrderDetailView.vue'), meta: { requiresAuth: true } },
  { path: '/admin/orders', name: 'admin-orders', component: () => import('../views/admin/AdminOrdersView.vue'), meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/admin/products', name: 'admin-products', component: () => import('../views/admin/AdminProductsView.vue'), meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const auth = useAuthStore()

  if (to.meta.requiresAuth && !auth.isLoggedIn) {
    useToastStore().info('请先登录')
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  if (to.meta.requiresAdmin && !auth.isAdmin) {
    useToastStore().error('无权限访问该页面，仅管理员可进入后台')
    return { path: '/' }
  }

  return true
})

export default router

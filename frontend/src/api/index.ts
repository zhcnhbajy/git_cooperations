import http from './http'
import type {
  CartAddDTO,
  CartUpdateDTO,
  CartVO,
  CategoryVO,
  LoginDTO,
  LoginVO,
  OrderCreateDTO,
  OrderVO,
  PageResult,
  ProductDetailVO,
  ProductSaveDTO,
  ProductVO,
  RegisterDTO,
  ReviewDTO,
  UserVO
} from './types'

/* ---------- 认证 A ---------- */
export const authApi = {
  register(data: RegisterDTO): Promise<UserVO> {
    return http.post('/auth/register', data)
  },
  login(data: LoginDTO): Promise<LoginVO> {
    return http.post('/auth/login', data)
  },
  logout(): Promise<null> {
    return http.post('/auth/logout')
  },
  me(): Promise<UserVO> {
    return http.get('/auth/me')
  }
}

/* ---------- 分类 B ---------- */
export const categoryApi = {
  tree(): Promise<CategoryVO[]> {
    return http.get('/categories')
  }
}

/* ---------- 商品 C ---------- */
export const productApi = {
  recommend(limit = 6): Promise<ProductVO[]> {
    return http.get('/products/recommend', { params: { limit } })
  },
  page(params: { categoryId?: number; pageNum?: number; pageSize?: number }): Promise<PageResult<ProductVO>> {
    return http.get('/products', { params })
  },
  detail(id: number): Promise<ProductDetailVO> {
    return http.get(`/products/${id}`)
  }
}

/* ---------- 购物车 D ---------- */
export const cartApi = {
  list(): Promise<CartVO> {
    return http.get('/cart')
  },
  add(data: CartAddDTO): Promise<CartVO['items'][number]> {
    return http.post('/cart', data)
  },
  updateQuantity(id: number, data: CartUpdateDTO): Promise<CartVO['items'][number]> {
    return http.put(`/cart/${id}`, data)
  },
  remove(id: number): Promise<null> {
    return http.delete(`/cart/${id}`)
  },
  clear(): Promise<null> {
    return http.delete('/cart')
  }
}

/* ---------- 订单 E ---------- */
export const orderApi = {
  create(data: OrderCreateDTO): Promise<OrderVO> {
    return http.post('/orders', data)
  },
  myOrders(params: { status?: number; pageNum?: number; pageSize?: number }): Promise<PageResult<OrderVO>> {
    return http.get('/orders', { params })
  },
  detail(orderNo: string): Promise<OrderVO> {
    return http.get(`/orders/${orderNo}`)
  },
  pay(orderNo: string): Promise<OrderVO> {
    return http.post(`/orders/${orderNo}/pay`)
  },
  cancel(orderNo: string): Promise<OrderVO> {
    return http.post(`/orders/${orderNo}/cancel`)
  },
  confirm(orderNo: string): Promise<OrderVO> {
    return http.post(`/orders/${orderNo}/confirm`)
  }
}

/* ---------- 评价 F ---------- */
export const reviewApi = {
  create(data: ReviewDTO): Promise<null> {
    return http.post('/reviews', data)
  }
}

/* ---------- 后台 G / H ---------- */
export const adminApi = {
  orders(params: { status?: number; pageNum?: number; pageSize?: number }): Promise<PageResult<OrderVO>> {
    return http.get('/admin/orders', { params })
  },
  orderDetail(orderNo: string): Promise<OrderVO> {
    return http.get(`/admin/orders/${orderNo}`)
  },
  ship(orderNo: string): Promise<null> {
    return http.put(`/admin/orders/${orderNo}/ship`)
  },
  products(params: { categoryId?: number; status?: number; pageNum?: number; pageSize?: number }): Promise<PageResult<ProductVO>> {
    return http.get('/admin/products', { params })
  },
  createProduct(data: ProductSaveDTO): Promise<{ id: number }> {
    return http.post('/admin/products', data)
  },
  updateProduct(id: number, data: ProductSaveDTO): Promise<null> {
    return http.put(`/admin/products/${id}`, data)
  },
  updateProductStatus(id: number, status: number): Promise<null> {
    return http.put(`/admin/products/${id}/status`, { status })
  }
}

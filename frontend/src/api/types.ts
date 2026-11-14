/**
 * 与后端 VO / DTO 100% 镜像的 TypeScript 类型定义。
 * 字段名与后端 JSON 输出严格对齐（见 docs/接口文档.md），禁止私自增删。
 * 金额单位一律为「分」，展示时才除以 100。
 */

/** 统一响应壳 Result<T>（由 http 拦截器解包，业务代码通常不直接使用） */
export interface Result<T> {
  code: number
  message: string
  data: T
  timestamp: number
}

/** 分页结构（PageHelper PageInfo） */
export interface PageResult<T> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
  pages: number
}

/** 用户信息 A4 */
export interface UserVO {
  id: number
  username: string
  role: number // 0 管理员 / 1 普通用户
}

/** 登录返回 A2 */
export interface LoginVO {
  token: string
  user: UserVO
}

/** 分类树节点 B1 */
export interface CategoryVO {
  id: number
  name: string
  children: CategoryVO[]
}

/** 商品（列表 / 推荐位）C1 C2；后台 H1 靠 status 区分上下架 */
export interface ProductVO {
  id: number
  name: string
  priceCents: number
  stock: number
  categoryId: number
  categoryName: string
  imageUrl: string
  description: string
  status: number
}

/** 商品评价 */
export interface ReviewVO {
  id: number
  username: string
  rating: number
  content: string
  createTime: string
}

/** 商品详情 C3 */
export interface ProductDetailVO extends ProductVO {
  status: number
  reviews: ReviewVO[]
}

/** 购物车行 D1 */
export interface CartItemVO {
  id: number
  productId: number
  productName: string
  priceCents: number
  quantity: number
  stock: number
  imageUrl: string
  available: boolean
  invalidReason: 'OFF_SHELF' | 'OUT_OF_STOCK' | null
}

/** 购物车 D1 */
export interface CartVO {
  items: CartItemVO[]
  totalAmountCents: number
  totalQuantity: number
}

/** 订单明细 E3 */
export interface OrderItemVO {
  productId: number
  productName: string
  priceCents: number
  quantity: number
  subtotalCents: number
}

/** 订单 E2 E3 E4 E5 E6 */
export interface OrderVO {
  id: number
  orderNo: string
  totalAmountCents: number
  status: number // 0待支付 1已支付 2已发货 3已完成 4已取消
  statusText: string
  address: string
  createTime: string
  items: OrderItemVO[]
}

/* ---------- 请求 DTO ---------- */

export interface RegisterDTO {
  username: string
  password: string
}

export interface LoginDTO {
  username: string
  password: string
}

export interface CartAddDTO {
  productId: number
  quantity: number
}

export interface CartUpdateDTO {
  quantity: number
}

export interface OrderCreateDTO {
  cartItemIds: number[]
  address: string
}

export interface ReviewDTO {
  orderId: number
  productId: number
  rating: number
  content: string
}

export interface ProductSaveDTO {
  name: string
  priceCents: number
  stock: number
  categoryId: number
  description: string
  imageUrl: string
  status?: number
}

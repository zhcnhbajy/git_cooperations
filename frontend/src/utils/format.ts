/** 金额分 → 元展示，如 250 → "¥2.50" */
export function formatPrice(cents: number): string {
  const yuan = Math.floor(cents / 100)
  const fen = cents % 100
  return `¥${yuan}.${String(fen).padStart(2, '0')}`
}

/** 商品图缺失时的占位用首字 */
export function firstChar(name: string): string {
  return name.slice(0, 1) || '商'
}

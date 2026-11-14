import { defineStore } from 'pinia'
import { ref } from 'vue'

export type ToastType = 'success' | 'error' | 'info'

export interface ToastItem {
  id: number
  type: ToastType
  message: string
}

let nextId = 1

/** 全局消息提示：成功 / 失败 / 提示 三种反馈 */
export const useToastStore = defineStore('toast', () => {
  const list = ref<ToastItem[]>([])

  function push(type: ToastType, message: string, duration = 3000): void {
    const id = nextId++
    list.value.push({ id, type, message })
    window.setTimeout(() => {
      remove(id)
    }, duration)
  }

  function remove(id: number): void {
    list.value = list.value.filter((t) => t.id !== id)
  }

  return {
    list,
    remove,
    success: (msg: string) => push('success', msg),
    error: (msg: string) => push('error', msg, 4000),
    info: (msg: string) => push('info', msg)
  }
})

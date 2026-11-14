<script setup lang="ts">
import { useToastStore } from '../stores/toast'

const toastStore = useToastStore()

const typeClass: Record<string, string> = {
  success: 'border-green-200 bg-green-50 text-green-800',
  error: 'border-red-200 bg-red-50 text-red-800',
  info: 'border-blue-200 bg-blue-50 text-blue-800'
}

const typeIcon: Record<string, string> = {
  success: '✓',
  error: '✕',
  info: 'ℹ'
}
</script>

<template>
  <div class="pointer-events-none fixed inset-x-0 top-4 z-50 flex flex-col items-center gap-2 px-4">
    <transition-group
      enter-active-class="transition duration-200 ease-out"
      enter-from-class="-translate-y-3 opacity-0"
      enter-to-class="translate-y-0 opacity-100"
      leave-active-class="transition duration-150 ease-in"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div
        v-for="item in toastStore.list"
        :key="item.id"
        class="pointer-events-auto flex w-full max-w-sm items-center gap-2 rounded-lg border px-4 py-2.5 shadow-md"
        :class="typeClass[item.type]"
        role="status"
      >
        <span class="text-base font-bold">{{ typeIcon[item.type] }}</span>
        <span class="text-sm">{{ item.message }}</span>
        <button class="ml-auto text-sm opacity-60 hover:opacity-100" @click="toastStore.remove(item.id)">×</button>
      </div>
    </transition-group>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{ pageNum: number; pages: number; total: number }>()

const emit = defineEmits<{ (e: 'change', page: number): void }>()

const pageList = computed<number[]>(() => {
  const result: number[] = []
  const start = Math.max(1, props.pageNum - 2)
  const end = Math.min(props.pages, start + 4)
  for (let i = start; i <= end; i++) result.push(i)
  return result
})
</script>

<template>
  <div v-if="pages > 1" class="flex items-center justify-center gap-2 py-4">
    <button class="btn-secondary !px-3 !py-1.5" :disabled="pageNum <= 1" @click="emit('change', pageNum - 1)">
      上一页
    </button>
    <button
      v-for="page in pageList"
      :key="page"
      class="rounded-lg px-3 py-1.5 text-sm transition"
      :class="page === pageNum ? 'bg-primary-600 text-white' : 'bg-white text-gray-700 hover:bg-gray-100 border border-gray-300'"
      @click="emit('change', page)"
    >
      {{ page }}
    </button>
    <button class="btn-secondary !px-3 !py-1.5" :disabled="pageNum >= pages" @click="emit('change', pageNum + 1)">
      下一页
    </button>
    <span class="ml-2 text-sm text-gray-400">共 {{ total }} 件</span>
  </div>
</template>

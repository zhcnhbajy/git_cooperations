<script setup lang="ts">
withDefaults(defineProps<{ text?: string }>(), { text: '暂无数据' })
</script>

<template>
  <div class="flex flex-col items-center justify-center gap-2 py-16 text-gray-400">
    <svg class="h-12 w-12 text-gray-300" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
      <path
        stroke-linecap="round"
        stroke-linejoin="round"
        d="M20.25 7.5l-.625 10.632a2.25 2.25 0 01-2.247 2.118H6.622a2.25 2.25 0 01-2.247-2.118L3.75 7.5m8.25 3v6.75m0 0l-3-3m3 3l3-3M3.375 7.5h17.25c.621 0 1.125-.504 1.125-1.125v-1.5c0-.621-.504-1.125-1.125-1.125H3.375c-.621 0-1.125.504-1.125 1.125v1.5c0 .621.504 1.125 1.125 1.125z"
      />
    </svg>
    <span class="text-sm">{{ text }}</span>
    <slot></slot>
  </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{ text?: string }>(), { text: '加载中…' })
</script>

<template>
  <div class="flex flex-col items-center justify-center gap-3 py-16 text-gray-400">
    <span
      class="inline-block h-8 w-8 animate-spin rounded-full border-4 border-primary-200 border-t-primary-600"
      aria-hidden="true"
    ></span>
    <span class="text-sm">{{ text }}</span>
  </div>
</template>

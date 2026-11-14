<script setup lang="ts">
import { ref } from 'vue'
import { firstChar } from '../utils/format'

const props = defineProps<{ src: string; name: string }>()

const failed = ref(false)

function onError(): void {
  failed.value = true
}
</script>

<template>
  <div class="relative h-full w-full overflow-hidden bg-gray-100">
    <img
      v-if="!failed && props.src"
      :src="props.src"
      :alt="props.name"
      class="h-full w-full object-cover"
      loading="lazy"
      @error="onError"
    />
    <div v-else class="flex h-full w-full items-center justify-center bg-gradient-to-br from-primary-50 to-blue-100">
      <span class="select-none text-3xl font-bold text-primary-300">{{ firstChar(props.name) }}</span>
    </div>
  </div>
</template>

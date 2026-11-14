<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{ modelValue?: number; editable?: boolean }>(),
  { modelValue: 0, editable: false }
)

const emit = defineEmits<{ (e: 'update:modelValue', value: number): void }>()

const stars = computed(() => [1, 2, 3, 4, 5])

function onClick(value: number): void {
  if (props.editable) {
    emit('update:modelValue', value)
  }
}
</script>

<template>
  <div class="flex items-center gap-0.5" :class="editable ? 'cursor-pointer' : ''">
    <span
      v-for="star in stars"
      :key="star"
      class="text-lg leading-none transition"
      :class="star <= modelValue ? 'text-amber-400' : 'text-gray-300'"
      @click="onClick(star)"
      :aria-label="`${star} 星`"
    >★</span>
  </div>
</template>

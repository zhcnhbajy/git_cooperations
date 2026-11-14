<script setup lang="ts">
import NavBar from './components/NavBar.vue'
import ToastHost from './components/ToastHost.vue'
</script>

<template>
  <div class="flex min-h-screen flex-col">
    <NavBar />
    <main class="mx-auto w-full max-w-6xl flex-1 px-4 py-6">
      <router-view />
    </main>
    <footer class="border-t border-gray-200 bg-white py-4 text-center text-sm text-gray-400">
      文具优选 · 校园文具商城 — Vue 3 + Spring Boot 课程作业演示
    </footer>
    <ToastHost />
  </div>
</template>

<script setup>
import { RouterView, useRoute } from 'vue-router'
import { computed } from 'vue'
import AppFooter from './components/AppFooter.vue'

const route = useRoute()
const showFooter = computed(() => route.name !== 'Dashboard' && route.name !== 'Landing')
</script>

<template>
  <div class="flex flex-col min-h-screen">
    <div class="flex-1">
      <RouterView />
    </div>
    <AppFooter v-if="showFooter" />
  </div>
</template>

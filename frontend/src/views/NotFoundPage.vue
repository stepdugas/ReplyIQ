<script setup>
</script>

<template>
  <div class="min-h-screen bg-[#0f1320] flex items-center justify-center px-6">
    <div class="text-center">
      <div class="text-6xl font-bold text-emerald-400 mb-4">404</div>
      <h1 class="text-2xl font-bold text-white mb-2">Page not found</h1>
      <p class="text-gray-400 mb-8 max-w-sm mx-auto">The page you're looking for doesn't exist or has been moved.</p>
      <router-link
        to="/"
        class="inline-block px-6 py-2.5 bg-emerald-500 hover:bg-emerald-600 text-white font-medium text-sm rounded-lg transition-colors focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:ring-offset-2 focus:ring-offset-[#0f1320]"
      >
        Back to Homepage
      </router-link>
    </div>
  </div>
</template>

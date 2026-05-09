<script setup>
</script>

<template>
  <footer class="bg-[#0b0e18] border-t border-[#2a3040] py-5 px-6">
    <div class="max-w-5xl mx-auto flex flex-col sm:flex-row items-center justify-between gap-3 text-xs text-gray-500">
      <span>&copy; 2026 Erie Apps LLC</span>
      <div class="flex items-center gap-4">
        <router-link to="/terms" class="hover:text-gray-300 transition-colors">Terms of Service</router-link>
        <router-link to="/privacy" class="hover:text-gray-300 transition-colors">Privacy Policy</router-link>
      </div>
    </div>
  </footer>
</template>

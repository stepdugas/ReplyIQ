<script setup>
import { ref } from 'vue'
import api from '../api'

const email = ref('')
const sent = ref(false)
const error = ref('')
const loading = ref(false)

async function submit() {
  error.value = ''
  loading.value = true
  try {
    await api.post('/auth/forgot-password', { email: email.value })
    sent.value = true
  } catch (e) {
    error.value = e.response?.data?.error || 'Something went wrong'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen bg-[#0f1320] flex items-center justify-center px-6">
    <div class="w-full max-w-sm">
      <!-- Logo -->
      <div class="flex items-center gap-2.5 mb-8 justify-center">
        <div class="w-10 h-10 bg-gradient-to-br from-emerald-500 to-teal-600 rounded-xl flex items-center justify-center">
          <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
          </svg>
        </div>
        <span class="text-white font-bold text-xl">ReplyIQ</span>
      </div>

      <!-- Success State -->
      <div v-if="sent" class="text-center">
        <div class="w-14 h-14 mx-auto mb-5 bg-emerald-500/15 rounded-full flex items-center justify-center">
          <svg class="w-7 h-7 text-emerald-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
          </svg>
        </div>
        <h2 class="text-xl font-bold text-white mb-2">Check your email</h2>
        <p class="text-gray-400 text-sm mb-6">If an account exists with <span class="text-white">{{ email }}</span>, we've sent a password reset link. It expires in 1 hour.</p>
        <router-link to="/login" class="text-emerald-400 hover:text-emerald-300 text-sm font-medium">Back to sign in</router-link>
      </div>

      <!-- Form State -->
      <div v-else>
        <h2 class="text-2xl font-bold text-white mb-1">Forgot your password?</h2>
        <p class="text-gray-500 text-sm mb-6">Enter your email and we'll send you a reset link.</p>

        <div v-if="error" class="mb-4 p-3 bg-red-500/10 border border-red-500/20 rounded-lg text-red-400 text-sm">
          {{ error }}
        </div>

        <form @submit.prevent="submit">
          <div class="mb-4">
            <label class="block text-xs font-medium text-gray-400 mb-1.5">Email</label>
            <input
              v-model="email"
              type="email"
              required
              autocomplete="email"
              class="w-full bg-[#1a1f2e] border border-[#2a3040] rounded-lg px-4 py-2.5 text-sm text-white placeholder-gray-600 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:ring-offset-2 focus:ring-offset-[#0f1320] transition-colors"
              placeholder="you@business.com"
            />
          </div>
          <button
            type="submit"
            :disabled="loading"
            class="w-full py-2.5 bg-emerald-500 hover:bg-emerald-600 disabled:opacity-50 text-white font-medium text-sm rounded-lg transition-colors"
          >
            {{ loading ? 'Sending...' : 'Send Reset Link' }}
          </button>
        </form>

        <p class="mt-6 text-center text-sm text-gray-500">
          Remember your password?
          <router-link to="/login" class="text-emerald-400 hover:text-emerald-300 ml-1 font-medium">Sign in</router-link>
        </p>
      </div>
    </div>
  </div>
</template>

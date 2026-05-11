<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { auth } from '../stores/auth'

const router = useRouter()
const route = useRoute()
const mode = ref(route.name === 'Signup' ? 'signup' : 'login')
const name = ref('')
const email = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

if (auth.isAuthenticated) {
  router.push('/dashboard')
}

async function submit() {
  error.value = ''
  loading.value = true
  try {
    if (mode.value === 'signup') {
      await auth.signup(name.value, email.value, password.value)
    } else {
      await auth.login(email.value, password.value)
    }
    router.push(mode.value === 'signup' ? '/onboarding' : '/dashboard')
  } catch (e) {
    error.value = e.response?.data?.error || 'Something went wrong'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen bg-[#0f1320] flex">
    <!-- Left: Branding -->
    <div class="hidden lg:flex lg:flex-1 items-center justify-center bg-gradient-to-br from-[#0f1320] to-[#1a2340] relative overflow-hidden">
      <div class="absolute inset-0 opacity-10">
        <div class="absolute top-20 left-20 w-72 h-72 bg-emerald-500 rounded-full blur-[128px]"></div>
        <div class="absolute bottom-20 right-20 w-96 h-96 bg-teal-600 rounded-full blur-[128px]"></div>
      </div>
      <div class="relative z-10 max-w-md px-8">
        <div class="flex items-center gap-3 mb-8">
          <div class="w-12 h-12 bg-gradient-to-br from-emerald-500 to-teal-600 rounded-xl flex items-center justify-center">
            <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
            </svg>
          </div>
          <span class="text-white font-bold text-2xl tracking-tight">ReplyIQ</span>
        </div>
        <h1 class="text-4xl font-bold text-white leading-tight mb-4">
          AI-powered replies for every Google review
        </h1>
        <p class="text-gray-400 text-lg leading-relaxed">
          Stop spending hours replying to reviews. ReplyIQ monitors your Google Business Profile, generates natural responses, and posts them automatically.
        </p>
        <div class="mt-8 space-y-3">
          <div class="flex items-center gap-3 text-sm text-gray-300">
            <svg class="w-5 h-5 text-emerald-400 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
            </svg>
            Monitors reviews every 30 minutes
          </div>
          <div class="flex items-center gap-3 text-sm text-gray-300">
            <svg class="w-5 h-5 text-emerald-400 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
            </svg>
            AI replies that sound human, not robotic
          </div>
          <div class="flex items-center gap-3 text-sm text-gray-300">
            <svg class="w-5 h-5 text-emerald-400 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
            </svg>
            Auto-post or approve first — you choose
          </div>
          <div class="flex items-center gap-3 text-sm text-gray-300">
            <svg class="w-5 h-5 text-emerald-400 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
            </svg>
            7-day free trial, no credit card required
          </div>
        </div>
      </div>
    </div>

    <!-- Right: Auth Form -->
    <div class="flex-1 flex items-center justify-center px-6">
      <div class="w-full max-w-sm">
        <!-- Mobile logo -->
        <div class="lg:hidden flex items-center gap-2.5 mb-8 justify-center">
          <div class="w-10 h-10 bg-gradient-to-br from-emerald-500 to-teal-600 rounded-xl flex items-center justify-center">
            <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
            </svg>
          </div>
          <span class="text-white font-bold text-xl">ReplyIQ</span>
        </div>

        <h2 class="text-2xl font-bold text-white mb-1">
          {{ mode === 'signup' ? 'Start your free trial' : 'Welcome back' }}
        </h2>
        <p class="text-gray-500 text-sm mb-6">
          {{ mode === 'signup' ? '7 days free, no credit card required' : 'Sign in to your account' }}
        </p>

        <div v-if="error" class="mb-4 p-3 bg-red-500/10 border border-red-500/20 rounded-lg text-red-400 text-sm">
          {{ error }}
        </div>

        <form @submit.prevent="submit" class="space-y-4">
          <div v-if="mode === 'signup'">
            <label class="block text-xs font-medium text-gray-400 mb-1.5">Full Name</label>
            <input
              v-model="name"
              type="text"
              required
              autocomplete="name"
              class="w-full bg-[#1a1f2e] border border-[#2a3040] rounded-lg px-4 py-2.5 text-sm text-white placeholder-gray-600 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:ring-offset-2 focus:ring-offset-[#0f1320] transition-colors"
              placeholder="Your Name"
            />
          </div>
          <div>
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
          <div>
            <div class="flex items-center justify-between mb-1.5">
              <label class="text-xs font-medium text-gray-400">Password</label>
              <router-link v-if="mode === 'login'" to="/forgot-password" class="text-xs text-emerald-400 hover:text-emerald-300 transition-colors">Forgot password?</router-link>
            </div>
            <input
              v-model="password"
              type="password"
              required
              :autocomplete="mode === 'signup' ? 'new-password' : 'current-password'"
              :minlength="mode === 'signup' ? 8 : undefined"
              class="w-full bg-[#1a1f2e] border border-[#2a3040] rounded-lg px-4 py-2.5 text-sm text-white placeholder-gray-600 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:ring-offset-2 focus:ring-offset-[#0f1320] transition-colors"
              placeholder="••••••••"
            />
          </div>
          <button
            type="submit"
            :disabled="loading"
            class="w-full py-2.5 bg-emerald-500 hover:bg-emerald-600 disabled:opacity-50 text-white font-medium text-sm rounded-lg transition-colors"
          >
            {{ loading ? 'Please wait...' : mode === 'signup' ? 'Create Account' : 'Sign In' }}
          </button>
        </form>

        <p class="mt-6 text-center text-sm text-gray-500">
          {{ mode === 'signup' ? 'Already have an account?' : "Don't have an account?" }}
          <router-link
            :to="mode === 'signup' ? '/login' : '/signup'"
            @click="mode = mode === 'signup' ? 'login' : 'signup'; error = ''"
            class="text-emerald-400 hover:text-emerald-300 ml-1 font-medium"
          >
            {{ mode === 'signup' ? 'Sign in' : 'Start free trial' }}
          </router-link>
        </p>
      </div>
    </div>
  </div>
</template>

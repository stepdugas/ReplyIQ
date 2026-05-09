<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'
import { auth } from '../stores/auth'

const locations = ref([])
const loading = ref(true)

const toneOptions = ['professional', 'friendly', 'brief']

onMounted(async () => {
  try {
    const { data } = await api.get('/locations')
    locations.value = data
  } catch (e) {
    console.error('Failed to load locations:', e)
  } finally {
    loading.value = false
  }
})

async function updateLocation(loc, field, value) {
  try {
    await api.patch(`/locations/${loc.id}/settings`, { [field]: value })
    loc[field] = value
  } catch (e) {
    console.error('Failed to update location:', e)
  }
}

async function connectGoogle() {
  try {
    const { data } = await api.get('/oauth2/google/authorize')
    window.location.href = data.url
  } catch (e) {
    console.error('Failed to get OAuth URL:', e)
  }
}

async function subscribe() {
  try {
    const { data } = await api.post('/stripe/checkout')
    window.location.href = data.url
  } catch (e) {
    console.error('Failed to create checkout:', e)
  }
}

async function manageSubscription() {
  try {
    const { data } = await api.post('/stripe/portal')
    window.location.href = data.url
  } catch (e) {
    console.error('Failed to open billing portal:', e)
  }
}

function logout() {
  auth.logout()
  window.location.href = '/'
}
</script>

<template>
  <aside class="w-80 bg-[#141824] border-r border-[#2a3040] flex flex-col h-full overflow-hidden">
    <!-- Brand -->
    <div class="p-5 border-b border-[#2a3040]">
      <div class="flex items-center gap-2.5">
        <div class="w-8 h-8 bg-gradient-to-br from-emerald-500 to-teal-600 rounded-lg flex items-center justify-center">
          <svg class="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
          </svg>
        </div>
        <span class="text-white font-bold text-lg tracking-tight">ReplyIQ</span>
      </div>
    </div>

    <!-- User Info -->
    <div class="p-5 border-b border-[#2a3040]">
      <div class="text-sm text-white font-medium">{{ auth.user?.name }}</div>
      <div class="text-xs text-gray-500 mt-0.5">{{ auth.user?.email }}</div>
      <div class="mt-2 flex items-center gap-1.5">
        <span class="w-1.5 h-1.5 rounded-full" :class="auth.user?.subscriptionStatus === 'active' ? 'bg-emerald-400' : 'bg-amber-400'"></span>
        <span class="text-xs capitalize" :class="auth.user?.subscriptionStatus === 'active' ? 'text-emerald-400' : 'text-amber-400'">
          {{ auth.user?.subscriptionStatus || 'unknown' }}
        </span>
      </div>
    </div>

    <!-- Locations -->
    <div class="flex-1 overflow-y-auto p-5">
      <div class="flex items-center justify-between mb-4">
        <h3 class="text-xs font-semibold text-gray-400 uppercase tracking-wider">Locations</h3>
        <button
          @click="connectGoogle"
          class="text-xs text-emerald-400 hover:text-emerald-300 transition-colors"
        >
          + Connect
        </button>
      </div>

      <div v-if="loading" class="text-xs text-gray-500">Loading...</div>

      <div v-else-if="locations.length === 0" class="text-center py-6">
        <div class="text-gray-500 text-sm mb-3">No locations connected</div>
        <button
          @click="connectGoogle"
          class="px-4 py-2 bg-emerald-500 hover:bg-emerald-600 text-white text-xs font-medium rounded-lg transition-colors"
        >
          Connect Google Business
        </button>
      </div>

      <div v-else class="space-y-3">
        <div
          v-for="loc in locations"
          :key="loc.id"
          class="bg-[#1a1f2e] border border-[#2a3040] rounded-lg p-3"
        >
          <div class="flex items-start gap-2 mb-2">
            <span class="w-2 h-2 rounded-full bg-emerald-400 mt-1.5 shrink-0"></span>
            <div>
              <div class="text-sm text-white font-medium leading-tight">{{ loc.locationName }}</div>
              <div class="text-xs text-gray-500 mt-0.5">{{ loc.address }}</div>
            </div>
          </div>

          <!-- Tone Selector -->
          <div class="mt-3">
            <label class="text-xs text-gray-500 block mb-1">Tone</label>
            <select
              :value="loc.tonePreference"
              @change="updateLocation(loc, 'tonePreference', $event.target.value)"
              class="w-full bg-[#141824] border border-[#2a3040] rounded text-xs text-gray-300 px-2 py-1.5 focus:outline-none focus:border-emerald-500/50"
            >
              <option v-for="t in toneOptions" :key="t" :value="t" class="capitalize">
                {{ t.charAt(0).toUpperCase() + t.slice(1) }}
              </option>
            </select>
          </div>

          <!-- Auto-post Toggle -->
          <div class="mt-2 flex items-center justify-between">
            <label class="text-xs text-gray-500">Auto-post replies</label>
            <button
              @click="updateLocation(loc, 'autoPost', !loc.autoPost)"
              :class="[
                'relative inline-flex h-5 w-9 items-center rounded-full transition-colors',
                loc.autoPost ? 'bg-emerald-500' : 'bg-[#2a3040]',
              ]"
            >
              <span
                :class="[
                  'inline-block h-3.5 w-3.5 transform rounded-full bg-white transition-transform',
                  loc.autoPost ? 'translate-x-4' : 'translate-x-1',
                ]"
              ></span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Subscription -->
    <div class="p-5 border-t border-[#2a3040]">
      <h3 class="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-3">Subscription</h3>
      <div class="bg-[#1a1f2e] border border-[#2a3040] rounded-lg p-3 mb-3">
        <div class="flex items-center gap-1.5 mb-1">
          <span class="w-1.5 h-1.5 rounded-full" :class="auth.user?.subscriptionStatus === 'active' ? 'bg-emerald-400' : auth.user?.subscriptionStatus === 'trialing' ? 'bg-amber-400' : 'bg-red-400'"></span>
          <span class="text-xs font-medium capitalize" :class="auth.user?.subscriptionStatus === 'active' ? 'text-emerald-400' : auth.user?.subscriptionStatus === 'trialing' ? 'text-amber-400' : 'text-red-400'">
            {{ auth.user?.subscriptionStatus || 'unknown' }}
          </span>
        </div>
        <div class="text-xs text-gray-500">$19.99/month</div>
      </div>
      <button
        v-if="auth.user?.subscriptionStatus === 'trialing'"
        @click="subscribe"
        class="w-full px-4 py-2 bg-emerald-500 hover:bg-emerald-600 text-white text-xs font-medium rounded-lg transition-colors mb-2"
      >
        Subscribe Now
      </button>
      <button
        v-else-if="auth.user?.subscriptionStatus === 'active'"
        @click="manageSubscription"
        class="w-full px-4 py-2 bg-[#1a1f2e] hover:bg-[#252b3d] text-gray-300 text-xs font-medium rounded-lg transition-colors mb-2"
      >
        Manage Billing
      </button>
      <button
        @click="logout"
        class="w-full px-4 py-2 bg-[#1a1f2e] hover:bg-[#252b3d] text-gray-400 text-xs font-medium rounded-lg transition-colors mb-3"
      >
        Sign Out
      </button>
      <div class="flex items-center justify-center gap-3 text-[10px] text-gray-600">
        <span>&copy; 2026 Erie Apps LLC</span>
        <router-link to="/terms" class="hover:text-gray-400 transition-colors">Terms</router-link>
        <router-link to="/privacy" class="hover:text-gray-400 transition-colors">Privacy</router-link>
      </div>
    </div>
  </aside>
</template>

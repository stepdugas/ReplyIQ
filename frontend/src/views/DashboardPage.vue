<script setup>
import { ref, onMounted, computed } from 'vue'
import api from '../api'
import StatsRow from '../components/StatsRow.vue'
import ReviewCard from '../components/ReviewCard.vue'
import DashboardSidebar from '../components/DashboardSidebar.vue'
import { toast } from '../stores/toast'

const stats = ref({ totalReviews: 0, unansweredReviews: 0, repliedThisMonth: 0, averageRating: 0 })
const reviews = ref([])
const loading = ref(true)
const activeFilter = ref('all')
const mobileMenuOpen = ref(false)

const filters = [
  { key: 'all', label: 'All Reviews' },
  { key: 'needs_reply', label: 'Needs Reply' },
  { key: 'pending', label: 'Pending' },
  { key: 'posted', label: 'Posted' },
  { key: 'failed', label: 'Failed' },
]

const filteredReviews = computed(() => {
  if (activeFilter.value === 'all') return reviews.value
  return reviews.value.filter((r) => r.replyStatus === activeFilter.value)
})

const filterCounts = computed(() => {
  const counts = { all: reviews.value.length }
  for (const f of filters) {
    if (f.key !== 'all') {
      counts[f.key] = reviews.value.filter((r) => r.replyStatus === f.key).length
    }
  }
  return counts
})

async function loadData() {
  loading.value = true
  try {
    const [statsRes, reviewsRes] = await Promise.all([
      api.get('/reviews/stats'),
      api.get('/reviews'),
    ])
    stats.value = statsRes.data
    reviews.value = reviewsRes.data
  } catch (e) {
    console.error('Failed to load dashboard data:', e)
  } finally {
    loading.value = false
  }
}

async function generateAll() {
  try {
    await api.post('/replies/generate-all')
    toast.showSuccess('All replies generated')
    await loadData()
  } catch (e) {
    toast.showError('Failed to generate replies')
    console.error('Failed to generate replies:', e)
  }
}

onMounted(loadData)
</script>

<template>
  <div class="flex h-screen bg-[#0f1320] overflow-hidden">
    <!-- Sidebar (desktop) -->
    <div class="hidden lg:flex">
      <DashboardSidebar />
    </div>

    <!-- Mobile menu overlay -->
    <div
      v-if="mobileMenuOpen"
      class="fixed inset-0 z-40 lg:hidden"
      @click="mobileMenuOpen = false"
    >
      <div class="absolute inset-0 bg-black/60"></div>
      <div class="relative z-50 w-80" @click.stop>
        <DashboardSidebar />
      </div>
    </div>

    <!-- Main Content -->
    <main class="flex-1 overflow-y-auto">
      <!-- Top Bar -->
      <header class="sticky top-0 z-30 bg-[#0f1320]/80 backdrop-blur-xl border-b border-[#2a3040]">
        <div class="flex items-center justify-between px-6 py-4">
          <div class="flex items-center gap-3">
            <button
              @click="mobileMenuOpen = true"
              class="lg:hidden text-gray-400 hover:text-white focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:ring-offset-2 focus:ring-offset-[#0f1320]"
              aria-label="Open navigation menu"
            >
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
              </svg>
            </button>
            <div>
              <h1 class="text-xl font-bold text-white">Dashboard</h1>
              <p class="text-xs text-gray-500 mt-0.5">Manage your reviews and replies</p>
            </div>
          </div>
          <div class="flex items-center gap-3">
            <button
              v-if="stats.unansweredReviews > 0"
              @click="generateAll"
              class="px-4 py-2 bg-emerald-500 hover:bg-emerald-600 text-white text-sm font-medium rounded-lg transition-colors flex items-center gap-2"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
              </svg>
              Generate All Replies
            </button>
          </div>
        </div>
      </header>

      <div class="p-6 space-y-6 max-w-5xl">
        <!-- Stats -->
        <StatsRow :stats="stats" />

        <!-- Filter Bar -->
        <div class="flex items-center gap-1 bg-[#1a1f2e] rounded-xl p-1 border border-[#2a3040] overflow-x-auto">
          <button
            v-for="f in filters"
            :key="f.key"
            @click="activeFilter = f.key"
            :class="[
              'px-4 py-2 rounded-lg text-xs font-medium whitespace-nowrap transition-all',
              activeFilter === f.key
                ? 'bg-emerald-500/15 text-emerald-400'
                : 'text-gray-400 hover:text-gray-200 hover:bg-[#252b3d]',
            ]"
          >
            {{ f.label }}
            <span
              v-if="filterCounts[f.key]"
              :class="[
                'ml-1.5 px-1.5 py-0.5 rounded-full text-[10px]',
                activeFilter === f.key ? 'bg-emerald-500/20 text-emerald-300' : 'bg-[#252b3d] text-gray-500',
              ]"
            >
              {{ filterCounts[f.key] }}
            </span>
          </button>
        </div>

        <!-- Review Feed -->
        <div v-if="loading" class="space-y-4">
          <div v-for="i in 3" :key="i" class="bg-[#1a1f2e] border border-[#2a3040] rounded-xl p-5 animate-pulse">
            <div class="flex items-center gap-3 mb-4">
              <div class="w-10 h-10 rounded-full bg-[#252b3d]"></div>
              <div class="space-y-2">
                <div class="w-24 h-3 bg-[#252b3d] rounded"></div>
                <div class="w-16 h-2 bg-[#252b3d] rounded"></div>
              </div>
            </div>
            <div class="w-full h-3 bg-[#252b3d] rounded mb-2 ml-[52px]"></div>
            <div class="w-3/4 h-3 bg-[#252b3d] rounded ml-[52px]"></div>
          </div>
        </div>

        <div v-else-if="filteredReviews.length === 0" class="text-center py-16">
          <div class="w-16 h-16 mx-auto mb-4 rounded-full bg-[#1a1f2e] flex items-center justify-center">
            <svg class="w-8 h-8 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
            </svg>
          </div>
          <h3 class="text-gray-400 font-medium mb-1">No reviews here</h3>
          <p class="text-gray-600 text-sm">
            {{ activeFilter === 'all' ? 'Connect a Google Business Profile to start monitoring reviews.' : 'No reviews match this filter.' }}
          </p>
        </div>

        <div v-else class="space-y-3">
          <ReviewCard
            v-for="review in filteredReviews"
            :key="review.id"
            :review="review"
            @updated="loadData"
          />
        </div>
      </div>
    </main>
  </div>
</template>

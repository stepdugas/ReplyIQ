<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api'

const router = useRouter()
const currentStep = ref(1)
const totalSteps = 3
const loading = ref(false)
const pollStatus = ref('')
const reviewCount = ref(0)
const locations = ref([])

const progress = computed(() => (currentStep.value / totalSteps) * 100)

async function connectGoogle() {
  loading.value = true
  try {
    const { data } = await api.get('/oauth2/google/authorize')
    window.location.href = data.url
  } catch (e) {
    console.error('Failed to get OAuth URL:', e)
    loading.value = false
  }
}

async function skipToDemo() {
  // For testing without real Google — go straight to dashboard
  currentStep.value = 3
  pollStatus.value = 'complete'
}

async function pollReviews() {
  currentStep.value = 3
  pollStatus.value = 'polling'
  loading.value = true

  try {
    // Fetch locations first
    const locRes = await api.get('/locations')
    locations.value = locRes.data

    // Trigger review poll
    const pollRes = await api.post('/reviews/poll')
    reviewCount.value = pollRes.data.newReviews

    pollStatus.value = 'complete'
  } catch (e) {
    console.error('Failed to poll reviews:', e)
    pollStatus.value = 'complete'
  } finally {
    loading.value = false
  }
}

function goToDashboard() {
  router.push('/dashboard')
}
</script>

<template>
  <div class="min-h-screen bg-[#0f1320] flex items-center justify-center px-6">
    <div class="w-full max-w-lg">

      <!-- Progress Bar -->
      <div class="mb-8">
        <div class="flex items-center justify-between mb-2">
          <span class="text-xs font-medium text-gray-400">Step {{ currentStep }} of {{ totalSteps }}</span>
          <span class="text-xs text-gray-500">{{ Math.round(progress) }}% complete</span>
        </div>
        <div class="h-1.5 bg-[#1a1f2e] rounded-full overflow-hidden">
          <div
            class="h-full bg-gradient-to-r from-emerald-500 to-teal-500 rounded-full transition-all duration-500"
            :style="{ width: progress + '%' }"
          ></div>
        </div>
      </div>

      <!-- Step 1: Welcome -->
      <div v-if="currentStep === 1" class="text-center">
        <div class="w-16 h-16 mx-auto mb-6 bg-gradient-to-br from-emerald-500 to-teal-600 rounded-2xl flex items-center justify-center">
          <svg class="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
          </svg>
        </div>
        <h1 class="text-2xl font-bold text-white mb-2">Welcome to ReplyIQ</h1>
        <p class="text-gray-400 mb-8 max-w-sm mx-auto">
          Let's get you set up in under 2 minutes. We'll connect your Google Business Profile and start monitoring your reviews.
        </p>

        <div class="space-y-3 text-left bg-[#1a1f2e] border border-[#2a3040] rounded-xl p-5 mb-8">
          <div class="flex items-start gap-3">
            <div class="w-6 h-6 rounded-full bg-emerald-500/15 flex items-center justify-center shrink-0 mt-0.5">
              <span class="text-emerald-400 text-xs font-bold">1</span>
            </div>
            <div>
              <div class="text-sm text-white font-medium">Connect Google Business Profile</div>
              <div class="text-xs text-gray-500">One-click OAuth — takes 10 seconds</div>
            </div>
          </div>
          <div class="flex items-start gap-3">
            <div class="w-6 h-6 rounded-full bg-emerald-500/15 flex items-center justify-center shrink-0 mt-0.5">
              <span class="text-emerald-400 text-xs font-bold">2</span>
            </div>
            <div>
              <div class="text-sm text-white font-medium">Choose your reply settings</div>
              <div class="text-xs text-gray-500">Set tone preference and auto-post mode</div>
            </div>
          </div>
          <div class="flex items-start gap-3">
            <div class="w-6 h-6 rounded-full bg-emerald-500/15 flex items-center justify-center shrink-0 mt-0.5">
              <span class="text-emerald-400 text-xs font-bold">3</span>
            </div>
            <div>
              <div class="text-sm text-white font-medium">Watch your reviews appear</div>
              <div class="text-xs text-gray-500">We pull in your existing reviews immediately</div>
            </div>
          </div>
        </div>

        <button
          @click="currentStep = 2"
          class="w-full py-3 bg-emerald-500 hover:bg-emerald-600 text-white font-medium rounded-xl transition-colors"
        >
          Let's Go
        </button>
      </div>

      <!-- Step 2: Connect Google -->
      <div v-if="currentStep === 2" class="text-center">
        <div class="w-16 h-16 mx-auto mb-6 bg-[#1a1f2e] border border-[#2a3040] rounded-2xl flex items-center justify-center">
          <svg class="w-8 h-8 text-white" viewBox="0 0 24 24">
            <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 01-2.2 3.32v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.1z"/>
            <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
            <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
            <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
          </svg>
        </div>
        <h1 class="text-2xl font-bold text-white mb-2">Connect Your Business</h1>
        <p class="text-gray-400 mb-8">
          Sign in with Google to connect your Business Profile. We only request access to read your reviews and post replies.
        </p>

        <div class="bg-[#1a1f2e] border border-[#2a3040] rounded-xl p-4 mb-6 text-left">
          <div class="text-xs font-medium text-gray-400 mb-2">What we access:</div>
          <div class="space-y-2">
            <div class="flex items-center gap-2 text-sm text-gray-300">
              <svg class="w-4 h-4 text-emerald-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
              Your business locations
            </div>
            <div class="flex items-center gap-2 text-sm text-gray-300">
              <svg class="w-4 h-4 text-emerald-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
              Customer reviews on each location
            </div>
            <div class="flex items-center gap-2 text-sm text-gray-300">
              <svg class="w-4 h-4 text-emerald-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
              Permission to post replies on your behalf
            </div>
          </div>
        </div>

        <button
          @click="connectGoogle"
          :disabled="loading"
          class="w-full py-3 bg-white hover:bg-gray-100 text-gray-800 font-medium rounded-xl transition-colors flex items-center justify-center gap-3"
        >
          <svg class="w-5 h-5" viewBox="0 0 24 24">
            <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 01-2.2 3.32v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.1z"/>
            <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
            <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
            <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
          </svg>
          {{ loading ? 'Connecting...' : 'Connect with Google' }}
        </button>

        <button
          @click="skipToDemo"
          class="w-full mt-3 py-2 text-gray-500 hover:text-gray-300 text-sm transition-colors"
        >
          Skip for now (explore with demo data)
        </button>
      </div>

      <!-- Step 3: Reviews Loading -->
      <div v-if="currentStep === 3" class="text-center">
        <div v-if="pollStatus === 'polling'" class="py-8">
          <div class="w-16 h-16 mx-auto mb-6 relative">
            <div class="absolute inset-0 rounded-full border-2 border-[#2a3040]"></div>
            <div class="absolute inset-0 rounded-full border-2 border-emerald-500 border-t-transparent animate-spin"></div>
          </div>
          <h1 class="text-2xl font-bold text-white mb-2">Pulling in your reviews...</h1>
          <p class="text-gray-400">This usually takes just a few seconds</p>
        </div>

        <div v-else class="py-4">
          <div class="w-16 h-16 mx-auto mb-6 bg-emerald-500/15 rounded-full flex items-center justify-center">
            <svg class="w-8 h-8 text-emerald-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
            </svg>
          </div>
          <h1 class="text-2xl font-bold text-white mb-2">You're all set!</h1>
          <p class="text-gray-400 mb-6">
            Your dashboard is ready. ReplyIQ will now monitor your reviews every 30 minutes and generate AI replies for you.
          </p>

          <div v-if="locations.length > 0" class="bg-[#1a1f2e] border border-[#2a3040] rounded-xl p-4 mb-6 text-left">
            <div class="text-xs font-medium text-gray-400 mb-3">Connected locations:</div>
            <div class="space-y-2">
              <div v-for="loc in locations" :key="loc.id" class="flex items-center gap-2">
                <span class="w-2 h-2 rounded-full bg-emerald-400"></span>
                <span class="text-sm text-white">{{ loc.locationName }}</span>
              </div>
            </div>
            <div v-if="reviewCount > 0" class="mt-3 pt-3 border-t border-[#2a3040] text-sm text-emerald-400">
              {{ reviewCount }} new reviews found
            </div>
          </div>

          <div class="bg-[#1a1f2e] border border-[#2a3040] rounded-xl p-4 mb-8 text-left">
            <div class="text-xs font-medium text-gray-400 mb-3">What happens next:</div>
            <div class="space-y-2 text-sm text-gray-300">
              <div class="flex items-start gap-2">
                <svg class="w-4 h-4 text-emerald-400 mt-0.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
                </svg>
                New reviews are checked every 30 minutes
              </div>
              <div class="flex items-start gap-2">
                <svg class="w-4 h-4 text-emerald-400 mt-0.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
                </svg>
                AI replies are generated instantly
              </div>
              <div class="flex items-start gap-2">
                <svg class="w-4 h-4 text-emerald-400 mt-0.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
                </svg>
                Approve or edit before posting (or enable auto-post)
              </div>
            </div>
          </div>

          <button
            @click="goToDashboard"
            class="w-full py-3 bg-emerald-500 hover:bg-emerald-600 text-white font-medium rounded-xl transition-colors"
          >
            Go to Dashboard
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

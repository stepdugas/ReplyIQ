<script setup>
import { ref } from 'vue'
import StarRating from './StarRating.vue'
import StatusBadge from './StatusBadge.vue'
import api from '../api'
import { toast } from '../stores/toast'

const props = defineProps({
  review: { type: Object, required: true },
})

const emit = defineEmits(['updated'])

const editing = ref(false)
const editText = ref('')
const generating = ref(false)

function startEdit() {
  editText.value = props.review.replyText || ''
  editing.value = true
}

async function saveEdit() {
  await api.put(`/replies/${props.review.id}`, { replyText: editText.value })
  editing.value = false
  toast.showSuccess('Reply saved')
  emit('updated')
}

async function generateReply() {
  generating.value = true
  try {
    await api.post(`/replies/generate/${props.review.id}`)
    toast.showSuccess('Reply generated')
    emit('updated')
  } catch (e) {
    toast.showError('Failed to generate reply')
    console.error('Failed to generate reply:', e)
  } finally {
    generating.value = false
  }
}

const approving = ref(false)

async function approveReply() {
  approving.value = true
  try {
    await api.post(`/replies/approve/${props.review.id}`)
    toast.showSuccess('Reply approved and posted')
    emit('updated')
  } catch (e) {
    toast.showError('Failed to approve reply')
    console.error('Failed to approve reply:', e)
  } finally {
    approving.value = false
  }
}

async function regenerateReply() {
  generating.value = true
  try {
    await api.post(`/replies/regenerate/${props.review.id}`)
    toast.showSuccess('Reply regenerated')
    emit('updated')
  } catch (e) {
    toast.showError('Failed to regenerate reply')
    console.error('Failed to regenerate reply:', e)
  } finally {
    generating.value = false
  }
}

function timeAgo(dateStr) {
  const now = new Date()
  const date = new Date(dateStr)
  const diff = Math.floor((now - date) / 1000)
  if (diff < 60) return 'just now'
  if (diff < 3600) return `${Math.floor(diff / 60)}m ago`
  if (diff < 86400) return `${Math.floor(diff / 3600)}h ago`
  if (diff < 604800) return `${Math.floor(diff / 86400)}d ago`
  return date.toLocaleDateString()
}
</script>

<template>
  <div class="bg-[#1a1f2e] border border-[#2a3040] rounded-xl p-5 hover:border-[#3a4060] transition-colors">
    <!-- Header -->
    <div class="flex items-start justify-between mb-3">
      <div class="flex items-center gap-3">
        <div class="w-10 h-10 rounded-full bg-gradient-to-br from-emerald-500 to-teal-600 flex items-center justify-center text-white font-semibold text-sm">
          {{ review.reviewerName.charAt(0).toUpperCase() }}
        </div>
        <div>
          <div class="text-white font-medium text-sm">{{ review.reviewerName }}</div>
          <div class="flex items-center gap-2 mt-0.5">
            <StarRating :rating="review.starRating" size="sm" />
            <span class="text-xs text-gray-500">{{ timeAgo(review.postedAt) }}</span>
          </div>
        </div>
      </div>
      <div class="flex items-center gap-2">
        <span class="text-xs text-gray-500 bg-[#252b3d] px-2 py-1 rounded">{{ review.locationName }}</span>
        <StatusBadge :status="review.replyStatus" />
      </div>
    </div>

    <!-- Review Text -->
    <div class="text-gray-300 text-sm leading-relaxed mb-4 pl-[52px]">
      "{{ review.reviewText || '(No text provided)' }}"
    </div>

    <!-- Reply Section -->
    <div class="pl-[52px]">
      <!-- Existing Reply -->
      <div v-if="review.replyText && !editing" class="bg-[#141824] border border-[#252b3d] rounded-lg p-4 mb-3">
        <div class="flex items-center gap-2 mb-2">
          <svg class="w-4 h-4 text-emerald-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h10a8 8 0 018 8v2M3 10l6 6m-6-6l6-6" />
          </svg>
          <span class="text-xs font-medium text-emerald-400">Your Reply</span>
        </div>
        <p class="text-gray-300 text-sm leading-relaxed">{{ review.replyText }}</p>
      </div>

      <!-- Edit Mode -->
      <div v-if="editing" class="mb-3">
        <textarea
          v-model="editText"
          class="w-full bg-[#141824] border border-emerald-500/30 rounded-lg p-3 text-sm text-gray-200 placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:ring-offset-2 focus:ring-offset-[#0f1320] resize-none"
          rows="4"
          placeholder="Edit your reply..."
        ></textarea>
        <div class="flex gap-2 mt-2">
          <button
            @click="saveEdit"
            class="px-4 py-1.5 bg-emerald-500 hover:bg-emerald-600 text-white text-xs font-medium rounded-lg transition-colors"
          >
            Save
          </button>
          <button
            @click="editing = false"
            class="px-4 py-1.5 bg-[#252b3d] hover:bg-[#303850] text-gray-300 text-xs font-medium rounded-lg transition-colors"
          >
            Cancel
          </button>
        </div>
      </div>

      <!-- Action Buttons -->
      <div class="flex items-center gap-2">
        <template v-if="review.replyStatus === 'needs_reply' || review.replyStatus === 'failed'">
          <button
            @click="generateReply"
            :disabled="generating"
            class="px-4 py-1.5 bg-emerald-500 hover:bg-emerald-600 disabled:opacity-50 disabled:cursor-wait text-white text-xs font-medium rounded-lg transition-colors flex items-center gap-1.5"
          >
            <svg v-if="generating" class="w-3 h-3 animate-spin" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"></path>
            </svg>
            {{ generating ? 'Generating...' : 'Generate Reply' }}
          </button>
        </template>

        <template v-if="review.replyStatus === 'pending'">
          <button
            @click="approveReply"
            :disabled="approving"
            class="px-4 py-1.5 bg-emerald-500 hover:bg-emerald-600 disabled:opacity-50 text-white text-xs font-medium rounded-lg transition-colors flex items-center gap-1.5"
          >
            <svg v-if="approving" class="w-3 h-3 animate-spin" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"></path>
            </svg>
            {{ approving ? 'Posting...' : 'Approve & Post' }}
          </button>
          <button
            @click="startEdit"
            class="px-4 py-1.5 bg-[#252b3d] hover:bg-[#303850] text-gray-300 text-xs font-medium rounded-lg transition-colors"
          >
            Edit
          </button>
          <button
            @click="regenerateReply"
            :disabled="generating"
            class="px-4 py-1.5 bg-[#252b3d] hover:bg-[#303850] text-gray-300 text-xs font-medium rounded-lg transition-colors"
          >
            Regenerate
          </button>
        </template>

        <template v-if="review.replyStatus === 'posted'">
          <span class="text-xs text-gray-500">
            Replied {{ review.repliedAt ? timeAgo(review.repliedAt) : '' }}
          </span>
        </template>
      </div>
    </div>
  </div>
</template>

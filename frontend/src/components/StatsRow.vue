<script setup>
defineProps({
  stats: { type: Object, required: true },
})

const cards = [
  { key: 'totalReviews', label: 'Total Reviews', icon: '📊' },
  { key: 'unansweredReviews', label: 'Needs Reply', icon: '💬' },
  { key: 'repliedThisMonth', label: 'Replied This Month', icon: '✅' },
  { key: 'averageRating', label: 'Avg Rating', icon: '⭐' },
]
</script>

<template>
  <div class="grid grid-cols-2 lg:grid-cols-4 gap-4">
    <div
      v-for="card in cards"
      :key="card.key"
      class="bg-[#1a1f2e] border border-[#2a3040] rounded-xl p-5 hover:border-emerald-500/30 transition-colors"
    >
      <div class="flex items-center justify-between mb-3">
        <span class="text-xs font-medium text-gray-400 uppercase tracking-wider">{{ card.label }}</span>
        <span class="text-lg">{{ card.icon }}</span>
      </div>
      <div class="text-3xl font-bold text-white">
        {{ card.key === 'averageRating' ? (stats[card.key] || 0).toFixed(1) : (stats[card.key] || 0) }}
      </div>
    </div>
  </div>
</template>

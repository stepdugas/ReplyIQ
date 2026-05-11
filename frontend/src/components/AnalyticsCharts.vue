<script setup>
import { computed, ref, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  ArcElement,
  Tooltip,
  Legend,
  Filler,
} from 'chart.js'

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, BarElement, ArcElement, Tooltip, Legend, Filler)

const props = defineProps({
  reviews: { type: Array, default: () => [] },
})

// Canvas refs
const reviewsOverTimeCanvas = ref(null)
const avgRatingCanvas = ref(null)
const replyRateCanvas = ref(null)
const ratingDistCanvas = ref(null)

// Chart instances
let reviewsChart = null
let ratingChart = null
let replyChart = null
let distChart = null

const darkChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: false },
    tooltip: {
      backgroundColor: '#1a1f2e',
      titleColor: '#e5e7eb',
      bodyColor: '#9ca3af',
      borderColor: '#2a3040',
      borderWidth: 1,
    },
  },
  scales: {
    x: {
      ticks: { color: '#6b7280', font: { size: 10 } },
      grid: { display: false },
      border: { display: false },
    },
    y: {
      ticks: { color: '#6b7280', font: { size: 10 } },
      grid: { display: false },
      border: { display: false },
    },
  },
}

// Compute last 30 days labels
function getLast30Days() {
  const days = []
  const now = new Date()
  for (let i = 29; i >= 0; i--) {
    const d = new Date(now)
    d.setDate(d.getDate() - i)
    days.push(d.toISOString().slice(0, 10))
  }
  return days
}

// Reviews over time data
const reviewsOverTimeData = computed(() => {
  const days = getLast30Days()
  const counts = {}
  days.forEach((d) => (counts[d] = 0))
  props.reviews.forEach((r) => {
    const day = (r.reviewDate || r.createdAt || '').slice(0, 10)
    if (counts[day] !== undefined) counts[day]++
  })
  return {
    labels: days.map((d) => d.slice(5)), // MM-DD
    datasets: [
      {
        data: days.map((d) => counts[d]),
        borderColor: '#10b981',
        backgroundColor: 'rgba(16,185,129,0.1)',
        fill: true,
        tension: 0.3,
        pointRadius: 0,
        borderWidth: 2,
      },
    ],
  }
})

// Average rating over time
const avgRatingData = computed(() => {
  const days = getLast30Days()
  const groups = {}
  days.forEach((d) => (groups[d] = []))
  props.reviews.forEach((r) => {
    const day = (r.reviewDate || r.createdAt || '').slice(0, 10)
    if (groups[day] && r.rating) groups[day].push(r.rating)
  })
  const avgs = days.map((d) => {
    const arr = groups[d]
    return arr.length ? arr.reduce((a, b) => a + b, 0) / arr.length : null
  })
  return {
    labels: days.map((d) => d.slice(5)),
    datasets: [
      {
        data: avgs,
        borderColor: avgs.map((v) => (v === null ? '#6b7280' : v >= 4 ? '#10b981' : v >= 3 ? '#f59e0b' : '#ef4444')),
        segment: {
          borderColor: (ctx) => {
            const val = ctx.p1.parsed.y
            if (val === null) return '#6b7280'
            return val >= 4 ? '#10b981' : val >= 3 ? '#f59e0b' : '#ef4444'
          },
        },
        tension: 0.3,
        pointRadius: 0,
        borderWidth: 2,
        spanGaps: true,
      },
    ],
  }
})

// Reply rate data
const replyRateData = computed(() => {
  const total = props.reviews.length
  const replied = props.reviews.filter((r) => r.replyStatus === 'posted').length
  const unreplied = total - replied
  const pct = total > 0 ? Math.round((replied / total) * 100) : 0
  return {
    pct,
    data: {
      labels: ['Replied', 'Unreplied'],
      datasets: [
        {
          data: [replied, unreplied],
          backgroundColor: ['#10b981', '#2a3040'],
          borderWidth: 0,
          cutout: '75%',
        },
      ],
    },
  }
})

// Rating distribution
const ratingDistData = computed(() => {
  const counts = [0, 0, 0, 0, 0]
  props.reviews.forEach((r) => {
    if (r.rating >= 1 && r.rating <= 5) counts[r.rating - 1]++
  })
  const colors = ['#ef4444', '#f97316', '#f59e0b', '#84cc16', '#10b981']
  return {
    labels: ['1', '2', '3', '4', '5'],
    datasets: [
      {
        data: counts,
        backgroundColor: colors,
        borderRadius: 4,
        borderWidth: 0,
      },
    ],
  }
})

function createCharts() {
  // Destroy existing
  reviewsChart?.destroy()
  ratingChart?.destroy()
  replyChart?.destroy()
  distChart?.destroy()

  if (reviewsOverTimeCanvas.value) {
    reviewsChart = new ChartJS(reviewsOverTimeCanvas.value, {
      type: 'line',
      data: reviewsOverTimeData.value,
      options: { ...darkChartOptions },
    })
  }

  if (avgRatingCanvas.value) {
    ratingChart = new ChartJS(avgRatingCanvas.value, {
      type: 'line',
      data: avgRatingData.value,
      options: {
        ...darkChartOptions,
        scales: {
          ...darkChartOptions.scales,
          y: { ...darkChartOptions.scales.y, min: 0, max: 5 },
        },
      },
    })
  }

  if (replyRateCanvas.value) {
    replyChart = new ChartJS(replyRateCanvas.value, {
      type: 'doughnut',
      data: replyRateData.value.data,
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false },
          tooltip: {
            backgroundColor: '#1a1f2e',
            titleColor: '#e5e7eb',
            bodyColor: '#9ca3af',
            borderColor: '#2a3040',
            borderWidth: 1,
          },
        },
      },
    })
  }

  if (ratingDistCanvas.value) {
    distChart = new ChartJS(ratingDistCanvas.value, {
      type: 'bar',
      data: ratingDistData.value,
      options: {
        ...darkChartOptions,
        scales: {
          ...darkChartOptions.scales,
          y: { ...darkChartOptions.scales.y, beginAtZero: true },
        },
      },
    })
  }
}

function updateCharts() {
  if (reviewsChart) {
    reviewsChart.data = reviewsOverTimeData.value
    reviewsChart.update()
  }
  if (ratingChart) {
    ratingChart.data = avgRatingData.value
    ratingChart.update()
  }
  if (replyChart) {
    replyChart.data = replyRateData.value.data
    replyChart.update()
  }
  if (distChart) {
    distChart.data = ratingDistData.value
    distChart.update()
  }
}

watch(() => props.reviews, () => {
  if (reviewsChart) {
    updateCharts()
  } else {
    nextTick(createCharts)
  }
}, { deep: true })

onMounted(() => {
  nextTick(createCharts)
})

onBeforeUnmount(() => {
  reviewsChart?.destroy()
  ratingChart?.destroy()
  replyChart?.destroy()
  distChart?.destroy()
})
</script>

<template>
  <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
    <!-- Reviews Over Time -->
    <div class="bg-[#1a1f2e] border border-[#2a3040] rounded-xl p-4">
      <h3 class="text-sm font-medium text-gray-300 mb-3">Reviews Over Time</h3>
      <div class="h-36">
        <canvas ref="reviewsOverTimeCanvas"></canvas>
      </div>
    </div>

    <!-- Average Rating Over Time -->
    <div class="bg-[#1a1f2e] border border-[#2a3040] rounded-xl p-4">
      <h3 class="text-sm font-medium text-gray-300 mb-3">Average Rating Over Time</h3>
      <div class="h-36">
        <canvas ref="avgRatingCanvas"></canvas>
      </div>
    </div>

    <!-- Reply Rate -->
    <div class="bg-[#1a1f2e] border border-[#2a3040] rounded-xl p-4">
      <h3 class="text-sm font-medium text-gray-300 mb-3">Reply Rate</h3>
      <div class="h-36 relative flex items-center justify-center">
        <canvas ref="replyRateCanvas"></canvas>
        <div class="absolute inset-0 flex items-center justify-center pointer-events-none">
          <span class="text-2xl font-bold text-white">{{ replyRateData.pct }}%</span>
        </div>
      </div>
    </div>

    <!-- Reviews by Rating -->
    <div class="bg-[#1a1f2e] border border-[#2a3040] rounded-xl p-4">
      <h3 class="text-sm font-medium text-gray-300 mb-3">Reviews by Rating</h3>
      <div class="h-36">
        <canvas ref="ratingDistCanvas"></canvas>
      </div>
    </div>
  </div>
</template>

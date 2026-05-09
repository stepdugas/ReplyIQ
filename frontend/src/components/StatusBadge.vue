<script setup>
defineProps({
  status: { type: String, required: true },
})

const config = {
  posted: { label: 'Posted', bg: 'bg-emerald-500/15', text: 'text-emerald-400', dot: 'bg-emerald-400' },
  pending: { label: 'Pending', bg: 'bg-amber-500/15', text: 'text-amber-400', dot: 'bg-amber-400' },
  needs_reply: { label: 'Needs Reply', bg: 'bg-blue-500/15', text: 'text-blue-400', dot: 'bg-blue-400' },
  failed: { label: 'Failed', bg: 'bg-red-500/15', text: 'text-red-400', dot: 'bg-red-400' },
}
</script>

<template>
  <span
    :class="[
      'inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium',
      config[status]?.bg || 'bg-gray-500/15',
      config[status]?.text || 'text-gray-400',
    ]"
  >
    <span :class="['w-1.5 h-1.5 rounded-full', config[status]?.dot || 'bg-gray-400']"></span>
    {{ config[status]?.label || status }}
  </span>
</template>

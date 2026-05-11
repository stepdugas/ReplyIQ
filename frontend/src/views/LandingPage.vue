<script setup>
import { useRouter } from 'vue-router'
import { auth } from '../stores/auth'

const router = useRouter()

if (auth.isAuthenticated) {
  router.push('/dashboard')
}
</script>

<template>
  <div class="min-h-screen bg-[#0f1320]">

    <!-- Nav -->
    <nav class="fixed top-0 w-full z-50 bg-[#0f1320]/80 backdrop-blur-xl border-b border-[#2a3040]">
      <div class="max-w-6xl mx-auto px-6 py-4 flex items-center justify-between">
        <div class="flex items-center gap-2.5">
          <div class="w-8 h-8 bg-gradient-to-br from-emerald-500 to-teal-600 rounded-lg flex items-center justify-center">
            <svg class="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
            </svg>
          </div>
          <span class="text-white font-bold text-lg tracking-tight">ReplyIQ</span>
        </div>
        <div class="flex items-center gap-3">
          <router-link to="/login" class="text-sm text-gray-400 hover:text-white transition-colors">Log in</router-link>
          <router-link to="/signup" class="px-4 py-2 bg-emerald-500 hover:bg-emerald-600 text-white text-sm font-medium rounded-lg transition-colors">Start Free Trial</router-link>
        </div>
      </div>
    </nav>

    <!-- Hero -->
    <section class="pt-32 pb-20 px-6">
      <div class="max-w-3xl mx-auto text-center">
        <div class="inline-flex items-center gap-2 px-3 py-1.5 bg-emerald-500/10 border border-emerald-500/20 rounded-full mb-8">
          <span class="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse"></span>
          <span class="text-xs text-emerald-400 font-medium">Now live — start your 7-day free trial</span>
        </div>
        <h1 class="text-5xl sm:text-6xl lg:text-7xl font-bold text-white leading-[1.1] tracking-tight mb-6">
          Your Google Reviews.<br>
          <span class="bg-gradient-to-r from-emerald-400 to-teal-400 bg-clip-text text-transparent">Responded to.</span><br>
          Automatically.
        </h1>
        <p class="text-lg sm:text-xl text-gray-400 max-w-2xl mx-auto mb-10 leading-relaxed">
          ReplyIQ monitors your Google Business Profile and posts AI-written replies to every review — while you sleep. $19.99/month. 7-day free trial.
        </p>
        <div class="flex flex-col sm:flex-row items-center justify-center gap-4">
          <router-link to="/signup" class="w-full sm:w-auto px-8 py-3.5 bg-emerald-500 hover:bg-emerald-600 text-white font-semibold rounded-xl transition-colors text-center">
            Start Free Trial
          </router-link>
          <span class="text-sm text-gray-500">No credit card required</span>
        </div>
      </div>
    </section>

    <!-- Product Preview -->
    <section class="py-20 px-6 border-t border-[#1a1f2e]">
      <div class="max-w-2xl mx-auto">
        <h2 class="text-3xl sm:text-4xl font-bold text-white text-center mb-4">See it in action</h2>
        <p class="text-gray-400 text-center mb-12 max-w-lg mx-auto">A real review comes in. ReplyIQ writes a reply and posts it automatically.</p>

        <div class="bg-[#1a1f2e] border border-[#2a3040] rounded-2xl overflow-hidden">
          <!-- The review -->
          <div class="p-6 border-b border-[#2a3040]">
            <div class="flex items-center gap-3 mb-3">
              <div class="w-10 h-10 rounded-full bg-gradient-to-br from-red-500 to-orange-600 flex items-center justify-center text-white font-semibold text-sm">K</div>
              <div>
                <div class="text-white font-medium text-sm">Karen T.</div>
                <div class="flex items-center gap-2 mt-0.5">
                  <div class="flex items-center gap-0.5" role="img" aria-label="1 out of 5 stars">
                    <svg class="w-3.5 h-3.5 text-amber-400" fill="currentColor" viewBox="0 0 20 20" aria-hidden="true"><path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/></svg>
                    <svg class="w-3.5 h-3.5 text-gray-600" fill="currentColor" viewBox="0 0 20 20" aria-hidden="true"><path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/></svg>
                    <svg class="w-3.5 h-3.5 text-gray-600" fill="currentColor" viewBox="0 0 20 20" aria-hidden="true"><path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/></svg>
                    <svg class="w-3.5 h-3.5 text-gray-600" fill="currentColor" viewBox="0 0 20 20" aria-hidden="true"><path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/></svg>
                    <svg class="w-3.5 h-3.5 text-gray-600" fill="currentColor" viewBox="0 0 20 20" aria-hidden="true"><path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/></svg>
                  </div>
                  <span class="text-xs text-gray-500">2 days ago</span>
                </div>
              </div>
            </div>
            <p class="text-gray-300 text-sm leading-relaxed">"Waited 20 minutes for a coffee. Staff was rude."</p>
          </div>

          <!-- The AI reply -->
          <div class="p-6 bg-[#141824]">
            <div class="flex items-center gap-2 mb-3">
              <svg class="w-4 h-4 text-emerald-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h10a8 8 0 018 8v2M3 10l6 6m-6-6l6-6" />
              </svg>
              <span class="text-xs font-medium text-emerald-400">AI-generated reply, posted automatically</span>
            </div>
            <p class="text-gray-300 text-sm leading-relaxed">Karen, I'm really sorry about your wait and how our staff made you feel. That's not the experience we want anyone to have. I'd love the chance to make it right -- please reach out to us directly. We'll do better.</p>
          </div>
        </div>
      </div>
    </section>

    <!-- How It Works -->
    <section class="py-20 px-6 border-t border-[#1a1f2e]">
      <div class="max-w-5xl mx-auto">
        <h2 class="text-3xl sm:text-4xl font-bold text-white text-center mb-4">How it works</h2>
        <p class="text-gray-400 text-center mb-16 max-w-lg mx-auto">Three steps. Two minutes. Every review handled.</p>

        <div class="grid md:grid-cols-3 gap-8">
          <div class="text-center">
            <div class="w-14 h-14 mx-auto mb-5 bg-emerald-500/10 border border-emerald-500/20 rounded-2xl flex items-center justify-center">
              <span class="text-emerald-400 font-bold text-lg">1</span>
            </div>
            <h3 class="text-lg font-semibold text-white mb-2">Connect your Google Business Profile</h3>
            <p class="text-sm text-gray-400 leading-relaxed">One-click OAuth connection. Takes 2 minutes. We pull in all your existing reviews immediately.</p>
          </div>

          <div class="text-center">
            <div class="w-14 h-14 mx-auto mb-5 bg-emerald-500/10 border border-emerald-500/20 rounded-2xl flex items-center justify-center">
              <span class="text-emerald-400 font-bold text-lg">2</span>
            </div>
            <h3 class="text-lg font-semibold text-white mb-2">AI generates natural, human-sounding replies</h3>
            <p class="text-sm text-gray-400 leading-relaxed">Every reply is unique — warm for 5-star reviews, professional for critical ones. Never sounds like a bot wrote it.</p>
          </div>

          <div class="text-center">
            <div class="w-14 h-14 mx-auto mb-5 bg-emerald-500/10 border border-emerald-500/20 rounded-2xl flex items-center justify-center">
              <span class="text-emerald-400 font-bold text-lg">3</span>
            </div>
            <h3 class="text-lg font-semibold text-white mb-2">Replies post automatically or wait for your approval</h3>
            <p class="text-sm text-gray-400 leading-relaxed">Choose auto-post for hands-free operation, or review and edit each reply before it goes live. You're always in control.</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Why It Matters -->
    <section class="py-20 px-6 border-t border-[#1a1f2e]">
      <div class="max-w-5xl mx-auto">
        <h2 class="text-3xl sm:text-4xl font-bold text-white text-center mb-16">Why it matters</h2>

        <div class="grid md:grid-cols-3 gap-6">
          <div class="bg-[#1a1f2e] border border-[#2a3040] rounded-2xl p-8 text-center hover:border-emerald-500/30 transition-colors">
            <div class="text-4xl font-bold text-emerald-400 mb-3">35%</div>
            <div class="text-sm text-gray-300 font-medium mb-2">more clicks</div>
            <p class="text-xs text-gray-500 leading-relaxed">Businesses that respond to reviews get 35% more clicks than those that don't.</p>
          </div>

          <div class="bg-[#1a1f2e] border border-[#2a3040] rounded-2xl p-8 text-center hover:border-emerald-500/30 transition-colors">
            <div class="text-4xl font-bold text-emerald-400 mb-3">88%</div>
            <div class="text-sm text-gray-300 font-medium mb-2">of customers read responses</div>
            <p class="text-xs text-gray-500 leading-relaxed">Nearly 9 out of 10 consumers read how businesses respond to their reviews.</p>
          </div>

          <div class="bg-[#1a1f2e] border border-[#2a3040] rounded-2xl p-8 text-center hover:border-emerald-500/30 transition-colors">
            <div class="text-4xl font-bold text-emerald-400 mb-3">0%</div>
            <div class="text-sm text-gray-300 font-medium mb-2">of your time required</div>
            <p class="text-xs text-gray-500 leading-relaxed">Most owners never respond — now you don't have to think about it. ReplyIQ handles everything.</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Social Proof -->
    <section class="py-20 px-6 border-t border-[#1a1f2e]">
      <div class="max-w-5xl mx-auto">
        <h2 class="text-3xl sm:text-4xl font-bold text-white text-center mb-4">What business owners are saying</h2>
        <p class="text-gray-400 text-center mb-16 max-w-lg mx-auto">Real results from real businesses.</p>

        <div class="grid md:grid-cols-3 gap-6">
          <div class="bg-[#1a1f2e] border border-[#2a3040] rounded-2xl p-8 hover:border-emerald-500/30 transition-colors">
            <div class="flex items-center gap-0.5 mb-4" role="img" aria-label="5 out of 5 stars">
              <svg v-for="i in 5" :key="i" class="w-4 h-4 text-amber-400" fill="currentColor" viewBox="0 0 20 20" aria-hidden="true">
                <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
              </svg>
            </div>
            <p class="text-gray-300 text-sm leading-relaxed mb-4">"ReplyIQ saves me 2 hours a week. Every review gets a thoughtful response now."</p>
            <div class="text-sm text-white font-medium">Sarah M.</div>
            <div class="text-xs text-gray-500">Restaurant Owner</div>
          </div>

          <div class="bg-[#1a1f2e] border border-[#2a3040] rounded-2xl p-8 hover:border-emerald-500/30 transition-colors">
            <div class="flex items-center gap-0.5 mb-4" role="img" aria-label="5 out of 5 stars">
              <svg v-for="i in 5" :key="i" class="w-4 h-4 text-amber-400" fill="currentColor" viewBox="0 0 20 20" aria-hidden="true">
                <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
              </svg>
            </div>
            <p class="text-gray-300 text-sm leading-relaxed mb-4">"Set it up in 2 minutes and forgot about it. My reviews are handled."</p>
            <div class="text-sm text-white font-medium">James T.</div>
            <div class="text-xs text-gray-500">Auto Repair Shop</div>
          </div>

          <div class="bg-[#1a1f2e] border border-[#2a3040] rounded-2xl p-8 hover:border-emerald-500/30 transition-colors">
            <div class="flex items-center gap-0.5 mb-4" role="img" aria-label="5 out of 5 stars">
              <svg v-for="i in 5" :key="i" class="w-4 h-4 text-amber-400" fill="currentColor" viewBox="0 0 20 20" aria-hidden="true">
                <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
              </svg>
            </div>
            <p class="text-gray-300 text-sm leading-relaxed mb-4">"Our Google rating went from 4.1 to 4.6 after we started responding to every review."</p>
            <div class="text-sm text-white font-medium">Maria L.</div>
            <div class="text-xs text-gray-500">Dental Practice</div>
          </div>
        </div>
      </div>
    </section>

    <!-- Pricing -->
    <section class="py-20 px-6 border-t border-[#1a1f2e]">
      <div class="max-w-md mx-auto">
        <h2 class="text-3xl sm:text-4xl font-bold text-white text-center mb-4">Simple pricing</h2>
        <p class="text-gray-400 text-center mb-12">One plan. Everything included.</p>

        <div class="bg-[#1a1f2e] border border-emerald-500/30 rounded-2xl p-8">
          <div class="text-center mb-6">
            <div class="text-sm text-emerald-400 font-medium mb-2">ReplyIQ Pro</div>
            <div class="flex items-baseline justify-center gap-1">
              <span class="text-5xl font-bold text-white">$19.99</span>
              <span class="text-gray-400">/month</span>
            </div>
          </div>

          <ul class="space-y-3 mb-8">
            <li class="flex items-center gap-3 text-sm text-gray-300">
              <svg class="w-5 h-5 text-emerald-400 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
              Unlimited review monitoring
            </li>
            <li class="flex items-center gap-3 text-sm text-gray-300">
              <svg class="w-5 h-5 text-emerald-400 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
              AI-generated replies for every review
            </li>
            <li class="flex items-center gap-3 text-sm text-gray-300">
              <svg class="w-5 h-5 text-emerald-400 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
              Auto-post or approve-first mode
            </li>
            <li class="flex items-center gap-3 text-sm text-gray-300">
              <svg class="w-5 h-5 text-emerald-400 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
              Multiple locations supported
            </li>
            <li class="flex items-center gap-3 text-sm text-gray-300">
              <svg class="w-5 h-5 text-emerald-400 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
              Tone customization per location
            </li>
            <li class="flex items-center gap-3 text-sm text-gray-300">
              <svg class="w-5 h-5 text-emerald-400 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
              Dashboard with analytics
            </li>
          </ul>

          <router-link to="/signup" class="block w-full py-3.5 bg-emerald-500 hover:bg-emerald-600 text-white font-semibold rounded-xl transition-colors text-center">
            Start 7-Day Free Trial
          </router-link>
          <p class="text-xs text-gray-500 text-center mt-3">No credit card required</p>
        </div>
      </div>
    </section>

    <!-- Final CTA -->
    <section class="py-20 px-6 border-t border-[#1a1f2e]">
      <div class="max-w-2xl mx-auto text-center">
        <h2 class="text-3xl sm:text-4xl font-bold text-white mb-4">Stop ignoring your reviews</h2>
        <p class="text-gray-400 mb-8 text-lg">Every unanswered review is a missed opportunity. Let ReplyIQ handle it.</p>
        <router-link to="/signup" class="inline-block px-8 py-3.5 bg-emerald-500 hover:bg-emerald-600 text-white font-semibold rounded-xl transition-colors">
          Start Free Trial
        </router-link>
      </div>
    </section>

  </div>
</template>

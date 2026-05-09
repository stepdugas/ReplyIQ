import { createRouter, createWebHistory } from 'vue-router'
import { auth } from '../stores/auth'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/HomePage.vue'),
  },
  {
    path: '/onboarding',
    name: 'Onboarding',
    component: () => import('../views/OnboardingPage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../views/DashboardPage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/terms',
    name: 'Terms',
    component: () => import('../views/TermsPage.vue'),
  },
  {
    path: '/privacy',
    name: 'Privacy',
    component: () => import('../views/PrivacyPage.vue'),
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return '/'
  }
})

export default router

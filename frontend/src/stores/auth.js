import { reactive } from 'vue'
import api from '../api'

export const auth = reactive({
  user: JSON.parse(localStorage.getItem('replyiq_user') || 'null'),
  token: localStorage.getItem('replyiq_token') || null,

  get isAuthenticated() {
    return !!this.token
  },

  async signup(name, email, password) {
    const { data } = await api.post('/auth/signup', { name, email, password })
    this.setSession(data)
    return data
  },

  async login(email, password) {
    const { data } = await api.post('/auth/login', { email, password })
    this.setSession(data)
    return data
  },

  setSession(data) {
    this.token = data.token
    this.user = { email: data.email, name: data.name, subscriptionStatus: data.subscriptionStatus }
    localStorage.setItem('replyiq_token', data.token)
    localStorage.setItem('replyiq_user', JSON.stringify(this.user))
  },

  logout() {
    this.token = null
    this.user = null
    localStorage.removeItem('replyiq_token')
    localStorage.removeItem('replyiq_user')
  },
})

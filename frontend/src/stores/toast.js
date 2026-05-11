import { reactive } from 'vue'

export const toast = reactive({
  visible: false,
  message: '',
  type: 'success', // 'success' | 'error'
  _timer: null,

  showSuccess(msg) {
    this._show(msg, 'success')
  },

  showError(msg) {
    this._show(msg, 'error')
  },

  _show(msg, type) {
    clearTimeout(this._timer)
    this.message = msg
    this.type = type
    this.visible = true
    this._timer = setTimeout(() => {
      this.visible = false
    }, 4000)
  },

  dismiss() {
    clearTimeout(this._timer)
    this.visible = false
  },
})

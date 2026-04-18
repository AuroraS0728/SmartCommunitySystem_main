const app = getApp()

const ROLE_OWNER = 1
const ROLE_SECURITY = 2
const ROLE_WORKER = 3

function roleMeta(role) {
  if (role === ROLE_SECURITY) {
    return {
      accountLabel: '安防账号',
      accountPlaceholder: 'WTGL + 6位数字',
      passwordPlaceholder: '6位倒序数字密码',
      tip: '物业安防端规则：账号 WTGL + 6位数字，密码为这6位数字倒序。'
    }
  }
  if (role === ROLE_WORKER) {
    return {
      accountLabel: '维修账号',
      accountPlaceholder: 'JZWX + 6位数字',
      passwordPlaceholder: '6位倒序数字密码',
      tip: '维修端规则：账号 JZWX + 6位数字，密码为这6位数字倒序。'
    }
  }
  return {
    accountLabel: '业主账号',
    accountPlaceholder: 'XQYZ + 6位数字',
    passwordPlaceholder: '6位倒序数字密码',
    tip: '业主端规则：账号 XQYZ + 6位数字，密码为这6位数字倒序。'
  }
}

Page({
  data: {
    role: ROLE_OWNER,
    account: '',
    password: '',
    loading: false,
    accountLabel: '',
    accountPlaceholder: '',
    passwordPlaceholder: '',
    tip: ''
  },

  onLoad() {
    this.syncRoleMeta()
  },

  onShow() {
    if (app.globalData.token) {
      app.goHome()
    }
  },

  chooseOwner() {
    this.switchRole(ROLE_OWNER)
  },

  chooseSecurity() {
    this.switchRole(ROLE_SECURITY)
  },

  chooseWorker() {
    this.switchRole(ROLE_WORKER)
  },

  switchRole(role) {
    if (this.data.role === role) return
    this.setData({ role, account: '', password: '' }, () => this.syncRoleMeta())
  },

  onAccountInput(e) {
    this.setData({ account: (e.detail.value || '').trim() })
  },

  onPasswordInput(e) {
    this.setData({ password: (e.detail.value || '').trim() })
  },

  syncRoleMeta() {
    const meta = roleMeta(this.data.role)
    this.setData({
      accountLabel: meta.accountLabel,
      accountPlaceholder: meta.accountPlaceholder,
      passwordPlaceholder: meta.passwordPlaceholder,
      tip: meta.tip
    })
  },

  submit() {
    const { role, account, password, loading } = this.data
    if (loading) return
    if (!account || !password) {
      wx.showToast({ title: '请输入账号和密码', icon: 'none' })
      return
    }

    this.setData({ loading: true })
    app
      .loginWithAccount(account, password, role)
      .then(() => {
        wx.showToast({ title: '登录成功', icon: 'success' })
        app.goHome()
      })
      .catch((error) => {
        wx.showToast({ title: error?.message || '登录失败', icon: 'none' })
      })
      .finally(() => {
        this.setData({ loading: false })
      })
  }
})

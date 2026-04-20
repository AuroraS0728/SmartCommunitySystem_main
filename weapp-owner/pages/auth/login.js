const app = getApp()

const ROLE_OWNER = 1
const ROLE_SECURITY = 2
const ROLE_WORKER = 3

function roleMeta(role) {
  if (role === ROLE_SECURITY) {
    return {
      accountLabel: '安防账号',
      accountPlaceholder: 'WTGL + 6位数字',
      passwordPlaceholder: '请输入密码',
      tip: '物业安防端账号规则：WTGL + 6位数字。'
    }
  }
  if (role === ROLE_WORKER) {
    return {
      accountLabel: '维修账号',
      accountPlaceholder: 'JZWX + 6位数字',
      passwordPlaceholder: '请输入密码',
      tip: '维修端账号规则：JZWX + 6位数字。'
    }
  }
  return {
    accountLabel: '房产号',
    accountPlaceholder: 'YZ + 楼号2位 + 单元2位 + 房间3位 + 年份后2位',
    passwordPlaceholder: '初始密码：姓名首字母+123456',
    tip: '业主首次登录需修改密码，初始密码规则：姓名首字母+123456。'
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
      return
    }
    const prefill = wx.getStorageSync('prefillOwnerLogin')
    if (prefill?.account && prefill?.password) {
      wx.removeStorageSync('prefillOwnerLogin')
      this.setData({
        role: ROLE_OWNER,
        account: String(prefill.account).toUpperCase(),
        password: String(prefill.password)
      }, () => this.syncRoleMeta())
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
    this.setData({ account: (e.detail.value || '').trim().toUpperCase() })
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

  goOwnerRegister() {
    if (this.data.role !== ROLE_OWNER) return
    wx.navigateTo({ url: '/pages/auth/register' })
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
      .then((data) => {
        if (role === ROLE_OWNER && data?.mustChangePassword) {
          wx.showToast({ title: '请先修改密码', icon: 'none' })
        } else {
          wx.showToast({ title: '登录成功', icon: 'success' })
        }
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

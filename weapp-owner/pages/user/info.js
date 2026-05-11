const { request } = require("../../api/request")

function profileExtraKey(userId) {
  return `owner_profile_extra_${userId || 0}`
}

function formatDate(date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, "0")
  const d = String(date.getDate()).padStart(2, "0")
  return `${y}-${m}-${d}`
}

Page({
  data: {
    user: null,
    nickname: "",
    avatarUrl: "",
    birthday: "",
    birthdayLocked: false,
    hasElderly: 0,
    hasChild: 0,
    hasPet: 0,
    houseArea: "",
    roomCount: "",
    saving: false,
    maxBirthday: formatDate(new Date())
  },

  onShow() {
    this.loadInfo()
  },

  async loadInfo() {
    try {
      const user = await request({ url: "/user/me" })
      const extra = wx.getStorageSync(profileExtraKey(user?.id)) || {}
      this.setData({
        user,
        nickname: user?.nickname || "",
        avatarUrl: extra.avatarUrl || user?.avatarUrl || "",
        birthday: extra.birthday || "",
        birthdayLocked: !!extra.birthday,
        hasElderly: Number(user?.hasElderly || 0),
        hasChild: Number(user?.hasChild || 0),
        hasPet: Number(user?.hasPet || 0),
        houseArea: user?.houseArea || "",
        roomCount: user?.roomCount || ""
      })
    } catch (error) {
      wx.showToast({ title: error?.message || "加载失败", icon: "none" })
    }
  },

  onChooseAvatar() {
    wx.chooseMedia({
      count: 1,
      mediaType: ["image"],
      sourceType: ["album", "camera"],
      success: (res) => {
        const file = res?.tempFiles?.[0]
        if (!file?.tempFilePath) return
        this.setData({ avatarUrl: file.tempFilePath })
      }
    })
  },

  onNicknameInput(e) {
    this.setData({ nickname: e.detail.value || "" })
  },

  onBirthdayChange(e) {
    if (this.data.birthdayLocked) return
    this.setData({ birthday: e.detail.value || "" })
  },

  onToggleProfile(e) {
    const key = e.currentTarget.dataset.key
    if (!key) return
    this.setData({ [key]: e.detail.value ? 1 : 0 })
  },

  onHouseAreaInput(e) {
    this.setData({ houseArea: e.detail.value || "" })
  },

  onRoomCountInput(e) {
    this.setData({ roomCount: e.detail.value || "" })
  },

  async onSave() {
    if (this.data.saving) return
    const user = this.data.user
    if (!user?.id) return
    const nickname = String(this.data.nickname || "").trim()
    if (!nickname) {
      wx.showToast({ title: "请输入昵称", icon: "none" })
      return
    }

    this.setData({ saving: true })
    try {
      await request({
        url: `/user/${user.id}`,
        method: "PUT",
        data: {
          nickname,
          avatarUrl: user.avatarUrl || "",
          phone: user.phone || "",
          role: user.role,
          status: user.status,
          hasElderly: Number(this.data.hasElderly || 0),
          hasChild: Number(this.data.hasChild || 0),
          hasPet: Number(this.data.hasPet || 0),
          houseArea: this.data.houseArea === "" ? null : Number(this.data.houseArea),
          roomCount: this.data.roomCount === "" ? null : Number(this.data.roomCount)
        }
      })
      const extra = {
        avatarUrl: this.data.avatarUrl || "",
        birthday: this.data.birthday || ""
      }
      wx.setStorageSync(profileExtraKey(user.id), extra)

      const app = getApp()
      app.globalData.userInfo = {
        ...(app.globalData.userInfo || {}),
        nickname,
        avatarUrl: this.data.avatarUrl || user.avatarUrl || "",
        hasElderly: Number(this.data.hasElderly || 0),
        hasChild: Number(this.data.hasChild || 0),
        hasPet: Number(this.data.hasPet || 0),
        houseArea: this.data.houseArea === "" ? null : Number(this.data.houseArea),
        roomCount: this.data.roomCount === "" ? null : Number(this.data.roomCount)
      }
      wx.setStorageSync("userInfo", app.globalData.userInfo)
      wx.showToast({ title: "保存成功", icon: "success" })
      this.setData({ birthdayLocked: !!extra.birthday })
    } catch (error) {
      wx.showToast({ title: error?.message || "保存失败", icon: "none" })
    } finally {
      this.setData({ saving: false })
    }
  },

  goSettings() {
    wx.navigateTo({ url: "/pages/user/settings/index" })
  }
})

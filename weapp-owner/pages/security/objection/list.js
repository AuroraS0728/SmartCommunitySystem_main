const { request } = require("../../../api/request")

const STATUS_OPTIONS = [
  { label: "全部", value: "" },
  { label: "待审核", value: "0" },
  { label: "已驳回", value: "1" },
  { label: "已通过", value: "2" }
]

function toNumber(value) {
  if (value === undefined || value === null || value === "") return null
  const n = Number(value)
  return Number.isFinite(n) ? n : null
}

Page({
  data: {
    loading: false,
    reviewing: false,
    list: [],
    statusOptions: STATUS_OPTIONS,
    statusValue: "",
    forms: {}
  },

  onShow() {
    this.loadList()
  },

  async loadList() {
    this.setData({ loading: true })
    try {
      const params = {}
      if (this.data.statusValue !== "") {
        params.status = Number(this.data.statusValue)
      }
      const rawList = await request({
        url: "/repair/fee-objection/list",
        data: params
      })
      const list = Array.isArray(rawList) ? rawList : []
      const forms = {}
      list.forEach((item) => {
        const id = item?.id
        if (id === undefined || id === null) return
        const prev = this.data.forms?.[id] || {}
        forms[id] = {
          adjustedAmount: prev.adjustedAmount || "",
          refundPoints: prev.refundPoints || "",
          remark: prev.remark || ""
        }
      })
      this.setData({ list, forms })
    } catch (error) {
      wx.showToast({ title: error?.message || "加载失败", icon: "none" })
      this.setData({ list: [] })
    } finally {
      this.setData({ loading: false })
    }
  },

  onStatusChange(e) {
    const idx = Number(e?.detail?.value)
    const option = this.data.statusOptions[idx]
    const value = option ? option.value : ""
    this.setData({ statusValue: value }, () => this.loadList())
  },

  onFormInput(e) {
    const id = e.currentTarget.dataset.id
    const field = e.currentTarget.dataset.field
    if (!id || !field) return
    const value = e.detail.value
    this.setData({
      [`forms.${id}.${field}`]: value
    })
  },

  async review(e) {
    if (this.data.reviewing) return
    const id = e.currentTarget.dataset.id
    const status = Number(e.currentTarget.dataset.status)
    if (!id || ![1, 2].includes(status)) return

    const form = this.data.forms?.[id] || {}
    const adjustedAmount = toNumber(form.adjustedAmount)
    const refundPoints = toNumber(form.refundPoints)
    if (form.adjustedAmount && adjustedAmount === null) {
      wx.showToast({ title: "调整金额格式不正确", icon: "none" })
      return
    }
    if (form.refundPoints && (refundPoints === null || refundPoints < 0)) {
      wx.showToast({ title: "退还积分格式不正确", icon: "none" })
      return
    }

    const payload = {
      status,
      remark: (form.remark || "").trim()
    }
    if (adjustedAmount !== null) {
      payload.adjustedAmount = adjustedAmount
    }
    if (refundPoints !== null) {
      payload.refundPoints = Math.floor(refundPoints)
    }

    this.setData({ reviewing: true })
    try {
      await request({
        url: `/repair/fee-objection/${id}/review`,
        method: "POST",
        data: payload
      })
      wx.showToast({ title: "审核完成", icon: "success" })
      await this.loadList()
    } catch (error) {
      wx.showToast({ title: error?.message || "审核失败", icon: "none" })
    } finally {
      this.setData({ reviewing: false })
    }
  }
})

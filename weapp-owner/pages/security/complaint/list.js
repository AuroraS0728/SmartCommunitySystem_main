const { request } = require("../../../api/request")

const STATUS_OPTIONS = [
  { label: "全部", value: "" },
  { label: "待回复", value: "1" },
  { label: "已回复", value: "3" }
]

function statusText(status) {
  const map = {
    1: "待回复",
    2: "处理中",
    3: "已回复"
  }
  return map[Number(status)] || "未知"
}

function riskText(level) {
  const map = {
    HIGH: "高风险",
    MID: "中风险",
    LOW: "低风险"
  }
  return map[level] || "未分析"
}

function sentimentText(label) {
  const map = {
    POSITIVE: "正向",
    NEGATIVE: "负向",
    NEUTRAL: "中性"
  }
  return map[label] || "未分析"
}

function riskClass(level) {
  const map = {
    HIGH: "risk-high",
    MID: "risk-mid",
    LOW: "risk-low"
  }
  return map[level] || "risk-empty"
}

function sentimentClass(label) {
  const map = {
    POSITIVE: "sentiment-positive",
    NEGATIVE: "sentiment-negative",
    NEUTRAL: "sentiment-neutral"
  }
  return map[label] || "sentiment-empty"
}

function scoreText(score) {
  return score === undefined || score === null || score === "" ? "--" : String(score)
}

Page({
  data: {
    loading: false,
    replying: false,
    list: [],
    statusOptions: STATUS_OPTIONS,
    statusValue: "",
    replyDrafts: {}
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
        url: "/complaint/list",
        data: params
      })
      const list = (Array.isArray(rawList) ? rawList : []).map((item) => ({
        ...item,
        statusText: statusText(item.status),
        riskText: riskText(item.riskLevel),
        riskClass: riskClass(item.riskLevel),
        sentimentText: sentimentText(item.sentimentLabel),
        sentimentClass: sentimentClass(item.sentimentLabel),
        sentimentScoreText: scoreText(item.sentimentScore),
        analyzedAtText: item.analyzedAt || "未分析"
      }))
      const replyDrafts = { ...this.data.replyDrafts }
      list.forEach((item) => {
        if (item?.id === undefined || item?.id === null) return
        if (replyDrafts[item.id] === undefined) {
          replyDrafts[item.id] = ""
        }
      })
      this.setData({ list, replyDrafts })
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

  onReplyInput(e) {
    const id = e.currentTarget.dataset.id
    if (!id) return
    this.setData({
      [`replyDrafts.${id}`]: e.detail.value
    })
  },

  async submitReply(e) {
    if (this.data.replying) return
    const id = e.currentTarget.dataset.id
    if (!id) return
    const reply = (this.data.replyDrafts?.[id] || "").trim()
    if (!reply) {
      wx.showToast({ title: "请输入回复内容", icon: "none" })
      return
    }

    this.setData({ replying: true })
    try {
      await request({
        url: `/complaint/${id}/reply`,
        method: "POST",
        data: { reply }
      })
      wx.showToast({ title: "回复成功", icon: "success" })
      this.setData({ [`replyDrafts.${id}`]: "" })
      await this.loadList()
    } catch (error) {
      wx.showToast({ title: error?.message || "回复失败", icon: "none" })
    } finally {
      this.setData({ replying: false })
    }
  }
})

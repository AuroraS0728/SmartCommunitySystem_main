const { request } = require("../../../api/request")

function buildMessages(list) {
  const all = Array.isArray(list) ? list : []
  const countByStatus = all.reduce((acc, item) => {
    const key = Number(item?.status || 0)
    acc[key] = (acc[key] || 0) + 1
    return acc
  }, {})

  const result = []
  const waitVisit = countByStatus[1] || 0
  const inService = countByStatus[2] || 0
  const waitEvaluate = countByStatus[3] || 0

  if (waitVisit > 0) {
    result.push({
      id: "new-order",
      title: "新工单通知",
      desc: `当前有 ${waitVisit} 条待上门工单，请尽快处理`,
      status: 1
    })
  }
  if (inService > 0) {
    result.push({
      id: "in-service",
      title: "催单提醒",
      desc: `当前有 ${inService} 条服务中工单，请及时完工`,
      status: 2
    })
  }
  if (waitEvaluate > 0) {
    result.push({
      id: "wait-eval",
      title: "评价反馈",
      desc: `当前有 ${waitEvaluate} 条工单待业主评价`,
      status: 3
    })
  }

  if (!result.length) {
    result.push({
      id: "empty",
      title: "暂无新消息",
      desc: "当前没有待处理提醒，可前往工单列表查看全部工单",
      status: 0
    })
  }
  return result
}

Page({
  data: {
    loading: false,
    workerId: null,
    messages: []
  },

  onShow() {
    this.loadMessages()
  },

  async loadMessages() {
    this.setData({ loading: true })
    try {
      const user = await request({ url: "/user/me" })
      const workerId = user?.id || null
      if (!workerId) {
        throw new Error("未获取到维修人员身份")
      }
      const list = await request({
        url: "/worker/tasks",
        data: { workerId }
      })
      this.setData({
        workerId,
        messages: buildMessages(list)
      })
    } catch (error) {
      wx.showToast({ title: error?.message || "加载失败", icon: "none" })
      this.setData({
        messages: [{
          id: "error",
          title: "消息加载失败",
          desc: "请稍后重试",
          status: 0
        }]
      })
    } finally {
      this.setData({ loading: false })
    }
  },

  openTaskList(e) {
    const status = Number(e?.currentTarget?.dataset?.status || 0)
    if (status > 0) {
      wx.navigateTo({ url: `/pages/worker/task/list?status=${status}` })
      return
    }
    wx.navigateTo({ url: "/pages/worker/task/list" })
  }
})

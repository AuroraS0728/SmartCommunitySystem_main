const { request } = require("../../api/request")

function parseImages(value) {
  if (!value) return []
  if (Array.isArray(value)) return value

  const text = String(value).trim()
  if (!text) return []

  try {
    const parsed = JSON.parse(text)
    if (Array.isArray(parsed)) return parsed
  } catch (e) {}

  if (text.includes(",")) {
    return text
      .split(",")
      .map((item) => item.trim())
      .filter(Boolean)
  }

  return [text]
}

function statusText(status) {
  const map = {
    1: "待派单/待上门验证",
    2: "服务中",
    3: "待评价",
    4: "已完成",
    5: "已取消"
  }
  return map[Number(status)] || "未知状态"
}

function statusClass(status) {
  const map = {
    1: "status-warning",
    2: "status-warning",
    3: "status-warning",
    4: "status-success",
    5: "status-danger"
  }
  return map[Number(status)] || "status-warning"
}

Page({
  data: {
    id: null,
    detail: {
      participants: [],
      pendingWorkerIds: [],
      objections: [],
      feeDetails: [],
      beforeImages: [],
      afterImages: []
    },
    loading: false,
    loadingVerifyCode: false,
    payingRepairFee: false,
    evaluating: false,
    confirmingFinish: false,
    rating: 5,
    comment: "",
    objectionReason: "",
    objectionDepositPoints: 20,
    submittingObjection: false
  },

  onLoad(query) {
    const id = Number(query?.id)
    if (!id) {
      wx.showToast({
        title: "工单ID无效",
        icon: "none"
      })
      return
    }

    this.setData({ id })
    this.loadDetail()
  },

  onShow() {
    if (this.data.id) {
      this.loadDetail()
    }
  },

  async loadDetail() {
    if (!this.data.id) return
    this.setData({ loading: true })

    try {
      const detail = (await request({ url: `/repair/${this.data.id}` })) || {}
      const order = detail?.order || {}
      const bill = detail?.repairFeeBill || null
      const feeDetails = Array.isArray(detail?.repairFeeDetails)
        ? detail.repairFeeDetails
        : []
      const feeTotal = feeDetails.reduce(
        (sum, item) => sum + Number(item?.totalAmount || 0),
        0
      )

      const participants = Array.isArray(detail?.participants)
        ? detail.participants
        : []
      const pendingWorkerIds = Array.isArray(detail?.pendingWorkerIds)
        ? detail.pendingWorkerIds
        : []
      const objections = Array.isArray(detail?.feeObjections)
        ? detail.feeObjections
        : []

      const beforeImages = parseImages(order.beforeImages)
      const afterImages = parseImages(order.afterImages)
      const orderStatus = Number(order.status)
      const hasAssignee = !!order.assignee
      const canFetchVerifyCode =
        [1, 2].includes(orderStatus) &&
        hasAssignee

      const workerFinishConfirmed =
        detail?.workerFinishConfirmed === true ||
        Number(detail?.workerFinishConfirmed || 0) === 1

      const ownerFinishConfirmed =
        detail?.ownerFinishConfirmed === true ||
        Number(detail?.ownerFinishConfirmed || 0) === 1

      const hasEvaluation = !!detail?.evaluation

      const canOwnerFinish =
        workerFinishConfirmed &&
        !ownerFinishConfirmed &&
        orderStatus === 2

      const canEvaluate =
        !hasEvaluation &&
        (detail?.canEvaluate === true || orderStatus === 3)

      this.setData({
        detail: {
          ...detail,
          beforeImages,
          afterImages,
          statusText: detail?.statusText || statusText(order.status),
          statusClass: statusClass(order.status),
          canShowVerifyCode: canFetchVerifyCode,
          canShowVerifyCodePending: orderStatus === 1 && !hasAssignee,
          verifyCodeHint: canFetchVerifyCode
            ? "验证码用于维修人员上门核验，请勿提前泄露给无关人员"
            : "工单派单后会生成上门验证码",
          priorityText: this.priorityText(order.priority),
          repairFeeBillStatusText: bill
            ? Number(bill.status) === 1
              ? "已支付"
              : "未支付"
            : "",
          repairFeeNeedPoints: bill
            ? Number(bill.needPoints || Math.ceil(Number(bill.amount || 0)))
            : 0,
          participants,
          pendingWorkerIds,
          feeDetails,
          feeTotal,
          objections,
          workerFinishConfirmed,
          ownerFinishConfirmed,
          canOwnerFinish,
          canEvaluate
        },
        rating: detail?.evaluation?.rating || 5,
        comment: detail?.evaluation?.comment || ""
      })

      if (canFetchVerifyCode && !detail?.verifyCode) {
        await this.fetchVerifyCode()
      }
    } catch (error) {
      wx.showToast({
        title: error?.message || "加载失败",
        icon: "none"
      })
      this.setData({
        detail: {
          participants: [],
          pendingWorkerIds: [],
          objections: [],
          feeDetails: [],
          beforeImages: [],
          afterImages: []
        }
      })
    } finally {
      this.setData({ loading: false })
    }
  },

  async fetchVerifyCode() {
    if (!this.data.id) return

    this.setData({ loadingVerifyCode: true })
    try {
      const resp = await request({
        url: `/repair/${this.data.id}/verify-code`
      })

      this.setData({
        detail: {
          ...this.data.detail,
          verifyCode: resp?.verifyCode || this.data.detail.verifyCode,
          statusText: resp?.statusText || this.data.detail.statusText
        }
      })
    } catch (error) {
      // 本地开发或 Redis 未启动时，不阻断详情页显示
    } finally {
      this.setData({ loadingVerifyCode: false })
    }
  },

  onRatingTap(e) {
    const rating = Number(e.currentTarget.dataset.value)
    if (!rating) return
    this.setData({ rating })
  },

  onCommentInput(e) {
    this.setData({
      comment: e.detail.value
    })
  },

  onObjectionReasonInput(e) {
    this.setData({
      objectionReason: e.detail.value
    })
  },

  onObjectionDepositInput(e) {
    this.setData({
      objectionDepositPoints: e.detail.value
    })
  },

  priorityText(priority) {
    const map = {
      1: "紧急",
      2: "普通",
      3: "低"
    }
    return map[Number(priority)] || "--"
  },

  async payRepairFee() {
    const bill = this.data.detail?.repairFeeBill
    if (!bill || Number(bill.status) === 1 || this.data.payingRepairFee) return

    const needPoints = Number(this.data.detail?.repairFeeNeedPoints || 0)

    const confirmed = await new Promise((resolve) => {
      wx.showModal({
        title: "确认积分支付",
        content: `确认使用 ${needPoints} 积分支付维修费用吗？`,
        success: ({ confirm }) => resolve(!!confirm),
        fail: () => resolve(false)
      })
    })

    if (!confirmed) return

    this.setData({ payingRepairFee: true })

    try {
      await request({
        url: "/points/consume",
        method: "POST",
        data: {
          businessType: 3,
          businessId: bill.id
        }
      })

      wx.showToast({
        title: "维修费用支付成功",
        icon: "success"
      })

      await this.loadDetail()
    } catch (error) {
      wx.showToast({
        title: error?.message || "支付失败",
        icon: "none"
      })
    } finally {
      this.setData({ payingRepairFee: false })
    }
  },

  async submitFeeObjection() {
    if (this.data.submittingObjection) return

    const reason = String(this.data.objectionReason || "").trim()
    if (!reason) {
      wx.showToast({
        title: "请先填写异议理由",
        icon: "none"
      })
      return
    }

    const depositPoints = Number(this.data.objectionDepositPoints || 0)
    if (!Number.isFinite(depositPoints) || depositPoints <= 0) {
      wx.showToast({
        title: "异议保证金积分不正确",
        icon: "none"
      })
      return
    }

    this.setData({ submittingObjection: true })

    try {
      await request({
        url: `/repair/${this.data.id}/fee-objection`,
        method: "POST",
        data: {
          reason,
          depositPoints
        }
      })

      wx.showToast({
        title: "已提交费用异议",
        icon: "success"
      })

      this.setData({
        objectionReason: ""
      })

      await this.loadDetail()
    } catch (error) {
      wx.showToast({
        title: error?.message || "提交异议失败",
        icon: "none"
      })
    } finally {
      this.setData({ submittingObjection: false })
    }
  },

  async confirmFinish() {
    if (!this.data.id || this.data.confirmingFinish) return

    if (!this.data.detail?.workerFinishConfirmed) {
      wx.showToast({
        title: "请等待工人提交完工信息",
        icon: "none"
      })
      return
    }

    const confirmed = await new Promise((resolve) => {
      wx.showModal({
        title: "确认服务结束",
        content: "请确认服务内容、服务图片和维修费用无误。确认后工单将进入评价流程。",
        success: (res) => resolve(!!res.confirm),
        fail: () => resolve(false)
      })
    })

    if (!confirmed) return

    this.setData({ confirmingFinish: true })

    try {
      await request({
        url: "/repair/status",
        method: "POST",
        data: {
          orderId: this.data.id,
          status: 3,
          remark: "owner confirmed finish"
        }
      })

      wx.showToast({
        title: "已确认结束",
        icon: "success"
      })

      await this.loadDetail()
    } catch (error) {
      wx.showToast({
        title: error?.message || "确认失败",
        icon: "none"
      })
    } finally {
      this.setData({ confirmingFinish: false })
    }
  },

  async submitEvaluation() {
    if (this.data.evaluating || !this.data.id) return

    if (this.data.rating < 1 || this.data.rating > 5) {
      wx.showToast({
        title: "请选择1-5星",
        icon: "none"
      })
      return
    }

    this.setData({ evaluating: true })

    try {
      await request({
        url: `/repair/${this.data.id}/evaluate`,
        method: "POST",
        data: {
          rating: this.data.rating,
          comment: this.data.comment,
          anonymous: 0
        }
      })

      wx.showToast({
        title: "评价成功",
        icon: "success"
      })

      await this.loadDetail()
    } catch (error) {
      wx.showToast({
        title: error?.message || "评价失败",
        icon: "none"
      })
    } finally {
      this.setData({ evaluating: false })
    }
  }
})

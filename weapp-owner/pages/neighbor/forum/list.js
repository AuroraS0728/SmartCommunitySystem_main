const { request } = require("../../../api/request")

const BOARD_OPTIONS = [
  { label: "全部", value: "" },
  { label: "闲聊", value: "chat" },
  { label: "求助", value: "help" },
  { label: "活动", value: "activity" },
  { label: "分享", value: "share" }
]

const SORT_OPTIONS = [
  { label: "最新发布", value: "" },
  { label: "热门优先", value: "hot" }
]

const BOARD_LABEL_MAP = {
  chat: "闲聊",
  help: "求助",
  activity: "活动",
  share: "分享"
}

function formatTime(value) {
  if (!value) return "--"
  const text = String(value).replace("T", " ")
  return text.length > 16 ? text.slice(0, 16) : text
}

function boardText(board) {
  if (!board) return "闲聊"
  return BOARD_LABEL_MAP[board] || board
}

Page({
  data: {
    boardOptions: BOARD_OPTIONS,
    sortOptions: SORT_OPTIONS,
    board: "",
    sortBy: "",
    sortLabel: "最新发布",
    keyword: "",
    mine: false,
    page: 1,
    size: 10,
    total: 0,
    list: [],
    loading: false,
    hasMore: true
  },

  onShow() {
    this.reload()
  },

  onPullDownRefresh() {
    this.reload().finally(() => wx.stopPullDownRefresh())
  },

  onReachBottom() {
    this.loadList()
  },

  onKeywordInput(e) {
    this.setData({ keyword: (e.detail.value || "").trim() })
  },

  onSearch() {
    this.reload()
  },

  onBoardTap(e) {
    const value = e.currentTarget.dataset.value
    if (String(value) === String(this.data.board)) return
    this.setData({ board: value }, () => this.reload())
  },

  onMineSwitch(e) {
    const mine = !!e.detail.value
    this.setData({ mine }, () => this.reload())
  },

  onSortTap() {
    wx.showActionSheet({
      itemList: SORT_OPTIONS.map((item) => item.label),
      success: ({ tapIndex }) => {
        const target = SORT_OPTIONS[tapIndex] || SORT_OPTIONS[0]
        this.setData(
          {
            sortBy: target.value,
            sortLabel: target.label
          },
          () => this.reload()
        )
      }
    })
  },

  goPost() {
    wx.navigateTo({ url: "/pages/neighbor/forum/post" })
  },

  goDetail(e) {
    const id = Number(e.currentTarget.dataset.id || 0)
    if (!id) return
    wx.navigateTo({ url: `/pages/neighbor/forum/detail?id=${id}` })
  },

  reload() {
    this.setData({
      page: 1,
      total: 0,
      list: [],
      hasMore: true
    })
    return this.loadList(true)
  },

  async loadList(force = false) {
    if (this.data.loading) return
    if (!force && !this.data.hasMore) return
    this.setData({ loading: true })
    try {
      const data = await request({
        url: "/neighbor/forum/list",
        data: {
          page: this.data.page,
          size: this.data.size,
          board: this.data.board || undefined,
          keyword: this.data.keyword || undefined,
          sortBy: this.data.sortBy || undefined,
          mine: this.data.mine
        }
      })
      const items = Array.isArray(data?.items) ? data.items : []
      const mapped = items.map((item) => ({
        ...item,
        boardText: boardText(item.board),
        timeText: formatTime(item.updateTime || item.createTime),
        preview: (item.content || "").slice(0, 80)
      }))
      const list = this.data.list.concat(mapped)
      const total = Number(data?.total || 0)
      const page = Number(data?.page || this.data.page)
      this.setData({
        list,
        total,
        page: page + 1,
        hasMore: list.length < total
      })
    } catch (error) {
      wx.showToast({ title: error?.message || "加载失败", icon: "none" })
    } finally {
      this.setData({ loading: false })
    }
  }
})

const { request } = require("../../api/request")

Page({
  data: {
    categoryOptions: ["水电", "门窗", "家电", "下水道", "公共设施", "家政保洁", "其他"],
    categoryMode: "preset",
    selectedCategory: "水电",
    customCategory: "",
    showCategoryPanel: false,
    description: "",
    submitting: false
  },
  toggleCategoryPanel() {
    this.setData({ showCategoryPanel: !this.data.showCategoryPanel })
  },
  selectPresetCategory(e) {
    const selectedCategory = e.currentTarget.dataset.item
    this.setData({
      categoryMode: "preset",
      selectedCategory,
      showCategoryPanel: false
    })
  },
  switchCustomCategory() {
    this.setData({
      categoryMode: "custom",
      showCategoryPanel: false
    })
  },
  onCustomCategory(e) {
    this.setData({ customCategory: e.detail.value })
  },
  onDesc(e) {
    this.setData({ description: e.detail.value })
  },
  getCategory() {
    if (this.data.categoryMode === "custom") {
      return (this.data.customCategory || "").trim()
    }
    return (this.data.selectedCategory || "").trim()
  },
  async submitRepair() {
    if (this.data.submitting) return

    const category = this.getCategory()
    const description = (this.data.description || "").trim()
    if (!category || !description) {
      wx.showToast({ title: "请填写完整报修信息", icon: "none" })
      return
    }

    this.setData({ submitting: true })
    try {
      await request({
        url: "/repair/submit",
        method: "POST",
        data: { propertyId: 101, category, description, images: "[]" }
      })
      wx.showToast({ title: "提交成功" })
      this.setData({
        description: "",
        showCategoryPanel: false
      })
    } catch (error) {
      wx.showToast({ title: error?.message || "提交失败", icon: "none" })
    } finally {
      this.setData({ submitting: false })
    }
  }
})

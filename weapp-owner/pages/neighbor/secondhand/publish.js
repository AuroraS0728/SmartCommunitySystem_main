const { request, uploadFile, resolveAssetUrl } = require("../../../api/request")

const CATEGORIES = [
  { label: "家电", value: "appliance" },
  { label: "家具", value: "furniture" },
  { label: "母婴", value: "baby" },
  { label: "图书", value: "book" },
  { label: "数码", value: "digital" },
  { label: "其他", value: "other" }
]

function parseImages(images) {
  if (!images) return []
  if (Array.isArray(images)) return images
  try {
    const parsed = JSON.parse(images)
    return Array.isArray(parsed) ? parsed : []
  } catch (error) {
    return typeof images === "string" ? [images] : []
  }
}

Page({
  data: {
    id: null,
    categories: CATEGORIES,
    category: "other",
    title: "",
    description: "",
    price: "",
    contact: "",
    imageList: [],
    submitting: false
  },

  onLoad(options) {
    const id = Number(options?.id || 0) || null
    this.setData({ id })
    if (id) this.loadDetail(id)
  },

  async loadDetail(id) {
    try {
      const data = await request({ url: `/neighbor/second-hand/${id}` })
      const item = data?.item || {}
      this.setData({
        title: item.title || "",
        description: item.description || "",
        category: item.category || "other",
        price: item.price === null || item.price === undefined ? "" : String(item.price),
        contact: item.contact || "",
        imageList: parseImages(item.images).map((url) => ({ url, displayUrl: resolveAssetUrl(url), uploaded: true }))
      })
    } catch (error) {
      wx.showToast({ title: error?.message || "加载失败", icon: "none" })
    }
  },

  onTitleInput(e) {
    this.setData({ title: e.detail.value || "" })
  },

  onDescInput(e) {
    this.setData({ description: e.detail.value || "" })
  },

  onPriceInput(e) {
    const value = (e.detail.value || "").replace(/[^\d.]/g, "")
    this.setData({ price: value })
  },

  onContactInput(e) {
    this.setData({ contact: e.detail.value || "" })
  },

  onCategoryTap(e) {
    const value = e.currentTarget.dataset.value
    if (!value || value === this.data.category) return
    this.setData({ category: value })
  },

  async chooseImage() {
    if (this.data.imageList.length >= 3) {
      wx.showToast({ title: "最多上传3张图片", icon: "none" })
      return
    }
    try {
      const media = await wx.chooseMedia({
        count: 3 - this.data.imageList.length,
        mediaType: ["image"],
        sourceType: ["camera", "album"]
      })
      const files = media?.tempFiles || []
      const picked = files
        .filter((file) => file?.tempFilePath)
        .map((file) => ({ path: file.tempFilePath, displayUrl: file.tempFilePath, uploaded: false }))
      this.setData({ imageList: this.data.imageList.concat(picked).slice(0, 3) })
    } catch (error) {
      if (error?.errMsg?.includes("cancel")) return
      wx.showToast({ title: error?.message || "选择图片失败", icon: "none" })
    }
  },

  removeImage(e) {
    const index = Number(e.currentTarget.dataset.index)
    if (Number.isNaN(index)) return
    this.setData({ imageList: this.data.imageList.filter((_, i) => i !== index) })
  },

  previewImage(e) {
    const current = e.currentTarget.dataset.url
    const urls = this.data.imageList.map((item) => item.displayUrl)
    if (!current || !urls.length) return
    wx.previewImage({ current, urls })
  },

  async uploadImages() {
    const urls = []
    for (const item of this.data.imageList) {
      if (item.uploaded && item.url) {
        urls.push(item.url)
      } else if (item.path) {
        urls.push(await uploadFile({ url: "/neighbor/second-hand/upload", filePath: item.path }))
      }
    }
    return urls
  },

  async onSubmit() {
    if (this.data.submitting) return
    const title = (this.data.title || "").trim()
    const description = (this.data.description || "").trim()
    const contact = (this.data.contact || "").trim()
    const price = this.data.price === "" ? 0 : Number(this.data.price)
    if (!title) return wx.showToast({ title: "请输入标题", icon: "none" })
    if (!description) return wx.showToast({ title: "请输入商品描述", icon: "none" })
    if (Number.isNaN(price) || price < 0) return wx.showToast({ title: "价格格式不正确", icon: "none" })
    if (!contact) return wx.showToast({ title: "请填写联系方式", icon: "none" })

    this.setData({ submitting: true })
    try {
      const imageUrls = await this.uploadImages()
      const payload = {
        title,
        category: this.data.category || "other",
        description,
        price,
        contact,
        images: JSON.stringify(imageUrls)
      }
      await request({
        url: this.data.id ? `/neighbor/second-hand/${this.data.id}` : "/neighbor/second-hand/publish",
        method: this.data.id ? "PUT" : "POST",
        data: payload
      })
      wx.showToast({ title: this.data.id ? "提交成功" : "发布成功", icon: "success" })
      setTimeout(() => wx.redirectTo({ url: "/pages/neighbor/secondhand/list" }), 320)
    } catch (error) {
      wx.showToast({ title: error?.message || "提交失败", icon: "none" })
    } finally {
      this.setData({ submitting: false })
    }
  }
})

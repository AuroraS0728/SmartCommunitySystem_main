const { request } = require("../../../api/request")

const CATEGORIES = [
  { label: "家电", value: "appliance" },
  { label: "家具", value: "furniture" },
  { label: "母婴", value: "baby" },
  { label: "图书", value: "book" },
  { label: "数码", value: "digital" },
  { label: "其他", value: "other" }
]

function readFileBase64(path) {
  return new Promise((resolve, reject) => {
    wx.getFileSystemManager().readFile({
      filePath: path,
      encoding: "base64",
      success: (res) => resolve(res.data),
      fail: () => reject(new Error("图片读取失败"))
    })
  })
}

Page({
  data: {
    categories: CATEGORIES,
    category: "other",
    title: "",
    description: "",
    price: "",
    contact: "",
    imageList: [],
    submitting: false
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
      wx.showToast({ title: "最多上传 3 张图片", icon: "none" })
      return
    }
    try {
      const media = await wx.chooseMedia({
        count: 3 - this.data.imageList.length,
        mediaType: ["image"],
        sourceType: ["camera", "album"]
      })
      const files = media?.tempFiles || []
      if (!files.length) return
      const encoded = []
      for (const file of files) {
        if (!file?.tempFilePath) continue
        const base64 = await readFileBase64(file.tempFilePath)
        encoded.push(`data:image/jpeg;base64,${base64}`)
      }
      const imageList = this.data.imageList.concat(encoded).slice(0, 3)
      this.setData({ imageList })
    } catch (error) {
      if (error?.errMsg?.includes("cancel")) return
      wx.showToast({ title: error?.message || "选择图片失败", icon: "none" })
    }
  },

  removeImage(e) {
    const index = Number(e.currentTarget.dataset.index)
    if (Number.isNaN(index)) return
    const imageList = this.data.imageList.filter((_, i) => i !== index)
    this.setData({ imageList })
  },

  previewImage(e) {
    const current = e.currentTarget.dataset.url
    if (!current) return
    wx.previewImage({
      current,
      urls: this.data.imageList
    })
  },

  async onSubmit() {
    if (this.data.submitting) return
    const title = (this.data.title || "").trim()
    const description = (this.data.description || "").trim()
    const contact = (this.data.contact || "").trim()
    const price = this.data.price === "" ? 0 : Number(this.data.price)

    if (!title) {
      wx.showToast({ title: "请输入标题", icon: "none" })
      return
    }
    if (title.length > 60) {
      wx.showToast({ title: "标题最多 60 个字", icon: "none" })
      return
    }
    if (!description) {
      wx.showToast({ title: "请输入商品描述", icon: "none" })
      return
    }
    if (Number.isNaN(price) || price < 0) {
      wx.showToast({ title: "价格格式不正确", icon: "none" })
      return
    }
    if (!contact) {
      wx.showToast({ title: "请填写联系方式", icon: "none" })
      return
    }

    this.setData({ submitting: true })
    try {
      await request({
        url: "/neighbor/second-hand/publish",
        method: "POST",
        data: {
          title,
          category: this.data.category || "other",
          description,
          price,
          contact,
          images: JSON.stringify(this.data.imageList)
        }
      })
      wx.showToast({ title: "发布成功", icon: "success" })
      setTimeout(() => {
        wx.redirectTo({ url: "/pages/neighbor/secondhand/list" })
      }, 320)
    } catch (error) {
      wx.showToast({ title: error?.message || "发布失败", icon: "none" })
    } finally {
      this.setData({ submitting: false })
    }
  }
})

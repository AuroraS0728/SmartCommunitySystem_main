const { request } = require("../../../api/request")

const TYPE_OPTIONS = [
  { label: "失物", value: 1 },
  { label: "招领", value: 2 }
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
    typeOptions: TYPE_OPTIONS,
    type: 1,
    title: "",
    description: "",
    location: "",
    contact: "",
    imageList: [],
    submitting: false
  },

  onTypeTap(e) {
    const value = Number(e.currentTarget.dataset.value || 1)
    this.setData({ type: value })
  },

  onTitleInput(e) {
    this.setData({ title: e.detail.value || "" })
  },

  onDescInput(e) {
    this.setData({ description: e.detail.value || "" })
  },

  onLocationInput(e) {
    this.setData({ location: e.detail.value || "" })
  },

  onContactInput(e) {
    this.setData({ contact: e.detail.value || "" })
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
      this.setData({ imageList: this.data.imageList.concat(encoded).slice(0, 3) })
    } catch (error) {
      if (error?.errMsg?.includes("cancel")) return
      wx.showToast({ title: error?.message || "选择图片失败", icon: "none" })
    }
  },

  removeImage(e) {
    const index = Number(e.currentTarget.dataset.index)
    if (Number.isNaN(index)) return
    this.setData({
      imageList: this.data.imageList.filter((_, i) => i !== index)
    })
  },

  previewImage(e) {
    const current = e.currentTarget.dataset.url
    if (!current) return
    wx.previewImage({ current, urls: this.data.imageList })
  },

  async onSubmit() {
    if (this.data.submitting) return
    const title = (this.data.title || "").trim()
    const description = (this.data.description || "").trim()
    const location = (this.data.location || "").trim()
    const contact = (this.data.contact || "").trim()

    if (!title) {
      wx.showToast({ title: "请输入标题", icon: "none" })
      return
    }
    if (!description) {
      wx.showToast({ title: "请输入描述", icon: "none" })
      return
    }

    this.setData({ submitting: true })
    try {
      await request({
        url: "/neighbor/lost-found/publish",
        method: "POST",
        data: {
          type: Number(this.data.type || 1),
          title,
          description,
          location,
          contact,
          images: JSON.stringify(this.data.imageList)
        }
      })
      wx.showToast({ title: "发布成功", icon: "success" })
      setTimeout(() => {
        wx.redirectTo({ url: "/pages/neighbor/lostfound/list" })
      }, 320)
    } catch (error) {
      wx.showToast({ title: error?.message || "发布失败", icon: "none" })
    } finally {
      this.setData({ submitting: false })
    }
  }
})

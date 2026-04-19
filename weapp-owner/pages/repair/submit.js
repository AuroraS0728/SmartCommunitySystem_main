const { request } = require("../../api/request")

const REPAIR_CATALOG = {
  水电维修: ["水管漏水", "水龙头更换", "马桶疏通", "下水道堵塞", "热水器故障", "电路跳闸", "插座损坏", "灯具维修", "线路老化更换"],
  家电维修: ["空调不制冷/漏水", "冰箱不制冷/结冰", "洗衣机不转/漏水", "电视机花屏/无信号", "油烟机故障", "微波炉/烤箱不工作", "净水器故障"],
  房屋结构: ["墙面开裂/脱落", "地面瓷砖空鼓", "吊顶损坏", "门窗变形/关不严", "防水层渗漏", "阳台栏杆松动"],
  家具维修: ["柜门铰链松动", "抽屉轨道卡顿", "沙发塌陷/皮面破损", "床架异响", "桌椅腿损坏"],
  智能设备: ["智能门锁故障", "智能猫眼/摄像头离线", "智能窗帘电机不转", "智能灯带不亮", "网关离线"],
  其他: ["开锁服务", "空调加氟", "地暖清洗", "燃气灶点火故障"]
}

const HOUSEKEEPING_CATALOG = {
  日常保洁: ["全屋大扫除", "日常定时保洁", "厨房深度清洁", "卫生间消毒", "玻璃擦拭", "地板打蜡", "沙发/地毯清洗"],
  家电清洗: ["空调清洗", "油烟机清洗", "洗衣机清洗", "冰箱清洗", "热水器除垢"],
  养老护理: ["老人陪护", "居家养老照料", "助浴", "康复按摩", "代办买药"],
  专项服务: ["除螨服务", "甲醛检测治理", "宠物护理", "家庭收纳整理", "开荒保洁"]
}

function catalogByType(serviceType) {
  return serviceType === 2 ? HOUSEKEEPING_CATALOG : REPAIR_CATALOG
}

Page({
  data: {
    serviceType: 1, // 1 维修 2 家政
    majorList: [],
    selectedMajor: "",
    subTypeList: [],
    selectedSubType: "",
    customSubType: "",
    useCustomSubType: false,
    description: "",
    submitting: false
  },
  onLoad() {
    this.initCatalog(1)
  },
  initCatalog(serviceType) {
    const catalog = catalogByType(serviceType)
    const majorList = Object.keys(catalog)
    const selectedMajor = majorList[0] || ""
    const subTypeList = catalog[selectedMajor] || []
    const selectedSubType = subTypeList[0] || ""
    this.setData({
      serviceType,
      majorList,
      selectedMajor,
      subTypeList,
      selectedSubType,
      customSubType: "",
      useCustomSubType: false
    })
  },
  onServiceTypeChange(e) {
    const serviceType = Number(e.currentTarget.dataset.type || 1)
    if (serviceType === this.data.serviceType) return
    this.initCatalog(serviceType)
  },
  onMajorChange(e) {
    const selectedMajor = e.currentTarget.dataset.major
    const catalog = catalogByType(this.data.serviceType)
    const subTypeList = catalog[selectedMajor] || []
    this.setData({
      selectedMajor,
      subTypeList,
      selectedSubType: subTypeList[0] || "",
      useCustomSubType: false,
      customSubType: ""
    })
  },
  onSubTypeChange(e) {
    this.setData({
      selectedSubType: e.currentTarget.dataset.subType,
      useCustomSubType: false
    })
  },
  onCustomSwitch() {
    this.setData({ useCustomSubType: true })
  },
  onCustomSubTypeInput(e) {
    this.setData({ customSubType: e.detail.value })
  },
  onDesc(e) {
    this.setData({ description: e.detail.value })
  },
  getSubType() {
    if (this.data.useCustomSubType) {
      return (this.data.customSubType || "").trim()
    }
    return (this.data.selectedSubType || "").trim()
  },
  async submitRepair() {
    if (this.data.submitting) return

    const serviceMajor = (this.data.selectedMajor || "").trim()
    const serviceSubType = this.getSubType()
    const description = (this.data.description || "").trim()
    if (!serviceMajor || !serviceSubType || !description) {
      wx.showToast({ title: "请完整填写服务信息", icon: "none" })
      return
    }

    this.setData({ submitting: true })
    try {
      await request({
        url: "/repair/submit",
        method: "POST",
        data: {
          propertyId: wx.getStorageSync("propertyId") || 101,
          serviceType: this.data.serviceType,
          serviceMajor,
          serviceSubType,
          category: serviceSubType,
          description,
          images: "[]"
        }
      })
      wx.showToast({ title: "提交成功", icon: "success" })
      this.setData({
        description: "",
        customSubType: "",
        useCustomSubType: false
      })
    } catch (error) {
      wx.showToast({ title: error?.message || "提交失败", icon: "none" })
    } finally {
      this.setData({ submitting: false })
    }
  }
})


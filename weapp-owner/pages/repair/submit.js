const { request } = require('../../api/request')

const REPAIR_CATALOG = {
  水电维修: ['水管漏水', '水龙头更换', '马桶疏通', '下水道堵塞', '热水器故障', '电路跳闸', '插座损坏', '灯具维修', '线路老化更换'],
  家电维修: ['空调不制冷/漏水', '冰箱不制冷/结冰', '洗衣机不转/漏水', '电视机花屏/无信号', '油烟机故障', '微波炉/烤箱不工作', '净水器故障'],
  房屋结构: ['墙面开裂/脱落', '地面瓷砖空鼓', '吊顶损坏', '门窗变形/关不严', '防水层渗漏', '阳台栏杆松动'],
  家具维修: ['柜门铰链松动', '抽屉轨道卡顿', '沙发塌陷/皮面破损', '床架异响', '桌椅腿损坏'],
  智能设备: ['智能门锁故障', '智能猫眼/摄像头离线', '智能窗帘电机不转', '智能灯带不亮', '网关离线'],
  其他: ['开锁服务', '空调加氟', '地暖清洗', '燃气灶点火故障']
}

const HOUSEKEEPING_CATALOG = {
  日常保洁: ['全屋大扫除', '日常定时保洁', '厨房深度清洁', '卫生间消毒', '玻璃擦拭', '地板打蜡', '沙发/地毯清洗'],
  家电清洗: ['空调清洗', '油烟机清洗', '洗衣机清洗', '冰箱清洗', '热水器除垢'],
  养老护理: ['老人陪护', '居家养老照料', '助浴', '康复按摩', '代办买药'],
  专项服务: ['除螨服务', '甲醛检测治理', '宠物护理', '家庭收纳整理', '开荒保洁']
}

const SLOT_LABELS = {
  '09:00-11:00': '上午 09:00-11:00',
  '11:00-13:00': '中午 11:00-13:00',
  '13:00-15:00': '下午 13:00-15:00',
  '15:00-17:00': '下午 15:00-17:00',
  '17:00-19:00': '傍晚 17:00-19:00'
}

const DEFAULT_SLOTS = Object.keys(SLOT_LABELS).map((code) => ({
  code,
  label: SLOT_LABELS[code],
  available: false,
  availableCount: 0
}))

function catalogByType(serviceType) {
  return Number(serviceType) === 2 ? HOUSEKEEPING_CATALOG : REPAIR_CATALOG
}

function formatDate(date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

function buildDateOptions(days = 7) {
  const list = []
  const today = new Date()
  for (let i = 0; i < days; i += 1) {
    const d = new Date(today)
    d.setDate(today.getDate() + i)
    list.push({
      value: formatDate(d),
      label: i === 0 ? '今天' : i === 1 ? '明天' : `${d.getMonth() + 1}月${d.getDate()}日`
    })
  }
  return list
}

function decodeText(value) {
  if (!value) {
    return ''
  }
  try {
    return decodeURIComponent(value)
  } catch (error) {
    return value
  }
}

function buildFacilityDescription(name, location) {
  const parts = []
  if (name) {
    parts.push(`设施名称：${name}`)
  }
  if (location) {
    parts.push(`设施位置：${location}`)
  }
  parts.push('问题描述：')
  return parts.join('\n')
}

Page({
  data: {
    serviceType: 1,
    majorList: [],
    selectedMajor: '',
    subTypeList: [],
    selectedSubType: '',
    customSubType: '',
    useCustomSubType: false,
    description: '',
    dateOptions: [],
    appointmentDate: '',
    slotList: DEFAULT_SLOTS,
    selectedSlot: '',
    loadingSlots: false,
    submitting: false,
    facilityId: null,
    facilityName: '',
    facilityLocation: ''
  },

  onLoad(options) {
    const dateOptions = buildDateOptions(7)
    const facilityId = Number(options?.facilityId || 0) || null
    const facilityName = decodeText(options?.facilityName)
    const facilityLocation = decodeText(options?.facilityLocation)
    const facilityDescription = facilityId ? buildFacilityDescription(facilityName, facilityLocation) : ''

    this.setData(
      {
        dateOptions,
        appointmentDate: dateOptions[0]?.value || '',
        facilityId,
        facilityName,
        facilityLocation,
        description: facilityDescription
      },
      () => {
        this.initCatalog(1)
      }
    )
  },

  initCatalog(serviceType) {
    const catalog = catalogByType(serviceType)
    const majorList = Object.keys(catalog)
    const selectedMajor = majorList[0] || ''
    const subTypeList = catalog[selectedMajor] || []
    const selectedSubType = subTypeList[0] || ''
    this.setData(
      {
        serviceType,
        majorList,
        selectedMajor,
        subTypeList,
        selectedSubType,
        customSubType: '',
        useCustomSubType: false,
        selectedSlot: '',
        slotList: DEFAULT_SLOTS
      },
      () => this.refreshSlotAvailability()
    )
  },

  onServiceTypeChange(e) {
    const serviceType = Number(e.currentTarget.dataset.type || 1)
    if (serviceType === this.data.serviceType) {
      return
    }
    this.initCatalog(serviceType)
  },

  onMajorChange(e) {
    const selectedMajor = e.currentTarget.dataset.major
    const catalog = catalogByType(this.data.serviceType)
    const subTypeList = catalog[selectedMajor] || []
    this.setData(
      {
        selectedMajor,
        subTypeList,
        selectedSubType: subTypeList[0] || '',
        useCustomSubType: false,
        customSubType: '',
        selectedSlot: '',
        slotList: DEFAULT_SLOTS
      },
      () => this.refreshSlotAvailability()
    )
  },

  onSubTypeChange(e) {
    this.setData(
      {
        selectedSubType: e.currentTarget.dataset.subType,
        useCustomSubType: false,
        selectedSlot: '',
        slotList: DEFAULT_SLOTS
      },
      () => this.refreshSlotAvailability()
    )
  },

  onCustomSwitch() {
    this.setData(
      {
        useCustomSubType: true,
        selectedSlot: '',
        slotList: DEFAULT_SLOTS
      },
      () => this.refreshSlotAvailability()
    )
  },

  onCustomSubTypeInput(e) {
    this.setData(
      {
        customSubType: e.detail.value || '',
        selectedSlot: '',
        slotList: DEFAULT_SLOTS
      },
      () => this.refreshSlotAvailability()
    )
  },

  onDesc(e) {
    this.setData({ description: e.detail.value || '' })
  },

  onDateChange(e) {
    const appointmentDate = e.detail.value
    this.setData(
      {
        appointmentDate,
        selectedSlot: '',
        slotList: DEFAULT_SLOTS
      },
      () => this.refreshSlotAvailability()
    )
  },

  onSlotTap(e) {
    const code = e.currentTarget.dataset.code
    const available = !!e.currentTarget.dataset.available
    if (!code || !available) {
      return
    }
    this.setData({ selectedSlot: code })
  },

  clearFacility() {
    this.setData({
      facilityId: null,
      facilityName: '',
      facilityLocation: '',
      description: ''
    })
  },

  getSubType() {
    if (this.data.useCustomSubType) {
      return (this.data.customSubType || '').trim()
    }
    return (this.data.selectedSubType || '').trim()
  },

  async refreshSlotAvailability() {
    const serviceMajor = (this.data.selectedMajor || '').trim()
    const appointmentDate = (this.data.appointmentDate || '').trim()
    const serviceSubType = this.getSubType()
    if (!serviceMajor || !appointmentDate) {
      return
    }

    this.setData({ loadingSlots: true })
    try {
      const data = await request({
        url: '/repair/available-slots',
        data: {
          serviceType: this.data.serviceType,
          serviceMajor,
          serviceSubType,
          appointmentDate
        }
      })
      const rawSlots = Array.isArray(data?.slots) ? data.slots : []
      const slotMap = new Map(rawSlots.map((item) => [item.code, item]))
      const slotList = DEFAULT_SLOTS.map((slot) => {
        const hit = slotMap.get(slot.code)
        return {
          ...slot,
          available: !!hit?.available,
          availableCount: Number(hit?.availableCount || 0)
        }
      })
      const selectedStillAvailable = slotList.some((slot) => slot.code === this.data.selectedSlot && slot.available)
      this.setData({
        slotList,
        selectedSlot: selectedStillAvailable ? this.data.selectedSlot : ''
      })
    } catch (error) {
      this.setData({
        slotList: DEFAULT_SLOTS,
        selectedSlot: ''
      })
      wx.showToast({ title: error?.message || '时段加载失败', icon: 'none' })
    } finally {
      this.setData({ loadingSlots: false })
    }
  },

  async submitRepair() {
    if (this.data.submitting) {
      return
    }

    const serviceMajor = (this.data.selectedMajor || '').trim()
    const serviceSubType = this.getSubType()
    const description = (this.data.description || '').trim()
    const appointmentDate = (this.data.appointmentDate || '').trim()
    const appointmentTimeSlot = (this.data.selectedSlot || '').trim()
    if (!serviceMajor || !serviceSubType || !description) {
      wx.showToast({ title: '请完整填写服务信息', icon: 'none' })
      return
    }
    if (!appointmentDate) {
      wx.showToast({ title: '请选择上门日期', icon: 'none' })
      return
    }
    if (!appointmentTimeSlot) {
      wx.showToast({ title: '请选择可用上门时段', icon: 'none' })
      return
    }

    this.setData({ submitting: true })
    try {
      await request({
        url: '/repair/submit',
        method: 'POST',
        data: {
          propertyId: wx.getStorageSync('propertyId') || 101,
          serviceType: this.data.serviceType,
          serviceMajor,
          serviceSubType,
          facilityId: this.data.facilityId,
          facilityName: this.data.facilityName,
          appointmentDate,
          appointmentTimeSlot,
          category: serviceSubType,
          description,
          images: '[]'
        }
      })
      wx.showToast({ title: '提交成功', icon: 'success' })
      this.setData({
        customSubType: '',
        useCustomSubType: false,
        selectedSlot: '',
        description: this.data.facilityId
          ? buildFacilityDescription(this.data.facilityName, this.data.facilityLocation)
          : ''
      })
      this.refreshSlotAvailability()
    } catch (error) {
      wx.showToast({ title: error?.message || '提交失败', icon: 'none' })
    } finally {
      this.setData({ submitting: false })
    }
  }
})

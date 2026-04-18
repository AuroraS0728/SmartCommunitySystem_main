const { request } = require('../../../api/request')
Page({
  data:{list:[]},
  async onShow(){ const list = await request({url:'/repair/list'}); this.setData({list}) }
})


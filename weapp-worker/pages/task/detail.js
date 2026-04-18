const { request } = require('../../../api/request')
Page({
  data:{detail:{}},
  async onLoad(q){ const detail = await request({url:`/repair/${q.id}`}); this.setData({detail}) }
})


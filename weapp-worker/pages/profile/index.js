const { request } = require('../../../api/request')
Page({
  data:{user:null,perf:null},
  async onShow(){ const user = await request({url:'/user/me'}); this.setData({user}); if(user&&user.id){ const perf = await request({url:'/worker/performance',data:{workerId:user.id}}); this.setData({perf}) } }
})


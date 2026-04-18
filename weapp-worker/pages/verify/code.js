const { request } = require('../../../api/request')
Page({
  data:{orderId:'',code:'',result:''},
  onOrderId(e){this.setData({orderId:e.detail.value})},
  onCode(e){this.setData({code:e.detail.value})},
  async verify(){ const r = await request({url:'/worker/verify-code',method:'POST',data:{orderId:Number(this.data.orderId),code:this.data.code}}); this.setData({result:r.pass?'通过':'失败'}) }
})


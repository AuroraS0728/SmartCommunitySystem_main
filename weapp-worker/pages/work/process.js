const { request } = require('../../../api/request')
Page({
  data:{id:null},
  onLoad(q){ this.setData({id:Number(q.id)}) },
  async start(){ await request({url:'/repair/status',method:'POST',data:{orderId:this.data.id,status:2,remark:'维修中'}}); wx.showToast({title:'已开始'}) },
  async finish(){ await request({url:'/repair/status',method:'POST',data:{orderId:this.data.id,status:3,remark:'维修端已确认结束'}}); wx.showToast({title:'已提交结束确认',icon:'none'}) }
})


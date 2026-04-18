const { request } = require("./request"); module.exports={ verify:(data)=>request({url:"/worker/verify-code",method:"POST",data}) }

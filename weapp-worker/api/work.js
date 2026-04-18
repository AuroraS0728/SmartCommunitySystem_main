const { request } = require("./request"); module.exports={ update:(data)=>request({url:"/repair/status",method:"POST",data}) }

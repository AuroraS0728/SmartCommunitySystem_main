const { request } = require("./request"); module.exports={ me:()=>request({url:"/user/me"}) }

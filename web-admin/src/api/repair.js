import request from "@/utils/request"

export const getRepairList = (params) => request.get("/repair/list", { params })
export const getRepairDetail = (id) => request.get(`/repair/${id}`)
export const assignRepair = (data) => request.post("/repair/assign", data)
export const updateRepairStatus = (data) => request.post("/repair/status", data)
export const getRepairVerifyCode = (id) => request.get(`/repair/${id}/verify-code`)

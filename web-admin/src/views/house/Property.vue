<template>
  <div class="page-grid">
    <section class="panel">
      <div class="panel-header">
        <h3>房屋档案</h3>
      </div>

      <div class="tool-grid">
        <div class="tool-card">
          <div class="tool-title">搜索房屋</div>
          <el-input
            v-model="keyword"
            placeholder="按房产号/楼栋/单元/房号/业主/租户搜索"
            clearable
            style="max-width: 420px"
          />
        </div>
        <div class="tool-card create-card">
          <div class="tool-title">新增房屋</div>
          <p>房产号规则：YZ + 楼号2位 + 单元2位 + 房号3位 + 年份后2位</p>
          <el-button type="primary" @click="openCreate">新增房屋</el-button>
        </div>
      </div>

      <div class="status-entry-row">
        <button
          v-for="section in statusSections"
          :key="section.status"
          class="status-entry"
          :class="{ active: selectedStatus === section.status }"
          type="button"
          @click="selectStatus(section.status)"
        >
          <el-tag :type="section.tagType">{{ section.label }}</el-tag>
          <span class="status-entry-count">{{ countText(section.status) }}</span>
        </button>
      </div>

      <div v-if="selectedStatus === null" class="empty-wrap">
        <el-empty description="请先选择状态后查看房屋列表" />
      </div>

      <template v-else>
        <div class="main-table-title">当前状态：{{ resolveStatusText(selectedStatus) }}</div>
        <el-table :data="displayList(selectedStatus)" stripe v-loading="isLoading(selectedStatus)">
          <el-table-column prop="id" label="ID" width="90" />
          <el-table-column prop="propertyCode" label="房产号" min-width="150" />
          <el-table-column prop="community" label="小区" min-width="140" />
          <el-table-column prop="building" label="楼栋" min-width="90" />
          <el-table-column prop="unit" label="单元" min-width="90" />
          <el-table-column prop="room" label="房号" min-width="90" />
          <el-table-column prop="ownerName" label="业主" min-width="120" />
          <el-table-column prop="tenantName" label="租户" min-width="120">
            <template #default="{ row }">{{ row.tenantName || "--" }}</template>
          </el-table-column>
          <el-table-column label="租约到期" min-width="170">
            <template #default="{ row }">{{ row.rentEndTime || "--" }}</template>
          </el-table-column>
          <el-table-column prop="area" label="面积(m²)" min-width="110" />
          <el-table-column label="状态" min-width="110">
            <template #default="{ row }">
              <el-tag :type="resolveStatusTag(row.status)">{{ resolveStatusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button size="small" text type="primary" @click="openEdit(row)">编辑</el-button>
              <el-button size="small" text type="danger" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>
    </section>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑房屋' : '新增房屋'" width="760px">
      <el-form label-width="96px">
        <el-alert title="保存后将自动生成房产号" type="info" :closable="false" style="margin-bottom: 14px" />
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="小区"><el-input v-model="form.community" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="楼栋"><el-input v-model="form.building" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单元"><el-input v-model="form.unit" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="房号"><el-input v-model="form.room" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="业主"><el-input v-model="form.ownerName" /></el-form-item>
          </el-col>
          <el-col :span="12" v-if="Number(form.status) === 5">
            <el-form-item label="租户"><el-input v-model="form.tenantName" /></el-form-item>
          </el-col>
          <el-col :span="12" v-if="Number(form.status) === 5">
            <el-form-item label="租约到期">
              <el-date-picker
                v-model="form.rentEndTime"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                format="YYYY-MM-DD HH:mm:ss"
                placeholder="请选择租约到期时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="面积">
              <el-input-number v-model="form.area" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="form.status" style="width: 100%">
                <el-option label="未售" :value="1" />
                <el-option label="已售" :value="2" />
                <el-option label="空置" :value="3" />
                <el-option label="已入住" :value="4" />
                <el-option label="已出租" :value="5" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from "vue"
import { ElMessage, ElMessageBox } from "element-plus"
import { addHouse, deleteHouse, getHouseList, updateHouse } from "@/api/house"

const statusSections = [
  { status: 4, label: "已入住", tagType: "success" },
  { status: 5, label: "已出租", tagType: "primary" },
  { status: 3, label: "空置", tagType: "info" },
  { status: 2, label: "已售", tagType: "warning" },
  { status: 1, label: "未售", tagType: "danger" }
]

const keyword = ref("")
const selectedStatus = ref(null)
const listMap = ref({ 1: [], 2: [], 3: [], 4: [], 5: [] })
const loadedMap = ref({ 1: false, 2: false, 3: false, 4: false, 5: false })
const loadingMap = ref({ 1: false, 2: false, 3: false, 4: false, 5: false })
const dialogVisible = ref(false)
const saving = ref(false)

const form = ref({
  id: null,
  community: "",
  building: "",
  unit: "",
  room: "",
  ownerName: "",
  tenantName: "",
  rentEndTime: "",
  area: 0,
  status: 3,
  oldStatus: null
})

function normalizeStatus(raw, row = null) {
  if (raw === null || raw === undefined || raw === "") {
    if (row?.tenantName) return 5
    if (row?.ownerName) return 4
    return null
  }
  const num = Number(raw)
  if (Number.isInteger(num)) {
    if (num >= 1 && num <= 5) return num
    if (num === 0) return 3
    if (num === 6) return 5
  }
  const text = String(raw).trim().toLowerCase()
  if (text.includes("未售") || text.includes("unsold")) return 1
  if (text.includes("已售") || text.includes("sold")) return 2
  if (text.includes("空置") || text.includes("vacant") || text.includes("empty")) return 3
  if (text.includes("已入住") || text.includes("入住") || text.includes("occupied")) return 4
  if (text.includes("已出租") || text.includes("出租") || text.includes("rented") || text.includes("lease")) return 5
  if (row?.tenantName) return 5
  if (row?.ownerName) return 4
  return null
}

function resolveStatusText(status) {
  const value = normalizeStatus(status)
  const map = { 1: "未售", 2: "已售", 3: "空置", 4: "已入住", 5: "已出租" }
  return value ? map[value] : String(status || "--")
}

function resolveStatusTag(status) {
  const value = normalizeStatus(status)
  if (value === 4) return "success"
  if (value === 5) return "primary"
  if (value === 3) return "info"
  if (value === 2) return "warning"
  if (value === 1) return "danger"
  return "info"
}

function resetForm() {
  form.value = {
    id: null,
    community: "",
    building: "",
    unit: "",
    room: "",
    ownerName: "",
    tenantName: "",
    rentEndTime: "",
    area: 0,
    status: 3,
    oldStatus: null
  }
}

function openCreate() {
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  const currentStatus = normalizeStatus(row.status, row) || 3
  form.value = {
    id: row.id,
    community: row.community,
    building: row.building,
    unit: row.unit,
    room: row.room,
    ownerName: row.ownerName,
    tenantName: row.tenantName || "",
    rentEndTime: row.rentEndTime || "",
    area: Number(row.area || 0),
    status: currentStatus,
    oldStatus: currentStatus
  }
  dialogVisible.value = true
}

function displayList(status) {
  const rows = listMap.value[status] || []
  const text = keyword.value.trim()
  if (!text) return rows
  return rows.filter(
    (item) =>
      String(item.propertyCode || "").includes(text) ||
      String(item.building || "").includes(text) ||
      String(item.unit || "").includes(text) ||
      String(item.room || "").includes(text) ||
      String(item.ownerName || "").includes(text) ||
      String(item.tenantName || "").includes(text)
  )
}

function isLoading(status) {
  return !!loadingMap.value[status]
}

function countText(status) {
  if (!loadedMap.value[status]) return "点击加载"
  return `${(listMap.value[status] || []).length} 条`
}

async function loadListByStatus(status, force = false) {
  const targetStatus = Number(status)
  if (!force && loadedMap.value[targetStatus]) return
  loadingMap.value = { ...loadingMap.value, [targetStatus]: true }
  try {
    const res = await getHouseList()
    const rows = Array.isArray(res?.data) ? res.data : []
    const bucket = { 1: [], 2: [], 3: [], 4: [], 5: [] }
    rows.forEach((item) => {
      const value = normalizeStatus(item.status, item)
      if (value && bucket[value]) bucket[value].push(item)
    })
    listMap.value = { ...listMap.value, ...bucket }
    loadedMap.value = { 1: true, 2: true, 3: true, 4: true, 5: true }
  } finally {
    loadingMap.value = { ...loadingMap.value, [targetStatus]: false }
  }
}

async function selectStatus(status) {
  const targetStatus = Number(status)
  selectedStatus.value = targetStatus
  await loadListByStatus(targetStatus)
}

async function refreshAfterMutation(extraStatuses = []) {
  const statusSet = new Set()
  if (selectedStatus.value !== null) statusSet.add(selectedStatus.value)
  extraStatuses.forEach((status) => {
    const value = normalizeStatus(status)
    if (value) statusSet.add(value)
  })
  if (!statusSet.size) return
  await Promise.all([...statusSet].map((item) => loadListByStatus(item, true)))
}

async function submit() {
  if (!form.value.community || !form.value.building || !form.value.unit || !form.value.room) {
    ElMessage.warning("请填写小区、楼栋、单元、房号")
    return
  }
  if (Number(form.value.status) === 5 && (!form.value.tenantName || !form.value.rentEndTime)) {
    ElMessage.warning("已出租状态必须填写租户和租约到期时间")
    return
  }
  saving.value = true
  try {
    const statusValue = Number(form.value.status)
    const payload = {
      community: form.value.community,
      building: form.value.building,
      unit: form.value.unit,
      room: form.value.room,
      ownerName: form.value.ownerName,
      tenantName: statusValue === 5 ? form.value.tenantName : "",
      rentEndTime: statusValue === 5 ? form.value.rentEndTime : null,
      area: form.value.area,
      status: statusValue
    }
    if (form.value.id) {
      await updateHouse(form.value.id, payload)
      ElMessage.success("更新成功")
    } else {
      await addHouse(payload)
      ElMessage.success("新增成功")
    }
    dialogVisible.value = false
    if (selectedStatus.value === null) selectedStatus.value = form.value.status
    await refreshAfterMutation([form.value.status, form.value.oldStatus])
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除房屋 #${row.id} 吗？`, "提示", { type: "warning" })
  await deleteHouse(row.id)
  ElMessage.success("删除成功")
  await refreshAfterMutation([row.status])
}
</script>

<style scoped>
.page-grid { display: grid; }
.panel { background: #fff; border: 1px solid #e2e8f0; border-radius: 12px; padding: 16px; }
.panel-header { display: flex; justify-content: space-between; align-items: center; gap: 12px; margin-bottom: 14px; }
.panel-header h3 { margin: 0; font-size: 16px; }
.tool-grid { display: grid; grid-template-columns: 2fr 1fr; gap: 12px; margin-bottom: 14px; }
.tool-card { border: 1px solid #dbeafe; background: #f8fafc; border-radius: 10px; padding: 12px; display: flex; flex-direction: column; gap: 10px; }
.tool-title { color: #334155; font-size: 13px; font-weight: 600; }
.create-card p { margin: 0; color: #64748b; font-size: 12px; line-height: 1.5; }
.status-entry-row { display: grid; grid-template-columns: repeat(5, minmax(0, 1fr)); gap: 10px; margin-bottom: 14px; }
.status-entry { border: 1px solid #dbeafe; background: #f8fafc; border-radius: 10px; padding: 10px 12px; display: flex; align-items: center; justify-content: space-between; cursor: pointer; }
.status-entry:hover { border-color: #93c5fd; background: #eff6ff; }
.status-entry.active { border-color: #2563eb; background: #eff6ff; }
.status-entry-count { color: #475569; font-size: 13px; }
.main-table-title { color: #64748b; font-size: 13px; margin-bottom: 10px; }
.empty-wrap { border: 1px dashed #cbd5e1; border-radius: 12px; background: #f8fafc; padding: 18px 8px; }
@media (max-width: 1200px) { .tool-grid { grid-template-columns: 1fr; } .status-entry-row { grid-template-columns: repeat(3, minmax(0, 1fr)); } }
@media (max-width: 860px) { .status-entry-row { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 760px) { .status-entry-row { grid-template-columns: 1fr; } }
</style>

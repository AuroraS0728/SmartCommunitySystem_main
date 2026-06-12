<template>
  <div class="page-grid">
    <section class="summary-grid">
      <article class="summary-card">
        <p>工单总量</p>
        <h3>{{ list.length }}</h3>
      </article>
      <article class="summary-card">
        <p>待上门验证</p>
        <h3>{{ pendingCount }}</h3>
      </article>
      <article class="summary-card">
        <p>服务中</p>
        <h3>{{ serviceCount }}</h3>
      </article>
      <article class="summary-card">
        <p>待评价</p>
        <h3>{{ waitRateCount }}</h3>
      </article>
      <article class="summary-card">
        <p>已完成</p>
        <h3>{{ doneCount }}</h3>
      </article>
    </section>

    <section class="panel">
      <div class="panel-header">
        <h3>维修/家政工单闭环管理</h3>
        <div class="filters">
          <el-input v-model="keyword" placeholder="搜索工单内容/类型" clearable />
          <el-select v-model="statusFilter" clearable placeholder="按状态筛选" style="width: 180px">
            <el-option :value="1" label="待上门验证" />
            <el-option :value="2" label="服务中" />
            <el-option :value="3" label="待评价" />
            <el-option :value="4" label="已完成" />
            <el-option :value="5" label="已取消" />
          </el-select>
          <el-button type="primary" @click="loadData">刷新</el-button>
        </div>
      </div>

      <el-table :data="displayList" stripe>
        <el-table-column prop="id" label="工单ID" width="96" />
        <el-table-column prop="category" label="类型" min-width="120" />
        <el-table-column prop="description" label="问题描述" min-width="220" />
        <el-table-column prop="userId" label="业主ID" width="110" />
        <el-table-column prop="assignee" label="维修员ID" width="110">
          <template #default="{ row }">
            {{ row.assignee || "-" }}
          </template>
        </el-table-column>
        <el-table-column label="推荐维修员" min-width="150">
          <template #default="{ row }">
            <div class="recommend-cell">
              <span>{{ row.suggestedWorkerId ? workerLabel(row.suggestedWorkerId) : "-" }}</span>
              <el-button
                v-if="Number(row.status) === 1"
                size="small"
                type="primary"
                link
                :loading="autoAssignLoadingId === row.id"
                @click="submitAutoAssign(row)"
              >
                {{ row.suggestedWorkerId ? "采纳推荐" : "推荐派单" }}
              </el-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="150">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" min-width="170" />
        <el-table-column label="操作" min-width="280">
          <template #default="{ row }">
            <el-button
              size="small"
              type="primary"
              text
              :disabled="row.status === 4 || row.status === 5"
              @click="openAssignDialog(row)"
            >
              {{ row.assignee ? "改派" : "派单" }}
            </el-button>
            <el-button
              size="small"
              type="success"
              text
              :disabled="!row.assignee || row.status === 4 || row.status === 5"
              @click="showVerifyCode(row)"
            >
              验证码
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="assignVisible" width="520px" title="工单派单">
      <el-form label-width="88px">
        <el-form-item label="工单ID">
          <el-input :model-value="String(assignForm.orderId || '')" disabled />
        </el-form-item>
        <el-form-item label="维修人员">
          <el-select v-model="assignForm.assignee" style="width: 100%" placeholder="请选择维修人员">
            <el-option
              v-for="worker in workerOptions"
              :key="worker.id"
              :label="workerOptionLabel(worker)"
              :value="worker.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="assignForm.remark" type="textarea" rows="3" placeholder="可填写上门时间等说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" :loading="assignLoading" @click="submitAssign">确认派单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue"
import { ElMessage, ElMessageBox } from "element-plus"
import { assignRepair, autoAssignRepair, getRepairList, getRepairVerifyCode } from "@/api/repair"
import { getWorkerList } from "@/api/worker"
import { workerOptionLabel } from "@/utils/workerDisplay"

const keyword = ref("")
const statusFilter = ref(null)
const list = ref([])
const workerOptions = ref([])
const assignVisible = ref(false)
const assignLoading = ref(false)
const autoAssignLoadingId = ref(null)
const assignForm = ref({
  orderId: null,
  assignee: null,
  remark: ""
})

const displayList = computed(() => {
  let result = [...list.value]
  if (keyword.value.trim()) {
    const text = keyword.value.trim()
    result = result.filter(
      (item) => String(item.description || "").includes(text) || String(item.category || "").includes(text)
    )
  }
  if (statusFilter.value !== null && statusFilter.value !== undefined && statusFilter.value !== "") {
    const target = Number(statusFilter.value)
    result = result.filter((item) => Number(item.status) === target)
  }
  return result
})

const pendingCount = computed(() => list.value.filter((item) => Number(item.status) === 1).length)
const serviceCount = computed(() => list.value.filter((item) => Number(item.status) === 2).length)
const waitRateCount = computed(() => list.value.filter((item) => Number(item.status) === 3).length)
const doneCount = computed(() => list.value.filter((item) => Number(item.status) === 4).length)
const workerNameMap = computed(() => {
  const map = new Map()
  workerOptions.value.forEach((worker) => {
    map.set(Number(worker.id), workerOptionLabel(worker))
  })
  return map
})

function workerLabel(workerId) {
  return workerNameMap.value.get(Number(workerId)) || `ID:${workerId}`
}

function statusText(status) {
  const map = {
    1: "待上门验证",
    2: "服务中",
    3: "待评价",
    4: "已完成",
    5: "已取消"
  }
  return map[Number(status)] || "未知状态"
}

function statusType(status) {
  const s = Number(status)
  if (s === 4) return "success"
  if (s === 3) return "warning"
  if (s === 2) return "primary"
  if (s === 5) return "info"
  return ""
}

async function loadWorkers() {
  const res = await getWorkerList()
  workerOptions.value = Array.isArray(res.data) ? res.data : []
}

async function loadData() {
  const res = await getRepairList()
  list.value = Array.isArray(res.data) ? res.data : []
}

function openAssignDialog(row) {
  assignForm.value = {
    orderId: row.id,
    assignee: row.assignee || null,
    remark: row.remark || ""
  }
  assignVisible.value = true
}

async function submitAssign() {
  if (!assignForm.value.orderId || !assignForm.value.assignee) {
    ElMessage.warning("请先选择维修人员")
    return
  }
  assignLoading.value = true
  try {
    const res = await assignRepair({
      orderId: assignForm.value.orderId,
      assignee: assignForm.value.assignee,
      remark: assignForm.value.remark
    })
    assignVisible.value = false
    ElMessage.success(`派单成功，验证码：${res.data?.verifyCode || "已生成"}`)
    await loadData()
  } finally {
    assignLoading.value = false
  }
}

async function submitAutoAssign(row) {
  if (!row?.id) return
  autoAssignLoadingId.value = row.id
  try {
    const res = await autoAssignRepair(row.id)
    const workerId = res.data?.workerId || res.data?.order?.assignee
    ElMessage.success(`已派给${workerLabel(workerId)}`)
    await loadData()
  } finally {
    autoAssignLoadingId.value = null
  }
}

async function showVerifyCode(row) {
  try {
    const res = await getRepairVerifyCode(row.id)
    const code = res.data?.verifyCode || "暂无"
    await ElMessageBox.alert(`工单 ${row.id} 当前验证码：${code}`, "现场验证码", {
      confirmButtonText: "知道了"
    })
  } catch (error) {
    ElMessage.error(error?.message || "获取验证码失败")
  }
}

onMounted(async () => {
  try {
    await Promise.all([loadWorkers(), loadData()])
  } catch {
    ElMessage.error("工单数据加载失败")
  }
})
</script>

<style scoped>
.page-grid {
  display: grid;
  gap: 16px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 14px;
}

.summary-card {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 16px;
}

.summary-card p {
  margin: 0;
  color: #64748b;
  font-size: 13px;
}

.summary-card h3 {
  margin: 10px 0 0;
  font-size: 28px;
}

.panel {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 16px;
}

.panel-header {
  margin-bottom: 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.panel-header h3 {
  margin: 0;
  font-size: 16px;
}

.filters {
  display: flex;
  gap: 10px;
}

.filters .el-input {
  width: 240px;
}

.recommend-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

@media (max-width: 1300px) {
  .summary-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 860px) {
  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .panel-header {
    flex-direction: column;
    align-items: stretch;
  }

  .filters {
    flex-direction: column;
  }

  .filters .el-input {
    width: 100%;
  }
}
</style>

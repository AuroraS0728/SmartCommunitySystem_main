<template>
  <div class="page-grid">
    <section class="summary-grid">
      <article class="summary-card">
        <p>高风险投诉</p>
        <h3>{{ highRiskCount }}</h3>
      </article>
      <article class="summary-card">
        <p>待发送催缴</p>
        <h3>{{ pendingReminderCount }}</h3>
      </article>
      <article class="summary-card">
        <p>待处理任务</p>
        <h3>{{ pendingTaskCount }}</h3>
      </article>
      <article class="summary-card">
        <p>SLA逾期工单</p>
        <h3>{{ delayedRepairCount }}</h3>
      </article>
    </section>

    <section class="panel">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="投诉分析" name="complaint">
          <div class="toolbar">
            <el-select v-model="complaintRisk" clearable placeholder="风险等级" style="width: 160px" @change="loadComplaints">
              <el-option label="高风险" value="HIGH" />
              <el-option label="中风险" value="MID" />
              <el-option label="低风险" value="LOW" />
            </el-select>
            <el-button type="primary" @click="loadComplaints">刷新</el-button>
          </div>
          <el-table v-loading="complaintLoading" :data="complaints" stripe>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="title" label="标题" min-width="150" />
            <el-table-column prop="content" label="内容" min-width="260" show-overflow-tooltip />
            <el-table-column label="情感" width="130">
              <template #default="{ row }">
                <el-tag :type="sentimentType(row.sentimentLabel)">{{ sentimentText(row.sentimentLabel) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="sentimentScore" label="分数" width="90" />
            <el-table-column label="风险" width="110">
              <template #default="{ row }">
                <el-tag :type="riskType(row.riskLevel)">{{ riskText(row.riskLevel) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="analyzedAt" label="分析时间" min-width="170" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button type="primary" link @click="reanalyze(row)">重新分析</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="紧急关键词" name="keyword">
          <div class="toolbar">
            <el-input v-model="keywordForm.keyword" placeholder="新增关键词" style="width: 220px" clearable />
            <el-button type="primary" @click="saveKeyword">新增</el-button>
          </div>
          <el-table :data="keywords" stripe>
            <el-table-column prop="keyword" label="关键词" min-width="180" />
            <el-table-column label="启用" width="120">
              <template #default="{ row }">
                <el-switch
                  :model-value="Number(row.enabled) === 1"
                  @change="(value) => toggleKeyword(row, value)"
                />
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" min-width="170" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button type="danger" link @click="removeKeyword(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="信用体系" name="credit">
          <div class="toolbar">
            <el-select v-model="creditForm.userId" filterable placeholder="选择业主" style="width: 240px">
              <el-option
                v-for="owner in owners"
                :key="owner.id"
                :label="`${owner.nickname || owner.account || '用户'} / ${owner.id}`"
                :value="owner.id"
              />
            </el-select>
            <el-input-number v-model="creditForm.changeValue" :min="-100" :max="100" controls-position="right" />
            <el-input v-model="creditForm.reason" placeholder="调整原因" style="width: 260px" />
            <el-button type="primary" @click="submitCredit">调整信用</el-button>
          </div>
          <el-table :data="owners" stripe style="margin-bottom: 16px">
            <el-table-column prop="id" label="业主ID" width="90" />
            <el-table-column prop="nickname" label="业主" min-width="140" />
            <el-table-column prop="phone" label="手机号" min-width="130" />
            <el-table-column prop="creditScore" label="信用分" width="100" />
            <el-table-column prop="creditLastUpdate" label="最后更新" min-width="170" />
          </el-table>
          <el-table :data="creditLogs" stripe>
            <el-table-column prop="userId" label="业主ID" width="90" />
            <el-table-column prop="changeValue" label="变动" width="90" />
            <el-table-column prop="beforeScore" label="调整前" width="90" />
            <el-table-column prop="afterScore" label="调整后" width="90" />
            <el-table-column prop="reason" label="原因" min-width="180" />
            <el-table-column prop="createTime" label="时间" min-width="170" />
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="智能催缴" name="reminder">
          <div class="toolbar">
            <el-button type="primary" @click="generateReminders">生成逾期催缴</el-button>
            <el-button @click="loadReminders">刷新</el-button>
          </div>
          <el-table :data="reminders" stripe>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="userId" label="业主ID" width="100" />
            <el-table-column prop="feeBillId" label="账单ID" width="100" />
            <el-table-column prop="method" label="方式" width="100" />
            <el-table-column prop="content" label="内容" min-width="260" />
            <el-table-column label="状态" width="110">
              <template #default="{ row }">
                <el-tag :type="Number(row.status) === 1 ? 'success' : 'warning'">
                  {{ Number(row.status) === 1 ? '已发送' : '待发送' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="sendTime" label="发送时间" min-width="170" />
            <el-table-column label="操作" width="160">
              <template #default="{ row }">
                <el-button type="primary" link :disabled="Number(row.status) === 1" @click="sendReminder(row)">标记发送</el-button>
                <el-button type="danger" link @click="removeReminder(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="物业任务" name="task">
          <div class="toolbar">
            <el-input v-model="taskForm.title" placeholder="任务标题" style="width: 220px" />
            <el-input v-model="taskForm.description" placeholder="任务说明" style="width: 280px" />
            <el-select v-model="taskForm.assignedTo" clearable filterable placeholder="指派人员" style="width: 220px">
              <el-option
                v-for="worker in workers"
                :key="worker.id"
                :label="`${worker.nickname || worker.account || '人员'} / ${worker.id}`"
                :value="worker.id"
              />
            </el-select>
            <el-button type="primary" @click="saveTask">创建任务</el-button>
          </div>
          <el-table :data="tasks" stripe>
            <el-table-column prop="title" label="标题" min-width="160" />
            <el-table-column prop="description" label="说明" min-width="240" />
            <el-table-column prop="assignedTo" label="指派给" width="100" />
            <el-table-column label="状态" width="110">
              <template #default="{ row }">
                <el-tag :type="Number(row.status) === 1 ? 'success' : 'warning'">
                  {{ Number(row.status) === 1 ? '已完成' : '待处理' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" min-width="170" />
            <el-table-column label="操作" width="170">
              <template #default="{ row }">
                <el-button type="success" link :disabled="Number(row.status) === 1" @click="completeTask(row)">完成</el-button>
                <el-button type="danger" link @click="removeTask(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="SLA监控" name="sla">
          <div class="toolbar">
            <el-select v-model="repairPriority" clearable placeholder="优先级" style="width: 140px" @change="loadRepairs">
              <el-option :value="1" label="紧急" />
              <el-option :value="2" label="普通" />
              <el-option :value="3" label="低" />
            </el-select>
            <el-button type="primary" @click="scanSla">扫描逾期</el-button>
            <el-button @click="loadRepairs">刷新</el-button>
          </div>
          <el-table :data="repairs" stripe>
            <el-table-column prop="id" label="工单ID" width="90" />
            <el-table-column prop="category" label="类型" min-width="130" />
            <el-table-column prop="description" label="描述" min-width="240" show-overflow-tooltip />
            <el-table-column label="优先级" width="110">
              <template #default="{ row }">
                <el-tag :type="priorityType(row.priority)">{{ priorityText(row.priority) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="suggestedWorkerId" label="推荐维修员" width="120" />
            <el-table-column prop="slaDeadline" label="SLA截止" min-width="170" />
            <el-table-column prop="delayCount" label="逾期次数" width="100" />
            <el-table-column label="操作" width="160">
              <template #default="{ row }">
                <el-button type="primary" link @click="setPriority(row, 1)">设为紧急</el-button>
                <el-button type="info" link @click="setPriority(row, 2)">普通</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getComplaintList } from '@/api/complaint'
import { getRepairList } from '@/api/repair'
import { getUsers } from '@/api/user'
import { getWorkerList } from '@/api/worker'
import {
  adjustCredit,
  analyzeComplaint,
  completePropertyTask,
  createEmergencyKeyword,
  createPropertyTask,
  deleteEmergencyKeyword,
  deletePaymentReminder,
  deletePropertyTask,
  generatePaymentReminders,
  getCreditLogs,
  getEmergencyKeywords,
  getPaymentReminders,
  getPropertyTasks,
  scanRepairSla,
  sendPaymentReminder,
  updateEmergencyKeyword,
  updateRepairPriority
} from '@/api/innovation'

const activeTab = ref('complaint')
const complaints = ref([])
const complaintLoading = ref(false)
const complaintRisk = ref('')
const keywords = ref([])
const keywordForm = ref({ keyword: '' })
const owners = ref([])
const creditLogs = ref([])
const creditForm = ref({ userId: null, changeValue: 5, reason: '' })
const reminders = ref([])
const tasks = ref([])
const workers = ref([])
const taskForm = ref({ title: '', description: '', assignedTo: null })
const repairs = ref([])
const repairPriority = ref(null)

const highRiskCount = computed(() => complaints.value.filter((item) => item.riskLevel === 'HIGH').length)
const pendingReminderCount = computed(() => reminders.value.filter((item) => Number(item.status) === 0).length)
const pendingTaskCount = computed(() => tasks.value.filter((item) => Number(item.status) === 0).length)
const delayedRepairCount = computed(() => repairs.value.filter((item) => Number(item.delayCount || 0) > 0).length)

function sentimentType(label) {
  if (label === 'POSITIVE') return 'success'
  if (label === 'NEGATIVE') return 'danger'
  return 'info'
}

function sentimentText(label) {
  const map = {
    POSITIVE: '正向',
    NEGATIVE: '负向',
    NEUTRAL: '中性'
  }
  return map[label] || '未分析'
}

function riskType(level) {
  if (level === 'HIGH') return 'danger'
  if (level === 'MID') return 'warning'
  return 'success'
}

function riskText(level) {
  const map = {
    HIGH: '高风险',
    MID: '中风险',
    LOW: '低风险'
  }
  return map[level] || '未分析'
}

function priorityText(priority) {
  const map = { 1: '紧急', 2: '普通', 3: '低' }
  return map[Number(priority)] || '--'
}

function priorityType(priority) {
  const value = Number(priority)
  if (value === 1) return 'danger'
  if (value === 2) return 'warning'
  return 'info'
}

async function loadComplaints() {
  complaintLoading.value = true
  try {
    const params = complaintRisk.value ? { riskLevel: complaintRisk.value } : {}
    const res = await getComplaintList(params)
    complaints.value = Array.isArray(res.data) ? res.data : []
  } finally {
    complaintLoading.value = false
  }
}

async function reanalyze(row) {
  await analyzeComplaint(row.id)
  ElMessage.success('已重新分析')
  await loadComplaints()
}

async function loadKeywords() {
  const res = await getEmergencyKeywords()
  keywords.value = Array.isArray(res.data) ? res.data : []
}

async function saveKeyword() {
  if (!keywordForm.value.keyword.trim()) {
    ElMessage.warning('请输入关键词')
    return
  }
  await createEmergencyKeyword({ keyword: keywordForm.value.keyword.trim(), enabled: 1 })
  keywordForm.value.keyword = ''
  ElMessage.success('已新增')
  await loadKeywords()
}

async function toggleKeyword(row, enabled) {
  await updateEmergencyKeyword(row.id, { enabled: enabled ? 1 : 0 })
  await loadKeywords()
}

async function removeKeyword(row) {
  await ElMessageBox.confirm(`确认删除关键词「${row.keyword}」？`, '删除确认', { type: 'warning' })
  await deleteEmergencyKeyword(row.id)
  ElMessage.success('已删除')
  await loadKeywords()
}

async function loadOwners() {
  const res = await getUsers({ pageNum: 1, pageSize: 1000, role: 1 })
  owners.value = Array.isArray(res?.data?.records) ? res.data.records : []
}

async function loadCreditLogs() {
  const res = await getCreditLogs({ page: 1, size: 100 })
  if (Array.isArray(res.data)) {
    creditLogs.value = res.data
    return
  }
  creditLogs.value = Array.isArray(res.data?.records) ? res.data.records : []
}

async function submitCredit() {
  if (!creditForm.value.userId) {
    ElMessage.warning('请选择业主')
    return
  }
  await adjustCredit({ ...creditForm.value })
  ElMessage.success('信用分已调整')
  creditForm.value.reason = ''
  await Promise.all([loadOwners(), loadCreditLogs()])
}

async function loadReminders() {
  const res = await getPaymentReminders({ pageNum: 1, pageSize: 100 })
  reminders.value = Array.isArray(res.data?.records) ? res.data.records : Array.isArray(res.data) ? res.data : []
}

async function generateReminders() {
  const res = await generatePaymentReminders()
  ElMessage.success(`已生成 ${Array.isArray(res.data) ? res.data.length : 0} 条催缴`)
  await loadReminders()
}

async function sendReminder(row) {
  await sendPaymentReminder(row.id)
  ElMessage.success('已标记发送')
  await loadReminders()
}

async function removeReminder(row) {
  await ElMessageBox.confirm(`确认删除催缴 #${row.id}？`, '删除确认', { type: 'warning' })
  await deletePaymentReminder(row.id)
  await loadReminders()
}

async function loadTasks() {
  const res = await getPropertyTasks({ pageNum: 1, pageSize: 100 })
  tasks.value = Array.isArray(res.data?.records) ? res.data.records : Array.isArray(res.data) ? res.data : []
}

async function loadWorkers() {
  const res = await getWorkerList()
  workers.value = Array.isArray(res.data) ? res.data : []
}

async function saveTask() {
  if (!taskForm.value.title.trim()) {
    ElMessage.warning('请输入任务标题')
    return
  }
  await createPropertyTask({ ...taskForm.value, status: 0 })
  taskForm.value = { title: '', description: '', assignedTo: null }
  ElMessage.success('任务已创建')
  await loadTasks()
}

async function completeTask(row) {
  await completePropertyTask(row.id)
  ElMessage.success('任务已完成')
  await loadTasks()
}

async function removeTask(row) {
  await ElMessageBox.confirm(`确认删除任务「${row.title}」？`, '删除确认', { type: 'warning' })
  await deletePropertyTask(row.id)
  await loadTasks()
}

async function loadRepairs() {
  const params = repairPriority.value ? { priority: repairPriority.value } : {}
  const res = await getRepairList(params)
  repairs.value = Array.isArray(res.data) ? res.data : []
}

async function scanSla() {
  const res = await scanRepairSla()
  ElMessage.success(`扫描完成，发现 ${res.data?.delayCount || 0} 个逾期工单`)
  await loadRepairs()
}

async function setPriority(row, priority) {
  await updateRepairPriority(row.id, { priority })
  ElMessage.success('优先级已更新')
  await loadRepairs()
}

onMounted(async () => {
  await Promise.all([
    loadComplaints(),
    loadKeywords(),
    loadOwners(),
    loadCreditLogs(),
    loadReminders(),
    loadTasks(),
    loadWorkers(),
    loadRepairs()
  ])
})
</script>

<style scoped>
.page-grid {
  display: grid;
  gap: 16px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.summary-card {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 16px;
}

.summary-card p {
  margin: 0;
  color: #64748b;
  font-size: 13px;
}

.summary-card h3 {
  margin: 10px 0 0;
  font-size: 26px;
}

.panel {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 16px;
}

.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  margin-bottom: 14px;
}

@media (max-width: 1100px) {
  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 720px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }
}
</style>

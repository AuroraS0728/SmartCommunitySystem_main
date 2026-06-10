<template>
  <div class="page-grid">
    <section class="summary-grid">
      <article class="summary-card">
        <p>账单总金额</p>
        <h3>¥ {{ formatMoney(totalAmount) }}</h3>
      </article>
      <article class="summary-card">
        <p>实收金额</p>
        <h3>¥ {{ formatMoney(totalPaid) }}</h3>
      </article>
      <article class="summary-card">
        <p>待收金额</p>
        <h3>¥ {{ formatMoney(totalAmount - totalPaid) }}</h3>
      </article>
      <article class="summary-card">
        <p>收缴率</p>
        <h3>{{ collectionRateText }}</h3>
      </article>
    </section>

    <section class="panel">
      <div class="panel-header">
        <div>
          <h3>费用管理</h3>
        </div>
        <div class="actions">
          <el-input
            v-model="keyword"
            placeholder="搜索房产号/房主/缴费类型/业务标识"
            clearable
            style="width: 280px"
          />
          <el-select v-model="businessTypeFilter" clearable placeholder="缴费类型" style="width: 140px" @change="loadData">
            <el-option label="物业费" :value="1" />
            <el-option label="停车费" :value="2" />
            <el-option label="维修费" :value="3" />
          </el-select>
          <el-select v-model="statusFilter" clearable placeholder="状态" style="width: 130px" @change="loadData">
            <el-option label="待缴费" :value="0" />
            <el-option label="部分缴费" :value="1" />
            <el-option label="已缴清" :value="2" />
          </el-select>
          <el-button :loading="loading" @click="loadData">刷新</el-button>
        </div>
      </div>

      <div class="bill-tools">
        <el-button type="primary" @click="openCreate">新增物业费账单</el-button>
        <el-input v-model="generateForm.billPeriod" placeholder="账期 YYYY-MM" style="width: 150px" />
        <el-input-number
          v-model="generateForm.propertyId"
          :min="1"
          :controls="false"
          placeholder="房产ID"
          style="width: 130px"
        />
        <el-button :loading="generating" @click="handleGenerate">生成账单</el-button>
        <el-input-number
          v-model="annualForm.propertyId"
          :min="1"
          :controls="false"
          placeholder="房产ID"
          style="width: 130px"
        />
        <el-input v-model="annualForm.startPeriod" placeholder="起始账期 YYYY-MM" style="width: 160px" />
        <el-button :loading="discounting" @click="handleAnnualDiscount">年付折扣</el-button>
      </div>

      <el-table :data="displayList" stripe v-loading="loading">
        <el-table-column prop="businessTypeText" label="缴费类型" min-width="110" />
        <el-table-column prop="propertyCode" label="房产号" min-width="150" />
        <el-table-column prop="ownerName" label="房主" min-width="120" />
        <el-table-column prop="businessRef" label="业务标识" min-width="180" show-overflow-tooltip />
        <el-table-column prop="subjectName" label="缴费主体" min-width="220" show-overflow-tooltip />
        <el-table-column label="应收金额" min-width="120">
          <template #default="{ row }">¥ {{ formatMoney(row.amount) }}</template>
        </el-table-column>
        <el-table-column label="已收金额" min-width="120">
          <template #default="{ row }">¥ {{ formatMoney(row.paidAmount) }}</template>
        </el-table-column>
        <el-table-column prop="needPoints" label="应付积分" min-width="100" />
        <el-table-column label="状态" min-width="110">
          <template #default="{ row }">
            <el-tag :type="resolveStatusType(row)">{{ row.statusText || resolveStatus(row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="到期时间" min-width="170">
          <template #default="{ row }">{{ formatTime(row.dueDate) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :disabled="Number(row.businessType) !== 1" @click="openEdit(row)">
              编辑
            </el-button>
            <el-button type="danger" link :disabled="Number(row.businessType) !== 1" @click="removeBill(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑物业费账单' : '新增物业费账单'" width="640px">
      <el-form label-width="110px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="房产ID">
              <el-input-number v-model="form.propertyId" :min="1" :controls="false" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="账期">
              <el-input v-model="form.billPeriod" placeholder="YYYY-MM" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="面积快照">
              <el-input-number v-model="form.areaSnapshot" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单价">
              <el-input-number v-model="form.unitPrice" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="折扣金额">
              <el-input-number v-model="form.discountAmount" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="折前金额">
              <el-input-number v-model="form.amount" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="已收金额">
              <el-input-number v-model="form.paidAmount" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="form.status" style="width: 100%">
                <el-option label="待缴费" :value="0" />
                <el-option label="部分缴费" :value="1" />
                <el-option label="已缴清" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="到期时间">
              <el-date-picker
                v-model="form.dueDate"
                type="datetime"
                value-format="YYYY-MM-DDTHH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitBill">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  applyAnnualDiscount,
  createBill,
  deleteBill,
  generateBills,
  getBills,
  getCollectionRate,
  getPaymentSubjects,
  updateBill
} from '@/api/fee'

const loading = ref(false)
const saving = ref(false)
const generating = ref(false)
const discounting = ref(false)
const dialogVisible = ref(false)
const keyword = ref('')
const businessTypeFilter = ref(null)
const statusFilter = ref(null)
const list = ref([])
const billMap = ref(new Map())
const collectionRate = ref(null)
const generateForm = ref({ billPeriod: currentPeriod(), propertyId: null })
const annualForm = ref({ propertyId: null, startPeriod: currentPeriod() })
const form = ref(defaultForm())

const displayList = computed(() => {
  if (!keyword.value.trim()) return list.value
  const text = keyword.value.trim()
  return list.value.filter(
    (item) =>
      String(item.propertyCode || '').includes(text) ||
      String(item.ownerName || '').includes(text) ||
      String(item.businessTypeText || '').includes(text) ||
      String(item.businessRef || '').includes(text) ||
      String(item.subjectName || '').includes(text)
  )
})

const totalAmount = computed(() => list.value.reduce((sum, item) => sum + Number(item.amount || 0), 0))
const totalPaid = computed(() => list.value.reduce((sum, item) => sum + Number(item.paidAmount || 0), 0))
const collectionRateText = computed(() => {
  if (collectionRate.value?.collectionRate !== undefined) {
    return `${Number(collectionRate.value.collectionRate || 0).toFixed(2)}%`
  }
  if (!totalAmount.value) return '0.00%'
  return `${((totalPaid.value / totalAmount.value) * 100).toFixed(2)}%`
})

function currentPeriod() {
  const now = new Date()
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
}

function defaultForm() {
  return {
    id: null,
    propertyId: null,
    billPeriod: currentPeriod(),
    areaSnapshot: null,
    unitPrice: 5,
    discountAmount: 0,
    amount: null,
    paidAmount: 0,
    status: 0,
    dueDate: ''
  }
}

function formatMoney(value) {
  return Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function formatTime(value) {
  if (!value) return '--'
  return String(value).replace('T', ' ').slice(0, 19)
}

function isPaid(row) {
  const type = Number(row.businessType || 0)
  const status = Number(row.status || 0)
  if (type === 1) return status === 2
  return status === 1
}

function resolveStatus(row) {
  if (isPaid(row)) return '已缴清'
  if (Number(row.businessType || 0) === 1 && Number(row.status || 0) === 1) return '部分缴费'
  return '待缴费'
}

function resolveStatusType(row) {
  const statusText = row.statusText || resolveStatus(row)
  if (String(statusText).includes('已缴')) return 'success'
  if (String(statusText).includes('部分')) return 'warning'
  return 'danger'
}

function normalizeRows(rows) {
  return (Array.isArray(rows) ? rows : []).map((item) => ({
    ...item,
    propertyCode: item.propertyCode || '--',
    ownerName: item.ownerName || '--',
    subjectName: item.subjectName || `${item.propertyCode || '--'} / ${item.ownerName || '--'}`,
    needPoints: Number(item.needPoints || 0)
  }))
}

function toIsoDateTime(value) {
  if (!value) return ''
  return String(value).replace(' ', 'T').slice(0, 19)
}

function billPayload() {
  return {
    propertyId: form.value.propertyId,
    billPeriod: form.value.billPeriod,
    areaSnapshot: form.value.areaSnapshot,
    unitPrice: form.value.unitPrice,
    discountAmount: form.value.discountAmount,
    amount: form.value.amount,
    paidAmount: form.value.paidAmount,
    status: form.value.status,
    dueDate: form.value.dueDate || null
  }
}

async function loadData() {
  loading.value = true
  try {
    const params = {
      businessType: businessTypeFilter.value ?? undefined,
      status: statusFilter.value ?? undefined
    }
    const [subjectRes, billRes, rateRes] = await Promise.all([
      getPaymentSubjects(params),
      getBills({ status: statusFilter.value ?? undefined }),
      getCollectionRate()
    ])
    list.value = normalizeRows(subjectRes.data)
    billMap.value = new Map((Array.isArray(billRes.data) ? billRes.data : []).map((bill) => [Number(bill.id), bill]))
    collectionRate.value = rateRes.data || null
  } finally {
    loading.value = false
  }
}

function openCreate() {
  form.value = defaultForm()
  dialogVisible.value = true
}

function openEdit(row) {
  if (Number(row.businessType) !== 1) return
  const bill = billMap.value.get(Number(row.businessId)) || {}
  form.value = {
    ...defaultForm(),
    ...bill,
    id: row.businessId,
    propertyId: bill.propertyId || row.propertyId,
    billPeriod: bill.billPeriod || row.businessRef,
    amount: Number(bill.amount ?? row.amount ?? 0) + Number(bill.discountAmount ?? 0),
    paidAmount: Number(bill.paidAmount ?? row.paidAmount ?? 0),
    discountAmount: Number(bill.discountAmount ?? 0),
    unitPrice: Number(bill.unitPrice ?? 5),
    areaSnapshot: bill.areaSnapshot == null ? null : Number(bill.areaSnapshot),
    status: Number(bill.status ?? row.status ?? 0),
    dueDate: toIsoDateTime(bill.dueDate || row.dueDate)
  }
  dialogVisible.value = true
}

async function submitBill() {
  if (!form.value.propertyId || !form.value.billPeriod) {
    ElMessage.warning('请填写房产ID和账期')
    return
  }
  saving.value = true
  try {
    if (form.value.id) {
      await updateBill(form.value.id, billPayload())
    } else {
      await createBill(billPayload())
    }
    ElMessage.success('账单已保存')
    dialogVisible.value = false
    await loadData()
  } finally {
    saving.value = false
  }
}

async function removeBill(row) {
  if (Number(row.businessType) !== 1) return
  await ElMessageBox.confirm(`确认删除物业费账单 #${row.businessId}？`, '删除确认', { type: 'warning' })
  await deleteBill(row.businessId)
  ElMessage.success('账单已删除')
  await loadData()
}

async function handleGenerate() {
  if (!generateForm.value.billPeriod) {
    ElMessage.warning('请填写账期')
    return
  }
  generating.value = true
  try {
    const res = await generateBills({
      billPeriod: generateForm.value.billPeriod,
      propertyId: generateForm.value.propertyId || undefined
    })
    ElMessage.success(`已生成 ${Array.isArray(res.data) ? res.data.length : 0} 条账单`)
    await loadData()
  } finally {
    generating.value = false
  }
}

async function handleAnnualDiscount() {
  if (!annualForm.value.propertyId || !annualForm.value.startPeriod) {
    ElMessage.warning('请填写房产ID和起始账期')
    return
  }
  discounting.value = true
  try {
    await applyAnnualDiscount({ ...annualForm.value })
    ElMessage.success('年付折扣已应用')
    await loadData()
  } finally {
    discounting.value = false
  }
}

onMounted(loadData)
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

.summary-card,
.panel {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
}

.summary-card {
  padding: 16px;
}

.summary-card p {
  margin: 0 0 8px;
  color: #64748b;
  font-size: 13px;
}

.summary-card h3 {
  margin: 0;
  font-size: 22px;
}

.panel {
  padding: 16px;
}

.panel-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 14px;
}

.panel-header h3 {
  margin: 0;
  font-size: 16px;
}

.panel-header p {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 13px;
}

.actions,
.bill-tools {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: flex-end;
}

.bill-tools {
  justify-content: flex-start;
  padding: 12px;
  margin-bottom: 14px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
}

@media (max-width: 1100px) {
  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .panel-header {
    display: block;
  }

  .actions {
    justify-content: flex-start;
    margin-top: 12px;
  }
}
</style>

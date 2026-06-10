<template>
  <div class="page-grid">
    <section class="summary-grid">
      <article class="summary-card">
        <p>快递总数</p>
        <h3>{{ rows.length }}</h3>
      </article>
      <article class="summary-card">
        <p>待取件</p>
        <h3>{{ pendingCount }}</h3>
      </article>
      <article class="summary-card">
        <p>已取件</p>
        <h3>{{ pickedCount }}</h3>
      </article>
      <article class="summary-card">
        <p>今日入库</p>
        <h3>{{ todayCount }}</h3>
      </article>
    </section>

    <section class="panel">
      <div class="panel-header">
        <h3>快递管理</h3>
        <div class="actions">
          <el-input
            v-model="keyword"
            placeholder="搜索快递公司、单号、收件人、手机号"
            clearable
            style="width: 280px"
          />
          <el-select v-model="companyFilter" placeholder="快递公司" clearable style="width: 160px">
            <el-option v-for="item in companyOptions" :key="item" :label="item" :value="item" />
          </el-select>
          <el-select v-model="statusFilter" placeholder="取件状态" clearable style="width: 140px">
            <el-option label="待取件" :value="0" />
            <el-option label="已取件" :value="1" />
          </el-select>
          <el-button :loading="loading" @click="loadData">刷新</el-button>
          <el-button type="primary" @click="openCreate">新增快递</el-button>
        </div>
      </div>

      <el-table :data="rows" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="84" />
        <el-table-column prop="propertyCode" label="房产号" min-width="120" />
        <el-table-column prop="ownerName" label="业主" min-width="100" />
        <el-table-column prop="courierCompany" label="快递公司" min-width="120" />
        <el-table-column prop="trackingNo" label="快递单号" min-width="170" show-overflow-tooltip />
        <el-table-column prop="pickupCode" label="取件码" min-width="110" />
        <el-table-column prop="shelfLocation" label="存放位置" min-width="120" />
        <el-table-column prop="recipientName" label="收件人" min-width="100" />
        <el-table-column prop="recipientPhone" label="联系电话" min-width="120" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="Number(row.status) === 1 ? 'success' : 'warning'">
              {{ row.statusText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="入库时间" min-width="170">
          <template #default="{ row }">{{ formatTime(row.arrivedTime) }}</template>
        </el-table-column>
        <el-table-column label="取件时间" min-width="170">
          <template #default="{ row }">{{ formatTime(row.pickupTime) }}</template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="dialogVisible" :title="dialogMode === 'create' ? '新增快递' : '编辑快递'" width="720px">
      <el-form :model="form" label-width="100px">
        <div class="form-grid">
          <el-form-item label="房产ID">
            <el-input-number v-model="form.propertyId" :min="1" controls-position="right" style="width: 100%" />
          </el-form-item>
          <el-form-item label="快递公司">
            <el-input v-model="form.courierCompany" />
          </el-form-item>
          <el-form-item label="快递单号">
            <el-input v-model="form.trackingNo" />
          </el-form-item>
          <el-form-item label="取件码">
            <el-input v-model="form.pickupCode" />
          </el-form-item>
          <el-form-item label="收件人">
            <el-input v-model="form.recipientName" />
          </el-form-item>
          <el-form-item label="联系电话">
            <el-input v-model="form.recipientPhone" />
          </el-form-item>
          <el-form-item label="存放位置">
            <el-input v-model="form.shelfLocation" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="form.status" style="width: 100%">
              <el-option label="待取件" :value="0" />
              <el-option label="已取件" :value="1" />
            </el-select>
          </el-form-item>
          <el-form-item label="入库时间">
            <el-date-picker
              v-model="form.arrivedTime"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              format="YYYY-MM-DD HH:mm:ss"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="取件时间">
            <el-date-picker
              v-model="form.pickupTime"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              format="YYYY-MM-DD HH:mm:ss"
              clearable
              style="width: 100%"
            />
          </el-form-item>
        </div>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createExpressPackage, deleteExpressPackage, getExpressPackages, updateExpressPackage } from '@/api/express'

const loading = ref(false)
const saving = ref(false)
const keyword = ref('')
const statusFilter = ref(undefined)
const companyFilter = ref('')
const rows = ref([])
const dialogVisible = ref(false)
const dialogMode = ref('create')
const editingId = ref(null)
const form = reactive(emptyForm())

const companyOptions = computed(() => [...new Set(rows.value.map((item) => item.courierCompany).filter(Boolean))])
const pendingCount = computed(() => rows.value.filter((item) => Number(item.status) !== 1).length)
const pickedCount = computed(() => rows.value.filter((item) => Number(item.status) === 1).length)
const todayCount = computed(() => {
  const today = new Date().toISOString().slice(0, 10)
  return rows.value.filter((item) => String(item.arrivedTime || '').startsWith(today)).length
})

function emptyForm() {
  return {
    propertyId: undefined,
    courierCompany: '',
    trackingNo: '',
    pickupCode: '',
    recipientName: '',
    recipientPhone: '',
    shelfLocation: '',
    status: 0,
    arrivedTime: '',
    pickupTime: '',
    remark: ''
  }
}

function resetForm() {
  Object.assign(form, emptyForm())
  form.arrivedTime = formatNow()
}

function formatNow() {
  const date = new Date()
  const pad = (value) => String(value).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

function formatTime(value) {
  if (!value) {
    return '--'
  }
  return String(value).replace('T', ' ').slice(0, 19)
}

async function loadData() {
  loading.value = true
  try {
    const { data } = await getExpressPackages({
      keyword: keyword.value || undefined,
      company: companyFilter.value || undefined,
      status: statusFilter.value
    })
    rows.value = Array.isArray(data) ? data : []
  } finally {
    loading.value = false
  }
}

function openCreate() {
  dialogMode.value = 'create'
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  dialogMode.value = 'edit'
  editingId.value = row.id
  Object.assign(form, {
    propertyId: row.propertyId,
    courierCompany: row.courierCompany || '',
    trackingNo: row.trackingNo || '',
    pickupCode: row.pickupCode || '',
    recipientName: row.recipientName || '',
    recipientPhone: row.recipientPhone || '',
    shelfLocation: row.shelfLocation || '',
    status: Number(row.status || 0),
    arrivedTime: formatTime(row.arrivedTime) === '--' ? '' : formatTime(row.arrivedTime),
    pickupTime: formatTime(row.pickupTime) === '--' ? '' : formatTime(row.pickupTime),
    remark: row.remark || ''
  })
  dialogVisible.value = true
}

async function submitForm() {
  if (!form.propertyId || !form.courierCompany || !form.trackingNo || !form.arrivedTime) {
    ElMessage.warning('请补全房产ID、快递公司、快递单号和入库时间')
    return
  }
  saving.value = true
  try {
    const payload = { ...form, pickupTime: form.pickupTime || null }
    if (dialogMode.value === 'create') {
      await createExpressPackage(payload)
      ElMessage.success('快递已新增')
    } else {
      await updateExpressPackage(editingId.value, payload)
      ElMessage.success('快递已更新')
    }
    dialogVisible.value = false
    await loadData()
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除快递单号 ${row.trackingNo} 吗？`, '删除确认', { type: 'warning' })
  await deleteExpressPackage(row.id)
  ElMessage.success('已删除')
  await loadData()
}

onMounted(() => {
  resetForm()
  loadData()
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

.summary-card,
.panel {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
}

.summary-card {
  padding: 18px;
}

.summary-card p {
  margin: 0;
  color: #64748b;
  font-size: 13px;
}

.summary-card h3 {
  margin: 10px 0 0;
  font-size: 28px;
  line-height: 1.1;
}

.panel {
  padding: 16px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.panel-header h3 {
  margin: 0;
}

.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  justify-content: flex-end;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

@media (max-width: 1200px) {
  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .summary-grid,
  .form-grid {
    grid-template-columns: 1fr;
  }

  .panel-header {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>

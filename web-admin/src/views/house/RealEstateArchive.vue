<template>
  <div class="page-grid">
    <section class="summary-grid">
      <article class="summary-card">
        <p>房产总数</p>
        <h3>{{ summary.totalProperties || 0 }}</h3>
      </article>
      <article class="summary-card">
        <p>楼栋数</p>
        <h3>{{ summary.buildingCount || 0 }}</h3>
      </article>
      <article class="summary-card">
        <p>已入住 / 已出租</p>
        <h3>{{ summary.occupiedCount || 0 }} / {{ summary.rentedCount || 0 }}</h3>
      </article>
      <article class="summary-card">
        <p>空置房源</p>
        <h3>{{ summary.vacantCount || 0 }}</h3>
      </article>
    </section>

    <section class="panel">
      <div class="panel-header">
        <h3>房间管理</h3>
        <div class="actions">
          <el-input
            v-model="keyword"
            placeholder="按房产号/楼栋/单元/房号/业主/租户搜索"
            clearable
            class="keyword-input"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          />
          <el-select v-model="statusFilter" clearable placeholder="状态" style="width: 140px" @change="handleSearch">
            <el-option label="未售" :value="1" />
            <el-option label="已售" :value="2" />
            <el-option label="空置" :value="3" />
            <el-option label="已入住" :value="4" />
            <el-option label="已出租" :value="5" />
          </el-select>
          <el-button @click="loadData">刷新</el-button>
          <el-button type="primary" @click="openCreate">新增档案</el-button>
        </div>
      </div>

      <div class="content-grid">
        <div class="building-panel">
          <div class="sub-title">楼栋概览</div>
          <el-table :data="buildingStats" max-height="520" stripe>
            <el-table-column prop="building" label="楼栋" min-width="110" />
            <el-table-column prop="propertyCount" label="房产数" width="92" />
            <el-table-column prop="occupiedCount" label="入住" width="92" />
            <el-table-column label="入住率" min-width="120">
              <template #default="{ row }">{{ row.occupancyRate }}%</template>
            </el-table-column>
          </el-table>
        </div>

        <div class="table-panel">
          <el-table class="room-table" :data="properties" stripe v-loading="loading">
            <el-table-column prop="propertyCode" label="房产号" min-width="150" show-overflow-tooltip />
            <el-table-column prop="community" label="小区" min-width="120" show-overflow-tooltip />
            <el-table-column prop="building" label="楼栋" width="86" />
            <el-table-column prop="unit" label="单元" width="86" />
            <el-table-column prop="room" label="房号" width="86" />
            <el-table-column prop="ownerName" label="业主" min-width="110" show-overflow-tooltip />
            <el-table-column prop="tenantName" label="租户" min-width="110" show-overflow-tooltip>
              <template #default="{ row }">{{ row.tenantName || '--' }}</template>
            </el-table-column>
            <el-table-column prop="area" label="面积(m²)" width="108" />
            <el-table-column label="状态" width="110">
              <template #default="{ row }">
                <el-tag :type="statusMeta(row.status).type">{{ statusMeta(row.status).text }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
                <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-bar">
            <div class="page-shortcuts">
              <el-button :disabled="pageNum <= 1 || loading" @click="goFirstPage">首页</el-button>
              <el-button :disabled="pageNum >= lastPage || loading" @click="goLastPage">尾页</el-button>
            </div>
            <el-pagination
              v-model:current-page="pageNum"
              v-model:page-size="pageSize"
              :total="total"
              :page-sizes="[20, 50, 100]"
              :disabled="loading"
              background
              layout="total, sizes, prev, pager, next, jumper"
              @current-change="handlePageChange"
              @size-change="handleSizeChange"
            />
          </div>
        </div>
      </div>
    </section>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑房产档案' : '新增房产档案'" width="760px">
      <el-form label-width="96px">
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="小区"><el-input v-model="form.community" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="楼栋"><el-input v-model="form.building" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="单元"><el-input v-model="form.unit" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="房号"><el-input v-model="form.room" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="业主"><el-input v-model="form.ownerName" /></el-form-item></el-col>
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
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="面积">
              <el-input-number v-model="form.area" :min="0" :precision="2" style="width: 100%" />
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
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addHouse, deleteHouse, updateHouse } from '@/api/house'
import { getRealEstateArchive } from '@/api/realEstate'

const loading = ref(false)
const saving = ref(false)
const keyword = ref('')
const statusFilter = ref(null)
const summary = ref({})
const buildingStats = ref([])
const properties = ref([])
const dialogVisible = ref(false)
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)

const lastPage = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)))

const form = ref({
  id: null,
  community: '',
  building: '',
  unit: '',
  room: '',
  ownerName: '',
  tenantName: '',
  rentEndTime: '',
  area: 0,
  status: 3
})

function statusMeta(status) {
  const map = {
    1: { text: '未售', type: 'danger' },
    2: { text: '已售', type: 'warning' },
    3: { text: '空置', type: 'info' },
    4: { text: '已入住', type: 'success' },
    5: { text: '已出租', type: 'primary' }
  }
  return map[Number(status)] || { text: '未知', type: 'info' }
}

function resetForm() {
  form.value = {
    id: null,
    community: '',
    building: '',
    unit: '',
    room: '',
    ownerName: '',
    tenantName: '',
    rentEndTime: '',
    area: 0,
    status: 3
  }
}

function openCreate() {
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  form.value = {
    id: row.id,
    community: row.community || '',
    building: row.building || '',
    unit: row.unit || '',
    room: row.room || '',
    ownerName: row.ownerName || '',
    tenantName: row.tenantName || '',
    rentEndTime: row.rentEndTime || '',
    area: Number(row.area || 0),
    status: Number(row.status || 3)
  }
  dialogVisible.value = true
}

async function loadData() {
  loading.value = true
  try {
    const res = await getRealEstateArchive({
      keyword: keyword.value.trim() || undefined,
      status: statusFilter.value || undefined,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    summary.value = res.data?.summary || {}
    buildingStats.value = Array.isArray(res.data?.buildingStats) ? res.data.buildingStats : []
    properties.value = Array.isArray(res.data?.properties) ? res.data.properties : []
    total.value = Number(res.data?.total || 0)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadData()
}

function handlePageChange(value) {
  pageNum.value = value
  loadData()
}

function handleSizeChange(value) {
  pageSize.value = value
  pageNum.value = 1
  loadData()
}

function goFirstPage() {
  if (pageNum.value === 1) return
  pageNum.value = 1
  loadData()
}

function goLastPage() {
  const target = lastPage.value
  if (pageNum.value === target) return
  pageNum.value = target
  loadData()
}

async function submit() {
  if (!form.value.community || !form.value.building || !form.value.unit || !form.value.room) {
    ElMessage.warning('请填写完整房产位置')
    return
  }
  if (Number(form.value.status) === 5 && (!form.value.tenantName || !form.value.rentEndTime)) {
    ElMessage.warning('已出租状态需要填写租户和租约到期')
    return
  }
  saving.value = true
  const payload = { ...form.value }
  try {
    if (form.value.id) {
      await updateHouse(form.value.id, payload)
      ElMessage.success('房产档案已更新')
    } else {
      await addHouse(payload)
      ElMessage.success('房产档案已创建')
    }
    dialogVisible.value = false
    await loadData()
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除房产档案 ${row.propertyCode || row.id} 吗？`, '提示', { type: 'warning' })
  await deleteHouse(row.id)
  ElMessage.success('已删除')
  await loadData()
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
  border-radius: 12px;
}

.summary-card {
  padding: 16px;
}

.summary-card p {
  margin: 0;
  color: #64748b;
  font-size: 13px;
}

.summary-card h3 {
  margin: 8px 0 0;
  font-size: 28px;
  color: #0f172a;
}

.panel {
  padding: 16px;
}

.panel-header,
.actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.panel-header {
  margin-bottom: 16px;
}

.panel-header h3,
.sub-title {
  margin: 0;
  font-size: 16px;
}

.content-grid {
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  gap: 16px;
}

.building-panel {
  display: grid;
  gap: 10px;
}

.keyword-input {
  width: 320px;
}

.table-panel {
  min-width: 0;
  display: grid;
  gap: 14px;
}

.room-table {
  width: 100%;
}

.pagination-bar {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 2px;
  flex-wrap: wrap;
}

.page-shortcuts {
  display: flex;
  gap: 8px;
}

@media (max-width: 1200px) {
  .summary-grid,
  .content-grid {
    grid-template-columns: 1fr 1fr;
  }

  .panel-header {
    align-items: stretch;
    flex-direction: column;
  }

  .actions {
    justify-content: flex-start;
    flex-wrap: wrap;
  }
}

@media (max-width: 900px) {
  .summary-grid,
  .content-grid {
    grid-template-columns: 1fr;
  }

  .keyword-input {
    width: 100%;
  }

  .pagination-bar {
    justify-content: flex-start;
  }
}
</style>

<template>
  <div class="page-grid">
    <section class="panel">
      <div class="panel-header">
        <h3>人员定编管理</h3>
        <div class="actions">
          <el-input v-model="keyword" placeholder="搜索姓名/电话/擅长类型" clearable style="width: 260px" />
          <el-button type="primary" @click="openCreate">新增人员</el-button>
        </div>
      </div>

      <el-table :data="displayList" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="nickname" label="姓名" min-width="120" />
        <el-table-column prop="phone" label="电话" min-width="130" />
        <el-table-column label="人员类型" min-width="120">
          <template #default="{ row }">{{ staffTypeText(row.staffType) }}</template>
        </el-table-column>
        <el-table-column prop="position" label="岗位" min-width="120" />
        <el-table-column prop="shiftGroup" label="排班" width="90" />
        <el-table-column prop="specialties" label="擅长类型" min-width="220" show-overflow-tooltip />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.currentStatus)">{{ staffStatusText(row.currentStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="人脸状态" width="120">
          <template #default="{ row }">
            <el-tag :type="faceStatusType(row.id)">
              {{ faceStatusText(row.id) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="人脸注册" min-width="180">
          <template #default="{ row }">
            <el-upload
              class="face-uploader"
              :show-file-list="false"
              :auto-upload="false"
              accept=".jpg,.jpeg,.png,image/jpeg,image/png"
              :before-upload="(file) => handleFaceBeforeUpload(file, row.id)"
            >
              <el-button
                size="small"
                type="primary"
                plain
                :loading="faceUploadingId === row.id"
                :disabled="faceUploadingId !== null && faceUploadingId !== row.id"
              >
                {{ faceActionText(row.id) }}
              </el-button>
            </el-upload>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button size="small" text type="primary" @click="loadPerformance(row.id)">绩效</el-button>
            <el-button size="small" text type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" text type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <section v-if="perf" class="panel perf-panel">
      <h3>绩效详情</h3>
      <div class="perf-grid">
        <article>
          <p>人员ID</p>
          <h4>{{ perf.workerId }}</h4>
        </article>
        <article>
          <p>完成工单</p>
          <h4>{{ perf.completedCount }}</h4>
        </article>
        <article>
          <p>服务评分</p>
          <h4>{{ perf.rating }}</h4>
        </article>
        <article>
          <p>估算收入</p>
          <h4>￥{{ formatMoney(perf.income) }}</h4>
        </article>
      </div>
    </section>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑人员' : '新增人员'" width="640px">
      <el-form label-width="110px">
        <el-form-item label="姓名">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="人员类型">
          <el-select v-model="form.staffType" style="width: 100%">
            <el-option :value="1" label="固定人员-家政保洁" />
            <el-option :value="2" label="维修-水工" />
            <el-option :value="3" label="维修-电工" />
            <el-option :value="4" label="维修-家电" />
            <el-option :value="5" label="外包合作公司" />
          </el-select>
        </el-form-item>
        <el-form-item label="岗位名称">
          <el-input v-model="form.position" placeholder="如：水电维修员、家政保洁员" />
        </el-form-item>
        <el-form-item label="排班组">
          <el-select v-model="form.shiftGroup" style="width: 100%">
            <el-option value="A" label="A班" />
            <el-option value="B" label="B班" />
            <el-option value="C" label="C班" />
          </el-select>
        </el-form-item>
        <el-form-item label="资质证书">
          <el-input
            v-model="form.certificates"
            type="textarea"
            :rows="2"
            placeholder="逗号分隔，如：电工证,高空作业证"
          />
        </el-form-item>
        <el-form-item label="擅长类型">
          <el-input
            v-model="form.skills"
            type="textarea"
            :rows="3"
            placeholder="逗号分隔，如：水电维修,下水道堵塞,空调不制冷"
          />
        </el-form-item>
        <el-form-item label="日最大接单">
          <el-input-number v-model="form.maxDailyOrders" :min="1" :max="30" style="width: 100%" />
        </el-form-item>
        <el-form-item label="在岗状态">
          <el-select v-model="form.currentStatus" style="width: 100%">
            <el-option :value="1" label="空闲" />
            <el-option :value="2" label="忙碌" />
            <el-option :value="3" label="休息/离线" />
          </el-select>
        </el-form-item>
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
import {
  addWorker,
  deleteWorker,
  getWorkerPerformance,
  getWorkerStaffingList,
  saveWorkerStaffing,
  updateWorker
} from '@/api/worker'
import { getWorkerFaceStatus, registerWorkerFace } from '@/api/face'

const keyword = ref('')
const list = ref([])
const perf = ref(null)
const dialogVisible = ref(false)
const saving = ref(false)
const faceUploadingId = ref(null)
const faceStatusMap = ref({})

const form = ref({
  id: null,
  nickname: '',
  phone: '',
  staffType: 2,
  position: '',
  shiftGroup: 'A',
  skills: '',
  certificates: '',
  maxDailyOrders: 5,
  currentStatus: 1
})

const displayList = computed(() => {
  if (!keyword.value.trim()) return list.value
  const text = keyword.value.trim()
  return list.value.filter((item) => {
    return (
      String(item.nickname || '').includes(text) ||
      String(item.phone || '').includes(text) ||
      String(item.specialties || '').includes(text)
    )
  })
})

function resetForm() {
  form.value = {
    id: null,
    nickname: '',
    phone: '',
    staffType: 2,
    position: '',
    shiftGroup: 'A',
    skills: '',
    certificates: '',
    maxDailyOrders: 5,
    currentStatus: 1
  }
}

function openCreate() {
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  form.value = {
    id: row.id,
    nickname: row.nickname || '',
    phone: row.phone || '',
    staffType: Number(row.staffType || 2),
    position: row.position || '',
    shiftGroup: row.shiftGroup || 'A',
    skills: row.specialties || '',
    certificates: row.certificates || '',
    maxDailyOrders: Number(row.maxDailyOrders || 5),
    currentStatus: Number(row.currentStatus || 1)
  }
  dialogVisible.value = true
}

function staffTypeText(type) {
  const map = {
    1: '固定人员-家政保洁',
    2: '维修-水工',
    3: '维修-电工',
    4: '维修-家电',
    5: '外包合作公司'
  }
  return map[Number(type)] || '--'
}

function staffStatusText(status) {
  const map = { 1: '空闲', 2: '忙碌', 3: '休息' }
  return map[Number(status)] || '--'
}

function statusTagType(status) {
  const s = Number(status)
  if (s === 1) return 'success'
  if (s === 2) return 'warning'
  if (s === 3) return 'info'
  return 'info'
}

function formatMoney(value) {
  return Number(value || 0).toLocaleString('zh-CN')
}

function faceStatusText(workerId) {
  if (faceStatusMap.value[workerId] === undefined) return '检测中'
  return faceStatusMap.value[workerId] ? '已注册' : '未注册'
}

function faceStatusType(workerId) {
  if (faceStatusMap.value[workerId] === undefined) return 'info'
  return faceStatusMap.value[workerId] ? 'success' : 'warning'
}

function faceActionText(workerId) {
  return faceStatusMap.value[workerId] ? '重新注册' : '上传人脸'
}

function validateFaceFile(file) {
  const name = String(file?.name || '')
  const extOk = /\.(jpg|jpeg|png)$/i.test(name)
  const type = String(file?.type || '').toLowerCase()
  const typeOk = type === 'image/jpeg' || type === 'image/png'
  if (!extOk && !typeOk) {
    ElMessage.error('仅支持 JPG/PNG 图片')
    return false
  }
  const isLt2m = Number(file?.size || 0) / 1024 / 1024 < 2
  if (!isLt2m) {
    ElMessage.error('图片大小不能超过 2MB')
    return false
  }
  return true
}

function fileToBase64(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => {
      const result = typeof reader.result === 'string' ? reader.result : ''
      const base64 = result.includes(',') ? result.split(',')[1] : result
      if (!base64) {
        reject(new Error('图片读取失败'))
        return
      }
      resolve(base64)
    }
    reader.onerror = () => reject(new Error('图片读取失败'))
    reader.readAsDataURL(file)
  })
}

async function loadFaceStatus(workers) {
  if (!workers.length) {
    faceStatusMap.value = {}
    return
  }
  const entries = await Promise.all(
    workers.map(async (worker) => {
      try {
        const res = await getWorkerFaceStatus(worker.id)
        return [worker.id, !!res.data?.registered]
      } catch {
        return [worker.id, false]
      }
    })
  )
  faceStatusMap.value = Object.fromEntries(entries)
}

async function loadWorkers() {
  const res = await getWorkerStaffingList()
  const workers = Array.isArray(res.data) ? res.data : []
  list.value = workers
  await loadFaceStatus(workers)
}

async function submit() {
  if (!form.value.nickname?.trim()) {
    ElMessage.warning('请填写姓名')
    return
  }
  saving.value = true
  try {
    const payload = {
      nickname: form.value.nickname,
      phone: form.value.phone,
      skills: form.value.skills,
      staffType: Number(form.value.staffType || 2),
      position: form.value.position,
      shiftGroup: form.value.shiftGroup,
      certificates: form.value.certificates,
      maxDailyOrders: Number(form.value.maxDailyOrders || 5),
      currentStatus: Number(form.value.currentStatus || 1)
    }
    if (form.value.id) {
      await updateWorker(form.value.id, payload)
      await saveWorkerStaffing({
        workerId: form.value.id,
        staffType: payload.staffType,
        position: payload.position,
        shiftGroup: payload.shiftGroup,
        certificates: payload.certificates,
        specialties: payload.skills,
        maxDailyOrders: payload.maxDailyOrders,
        currentStatus: payload.currentStatus
      })
      ElMessage.success('更新成功')
    } else {
      await addWorker(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await loadWorkers()
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除人员 #${row.id} 吗？`, '提示', { type: 'warning' })
  await deleteWorker(row.id)
  ElMessage.success('删除成功')
  await loadWorkers()
}

async function loadPerformance(id) {
  const res = await getWorkerPerformance(id)
  perf.value = {
    workerId: id,
    completedCount: res.data?.completedCount ?? 0,
    rating: res.data?.rating ?? '--',
    income: res.data?.income ?? 0
  }
}

async function handleFaceBeforeUpload(file, workerId) {
  if (!validateFaceFile(file)) return false
  if (!workerId) {
    ElMessage.error('缺少维修员ID')
    return false
  }
  if (faceUploadingId.value !== null) {
    ElMessage.warning('正在上传中，请稍候')
    return false
  }
  const wasRegistered = !!faceStatusMap.value[workerId]
  faceUploadingId.value = workerId
  try {
    const imageBase64 = await fileToBase64(file)
    await registerWorkerFace(workerId, imageBase64)
    faceStatusMap.value = { ...faceStatusMap.value, [workerId]: true }
    ElMessage.success(wasRegistered ? '人脸重新注册成功' : '人脸注册成功')
  } catch (error) {
    ElMessage.error(error?.message || '人脸注册失败')
  } finally {
    faceUploadingId.value = null
  }
  return false
}

onMounted(async () => {
  await loadWorkers()
})
</script>

<style scoped>
.page-grid {
  display: grid;
  gap: 16px;
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

.actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.face-uploader {
  display: inline-flex;
}

.perf-panel h3 {
  margin: 0 0 12px;
  font-size: 16px;
}

.perf-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.perf-grid article {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 12px;
}

.perf-grid p {
  margin: 0;
  color: #64748b;
  font-size: 13px;
}

.perf-grid h4 {
  margin: 8px 0 0;
  font-size: 22px;
}

@media (max-width: 1024px) {
  .perf-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .panel-header {
    flex-direction: column;
    align-items: stretch;
  }

  .actions {
    flex-direction: column;
  }

  .perf-grid {
    grid-template-columns: 1fr;
  }
}
</style>


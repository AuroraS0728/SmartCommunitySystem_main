<template>
  <div class="page-grid">
    <section class="summary-grid">
      <article class="summary-card">
        <p>业主总数</p>
        <h3>{{ ownerRows.length }}</h3>
      </article>
      <article class="summary-card">
        <p>积分总量</p>
        <h3>{{ totalPoints }}</h3>
      </article>
      <article class="summary-card">
        <p>平均积分</p>
        <h3>{{ averagePoints }}</h3>
      </article>
    </section>

    <section class="panel">
      <div class="panel-header">
        <h3>积分与画像</h3>
        <el-input
          v-model="keyword"
          placeholder="按昵称、房产号、手机号搜索"
          clearable
          style="max-width: 280px"
        />
      </div>

      <el-table v-loading="loading" :data="displayList" stripe>
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="nickname" label="业主昵称" min-width="140" />
        <el-table-column prop="account" label="房产号" min-width="150" />
        <el-table-column prop="phone" label="手机号" min-width="130" />
        <el-table-column label="家庭画像" min-width="190">
          <template #default="{ row }">
            <div class="profile-tags">
              <el-tag v-if="Number(row.hasElderly) === 1" type="warning">老人</el-tag>
              <el-tag v-if="Number(row.hasChild) === 1" type="success">儿童</el-tag>
              <el-tag v-if="Number(row.hasPet) === 1" type="info">宠物</el-tag>
              <span
                v-if="Number(row.hasElderly) !== 1 && Number(row.hasChild) !== 1 && Number(row.hasPet) !== 1"
                class="muted"
              >
                未填写
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="房屋信息" min-width="130">
          <template #default="{ row }">
            {{ row.houseArea ?? '-' }}㎡ / {{ row.roomCount ?? '-' }}间
          </template>
        </el-table-column>
        <el-table-column prop="points" label="当前积分" min-width="120" />
        <el-table-column label="状态" min-width="110">
          <template #default="{ row }">
            <el-tag :type="Number(row.status) === 1 ? 'success' : 'danger'">
              {{ Number(row.status) === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" min-width="180" />
        <el-table-column label="操作" width="190" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openRecharge(row)">积分充值</el-button>
            <el-button type="primary" link @click="openProfile(row)">画像编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="rechargeVisible" title="积分充值" width="420px" destroy-on-close>
      <el-form label-width="88px">
        <el-form-item label="业主">
          <span>{{ rechargeForm.userName }}</span>
        </el-form-item>
        <el-form-item label="充值积分">
          <el-input-number
            v-model="rechargeForm.amount"
            :min="1"
            :max="10000"
            :step="1"
            step-strictly
            controls-position="right"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="rechargeForm.remark" maxlength="255" show-word-limit placeholder="如：物业赠送" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rechargeVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitRecharge">确认充值</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="profileVisible" title="编辑业主画像" width="460px" destroy-on-close>
      <el-form label-width="96px">
        <el-form-item label="业主">
          <span>{{ profileForm.userName }}</span>
        </el-form-item>
        <el-form-item label="家庭成员">
          <div class="switch-group">
            <el-switch v-model="profileForm.hasElderly" active-text="老人" />
            <el-switch v-model="profileForm.hasChild" active-text="儿童" />
            <el-switch v-model="profileForm.hasPet" active-text="宠物" />
          </div>
        </el-form-item>
        <el-form-item label="房屋面积">
          <el-input-number
            v-model="profileForm.houseArea"
            :min="0"
            :max="10000"
            :step="1"
            step-strictly
            controls-position="right"
          />
          <span class="unit">㎡</span>
        </el-form-item>
        <el-form-item label="房间数">
          <el-input-number
            v-model="profileForm.roomCount"
            :min="0"
            :max="127"
            :step="1"
            step-strictly
            controls-position="right"
          />
          <span class="unit">间</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="profileVisible = false">取消</el-button>
        <el-button type="primary" :loading="profileLoading" @click="submitProfile">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getUsers, updateUserProfile } from '@/api/user'
import { rechargePoints } from '@/api/points'

const loading = ref(false)
const submitLoading = ref(false)
const keyword = ref('')
const ownerRows = ref([])
const rechargeVisible = ref(false)
const profileVisible = ref(false)
const profileLoading = ref(false)
const rechargeForm = ref({
  userId: null,
  userName: '',
  amount: 100,
  remark: '物业充值'
})
const profileForm = ref({
  userId: null,
  userName: '',
  hasElderly: false,
  hasChild: false,
  hasPet: false,
  houseArea: null,
  roomCount: null
})

const displayList = computed(() => {
  const text = keyword.value.trim().toLowerCase()
  if (!text) return ownerRows.value
  return ownerRows.value.filter((row) => {
    return (
      String(row.nickname || '').toLowerCase().includes(text) ||
      String(row.account || '').toLowerCase().includes(text) ||
      String(row.phone || '').toLowerCase().includes(text)
    )
  })
})

const totalPoints = computed(() => ownerRows.value.reduce((sum, item) => sum + Number(item.points || 0), 0))
const averagePoints = computed(() => {
  if (!ownerRows.value.length) return 0
  return Math.round(totalPoints.value / ownerRows.value.length)
})

function formatTime(value) {
  if (!value) return '--'
  if (typeof value === 'string') return value.replace('T', ' ').slice(0, 19)
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '--'
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  const hh = String(date.getHours()).padStart(2, '0')
  const mm = String(date.getMinutes()).padStart(2, '0')
  const ss = String(date.getSeconds()).padStart(2, '0')
  return `${y}-${m}-${d} ${hh}:${mm}:${ss}`
}

async function loadData() {
  loading.value = true
  try {
    const res = await getUsers({ pageNum: 1, pageSize: 1000, role: 1 })
    const records = Array.isArray(res?.data?.records) ? res.data.records : []
    ownerRows.value = records.map((item) => ({
      ...item,
      nickname: item.nickname || '-',
      account: item.account || '-',
      phone: item.phone || '-',
      points: Number(item.points || 0),
      createTime: formatTime(item.createTime)
    }))
  } finally {
    loading.value = false
  }
}

function openRecharge(row) {
  rechargeForm.value = {
    userId: row.id,
    userName: row.nickname || row.account || `用户${row.id}`,
    amount: 100,
    remark: '物业充值'
  }
  rechargeVisible.value = true
}

function openProfile(row) {
  profileForm.value = {
    userId: row.id,
    userName: row.nickname || row.account || `用户${row.id}`,
    hasElderly: Number(row.hasElderly) === 1,
    hasChild: Number(row.hasChild) === 1,
    hasPet: Number(row.hasPet) === 1,
    houseArea: row.houseArea ?? null,
    roomCount: row.roomCount ?? null
  }
  profileVisible.value = true
}

async function submitRecharge() {
  const amount = Number(rechargeForm.value.amount || 0)
  if (!rechargeForm.value.userId) {
    ElMessage.error('未选择充值业主')
    return
  }
  if (!Number.isInteger(amount) || amount < 1 || amount > 10000) {
    ElMessage.error('充值积分需为 1 ~ 10000 的整数')
    return
  }
  submitLoading.value = true
  try {
    const res = await rechargePoints({
      userId: rechargeForm.value.userId,
      amount,
      remark: rechargeForm.value.remark || '物业充值'
    })
    ElMessage.success(`充值成功，最新积分：${res?.data?.balance ?? '-'}`)
    rechargeVisible.value = false
    await loadData()
  } finally {
    submitLoading.value = false
  }
}

async function submitProfile() {
  if (!profileForm.value.userId) {
    ElMessage.error('未选择业主')
    return
  }
  profileLoading.value = true
  try {
    await updateUserProfile(profileForm.value.userId, {
      hasElderly: profileForm.value.hasElderly ? 1 : 0,
      hasChild: profileForm.value.hasChild ? 1 : 0,
      hasPet: profileForm.value.hasPet ? 1 : 0,
      houseArea: profileForm.value.houseArea,
      roomCount: profileForm.value.roomCount
    })
    ElMessage.success('画像信息已保存')
    profileVisible.value = false
    await loadData()
  } finally {
    profileLoading.value = false
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
  grid-template-columns: repeat(3, minmax(0, 1fr));
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

.profile-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.muted {
  color: #94a3b8;
}

.switch-group {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
}

.unit {
  margin-left: 8px;
  color: #64748b;
}

@media (max-width: 960px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }
}
</style>

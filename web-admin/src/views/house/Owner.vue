<template>
  <div class="page-grid">
    <section class="summary-grid">
      <article class="summary-card">
        <p>业主总数</p>
        <h3>{{ ownerRows.length }}</h3>
      </article>
      <article class="summary-card">
        <p>已认证业主</p>
        <h3>{{ verifiedCount }}</h3>
      </article>
      <article class="summary-card">
        <p>本月新增</p>
        <h3>{{ monthlyAdded }}</h3>
      </article>
    </section>

    <section class="panel">
      <div class="panel-header">
        <h3>业主信息</h3>
        <el-input v-model="keyword" placeholder="按姓名/房号搜索" clearable style="max-width: 260px" />
      </div>
      <el-table :data="displayList" stripe>
        <el-table-column prop="name" label="业主姓名" min-width="120" />
        <el-table-column prop="phone" label="联系电话" min-width="130" />
        <el-table-column prop="property" label="房屋信息" min-width="180" />
        <el-table-column prop="bindTime" label="绑定时间" min-width="160" />
        <el-table-column label="认证状态" min-width="120">
          <template #default="{ row }">
            <el-tag :type="row.verified ? 'success' : 'warning'">{{ row.verified ? '已认证' : '待认证' }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { getHouseList } from '@/api/house'

const keyword = ref('')
const ownerRows = ref([])

const fallbackOwners = [
  { name: '张敏', phone: '13800001234', property: '1号楼 1单元 101', bindTime: '2026-03-08 11:20', verified: true },
  { name: '李华', phone: '13900004567', property: '2号楼 2单元 1001', bindTime: '2026-03-15 09:40', verified: true },
  { name: '陈然', phone: '13700007890', property: '8号楼 2单元 201', bindTime: '2026-04-10 13:12', verified: false }
]

const displayList = computed(() => {
  if (!keyword.value.trim()) return ownerRows.value
  const text = keyword.value.trim()
  return ownerRows.value.filter((row) => row.name.includes(text) || row.property.includes(text))
})

const verifiedCount = computed(() => ownerRows.value.filter((item) => item.verified).length)
const monthlyAdded = computed(() => {
  const now = new Date()
  const monthKey = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
  return ownerRows.value.filter((item) => String(item.bindTime).startsWith(monthKey)).length
})

async function loadData() {
  try {
    const res = await getHouseList()
    const source = Array.isArray(res.data) ? res.data : []
    const rows = source
      .filter((item) => item.ownerName)
      .map((item) => ({
        name: item.ownerName,
        phone: item.ownerPhone || '--',
        property: `${item.building || '--'} ${item.unit || '--'} ${item.room || '--'}`,
        bindTime: item.bindTime || item.createTime || '--',
        verified: item.verified ?? true
      }))
    ownerRows.value = rows.length ? rows : fallbackOwners
  } catch (error) {
    ownerRows.value = fallbackOwners
  }
}

onMounted(async () => {
  await loadData()
})
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

@media (max-width: 960px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }
}
</style>

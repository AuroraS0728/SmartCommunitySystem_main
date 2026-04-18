<template>
  <div class="page-grid">
    <section class="summary-grid">
      <article class="summary-card">
        <p>楼栋总数</p>
        <h3>{{ buildingCount }}</h3>
      </article>
      <article class="summary-card">
        <p>房屋总数</p>
        <h3>{{ houseCount }}</h3>
      </article>
      <article class="summary-card">
        <p>已入住户数</p>
        <h3>{{ occupiedCount }}</h3>
      </article>
      <article class="summary-card">
        <p>当前入住率</p>
        <h3>{{ occupancyRate }}%</h3>
      </article>
    </section>

    <section class="panel">
      <div class="panel-header">
        <h3>楼栋视图</h3>
        <el-input v-model="keyword" placeholder="按楼栋搜索，如 8号楼" clearable style="max-width: 260px" />
      </div>
      <el-table :data="displayList" stripe>
        <el-table-column prop="building" label="楼栋" min-width="140" />
        <el-table-column prop="houseTotal" label="房屋数" min-width="100" />
        <el-table-column prop="occupiedTotal" label="已入住" min-width="100" />
        <el-table-column label="入住率" min-width="180">
          <template #default="{ row }">
            <div class="progress-cell">
              <el-progress :percentage="row.rate" :show-text="false" :stroke-width="8" />
              <span>{{ row.rate }}%</span>
            </div>
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
const houseList = ref([])

const fallbackHouses = [
  { building: '1号楼', room: '101', ownerName: '张敏', occupied: true },
  { building: '1号楼', room: '102', ownerName: '', occupied: false },
  { building: '2号楼', room: '1001', ownerName: '李华', occupied: true },
  { building: '2号楼', room: '1002', ownerName: '王强', occupied: true },
  { building: '8号楼', room: '201', ownerName: '陈然', occupied: true },
  { building: '8号楼', room: '202', ownerName: '', occupied: false }
]

const buildingRows = computed(() => {
  const map = new Map()
  houseList.value.forEach((item) => {
    const building = item.building || '未分配楼栋'
    if (!map.has(building)) {
      map.set(building, { building, houseTotal: 0, occupiedTotal: 0, rate: 0 })
    }
    const row = map.get(building)
    row.houseTotal += 1
    if (isOccupied(item)) row.occupiedTotal += 1
    row.rate = row.houseTotal ? Number(((row.occupiedTotal / row.houseTotal) * 100).toFixed(1)) : 0
  })
  return Array.from(map.values())
})

const displayList = computed(() => {
  const text = keyword.value.trim()
  if (!text) return buildingRows.value
  return buildingRows.value.filter((item) => item.building.includes(text))
})

const buildingCount = computed(() => buildingRows.value.length)
const houseCount = computed(() => houseList.value.length)
const occupiedCount = computed(() => houseList.value.filter((item) => isOccupied(item)).length)
const occupancyRate = computed(() => {
  if (!houseCount.value) return 0
  return Number(((occupiedCount.value / houseCount.value) * 100).toFixed(1))
})

function isOccupied(item) {
  if (typeof item.occupied === 'boolean') return item.occupied
  if (item.ownerName) return true
  const status = String(item.status || '').toLowerCase()
  return status.includes('入住') || status.includes('occupied')
}

async function loadData() {
  try {
    const res = await getHouseList()
    houseList.value = Array.isArray(res.data) ? res.data : fallbackHouses
  } catch (error) {
    houseList.value = fallbackHouses
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
  grid-template-columns: repeat(4, minmax(0, 1fr));
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
  color: #0f172a;
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

.progress-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.progress-cell span {
  color: #475569;
  font-size: 12px;
  white-space: nowrap;
}

@media (max-width: 1200px) {
  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 680px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }
}
</style>

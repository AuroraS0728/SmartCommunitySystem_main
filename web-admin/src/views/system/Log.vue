<template>
  <div class="page-grid">
    <section class="panel">
      <div class="panel-header">
        <h3>操作审计日志</h3>
        <div class="actions">
          <el-input-number v-model="limit" :min="20" :max="1000" :step="20" />
          <el-button :loading="loading" @click="loadLogs">刷新</el-button>
        </div>
      </div>

      <el-alert type="info" :closable="false" show-icon>
        日志来源：后端文件 <code>logs/operation.log</code>（实时写入）
      </el-alert>

      <el-table :data="rows" stripe style="margin-top: 12px">
        <el-table-column prop="time" label="时间" width="210" />
        <el-table-column prop="level" label="级别" width="90">
          <template #default="{ row }">
            <el-tag :type="tagType(row.level)">{{ row.level }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容" min-width="680" />
      </el-table>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { getOperationLogs } from '@/api/system'

const loading = ref(false)
const limit = ref(200)
const rows = ref([])

function tagType(level) {
  if (level === 'ERROR') return 'danger'
  if (level === 'WARN') return 'warning'
  return 'success'
}

function parseLine(line) {
  const match = String(line).match(/^(\d{4}-\d{2}-\d{2}\s+\d{2}:\d{2}:\d{2}\.\d{3})\s+([A-Z]+)\s+(.*)$/)
  if (!match) {
    return { time: '--', level: 'INFO', content: String(line || '') }
  }
  return {
    time: match[1],
    level: match[2],
    content: match[3]
  }
}

async function loadLogs() {
  loading.value = true
  try {
    const res = await getOperationLogs({ limit: limit.value })
    const lines = Array.isArray(res.data) ? res.data : []
    rows.value = lines.map(parseLine).reverse()
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await loadLogs()
})
</script>

<style scoped>
.page-grid {
  display: grid;
}

.panel {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 16px;
}

.panel-header {
  margin-bottom: 12px;
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

@media (max-width: 760px) {
  .panel-header {
    flex-direction: column;
    align-items: stretch;
  }

  .actions {
    flex-direction: column;
  }
}
</style>

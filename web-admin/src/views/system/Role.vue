<template>
  <div class="page-grid">
    <section class="panel">
      <div class="panel-header">
        <h3>角色权限矩阵</h3>
      </div>
      <el-table :data="roles" stripe>
        <el-table-column prop="name" label="角色名称" min-width="140" />
        <el-table-column prop="scope" label="数据范围" min-width="220" />
        <el-table-column prop="routes" label="可访问路由" min-width="260" />
        <el-table-column label="状态" min-width="120">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '启用中' : '已停用' }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </section>
  </div>
</template>

<script setup>
const roles = [
  {
    name: '管理员',
    scope: '全部楼栋与运营数据',
    routes: '全部后台模块',
    enabled: true
  },
  {
    name: '物业客服',
    scope: '公告、访客、工单',
    routes: '/notice/*, /visitor/*, /repair/*',
    enabled: true
  },
  {
    name: '维修人员',
    scope: '工单派发与处理记录',
    routes: '/repair/list, /worker/manage',
    enabled: true
  },
  {
    name: '审计员',
    scope: '只读日志与统计',
    routes: '/dashboard, /system/log',
    enabled: false
  }
]
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
  margin-bottom: 14px;
}

.panel-header h3 {
  margin: 0;
  font-size: 16px;
}
</style>

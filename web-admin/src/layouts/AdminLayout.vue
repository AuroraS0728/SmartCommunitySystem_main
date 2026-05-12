<template>
  <div class="admin-shell">
    <aside class="shell-sidebar">
      <div class="brand">
        <div class="brand-logo">智</div>
        <div class="brand-text">
          <p class="brand-title">智慧社区管理</p>
          <p class="brand-subtitle">Smart Community System</p>
        </div>
      </div>

      <nav class="menu">
        <template v-for="group in menuGroups" :key="group.title">
          <p class="menu-group-title">{{ group.title }}</p>
          <router-link
            v-for="item in group.items"
            :key="item.path"
            :to="item.path"
            class="menu-item"
            :class="{ active: isActive(item.path) }"
          >
            {{ item.label }}
          </router-link>
        </template>
      </nav>

      <div class="sidebar-footer">
        <div class="avatar">{{ username.slice(0, 1) }}</div>
        <div>
          <p class="username">{{ username }}</p>
          <p class="role-text">{{ roleText }}</p>
        </div>
      </div>
    </aside>

    <main class="shell-main">
      <header class="shell-header">
        <div>
          <h1>{{ pageTitle }}</h1>
          <p class="header-subtitle">{{ pageSubtitle }}</p>
        </div>
        <div class="header-right">
          <span class="time-text">{{ currentTime }}</span>
          <el-button type="primary" plain @click="handleLogout">退出登录</el-button>
        </div>
      </header>

      <section class="shell-content">
        <router-view />
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { logout } from '@/api/auth'
import { useUserStore } from '@/store'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const currentTime = ref('')
let timer = null

const menuGroups = [
  {
    title: '核心业务',
    items: [
      { label: '数据看板', path: '/dashboard' },
      { label: '房产档案', path: '/house/archive' },
      { label: '业主管理', path: '/house/owner' },
      { label: '智能工单管控', path: '/repair/smart' },
      { label: '费用管理', path: '/fee/manage' },
      { label: '智能运营', path: '/innovation/ops' },
      { label: '活动专区', path: '/activity/manage' }
    ]
  },
  {
    title: '服务运营',
    items: [
      { label: '公告管理', path: '/notice/manage' },
      { label: '访客管理', path: '/visitor/record' },
      { label: '家政维修管理', path: '/worker/manage' },
      { label: '催缴待办', path: '/operation/tasks' },
      { label: '信用分明细', path: '/credit/log' },
      { label: '推荐规则配置', path: '/recommend/rules' }
    ]
  },
  {
    title: '系统设置',
    items: [
      { label: '角色权限', path: '/system/role' },
      { label: '操作日志', path: '/system/log' },
      { label: '积分充值记录', path: '/system/points-recharge' }
    ]
  }
]

const pageTitle = computed(() => route.meta.title || '智慧社区管理系统')
const pageSubtitle = computed(() => route.meta.subtitle || '统一管理社区资产、服务与运营数据')

const username = computed(() => userStore.userInfo?.nickname || userStore.userInfo?.name || '系统管理员')
const roleText = computed(() => userStore.userInfo?.roleName || '管理员')

function isActive(path) {
  return route.path === path
}

function updateTime() {
  const now = new Date()
  const text = `${now.getFullYear()}年${String(now.getMonth() + 1).padStart(2, '0')}月${String(now.getDate()).padStart(2, '0')}日 ${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}:${String(now.getSeconds()).padStart(2, '0')}`
  currentTime.value = text
}

async function handleLogout() {
  try {
    await logout()
  } catch (error) {
    // ignore server logout failure and continue local logout
  }
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}

onMounted(() => {
  updateTime()
  timer = setInterval(updateTime, 1000)
})

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.admin-shell {
  min-height: 100vh;
  display: flex;
  background: #f3f6fb;
}

.shell-sidebar {
  width: 252px;
  background: #0f172a;
  color: #cbd5e1;
  display: flex;
  flex-direction: column;
  padding: 16px 14px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 6px;
  margin-bottom: 16px;
}

.brand-logo {
  width: 40px;
  height: 40px;
  border-radius: 11px;
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  color: #fff;
  font-size: 18px;
  font-weight: 700;
  display: grid;
  place-items: center;
}

.brand-title {
  margin: 0;
  color: #fff;
  font-weight: 700;
}

.brand-subtitle {
  margin: 3px 0 0;
  color: #64748b;
  font-size: 12px;
}

.menu {
  flex: 1;
  overflow-y: auto;
  padding-right: 4px;
}

.menu-group-title {
  margin: 14px 6px 8px;
  color: #64748b;
  font-size: 12px;
  letter-spacing: 0.08em;
}

.menu-item {
  display: block;
  margin-bottom: 4px;
  border-radius: 8px;
  padding: 10px;
  color: inherit;
  text-decoration: none;
  font-size: 14px;
  transition: background-color 0.2s ease, color 0.2s ease;
}

.menu-item:hover {
  background: #1e293b;
  color: #fff;
}

.menu-item.active {
  background: #2563eb;
  color: #fff;
  font-weight: 600;
}

.sidebar-footer {
  border-top: 1px solid #1e293b;
  margin-top: 12px;
  padding: 14px 8px 6px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: #1e293b;
  color: #f8fafc;
  display: grid;
  place-items: center;
  font-size: 14px;
  font-weight: 700;
}

.username {
  margin: 0;
  color: #fff;
  font-size: 13px;
}

.role-text {
  margin: 2px 0 0;
  color: #64748b;
  font-size: 12px;
}

.shell-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.shell-header {
  height: 68px;
  background: #fff;
  border-bottom: 1px solid #e2e8f0;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.shell-header h1 {
  margin: 0;
  color: #0f172a;
  font-size: 20px;
}

.header-subtitle {
  margin: 4px 0 0;
  color: #64748b;
  font-size: 13px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 14px;
}

.time-text {
  color: #475569;
  font-size: 14px;
}

.shell-content {
  flex: 1;
  overflow-y: auto;
  padding: 22px;
}

@media (max-width: 920px) {
  .admin-shell {
    flex-direction: column;
  }

  .shell-sidebar {
    width: 100%;
    max-height: 300px;
    padding-bottom: 10px;
  }

  .shell-header {
    height: auto;
    padding: 12px;
    gap: 8px;
    flex-wrap: wrap;
  }

  .shell-content {
    padding: 14px;
  }

  .time-text {
    font-size: 12px;
  }
}
</style>

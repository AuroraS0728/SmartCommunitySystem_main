import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', component: () => import('@/views/login/Login.vue') },
  {
    path: '/',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        component: () => import('@/views/dashboard/Index.vue'),
        meta: { title: '系统概览', subtitle: '查看社区核心经营数据与服务实时状态' }
      },
      {
        path: 'house/building',
        component: () => import('@/views/house/Building.vue'),
        meta: { title: '楼栋管理', subtitle: '按楼栋维度查看房屋、入住率和设施分布' }
      },
      {
        path: 'house/property',
        component: () => import('@/views/house/Property.vue'),
        meta: { title: '房屋管理', subtitle: '维护房屋档案和业主绑定关系' }
      },
      {
        path: 'house/owner',
        component: () => import('@/views/house/Owner.vue'),
        meta: { title: '业主管理', subtitle: '管理业主账户、认证信息和联系资料' }
      },
      {
        path: 'repair/list',
        component: () => import('@/views/repair/RepairList.vue'),
        meta: { title: '工单管理', subtitle: '查看报修工单状态并跟踪处理进度' }
      },
      {
        path: 'fee/manage',
        component: () => import('@/views/fee/FeeManage.vue'),
        meta: { title: '费用管理', subtitle: '追踪账单收缴状态与欠费风险' }
      },
      {
        path: 'notice/manage',
        component: () => import('@/views/notice/NoticeManage.vue'),
        meta: { title: '公告管理', subtitle: '统一维护社区公告和发布时间' }
      },
      {
        path: 'visitor/record',
        component: () => import('@/views/visitor/VisitorRecord.vue'),
        meta: { title: '访客管理', subtitle: '查看访客预约记录和通行有效期' }
      },
      {
        path: 'worker/manage',
        component: () => import('@/views/worker/WorkerManage.vue'),
        meta: { title: '家政维修管理', subtitle: '管理服务人员和绩效表现' }
      },
      {
        path: 'system/role',
        component: () => import('@/views/system/Role.vue'),
        meta: { title: '角色权限', subtitle: '配置系统角色和资源访问范围' }
      },
      {
        path: 'system/log',
        component: () => import('@/views/system/Log.vue'),
        meta: { title: '操作日志', subtitle: '审计后台关键操作与风险行为' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _, next) => {
  const token = localStorage.getItem('token')
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  const isPropertyAdmin = Number(userInfo?.role) === 2
  if (token && !isPropertyAdmin) {
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    if (to.path !== '/login') return next('/login')
  }
  if (to.path !== '/login' && !token) return next('/login')
  if (to.path === '/login' && token && isPropertyAdmin) return next('/dashboard')
  next()
})

export default router

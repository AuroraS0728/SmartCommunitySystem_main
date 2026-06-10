<template>
  <div class="login-page">
    <div class="login-deco"></div>
    <div class="login-card">
      <div class="brand">
        <div class="brand-logo">慧</div>
        <div>
          <h2>智慧社区管理系统</h2>
        </div>
      </div>

      <el-form :model="form" label-width="74px">
        <el-form-item label="账号">
          <el-input v-model.trim="form.account" placeholder="请输入 WTGL 开头账号" clearable />
        </el-form-item>
        <el-form-item label="密码">
          <el-input
            v-model.trim="form.password"
            type="password"
            show-password
            placeholder="请输入 6 位倒序密码"
            @keyup.enter="doLogin"
          />
        </el-form-item>
      </el-form>

      <el-button type="primary" :loading="submitting" @click="doLogin" style="width: 100%">登录</el-button>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { accountLogin } from '@/api/auth'
import { useUserStore } from '@/store'

const router = useRouter()
const userStore = useUserStore()
const submitting = ref(false)
const form = reactive({ account: '', password: '' })

async function doLogin() {
  if (!form.account || !form.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }
  submitting.value = true
  try {
    const res = await accountLogin({ account: form.account, password: form.password, role: 2 })
    if (Number(res.data?.userInfo?.role) !== 2) {
      userStore.logout()
      ElMessage.error('仅支持物业管理端登录')
      return
    }
    userStore.setLogin(res.data)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (error) {
    ElMessage.error(error.message || '登录失败，请检查账号或密码')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background:
    radial-gradient(60% 60% at 15% 18%, rgba(37, 99, 235, 0.24), transparent 65%),
    radial-gradient(50% 50% at 85% 80%, rgba(30, 64, 175, 0.2), transparent 70%),
    linear-gradient(145deg, #0f172a, #111827 45%, #1e293b);
  padding: 20px;
  position: relative;
}

.login-deco {
  position: absolute;
  width: min(840px, 92vw);
  height: min(520px, 62vh);
  border-radius: 28px;
  background:
    linear-gradient(135deg, rgba(37, 99, 235, 0.18), rgba(59, 130, 246, 0.03)),
    linear-gradient(45deg, rgba(15, 23, 42, 0.4), rgba(30, 41, 59, 0.2));
  border: 1px solid rgba(148, 163, 184, 0.2);
  box-shadow: 0 30px 80px -40px rgba(15, 23, 42, 0.7);
}

.login-card {
  width: 460px;
  max-width: 100%;
  background: rgba(255, 255, 255, 0.94);
  border: 1px solid rgba(226, 232, 240, 0.8);
  border-radius: 16px;
  padding: 26px 24px 24px;
  box-shadow: 0 26px 60px -34px rgba(15, 23, 42, 0.8);
  z-index: 1;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.brand-logo {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  color: #fff;
  font-size: 20px;
  font-weight: 700;
}

.brand h2 {
  margin: 0;
  color: #0f172a;
  font-size: 22px;
}

.brand p {
  margin: 4px 0 0;
  color: #64748b;
  font-size: 13px;
}

.hint {
  margin: 4px 0 14px;
  color: #64748b;
  font-size: 12px;
}
</style>

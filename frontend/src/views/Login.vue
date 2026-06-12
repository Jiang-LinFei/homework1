<template>
  <div class="login-wrap">
    <div class="login-hero">
      <div class="brand">
        <el-icon :size="34"><Monitor /></el-icon>
        <span>机房预约管理系统</span>
      </div>
      <h1>按时间段 / 课表预约机房<br />智能冲突检测 · 占用一目了然</h1>
      <ul class="features">
        <li><el-icon><Calendar /></el-icon> 占用日历 · 预约与排课统一视图</li>
        <li><el-icon><CircleCheck /></el-icon> 审批工作流 · 教师/管理员审核</li>
        <li><el-icon><DataLine /></el-icon> 利用率仪表盘 · 数据驱动决策</li>
      </ul>
    </div>

    <div class="login-panel">
      <div class="login-card">
        <h2>欢迎登录</h2>
        <p class="muted">请输入账号密码进入系统</p>
        <el-form @submit.prevent="onSubmit" :model="form" size="large">
          <el-form-item>
            <el-input
              v-model="form.username"
              placeholder="账号（如 admin / teacher / student）"
              :prefix-icon="User"
              @keyup.enter="onSubmit"
            />
          </el-form-item>
          <el-form-item>
            <el-input
              v-model="form.password"
              type="password"
              show-password
              placeholder="密码"
              :prefix-icon="Lock"
              @keyup.enter="onSubmit"
            />
          </el-form-item>
          <el-button
            type="primary"
            class="login-btn"
            :loading="loading"
            @click="onSubmit"
          >
            登 录
          </el-button>
        </el-form>
        <div class="demo-tip muted">
          演示账号：admin/admin · teacher/teacher · student/student
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Monitor, Calendar, CircleCheck, DataLine } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const form = reactive({ username: '', password: '' })
const loading = ref(false)
const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

async function onSubmit() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }
  loading.value = true
  try {
    const user = await auth.login(form.username, form.password)
    ElMessage.success(`欢迎，${user.name || user.username}`)
    const redirect = (route.query.redirect as string) || '/dashboard'
    router.replace(redirect)
  } catch (e: any) {
    if (e?.response?.status === 401) {
      ElMessage.error('账号或密码错误，请重新输入')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrap {
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  min-height: 100vh;
}
.login-hero {
  background: linear-gradient(135deg, #1d4ed8 0%, #2563eb 55%, #059669 140%);
  color: #fff;
  padding: 64px 56px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 22px;
  font-weight: 700;
  margin-bottom: 40px;
}
.login-hero h1 {
  font-size: 34px;
  line-height: 1.4;
  margin: 0 0 36px;
}
.features {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  gap: 16px;
  font-size: 16px;
  opacity: 0.95;
}
.features li {
  display: flex;
  align-items: center;
  gap: 10px;
}
.login-panel {
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-background);
  padding: 32px;
}
.login-card {
  width: 100%;
  max-width: 380px;
  background: #fff;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  padding: 40px 36px;
}
.login-card h2 {
  margin: 0 0 6px;
  font-size: 24px;
}
.login-card .muted {
  margin: 0 0 24px;
}
.login-btn {
  width: 100%;
  letter-spacing: 4px;
}
.demo-tip {
  margin-top: 18px;
  font-size: 12px;
  text-align: center;
}
@media (max-width: 860px) {
  .login-wrap {
    grid-template-columns: 1fr;
  }
  .login-hero {
    display: none;
  }
}
</style>

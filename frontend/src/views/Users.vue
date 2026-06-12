<template>
  <div>
    <div class="page-toolbar">
      <h2 class="page-title">用户管理</h2>
      <el-button type="primary" :icon="Plus" @click="openDialog()">新增用户</el-button>
    </div>

    <div class="page-card">
      <el-table :data="rows" v-loading="loading" stripe>
        <el-table-column prop="username" label="账号" min-width="120" />
        <el-table-column prop="name" label="姓名" min-width="120" />
        <el-table-column label="角色" width="110">
          <template #default="{ row }">
            <el-tag :type="roleType(row.role)">{{ row.role_display }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="电话" min-width="130" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.is_active ? 'success' : 'info'">
              {{ row.is_active ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialog" :title="form.id ? '编辑用户' : '新增用户'" width="440px">
      <el-form :model="form" label-width="84px">
        <el-form-item label="账号"><el-input v-model="form.username" :disabled="!!form.id" /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role">
            <el-option label="管理员" value="admin" />
            <el-option label="教师" value="teacher" />
            <el-option label="学生" value="student" />
          </el-select>
        </el-form-item>
        <el-form-item label="电话"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password
            :placeholder="form.id ? '留空则不修改' : '默认 123456'" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.is_active" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { usersApi } from '@/api'

const rows = ref<any[]>([])
const loading = ref(false)
const dialog = ref(false)
const form = reactive<any>({})

function roleType(r: string) {
  return r === 'admin' ? 'danger' : r === 'teacher' ? 'warning' : 'primary'
}

async function load() {
  loading.value = true
  try {
    const { data } = await usersApi.list({ page_size: 100 })
    rows.value = data.results
  } finally {
    loading.value = false
  }
}

function openDialog(row?: any) {
  Object.keys(form).forEach((k) => delete form[k])
  Object.assign(form, row
    ? { ...row, password: '' }
    : { role: 'student', is_active: true, password: '' })
  dialog.value = true
}

async function save() {
  const payload = { ...form }
  if (!payload.password) delete payload.password
  if (form.id) await usersApi.update(form.id, payload)
  else await usersApi.create(payload)
  ElMessage.success('保存成功')
  dialog.value = false
  load()
}

async function remove(row: any) {
  await ElMessageBox.confirm(`确定删除用户「${row.username}」？`, '提示', { type: 'warning' })
  await usersApi.remove(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>

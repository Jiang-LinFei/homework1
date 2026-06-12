<template>
  <div>
    <div class="page-toolbar">
      <h2 class="page-title">机房管理</h2>
      <el-button type="primary" :icon="Plus" @click="openDialog()">新增机房</el-button>
    </div>

    <div class="page-card">
      <el-table :data="rows" v-loading="loading" stripe>
        <el-table-column prop="name" label="机房名称" min-width="140" />
        <el-table-column prop="location" label="位置" min-width="120" />
        <el-table-column prop="capacity" label="容量" width="80" />
        <el-table-column label="开放时段" min-width="140">
          <template #default="{ row }">{{ row.open_time }} - {{ row.close_time }}</template>
        </el-table-column>
        <el-table-column prop="seat_count" label="机位数" width="90" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ row.status_display }}</el-tag>
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

    <el-dialog v-model="dialog" :title="form.id ? '编辑机房' : '新增机房'" width="480px">
      <el-form :model="form" label-width="84px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="位置"><el-input v-model="form.location" /></el-form-item>
        <el-form-item label="容量">
          <el-input-number v-model="form.capacity" :min="0" />
        </el-form-item>
        <el-form-item label="开放时间">
          <el-time-picker v-model="form.open_time" value-format="HH:mm:ss" />
        </el-form-item>
        <el-form-item label="关闭时间">
          <el-time-picker v-model="form.close_time" value-format="HH:mm:ss" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option label="开放" value="open" />
            <el-option label="关闭" value="closed" />
            <el-option label="维护中" value="maintenance" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.description" type="textarea" /></el-form-item>
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
import { roomsApi } from '@/api'

const rows = ref<any[]>([])
const loading = ref(false)
const dialog = ref(false)
const form = reactive<any>({})

function statusType(s: string) {
  return s === 'open' ? 'success' : s === 'maintenance' ? 'warning' : 'info'
}

async function load() {
  loading.value = true
  try {
    const { data } = await roomsApi.list({ page_size: 100 })
    rows.value = data.results
  } finally {
    loading.value = false
  }
}

function openDialog(row?: any) {
  Object.keys(form).forEach((k) => delete form[k])
  Object.assign(form, row
    ? { ...row }
    : { capacity: 0, open_time: '08:00:00', close_time: '22:00:00', status: 'open' })
  dialog.value = true
}

async function save() {
  if (form.id) await roomsApi.update(form.id, form)
  else await roomsApi.create(form)
  ElMessage.success('保存成功')
  dialog.value = false
  load()
}

async function remove(row: any) {
  await ElMessageBox.confirm(`确定删除机房「${row.name}」？`, '提示', { type: 'warning' })
  await roomsApi.remove(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>

<template>
  <div>
    <div class="page-toolbar">
      <h2 class="page-title">节次管理</h2>
      <el-button type="primary" :icon="Plus" @click="openDialog()">新增节次</el-button>
    </div>

    <div class="page-card">
      <el-table :data="rows" v-loading="loading" stripe>
        <el-table-column prop="order" label="排序" width="80" />
        <el-table-column prop="name" label="名称" min-width="140" />
        <el-table-column prop="start_time" label="开始" min-width="100" />
        <el-table-column prop="end_time" label="结束" min-width="100" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialog" :title="form.id ? '编辑节次' : '新增节次'" width="420px">
      <el-form :model="form" label-width="84px">
        <el-form-item label="名称"><el-input v-model="form.name" placeholder="如 第1-2节" /></el-form-item>
        <el-form-item label="开始时间">
          <el-time-picker v-model="form.start_time" value-format="HH:mm:ss" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-time-picker v-model="form.end_time" value-format="HH:mm:ss" />
        </el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.order" :min="0" /></el-form-item>
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
import { timeslotsApi } from '@/api'

const rows = ref<any[]>([])
const loading = ref(false)
const dialog = ref(false)
const form = reactive<any>({})

async function load() {
  loading.value = true
  try {
    const { data } = await timeslotsApi.list({ page_size: 100 })
    rows.value = data.results
  } finally {
    loading.value = false
  }
}

function openDialog(row?: any) {
  Object.keys(form).forEach((k) => delete form[k])
  Object.assign(form, row ? { ...row } : { order: rows.value.length + 1 })
  dialog.value = true
}

async function save() {
  if (form.id) await timeslotsApi.update(form.id, form)
  else await timeslotsApi.create(form)
  ElMessage.success('保存成功')
  dialog.value = false
  load()
}

async function remove(row: any) {
  await ElMessageBox.confirm(`确定删除节次「${row.name}」？`, '提示', { type: 'warning' })
  await timeslotsApi.remove(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>

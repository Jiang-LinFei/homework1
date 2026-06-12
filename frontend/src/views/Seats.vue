<template>
  <div>
    <div class="page-toolbar">
      <h2 class="page-title">机位管理</h2>
      <div style="display: flex; gap: 10px">
        <el-select v-model="roomId" placeholder="选择机房" style="width: 200px" @change="load">
          <el-option v-for="r in rooms" :key="r.id" :label="r.name" :value="r.id" />
        </el-select>
        <el-button type="primary" :icon="Plus" :disabled="!roomId" @click="openDialog()">
          新增机位
        </el-button>
      </div>
    </div>

    <div class="page-card">
      <el-table :data="rows" v-loading="loading" stripe>
        <el-table-column prop="room_name" label="所属机房" min-width="140" />
        <el-table-column prop="code" label="机位编号" min-width="120" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.status === 'available' ? 'success' : 'warning'">
              {{ row.status_display }}
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

    <el-dialog v-model="dialog" :title="form.id ? '编辑机位' : '新增机位'" width="420px">
      <el-form :model="form" label-width="84px">
        <el-form-item label="机房">
          <el-select v-model="form.room">
            <el-option v-for="r in rooms" :key="r.id" :label="r.name" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="编号"><el-input v-model="form.code" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option label="可用" value="available" />
            <el-option label="维护中" value="maintenance" />
          </el-select>
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
import { roomsApi, seatsApi } from '@/api'

const rooms = ref<any[]>([])
const rows = ref<any[]>([])
const roomId = ref<number>()
const loading = ref(false)
const dialog = ref(false)
const form = reactive<any>({})

async function load() {
  loading.value = true
  try {
    const params: any = { page_size: 200 }
    if (roomId.value) params.room = roomId.value
    const { data } = await seatsApi.list(params)
    rows.value = data.results
  } finally {
    loading.value = false
  }
}

function openDialog(row?: any) {
  Object.keys(form).forEach((k) => delete form[k])
  Object.assign(form, row ? { ...row } : { room: roomId.value, status: 'available' })
  dialog.value = true
}

async function save() {
  if (form.id) await seatsApi.update(form.id, form)
  else await seatsApi.create(form)
  ElMessage.success('保存成功')
  dialog.value = false
  load()
}

async function remove(row: any) {
  await ElMessageBox.confirm(`确定删除机位「${row.code}」？`, '提示', { type: 'warning' })
  await seatsApi.remove(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(async () => {
  const { data } = await roomsApi.list({ page_size: 100 })
  rooms.value = data.results
  if (rooms.value.length) roomId.value = rooms.value[0].id
  load()
})
</script>

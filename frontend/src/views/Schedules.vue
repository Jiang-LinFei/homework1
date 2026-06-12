<template>
  <div>
    <div class="page-toolbar">
      <h2 class="page-title">排课管理</h2>
      <el-button type="primary" :icon="Plus" @click="openDialog()">新增排课</el-button>
    </div>

    <div class="page-card">
      <el-table :data="rows" v-loading="loading" stripe>
        <el-table-column prop="course_name" label="课程" min-width="140" />
        <el-table-column prop="room_name" label="机房" min-width="130" />
        <el-table-column prop="teacher_name" label="教师" min-width="100" />
        <el-table-column prop="weekday_display" label="星期" width="90" />
        <el-table-column prop="timeslot_name" label="节次" min-width="140" />
        <el-table-column label="周次" width="110">
          <template #default="{ row }">{{ row.start_week }}-{{ row.end_week }} 周</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialog" :title="form.id ? '编辑排课' : '新增排课'" width="500px">
      <el-form :model="form" label-width="96px">
        <el-form-item label="课程名称"><el-input v-model="form.course_name" /></el-form-item>
        <el-form-item label="机房">
          <el-select v-model="form.room">
            <el-option v-for="r in rooms" :key="r.id" :label="r.name" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="教师">
          <el-select v-model="form.teacher" clearable>
            <el-option v-for="t in teachers" :key="t.id" :label="t.name || t.username" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="星期">
          <el-select v-model="form.weekday">
            <el-option v-for="d in weekdays" :key="d.v" :label="d.l" :value="d.v" />
          </el-select>
        </el-form-item>
        <el-form-item label="节次">
          <el-select v-model="form.timeslot">
            <el-option v-for="s in slots" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="起止周">
          <el-input-number v-model="form.start_week" :min="1" :max="30" /> 至
          <el-input-number v-model="form.end_week" :min="1" :max="30" />
        </el-form-item>
        <el-form-item label="学期开始日">
          <el-date-picker v-model="form.semester_start" type="date" value-format="YYYY-MM-DD"
            placeholder="第1周的周一" />
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
import { roomsApi, schedulesApi, timeslotsApi, usersApi } from '@/api'

const rows = ref<any[]>([])
const rooms = ref<any[]>([])
const slots = ref<any[]>([])
const teachers = ref<any[]>([])
const loading = ref(false)
const dialog = ref(false)
const form = reactive<any>({})
const weekdays = [
  { v: 1, l: '周一' }, { v: 2, l: '周二' }, { v: 3, l: '周三' }, { v: 4, l: '周四' },
  { v: 5, l: '周五' }, { v: 6, l: '周六' }, { v: 7, l: '周日' },
]

async function load() {
  loading.value = true
  try {
    const { data } = await schedulesApi.list({ page_size: 200 })
    rows.value = data.results
  } finally {
    loading.value = false
  }
}

function openDialog(row?: any) {
  Object.keys(form).forEach((k) => delete form[k])
  Object.assign(form, row
    ? { ...row }
    : { start_week: 1, end_week: 18, weekday: 1 })
  dialog.value = true
}

async function save() {
  if (form.id) await schedulesApi.update(form.id, form)
  else await schedulesApi.create(form)
  ElMessage.success('保存成功')
  dialog.value = false
  load()
}

async function remove(row: any) {
  await ElMessageBox.confirm(`确定删除排课「${row.course_name}」？`, '提示', { type: 'warning' })
  await schedulesApi.remove(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(async () => {
  const [r, s, u] = await Promise.all([
    roomsApi.list({ page_size: 100 }),
    timeslotsApi.list({ page_size: 100 }),
    usersApi.list({ role: 'teacher', page_size: 100 }),
  ])
  rooms.value = r.data.results
  slots.value = s.data.results
  teachers.value = u.data.results
  load()
})
</script>

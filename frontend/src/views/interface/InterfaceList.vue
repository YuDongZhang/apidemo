<template>
  <div class="interface-list-container">
    <div class="list-header">
      <h2>接口管理</h2>
      <el-button type="primary" @click="showCreateDialog">
        <el-icon><Plus /></el-icon> 新增接口
      </el-button>
    </div>

    <!-- Filter by Category -->
    <div class="filter-bar">
      <el-select v-model="selectedCategory" placeholder="按栏目筛选" clearable @change="loadInterfaces">
        <el-option label="全部" :value="null" />
        <el-option
          v-for="cat in categories"
          :key="cat.id"
          :label="cat.name"
          :value="cat.id"
        />
      </el-select>
    </div>

    <!-- Interface Cards -->
    <div class="interface-grid">
      <div
        v-for="item in interfaces"
        :key="item.id"
        class="interface-card"
        @click="goToDetail(item)"
      >
        <div class="card-header">
          <el-tag :type="getMethodTagType(item.method)" size="small">{{ item.method }}</el-tag>
          <span class="interface-name">{{ item.name }}</span>
        </div>
        <div class="card-path">{{ item.path }}</div>
        <div class="card-desc">{{ item.description || '暂无描述' }}</div>
        <div class="card-actions" @click.stop>
          <el-button type="primary" link size="small" @click="editInterface(item)">
            <el-icon><Edit /></el-icon>
          </el-button>
          <el-button type="danger" link size="small" @click="deleteItem(item)">
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
      </div>

      <div v-if="interfaces.length === 0" class="empty-state">
        <el-empty description="暂无接口，点击上方按钮添加" />
      </div>
    </div>

    <!-- Create/Edit Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingItem ? '编辑接口' : '新增接口'"
      width="500px"
    >
      <el-form :model="form" label-width="80px">
        <el-form-item label="接口名称" required>
          <el-input v-model="form.name" placeholder="如：获取用户列表" />
        </el-form-item>
        <el-form-item label="请求方法">
          <el-select v-model="form.method" style="width: 100%">
            <el-option label="GET" value="GET" />
            <el-option label="POST" value="POST" />
            <el-option label="PUT" value="PUT" />
            <el-option label="DELETE" value="DELETE" />
          </el-select>
        </el-form-item>
        <el-form-item label="接口路径">
          <el-input v-model="form.path" placeholder="/api/users" />
        </el-form-item>
        <el-form-item label="所属栏目">
          <el-select v-model="form.categoryId" placeholder="选择栏目" clearable style="width: 100%">
            <el-option
              v-for="cat in categories"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="接口描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveInterface">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { getInterfaces, createInterface, updateInterface, deleteInterface } from '../../api/api-interface'
import { getCategories } from '../../api/category'

const router = useRouter()

const interfaces = ref([])
const categories = ref([])
const selectedCategory = ref(null)
const dialogVisible = ref(false)
const editingItem = ref(null)
const saving = ref(false)

const form = reactive({
  name: '',
  method: 'GET',
  path: '',
  description: '',
  categoryId: null,
  sortOrder: 0
})

onMounted(() => {
  loadCategories()
  loadInterfaces()
})

const loadCategories = async () => {
  try {
    const res = await getCategories()
    categories.value = res.data
  } catch (error) {
    console.error(error)
  }
}

const loadInterfaces = async () => {
  try {
    const res = await getInterfaces(selectedCategory.value)
    interfaces.value = res.data
  } catch (error) {
    console.error(error)
  }
}

const getMethodTagType = (method) => {
  const types = {
    GET: 'success',
    POST: 'primary',
    PUT: 'warning',
    DELETE: 'danger'
  }
  return types[method] || 'info'
}

const showCreateDialog = () => {
  editingItem.value = null
  form.name = ''
  form.method = 'GET'
  form.path = ''
  form.description = ''
  form.categoryId = null
  form.sortOrder = 0
  dialogVisible.value = true
}

const editInterface = (item) => {
  editingItem.value = item
  form.name = item.name
  form.method = item.method
  form.path = item.path
  form.description = item.description
  form.categoryId = item.category?.id || null
  form.sortOrder = item.sortOrder
  dialogVisible.value = true
}

const saveInterface = async () => {
  if (!form.name) {
    ElMessage.warning('请输入接口名称')
    return
  }

  saving.value = true
  try {
    if (editingItem.value) {
      await updateInterface(editingItem.value.id, form)
      ElMessage.success('更新成功')
    } else {
      await createInterface(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadInterfaces()
  } catch (error) {
    console.error(error)
  } finally {
    saving.value = false
  }
}

const deleteItem = async (item) => {
  try {
    await ElMessageBox.confirm(`确定删除接口 "${item.name}" 吗？`, '提示', {
      type: 'warning'
    })
    await deleteInterface(item.id)
    ElMessage.success('删除成功')
    loadInterfaces()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const goToDetail = (item) => {
  router.push(`/interfaces/${item.id}`)
}
</script>

<style scoped>
.interface-list-container {
  min-height: 100%;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.list-header h2 {
  margin: 0;
  color: #303133;
}

.filter-bar {
  margin-bottom: 20px;
}

.interface-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.interface-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
}

.interface-card:hover {
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.interface-name {
  font-weight: 600;
  color: #303133;
}

.card-path {
  font-family: monospace;
  font-size: 13px;
  color: #909399;
  background: #f5f7fa;
  padding: 4px 8px;
  border-radius: 4px;
  margin-bottom: 8px;
}

.card-desc {
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-actions {
  position: absolute;
  top: 12px;
  right: 12px;
  opacity: 0;
  transition: opacity 0.3s;
}

.interface-card:hover .card-actions {
  opacity: 1;
}

.empty-state {
  grid-column: 1 / -1;
  padding: 40px;
}
</style>

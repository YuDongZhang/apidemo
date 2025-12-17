<template>
  <div class="layout-container">
    <!-- Left Sidebar - Categories -->
    <aside class="sidebar">
      <div class="sidebar-header">
        <h2 class="gradient-text">栏目管理</h2>
        <el-button type="primary" size="small" @click="showCategoryDialog()">
          <el-icon><Plus /></el-icon>
        </el-button>
      </div>
      
      <div class="category-list">
        <div
          v-for="category in categories"
          :key="category.id"
          class="category-item"
          :class="{ active: activeCategory === category.id }"
          @click="selectCategory(category)"
        >
          <span class="category-name">{{ category.name }}</span>
          <div class="category-actions">
            <el-button type="primary" link size="small" @click.stop="showCategoryDialog(category)">
              <el-icon><Edit /></el-icon>
            </el-button>
            <el-button type="danger" link size="small" @click.stop="handleDeleteCategory(category)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
    </aside>
    
    <!-- Middle - Feature List -->
    <main class="main-content">
      <div class="content-header">
        <h2>{{ currentCategory?.name || '全部' }} - 功能接口</h2>
        <div class="user-info">
          <span>{{ userStore.user?.nickname || userStore.user?.username }}</span>
          <el-button type="danger" link @click="handleLogout">退出</el-button>
        </div>
      </div>
      
      <div class="feature-grid">
        <div class="feature-card glass-card" @click="goToVideos">
          <h3>视频接口</h3>
        </div>
        
        <!-- More feature cards can be added here -->
      </div>
    </main>
    
    <!-- Right - Detail/Action Panel -->
    <aside class="detail-panel">
      <router-view />
    </aside>
    
    <!-- Category Dialog -->
    <el-dialog
      v-model="categoryDialogVisible"
      :title="editingCategory ? '编辑栏目' : '新增栏目'"
      width="400px"
    >
      <el-form :model="categoryForm" label-width="80px">
        <el-form-item label="栏目名称">
          <el-input v-model="categoryForm.name" placeholder="请输入栏目名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="categoryForm.description" type="textarea" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="categoryForm.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="categoryDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveCategory">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, VideoCamera } from '@element-plus/icons-vue'
import { getCategories, createCategory, updateCategory, deleteCategory } from '../api/category'
import { useUserStore } from '../store/user'

const router = useRouter()
const userStore = useUserStore()

const categories = ref([])
const activeCategory = ref(null)
const currentCategory = ref(null)
const categoryDialogVisible = ref(false)
const editingCategory = ref(null)

const categoryForm = reactive({
  name: '',
  description: '',
  sortOrder: 0
})

onMounted(() => {
  loadCategories()
})

const loadCategories = async () => {
  try {
    const res = await getCategories()
    categories.value = res.data
  } catch (error) {
    console.error(error)
  }
}

const selectCategory = (category) => {
  activeCategory.value = category.id
  currentCategory.value = category
}

const showCategoryDialog = (category = null) => {
  editingCategory.value = category
  if (category) {
    categoryForm.name = category.name
    categoryForm.description = category.description || ''
    categoryForm.sortOrder = category.sortOrder || 0
  } else {
    categoryForm.name = ''
    categoryForm.description = ''
    categoryForm.sortOrder = 0
  }
  categoryDialogVisible.value = true
}

const handleSaveCategory = async () => {
  try {
    if (editingCategory.value) {
      await updateCategory(editingCategory.value.id, categoryForm)
      ElMessage.success('更新成功')
    } else {
      await createCategory(categoryForm)
      ElMessage.success('创建成功')
    }
    categoryDialogVisible.value = false
    loadCategories()
  } catch (error) {
    console.error(error)
  }
}

const handleDeleteCategory = async (category) => {
  try {
    await ElMessageBox.confirm(`确定删除栏目 "${category.name}" 吗？`, '提示', {
      type: 'warning'
    })
    await deleteCategory(category.id)
    ElMessage.success('删除成功')
    loadCategories()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const goToVideos = () => {
  router.push('/videos')
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout-container {
  display: grid;
  grid-template-columns: 250px auto 1fr;
  min-height: 100vh;
  gap: 20px;
  padding: 20px;
}

/* Sidebar */
.sidebar {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 20px;
  height: fit-content;
  max-height: calc(100vh - 40px);
  overflow-y: auto;
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.sidebar-header h2 {
  font-size: 1.2rem;
  margin: 0;
}

.category-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.category-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  background: #f5f5f5;
}

.category-item:hover {
  background: #e8e8e8;
}

.category-item.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.category-item.active .el-button {
  color: white;
}

.category-actions {
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.3s;
}

.category-item:hover .category-actions {
  opacity: 1;
}

/* Main Content */
.main-content {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 20px;
  height: fit-content;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.content-header h2 {
  margin: 0;
  color: #333;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

.feature-card {
  padding: 24px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.feature-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  color: white;
}

.feature-card h3 {
  margin: 0 0 8px;
  color: #333;
}

.feature-card p {
  margin: 0;
  color: #666;
  font-size: 0.9rem;
}

/* Detail Panel */
.detail-panel {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 20px;
  height: fit-content;
  max-height: calc(100vh - 40px);
  overflow-y: auto;
}

@media (max-width: 1200px) {
  .layout-container {
    grid-template-columns: 200px 1fr;
  }
  
  .detail-panel {
    display: none;
  }
}

@media (max-width: 768px) {
  .layout-container {
    grid-template-columns: 1fr;
  }
  
  .sidebar {
    display: none;
  }
}
</style>

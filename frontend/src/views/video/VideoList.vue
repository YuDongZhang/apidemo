<template>
  <div class="video-list-container">
    <div class="video-header">
      <h2>视频列表</h2>
      <el-button type="primary" @click="showVideoDialog()">
        <el-icon><Plus /></el-icon> 新增视频
      </el-button>
    </div>
    
    <!-- Video Table -->
    <el-table :data="videos" stripe class="video-table">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="author" label="作者" width="120" />
      <el-table-column prop="publishTime" label="发布时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.publishTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="description" label="视频描述" show-overflow-tooltip />
      <el-table-column label="视频" width="100">
        <template #default="{ row }">
          <el-button link type="primary" @click="previewVideo(row)">
            <el-icon><VideoPlay /></el-icon> 预览
          </el-button>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="showVideoDialog(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- Video Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingVideo ? '编辑视频' : '新增视频'"
      width="600px"
    >
      <el-form :model="videoForm" label-width="100px">
        <el-form-item label="作者" required>
          <el-input v-model="videoForm.author" placeholder="请输入作者名称" />
        </el-form-item>
        <el-form-item label="发布时间">
          <el-date-picker
            v-model="videoForm.publishTime"
            type="datetime"
            placeholder="选择发布时间"
          />
        </el-form-item>
        <el-form-item label="视频描述">
          <el-input
            v-model="videoForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入视频描述"
          />
        </el-form-item>
        <el-form-item label="视频文件">
          <el-upload
            class="video-uploader"
            :show-file-list="false"
            :http-request="handleUpload"
            accept="video/*"
          >
            <el-button type="primary">
              <el-icon><Upload /></el-icon> 上传视频
            </el-button>
          </el-upload>
          <span v-if="videoForm.videoUrl" class="upload-tip">
            已上传: {{ videoForm.videoUrl }}
          </span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
    
    <!-- Video Preview Dialog -->
    <el-dialog v-model="previewVisible" title="视频预览" width="800px">
      <video
        v-if="previewUrl"
        :src="previewUrl"
        controls
        style="width: 100%; max-height: 500px;"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, VideoPlay, Upload } from '@element-plus/icons-vue'
import { getVideos, createVideo, updateVideo, deleteVideo, uploadFile } from '../../api/video'

const videos = ref([])
const dialogVisible = ref(false)
const editingVideo = ref(null)
const saving = ref(false)
const previewVisible = ref(false)
const previewUrl = ref('')

const videoForm = reactive({
  author: '',
  publishTime: null,
  description: '',
  videoUrl: '',
  categoryId: null
})

onMounted(() => {
  loadVideos()
})

const loadVideos = async () => {
  try {
    const res = await getVideos()
    videos.value = res.data
  } catch (error) {
    console.error(error)
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

const showVideoDialog = (video = null) => {
  editingVideo.value = video
  if (video) {
    videoForm.author = video.author
    videoForm.publishTime = video.publishTime
    videoForm.description = video.description
    videoForm.videoUrl = video.videoUrl
    videoForm.categoryId = video.categoryId
  } else {
    videoForm.author = ''
    videoForm.publishTime = null
    videoForm.description = ''
    videoForm.videoUrl = ''
    videoForm.categoryId = null
  }
  dialogVisible.value = true
}

const handleUpload = async (options) => {
  try {
    const res = await uploadFile(options.file)
    videoForm.videoUrl = res.data.url
    ElMessage.success('上传成功')
  } catch (error) {
    console.error(error)
  }
}

const handleSave = async () => {
  if (!videoForm.author) {
    ElMessage.warning('请输入作者名称')
    return
  }
  
  saving.value = true
  try {
    const data = {
      author: videoForm.author,
      publishTime: videoForm.publishTime,
      description: videoForm.description,
      videoUrl: videoForm.videoUrl,
      categoryId: videoForm.categoryId
    }
    
    if (editingVideo.value) {
      await updateVideo(editingVideo.value.id, data)
      ElMessage.success('更新成功')
    } else {
      await createVideo(data)
      ElMessage.success('创建成功')
    }
    
    dialogVisible.value = false
    loadVideos()
  } catch (error) {
    console.error(error)
  } finally {
    saving.value = false
  }
}

const handleDelete = async (video) => {
  try {
    await ElMessageBox.confirm(`确定删除视频 "${video.author}" 吗？`, '提示', {
      type: 'warning'
    })
    await deleteVideo(video.id)
    ElMessage.success('删除成功')
    loadVideos()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const previewVideo = (video) => {
  if (video.videoUrl) {
    previewUrl.value = video.videoUrl
    previewVisible.value = true
  } else {
    ElMessage.warning('该视频没有上传视频文件')
  }
}
</script>

<style scoped>
.video-list-container {
  padding: 20px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  min-height: calc(100vh - 40px);
}

.video-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.video-header h2 {
  margin: 0;
  color: #333;
}

.video-table {
  border-radius: 8px;
  overflow: hidden;
}

.video-uploader {
  display: inline-block;
}

.upload-tip {
  margin-left: 12px;
  color: #67c23a;
  font-size: 0.9rem;
}
</style>

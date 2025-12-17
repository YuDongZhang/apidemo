<template>
  <div class="interface-form-container">
    <!-- Header -->
    <div class="form-header">
      <el-button link @click="goBack">
        <el-icon><ArrowLeft /></el-icon> 返回列表
      </el-button>
      <h2>{{ interfaceData?.name || '接口详情' }}</h2>
      <div class="header-actions">
        <el-button type="primary" @click="saveAll" :loading="saving">保存</el-button>
      </div>
    </div>

    <!-- Main Content -->
    <div class="form-content">
      <!-- Left: Parameter Tree Editor -->
      <div class="parameter-section">
        <div class="section-header">
          <h3>参数定义</h3>
          <el-button type="primary" size="small" @click="addRootParameter">
            <el-icon><Plus /></el-icon> 添加参数
          </el-button>
        </div>

        <div class="parameter-tree">
          <div v-if="parameters.length === 0" class="empty-params">
            <el-empty description="暂无参数，点击上方按钮添加" :image-size="80" />
          </div>
          <ParameterNode
            v-for="(param, index) in parameters"
            :key="index"
            :param="param"
            :depth="0"
            @update="updateParameter(index, $event)"
            @delete="deleteParameter(index)"
            @add-child="addChildParameter(param)"
          />
        </div>
      </div>

      <!-- Right: JSON Preview -->
      <div class="preview-section">
        <div class="section-header">
          <h3>JSON 示例</h3>
          <el-button size="small" @click="refreshPreview">
            <el-icon><Refresh /></el-icon> 刷新
          </el-button>
        </div>
        <div class="json-preview">
          <pre>{{ jsonPreview }}</pre>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, defineComponent, h } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElInput, ElSelect, ElOption, ElCheckbox, ElButton, ElIcon } from 'element-plus'
import { ArrowLeft, Plus, Refresh, Delete, ArrowDown } from '@element-plus/icons-vue'
import { getInterface, getParameters, saveParameters, getJsonExample } from '../../api/api-interface'

const router = useRouter()
const route = useRoute()

const interfaceId = computed(() => route.params.id)
const interfaceData = ref(null)
const parameters = ref([])
const jsonPreview = ref('{}')
const saving = ref(false)

onMounted(() => {
  loadInterface()
  loadParameters()
})

const loadInterface = async () => {
  try {
    const res = await getInterface(interfaceId.value)
    interfaceData.value = res.data
  } catch (error) {
    console.error(error)
  }
}

const loadParameters = async () => {
  try {
    const res = await getParameters(interfaceId.value)
    parameters.value = convertToTreeData(res.data)
    refreshPreview()
  } catch (error) {
    console.error(error)
  }
}

const convertToTreeData = (params) => {
  return params.map(p => ({
    name: p.name,
    type: p.type,
    required: p.required,
    description: p.description,
    exampleValue: p.exampleValue,
    children: p.children ? convertToTreeData(p.children) : []
  }))
}

const addRootParameter = () => {
  parameters.value.push({
    name: '',
    type: 'string',
    required: false,
    description: '',
    exampleValue: '',
    children: []
  })
}

const updateParameter = (index, newParam) => {
  parameters.value[index] = newParam
}

const deleteParameter = (index) => {
  parameters.value.splice(index, 1)
}

const addChildParameter = (parentParam) => {
  if (!parentParam.children) {
    parentParam.children = []
  }
  parentParam.children.push({
    name: '',
    type: 'string',
    required: false,
    description: '',
    exampleValue: '',
    children: []
  })
}

const refreshPreview = async () => {
  // Generate local preview based on current parameters
  const json = generateLocalPreview(parameters.value)
  jsonPreview.value = JSON.stringify(json, null, 2)
}

const generateLocalPreview = (params) => {
  const result = {}
  for (const param of params) {
    if (!param.name) continue
    result[param.name] = generateParamValue(param)
  }
  return result
}

const generateParamValue = (param) => {
  const type = param.type || 'string'
  const example = param.exampleValue

  switch (type.toLowerCase()) {
    case 'object':
      if (param.children && param.children.length > 0) {
        return generateLocalPreview(param.children)
      }
      return {}
    case 'array':
      if (param.children && param.children.length > 0) {
        const firstChild = param.children[0]
        const childValue = firstChild.type === 'object'
          ? generateLocalPreview(firstChild.children || [])
          : generatePrimitiveValue(firstChild)
        return [childValue, childValue]
      }
      return []
    case 'number':
      return example ? Number(example) : 12345
    case 'boolean':
      return example === 'false' ? false : true
    default:
      return example || `示例${param.name}`
  }
}

const generatePrimitiveValue = (param) => {
  const type = param.type || 'string'
  const example = param.exampleValue
  switch (type) {
    case 'number': return example ? Number(example) : 123
    case 'boolean': return example === 'false' ? false : true
    default: return example || '示例值'
  }
}

const saveAll = async () => {
  saving.value = true
  try {
    await saveParameters(interfaceId.value, parameters.value)
    ElMessage.success('保存成功')
    refreshPreview()
  } catch (error) {
    console.error(error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const goBack = () => {
  router.push('/interfaces')
}

// ParameterNode Component (inline)
const ParameterNode = defineComponent({
  name: 'ParameterNode',
  props: {
    param: { type: Object, required: true },
    depth: { type: Number, default: 0 }
  },
  emits: ['update', 'delete', 'add-child'],
  setup(props, { emit }) {
    const expanded = ref(true)

    const typeOptions = [
      { label: 'String', value: 'string' },
      { label: 'Number', value: 'number' },
      { label: 'Boolean', value: 'boolean' },
      { label: 'Object', value: 'object' },
      { label: 'Array', value: 'array' }
    ]

    const updateField = (field, value) => {
      emit('update', { ...props.param, [field]: value })
    }

    const hasChildren = computed(() => 
      props.param.type === 'object' || props.param.type === 'array'
    )

    return () => h('div', { class: 'param-node', style: { marginLeft: `${props.depth * 20}px` } }, [
      h('div', { class: 'param-row' }, [
        // Expand/Collapse button
        hasChildren.value && props.param.children?.length > 0
          ? h(ElButton, {
              link: true,
              size: 'small',
              onClick: () => { expanded.value = !expanded.value }
            }, () => h(ElIcon, null, () => h(ArrowDown, { 
              style: { transform: expanded.value ? 'rotate(0deg)' : 'rotate(-90deg)', transition: 'transform 0.2s' }
            })))
          : h('div', { style: { width: '24px' } }),
        
        // Name input
        h(ElInput, {
          modelValue: props.param.name,
          placeholder: '参数名',
          size: 'small',
          style: { width: '120px' },
          'onUpdate:modelValue': (v) => updateField('name', v)
        }),

        // Type select
        h(ElSelect, {
          modelValue: props.param.type,
          size: 'small',
          style: { width: '100px' },
          'onUpdate:modelValue': (v) => updateField('type', v)
        }, () => typeOptions.map(opt => 
          h(ElOption, { key: opt.value, label: opt.label, value: opt.value })
        )),

        // Required checkbox
        h(ElCheckbox, {
          modelValue: props.param.required,
          label: '必填',
          size: 'small',
          'onUpdate:modelValue': (v) => updateField('required', v)
        }),

        // Description
        h(ElInput, {
          modelValue: props.param.description,
          placeholder: '描述',
          size: 'small',
          style: { width: '150px' },
          'onUpdate:modelValue': (v) => updateField('description', v)
        }),

        // Example value
        h(ElInput, {
          modelValue: props.param.exampleValue,
          placeholder: '示例值',
          size: 'small',
          style: { width: '100px' },
          'onUpdate:modelValue': (v) => updateField('exampleValue', v)
        }),

        // Add child button (only for object/array)
        hasChildren.value
          ? h(ElButton, {
              type: 'primary',
              link: true,
              size: 'small',
              onClick: () => emit('add-child')
            }, () => h(ElIcon, null, () => h(Plus)))
          : null,

        // Delete button
        h(ElButton, {
          type: 'danger',
          link: true,
          size: 'small',
          onClick: () => emit('delete')
        }, () => h(ElIcon, null, () => h(Delete)))
      ]),

      // Children
      expanded.value && props.param.children?.length > 0
        ? h('div', { class: 'param-children' },
            props.param.children.map((child, idx) =>
              h(ParameterNode, {
                key: idx,
                param: child,
                depth: props.depth + 1,
                onUpdate: (newChild) => {
                  const newChildren = [...props.param.children]
                  newChildren[idx] = newChild
                  updateField('children', newChildren)
                },
                onDelete: () => {
                  const newChildren = props.param.children.filter((_, i) => i !== idx)
                  updateField('children', newChildren)
                },
                onAddChild: () => {
                  const newChildren = [...(child.children || [])]
                  newChildren.push({
                    name: '',
                    type: 'string',
                    required: false,
                    description: '',
                    exampleValue: '',
                    children: []
                  })
                  const updatedChild = { ...child, children: newChildren }
                  const allChildren = [...props.param.children]
                  allChildren[idx] = updatedChild
                  updateField('children', allChildren)
                }
              })
            )
          )
        : null
    ])
  }
})
</script>

<style scoped>
.interface-form-container {
  min-height: 100%;
  display: flex;
  flex-direction: column;
}

.form-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.form-header h2 {
  flex: 1;
  margin: 0;
  color: #303133;
}

.form-content {
  display: grid;
  grid-template-columns: 1fr 400px;
  gap: 20px;
  flex: 1;
}

.parameter-section,
.preview-section {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  padding: 16px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.section-header h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.parameter-tree {
  max-height: calc(100vh - 300px);
  overflow-y: auto;
}

.empty-params {
  padding: 40px;
  text-align: center;
}

.param-node {
  margin-bottom: 8px;
}

.param-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  background: #f9fafb;
  border-radius: 8px;
}

.param-row:hover {
  background: #f0f2f5;
}

.param-children {
  border-left: 2px solid #e4e7ed;
  margin-left: 12px;
  padding-left: 8px;
  margin-top: 8px;
}

.json-preview {
  background: #1e1e1e;
  border-radius: 8px;
  padding: 16px;
  max-height: calc(100vh - 300px);
  overflow: auto;
}

.json-preview pre {
  margin: 0;
  font-family: 'Fira Code', 'Consolas', monospace;
  font-size: 13px;
  color: #9cdcfe;
  white-space: pre-wrap;
  word-break: break-all;
}

@media (max-width: 1200px) {
  .form-content {
    grid-template-columns: 1fr;
  }
}
</style>

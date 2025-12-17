import request from './request'

/**
 * 获取接口列表
 */
export function getInterfaces(categoryId) {
    const params = categoryId ? { categoryId } : {}
    return request.get('/interfaces', { params })
}

/**
 * 获取单个接口详情
 */
export function getInterface(id) {
    return request.get(`/interfaces/${id}`)
}

/**
 * 创建接口
 */
export function createInterface(data) {
    return request.post('/interfaces', data)
}

/**
 * 更新接口
 */
export function updateInterface(id, data) {
    return request.put(`/interfaces/${id}`, data)
}

/**
 * 删除接口
 */
export function deleteInterface(id) {
    return request.delete(`/interfaces/${id}`)
}

/**
 * 获取接口参数
 */
export function getParameters(interfaceId) {
    return request.get(`/interfaces/${interfaceId}/parameters`)
}

/**
 * 保存接口参数
 */
export function saveParameters(interfaceId, parameters) {
    return request.post(`/interfaces/${interfaceId}/parameters`, parameters)
}

/**
 * 生成JSON示例
 */
export function getJsonExample(interfaceId) {
    return request.get(`/interfaces/${interfaceId}/json-example`)
}

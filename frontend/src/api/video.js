import request from './request'

export function getVideos(categoryId) {
    const params = categoryId ? { categoryId } : {}
    return request.get('/videos', { params })
}

export function getVideo(id) {
    return request.get(`/videos/${id}`)
}

export function createVideo(data) {
    return request.post('/videos', data)
}

export function updateVideo(id, data) {
    return request.put(`/videos/${id}`, data)
}

export function deleteVideo(id) {
    return request.delete(`/videos/${id}`)
}

export function uploadFile(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/files/upload', formData, {
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

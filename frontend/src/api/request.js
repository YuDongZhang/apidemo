import axios from 'axios'
import { useUserStore } from '../store/user'
import router from '../router'
import { ElMessage } from 'element-plus'

const request = axios.create({
    baseURL: '/api',
    timeout: 30000
})

// Request interceptor
request.interceptors.request.use(
    config => {
        const userStore = useUserStore()
        if (userStore.token) {
            config.headers.Authorization = `Bearer ${userStore.token}`
        }
        return config
    },
    error => {
        return Promise.reject(error)
    }
)

// Response interceptor
request.interceptors.response.use(
    response => {
        const res = response.data
        if (res.code !== 0) {
            ElMessage.error(res.msg || '请求失败')
            return Promise.reject(new Error(res.msg || '请求失败'))
        }
        return res
    },
    error => {
        if (error.response?.status === 401) {
            const userStore = useUserStore()
            userStore.logout()
            router.push('/login')
            ElMessage.error('登录已过期，请重新登录')
        } else {
            ElMessage.error(error.message || '网络错误')
        }
        return Promise.reject(error)
    }
)

export default request

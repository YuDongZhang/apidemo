import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../store/user'

const routes = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('../views/Login.vue')
    },
    {
        path: '/',
        name: 'Layout',
        component: () => import('../views/Layout.vue'),
        meta: { requiresAuth: true },
        children: [
            {
                path: '',
                name: 'Home',
                component: () => import('../views/Home.vue')
            },
            {
                path: 'videos',
                name: 'VideoList',
                component: () => import('../views/video/VideoList.vue')
            },
            {
                path: 'interfaces',
                name: 'InterfaceList',
                component: () => import('../views/interface/InterfaceList.vue')
            },
            {
                path: 'interfaces/:id',
                name: 'InterfaceForm',
                component: () => import('../views/interface/InterfaceForm.vue')
            }
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// Navigation guard
router.beforeEach((to, from, next) => {
    const userStore = useUserStore()

    if (to.meta.requiresAuth && !userStore.isLoggedIn) {
        next('/login')
    } else if (to.path === '/login' && userStore.isLoggedIn) {
        next('/')
    } else {
        next()
    }
})

export default router

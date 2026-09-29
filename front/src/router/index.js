import { createRouter, createWebHistory } from 'vue-router'

import LoginView from '@/views/LoginView.vue'
import RegisterView from '@/views/RegisterView.vue'

import UserLayout from '@/components/UserLayout.vue'
import OrgaLayout from '@/components/orga/OrgaLayout.vue'
import UserDashboard from '@/views/user-view/UserDashboard.vue'
import EventDetailView from '@/views/user-view/EventDetailView.vue'

import OrgaDashboard from "@/views/orga-view/OrgaDashboard.vue"
import CreateEventView from '@/views/orga-view/CreateEventView.vue'

import { useUser } from '@/composables/useUser'
import HomeView from '@/views/HomeView.vue'
import AdminPolesView from '@/views/admin-view/AdminPolesView.vue'
import AdminOrganizersView from '@/views/admin-view/AdminOrganizersView.vue'
import AdminUsersView from '@/views/admin-view/AdminUsersView.vue'
import FavorisView from '@/views/user-view/FavorisView.vue'
import InscriptionsView from '@/views/user-view/InscriptionsView.vue'
import AdminEventsView from "@/views/admin-view/AdminEventsView.vue";
import { validateSession } from '@/services/api.js'

const router = createRouter({
    history: createWebHistory(),
    routes: [
        // PUBLIC
        {
            path: '/',
            component: HomeView,
            meta: { public: true }
        },
        {
            path: '/home',
            name: 'home',
            component: HomeView,
            meta: { public: true }
        },
        {
            path: '/login',
            name: 'login',
            component: LoginView,
            meta: { public: true }
        },
        {
            path: '/register',
            name: 'register',
            component: RegisterView,
            meta: { public: true }
        },

        // USER AREA (with layout)
        {
            path: '/app',
            component: UserLayout,
            children: [
                {
                    path: '',
                    name: 'user-dashboard',
                    component: UserDashboard
                },
                {
                    path: 'events/:id',
                    name: 'event-detail',
                    component: EventDetailView
                },
                {
                    path: 'favoris',
                    name: 'user-favoris',
                    component: FavorisView
                },
                {
                    path: 'inscriptions',
                    name: 'user-inscriptions',
                    component: InscriptionsView },

            ]
        },
        // ORGA AREA (with layout)
        {
            path: '/orga',
            component: OrgaLayout,
            children: [
                {
                    path: '',
                    name: 'orga-dashboard',
                    component: OrgaDashboard
                },
                {
                    path: 'create-event',
                    name: 'orga-event',
                    component: CreateEventView
                },
                {
                    path: 'events',
                    name: 'orga-events',
                    component: () => import('@/views/orga-view/MesEvenementsView.vue')
                },
                {
                    path: 'participants',
                    name: 'orga-participants',
                    component: () => import('@/views/orga-view/ParticipantsView.vue')
                },

            ]
        },

        // OTHER DASHBOARDS (can later have their own layouts)
        {
            path: '/admin',
            name: 'admin-home',
            component: AdminPolesView
        },
        {
            path: '/admin/poles',
            name: 'admin-poles',
            component: AdminPolesView
        },
        {
            path: '/admin/organizers',
            name: 'admin-organizers',
            component: AdminOrganizersView
        },
        {
            path: '/admin/users',
            name: 'admin-users',
            component: AdminUsersView
        },
        {
            path: '/admin/events',
            name: 'admin-events',
            component: AdminEventsView
        }
    ]
})


router.beforeEach(async (to, from, next) => {
    const { isAdmin, isOrga, mode, refreshSession } = useUser()

    // allow public pages
    if (to.meta.public) return next()

    // block if not logged
    if (!refreshSession()) {
        return next({ name: 'login' })
    }

    try {
        await validateSession()
    } catch (error) {
        return next({ name: 'login' })
    }

    // --- ADMIN ---
    if (to.path.startsWith('/admin')) {
        if (!isAdmin.value) return next('/app')
        return next()
    }

    // --- ORGA ---
    if (to.path.startsWith('/orga')) {
        if (!isOrga.value) return next('/app')

        // force mode orga
        if (mode.value !== 'orga') {
            return next('/app')
        }

        return next()
    }

    next()
})

export default router

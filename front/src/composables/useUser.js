import { ref, computed } from 'vue'
import router from "@/router/index.js";

const user = ref(null)
const activeOrg = ref(null)
const mode = ref('user') // 'user' | 'orga' | 'admin'

function isValidUserSession(data) {
    return Boolean(
        data
        && data.token
        && data.email
        && Array.isArray(data.roles)
        && !isTokenExpired(data.token)
    )
}

function isTokenExpired(token) {
    const payload = decodeJwtPayload(token)

    if (!payload?.exp) {
        return true
    }

    return payload.exp * 1000 <= Date.now()
}

function decodeJwtPayload(token) {
    try {
        const [, payload] = token.split('.')
        if (!payload) return null

        const normalizedPayload = payload.replace(/-/g, '+').replace(/_/g, '/')
        const paddedPayload = normalizedPayload.padEnd(
            normalizedPayload.length + (4 - normalizedPayload.length % 4) % 4,
            '='
        )

        return JSON.parse(atob(paddedPayload))
    } catch (error) {
        return null
    }
}

function clearSession() {
    user.value = null
    activeOrg.value = null
    mode.value = 'user'
    localStorage.removeItem('user')
    localStorage.removeItem('mode')
    localStorage.removeItem('activeOrg')
}

// --- LOAD STORAGE ---
const savedUser = localStorage.getItem('user')
const savedMode = localStorage.getItem('mode')
const savedOrg = localStorage.getItem('activeOrg')

try {
    const parsedUser = savedUser ? JSON.parse(savedUser) : null

    if (isValidUserSession(parsedUser)) {
        user.value = parsedUser
        if (savedMode) mode.value = savedMode
        if (savedOrg) activeOrg.value = JSON.parse(savedOrg)
    } else if (savedUser || savedMode || savedOrg) {
        clearSession()
    }
} catch (error) {
    clearSession()
}

export function useUser() {

    const setUser = (data) => {
        if (!isValidUserSession(data)) {
            clearSession()
            return
        }

        user.value = data
        localStorage.setItem('user', JSON.stringify(data))

        // mode par défaut
        mode.value = 'user'
        localStorage.setItem('mode', 'user')

        activeOrg.value = null
        localStorage.removeItem('activeOrg')
    }

    const switchToOrg = (org) => {
        if (!user.value?.roles?.includes('ORGANISATEUR')) return

        activeOrg.value = org
        mode.value = 'orga'

        localStorage.setItem('mode', 'orga')
        localStorage.setItem('activeOrg', JSON.stringify(org))
    }

    const switchBackToUser = () => {
        mode.value = 'user'
        activeOrg.value = null
        localStorage.setItem('mode', 'user')
        localStorage.removeItem('activeOrg')
        router.push({ name: 'user-dashboard' })
    }

    const logout = () => {
        clearSession()
    }

    const refreshSession = () => {
        if (!isValidUserSession(user.value)) {
            clearSession()
            return false
        }

        return true
    }

    // --- COMPUTED ---

    const isLogged = computed(() => isValidUserSession(user.value))

    const isOrga = computed(() =>
        isLogged.value && user.value.roles.includes('ORGANISATEUR')
    )

    const isAdmin = computed(() =>
        isLogged.value && user.value.roles.includes('ADMIN')
    )

    const isOrgaMode = computed(() => mode.value === 'orga')
    const isAdminMode = computed(() => mode.value === 'admin')

    return {
        user,
        activeOrg,
        mode,

        isLogged,
        isOrga,
        isAdmin,
        isOrgaMode,
        isAdminMode,

        setUser,
        switchToOrg,
        switchBackToUser,
        logout,
        refreshSession
    }
}

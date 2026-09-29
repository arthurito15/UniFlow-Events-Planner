import { ref } from 'vue'
import { login, register } from '@/services/api.js'
import { useRouter } from 'vue-router'
import { useUser } from '@/composables/useUser'

export function useAuth() {
    const router = useRouter()

    const loading = ref(false)
    const errorMessage = ref('')
    const successMessage = ref('')

    // ===== LOGIN =====
    const handleLogin = async (email, password) => {
        loading.value = true
        errorMessage.value = ''

        try {
            const { setUser } = useUser()
            const result = await login(email, password)

            setUser({
                id: result.id,
                name: result.displayName,
                email: result.email,
                dateInscription: result.dateInscription,
                roles: result.roles,
                token: result.token,
                organisations: result.organisations ?? []
            })

            if (result.roles?.includes('ADMIN')) {
                await router.push({ name: 'admin-poles' })
            } else {
                await router.push({ name: 'user-dashboard' })
            }

        } catch (error) {
            errorMessage.value = "Identifiants incorrects"
        } finally {
            loading.value = false
        }
    }

    // ===== REGISTER =====
    const handleRegister = async (form) => {
        errorMessage.value = ''
        successMessage.value = ''

        if (form.password !== form.confirmPassword) {
            errorMessage.value = "Les mots de passe ne correspondent pas"
            return
        }

        loading.value = true

        try {
            await register(form)

            successMessage.value = "Compte créé !"
            await router.push('/login')

        } catch (error) {
            errorMessage.value = error.message
        } finally {
            loading.value = false
        }
    }

    return {
        loading,
        errorMessage,
        successMessage,
        handleLogin,
        handleRegister
    }
}

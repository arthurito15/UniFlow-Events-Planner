<template>
  <div class="auth-page">
    <AuthHeader />

    <main class="container d-flex justify-content-center align-items-center flex-grow-1">
      <div class="card p-4 shadow" style="width: 100%; max-width: 400px;">
        <h3 class="text-center mb-4">Connexion</h3>

        <form @submit.prevent="handleLogin(email, password)">
          <div class="mb-3">
            <label class="form-label">Email</label>
            <input
                type="email"
                class="form-control"
                v-model="email"
                placeholder="ex: email@mail.com"
                required
            >
          </div>

          <div class="mb-3">
            <label class="form-label">Mot de passe</label>
            <input
                type="password"
                class="form-control"
                v-model="password"

            >
          </div>

          <!-- Message d'erreur -->
          <div v-if="errorMessage" class="alert alert-danger alert-dismissible fade show" role="alert">
            {{ errorMessage }}
            <button type="button" class="btn-close" @click="errorMessage = ''"></button>
          </div>

          <button
              type="submit"
              class="btn btn-primary w-100"
              :disabled="loading"
          >
            <span v-if="loading">Connexion en cours...</span>
            <span v-else>Se connecter</span>
          </button>
        </form>

        <div class="text-center mt-3">
          <small>
            Pas de compte ?
            <router-link to="/register">Inscription</router-link>
          </small>
        </div>
      </div>
    </main>

    <AuthFooter />
  </div>
</template>

<script>
import { ref } from 'vue'
import { useAuth } from '@/composables/useAuth'
import AuthHeader from '@/components/AuthHeader.vue'
import AuthFooter from '@/components/AuthFooter.vue'

export default {
  components: {
    AuthHeader,
    AuthFooter
  },
  setup() {
    const {
      loading,
      errorMessage,
      handleLogin
    } = useAuth()

    const email = ref('')
    const password = ref('')

    return {
      email,
      password,
      loading,
      errorMessage,
      handleLogin
    }
  }
}
</script>
<template>
  <div class="auth-page">
    <AuthHeader />

    <main class="container d-flex justify-content-center align-items-center flex-grow-1">
      <div class="card p-4 shadow" style="width: 100%; max-width: 400px;">
        <h3 class="text-center mb-4">Inscription</h3>

        <form @submit.prevent="handleRegister(form)">
          <!-- Nom -->
          <div class="mb-3">
            <label class="form-label">Nom</label>
            <input
                type="text"
                class="form-control"
                v-model="form.nom"
                required
            >
          </div>

          <!-- Prénom -->
          <div class="mb-3">
            <label class="form-label">Prénom</label>
            <input
                type="text"
                class="form-control"
                v-model="form.prenom"
                required
            >
          </div>

          <!-- Email -->
          <div class="mb-3">
            <label class="form-label">Email</label>
            <input
                type="email"
                class="form-control"
                v-model="form.email"
                placeholder="ex: email@mail.com"
                required
            >
          </div>

          <!-- Mot de passe -->
          <div class="mb-3">
            <label class="form-label">Mot de passe</label>
            <input
                type="password"
                class="form-control"
                v-model="form.password"
                required
            >
          </div>

          <!-- Confirmation mot de passe -->
          <div class="mb-3">
            <label class="form-label">Confirmer le mot de passe</label>
            <input
                type="password"
                class="form-control"
                v-model="form.confirmPassword"
                required
            >
          </div>

          <!-- Message d'erreur -->
          <div v-if="errorMessage" class="alert alert-danger alert-dismissible fade show" role="alert">
            {{ errorMessage }}
            <button type="button" class="btn-close" @click="errorMessage = ''"></button>
          </div>

          <!-- Message de succès -->
          <div v-if="successMessage" class="alert alert-success alert-dismissible fade show" role="alert">
            {{ successMessage }}
            <button type="button" class="btn-close" @click="successMessage = ''"></button>
          </div>

          <button
              type="submit"
              class="btn btn-primary w-100"
              :disabled="loading"
          >
            <span v-if="loading">Inscription en cours...</span>
            <span v-else>S'inscrire</span>
          </button>
        </form>

        <div class="text-center mt-3">
          <small>
            Déjà un compte ?
            <router-link to="/login">Se connecter</router-link>
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
      successMessage,
      handleRegister
    } = useAuth()

    const form = ref({
      nom: '',
      prenom: '',
      email: '',
      password: '',
      confirmPassword: ''
    })

    return {
      form,
      loading,
      errorMessage,
      successMessage,
      handleRegister
    }
  }
}
</script>
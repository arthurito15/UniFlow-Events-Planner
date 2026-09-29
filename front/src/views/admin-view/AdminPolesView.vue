<template>
  <div class="dashboard-shell">
    <AdminSidebar :items="sidebarItems" active="Pôles" />

    <div class="dashboard-content">
      <AdminHeader
          v-model="search"
          title="Gestion des pôles"
          :user="user"
      />

      <main class="dashboard-main">
        <section class="section-block">
          <div class="section-head">
            <div>
              <p class="eyebrow">Administration</p>
              <h2>Pôles</h2>
              <p class="section-text">
                Gère les pôles universitaires avec une interface simple.
              </p>
            </div>

            <button class="primary-btn" @click="handleCreatePole">
              + Créer un pôle
            </button>
          </div>

          <p v-if="error" class="error-message">{{ error }}</p>

          <div v-if="showCreateForm" class="inline-form-card">
            <div class="inline-form-head">
              <div>
                <h3>Nouveau pôle</h3>
                <p>Ajoute le nom du pôle.</p>
              </div>

              <button type="button" class="secondary-btn" @click="closeCreateForm">
                Annuler
              </button>
            </div>

            <form class="inline-form-grid" @submit.prevent="submitCreatePole">
              <div class="form-group full-width">
                <label>Nom du pôle</label>
                <input v-model="form.name" type="text" required />
              </div>

              <div class="full-width form-actions">
                <button type="button" class="secondary-btn" @click="closeCreateForm">
                  Annuler
                </button>
                <button type="submit" class="primary-btn">
                  Créer le pôle
                </button>
              </div>
            </form>
          </div>

          <div v-if="editingPole" class="inline-form-card">
            <div class="inline-form-head">
              <div>
                <h3>Modifier le pôle</h3>
                <p>Modifier  le nom du pôle.</p>
              </div>

              <button type="button" class="secondary-btn" @click="cancelEdit">
                Annuler
              </button>
            </div>

            <form class="inline-form-grid" @submit.prevent="submitEditPole">
              <div class="form-group full-width">
                <label>Nouveau nom du pôle</label>
                <input v-model="editForm.name" type="text" required />
              </div>

              <div class="full-width form-actions">
                <button type="button" class="secondary-btn" @click="cancelEdit">
                  Annuler
                </button>
                <button type="submit" class="primary-btn">
                  Enregistrer
                </button>
              </div>
            </form>
          </div>

          <p v-if="loading" class="loading-message">Chargement des pôles...</p>

          <div v-else class="poles-grid">
            <PoleCard
                v-for="pole in displayedPoles"
                :key="pole.id"
                :pole="pole"
                @edit="handleEditPole"
                @delete="handleDeletePole"
            />
          </div>
        </section>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, reactive, onMounted } from 'vue'
import AdminHeader from '../../components/admin/AdminHeader.vue'
import AdminSidebar from '../../components/admin/AdminSidebar.vue'
import PoleCard from '../../components/admin/PoleCard.vue'
import { useUser } from '../../composables/useUser.js'
import {
  getPoles,
  createPole,
  updatePole,
  deletePole as deletePoleApi
} from '@/services/api.js'

const { user } = useUser()

const search = ref('')
const poles = ref([])
const showCreateForm = ref(false)
const loading = ref(false)
const error = ref('')

const editingPole = ref(null)

const form = reactive({
  name: ''
})

const editForm = reactive({
  name: ''
})

const sidebarItems = [
  { label: 'Pôles', icon: '◫', route: '/admin/poles' },
  { label: 'Organisations', icon: '◉', route: '/admin/organizers' },
  { label: 'Événements', icon: '◌', route: '/admin/events' },
  { label: 'Utilisateurs', icon: '◍', route: '/admin/users' }
]

const themes = [
  'theme-purple',
  'theme-blue',
  'theme-green',
  'theme-orange'
]

function getRandomTheme() {
  return themes[Math.floor(Math.random() * themes.length)]
}

const filteredPoles = computed(() => {
  const value = search.value.toLowerCase().trim()

  if (!value) return poles.value

  return poles.value.filter((pole) =>
      pole.name?.toLowerCase().includes(value)
  )
})

const displayedPoles = computed(() => {
  return filteredPoles.value.map((pole, index) => ({
    ...pole,
    theme: pole.theme || themes[index % themes.length]
  }))
})

async function loadPoles() {
  try {
    loading.value = true
    error.value = ''
    poles.value = await getPoles()
  } catch (e) {
    console.error(e)
    error.value = 'Impossible de charger les pôles.'
  } finally {
    loading.value = false
  }
}

function handleCreatePole() {
  showCreateForm.value = true
  editingPole.value = null
}

function closeCreateForm() {
  showCreateForm.value = false
  resetForm()
}

function resetForm() {
  form.name = ''
}

async function submitCreatePole() {
  if (!form.name.trim()) {
    error.value = 'Le nom du pôle est obligatoire.'
    return
  }

  try {
    error.value = ''

    await createPole({
      name: form.name.trim(),
      theme: getRandomTheme()
    })

    closeCreateForm()
    await loadPoles()
  } catch (e) {
    console.error(e)
    error.value = 'Impossible de créer le pôle.'
  }
}

function handleEditPole(pole) {
  showCreateForm.value = false
  editingPole.value = pole
  editForm.name = pole.name
}

function cancelEdit() {
  editingPole.value = null
  editForm.name = ''
}

async function submitEditPole() {
  if (!editingPole.value) return

  if (!editForm.name.trim()) {
    error.value = 'Le nom du pôle est obligatoire.'
    return
  }

  try {
    error.value = ''

    await updatePole(editingPole.value.id, {
      name: editForm.name.trim(),
      theme: editingPole.value.theme || getRandomTheme()
    })

    cancelEdit()
    await loadPoles()
  } catch (e) {
    console.error(e)
    error.value = 'Impossible de modifier le pôle.'
  }
}

async function handleDeletePole(pole) {
  const confirmed = confirm(`Voulez-vous vraiment supprimer le pôle : ${pole.name} ?`)

  if (!confirmed) return

  try {
    error.value = ''

    await deletePoleApi(pole.id)

    if (editingPole.value?.id === pole.id) {
      cancelEdit()
    }

    await loadPoles()
  } catch (e) {
    console.error(e)
    error.value = 'Impossible de supprimer le pôle.'
  }
}

onMounted(() => {
  loadPoles()
})
</script>

<style scoped>
.dashboard-shell {
  min-height: 100vh;
  display: flex;
  background: #f7f5fb;
}

.dashboard-content {
  flex: 1;
  padding: 28px;
}

.dashboard-main {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.section-block {
  background: #ffffff;
  border: 1px solid #eee8f5;
  border-radius: 24px;
  padding: 24px;
  box-shadow: 0 18px 40px rgba(25, 15, 35, 0.04);
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.eyebrow {
  margin: 0 0 6px;
  color: #8b7aa3;
  font-size: 13px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.section-head h2 {
  margin: 0;
  color: #24192f;
  font-size: 24px;
}

.section-text {
  margin: 8px 0 0;
  color: #756d80;
  max-width: 700px;
  line-height: 1.5;
}

.primary-btn {
  border: none;
  background: #7c3aed;
  color: white;
  border-radius: 14px;
  padding: 12px 18px;
  font-weight: 600;
  box-shadow: 0 10px 20px rgba(124, 58, 237, 0.18);
  cursor: pointer;
}

.secondary-btn {
  border: 1px solid #ddd6e6;
  padding: 10px 16px;
  border-radius: 12px;
  background: white;
  color: #5c4b74;
  cursor: pointer;
}

.poles-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 18px;
}

.inline-form-card {
  background: #faf8fd;
  border: 1px solid #ebe5f3;
  border-radius: 20px;
  padding: 20px;
  margin-bottom: 20px;
}

.inline-form-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 18px;
  gap: 16px;
}

.inline-form-head h3 {
  margin: 0;
  color: #24192f;
}

.inline-form-head p {
  margin: 6px 0 0;
  color: #756d80;
}

.inline-form-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-group label {
  color: #4b3b5f;
  font-weight: 600;
}

.form-group input {
  border: 1px solid #ddd6e6;
  border-radius: 12px;
  padding: 12px;
  font-size: 14px;
}

.full-width {
  grid-column: 1 / -1;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.error-message {
  background: #fff1f0;
  border: 1px solid #ffd7d2;
  color: #b42318;
  border-radius: 14px;
  padding: 12px 14px;
  margin-bottom: 18px;
}

.loading-message {
  color: #756d80;
}

@media (max-width: 1100px) {
  .poles-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 700px) {
  .dashboard-content {
    padding: 18px;
  }

  .poles-grid {
    grid-template-columns: 1fr;
  }

  .inline-form-head {
    flex-direction: column;
  }

  .form-actions {
    flex-direction: column;
  }
}
</style>
<template>
  <div class="dashboard-shell">
    <AdminSidebar :items="sidebarItems" active="Organisations" />

    <div class="dashboard-content">
      <AdminHeader
          v-model="search"
          title="Gestion des organisations"
          :user="user"
      />

      <main class="dashboard-main">
        <section class="section-block">
          <div class="section-head section-head--tight">
            <div>
              <p class="eyebrow">Gestion</p>
              <h2>Organisations</h2>
              <p class="section-text">
                Gère les organisations et leur affectation aux pôles.
              </p>
            </div>

            <button class="primary-btn" @click="handleCreateOrganizer">
              + Ajouter une organisation
            </button>
          </div>

          <p v-if="error" class="error-message">{{ error }}</p>

          <div v-if="showCreateForm" class="inline-form-card">
            <div class="inline-form-head">
              <div>
                <h3>Nouvelle organisation</h3>
                <p>Ajoute une organisation.</p>
              </div>

              <button type="button" class="secondary-btn" @click="closeCreateForm">
                Annuler
              </button>
            </div>

            <form class="inline-form-grid" @submit.prevent="submitCreateOrganizer">
              <div class="form-group">
                <label>Nom</label>
                <input v-model="form.name" type="text" required />
              </div>

              <div class="form-group">
                <label>Pôle</label>
                <select v-model="form.pole" required>
                  <option value="">Choisir</option>
                  <option
                      v-for="option in poleOptions"
                      :key="option"
                      :value="option"
                  >
                    {{ option }}
                  </option>
                </select>
              </div>

              <div class="full-width form-actions">
                <button type="button" class="secondary-btn" @click="closeCreateForm">
                  Annuler
                </button>
                <button type="submit" class="primary-btn">
                  Créer l’organisation
                </button>
              </div>
            </form>
          </div>

          <div v-if="editingOrganizer" class="inline-form-card">
            <div class="inline-form-head">
              <div>
                <h3>Modifier l'organisation</h3>
                <p>Modifie son nom ou son pôle.</p>
              </div>

              <button type="button" class="secondary-btn" @click="cancelEditOrganizer">
                Annuler
              </button>
            </div>

            <form class="inline-form-grid" @submit.prevent="submitEditOrganizer">
              <div class="form-group">
                <label>Nom</label>
                <input v-model="editForm.name" type="text" required />
              </div>

              <div class="form-group">
                <label>Pôle</label>
                <select v-model="editForm.pole" required>
                  <option value="">Choisir</option>
                  <option
                      v-for="option in poleOptions"
                      :key="option"
                      :value="option"
                  >
                    {{ option }}
                  </option>
                </select>
              </div>

              <div class="full-width form-actions">
                <button type="button" class="secondary-btn" @click="cancelEditOrganizer">
                  Annuler
                </button>
                <button type="submit" class="primary-btn">
                  Enregistrer
                </button>
              </div>
            </form>
          </div>

          <div class="filter-zone">
            <div class="filter-group">
              <label>Filtrer par pôle</label>

              <select v-model="selectedPoleFilter" class="filter-select">
                <option value="Tous">
                  Tous les pôles ({{ counters.Tous ?? 0 }})
                </option>

                <option
                    v-for="pole in poleOptions"
                    :key="pole"
                    :value="pole"
                >
                  {{ pole }} ({{ counters[pole] ?? 0 }})
                </option>
              </select>
            </div>
          </div>

          <p v-if="loading" class="loading-message">Chargement des organisations...</p>

          <div v-else class="organizer-list">
            <OrganizerRow
                v-for="organizer in filteredOrganizers"
                :key="organizer.id"
                :organizer="organizer"
                :pole-options="poleOptions"
                @change-pole="updateOrganizerPole"
                @edit="handleEditOrganizer"
                @delete="deleteOrganizer"
            />

            <div v-if="filteredOrganizers.length === 0" class="empty-state">
              Aucun organisateur trouvé.
            </div>
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
import OrganizerRow from '../../components/admin/OrganizerRow.vue'
import { useUser } from '../../composables/useUser.js'
import {
  createOrganizer,
  deleteOrganizer as deleteOrganizerApi,
  getOrganizers,
  getPoles,
  updateOrganizer
} from '@/services/api.js'

const { user } = useUser()

const search = ref('')
const selectedPoleFilter = ref('Tous')
const poles = ref([])
const organizers = ref([])
const loading = ref(false)
const error = ref('')
const showCreateForm = ref(false)
const editingOrganizer = ref(null)

const form = reactive({
  name: '',
  email: '',
  pole: '',
  status: ''
})

const editForm = reactive({
  name: '',
  pole: ''
})

const sidebarItems = [
  { label: 'Pôles', icon: '◫', route: '/admin/poles' },
  { label: 'Organisations', icon: '◉', route: '/admin/organizers' },
  { label: 'Événements', icon: '◌', route: '/admin/events' },
  { label: 'Utilisateurs', icon: '◍', route: '/admin/users' }
]

const poleOptions = computed(() => {
  return poles.value.map((pole) => pole.name)
})

const filteredOrganizers = computed(() => {
  const value = search.value.toLowerCase().trim()

  return organizers.value.filter((organizer) => {
    const matchesSearch =
        !value ||
        (organizer.name ?? '').toLowerCase().includes(value) ||
        (organizer.email ?? '').toLowerCase().includes(value)

    const matchesPole =
        selectedPoleFilter.value === 'Tous' ||
        organizer.pole === selectedPoleFilter.value

    return matchesSearch && matchesPole
  })
})

const counters = computed(() => {
  const result = {
    Tous: organizers.value.length
  }

  poleOptions.value.forEach((poleName) => {
    result[poleName] = organizers.value.filter(
        (organizer) => organizer.pole === poleName
    ).length
  })

  return result
})

async function loadAdminData() {
  try {
    loading.value = true
    error.value = ''

    const [loadedPoles, loadedOrganizers] = await Promise.all([
      getPoles(),
      getOrganizers()
    ])

    poles.value = loadedPoles ?? []
    organizers.value = loadedOrganizers ?? []
  } catch (e) {
    console.error(e)
    error.value = 'Impossible de charger les organisations.'
  } finally {
    loading.value = false
  }
}

function handleCreateOrganizer() {
  openCreateForm()
}

function openCreateForm() {
  showCreateForm.value = true
  cancelEditOrganizer()
}

function closeCreateForm() {
  showCreateForm.value = false
  resetForm()
}

function resetForm() {
  form.name = ''
  form.email = ''
  form.pole = ''
  form.status = ''
}

function resetEditForm() {
  editForm.name = ''
  editForm.pole = ''
}

async function submitCreateOrganizer() {
  try {
    error.value = ''

    const createdOrganizer = await createOrganizer({
      name: form.name,
      email: form.email,
      pole: form.pole,
      status: form.status
    })

    if (createdOrganizer) {
      organizers.value.push(createdOrganizer)
    } else {
      await loadAdminData()
    }

    closeCreateForm()
  } catch (e) {
    console.error(e)
    error.value = "Impossible de créer l'organisation."
  }
}

function handleEditOrganizer(organizer) {
  showCreateForm.value = false
  editingOrganizer.value = organizer
  editForm.name = organizer.name ?? organizer.nomStructure ?? ''
  editForm.pole = organizer.pole ?? ''
}

function cancelEditOrganizer() {
  editingOrganizer.value = null
  resetEditForm()
}

async function submitEditOrganizer() {
  if (!editingOrganizer.value) return

  try {
    error.value = ''
    const name = editForm.name.trim()

    if (!name) {
      error.value = "Le nom de l'organisation est obligatoire."
      return
    }

    const updatedOrganizer = await updateOrganizer(editingOrganizer.value.id, {
      ...editingOrganizer.value,
      name,
      nomStructure: name,
      pole: editForm.pole
    })

    const index = organizers.value.findIndex((item) => item.id === editingOrganizer.value.id)
    if (index !== -1 && updatedOrganizer) {
      organizers.value[index] = updatedOrganizer
    } else {
      await loadAdminData()
    }

    cancelEditOrganizer()
  } catch (e) {
    console.error(e)
    error.value = "Impossible de modifier l'organisation."
  }
}

async function deleteOrganizer(organizer) {
  const confirmed = confirm(`Voulez-vous vraiment supprimer l'organisation : ${organizer.name} ?`)

  if (!confirmed) return

  try {
    error.value = ''

    await deleteOrganizerApi(organizer.id)
    organizers.value = organizers.value.filter((item) => item.id !== organizer.id)
  } catch (e) {
    console.error(e)
    error.value = "Impossible de supprimer l'organisation."
  }
}

async function updateOrganizerPole({ organizerId, pole }) {
  const organizer = organizers.value.find((item) => item.id === organizerId)

  if (!organizer) return

  try {
    error.value = ''

    const updatedOrganizer = await updateOrganizer(organizerId, {
      ...organizer,
      pole
    })

    Object.assign(organizer, updatedOrganizer)
  } catch (e) {
    console.error(e)
    error.value = "Impossible de modifier le pôle de l'organisation."
  }
}

onMounted(() => {
  loadAdminData()
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

.section-head--tight {
  margin-bottom: 18px;
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
  background: white;
  color: #5c4b74;
  border-radius: 14px;
  padding: 12px 18px;
  font-weight: 600;
  cursor: pointer;
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

.filter-zone {
  display: flex;
  justify-content: flex-start;
  margin-bottom: 18px;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.filter-group label {
  color: #4b3b5f;
  font-weight: 600;
  font-size: 13px;
}

.filter-select {
  border: 1px solid #e5dff0;
  background: #faf8fd;
  color: #58486f;
  border-radius: 999px;
  padding: 10px 16px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  outline: none;
  min-width: 260px;
}

.filter-select:focus {
  border-color: #7c3aed;
}

.organizer-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.empty-state {
  border: 1px dashed #dacfe8;
  border-radius: 18px;
  padding: 24px;
  text-align: center;
  color: #7a7284;
  background: #fbf9fd;
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
  gap: 16px;
  margin-bottom: 18px;
  flex-wrap: wrap;
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
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-weight: 600;
  color: #2f243b;
}

.form-group input,
.form-group textarea,
.form-group select {
  border: 1px solid #ddd6e6;
  border-radius: 14px;
  padding: 12px 14px;
  background: white;
  outline: none;
}

.full-width {
  grid-column: 1 / -1;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 768px) {
  .inline-form-grid {
    grid-template-columns: 1fr;
  }

  .filter-select {
    min-width: 100%;
  }
}
</style>

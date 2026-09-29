<template>
  <div class="dashboard-shell">
    <AdminSidebar :items="sidebarItems" active="Pôles & Organisateurs" />

    <div class="dashboard-content">
      <AdminHeader
          v-model="search"
          title="Gestion des pôles et organisateurs"
          :user="user"
      />

      <main class="dashboard-main">
        <section class="section-block">
          <div class="section-head">
            <div>
              <p class="eyebrow">Administration</p>
              <h2>Pôles</h2>
              <p class="section-text">
                Gère les espaces universitaires avec une présentation moderne en cartes.
              </p>
            </div>

            <button class="primary-btn" @click="handleCreatePole">
              + Créer un pôle
            </button>
          </div>

          <div class="poles-grid">
            <PoleCard
                v-for="pole in poles"
                :key="pole.id"
                :pole="pole"
                @edit="handleEditPole"
                @delete="deletePole"
            />
          </div>
        </section>

        <section class="section-block">
          <div class="section-head section-head--tight">
            <div>
              <p class="eyebrow">Gestion</p>
              <h2>Organisateurs</h2>
              <p class="section-text">
                Réaffecte facilement un organisateur à un pôle avec une liste déroulante.
              </p>
            </div>

            <button class="primary-btn" @click="handleCreateOrganizer">
              + Ajouter un organisateur
            </button>
          </div>

          <div class="filter-tabs">
            <button
                v-for="tab in tabs"
                :key="tab"
                type="button"
                class="filter-tab"
                :class="{ 'filter-tab--active': selectedPoleFilter === tab }"
                @click="selectedPoleFilter = tab"
            >
              {{ tab }} ({{ counters[tab] ?? 0 }})
            </button>
          </div>

          <div class="organizer-list">
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
import { computed, onMounted, ref } from 'vue'
import AdminHeader from '../../components/admin/AdminHeader.vue'
import AdminSidebar from '../../components/admin/AdminSidebar.vue'
import PoleCard from '../../components/admin/PoleCard.vue'
import OrganizerRow from '../../components/admin/OrganizerRow.vue'
import { useUser } from '../../composables/useUser.js'
import {
  deleteOrganizer as deleteOrganizerApi,
  deletePole as deletePoleApi,
  getOrganizers,
  getPoles,
  updateOrganizer
} from '@/services/api.js'
import { useRouter } from 'vue-router'

const sidebarItems = [
  { label: 'Pôles & Organisateurs', icon: '◫' },
  { label: 'Événements', icon: '◌' },
  { label: 'Utilisateurs', icon: '◍' }
]

const { user } = useUser()
const router = useRouter()

const search = ref('')
const selectedPoleFilter = ref('Tous')
const poles = ref([])
const organizers = ref([])

const poleOptions = computed(() => poles.value.map((pole) => pole.name))
const tabs = computed(() => ['Tous', ...poleOptions.value])

const filteredOrganizers = computed(() => {
  return organizers.value.filter((organizer) => {
    const matchesSearch =
        (organizer.name ?? '').toLowerCase().includes(search.value.toLowerCase()) ||
        (organizer.email ?? '').toLowerCase().includes(search.value.toLowerCase())

    const matchesPole =
        selectedPoleFilter.value === 'Tous' ||
        organizer.pole === selectedPoleFilter.value

    return matchesSearch && matchesPole
  })
})

const counters = computed(() => {
  const result = { Tous: organizers.value.length }

  poleOptions.value.forEach((poleName) => {
    result[poleName] = organizers.value.filter(
        (organizer) => organizer.pole === poleName
    ).length
  })

  return result
})

async function loadAdminData() {
  try {
    const [loadedPoles, loadedOrganizers] = await Promise.all([
      getPoles(),
      getOrganizers()
    ])
    poles.value = loadedPoles ?? []
    organizers.value = loadedOrganizers ?? []
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  loadAdminData()
})

const handleCreatePole = () => {
  router.push('/admin/poles')
}

const handleCreateOrganizer = () => {
  router.push('/admin/organizers')
}
const handleEditPole = () => {
  router.push('/admin/poles')
}

const handleEditOrganizer = () => {
  router.push('/admin/organizers')
}

const deletePole = async (pole) => {
  try {
    await deletePoleApi(pole.id)
    poles.value = poles.value.filter((item) => item.id !== pole.id)
  } catch (e) {
    console.error(e)
  }
}

const deleteOrganizer = async (organizer) => {
  try {
    await deleteOrganizerApi(organizer.id)
    organizers.value = organizers.value.filter((item) => item.id !== organizer.id)
  } catch (e) {
    console.error(e)
  }
}

const updateOrganizerPole = async ({ organizerId, pole }) => {
  const organizer = organizers.value.find((item) => item.id === organizerId)
  if (organizer) {
    try {
      const updatedOrganizer = await updateOrganizer(organizerId, {
        ...organizer,
        pole
      })
      Object.assign(organizer, updatedOrganizer)
    } catch (e) {
      console.error(e)
    }
  }
}
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
}

.poles-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 18px;
}

.filter-tabs {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 18px;
}

.filter-tab {
  border: 1px solid #e5dff0;
  background: #faf8fd;
  color: #58486f;
  border-radius: 999px;
  padding: 10px 14px;
  font-weight: 600;
}

.filter-tab--active {
  background: #2d1840;
  color: white;
  border-color: #2d1840;
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
</style>

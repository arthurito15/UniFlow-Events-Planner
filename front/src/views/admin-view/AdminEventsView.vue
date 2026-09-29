<template>
  <div class="dashboard-shell">
    <AdminSidebar :items="sidebarItems" active="Événements" />

    <div class="dashboard-content">
      <AdminHeader
          v-model="search"
          title="Gestion des événements"
          :user="user"
      />

      <main class="dashboard-main">
        <div class="section-block filter-section">
          <p class="eyebrow">Filtres</p>

          <div class="filter-row">
            <button class="filter-tab filter-tab--active">Tous</button>
            <button class="filter-tab">Cette semaine</button>
            <button class="filter-tab">Ce mois</button>
            <button class="filter-tab">Gratuit</button>

            <select v-model="selectedPole" class="filter-select">
              <option value="">Tous les pôles</option>
              <option
                  v-for="pole in poles"
                  :key="pole.id"
                  :value="pole.name"
              >
                {{ pole.name }}
              </option>
            </select>
          </div>
        </div>

        <div class="section-block">
          <div class="section-head">
            <div>
              <p class="eyebrow">Administration</p>
              <h2>Événements</h2>
              <p class="section-text">
                Consulte tous les événements de la plateforme et supprime ceux qui ne doivent plus apparaître.
              </p>
            </div>
          </div>

          <div v-if="loading" class="skeleton-grid">
            <div v-for="i in 6" :key="i" class="skeleton-card"></div>
          </div>

          <div v-else-if="errorMessage" class="error-state">
            {{ errorMessage }}
          </div>

          <div v-else-if="displayedEvents.length === 0" class="empty-state">
            Aucun événement trouvé.
          </div>

          <div v-else class="events-grid">
            <div
                v-for="event in displayedEvents"
                :key="event.id"
                class="event-card-wrapper"
            >
              <router-link
                  :to="`/events/${event.id}`"
                  class="event-link"
              >
                <EventCard
                    :event="event"
                    :favorite="false"
                />
              </router-link>

              <button
                  class="delete-btn"
                  @click="handleDeleteEvent(event)"
              >
                Supprimer
              </button>
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import AdminHeader from '@/components/admin/AdminHeader.vue'
import AdminSidebar from '@/components/admin/AdminSidebar.vue'
import EventCard from '@/components/EventCard.vue'
import { useUser } from '@/composables/useUser.js'
import {
  getAllEvents,
  getPoles,
  deleteEvent as deleteEventApi
} from '@/services/api.js'

const { user } = useUser()

const search = ref('')
const events = ref([])
const poles = ref([])
const selectedPole = ref('')
const loading = ref(false)
const errorMessage = ref('')

const sidebarItems = [
  { label: 'Pôles', icon: '◫', route: '/admin/poles' },
  { label: 'Organisations', icon: '◉', route: '/admin/organizers' },
  { label: 'Événements', icon: '◌', route: '/admin/events' },
  { label: 'Utilisateurs', icon: '◍', route: '/admin/users' }
]

const displayedEvents = computed(() => {
  const value = search.value.toLowerCase().trim()

  return events.value.filter((event) => {
    const matchSearch =
        !value ||
        event.title?.toLowerCase().includes(value) ||
        event.description?.toLowerCase().includes(value) ||
        event.location?.toLowerCase().includes(value) ||
        event.address?.toLowerCase().includes(value) ||
        event.organizer?.toLowerCase().includes(value) ||
        event.organisationName?.toLowerCase().includes(value)

    const eventPoleName =
        event.poleName ||
        event.pole?.name ||
        event.tags?.[0]

    const matchPole =
        !selectedPole.value ||
        eventPoleName === selectedPole.value

    return matchSearch && matchPole
  })
})

async function loadEvents() {
  try {
    loading.value = true
    errorMessage.value = ''
    events.value = await getAllEvents()
  } catch (e) {
    console.error(e)
    errorMessage.value = 'Impossible de charger les événements.'
  } finally {
    loading.value = false
  }
}

async function loadPoles() {
  try {
    poles.value = await getPoles()
  } catch (e) {
    console.error(e)
  }
}

async function handleDeleteEvent(event) {
  const confirmed = confirm(`Voulez-vous vraiment supprimer l’événement : ${event.title} ?`)

  if (!confirmed) return

  try {
    errorMessage.value = ''

    await deleteEventApi(event.id)

    events.value = events.value.filter((item) => item.id !== event.id)
  } catch (e) {
    console.error(e)
    errorMessage.value = "Impossible de supprimer l'événement."
  }
}

onMounted(async () => {
  await loadEvents()
  await loadPoles()
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
  min-width: 0;
}

.dashboard-main {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.section-block {
  background: #ffffff;
  border: 1px solid #eee8f5;
  border-radius: 24px;
  padding: 24px;
  box-shadow: 0 8px 32px rgba(25, 15, 35, 0.04);
}

.filter-section {
  padding: 18px 24px;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.eyebrow {
  margin: 0 0 6px;
  color: #8b7aa3;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.09em;
}

.section-head h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #24192f;
}

.section-text {
  margin: 6px 0 0;
  font-size: 14px;
  color: #756d80;
  line-height: 1.5;
}

.filter-row {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 8px;
  align-items: center;
}

.filter-tab {
  border: 1px solid #e5dff0;
  background: #faf8fd;
  color: #58486f;
  border-radius: 999px;
  padding: 8px 16px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
}

.filter-tab:hover {
  border-color: #c4b3e0;
  background: #f2eef9;
}

.filter-tab--active {
  background: #2d1840;
  color: white;
  border-color: #2d1840;
  box-shadow: 0 4px 12px rgba(45, 24, 64, 0.18);
}

.filter-select {
  border: 1px solid #e5dff0;
  background: #faf8fd;
  color: #58486f;
  border-radius: 999px;
  padding: 8px 14px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  outline: none;
}

.filter-select:focus {
  border-color: #7c3aed;
}

.events-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
  width: 100%;
}

.event-card-wrapper {
  position: relative;
}

.event-link {
  text-decoration: none;
  color: inherit;
  display: block;
}

.delete-btn {
  position: absolute;
  right: 12px;
  bottom: 12px;
  z-index: 5;
  border: none;
  background: #dc2626;
  color: white;
  border-radius: 12px;
  padding: 8px 12px;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 8px 18px rgba(220, 38, 38, 0.25);
}

.delete-btn:hover {
  background: #b91c1c;
}

.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}

.skeleton-card {
  height: 220px;
  border-radius: 20px;
  background: linear-gradient(90deg, #f2eef9 25%, #ece5f7 50%, #f2eef9 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s infinite;
}

@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }

  100% {
    background-position: -200% 0;
  }
}

.empty-state {
  border: 1px dashed #dacfe8;
  border-radius: 18px;
  padding: 32px;
  text-align: center;
  color: #7a7284;
  background: #fbf9fd;
}

.error-state {
  background: #fff5f5;
  border: 1px solid #f5d0d0;
  border-radius: 14px;
  padding: 16px 20px;
  color: #c53030;
  font-weight: 500;
}
</style>
<template>
  <div class="page-content">
    <div class="page-header">
      <div></div>
      <router-link to="/orga/create-event" class="primary-btn">
        + Créer un événement
      </router-link>
    </div>

    <div class="stats-grid">
      <div class="stat-card">
        <span class="stat-label">Événements actifs</span>
        <span class="stat-value">{{ stats.active }}</span>
      </div>
      <div class="stat-card">
        <span class="stat-label">Total participants</span>
        <span class="stat-value">{{ stats.participants }}</span>
      </div>
      <div class="stat-card">
        <span class="stat-label">Listes d'attente</span>
        <span class="stat-value">{{ stats.waitlist }}</span>
      </div>
      <div class="stat-card">
        <span class="stat-label">Événements passés</span>
        <span class="stat-value">{{ stats.past }}</span>
      </div>
    </div>

    <div class="bottom-grid">
      <!--
      <div class="chart-card">
        <p class="chart-placeholder">Un diagramme</p>
      </div>
      -->
      <div class="upcoming-card">
        <h3>Mes événements à venir</h3>
        <div class="upcoming-list">
          <div v-for="event in upcomingEvents" :key="event.id" class="upcoming-item">
            <span class="upcoming-title">{{ event.title }}</span>
            <span class="upcoming-date">{{ event.date }}</span>
          </div>
          <div v-if="upcomingEvents.length === 0" class="empty-state">
            Aucun événement à venir
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useCurrentOrganisation } from '@/composables/useCurrentOrganisation.js'
import { getEventsByOrga, getParticipants } from '@/services/api.js'
import { useRoute } from 'vue-router'

const { currentOrga, loadCurrentOrganisation } = useCurrentOrganisation()
const events = ref([])
const participantCount = ref(0)

const upcomingEvents = computed(() => events.value.slice(0, 3))

const stats = computed(() => ({
  active: events.value.length,
  participants: participantCount.value,
  waitlist: 0,
  past: events.value.filter((event) => {
    return event.endDate && new Date(event.endDate) < new Date()
  }).length
}))


const route = useRoute()

async function loadData() {
  const organisation = await loadCurrentOrganisation()
  if (!organisation?.id) return

  try {
    events.value = await getEventsByOrga(organisation.id)

    const participantsByEvent = await Promise.all(
        events.value.map((event) => getParticipants(event.id))
    )

    participantCount.value = participantsByEvent.reduce((total, participants) => {
      return total + (participants?.length ?? 0)
    }, 0)

  } catch (e) {
    console.error(e)
  }
}

// 1. premier chargement
onMounted(loadData)

// 2. à chaque changement d’orga dans l’URL
watch(
    () => route.query.org,
    () => {
      loadData()
    }
)

</script>

<style scoped>
.page-content { display: flex; flex-direction: column; gap: 24px; }

.page-header { display: flex; justify-content: space-between; align-items: flex-start; }
.page-header h1 { font-size: 28px; font-weight: 700; color: #1e1030; margin: 0 0 4px; }
.subtitle { color: #756d80; margin: 0; font-size: 14px; }

.primary-btn {
  background: #7c3aed; color: white; border: none; border-radius: 14px;
  padding: 12px 20px; font-size: 14px; font-weight: 600; cursor: pointer;
  text-decoration: none; box-shadow: 0 10px 20px rgba(124,58,237,0.18);
}
.primary-btn:hover { background: #6d28d9; }

.stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.stat-card {
  background: white; border: 1px solid #ebe5f3; border-radius: 20px;
  padding: 20px 24px; display: flex; flex-direction: column; gap: 8px;
  box-shadow: 0 4px 16px rgba(30,16,48,0.05);
}
.stat-label { font-size: 13px; color: #9b8fb0; font-weight: 500; }
.stat-value { font-size: 32px; font-weight: 700; color: #1e1030; }

.bottom-grid { display: grid; grid-template-columns: 1fr 320px; gap: 20px; }
.chart-card {
  background: white; border: 1px solid #ebe5f3; border-radius: 20px;
  padding: 24px; min-height: 260px; display: flex; align-items: center;
  justify-content: center; box-shadow: 0 4px 16px rgba(30,16,48,0.05);
}
.chart-placeholder { color: #b0a3c4; font-size: 15px; }

.upcoming-card {
  background: white; border: 1px solid #ebe5f3; border-radius: 20px;
  padding: 24px; box-shadow: 0 4px 16px rgba(30,16,48,0.05);
}
.upcoming-card h3 { margin: 0 0 16px; font-size: 15px; font-weight: 600; color: #1e1030; }
.upcoming-list { display: flex; flex-direction: column; gap: 10px; }
.upcoming-item { background: #f7f5fb; border-radius: 12px; padding: 12px 14px; display: flex; flex-direction: column; gap: 4px; }
.upcoming-title { font-size: 14px; font-weight: 600; color: #24192f; }
.upcoming-date { font-size: 12px; color: #9b8fb0; }
.empty-state { color: #b0a3c4; font-size: 14px; text-align: center; padding: 20px 0; }
</style>

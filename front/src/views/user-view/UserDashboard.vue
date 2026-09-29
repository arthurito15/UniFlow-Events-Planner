<template>
  <div class="user-dashboard">

    <!-- Filter bar -->
    <div class="section-block filter-section">
      <p class="eyebrow">Filtres</p>
      <div class="filter-row">
        <button class="filter-tab filter-tab--active">Tous</button>
        <button class="filter-tab">Cette semaine</button>
        <button class="filter-tab">Ce mois</button>
        <button class="filter-tab">Gratuit</button>
      </div>
    </div>

    <!-- Events -->
    <div class="section-block">
      <div class="section-head">
        <div>
          <p class="eyebrow">Catalogue</p>
          <h2>Événements</h2>
          <p class="section-text">Découvre les événements proposés par les pôles de ton université.</p>
        </div>
      </div>

      <div v-if="loading" class="skeleton-grid">
        <div v-for="i in 6" :key="i" class="skeleton-card"></div>
      </div>

      <div v-else-if="errorMessage" class="error-state">
        {{ errorMessage }}
      </div>

      <div v-else-if="events.length === 0" class="empty-state">
        Aucun événement trouvé.
      </div>

      <div v-else class="events-grid">
        <EventCard
            v-for="event in events"
            :key="event.id"
            :event="event"
            :favorite="event.favorite"
            @toggle-favorite="onToggleFavorite(event)"
        />
      </div>
    </div>

  </div>
</template>

<script setup>
import { watch, onMounted } from 'vue'
import EventCard from '@/components/EventCard.vue'
import { useEvent } from '@/composables/useEvent.js'
import { useUser } from '@/composables/useUser'
import { addFavori, removeFavori } from '@/services/api.js'
import { useRoute } from 'vue-router'

const { events, loading, errorMessage, loadEvents, loadEventsByPole } = useEvent()
const { user } = useUser()

async function onToggleFavorite(event) {
  const newState = !event.favorite
  // Optimistic update local
  const idx = events.value.findIndex(e => e.id === event.id)
  if (idx !== -1) events.value[idx] = { ...events.value[idx], favorite: newState }

  try {
    if (newState) {
      await addFavori(user.value.id, event.id)
    } else {
      await removeFavori(user.value.id, event.id)
    }
    window.dispatchEvent(new CustomEvent('uniflow:favoris-updated'))
  } catch {
    // Rollback
    if (idx !== -1) events.value[idx] = { ...events.value[idx], favorite: !newState }
  }
}

onMounted(() => {
  loadEvents()
})

const route = useRoute()
watch(
    () => route.query.pole,
    (pole) => {
      if (pole) {
        loadEventsByPole(pole)
      } else {
        loadEvents()
      }
    },
    { immediate: true }
)
</script>

<style scoped>
.user-dashboard {
  display: flex;
  flex-direction: column;
  gap: 20px;
  width: 100%;
  min-width: 0;
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

/* Filter tabs */
.filter-row {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 8px;
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
  transition: all 0.2s;
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

/* Grid */
.events-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
  width: 100%;
}

/* Skeleton */
.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
  width: 100%;
}

.skeleton-card {
  height: 220px;
  border-radius: 20px;
  background: linear-gradient(90deg, #f2eef9 25%, #ece5f7 50%, #f2eef9 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s infinite;
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* States */
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
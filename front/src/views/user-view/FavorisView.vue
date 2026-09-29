<template>
  <div class="page">

    <div class="section-block">
      <div class="section-head">
        <div>
          <p class="eyebrow">Mes favoris</p>
          <h2>Événements favoris</h2>
          <p class="section-text">Les événements que tu as mis en favoris.</p>
        </div>
      </div>

      <div v-if="loading" class="skeleton-grid">
        <div v-for="i in 6" :key="i" class="skeleton-card"></div>
      </div>

      <div v-else-if="errorMessage" class="error-state">{{ errorMessage }}</div>

      <div v-else-if="events.length === 0" class="empty-state">
        <p>⭐ Aucun favori pour l'instant.</p>
        <p class="empty-sub">Explore les événements et clique sur l'étoile pour les retrouver ici.</p>
      </div>

      <div v-else class="events-grid">
        <EventCard
            v-for="event in events"
            :key="event.id"
            :event="event"
            :favorite="true"
            @toggle-favorite="onToggleFavorite(event)"
        />
      </div>
    </div>

  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import EventCard from '@/components/EventCard.vue'
import { useUser } from '@/composables/useUser'
import { getFavoris, removeFavori, addFavori } from '@/services/api.js'

const { user } = useUser()

const events = ref([])
const loading = ref(false)
const errorMessage = ref('')

async function loadFavoris() {
  if (!user.value) return
  loading.value = true
  errorMessage.value = ''
  try {
    events.value = await getFavoris(user.value.id)
  } catch (e) {
    errorMessage.value = e.message
  } finally {
    loading.value = false
  }
}

async function onToggleFavorite(event) {
  // Retire immédiatement de la liste (on est sur la page favoris)
  events.value = events.value.filter(e => e.id !== event.id)
  try {
    await removeFavori(user.value.id, event.id)
    window.dispatchEvent(new CustomEvent('uniflow:favoris-updated'))
  } catch {
    await loadFavoris()
  }
}

onMounted(loadFavoris)
</script>

<style scoped>
.page {
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

.events-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
  width: 100%;
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
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.empty-state {
  border: 1px dashed #dacfe8;
  border-radius: 18px;
  padding: 40px;
  text-align: center;
  color: #7a7284;
  background: #fbf9fd;
  font-size: 15px;
}

.empty-sub {
  margin-top: 8px;
  font-size: 13px;
  color: #a394b8;
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
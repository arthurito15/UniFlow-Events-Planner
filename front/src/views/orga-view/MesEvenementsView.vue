<template>
  <div class="page-content">
    <div class="page-header">
      <div></div>
      <router-link to="/orga/create-event" class="primary-btn">
        + Créer un événement
      </router-link>
    </div>

    <div class="section-block">
      <div v-if="events.length === 0" class="empty-state">
        Aucun événement pour le moment.
      </div>

      <div class="event-list">
        <div v-for="event in events" :key="event.id" class="event-row">
          <div class="event-avatar">
            {{ event.title.charAt(0).toUpperCase() }}
          </div>

          <div class="event-info">
            <h3>{{ event.title }}</h3>
            <p>{{ event.location || event.address || '—' }}</p>
          </div>

          <div class="event-date">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
            {{ event.date || event.beginDate || '—' }}
          </div>

          <div class="event-organizer">
            {{ event.organizer || event.organisateur || '—' }}
          </div>

          <div class="event-actions">
            <button class="ghost-btn ghost-btn--danger" @click="deleteEvent(event)">Supprimer</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useCurrentOrganisation } from '@/composables/useCurrentOrganisation.js'
import { deleteEvent as deleteEventApi, getEventsByOrga } from '@/services/api.js'

const { currentOrga, loadCurrentOrganisation } = useCurrentOrganisation()
const events = ref([])
const errorMsg = ref('')

async function loadEvents() {
  const organisation = await loadCurrentOrganisation()
  if (!organisation?.id) return

  try {
    errorMsg.value = ''
    events.value = await getEventsByOrga(organisation.id)
  } catch (e) {
    console.error(e)
    errorMsg.value = 'Impossible de charger les événements.'
  }
}

onMounted(() => {
  loadEvents()
})

const deleteEvent = async (event) => {
  try {
    await deleteEventApi(event.id)
    events.value = events.value.filter((item) => item.id !== event.id)
  } catch (e) {
    console.error(e)
    errorMsg.value = "Impossible de supprimer l'événement."
  }
}
</script>

<style scoped>
.page-content { display: flex; flex-direction: column; gap: 24px; }

.page-header { display: flex; justify-content: space-between; align-items: flex-start; }
.page-header h1 { font-size: 28px; font-weight: 700; color: #1e1030; margin: 0 0 4px; }
.subtitle { color: #756d80; margin: 0; font-size: 14px; }

.primary-btn {
  background: #7c3aed; color: white; border: none; border-radius: 14px;
  padding: 12px 20px; font-size: 14px; font-weight: 600; cursor: pointer;
  text-decoration: none; box-shadow: 0 10px 20px rgba(124,58,237,0.18); transition: background 0.15s;
}
.primary-btn:hover { background: #6d28d9; }

.section-block {
  background: white; border: 1px solid #eee8f5; border-radius: 24px;
  padding: 24px; box-shadow: 0 18px 40px rgba(25,15,35,0.04);
}

.event-list { display: flex; flex-direction: column; gap: 14px; }

.event-row {
  background: white; border: 1px solid #ebe5f3; border-radius: 20px;
  padding: 16px 18px; display: flex; align-items: center; gap: 16px;
  box-shadow: 0 4px 16px rgba(30,16,48,0.04); flex-wrap: wrap;
}

.event-avatar {
  width: 48px; height: 48px; border-radius: 14px; background: #ede9fe;
  color: #5b21b6; display: flex; align-items: center; justify-content: center;
  font-weight: 700; font-size: 18px; flex-shrink: 0;
}

.event-info { flex: 1; min-width: 160px; }
.event-info h3 { margin: 0 0 4px; color: #24192f; font-size: 15px; }
.event-info p { margin: 0; color: #756d80; font-size: 13px; }

.event-date {
  display: flex; align-items: center; gap: 6px;
  color: #756d80; font-size: 13px; white-space: nowrap;
}

.event-organizer {
  color: #5b21b6; font-size: 13px; font-weight: 600;
  background: #ede9fe; border-radius: 999px; padding: 4px 12px;
}

.event-actions { display: flex; gap: 8px; }

.ghost-btn {
  border: 1px solid #e7e0f1; background: white; color: #5c4b74;
  border-radius: 12px; padding: 8px 14px; font-size: 13px; cursor: pointer; transition: background 0.15s;
}
.ghost-btn:hover { background: #f5f0fb; }
.ghost-btn--danger { color: #b42318; }
.ghost-btn--danger:hover { background: #fff1f0; }

.empty-state {
  border: 1px dashed #dacfe8; border-radius: 18px; padding: 32px;
  text-align: center; color: #7a7284; background: #fbf9fd;
}
</style>

<template>
  <div class="page-content">

    <!-- Sélecteur d'événement -->
    <div class="section-block">
      <div class="filter-row">
        <label class="filter-label">Événement :</label>
        <select v-model="selectedEventId" class="event-select">
          <option value="">Tous les événements</option>
          <option v-for="event in events" :key="event.id" :value="event.id">
            {{ event.title }}
          </option>
        </select>
        <span class="participant-count">{{ filteredParticipants.length }} participant(s)</span>
      </div>
    </div>

    <!-- Liste participants -->
    <div class="section-block">
      <div v-if="filteredParticipants.length === 0" class="empty-state">
        Aucun participant pour cet événement.
      </div>

      <div class="participant-list">
        <div v-for="p in filteredParticipants" :key="p.id" class="participant-row">
          <div class="participant-avatar">
            {{ p.prenom.charAt(0) }}{{ p.nom.charAt(0) }}
          </div>

          <div class="participant-info">
            <h3>{{ p.prenom }} {{ p.nom }}</h3>
            <p>{{ p.email }}</p>
          </div>

          <div class="participant-event">
            {{ getEventTitle(p.eventId) }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useCurrentOrganisation } from '@/composables/useCurrentOrganisation.js'
import { getEventsByOrga, getParticipants } from '@/services/api.js'

const { currentOrga, loadCurrentOrganisation } = useCurrentOrganisation()
const selectedEventId = ref('')
const events = ref([])
const participants = ref([])

const filteredParticipants = computed(() => {
  if (!selectedEventId.value) return participants.value
  return participants.value.filter((participant) => {
    return Number(participant.eventId) === Number(selectedEventId.value)
  })
})

const getEventTitle = (eventId) => {
  return events.value.find(e => Number(e.id) === Number(eventId))?.title || '—'
}

onMounted(async () => {
  const organisation = await loadCurrentOrganisation()
  if (!organisation?.id) return

  try {
    events.value = await getEventsByOrga(organisation.id)
    const participantsByEvent = await Promise.all(
        events.value.map((event) => getParticipants(event.id))
    )
    participants.value = participantsByEvent.flat()
  } catch (e) {
    console.error(e)
  }
})
</script>

<style scoped>
.page-content { display: flex; flex-direction: column; gap: 24px; }

.page-header { display: flex; justify-content: space-between; align-items: flex-start; }
.page-header h1 { font-size: 28px; font-weight: 700; color: #1e1030; margin: 0 0 4px; }
.subtitle { color: #756d80; margin: 0; font-size: 14px; }

.section-block {
  background: white; border: 1px solid #eee8f5; border-radius: 24px;
  padding: 24px; box-shadow: 0 18px 40px rgba(25,15,35,0.04);
}

.filter-row { display: flex; align-items: center; gap: 16px; flex-wrap: wrap; }
.filter-label { font-size: 14px; font-weight: 600; color: #3d2f58; }

.event-select {
  border: 1px solid #ddd6e6; border-radius: 12px; padding: 10px 14px;
  font-size: 14px; color: #24192f; outline: none; background: white; min-width: 220px;
}
.event-select:focus { border-color: #7c3aed; }

.participant-count {
  margin-left: auto; background: #ede9fe; color: #5b21b6;
  border-radius: 999px; padding: 4px 14px; font-size: 13px; font-weight: 600;
}

.participant-list { display: flex; flex-direction: column; gap: 12px; }

.participant-row {
  border: 1px solid #ebe5f3; border-radius: 20px; padding: 16px 18px;
  display: flex; align-items: center; gap: 16px;
  box-shadow: 0 4px 16px rgba(30,16,48,0.04); flex-wrap: wrap;
}

.participant-avatar {
  width: 48px; height: 48px; border-radius: 14px; background: #ede9fe;
  color: #5b21b6; display: flex; align-items: center; justify-content: center;
  font-weight: 700; font-size: 16px; flex-shrink: 0;
}

.participant-info { flex: 1; min-width: 160px; }
.participant-info h3 { margin: 0 0 4px; color: #24192f; font-size: 15px; }
.participant-info p { margin: 0; color: #756d80; font-size: 13px; }

.participant-event {
  color: #5b21b6; font-size: 13px; font-weight: 600;
  background: #ede9fe; border-radius: 999px; padding: 4px 12px; white-space: nowrap;
}

.empty-state {
  border: 1px dashed #dacfe8; border-radius: 18px; padding: 32px;
  text-align: center; color: #7a7284; background: #fbf9fd;
}
</style>
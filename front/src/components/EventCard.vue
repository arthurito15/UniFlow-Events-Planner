<template>
  <div class="event-card-wrap">

      <div class="event-card">
        <button
            class="fav-btn"
            :class="{ 'fav-btn--active': favorite }"
            :title="favorite ? 'Retirer des favoris' : 'Ajouter aux favoris'"
            @click.prevent.stop="$emit('toggle-favorite')"
        >
          {{ favorite ? '★' : '☆' }}
        </button>
        <router-link
            :to="{ name: 'event-detail', params: { id: event.id } }"
            class="event-card-link"
        >
          <div class="event-banner">
            <span class="event-category">{{ event.poleName ?? 'Événement' }}</span>
          </div>

          <div class="event-body">
            <p class="event-date">{{ event.date ?? event.beginDate }}</p>
            <h3 class="event-title">{{ event.title }}</h3>
            <p class="event-organizer">par <strong>{{ event.organisationName }}</strong></p>
          </div>

          <div class="event-footer">
            <span class="event-arrow">→</span>
          </div>
        </router-link>
      </div>

  </div>
</template>

<script setup>
defineProps({
  event: Object,
  favorite: Boolean,
  registered: Boolean
})

defineEmits(['toggle-favorite'])
</script>

<style scoped>
.event-card-wrap {
  position: relative;
  display: block;
  height: 100%;
}

.event-card-link {
  text-decoration: none;
  display: block;
  height: 100%;
}

.event-card {
  background: #ffffff;
  border: 1px solid #eee8f5;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(25, 15, 35, 0.05);
  transition: transform 0.2s, box-shadow 0.2s;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.event-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 16px 40px rgba(124, 58, 237, 0.12);
}

.event-banner {
  height: 120px;
  background-image: url("https://storage.letudiant.fr/mediatheque/educpros/2/6/22426-lyon-1-campus-580x310.jpeg");
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  display: flex;
  align-items: flex-end;
  padding: 12px 14px;
}

.event-category {
  background: rgba(255, 255, 255, 0.15);
  color: rgba(255, 255, 255, 0.9);
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.event-body {
  padding: 16px;
  flex: 1;
}

.event-date {
  margin: 0 0 6px;
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: #8b7aa3;
}

.event-title {
  margin: 0 0 8px;
  font-size: 15px;
  font-weight: 700;
  color: #24192f;
  line-height: 1.35;
}

.event-organizer {
  margin: 0;
  font-size: 13px;
  color: #756d80;
}

.event-organizer strong { color: #58486f; }

.event-footer {
  padding: 10px 16px;
  border-top: 1px solid #f2eef9;
  display: flex;
  justify-content: flex-end;
}

.event-arrow {
  font-size: 18px;
  color: #7c3aed;
  font-weight: 700;
  transition: transform 0.2s;
}

.event-card:hover .event-arrow { transform: translateX(4px); }

/* Étoile flottante */
.fav-btn {
  position: absolute;
  top: 15px;
  right: 15px;
  z-index: 2;
  border: none;
  background: none;
  color: rgba(255, 255, 255, 0.5);
  font-size: 30px;
  line-height: 1;
  padding: 0;
  cursor: pointer;
  transition: color 0.15s, transform 0.15s;
}

.fav-btn:hover {
  color: #f59e0b;
  transform: scale(1.2);
}

.fav-btn--active {
  color: #f59e0b;
}
</style>
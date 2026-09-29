<template>
  <div class="event-detail">

    <div v-if="loading" class="skeleton-hero"></div>

    <div v-else-if="errorMessage" class="error-state">
      {{ errorMessage }}
    </div>

    <div v-else-if="event" class="detail-layout">

      <EventDetailHero
          :title="event.title"
          :date="event.date"
          :organizer="event.organizer"
      />

      <div class="detail-columns">
        <div class="detail-main">
          <EventDetailInfo
              :location="event.location"
              :startTime="event.startTime"
              :endTime="event.endTime"
              :tags="event.tags"
              :description="event.description"
          />
        </div>

        <div class="detail-aside">
          <div class="section-block">
            <p class="eyebrow">Participation</p>
            <h2 class="aside-title">Actions</h2>
            <EventDetailActions
                :favorite="favorite"
                :is-registered="isRegistered"
                :is-registering="isRegistering"
                @toggle-favorite="toggleFavorite"
                @register="register"
            />
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { watch } from 'vue'
import { useRoute } from 'vue-router'
import EventDetailHero from '@/components/event-detail/EventDetailHero.vue'
import EventDetailInfo from '@/components/event-detail/EventDetailInfo.vue'
import EventDetailActions from '@/components/event-detail/EventDetailActions.vue'
import { useEvent } from '@/composables/useEvent.js'

const route = useRoute()
const { event, favorite, loading, errorMessage, isRegistering, isRegistered, loadEvent, toggleFavorite, register } = useEvent()

watch(
    () => route.params.id,
    (id) => { if (id) loadEvent(id) },
    { immediate: true }
)
</script>

<style scoped>
.event-detail {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-layout {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-columns {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 20px;
  align-items: start;
}

@media (max-width: 860px) {
  .detail-columns {
    grid-template-columns: 1fr;
  }
}

.section-block {
  background: #ffffff;
  border: 1px solid #eee8f5;
  border-radius: 24px;
  padding: 24px;
  box-shadow: 0 8px 32px rgba(25, 15, 35, 0.04);
}

.eyebrow {
  margin: 0 0 4px;
  color: #8b7aa3;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.09em;
}

.aside-title {
  margin: 0 0 20px;
  font-size: 18px;
  font-weight: 700;
  color: #24192f;
}

/* Skeleton */
.skeleton-hero {
  height: 220px;
  border-radius: 24px;
  background: linear-gradient(90deg, #f2eef9 25%, #ece5f7 50%, #f2eef9 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s infinite;
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
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
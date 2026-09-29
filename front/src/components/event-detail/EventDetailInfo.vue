<template>
  <div class="section-block">
    <p class="eyebrow">Détails</p>
    <h2 class="info-title">Informations</h2>

    <div class="info-grid">
      <div class="info-item">
        <span class="info-icon">📍</span>
        <div>
          <div class="info-label">Lieu</div>
          <div class="info-value">{{ location || '–' }}</div>
        </div>
      </div>
      <div class="info-item">
        <span class="info-icon">🕐</span>
        <div>
          <div class="info-label">Horaire</div>
          <div class="info-value">{{ startTime }} – {{ endTime }}</div>
        </div>
      </div>
    </div>

    <div v-if="tags?.length" class="tags-row">
      <span v-for="tag in tags" :key="tag" class="tag-chip">
        {{ tag }}
      </span>
    </div>

    <hr class="divider" />

    <p class="description">{{ description }}</p>
  </div>
</template>

<script setup>
defineProps({
  location: String,
  startTime: String,
  endTime: String,
  tags: Array,
  description: String
})
</script>

<style scoped>
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

.info-title {
  margin: 0 0 20px;
  font-size: 20px;
  font-weight: 700;
  color: #24192f;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
  margin-bottom: 20px;
}

.info-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  background: #f7f5fb;
  border: 1px solid #eee8f5;
  border-radius: 16px;
  padding: 14px 16px;
}

.info-icon {
  font-size: 18px;
  flex-shrink: 0;
}

.info-label {
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: #8b7aa3;
  margin-bottom: 4px;
}

.info-value {
  font-size: 14px;
  font-weight: 600;
  color: #24192f;
}

.tags-row {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 20px;
}

.tag-chip {
  background: #ede9f8;
  color: #5b21b6;
  border: 1px solid #ddd5f5;
  border-radius: 999px;
  padding: 5px 12px;
  font-size: 12px;
  font-weight: 700;
}

.divider {
  border: none;
  border-top: 1px solid #f2eef9;
  margin: 0 0 20px;
}

.description {
  margin: 0;
  font-size: 14px;
  color: #4a4055;
  line-height: 1.7;
}
</style>
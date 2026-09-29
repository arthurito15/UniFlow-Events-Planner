<template>
  <div class="detail-hero">
    <div class="hero-banner"></div>
    <div class="hero-content">
      <h1 class="hero-title">{{ title }}</h1>
      <div class="hero-meta">
        <span class="meta-chip">📅 {{ date }}</span>
        <span class="meta-chip">👤 {{ organizer }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: String,
  date: String,
  organizer: String
})
</script>

<style scoped>
.detail-hero {
  background: #ffffff;
  border: 1px solid #eee8f5;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(25, 15, 35, 0.05);
}

.hero-banner {
  height: 180px;
  background: linear-gradient(135deg, #2d1840 0%, #5b21b6 50%, #7c3aed 100%);
}

.hero-content {
  padding: 24px;
}

.hero-title {
  margin: 0 0 16px;
  font-size: 28px;
  font-weight: 600;
  color: #24192f;
  line-height: 1.2;
}

.hero-meta {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.meta-chip {
  background: #f7f5fb;
  border: 1px solid #eee8f5;
  border-radius: 999px;
  padding: 7px 14px;
  font-size: 13px;
  font-weight: 600;
  color: #58486f;
}
</style>
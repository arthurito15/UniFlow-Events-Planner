<template>
  <article class="pole-card" :class="pole.theme || 'theme-purple'">
    <div class="top-row">
      <span class="theme-badge">Pôle</span>

      <div class="actions">
        <button type="button" class="ghost-btn" @click="$emit('edit', pole)">
          Modifier
        </button>
        <button type="button" class="ghost-btn ghost-btn--danger" @click="$emit('delete', pole)">
          Supprimer
        </button>
      </div>
    </div>

    <div class="content">
      <h3>{{ pole.name }}</h3>
    </div>
  </article>
</template>

<script setup>
defineProps({
  pole: {
    type: Object,
    required: true
  }
})

defineEmits(['edit', 'delete'])
</script>

<style scoped>
.pole-card {
  border: 1px solid #ebe5f3;
  border-radius: 22px;
  padding: 18px;
  box-shadow: 0 12px 26px rgba(30, 20, 45, 0.06);
  display: flex;
  flex-direction: column;
  gap: 26px;
  min-height: 150px;
  position: relative;
  overflow: hidden;
  background: white;
}

.pole-card::before {
  content: "";
  position: absolute;
  inset: 0;
  opacity: 0.16;
  pointer-events: none;
}

.theme-purple::before {
  background: linear-gradient(135deg, #7c3aed, #c084fc);
}

.theme-blue::before {
  background: linear-gradient(135deg, #2563eb, #93c5fd);
}

.theme-green::before {
  background: linear-gradient(135deg, #059669, #86efac);
}

.theme-orange::before {
  background: linear-gradient(135deg, #ea580c, #fdba74);
}

.top-row,
.content {
  position: relative;
  z-index: 1;
}

.top-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.theme-badge {
  background: rgba(255, 255, 255, 0.75);
  border: 1px solid #eee6f6;
  color: #5c4b74;
  border-radius: 999px;
  padding: 7px 12px;
  font-size: 13px;
  font-weight: 700;
}

.actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.ghost-btn {
  border: 1px solid #e7e0f1;
  background: rgba(255, 255, 255, 0.85);
  color: #5c4b74;
  border-radius: 12px;
  padding: 8px 12px;
  font-size: 13px;
  cursor: pointer;
}

.ghost-btn--danger {
  color: #b42318;
}

.pole-card h3 {
  margin: 0;
  font-size: 24px;
  color: #24192f;
}
</style>
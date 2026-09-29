<template>
  <article class="organizer-row">
    <div class="left">
      <div class="avatar">
        {{ organizer.name.charAt(0).toUpperCase() }}
      </div>

      <div class="info">
        <h3>{{ organizer.name }}</h3>
        <p>{{ organizer.email }}</p>
      </div>
    </div>

    <div class="right">
      <span class="status" :class="{ 'status--pending': organizer.status === 'En attente' }">
        {{ organizer.status }}
      </span>

      <select
          class="pole-select"
          :value="organizer.pole"
          @change="emitPoleChange"
      >
        <option v-for="option in poleOptions" :key="option" :value="option">
          {{ option }}
        </option>
      </select>

      <button type="button" class="ghost-btn" @click="$emit('edit', organizer)">
        Modifier
      </button>

      <button type="button" class="ghost-btn ghost-btn--danger" @click="$emit('delete', organizer)">
        Supprimer
      </button>
    </div>
  </article>
</template>

<script setup>
const props = defineProps({
  organizer: {
    type: Object,
    required: true
  },
  poleOptions: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['change-pole', 'edit', 'delete'])

const emitPoleChange = (event) => {
  emit('change-pole', {
    organizerId: props.organizer.id,
    pole: event.target.value
  })
}
</script>

<style scoped>
.organizer-row {
  background: white;
  border: 1px solid #ebe5f3;
  border-radius: 20px;
  padding: 16px 18px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  box-shadow: 0 10px 24px rgba(30, 20, 45, 0.04);
  flex-wrap: wrap;
}

.left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.avatar {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  background: #ede9fe;
  color: #5b21b6;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 18px;
}

.info h3 {
  margin: 0 0 4px;
  color: #24192f;
}

.info p {
  margin: 0;
  color: #756d80;
}

.right {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.status {
  background: #ecfdf3;
  color: #027a48;
  border-radius: 999px;
  padding: 7px 12px;
  font-size: 13px;
  font-weight: 600;
}

.status--pending {
  background: #fff7e6;
  color: #b54708;
}

.pole-select {
  border: 1px solid #ddd6e6;
  border-radius: 12px;
  padding: 10px 12px;
  background: white;
  min-width: 180px;
}

.ghost-btn {
  border: 1px solid #e7e0f1;
  background: white;
  color: #5c4b74;
  border-radius: 12px;
  padding: 10px 12px;
}

.ghost-btn--danger {
  color: #b42318;
}
</style>
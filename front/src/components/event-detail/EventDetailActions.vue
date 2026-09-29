<template>
  <div class="actions">
    <button
        type="button"
        class="action-btn"
        :class="favorite ? 'action-btn--fav-active' : 'action-btn--fav'"
        @click="$emit('toggle-favorite')"
    >
      <span class="btn-icon">{{ favorite ? '★' : '☆' }}</span>
      {{ favorite ? 'Dans mes favoris' : 'Ajouter aux favoris' }}
    </button>

    <button
        type="button"
        class="action-btn action-btn--register"
        :class="{ 'action-btn--registered': isRegistered }"
        :disabled="isRegistering"
        @click="$emit('register')"
    >
      <span v-if="isRegistering">⟳ Inscription en cours…</span>
      <span v-else-if="isRegistered">✓ Se désinscrire</span>
      <span v-else>Je m'inscris</span>
    </button>
  </div>
</template>

<script setup>
defineProps({
  favorite: Boolean,
  isRegistered: Boolean,
  isRegistering: Boolean
})

defineEmits(['toggle-favorite', 'register'])
</script>

<style scoped>
.actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  padding: 13px 18px;
  border-radius: 14px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.btn-icon {
  font-size: 16px;
}

/* Favori - idle */
.action-btn--fav {
  background: #f7f5fb;
  border: 1.5px solid #e5dff0;
  color: #58486f;
}

.action-btn--fav:hover {
  background: #f2eef9;
  border-color: #c4b3e0;
}

/* Favori - actif */
.action-btn--fav-active {
  background: #fffbeb;
  border: 1.5px solid #fde68a;
  color: #92400e;
}

/* S'inscrire */
.action-btn--register {
  background: #7c3aed;
  color: white;
  box-shadow: 0 10px 24px rgba(124, 58, 237, 0.25);
}

.action-btn--register:hover:not(:disabled) {
  background: #6d28d9;
  box-shadow: 0 12px 28px rgba(124, 58, 237, 0.35);
  transform: translateY(-1px);
}

.action-btn--registered {
  background: #2d1840;
  color: #ffffff;
}

.action-btn--register:disabled {
  cursor: wait;
  opacity: 0.75;
  box-shadow: none;
}
</style>

<template>
  <header class="app-header">
    <div class="header-left">
      <p class="eyebrow">Tableau de bord</p>
      <h1>{{ title }}</h1>
    </div>

    <div class="header-actions">
      <div class="search-wrap">
        <span class="search-icon">⌕</span>
        <input
            :value="modelValue"
            @input="$emit('update:modelValue', $event.target.value)"
            type="text"
            class="search-input"
            placeholder="Rechercher..."
        />
      </div>

      <div class="user-block">
        <div class="avatar">{{ userInitial }}</div>
        <div>
          <strong>{{ userName }}</strong>
          <p>Administrateur</p>
          <p v-if="userEmail">{{ userEmail }}</p>
        </div>
      </div>


    </div>
  </header>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUser } from '@/composables/useUser'

const props = defineProps({
  title: {
    type: String,
    default: 'Admin dashboard'
  },
  modelValue: {
    type: String,
    default: ''
  },
  user: {
    type: Object,
    default: null
  }
})

defineEmits(['update:modelValue'])

const router = useRouter()
const viewOpen = ref(false)

const { activeOrg, switchToOrg, switchBackToUser } = useUser()

const userName = computed(() => {
  return props.user?.name || props.user?.nom || 'Admin'
})

const userEmail = computed(() => {
  return props.user?.email || ''
})

const userInitial = computed(() => {
  return userName.value.charAt(0).toUpperCase()
})

function goUserMode() {
  switchBackToUser()
  viewOpen.value = false
  router.push('/app')
}

function switchOrg(org) {
  if (!org) return

  switchToOrg(org)
  viewOpen.value = false

  router.push({
    name: 'orga-dashboard',
    query: { org }
  })
}
</script>

<style scoped>
.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 28px;
  flex-wrap: wrap;
}

.header-left .eyebrow {
  margin: 0 0 6px;
  color: #8b7aa3;
  font-size: 13px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.app-header h1 {
  margin: 0;
  color: #21152d;
  font-size: 28px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
}

.search-wrap {
  position: relative;
}

.search-icon {
  position: absolute;
  left: 13px;
  top: 50%;
  transform: translateY(-50%);
  color: #8b7aa3;
  font-size: 17px;
}

.search-input {
  min-width: 280px;
  padding: 12px 14px 12px 38px;
  border: 1px solid #ddd6e6;
  border-radius: 14px;
  background: white;
  font-size: 14px;
  color: #24192f;
  outline: none;
}

.user-block {
  display: flex;
  align-items: center;
  gap: 12px;
  background: white;
  border: 1px solid #ece7f3;
  border-radius: 16px;
  padding: 10px 14px;
}

.avatar {
  width: 42px;
  height: 42px;
  border-radius: 14px;
  background: #ede9fe;
  color: #5b21b6;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}

.user-block strong {
  display: block;
  color: #251a32;
  font-size: 14px;
}

.user-block p {
  margin: 3px 0 0;
  color: #7a7284;
  font-size: 12px;
}

.view-dropdown {
  position: relative;
}

.view-btn {
  border: 1px solid #e5dff0;
  background: white;
  color: #58486f;
  border-radius: 12px;
  padding: 12px 16px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}

.view-menu {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  background: white;
  border: 1px solid #eee8f5;
  border-radius: 16px;
  box-shadow: 0 12px 32px rgba(25, 15, 35, 0.1);
  list-style: none;
  padding: 8px;
  margin: 0;
  min-width: 220px;
  z-index: 200;
}

.view-menu-item {
  padding: 10px 14px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 500;
  color: #24192f;
  cursor: pointer;
}

.view-menu-item:hover {
  background: #f3eef8;
}

.view-menu-item--active {
  background-color: #2d1840;
  color: white;
}

.view-menu-item--disabled {
  opacity: 0.6;
  cursor: default;
  pointer-events: none;
}

.view-menu-separator {
  height: 1px;
  background: #eee8f5;
  margin: 8px 4px;
}
</style>
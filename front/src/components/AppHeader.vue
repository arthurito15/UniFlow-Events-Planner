<template>
  <div>
    <header class="app-header">
      <div class="header-left">
        <p class="eyebrow">Espace étudiant</p>
        <h1>{{ pageTitle }}</h1>
      </div>

      <div class="header-actions">
        <!-- Barre de recherche -->
        <div class="search-wrap">
          <span class="search-icon">⌕</span>
          <input
              v-model="search"
              type="text"
              class="search-input"
              placeholder="Rechercher un événement…"
          />
        </div>

        <!-- Bloc utilisateur -->
        <div class="user-block">
          <div class="avatar">{{ userInitials }}</div>
          <div>
            <strong>{{ user.name }}</strong>
            <div>
              <p>{{ userRole }}</p>
              <p>{{ user.email }}</p>
            </div>
          </div>
        </div>

        <!-- Dropdown organisations (si organisateur) -->
        <template v-if="isOrga && user?.organisations?.length > 0">
          <div class="orga-dropdown">
            <button class="orga-btn" @click="orgaOpen = !orgaOpen">
              {{ activeOrg ?? 'Mes organisations' }} ▾
            </button>
            <ul v-if="orgaOpen" class="orga-menu">

              <!-- élément par défaut -->
              <li class="orga-menu-item orga-menu-item--disabled">
                Mes organisations
              </li>

              <li
                  v-for="org in user.organisations"
                  :key="org"
                  class="orga-menu-item"
                  :class="{ 'orga-menu-item--active': activeOrg === org }"
                  @click="switchOrg(org)"
              >
                {{ org }}
              </li>

              <li
                  class="orga-menu-item"
                  @click="switchBackToUser"
              >
                ← Mode utilisateur
              </li>

            </ul>
          </div>
        </template>
      </div>
    </header>

    <!-- Sous-nav pôles (spécifique user) -->
    <nav class="poles-nav">
      <span
          v-for="item in menu"
          :key="item.value"
          class="pole-pill"
          :class="{ 'pole-pill--active': selectedPole === item.value }"
          @click="selectPole(item.value)"
      >
        {{ item.label }}
      </span>
    </nav>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUser } from '@/composables/useUser'
import { useRouter, useRoute } from 'vue-router'
import { getPoles } from '@/services/api.js'

const search = ref('')
const orgaOpen = ref(false)
const { user, activeOrg, isOrga, switchToOrg, switchBackToUser } = useUser()
const router = useRouter()
const route = useRoute()

const menu = ref([])

onMounted(async () => {
  try {
    const poles = await getPoles()
    menu.value = poles.map(p => ({ label: p.name, value: p.name }))
  } catch (e) {
    console.error('Erreur chargement pôles', e)
  }
})

const selectedPole = computed(() => route.query.pole ?? null)

function selectPole(pole) {
  // Clic sur le pôle actif = reset vers "Tous"
  if (selectedPole.value === pole) {
    router.push({ name: 'user-dashboard', query: {} })
  } else {
    router.push({ name: 'user-dashboard', query: { pole } })
  }
}

const userInitials = computed(() => {
  if (!user.value?.name) return ''
  return user.value.name.split(' ').map(n => n[0]).join('').toUpperCase()
})

const userRole = computed(() => 'Utilisateur')

const pageTitle = computed(() => {
  if (!selectedPole.value) return 'Tous les événements'
  return menu.value.find(m => m.value === selectedPole.value)?.label ?? 'Événements'
})

function switchOrg(org) {
  if (!org) return

  switchToOrg(org)
  orgaOpen.value = false

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
  pointer-events: none;
}

.search-input {
  min-width: 260px;
  padding: 12px 14px 12px 38px;
  border: 1px solid #ddd6e6;
  border-radius: 14px;
  background: white;
  font-size: 14px;
  color: #24192f;
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.search-input::placeholder { color: #a394b8; }

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

/* Orga dropdown */
.orga-dropdown { position: relative; }

.orga-btn {
  border: 1px solid #e5dff0;
  background: white;
  color: #58486f;
  border-radius: 12px;
  padding: 10px 14px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
}

.orga-menu {
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
  min-width: 180px;
  z-index: 200;
}

.orga-menu-item {
  padding: 9px 14px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 500;
  color: #24192f;
  cursor: pointer;
  transition: background 0.15s;
}

.orga-menu-item--disabled {
  opacity: 0.6;
  cursor: default;
  pointer-events: none;
}

.orga-menu-item:hover { background: #f2eef9; }
.orga-menu-item--active { background-color: #2d1840; color: white; }

/* Sous-nav pôles */
.poles-nav {
  display: flex;
  justify-content: space-evenly;
  padding: 0;
  border-top: 1px solid #eee8f5;
  background: white;
}

.pole-pill {
  border: none;
  border-bottom: 3px solid transparent;
  background: transparent;
  color: #58486f;
  border-radius: 0;
  padding: 14px 24px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
  transition: color 0.2s, background 0.2s;
}

.pole-pill:hover {
  color: #2d1840;
  background: #f7f5fb;
}

.pole-pill--active {
  color: #2d1840;
  border-bottom: 3px solid #2d1840;
}
</style>
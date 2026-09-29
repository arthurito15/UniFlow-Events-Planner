<template>
  <aside class="user-sidebar">

    <div class="brand" @click="goHome">
      <div class="brand-logo">U</div>
      <div>
        <h2>UniFlow</h2>
        <p>Espace Étudiant</p>
      </div>
    </div>

    <!-- Navigation principale -->
    <nav class="nav-links">
      <button
          v-for="item in navItems"
          :key="item.label"
          type="button"
          class="nav-item"
          :class="{ 'nav-item--active': currentRoute === item.route }"
          @click="goTo(item.route)"
      >
        <span class="icon">{{ item.icon }}</span>
        <span>{{ item.label }}</span>
      </button>
    </nav>

    <!-- Événements à venir (max 3) -->
    <div class="sidebar-section">
      <div class="section-header">
        <p class="section-label">Mes événements à venir</p>
        <button class="section-link" @click="goTo('/app/inscriptions')">Voir tout</button>
      </div>

      <div v-if="loadingInscriptions" class="skeleton-list">
        <div class="skeleton-row" v-for="i in 3" :key="i"></div>
      </div>

      <p v-else-if="upcomingEvents.length === 0" class="empty-msg">
        Aucun événement à venir
      </p>

      <ul v-else class="event-list">
        <li
            v-for="event in upcomingEvents.slice(0, 3)"
            :key="event.id"
            class="event-item"
            @click="goToEvent(event.id)"
        >
          <span class="event-dot"></span>
          <div>
            <div class="event-item-title">{{ event.title }}</div>
            <div class="event-item-date">{{ formatDate(event.beginDate) }}</div>
          </div>
        </li>
      </ul>
    </div>

    <!-- Favoris (max 3) -->
    <div class="sidebar-section">
      <div class="section-header">
        <p class="section-label">Mes favoris</p>
        <button class="section-link" @click="goTo('/app/favoris')">Voir tout</button>
      </div>

      <div v-if="loadingFavoris" class="skeleton-list">
        <div class="skeleton-row" v-for="i in 2" :key="i"></div>
      </div>

      <p v-else-if="favoris.length === 0" class="empty-msg">
        Aucun favori pour l'instant
      </p>

      <ul v-else class="event-list">
        <li
            v-for="event in favoris.slice(0, 3)"
            :key="event.id"
            class="event-item"
            @click="goToEvent(event.id)"
        >
          <span class="event-dot event-dot--fav">★</span>
          <div>
            <div class="event-item-title">{{ event.title }}</div>
            <div class="event-item-date">{{ event.organisationName }}</div>
          </div>
        </li>
      </ul>
    </div>

    <!-- Déconnexion épinglé en bas -->
    <div class="sidebar-footer">
      <button class="logout-item" @click="handleLogout">
        <span class="icon">⎋</span>
        <span>Déconnexion</span>
      </button>
    </div>

  </aside>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useUser } from '@/composables/useUser'
import { getFavoris, getInscriptions } from '@/services/api.js'
import { useRouter, useRoute } from 'vue-router'

const { user, logout } = useUser()
const router = useRouter()
const route = useRoute()

const navItems = [
  { label: 'Accueil',          icon: '⌂', route: '/app' },
  { label: 'Mes favoris',      icon: '★', route: '/app/favoris' },
  { label: 'Mes inscriptions', icon: '◍', route: '/app/inscriptions' },
  { label: 'Profil',           icon: '◎', route: '/app/profil' },
]

// Réactif sur le chemin courant (corrige le bug "bloqué à favoris")
const currentRoute = computed(() => route.path)

const favoris = ref([])
const upcomingEvents = ref([])
const loadingFavoris = ref(true)
const loadingInscriptions = ref(true)

function goHome() { router.push('/app') }
function goTo(r) { router.push(r) }
function goToEvent(id) { router.push({ name: 'event-detail', params: { id } }) }

function handleLogout() {
  logout()
  router.push({ name: 'login' })
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('fr-FR', {
    day: 'numeric',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit'
  })
}

async function loadFavoris() {
  if (!user.value) return
  loadingFavoris.value = true
  try {
    favoris.value = await getFavoris(user.value.id)
  } catch (e) {
    console.error(e)
  } finally {
    loadingFavoris.value = false
  }
}

async function loadInscriptions() {
  if (!user.value) return
  loadingInscriptions.value = true
  try {
    const all = await getInscriptions(user.value.id)
    const now = new Date()
    upcomingEvents.value = all
        .filter(e => e.beginDate && new Date(e.beginDate) > now)
        .sort((a, b) => new Date(a.beginDate) - new Date(b.beginDate))
  } catch (e) {
    console.error(e)
  } finally {
    loadingInscriptions.value = false
  }
}

async function loadSidebarData() {
  await Promise.all([loadFavoris(), loadInscriptions()])
}

onMounted(() => {
  loadSidebarData()
  window.addEventListener('uniflow:favoris-updated', loadFavoris)
  window.addEventListener('uniflow:inscriptions-updated', loadInscriptions)
})

onUnmounted(() => {
  window.removeEventListener('uniflow:favoris-updated', loadFavoris)
  window.removeEventListener('uniflow:inscriptions-updated', loadInscriptions)
})
</script>

<style scoped>
.user-sidebar {
  position: sticky;
  top: 0;
  height: 100vh;
  overflow-y: auto;
  width: 270px;
  flex-shrink: 0;
  background: linear-gradient(180deg, #2b1538 0%, #1f1029 100%);
  color: white;
  padding: 24px 18px;
  display: flex;
  flex-direction: column;
  gap: 28px;
}

/* Brand */
.brand {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 10px 8px 18px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  cursor: pointer;
  flex-shrink: 0;
}

.brand-logo {
  width: 46px;
  height: 46px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.14);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 20px;
  flex-shrink: 0;
}

.brand h2 { margin: 0; font-size: 20px; }
.brand p { margin: 4px 0 0; color: rgba(255,255,255,0.68); font-size: 13px; }

/* Navigation */
.nav-links {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.nav-item {
  border: none;
  background: transparent;
  color: rgba(255, 255, 255, 0.82);
  border-radius: 16px;
  padding: 14px 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  text-align: left;
  font-size: 15px;
  cursor: pointer;
  transition: background 0.2s;
  width: 100%;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.08);
}
.nav-item--active { background: white; color: #2d1840; font-weight: 700; }
.icon { font-size: 16px; }

/* Sections */
.sidebar-section {
  border-top: 1px solid rgba(255,255,255,0.08);
  padding-top: 16px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding: 0 8px;
}

.section-label {
  margin: 0;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.09em;
  color: rgba(255, 255, 255, 0.45);
}

.section-link {
  background: none;
  border: none;
  color: rgba(255,255,255,0.35);
  font-size: 11px;
  cursor: pointer;
  padding: 0;
  transition: color 0.2s;
}

.section-link:hover { color: rgba(255,255,255,0.7); }

/* Skeleton */
.skeleton-list { display: flex; flex-direction: column; gap: 8px; padding: 0 8px; }

.skeleton-row {
  height: 38px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.07);
  animation: pulse 1.4s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.empty-msg {
  margin: 0;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.38);
  font-style: italic;
  padding: 0 8px;
}

.event-list {
  list-style: none;
  padding: 0 8px;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.event-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.15s;
  padding: 8px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.event-item:last-child { border-bottom: none; }

.event-item:hover { background: rgba(255,255,255,0.07); }

.event-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #7c3aed;
  flex-shrink: 0;
  margin-top: 5px;
}

.event-dot--fav {
  width: auto; height: auto;
  background: none; border-radius: 0;
  color: #f59e0b;
  font-size: 12px;
  margin-top: 1px;
}

.event-item-title {
  font-size: 13px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.3;
}

.event-item-date {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.45);
  margin-top: 2px;
}

/* Footer déconnexion */
.sidebar-footer {
  margin-top: auto;
  padding-top: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.logout-item {
  width: 100%;
  border: none;
  background: transparent;
  color: rgba(255, 255, 255, 0.55);
  border-radius: 16px;
  padding: 14px 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  text-align: left;
  font-size: 15px;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
}

.logout-item:hover {
  background: rgba(220, 50, 50, 0.15);
  color: #fca5a5;
}
</style>

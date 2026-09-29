<template>
  <aside class="admin-sidebar">
    <!-- Logo / Brand -->
    <div class="brand" @click="goTo('/admin')">
      <div class="brand-logo">U</div>
      <div>
        <h2>UniFlow</h2>
        <p>Espace Administrateur</p>
      </div>
    </div>

    <!-- Navigation -->
    <nav class="nav-links">
      <button
          v-for="item in items"
          :key="item.label"
          class="nav-item"
          :class="{ 'nav-item--active': item.label === active }"
          type="button"
          @click="goTo(item.route)"
      >
        <span class="icon">{{ item.icon }}</span>
        <span>{{ item.label }}</span>
      </button>
    </nav>

    <!-- Déconnexion — épinglé en bas -->
    <div class="sidebar-footer">
      <button class="logout-item" @click="handleLogout">
        <span class="icon">⎋</span>
        <span>Déconnexion</span>
      </button>
    </div>
  </aside>
</template>

<script setup>
import { useRouter } from 'vue-router'
import {useUser} from "@/composables/useUser.js";
const { logout } = useUser()

defineProps({
  items: {
    type: Array,
    default: () => []
  },
  active: {
    type: String,
    default: ''
  }
})

const router = useRouter()

const goTo = (route) => {
  if (route) {
    router.push(route)
  }
}

function handleLogout() {
  logout()
  router.push({ name: 'home' })
}
</script>

<style scoped>
.admin-sidebar {
  position: sticky;
  top: 0;

  height: 100vh;
  overflow-y: auto;

  width: 270px;

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
}

.brand h2 {
  margin: 0;
  font-size: 20px;
}

.brand p {
  margin: 4px 0 0;
  color: rgba(255, 255, 255, 0.68);
  font-size: 13px;
}

/* Navigation */
.nav-links {
  display: flex;
  flex-direction: column;
  gap: 10px;
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
  transition: 0.2s ease;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.08);
}

.nav-item--active {
  background: white;
  color: #2d1840;
  font-weight: 700;
}

.icon {
  font-size: 16px;
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
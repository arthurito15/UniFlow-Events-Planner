<template>
  <div class="dashboard-shell">

    <!-- Sidebar dark — même structure que l'admin -->
    <SideBar />

    <!-- Zone de contenu principale -->
    <div class="dashboard-content">

      <!-- Header (titre + search + user + nav pôles) -->
      <AppHeader />

      <!-- Page courante -->
      <main class="dashboard-main">
        <router-view />
      </main>

    </div>
  </div>
</template>

<script setup>
import AppHeader from '@/components/AppHeader.vue'
import SideBar from '@/components/SideBar.vue'
</script>

<style scoped>
.dashboard-shell {
  min-height: 100vh;
  display: flex;
  background: #f7f5fb;
}

.dashboard-content {
  flex: 1;
  padding: 28px;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.dashboard-main {
  flex: 1;
  min-width: 0;
  width: 100%;
}
</style>
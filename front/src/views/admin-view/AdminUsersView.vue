<template>
  <div class="dashboard-shell">
    <AdminSidebar :items="sidebarItems" active="Utilisateurs" />

    <div class="dashboard-content">
      <AdminHeader
          v-model="search"
          title="Gestion des utilisateurs"
          :user="user"
      />

      <main class="dashboard-main">
        <section class="section-block">
          <div class="section-head">
            <div>
              <p class="eyebrow">Administration</p>
              <h2>Utilisateurs</h2>
              <p class="section-text">
                Consulte tous les comptes de la plateforme : administrateurs,
                organisateurs et utilisateurs.
              </p>
            </div>
          </div>

          <div class="toolbar">
            <input
                v-model="search"
                type="text"
                class="search-input"
                placeholder="Rechercher par nom ou email..."
            />

            <select v-model="selectedStatus" class="filter-select">
              <option value="Tous">Tous les statuts</option>
              <option value="Admin">Admin</option>
              <option value="Organisateur">Organisateur</option>
              <option value="Utilisateur">Utilisateur</option>
            </select>
          </div>

          <div class="users-table-card">
            <div class="users-table users-table--head">
              <div>Nom complet</div>
              <div>Statuts</div>
              <div>E-mail</div>
              <div>Organisation</div>
              <div>Date d’inscription</div>
              <div>Actions</div>
            </div>

            <template v-for="userItem in filteredUsers" :key="userItem.id">
              <div class="users-table users-table--row">
                <div class="cell-name">
                  <div class="avatar">
                    {{ userItem.name.charAt(0).toUpperCase() }}
                  </div>
                  <div>
                    <strong>{{ userItem.name }}</strong>
                  </div>
                </div>

                <div class="roles-cell">
                  <span
                      v-for="role in userItem.roles"
                      :key="role"
                      class="status-badge"
                      :class="{
                      'status-badge--admin': role === 'Admin',
                      'status-badge--organizer': role === 'Organisateur',
                      'status-badge--user': role === 'Utilisateur'
                    }"
                  >
                    {{ role }}
                  </span>
                </div>

                <div class="email-cell">{{ userItem.email }}</div>

                <div>
                  <span v-if="userItem.roles.includes('Organisateur')">
                    {{ displayOrganizations(userItem) }}
                  </span>
                  <span v-else class="empty-text">—</span>
                </div>

                <div>{{ userItem.registrationDate }}</div>

                <div class="actions-cell">
                  <button class="ghost-btn" @click="handleEditUser(userItem)">
                    Modifier
                  </button>
                  <button
                      class="ghost-btn ghost-btn--danger"
                      @click="handleDeleteUser(userItem)"
                  >
                    Supprimer
                  </button>
                </div>
              </div>

              <div v-if="editingUserId === userItem.id" class="edit-user-card">
                <div class="edit-user-head">
                  <div>
                    <h3>Modifier {{ userItem.name }}</h3>
                    <p>
                      Ajoute ou retire le rôle organisateur pour cet utilisateur.
                    </p>
                  </div>

                  <button class="secondary-btn" type="button" @click="closeEditUser">
                    Annuler
                  </button>
                </div>

                <div class="edit-user-content">
                  <div class="form-group">
                    <label>Organisations</label>

                    <div class="organization-checklist">
                      <label
                          v-for="organization in organizationOptions"
                          :key="organization"
                          class="organization-option"
                      >
                        <input
                            v-model="editForm.organizations"
                            type="checkbox"
                            :value="organization"
                        />
                        <span>{{ organization }}</span>
                      </label>
                    </div>
                  </div>

                  <div class="edit-actions">
                    <button
                        class="primary-btn"
                        type="button"
                        @click="saveOrganization(userItem)"
                    >
                      Enregistrer
                    </button>

                    <button
                        v-if="userItem.roles.includes('Organisateur')"
                        class="danger-btn"
                        type="button"
                        @click="removeOrganization(userItem)"
                    >
                      Retirer les organisations
                    </button>
                  </div>
                </div>
              </div>
            </template>

            <div v-if="filteredUsers.length === 0" class="empty-state">
              Aucun utilisateur trouvé.
            </div>
          </div>
        </section>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, reactive, onMounted } from 'vue'
import AdminHeader from '../../components/admin/AdminHeader.vue'
import AdminSidebar from '../../components/admin/AdminSidebar.vue'
import { useUser } from '../../composables/useUser.js'
import {
  deleteUser as deleteUserApi,
  getOrganizers,
  getUsers,
  updateUserOrganization
} from '@/services/api.js'

const { user } = useUser()

const search = ref('')
const selectedStatus = ref('Tous')
const editingUserId = ref(null)
const users = ref([])
const organizers = ref([])
const error = ref('')

const editForm = reactive({
  organizations: []
})

const sidebarItems = [
  { label: 'Pôles', icon: '◫', route: '/admin/poles' },
  { label: 'Organisations', icon: '◉', route: '/admin/organizers' },
  { label: 'Événements', icon: '◌', route: '/admin/events' },
  { label: 'Utilisateurs', icon: '◍', route: '/admin/users' }
]

const organizationOptions = computed(() => {
  return organizers.value.map((organizer) => organizer.name)
})

const filteredUsers = computed(() => {
  return users.value.filter((item) => {
    const matchesSearch =
        (item.name ?? '').toLowerCase().includes(search.value.toLowerCase()) ||
        (item.email ?? '').toLowerCase().includes(search.value.toLowerCase())

    const matchesStatus =
        selectedStatus.value === 'Tous' ||
        item.roles.includes(selectedStatus.value)

    return matchesSearch && matchesStatus
  })
})

async function loadAdminData() {
  try {
    error.value = ''
    const [loadedUsers, loadedOrganizers] = await Promise.all([
      getUsers(),
      getOrganizers()
    ])
    users.value = loadedUsers ?? []
    organizers.value = loadedOrganizers ?? []
  } catch (e) {
    console.error(e)
    error.value = 'Impossible de charger les utilisateurs.'
  }
}

onMounted(() => {
  loadAdminData()
})

const handleEditUser = (userItem) => {
  editingUserId.value = userItem.id
  editForm.organizations = userOrganizations(userItem)
}

const closeEditUser = () => {
  editingUserId.value = null
  editForm.organizations = []
}

const saveOrganization = async (userItem) => {
  try {
    const updatedUser = await updateUserOrganization(userItem.id, editForm.organizations)
    Object.assign(userItem, updatedUser)
    closeEditUser()
  } catch (e) {
    console.error(e)
    error.value = "Impossible de modifier l'organisation de l'utilisateur."
  }
}

const removeOrganization = async (userItem) => {
  try {
    const updatedUser = await updateUserOrganization(userItem.id, [])
    Object.assign(userItem, updatedUser)
    closeEditUser()
  } catch (e) {
    console.error(e)
    error.value = "Impossible de retirer l'organisation de l'utilisateur."
  }
}

const handleDeleteUser = (userItem) => {
  deleteUserApi(userItem.id)
      .then(() => {
        users.value = users.value.filter((item) => item.id !== userItem.id)
      })
      .catch((e) => {
        console.error(e)
        error.value = "Impossible de supprimer l'utilisateur."
      })
}

function userOrganizations(userItem) {
  if (Array.isArray(userItem.organizations)) {
    return [...userItem.organizations]
  }
  return userItem.organization ? [userItem.organization] : []
}

function displayOrganizations(userItem) {
  const organizations = userOrganizations(userItem)
  return organizations.length > 0 ? organizations.join(', ') : '—'
}
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
}

.dashboard-main {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.section-block {
  background: #ffffff;
  border: 1px solid #eee8f5;
  border-radius: 24px;
  padding: 24px;
  box-shadow: 0 18px 40px rgba(25, 15, 35, 0.04);
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.eyebrow {
  margin: 0 0 6px;
  color: #8b7aa3;
  font-size: 13px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.section-head h2 {
  margin: 0;
  color: #24192f;
  font-size: 24px;
}

.section-text {
  margin: 8px 0 0;
  color: #756d80;
  max-width: 700px;
  line-height: 1.5;
}

.toolbar {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 18px;
}

.search-input,
.filter-select {
  border: 1px solid #ddd6e6;
  border-radius: 14px;
  padding: 12px 14px;
  background: white;
  outline: none;
}

.search-input {
  flex: 1;
  min-width: 240px;
}

.filter-select {
  min-width: 220px;
}

.users-table-card {
  border: 1px solid #eee8f5;
  border-radius: 20px;
  overflow: hidden;
  background: white;
}

.users-table {
  display: grid;
  grid-template-columns: 1.4fr 1.1fr 1.5fr 1.2fr 1.1fr 1.2fr;
  gap: 12px;
  align-items: center;
  padding: 16px 18px;
}

.users-table--head {
  background: #faf8fd;
  color: #6d5f7f;
  font-weight: 700;
  font-size: 14px;
  border-bottom: 1px solid #eee8f5;
}

.users-table--row {
  border-bottom: 1px solid #f1ecf7;
}

.cell-name {
  display: flex;
  align-items: center;
  gap: 12px;
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

.roles-cell {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  padding: 7px 12px;
  font-size: 13px;
  font-weight: 700;
}

.status-badge--admin {
  background: #efe3ff;
  color: #6d28d9;
}

.status-badge--organizer {
  background: #e0f2fe;
  color: #0369a1;
}

.status-badge--user {
  background: #ecfdf3;
  color: #027a48;
}

.email-cell {
  word-break: break-word;
}

.empty-text {
  color: #9a90a8;
}

.actions-cell {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.ghost-btn {
  border: 1px solid #e7e0f1;
  background: white;
  color: #5c4b74;
  border-radius: 12px;
  padding: 9px 12px;
}

.ghost-btn--danger {
  color: #b42318;
}

.empty-state {
  padding: 24px;
  text-align: center;
  color: #7a7284;
  background: #fbf9fd;
}

.edit-user-card {
  background: #faf8fd;
  border-bottom: 1px solid #eee8f5;
  padding: 20px;
}

.edit-user-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
  flex-wrap: wrap;
}

.edit-user-head h3 {
  margin: 0;
  color: #24192f;
}

.edit-user-head p {
  margin: 6px 0 0;
  color: #756d80;
}

.edit-user-content {
  display: flex;
  align-items: flex-end;
  gap: 14px;
  flex-wrap: wrap;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 280px;
  flex: 1;
}

.form-group label {
  font-weight: 600;
  color: #2f243b;
}

.form-group input:not([type='checkbox']),
.form-group select {
  border: 1px solid #ddd6e6;
  border-radius: 14px;
  padding: 12px 14px;
  background: white;
  outline: none;
}

.organization-checklist {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 8px;
  max-height: 180px;
  overflow: auto;
  border: 1px solid #ddd6e6;
  border-radius: 14px;
  padding: 10px;
  background: white;
}

.organization-option {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  border-radius: 10px;
  color: #2f243b;
  font-weight: 500;
}

.organization-option:hover {
  background: #faf8fd;
}

.organization-option input {
  width: 16px;
  height: 16px;
  accent-color: #7c3aed;
}

.edit-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.secondary-btn {
  border: 1px solid #ddd6e6;
  background: white;
  color: #5c4b74;
  border-radius: 14px;
  padding: 12px 18px;
  font-weight: 600;
}

.primary-btn {
  border: none;
  background: #7c3aed;
  color: white;
  border-radius: 14px;
  padding: 12px 18px;
  font-weight: 600;
}

.danger-btn {
  border: 1px solid #f3b4b4;
  background: #fff5f5;
  color: #b42318;
  border-radius: 14px;
  padding: 12px 18px;
  font-weight: 600;
}

@media (max-width: 768px) {
  .dashboard-content {
    padding: 16px;
  }

  .users-table {
    grid-template-columns: 1fr;
    gap: 10px;
  }

  .users-table--head {
    display: none;
  }

  .users-table--row {
    padding: 18px;
  }
}
</style>

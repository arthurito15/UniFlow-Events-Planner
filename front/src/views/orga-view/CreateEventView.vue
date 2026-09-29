<template>
        <div class="page-header">
          <div></div>
          <div class="header-actions">
            <button class="primary-btn" @click="submitEvent">Publier</button>
            <button class="danger-btn" @click="resetForm">Supprimer</button>
          </div>
        </div>

        <div class="form-grid">
          <!-- Left -->
          <div class="form-card">
            <div class="form-group">
              <label>Nom de l'événement :</label>
              <input v-model="form.title" type="text" placeholder="Nom de l'événement" />
            </div>

            <div class="field-row">
              <div class="form-group">
                <label>Date de début :</label>
                <input v-model="form.beginDate" type="date" />
              </div>
              <div class="form-group">
                <label>Durée (nb jours) :</label>
                <input v-model="form.duration" type="number" min="1" placeholder="1" />
              </div>
            </div>

            <div class="form-group">
              <label>Mode :</label>
              <div class="checkbox-group">
                <label class="checkbox-label">
                  <input type="checkbox" v-model="form.remote" /> En distanciel
                </label>
                <label class="checkbox-label">
                  <input type="checkbox" v-model="form.onsite" /> En présentiel
                </label>
              </div>
            </div>

            <div class="form-group">
              <label>Lieu :</label>
              <div class="input-icon">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
                <input v-model="form.address" type="text" placeholder="Adresse" />
              </div>
            </div>

            <div class="form-group">
              <label>Nombre max de participants :</label>
              <div class="capacity-row">
                <input v-model="form.capacity" type="number" min="1" placeholder="50" :disabled="form.unlimited" />
                <label class="checkbox-label">
                  <input type="checkbox" v-model="form.unlimited" /> Sans limite
                </label>
              </div>
            </div>

            <div class="form-group">
              <label>Organisateur :</label>
              <div class="organizer-field">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
                <em>{{ currentOrga.name }}</em>
              </div>
            </div>

            <div class="form-group">
              <label>Description détaillée :</label>
              <textarea v-model="form.description" rows="5" placeholder="Décrivez votre événement..."></textarea>
            </div>
          </div>

          <!-- Right -->
          <div class="right-col">
            <div class="form-card upload-card">
              <label class="upload-label">Téléverser l'affiche de votre événement</label>
              <div class="upload-zone" @click="posterInput.click()" @dragover.prevent @drop.prevent="handleDrop($event, 'poster')">
                <template v-if="!posterPreview">
                  <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="#9b8fb0" stroke-width="1.5"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
                  <span>Parcourir vos fichiers</span>
                </template>
                <img v-else :src="posterPreview" class="preview-img" />
              </div>
              <input ref="posterInput" type="file" accept="image/*" style="display:none" @change="handlePosterChange" />
            </div>

            <div class="form-card upload-card">
              <label class="upload-label">Ajouter plus de photos des éditions précédentes !</label>
              <div class="photos-grid" v-if="photos.length > 0">
                <div v-for="(photo, i) in photos" :key="i" class="photo-thumb">
                  <img :src="photo" />
                </div>
              </div>
              <div class="upload-zone small" @click="photosInput.click()" @dragover.prevent @drop.prevent="handleDrop($event, 'photos')">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#9b8fb0" stroke-width="1.5"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
                <span>Parcourir vos fichiers</span>
              </div>
              <input ref="photosInput" type="file" accept="image/*" multiple style="display:none" @change="handlePhotosChange" />
            </div>
          </div>
        </div>

        <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>
        <p v-if="successMsg" class="success-msg">{{ successMsg }}</p>

</template>

<script setup>
import { onMounted, ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useCurrentOrganisation } from '@/composables/useCurrentOrganisation.js'
import { createEvent } from '@/services/api.js'

const router = useRouter()
const { currentOrga, loadCurrentOrganisation } = useCurrentOrganisation()

const posterInput = ref(null)
const photosInput = ref(null)
const posterPreview = ref(null)
const photos = ref([])
const errorMsg = ref('')
const successMsg = ref('')

const form = reactive({
  title: '',
  beginDate: '',
  duration: 1,
  remote: false,
  onsite: true,
  address: '',
  capacity: '',
  unlimited: false,
  description: '',
})

const handlePosterChange = (e) => {
  const file = e.target.files[0]
  if (file) posterPreview.value = URL.createObjectURL(file)
}

const handlePhotosChange = (e) => {
  for (const file of e.target.files) photos.value.push(URL.createObjectURL(file))
}

const handleDrop = (e, type) => {
  const files = e.dataTransfer.files
  if (type === 'poster' && files[0]) posterPreview.value = URL.createObjectURL(files[0])
  else if (type === 'photos') for (const f of files) photos.value.push(URL.createObjectURL(f))
}

const resetForm = () => {
  Object.assign(form, { title: '', beginDate: '', duration: 1, remote: false, onsite: true, address: '', capacity: '', unlimited: false, description: '' })
  posterPreview.value = null
  photos.value = []
  errorMsg.value = ''
  successMsg.value = ''
}

function addDays(date, days) {
  const result = new Date(date)
  result.setDate(result.getDate() + days)
  return result
}

function toLocalDateTime(date, time) {
  return `${date.toISOString().slice(0, 10)}T${time}`
}

function buildEventPayload() {
  const beginDate = new Date(`${form.beginDate}T00:00:00`)
  const duration = Number(form.duration) || 1
  const endDate = addDays(beginDate, Math.max(duration - 1, 0))
  const capacity = form.unlimited ? null : Number(form.capacity) || null

  const payload = {
    title: form.title,
    description: form.description,
    address: form.remote && !form.onsite ? 'Distanciel' : form.address,
    beginDate: toLocalDateTime(beginDate, '09:00:00'),
    endDate: toLocalDateTime(endDate, '18:00:00'),
    capacity,
    status: 'PUBLISHED',
    price: 0,
    organisation: { id: currentOrga.value.id }
  }

  if (currentOrga.value.poleId) {
    payload.pole = { id: currentOrga.value.poleId }
  }

  return payload
}

const submitEvent = async () => {
  errorMsg.value = ''

  if (!form.title || !form.beginDate) {
    errorMsg.value = 'Le nom et la date de début sont obligatoires.'
    return
  }

  if (!currentOrga.value.id) {
    errorMsg.value = "Impossible de retrouver l'organisation courante."
    return
  }

  try {
    await createEvent(buildEventPayload())
    successMsg.value = 'Événement publié avec succès !'
    setTimeout(() => router.push('/orga'), 1200)
  } catch (e) {
    console.error(e)
    errorMsg.value = "Impossible de créer l'événement."
  }
}

onMounted(() => {
  loadCurrentOrganisation()
})
</script>

<style scoped>

.page-header { display: flex; justify-content: space-between; align-items: flex-start; padding-bottom: 20px}

.header-actions { display: flex; gap: 10px; }

.primary-btn {
  background: #7c3aed; color: white; border: none; border-radius: 14px;
  padding: 12px 24px; font-size: 14px; font-weight: 600; cursor: pointer;
  box-shadow: 0 10px 20px rgba(124,58,237,0.18); transition: background 0.15s;
}
.primary-btn:hover { background: #6d28d9; }

.danger-btn {
  background: #dc2626; color: white; border: none; border-radius: 14px;
  padding: 12px 24px; font-size: 14px; font-weight: 600; cursor: pointer; transition: background 0.15s;
}
.danger-btn:hover { background: #b91c1c; }

.form-grid { display: grid; grid-template-columns: 1fr 360px; gap: 20px; align-items: start; }
.right-col { display: flex; flex-direction: column; gap: 20px; }

.form-card {
  background: white; border: 1px solid #ebe5f3; border-radius: 20px;
  padding: 28px; box-shadow: 0 4px 16px rgba(30,16,48,0.05);
  display: flex; flex-direction: column; gap: 20px;
}

.form-group { display: flex; flex-direction: column; gap: 8px; }
.form-group label { font-size: 13px; font-weight: 600; color: #3d2f58; }

.form-group input[type="text"],
.form-group input[type="number"],
.form-group input[type="date"],
.form-group textarea {
  border: 1px solid #ddd6e6; border-radius: 12px; padding: 10px 14px;
  font-size: 14px; color: #24192f; outline: none; font-family: inherit; transition: border-color 0.15s;
}
.form-group input:focus, .form-group textarea:focus { border-color: #7c3aed; }
.form-group textarea { resize: vertical; }

.field-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }

.checkbox-group { display: flex; gap: 20px; }
.checkbox-label { display: flex; align-items: center; gap: 8px; font-size: 14px; color: #3d2f58; cursor: pointer; }

.input-icon { display: flex; align-items: center; gap: 10px; border: 1px solid #ddd6e6; border-radius: 12px; padding: 10px 14px; color: #9b8fb0; }
.input-icon input { border: none !important; padding: 0 !important; border-radius: 0 !important; outline: none; flex: 1; font-size: 14px; color: #24192f; }

.capacity-row { display: flex; align-items: center; gap: 16px; }
.capacity-row input { width: 100px; border: 1px solid #ddd6e6; border-radius: 12px; padding: 10px 14px; font-size: 14px; outline: none; }

.organizer-field {
  display: flex; align-items: center; gap: 10px; border: 1px solid #ddd6e6;
  border-radius: 12px; padding: 10px 14px; font-size: 14px; font-weight: 600;
  font-style: italic; color: #5b21b6; background: #faf8ff;
}

.upload-card { gap: 14px; }
.upload-label { font-size: 13px; font-weight: 600; color: #3d2f58; }

.upload-zone {
  border: 2px dashed #ddd6e6; border-radius: 14px; padding: 32px 20px;
  display: flex; flex-direction: column; align-items: center; gap: 10px;
  cursor: pointer; transition: border-color 0.15s; position: relative; overflow: hidden;
}
.upload-zone:hover { border-color: #7c3aed; }
.upload-zone span { font-size: 13px; color: #9b8fb0; }
.upload-zone.small { padding: 18px; }

.preview-img { position: absolute; inset: 0; width: 100%; height: 100%; object-fit: cover; border-radius: 12px; }

.photos-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; }
.photo-thumb { aspect-ratio: 1; border-radius: 10px; overflow: hidden; background: #f0ebfa; }
.photo-thumb img { width: 100%; height: 100%; object-fit: cover; }

.error-msg { color: #b42318; background: #fff1f0; border: 1px solid #fecdca; border-radius: 10px; padding: 10px 16px; font-size: 14px; }
.success-msg { color: #027a48; background: #ecfdf3; border: 1px solid #a9efcb; border-radius: 10px; padding: 10px 16px; font-size: 14px; }
</style>

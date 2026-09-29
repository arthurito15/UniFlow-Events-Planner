import { ref } from 'vue'
import { useUser } from '@/composables/useUser'
import { getOrganizers } from '@/services/api'

export function useCurrentOrganisation() {
  const { user, activeOrg } = useUser()

  const currentOrga = ref({
    id: null,
    name: activeOrg.value || user.value?.organisations?.[0] || 'Organisation',
    email: user.value?.email || '',
    pole: '',
    poleId: null
  })
  const organisations = ref([])
  const loadingOrganisation = ref(false)
  const organisationError = ref('')

  async function loadCurrentOrganisation() {
    loadingOrganisation.value = true
    organisationError.value = ''

    try {
      const allOrganisations = await getOrganizers()
      organisations.value = allOrganisations ?? []

      const preferredName = activeOrg.value || user.value?.organisations?.[0]
      const matchingOrganisation = organisations.value.find((organisation) => {
        return organisation.name === preferredName
      })

      currentOrga.value = matchingOrganisation ?? {
        ...currentOrga.value,
        name: preferredName || currentOrga.value.name,
        email: user.value?.email || currentOrga.value.email
      }
    } catch (error) {
      console.error(error)
      organisationError.value = "Impossible de charger l'organisation courante."
    } finally {
      loadingOrganisation.value = false
    }

    return currentOrga.value
  }

  return {
    currentOrga,
    organisations,
    loadingOrganisation,
    organisationError,
    loadCurrentOrganisation
  }
}

import { computed, ref } from 'vue'
import {
    addFavori,
    addInscription,
    getAllEvents,
    getEventById,
    getEventsByPole,
    getFavoris,
    getInscriptions,
    removeFavori,
    removeInscription
} from '@/services/api.js'
import { useUser } from '@/composables/useUser'

export function useEvent() {
    const { user } = useUser()

    // LISTE (dashboard)
    const events = ref([])

    // DETAIL
    const event = ref(null)
    const favorite = ref(false)

    const loading = ref(false)
    const errorMessage = ref('')

    const isRegistering = ref(false)
    const isRegistered = ref(false)

    const hasEvent = computed(() => !!event.value)

    // =========================
    // LOAD LIST (dashboard)
    // =========================
    const loadEvents = async () => {
        loading.value = true
        errorMessage.value = ''

        try {
            events.value = await withUserStates(await getAllEvents())
        } catch (e) {
            errorMessage.value = e.message
            events.value = []
        } finally {
            loading.value = false
        }
    }

    const loadEventByPole = async (pole) => {
        loading.value = true
        errorMessage.value = ''

        try {
            events.value = await withUserStates(await getEventsByPole(pole))
        } catch (e) {
            errorMessage.value = e.message
            events.value = []
        } finally {
            loading.value = false
        }
    }

    // =========================
    // LOAD ONE (detail)
    // =========================
    const loadEvent = async (id) => {
        loading.value = true
        errorMessage.value = ''
        isRegistered.value = false

        try {
            const data = await getEventById(id)
            const [favoriteIds, inscriptionIds] = await loadUserEventIds()

            event.value = data
            favorite.value = favoriteIds.has(Number(data.id))
            isRegistered.value = inscriptionIds.has(Number(data.id))
            event.value.favorite = favorite.value
        } catch (e) {
            errorMessage.value = e.message
            event.value = null
            favorite.value = false
        } finally {
            loading.value = false
        }
    }

    // =========================
    // ACTIONS
    // =========================
    const toggleFavorite = async () => {
        if (!event.value || !user.value) return

        const nextValue = !favorite.value
        const previousValue = favorite.value
        favorite.value = nextValue
        event.value.favorite = nextValue

        try {
            if (nextValue) {
                await addFavori(user.value.id, event.value.id)
            } else {
                await removeFavori(user.value.id, event.value.id)
            }

            updateEventInList(event.value.id, { favorite: nextValue })
            notifyFavorisUpdated()
        } catch (e) {
            favorite.value = previousValue
            event.value.favorite = previousValue
            errorMessage.value = e.message
        }
    }

    const register = async () => {
        if (!event.value || !user.value || isRegistering.value) return

        isRegistering.value = true
        const previousValue = isRegistered.value
        isRegistered.value = !previousValue

        try {
            if (previousValue) {
                await removeInscription(user.value.id, event.value.id)
            } else {
                await addInscription(user.value.id, event.value.id)
            }
            // Notifie uniquement la liste des inscriptions dans la sidebar
            notifyInscriptionsUpdated()
        } catch (e) {
            isRegistered.value = previousValue
            errorMessage.value = e.message
        } finally {
            isRegistering.value = false
        }
    }

    // =========================
    // HELPERS
    // =========================
    async function loadUserEventIds() {
        if (!user.value) {
            return [new Set(), new Set()]
        }

        const [favoris, inscriptions] = await Promise.all([
            getFavoris(user.value.id),
            getInscriptions(user.value.id)
        ])

        return [
            new Set(favoris.map((item) => Number(item.id))),
            new Set(inscriptions.map((item) => Number(item.id)))
        ]
    }

    async function withUserStates(loadedEvents) {
        const [favoriteIds, inscriptionIds] = await loadUserEventIds()
        return loadedEvents.map((item) => ({
            ...item,
            favorite: favoriteIds.has(Number(item.id)),
            registered: inscriptionIds.has(Number(item.id))
        }))
    }

    function updateEventInList(eventId, changes) {
        const index = events.value.findIndex((item) => Number(item.id) === Number(eventId))
        if (index !== -1) {
            events.value[index] = {
                ...events.value[index],
                ...changes
            }
        }
    }

    function notifyInscriptionsUpdated() {
        window.dispatchEvent(new CustomEvent('uniflow:inscriptions-updated'))
    }

    function notifyFavorisUpdated() {
        window.dispatchEvent(new CustomEvent('uniflow:favoris-updated'))
    }

    return {
        // LIST
        events,
        loadEventByPole,
        loadEventsByPole: loadEventByPole,
        loadEvents,

        // DETAIL
        event,
        favorite,
        hasEvent,
        loadEvent,

        // UI
        loading,
        errorMessage,
        isRegistering,
        isRegistered,

        // ACTIONS
        toggleFavorite,
        register
    }
}

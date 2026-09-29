/**
 * URL du backend. (par défaut Spring Boot sur 8080)
 */
const API_URL = import.meta.env.VITE_API_URL || "http://localhost:8080/api";

// Auth helpers

function getToken() {
    const saved = localStorage.getItem("user")
    if (!saved) return null
    return JSON.parse(saved).token ?? null
}

function authHeaders() {
    const token = getToken()
    const headers = { "Content-Type": "application/json" }
    if (token) headers["Authorization"] = `Bearer ${token}`
    return headers
}

function clearSessionAndRedirectToLogin() {
    localStorage.removeItem("user")
    localStorage.removeItem("mode")
    localStorage.removeItem("activeOrg")

    if (window.location.pathname !== "/login") {
        window.location.href = "/login"
    }
}

// Fetch central

async function apiFetch(path, options = {}) {
    const url = path.startsWith("http") ? path : `${API_URL}${path}`

    const res = await fetch(url, {
        ...options,
        headers: {
            ...authHeaders(),
            ...options.headers   // permet de surcharger si besoin
        }
    })

    // Token expiré ou invalide → on vide la session et on redirige
    if (res.status === 401) {
        clearSessionAndRedirectToLogin()
        throw new Error("Session expiree")
    }

    if (!res.ok) {
        const message = await res.text()
        throw new Error(message || `Erreur ${res.status}`)
    }

    // 204 No Content → pas de JSON
    if (res.status === 204) return null

    return res.json()
}

// Formatage

function formatDateTime(value, options) {
    if (!value) return ""

    const date = new Date(value)
    if (Number.isNaN(date.getTime())) return ""

    return new Intl.DateTimeFormat("fr-FR", options).format(date)
}

function normalizeEvent(event) {
    return {
        ...event,
        date: formatDateTime(event.beginDate, { day: "2-digit", month: "long", year: "numeric" }),
        organizer: event.organisationName || "Organisateur inconnu",
        location: event.address,
        startTime: formatDateTime(event.beginDate, { hour: "2-digit", minute: "2-digit" }),
        endTime:   formatDateTime(event.endDate,   { hour: "2-digit", minute: "2-digit" }),
        tags: event.poleName ? [event.poleName] : [],
        favorite: Boolean(event.favorite)
    }
}

// Auth (pas de token nécessaire)

export async function login(email, password) {
    return apiFetch("/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },  // pas de token ici
        body: JSON.stringify({ email, password })
    })
}

export async function register(userData) {
    return apiFetch("/auth/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(userData)
    })
}

export async function validateSession() {
    await apiFetch("/auth/me")
    return true
}

// Événements

export async function getAllEvents() {
    const events = await apiFetch("/events/all")
    return (events ?? []).map(normalizeEvent)
}

export async function getEventsByPole(pole) {
    const events = await apiFetch(`/events?pole=${encodeURIComponent(pole)}`)
    return (events ?? []).map(normalizeEvent)
}

export async function getEventById(id) {
    const event = await apiFetch(`/events/${id}`)
    return normalizeEvent(event)
}

// Pôles

export async function getPoles() {
    return apiFetch("/poles/all")
}

export async function createPole(pole) {
    return apiFetch("/poles/create", {
        method: "POST",
        body: JSON.stringify(pole)
    })
}

export async function updatePole(id, pole) {
    return apiFetch(`/poles/${id}`, {
        method: "PUT",
        body: JSON.stringify(pole)
    })
}

export async function deletePole(id) {
    return apiFetch(`/poles/${id}`, { method: "DELETE" })
}

// Favoris

export async function getFavoris(userId) {
    const events = await apiFetch(`/events/favori/${userId}`)
    return (events ?? []).map(normalizeEvent)
}

export async function addFavori(userId, eventId) {
    return apiFetch(`/events/favori/${userId}/${eventId}`, { method: "POST" })
}

export async function removeFavori(userId, eventId) {
    return apiFetch(`/events/favori/${userId}/${eventId}`, { method: "DELETE" })
}

// Inscriptions

export async function getInscriptions(userId) {
    const events = await apiFetch(`/inscriptions/${userId}`)
    return (events ?? []).map(normalizeEvent)
}

export async function addInscription(userId, eventId) {
    return apiFetch(`/inscriptions/${userId}/${eventId}`, { method: "POST" })
}

export async function removeInscription(userId, eventId) {
    return apiFetch(`/inscriptions/${userId}/${eventId}`, { method: "DELETE" })
}

// Organisateurs (admin)

export async function getOrganizers() {
    return apiFetch("/organizers/all")
}

export async function createOrganizer(data) {
    return apiFetch("/organizers/create", {
        method: "POST",
        body: JSON.stringify(data)
    })
}

export async function updateOrganizer(id, data) {
    return apiFetch(`/organizers/${id}`, {
        method: "PUT",
        body: JSON.stringify(data)
    })
}

export async function deleteOrganizer(id) {
    return apiFetch(`/organizers/${id}`, { method: "DELETE" })
}

// Utilisateurs (admin)

export async function getUsers() {
    return apiFetch("/users/all")
}

export async function deleteUser(id) {
    return apiFetch(`/users/${id}`, { method: "DELETE" })
}

export async function updateUserOrganization(id, organization) {
    const body = Array.isArray(organization)
        ? { organizations: organization }
        : { organization }

    return apiFetch(`/users/${id}/organization`, {
        method: "PUT",
        body: JSON.stringify(body)
    })
}

// Événements orga

export async function getEventsByOrga(orgId) {
    const events = await apiFetch(`/events/orga/${orgId}`)
    return (events ?? []).map(normalizeEvent)
}

export async function createEvent(data) {
    return apiFetch("/events/create", {
        method: "POST",
        body: JSON.stringify(data)
    })
}

export async function updateEvent(id, data) {
    return apiFetch(`/events/${id}`, {
        method: "PUT",
        body: JSON.stringify(data)
    })
}

export async function deleteEvent(id) {
    return apiFetch(`/events/${id}`, { method: "DELETE" })
}

export async function getParticipants(eventId) {
    return await apiFetch(`/inscriptions/event/${eventId}`) ?? []
}

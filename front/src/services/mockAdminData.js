export const poles = [
    {
        id: 1,
        name: 'Bibliothèque',
        icon: '📚',
        description: 'Gestion des événements liés à la bibliothèque universitaire.',
        adminName: 'Sarah Martin',
        adminEmail: 'bibliotheque@uniflow.fr',
        theme: 'violet'
    },
    {
        id: 2,
        name: 'Associations',
        icon: '👥',
        description: 'Pilotage des activités associatives étudiantes.',
        adminName: 'Amine Kaci',
        adminEmail: 'associations@uniflow.fr',
        theme: 'rose'
    },
    {
        id: 3,
        name: 'Départements',
        icon: '🏛️',
        description: 'Organisation des événements des départements académiques.',
        adminName: 'Nadia Benali',
        adminEmail: 'departements@uniflow.fr',
        theme: 'bleu'
    },
    {
        id: 4,
        name: 'Crous',
        icon: '🎓',
        description: 'Vie étudiante, restauration et services campus.',
        adminName: 'Yanis B.',
        adminEmail: 'crous@uniflow.fr',
        theme: 'jaune'
    },
    {
        id: 5,
        name: 'Théâtre Astrée',
        icon: '🎭',
        description: 'Événements culturels, spectacles et programmation.',
        adminName: 'Lina Haddad',
        adminEmail: 'theatre.astree@uniflow.fr',
        theme: 'vert'
    }
]

export const organisateurs = [
    {
        id: 1,
        name: 'AML',
        email: 'aml@uniflow.fr',
        pole: 'Associations',
        status: 'Actif'
    },
    {
        id: 2,
        name: 'Département Informatique',
        email: 'info@uniflow.fr',
        pole: 'Départements',
        status: 'Actif'
    },
    {
        id: 3,
        name: 'BU Lyon 1',
        email: 'bu@uniflow.fr',
        pole: 'Bibliothèque',
        status: 'Actif'
    },
    {
        id: 4,
        name: 'Crous Campus',
        email: 'crous-campus@uniflow.fr',
        pole: 'Crous',
        status: 'En attente'
    },
    {
        id: 5,
        name: 'Astrée Culture',
        email: 'astree@uniflow.fr',
        pole: 'Théâtre Astrée',
        status: 'Actif'
    }
]
export const users = [
    {
        id: 1,
        name: 'arthur',
        email: 'admin@univ-lyon1.fr',
        roles: ['Admin'],
        organization: '',
        organizations: [],
        registrationDate: '2026-01-10'
    },
    {
        id: 2,
        name: 'kaim',
        email: 'aml@uniflow.fr',
        roles: ['Organisateur'],
        organization: 'AML',
        organizations: ['AML'],
        registrationDate: '2026-01-12'
    },
    {
        id: 3,
        name: 'aldjia yennek',
        email: 'info@uniflow.fr',
        roles: ['Organisateur'],
        organization: 'Département Informatique',
        organizations: ['Département Informatique'],
        registrationDate: '2026-01-14'
    },
    {
        id: 4,
        name: 'Meriem Mekhdoul',
        email: 'meriem@etu.univ-lyon1.fr',
        roles: ['Utilisateur', 'Organisateur'],
        organization: 'Association Campus',
        organizations: ['Association Campus', 'AML'],
        registrationDate: '2026-01-18'
    },
    {
        id: 5,
        name: 'Yasmine Ait',
        email: 'yasmine@etu.univ-lyon1.fr',
        roles: ['Utilisateur'],
        organization: '',
        organizations: [],
        registrationDate: '2026-01-20'
    }
]

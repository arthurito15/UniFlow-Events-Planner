export const mockEvents = [
    {
        id: 1,
        title: 'Forum des métiers',
        date: '26 Février 2026',
        organizer: 'AML',
        favorite: true,
        location: 'Université Claude Bernard Lyon 1',
        startTime: '09:00',
        endTime: '17:00',
        tags: ['Orientation', 'Entreprises', 'Etudiants'],
        description:
            'Un événement pour découvrir les métiers, rencontrer des professionnels et échanger sur les parcours possibles. Ateliers, conférences et stands seront au programme.'
    },
    {
        id: 2,
        title: 'destribution de panier',
        date: '26 Février 2026',
        organizer: 'AML',
        favorite: false,
        location: 'Université Claude Bernard Lyon 1',
        startTime: '09:00',
        endTime: '17:00',
        tags: ['Alternance', 'Stages', 'Réseautage'],
        description:
            'Deuxième session du forum : focus sur l alternance et les stages. Inscrivez-vous pour recevoir les informations pratiques avant l event.'
    }
]

export function getEventById(id) {
    const numericId = Number(id)
    return mockEvents.find((e) => e.id === numericId) || null
}

export function getAllEvents() {
    return [
        {
            id: 1,
            title: 'Forum des métiers',
            date: '26 Février 2026',
            organizer: 'AML',
            favorite: true
        },
        {
            id: 2,
            title: 'Hackathon IA',
            date: '10 Mars 2026',
            organizer: 'INSA',
            favorite: false
        },
        {
            id: 3,
            title: 'Forum des métiers',
            date: '26 Février 2026',
            organizer: 'AML',
            favorite: true
        },
        {
            id: 4,
            title: 'Hackathon IA',
            date: '10 Mars 2026',
            organizer: 'INSA',
            favorite: false
        },
        {
            id: 5,
            title: 'Forum des métiers',
            date: '26 Février 2026',
            organizer: 'AML',
            favorite: true
        },
        {
            id: 6,
            title: 'Hackathon IA',
            date: '10 Mars 2026',
            organizer: 'INSA',
            favorite: false
        },
        {
            id: 7,
            title: 'Forum des métiers',
            date: '26 Février 2026',
            organizer: 'AML',
            favorite: true
        },
        {
            id: 8,
            title: 'Hackathon IA',
            date: '10 Mars 2026',
            organizer: 'INSA',
            favorite: false
        },
        {
            id: 9,
            title: 'Forum des métiers',
            date: '26 Février 2026',
            organizer: 'AML',
            favorite: true
        },
        {
            id: 10,
            title: 'Hackathon IA',
            date: '10 Mars 2026',
            organizer: 'INSA',
            favorite: false
        }
    ]
}
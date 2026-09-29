-- Jeu de donnees simple pour les demonstrations UniFlow.
-- Le script remet les tables de demo a zero a chaque demarrage.

DELETE FROM FAVORIS;
DELETE FROM INSCRIPTION;
DELETE FROM ORGANISATION_MEMBRES;
DELETE FROM EVENT;
DELETE FROM ORGANISATION;
DELETE FROM POLE;
DELETE FROM COMPTE_ROLES;
DELETE FROM COMPTE;

-- =====================================================
-- Comptes
-- =====================================================

INSERT INTO COMPTE (EMAIL, PASSWORD_HASH, DATE_INSCRIPTION, NOM, PRENOM) VALUES
('admin@univ-lyon1.fr', 'admin123', CURRENT_TIMESTAMP, 'Admin', 'UniFlow'),
('camille.martin@univ-lyon1.fr', 'demo123', CURRENT_TIMESTAMP, 'Martin', 'Camille'),
('alice.bernard@univ-lyon1.fr', 'alice123', CURRENT_TIMESTAMP, 'Bernard', 'Alice'),
('nathan.dubois@univ-lyon1.fr', 'nathan123', CURRENT_TIMESTAMP, 'Dubois', 'Nathan'),
('sara.leroy@univ-lyon1.fr', 'sara123', CURRENT_TIMESTAMP, 'Leroy', 'Sara'),
('hugo.moreau@univ-lyon1.fr', 'hugo123', CURRENT_TIMESTAMP, 'Moreau', 'Hugo'),
('bde@univ-lyon1.fr', 'bde123', CURRENT_TIMESTAMP, 'Campus', 'BDE'),
('bu@univ-lyon1.fr', 'bu123', CURRENT_TIMESTAMP, 'Universitaire', 'Bibliotheque');

INSERT INTO COMPTE_ROLES (COMPTE_ID, ROLES)
SELECT ID, 'ADMIN'
FROM COMPTE
WHERE EMAIL = 'admin@univ-lyon1.fr';

INSERT INTO COMPTE_ROLES (COMPTE_ID, ROLES)
SELECT ID, 'UTILISATEUR'
FROM COMPTE
WHERE EMAIL IN (
    'camille.martin@univ-lyon1.fr',
    'alice.bernard@univ-lyon1.fr',
    'nathan.dubois@univ-lyon1.fr',
    'sara.leroy@univ-lyon1.fr',
    'hugo.moreau@univ-lyon1.fr',
    'bde@univ-lyon1.fr',
    'bu@univ-lyon1.fr'
);

INSERT INTO COMPTE_ROLES (COMPTE_ID, ROLES)
SELECT ID, 'ORGANISATEUR'
FROM COMPTE
WHERE EMAIL IN ('bde@univ-lyon1.fr', 'bu@univ-lyon1.fr');

-- =====================================================
-- Poles
-- =====================================================

INSERT INTO POLE (NAME) VALUES
('Associations'),
('Crous'),
('Departements'),
('BU');

-- =====================================================
-- Organisations
-- =====================================================

INSERT INTO ORGANISATION (NOM_STRUCTURE, POLE_ID)
SELECT 'BDE Campus', p.ID
FROM POLE p
WHERE p.NAME = 'Associations';

INSERT INTO ORGANISATION (NOM_STRUCTURE, POLE_ID)
SELECT 'Crous Lyon 1', p.ID
FROM POLE p
WHERE p.NAME = 'Crous';

INSERT INTO ORGANISATION (NOM_STRUCTURE, POLE_ID)
SELECT 'Departement Informatique', p.ID
FROM POLE p
WHERE p.NAME = 'Departements';

INSERT INTO ORGANISATION (NOM_STRUCTURE, POLE_ID)
SELECT 'Bibliotheque Universitaire', p.ID
FROM POLE p
WHERE p.NAME = 'BU';

INSERT INTO ORGANISATION_MEMBRES (ORGANISATION_ID, COMPTE_ID)
SELECT o.ID, c.ID
FROM ORGANISATION o
JOIN COMPTE c ON (
    (o.NOM_STRUCTURE = 'BDE Campus' AND c.EMAIL = 'bde@univ-lyon1.fr')
    OR (o.NOM_STRUCTURE = 'Bibliotheque Universitaire' AND c.EMAIL = 'bu@univ-lyon1.fr')
);

-- =====================================================
-- Evenements
-- =====================================================

INSERT INTO EVENT (TITLE, DESCRIPTION, ADDRESS, BEGIN_DATE, END_DATE, CAPACITY, STATUS, PRICE, ORGANISATION_ID, POLE_ID)
SELECT 'Forum des associations',
       'Decouverte des associations du campus avec stands, inscriptions et rencontres.',
       'Maison des Etudiants - Hall principal',
       TIMESTAMP '2026-05-05 12:00:00',
       TIMESTAMP '2026-05-05 18:00:00',
       200,
       'PUBLISHED',
       0.0,
       o.ID,
       p.ID
FROM ORGANISATION o
JOIN POLE p ON p.NAME = 'Associations'
WHERE o.NOM_STRUCTURE = 'BDE Campus';

INSERT INTO EVENT (TITLE, DESCRIPTION, ADDRESS, BEGIN_DATE, END_DATE, CAPACITY, STATUS, PRICE, ORGANISATION_ID, POLE_ID)
SELECT 'Soiree jeux de rentree',
       'Soiree conviviale avec jeux de plateau, quiz et accueil des nouveaux etudiants.',
       'Maison des Etudiants - Salle polyvalente',
       TIMESTAMP '2026-05-12 18:30:00',
       TIMESTAMP '2026-05-12 22:30:00',
       80,
       'PUBLISHED',
       2.0,
       o.ID,
       p.ID
FROM ORGANISATION o
JOIN POLE p ON p.NAME = 'Associations'
WHERE o.NOM_STRUCTURE = 'BDE Campus';

INSERT INTO EVENT (TITLE, DESCRIPTION, ADDRESS, BEGIN_DATE, END_DATE, CAPACITY, STATUS, PRICE, ORGANISATION_ID, POLE_ID)
SELECT 'Distribution paniers etudiants',
       'Distribution de paniers alimentaires sur inscription.',
       'Restaurant universitaire Astree',
       TIMESTAMP '2026-05-07 11:30:00',
       TIMESTAMP '2026-05-07 14:00:00',
       60,
       'PUBLISHED',
       0.0,
       o.ID,
       p.ID
FROM ORGANISATION o
JOIN POLE p ON p.NAME = 'Crous'
WHERE o.NOM_STRUCTURE = 'Crous Lyon 1';

INSERT INTO EVENT (TITLE, DESCRIPTION, ADDRESS, BEGIN_DATE, END_DATE, CAPACITY, STATUS, PRICE, ORGANISATION_ID, POLE_ID)
SELECT 'Atelier budget et logement',
       'Conseils pratiques pour gerer son budget, les aides et la recherche de logement.',
       'Crous - Salle conseil',
       TIMESTAMP '2026-05-20 17:00:00',
       TIMESTAMP '2026-05-20 19:00:00',
       40,
       'PUBLISHED',
       0.0,
       o.ID,
       p.ID
FROM ORGANISATION o
JOIN POLE p ON p.NAME = 'Crous'
WHERE o.NOM_STRUCTURE = 'Crous Lyon 1';

INSERT INTO EVENT (TITLE, DESCRIPTION, ADDRESS, BEGIN_DATE, END_DATE, CAPACITY, STATUS, PRICE, ORGANISATION_ID, POLE_ID)
SELECT 'Atelier Git et projet',
       'Session pratique pour preparer le travail en equipe sur les projets universitaires.',
       'Batiment Nautibus - Salle TP 3',
       TIMESTAMP '2026-05-15 14:00:00',
       TIMESTAMP '2026-05-15 17:00:00',
       32,
       'PUBLISHED',
       0.0,
       o.ID,
       p.ID
FROM ORGANISATION o
JOIN POLE p ON p.NAME = 'Departements'
WHERE o.NOM_STRUCTURE = 'Departement Informatique';

INSERT INTO EVENT (TITLE, DESCRIPTION, ADDRESS, BEGIN_DATE, END_DATE, CAPACITY, STATUS, PRICE, ORGANISATION_ID, POLE_ID)
SELECT 'Conference IA responsable',
       'Introduction aux usages de l IA et aux questions ethiques pour les projets etudiants.',
       'Amphi Nautibus',
       TIMESTAMP '2026-06-02 17:30:00',
       TIMESTAMP '2026-06-02 19:00:00',
       120,
       'PUBLISHED',
       0.0,
       o.ID,
       p.ID
FROM ORGANISATION o
JOIN POLE p ON p.NAME = 'Departements'
WHERE o.NOM_STRUCTURE = 'Departement Informatique';

INSERT INTO EVENT (TITLE, DESCRIPTION, ADDRESS, BEGIN_DATE, END_DATE, CAPACITY, STATUS, PRICE, ORGANISATION_ID, POLE_ID)
SELECT 'Nuit de revision a la BU',
       'Ouverture prolongee avec espaces de travail, pauses encadrees et aide documentaire.',
       'Bibliotheque Universitaire - Salle silence',
       TIMESTAMP '2026-05-28 18:00:00',
       TIMESTAMP '2026-05-29 00:00:00',
       100,
       'PUBLISHED',
       0.0,
       o.ID,
       p.ID
FROM ORGANISATION o
JOIN POLE p ON p.NAME = 'BU'
WHERE o.NOM_STRUCTURE = 'Bibliotheque Universitaire';

INSERT INTO EVENT (TITLE, DESCRIPTION, ADDRESS, BEGIN_DATE, END_DATE, CAPACITY, STATUS, PRICE, ORGANISATION_ID, POLE_ID)
SELECT 'Atelier recherche documentaire',
       'Apprendre a utiliser les bases scientifiques et citer correctement ses sources.',
       'Bibliotheque Universitaire - Salle formation',
       TIMESTAMP '2026-06-05 10:00:00',
       TIMESTAMP '2026-06-05 12:00:00',
       25,
       'PUBLISHED',
       0.0,
       o.ID,
       p.ID
FROM ORGANISATION o
JOIN POLE p ON p.NAME = 'BU'
WHERE o.NOM_STRUCTURE = 'Bibliotheque Universitaire';

-- =====================================================
-- Favoris et inscriptions
-- =====================================================

INSERT INTO FAVORIS (COMPTE_ID, EVENT_ID)
SELECT c.ID, e.ID
FROM COMPTE c
JOIN EVENT e ON (
    (c.EMAIL = 'camille.martin@univ-lyon1.fr' AND e.TITLE IN ('Forum des associations', 'Atelier Git et projet'))
    OR (c.EMAIL = 'alice.bernard@univ-lyon1.fr' AND e.TITLE IN ('Nuit de revision a la BU', 'Conference IA responsable'))
    OR (c.EMAIL = 'nathan.dubois@univ-lyon1.fr' AND e.TITLE IN ('Distribution paniers etudiants', 'Soiree jeux de rentree'))
    OR (c.EMAIL = 'sara.leroy@univ-lyon1.fr' AND e.TITLE IN ('Atelier budget et logement', 'Atelier recherche documentaire'))
);

INSERT INTO INSCRIPTION (USER_ID, EVENT_ID)
SELECT c.ID, e.ID
FROM COMPTE c
JOIN EVENT e ON (
    (e.TITLE = 'Forum des associations' AND c.EMAIL IN (
        'camille.martin@univ-lyon1.fr',
        'alice.bernard@univ-lyon1.fr',
        'nathan.dubois@univ-lyon1.fr',
        'sara.leroy@univ-lyon1.fr',
        'hugo.moreau@univ-lyon1.fr'
    ))
    OR (e.TITLE = 'Soiree jeux de rentree' AND c.EMAIL IN (
        'camille.martin@univ-lyon1.fr',
        'nathan.dubois@univ-lyon1.fr',
        'sara.leroy@univ-lyon1.fr'
    ))
    OR (e.TITLE = 'Atelier Git et projet' AND c.EMAIL IN (
        'camille.martin@univ-lyon1.fr',
        'alice.bernard@univ-lyon1.fr',
        'hugo.moreau@univ-lyon1.fr'
    ))
    OR (e.TITLE = 'Nuit de revision a la BU' AND c.EMAIL IN (
        'alice.bernard@univ-lyon1.fr',
        'sara.leroy@univ-lyon1.fr',
        'hugo.moreau@univ-lyon1.fr'
    ))
    OR (e.TITLE = 'Distribution paniers etudiants' AND c.EMAIL IN (
        'nathan.dubois@univ-lyon1.fr',
        'sara.leroy@univ-lyon1.fr'
    ))
);

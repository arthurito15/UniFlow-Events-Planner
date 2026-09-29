# UniFlow

UniFlow est une application web de gestion d’événements universitaires.

Elle permet aux étudiants de découvrir les événements organisés sur leur campus, de consulter leurs détails, d’indiquer leur intérêt ou de s’inscrire.  
Elle permet également aux organisateurs de créer, modifier et gérer leurs événements depuis une interface simple.


## Présentation du projet

Le projet UniFlow a été réalisé dans le cadre de l’UE de projet de développement d’application web.

Le thème général du projet est la création d’une application permettant de gérer des événements participatifs.  
Nous avons choisi de développer une plateforme dédiée aux événements universitaires.

UniFlow centralise les événements étudiants, culturels, associatifs et universitaires afin de faciliter leur consultation par les étudiants et leur gestion par les organisateurs.

---

## Fonctionnalités principales

### Côté étudiant

- Création de compte
- Connexion
- Consultation des événements disponibles
- Consultation du détail d’un événement
- Inscription à un événement
- Indication d’intérêt pour un événement, si disponible

### Côté organisateur

- Connexion à l’espace organisateur
- Création d’un événement
- Modification d’un événement
- Gestion ou suppression d’un événement, si disponible
- Consultation des inscriptions ou participations, si disponible

### Fonctionnalités techniques

- Interface web responsive
- Communication entre le front-end et le back-end
- Connexion à une base de données
- Déploiement sur une VM de démonstration
- Utilisation de GitLab pour le suivi du projet
- Intégration continue avec GitLab CI/CD
- Analyse de la qualité du code avec SonarQube
- Documentation du projet dans le wiki GitLab

---

## Utilisateurs ciblés

UniFlow vise trois types d’utilisateurs :

- **Étudiants** : consulter les événements du campus et s’y inscrire.
- **Organisateurs** : créer, modifier et suivre leurs événements.
- **Université** : centraliser les événements étudiants, associatifs, culturels et académiques.

---

## Équipe

| Nom | Prénom | Email | Numéro étudiant | Rôle |
|---|---|---|---|---|
| BADRIOUEN | AYOUB | ayoub.badriouen@etu.univ-lyon1.fr | p2506183 | Intégration + Front |
| CHINOUN | RIADH | riadh.chinoun@etu.univ-lyon1.fr | p2203462 | Organisation + Intégration |
| HAMIDOUNI | EL-KAIM | el-kaim.hamidouni@etu.univ-lyon1.fr | p2100030 | Back-end |
| KONKOBO | ULRICH-ARTHUR | ulrich-arthur.konkobo@etu.univ-lyon1.fr | p2513439 | Back-end + BD |
| MEKHDOUL | MERIEM | meriem.mekhdoul@etu.univ-lyon1.fr | p2310195 | BD + Front + Produit |
| YENNEK | ALDJIA | aldjia.yennek@etu.univ-lyon1.fr | p2510289 | BD + Front + Produit |

---

## Architecture

L’application suit une architecture web classique séparant l’interface utilisateur, la logique serveur et la base de données.

```text
Utilisateur
   |
   v
Front-end Web
   |
   v
API Back-end
   |
   v
Base de données
   |
   v
VM de démonstration
```

### Description des composants

- **Front-end** : interface utilisateur permettant la navigation, la consultation et la gestion des événements.
- **Back-end** : gestion de la logique métier, des utilisateurs, des événements et des inscriptions.
- **Base de données** : stockage des utilisateurs, des événements, des inscriptions et des rôles.
- **VM** : environnement utilisé pour la démonstration finale.
- **GitLab CI/CD** : automatisation de certaines étapes du projet.
- **SonarQube** : analyse de la qualité du code.

---
## Dépendances

### Backend (Spring Boot / Java 21)

| Dépendance | Rôle |
|---|---|
| `spring-boot-starter-web` | API REST (contrôleurs, endpoints HTTP) |
| `spring-boot-starter-data-jpa` | Base de données avec JPA / Hibernate |
| `spring-boot-starter-security` | Authentification et autorisation |
| `spring-boot-starter-tomcat` | Serveur web embarqué |
| `java-jwt` (Auth0) | Gestion des tokens JWT |
| `H2 Database` | Base de données légère (développement) |
| `spring-boot-starter-test` | Tests (JUnit, Mockito) |

### Frontend (Vue 3 / Vite)

| Dépendance | Rôle |
|---|---|
| `vue` | Framework principal |
| `vue-router` | Navigation entre pages |
| `bootstrap` | Design et composants UI |
| `vite` | Outil de build |
| `@vitejs/plugin-vue` | Support Vue avec Vite |

## Démo

L'application est accessible sur la VM de démonstration :

- : http://192.168.75.124/

> Accessible uniquement depuis le réseau universitaire ou via VPN.
---

## Installation et build

### Cloner le projet

```bash
git clone https://forge.univ-lyon1.fr/mif10-g1-2025-2026/uniflow.git
cd uniflow
```

### Frontend

```bash
cd front
npm install
npm run dev        # Développement → http://localhost:5173
npm run build      # Production → génère front/dist/
npm run preview    # Prévisualiser le build
```

### Backend

```bash
cd backend
mvn clean install      # Compile + tests
mvn spring-boot:run    # Développement → http://localhost:8080
```

## Scénario de démonstration

Le scénario prévu pour la soutenance est le suivant :

1. Accéder à la page d’accueil UniFlow.
2. Se connecter avec un compte étudiant.
3. Consulter la liste des événements.
4. Ouvrir le détail d’un événement.
5. S’inscrire ou indiquer son intérêt pour l’événement.
6. Se connecter avec un compte organisateur.
7. Créer ou modifier un événement.
8. Vérifier que l’événement est visible côté étudiant.

---

## Branche finale

Une branche `FINAL` est créée pour le rendu final.

Cette branche correspond au code utilisé pour la démonstration finale.

Pour récupérer la branche finale :

```bash
git checkout FINAL
```

---

## Documentation

La documentation complète du projet est disponible dans le wiki GitLab.

Le wiki contient notamment :

- la présentation du projet ;
- la page de l’équipe ;
- les user stories ;
- les maquettes ;
- l’architecture technique ;
- les choix technologiques ;
- le guide d’utilisation ;
- la documentation technique ;
- le processus qualité ;
- les captures SonarQube ;
- les notes de réunion ;
- la page dédiée à la démonstration.

Lien vers le wiki :

```text
[LIEN_WIKI_GITLAB]
```

---

## Limites connues

Certaines fonctionnalités peuvent être limitées dans la version finale :

- recherche avancée d’événements ;
- notifications ;
- statistiques détaillées ;
- gestion avancée des rôles ;
- amélioration des tests automatisés ;
- amélioration de l’interface d’administration.

---

## Perspectives d’amélioration

Pour une future version, nous pourrions ajouter :

- un système de notifications ;
- un calendrier intégré ;
- des filtres par date, thème ou campus ;
- des statistiques de participation ;
- une gestion plus fine des rôles ;
- davantage de tests automatisés.

---

## Auteurs

Projet réalisé par :

- Ayoub BADRIOUEN
- Riadh CHINOUN
- El-Kaim HAMIDOUNI
- Ulrich-Arthur KONKOBO
- Meriem MEKHDOUL
- Aldjia YENNEK

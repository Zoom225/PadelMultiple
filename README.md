# PadelMultiple

## Présentation
Application web complète pour la gestion d’un club de padel, composée d’un backend Spring Boot (Java 21) et d’un frontend Angular. Elle gère membres, réservations, paiements, matchs, etc.

---

## Prérequis
- Java 21
- Node.js >= 18
- npm >= 9
- Docker (optionnel)

---

## Structure du projet
- `frontend/` : Application Angular
- `src/main/java/` : Backend Spring Boot
- `docker-compose.yml` : Lancement via Docker

---

## Commandes d'exécution et de test

### Commandes Docker
- **Démarrer tous les services (backend, base PostgreSQL, etc.)** :
```powershell
docker-compose up --build
```
- **Arrêter tous les services** :
```powershell
docker-compose down
```

### Commandes Backend
- **Compiler et lancer le backend** :
```powershell
./mvnw clean install
./mvnw spring-boot:run
```

### Commandes Frontend
- **Installer les dépendances et lancer le serveur de développement** :
```powershell
cd frontend
npm install
npm start
```

### Tests Backend
- **Lancer tous les tests unitaires (contrôleurs, services, repositories)** :
```powershell
./mvnw test
```
- **Exécuter un test spécifique** (remplacez `NomDeLaClasseDeTest` par le nom de la classe) :
```powershell
./mvnw -Dtest=NomDeLaClasseDeTest test
```
- **Exemples de commandes pour les tests principaux** :
| Couche         | Classe de test                                 | Commande d'exécution                                 |
|---------------|------------------------------------------------|------------------------------------------------------|
| Contrôleur    | com.padelPlay.PadelPlayApplicationTests         | ./mvnw -Dtest=PadelPlayApplicationTests test         |
| Service       | com.padelPlay.service.MatchServiceTest          | ./mvnw -Dtest=MatchServiceTest test                  |
| Service       | com.padelPlay.service.MembreServiceTest         | ./mvnw -Dtest=MembreServiceTest test                 |
| Service       | com.padelPlay.service.PaiementServiceTest       | ./mvnw -Dtest=PaiementServiceTest test               |
| Service       | com.padelPlay.service.ReservationServiceTest    | ./mvnw -Dtest=ReservationServiceTest test            |

### Tests Frontend
- **Lancer les tests unitaires** :
```powershell
cd frontend
npm run test
```

---

## Connexion à la base de données

### Par défaut (H2 en mémoire)
- **URL JDBC** : `jdbc:h2:mem:testdb`
- **Utilisateur** : `sa`
- **Mot de passe** : (vide)
- **Console H2** : http://localhost:8080/h2-console

### PostgreSQL (production, Docker ou local)
- **URL JDBC** : `jdbc:postgresql://localhost:5440/padelService`
- **Utilisateur** : `padel`
- **Mot de passe** : `padel`

> Ces paramètres sont configurables dans `src/main/resources/application.properties` ou dans les variables d’environnement Docker.

---

## Comptes de connexion par défaut (pour tests)

### Membres
- **Lucas Martin** (GLOBAL) :
  - Email : lucas.martin@email.com
  - Mot de passe : (défini lors de l’inscription ou par l’admin)
- **Emma Dubois** (GLOBAL) :
  - Email : emma.dubois@email.com
  - Mot de passe : (défini lors de l’inscription ou par l’admin)
- **Tom Bernard** (SITE Lyon) :
  - Email : tom.bernard@email.com
  - Mot de passe : (défini lors de l’inscription ou par l’admin)
- **Sarah Leroy** (SITE Paris) :
  - Email : sarah.leroy@email.com
  - Mot de passe : (défini lors de l’inscription ou par l’admin)
- **Alex Petit** (LIBRE) :
  - Email : alex.petit@email.com
  - Mot de passe : (défini lors de l’inscription ou par l’admin)

### Administrateurs
- **Admin Global** :
  - Email : admin@padel.com
  - Mot de passe : Admin1234!
- **Admin Lyon** :
  - Email : admin.lyon@padel.com
  - Mot de passe : Admin1234!
- **Admin Paris** :
  - Email : admin.paris@padel.com
  - Mot de passe : Admin1234!

> Les mots de passe membres sont à définir lors de la première connexion ou par l’administrateur.

---

## Accès à l’application
- **Frontend** : http://localhost:4200
- **Backend API** : http://localhost:8080/api
- **Swagger (documentation API)** : http://localhost:8080/swagger-ui.html

---

## Notes complémentaires
- Pour modifier la configuration de la base, éditez `application.properties`.
- Les tests backend couvrent les contrôleurs, services et repositories.
- Le proxy Angular (`proxy.conf.json`) permet d’éviter les problèmes CORS en développement.
- Pour utiliser PostgreSQL en local sans Docker, assurez-vous que la base `padelService` existe et que l’utilisateur `padel`/`padel` a les droits.

---

## Auteurs
Projet réalisé par KANGOUTE AZOUMANAN " etudiant en developpement d'application "

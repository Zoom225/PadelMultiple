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

## Commandes d’exécution

### 1. Lancer avec Docker (recommandé pour tout avoir prêt)

- **Démarrer tous les services (backend, base PostgreSQL, etc.)** :
```powershell
docker-compose up --build
```
- **Arrêter tous les services** :
```powershell
docker-compose down
```

#### Configuration de la base PostgreSQL via Docker

- **Nom du service** : postgres
- **Image** : postgres:15
- **Nom du conteneur** : padel-db
- **Variables d'environnement** :
    - POSTGRES_DB: padelService
    - POSTGRES_USER: padel
    - POSTGRES_PASSWORD: padel
- **Port local** : 5440 (accès à la base sur `localhost:5440`)
- **Volume persistant** : padel-db-data

**Exemple de connexion JDBC pour le backend :**
```
jdbc:postgresql://localhost:5440/padelService
Utilisateur : padel
Mot de passe : padel
```

---

### 2. Backend (Spring Boot)

- **Compiler et lancer le backend** :
```powershell
./mvnw clean install
./mvnw spring-boot:run
```
- **Lancer tous les tests unitaires (contrôleurs, services, repositories)** :
```powershell
./mvnw test
```
- **Méthodologie de test** :
  - Les tests sont écrits avec JUnit et Spring Boot Test.
  - Les classes de test se trouvent dans `src/test/java/`.
  - Les tests couvrent les couches contrôleur, service et repository.
  - Pour exécuter un test spécifique, utilisez :
    ```powershell
    ./mvnw -Dtest=NomDeLaClasseDeTest test
    ```

#### Principales classes de test et commandes associées

| Couche         | Classe de test                                 | Commande d'exécution                                 |
|---------------|------------------------------------------------|------------------------------------------------------|
| Contrôleur    | com.padelPlay.PadelPlayApplicationTests         | ./mvnw -Dtest=PadelPlayApplicationTests test         |
| Service       | com.padelPlay.service.MatchServiceTest          | ./mvnw -Dtest=MatchServiceTest test                  |
| Service       | com.padelPlay.service.MembreServiceTest         | ./mvnw -Dtest=MembreServiceTest test                 |
| Service       | com.padelPlay.service.PaiementServiceTest       | ./mvnw -Dtest=PaiementServiceTest test               |
| Service       | com.padelPlay.service.ReservationServiceTest    | ./mvnw -Dtest=ReservationServiceTest test            |

> Remplacez le nom de la classe si vous souhaitez exécuter d'autres tests présents dans le dossier `src/test/java/`.

---

### 3. Frontend (Angular)

- **Installer les dépendances et lancer le serveur de développement** :
```powershell
cd frontend
npm install
npm start
```
- **Lancer les tests unitaires** :
```powershell
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

## Exemples de tests unitaires (backend)

Voici comment exécuter des tests précis sur les principales classes de test :

| Couche         | Classe de test                                 | Commande d'exécution                                 |
|---------------|------------------------------------------------|------------------------------------------------------|
| Contrôleur    | PadelPlayApplicationTests                      | ./mvnw -Dtest=PadelPlayApplicationTests test         |
| Service       | MatchServiceTest                               | ./mvnw -Dtest=MatchServiceTest test                  |
| Service       | MembreServiceTest                              | ./mvnw -Dtest=MembreServiceTest test                 |
| Service       | PaiementServiceTest                            | ./mvnw -Dtest=PaiementServiceTest test               |
| Service       | ReservationServiceTest                         | ./mvnw -Dtest=ReservationServiceTest test            |

**Exemple d’exécution d’un test service :**
```powershell
./mvnw -Dtest=MembreServiceTest test
```

**Exemple d’exécution d’un test contrôleur :**
```powershell
./mvnw -Dtest=PadelPlayApplicationTests test
```

> Adaptez le nom de la classe selon le composant à tester. Les résultats s’affichent dans la console et dans le dossier `target/surefire-reports/`.

---

## Commande générale pour lancer tous les tests backend

Pour exécuter l’ensemble des tests (contrôleurs, services, repositories) :

```powershell
./mvnw test
```

Les résultats s’affichent dans la console et sont enregistrés dans le dossier `target/surefire-reports/`.

---

## Auteurs
Projet réalisé par l’équipe PadelMultiple.

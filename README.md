# DonoPlatform - Plateforme de dons d'objets

Application web Spring Boot permettant le partage d'objets entre particuliers.

## Membres du groupe
- LAMINOU AMADOU Abdoul Bassit
- KANE Abdou Latif

## Prérequis
- **Java 17+** (obligatoire)
- **Maven 3.8+** (peut être installé ou utiliser Maven Wrapper)

## Installation et lancement

### Étape 1 : Vérifier Java

```bash
# Vérifier la version de Java
java -version

# Doit afficher Java 17 ou supérieur
```

### Étape 2 : Installer Maven (si nécessaire)

**Si vous n'avez pas Maven installé**, deux options :

#### Option A : Installer Maven
- Windows : https://maven.apache.org/download.cgi
- Linux/Mac :
  ```bash
  # Ubuntu/Debian
  sudo apt install maven
  
  # MacOS
  brew install maven
  ```

#### Option B : Ajouter Maven Wrapper au projet
```bash
# Si vous avez Maven installé temporairement
mvn wrapper:wrapper
chmod +x mvnw  # Linux/Mac uniquement
```

### Étape 3 : Lancer l'application

#### Linux/Mac
```bash
chmod +x run.sh
./run.sh run
```

#### Windows
```batch
run.bat run
```

**Note** : Les scripts détectent automatiquement Maven Wrapper (`mvnw`/`mvnw.cmd`) ou Maven système (`mvn`).

### Commandes disponibles

| Commande | Description |
|----------|-------------|
| `compile` | Compile le code source |
| `package` | Crée le JAR exécutable |
| `run` | Lance l'application (défaut) |
| `clean` | Nettoie le projet |
| `all` | Build complet (compile + package) |

**Exemples :**
```bash
# Linux/Mac
./run.sh compile
./run.sh package

# Windows
run.bat compile
run.bat package
```

## Accès à l'application

Une fois lancée, l'application est accessible sur :

- **Interface Web** : http://localhost:8080
- **API JSON** : http://localhost:8080 (avec header `Accept: application/json`)
- **Console H2** : http://localhost:8080/h2-console
    - **JDBC URL** : `jdbc:h2:file:./data/donoplatform_db`
    - **Username** : `sa`
    - **Password** : (vide)

## Comptes de test

Des comptes sont créés automatiquement au démarrage :

### Utilisateurs
- user1@gmail.com / 123456
- user2@gmail.com / 123456
- user3@gmail.com / 123456
- user4@gmail.com / 123456
- user5@gmail.com / 123456

### Administrateurs
- admin1@gmail.com / 123456
- admin2@gmail.com / 123456

## Fonctionnalités principales

- ✅ Création et gestion d'annonces de dons
- ✅ Recherche avancée avec filtres multiples
- ✅ Sauvegarde de recherches avec notifications automatiques
- ✅ Système de favoris
- ✅ Lots d'objets groupés
- ✅ Messagerie interne entre utilisateurs
- ✅ Double authentification (JWT pour API, Sessions pour Web)
- ✅ Interface Web responsive
- ✅ API REST complète avec HATEOAS

## Architecture

- **Framework** : Spring Boot 3.5.7
- **Base de données** : H2 (embedded)
- **Sécurité** : Spring Security
- **Templates** : Thymeleaf
- **Build** : Maven

## Dépannage

### Erreur : "mvnw.cmd not found" ou "mvnw not found"
➡️ Les nouveaux scripts détectent automatiquement Maven système si mvnw n'existe pas.

### Erreur : "Neither mvnw nor Maven are available"
➡️ Installez Maven ou ajoutez Maven Wrapper au projet :
```bash
mvn wrapper:wrapper
```

### Port 8080 déjà utilisé
➡️ Modifiez le port dans `src/main/resources/application.properties` :
```properties
server.port=8081
```

### Base de données H2 verrouillée
➡️ Assurez-vous qu'aucune autre instance n'est en cours :
```bash
# Linux/Mac
./run.sh clean

# Windows
run.bat clean
```

## Structure du projet

```
donoplatform/
├── src/main/java/fr/isima/glia/donoplatform/
│   ├── controller/     # Contrôleurs REST et Web
│   ├── service/        # Logique métier
│   ├── repository/     # Accès aux données
│   ├── model/          # Entités JPA
│   ├── dto/            # Data Transfer Objects
│   ├── security/       # Configuration sécurité
│   └── config/         # Configuration Spring
├── src/main/resources/
│   ├── templates/      # Templates Thymeleaf
│   ├── static/css/     # Feuilles de style
│   └── application.properties
├── run.sh             # Script Linux/Mac
├── run.bat            # Script Windows
└── pom.xml            # Configuration Maven
```

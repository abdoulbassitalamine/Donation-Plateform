# DonoPlatform - Plateforme de dons d'objets

Application web Spring Boot permettant le partage d'objets entre particuliers.

## Membres du groupe
- [Votre nom]
- [Nom du binôme]

## Prérequis
- Java 17+
- Maven 3.8+ (inclus via Maven Wrapper)

## Installation et lancement

### Linux/Mac
```bash
chmod +x run.sh
./run.sh run
```

### Windows
```batch
run.bat run
```

### Commandes disponibles
- `compile` : Compile le code
- `test` : Exécute les tests
- `package` : Crée le JAR exécutable
- `run` : Lance l'application (défaut)
- `clean` : Nettoie le projet
- `all` : Build complet

## Accès à l'application
- Web : http://localhost:8080
- API JSON : http://localhost:8080 (header `Accept: application/json`)
- Console H2 : http://localhost:8080/h2-console

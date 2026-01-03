#!/bin/bash

# DonoPlatform - Script de compilation et exécution
# Usage: ./run.sh [compile|test|run|clean|package]

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

print_header() {
    echo -e "${GREEN}================================================${NC}"
    echo -e "${GREEN}  DonoPlatform - Spring Boot Application${NC}"
    echo -e "${GREEN}================================================${NC}"
    echo ""
}

compile() {
    echo -e "${YELLOW}[1/2] Compilation du projet...${NC}"
    ./mvnw clean compile -DskipTests
    echo -e "${GREEN}✓ Compilation réussie${NC}"
    echo ""
}

run_tests() {
    echo -e "${YELLOW}[2/2] Exécution des tests...${NC}"
    ./mvnw test
    echo -e "${GREEN}✓ Tests terminés${NC}"
    echo ""
}

package_app() {
    echo -e "${YELLOW}[1/1] Création du JAR exécutable...${NC}"
    ./mvnw clean package -DskipTests
    echo -e "${GREEN}✓ JAR créé : target/donoplatform-0.0.1-SNAPSHOT.jar${NC}"
    echo ""
}

run_app() {
    echo -e "${YELLOW}Démarrage de l'application...${NC}"
    echo ""
    
    if [ -f "target/donoplatform-0.0.1-SNAPSHOT.jar" ]; then
        java -jar target/donoplatform-0.0.1-SNAPSHOT.jar
    else
        echo -e "${YELLOW}JAR introuvable, compilation en cours...${NC}"
        package_app
        java -jar target/donoplatform-0.0.1-SNAPSHOT.jar
    fi
}

clean_all() {
    echo -e "${YELLOW}Nettoyage du projet...${NC}"
    ./mvnw clean
    rm -rf data/*.db
    echo -e "${GREEN}✓ Projet nettoyé${NC}"
    echo ""
}

show_info() {
    echo -e "${GREEN}Application démarrée avec succès!${NC}"
    echo ""
    echo "📍 URLs disponibles :"
    echo "   - Application web : http://localhost:8080"
    echo "   - API JSON : http://localhost:8080 (avec Accept: application/json)"
    echo "   - H2 Console : http://localhost:8080/h2-console"
    echo ""
    echo "🔐 Base H2 :"
    echo "   - URL JDBC : jdbc:h2:file:./data/donoplatform_db"
    echo "   - Username : sa"
    echo "   - Password : (vide)"
    echo ""
    echo "Appuyez sur Ctrl+C pour arrêter l'application"
}

# Main
print_header

case "${1:-run}" in
    compile)
        compile
        ;;
    test)
        compile
        run_tests
        ;;
    package)
        package_app
        show_info
        ;;
    run)
        run_app
        ;;
    clean)
        clean_all
        ;;
    all)
        compile
        run_tests
        package_app
        echo -e "${GREEN}✓ Build complet terminé${NC}"
        ;;
    *)
        echo "Usage: $0 {compile|test|package|run|clean|all}"
        echo ""
        echo "  compile  - Compile le code source"
        echo "  test     - Compile et exécute les tests"
        echo "  package  - Crée le JAR exécutable"
        echo "  run      - Lance l'application (défaut)"
        echo "  clean    - Nettoie le projet"
        echo "  all      - Compile, teste et package"
        exit 1
        ;;
esac
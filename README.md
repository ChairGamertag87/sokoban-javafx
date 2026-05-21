# Sokoban JavaFX

Projet universitaire — Jeu Sokoban développé en JavaFX dans le cadre du cours IHM 2026.

---

## Stack technique

- **Java 21**
- **JavaFX 21.0.2**
- **FXML** pour la définition des vues
- **CSS** pour le style de l'interface
- **Maven** pour la gestion des dépendances et le build

---

## Structure du projet

```
src/
  main/
    java/
      ihm/sokoban/
        Main.java                        → Point d'entrée
        SokobanApp.java                  → Application JavaFX (chargement FXML, Scene, clavier)
        controller/
          SokobanController.java         → Contrôleur principal de l'IHM
        model/                           → Moteur de jeu (fourni, non modifiable)
        util/                            → Utilitaires niveaux (fourni, non modifiable)
    resources/
      ihm/sokoban/
        fxml/
          sokoban.fxml                   → Vue principale (BorderPane, GridPane, menus, labels)
        css/
          sokoban.css                    → Styles par TypeCase + layout
```

---

## Ce qui est actuellement en place

### Squelette de l'IHM (étape 1/14)

- **`sokoban.fxml`** — Vue complète avec :
  - `MenuBar` structurée (Jeu / Édition / Aide)
  - `GridPane` central prêt à recevoir le plateau dynamique
  - Barre de stats en bas (mouvements, poussées, caisses placées)
  - Boutons d'action (Annuler, Recommencer, Précédent, Suivant)
  - Label message contextuel (victoire, défaite, etc.)

- **`SokobanController.java`** — Contrôleur principal avec :
  - `initialize()` → instancie `JeuSokoban(0)` (10 niveaux Palier B)
  - `chargerNiveau()` → reconstruction dynamique du GridPane
  - `setupClavier()` → EventFilter sur la Scene (flèches, R, Z)
  - `rafraichirUI()` / `rafraichirPlateau()` → synchronisation de la vue
  - `checkEtat()` → détection victoire/défaite avec Alerts
  - Tous les handlers FXML (recommencer, annuler, navigation, quitter, à propos)

- **`sokoban.css`** — Styles définis pour chaque `TypeCase` :
  - Mur, Sol, Cible, Caisse, **Caisse sur cible (vert)**, Joueur, Vide
  - Style global sombre + barre de stats + boutons avec hover

- **`SokobanApp.java`** — Charge le FXML, crée la Scene, attache le CSS, branche le clavier et gère la fermeture propre via `setOnCloseRequest`

### Ce qui reste à implémenter

Voir le plan d'action complet dans `.claude/CLAUDE.md`.

---

## Lancer le projet

Depuis IntelliJ IDEA avec le JDK 21 :

```
Run → Main
```

Ou via Maven (nécessite JDK 21 dans `JAVA_HOME`) :

```bash
mvn javafx:run
```

---

## Palier visé

**Palier B** — 10 niveaux variés avec grille dynamique + options bonus (undo, navigation, MenuBar, compteurs).

---

## Outils d'assistance utilisés

Ce projet a été développé avec l'assistance de **Claude** (Anthropic) :

- **Claude Opus 4.6** — Architecture, logique du contrôleur, structure FXML, plan d'action
- **Claude Sonnet 4.6** — Conseils CSS, style de l'interface, suggestions visuelles

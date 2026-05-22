# Sokoban JavaFX

Projet universitaire — Jeu Sokoban avec theme Minecraft, developpe en JavaFX dans le cadre du cours IHM 2026.

---

## Stack technique

- **Java 21**
- **JavaFX 21.0.2** (controls, fxml, media)
- **FXML** pour la definition des vues
- **CSS** pour le theme Minecraft
- **Maven** pour la gestion des dependances et le build

---

## Structure du projet

```
src/
  main/
    java/
      ihm/sokoban/
        Main.java                        -> Point d'entree
        SokobanApp.java                  -> Application JavaFX (menu principal)
        controller/
          MenuController.java            -> Controleur du menu d'accueil
          SokobanController.java         -> Controleur principal du jeu
          SoundManager.java              -> Gestionnaire de sons
        model/                           -> Moteur de jeu (fourni, non modifiable)
        util/                            -> Utilitaires niveaux (fourni, non modifiable)
    resources/
      ihm/sokoban/
        fxml/
          menu.fxml                      -> Ecran d'accueil (titre + choix niveaux)
          sokoban.fxml                   -> Vue principale du jeu
        css/
          sokoban.css                    -> Theme Minecraft complet
        fonts/
          Minecraftia-Regular.ttf        -> Police pixel art
        images/                          -> Textures Minecraft (mur, sol, caisse, joueur, etc.)
        sounds/                          -> Sons (click, levelup)
```

---

## Fonctionnalites implementees

### Palier A (complet)
- Affichage plateau avec textures Minecraft distinctes par TypeCase
- Deplacement joueur aux fleches (EventFilter)
- Poussee de caisses + gestion blocage sans crash
- Detection victoire avec son + message differencie (dernier niveau)
- Detection defaite avec choix Annuler / Recommencer
- Compteur de mouvements, poussees, caisses placees
- Bouton et touche Recommencer
- Quitter avec confirmation

### Palier B (complet)
- Grille construite dynamiquement (getNbLignes / getNbColonnes)
- Reconstruction du GridPane a chaque changement de niveau
- Support des 10 niveaux NiveauxSokoban (5x5 a 10x9)

### Options bonus
- **Menu principal** — ecran d'accueil style Minecraft avec choix Tutoriel / Normal / Quitter
- **Theme Minecraft** — textures PNG, boutons avec texture, police Minecraftia, fond terracotta
- **Sons** — click sur boutons, levelup a la victoire
- **Undo** — bouton + touche Z
- **Navigation niveaux** — boutons + touches A/E
- **MenuBar** — Jeu / Edition / Aide avec raccourcis
- **Compteurs** — mouvements, poussees, caisses X/Y, nom du niveau
- **Fenetre maximisee** au demarrage

---

## Lancer le projet

Depuis IntelliJ IDEA avec le JDK 21 :

```
Run -> Main
```

Ou via Maven (necessite JDK 21 dans `JAVA_HOME`) :

```bash
mvn javafx:run
```

---

## Palier vise

**Palier B** — 10 niveaux varies avec grille dynamique + options bonus (theme Minecraft, sons, menu principal, undo, navigation, MenuBar, compteurs).

---

## Outils d'assistance utilises

Ce projet a ete developpe avec l'assistance de **Claude** (Anthropic) :

- **Claude Opus 4.6** — Architecture, logique des controleurs, structure FXML, plan d'action
- **Claude Sonnet 4.6** — CSS, integration theme Minecraft, suggestions visuelles

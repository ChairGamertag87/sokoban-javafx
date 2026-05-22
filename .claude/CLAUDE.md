# CLAUDE.md — Projet Sokoban JavaFX

## Vue d'ensemble

Application JavaFX permettant de jouer au Sokoban avec un theme visuel Minecraft.
Le moteur de jeu est **fourni et intouchable** (packages `ihm.sokoban.model` et `ihm.sokoban.util`).

- **Palier vise** : B (10 niveaux varies, grille dynamique) + options
- **Stack** : Java 21, JavaFX, FXML, Maven (modele HelloJavaFX)
- **Deadline** : vendredi 22 mai avant 18h45 (bonus +1 point si respecte)

---

## Contraintes absolues (penalites lourdes si violation)

| Contrainte | Penalite |
|---|---|
| Ne **jamais** modifier les packages `ihm.sokoban.model` et `ihm.sokoban.util` | -15 pts |
| FXML **obligatoire** (pas de UI full-code) | -10 pts |
| Pas de JAR executable (`*-shaded.jar`) | -4 pts |
| Exceptions non gerees / crashes | -4 pts |
| Code mort ou fichiers inutiles dans le rendu | -4 pts |
| `System.out.println(jeu)` ou logs de debug laisses | -2 pts |
| Pas de document de synthese PDF | -2 pts |
| Retard de rendu | -2 pts/jour |

---

## Architecture du projet

```
src/
  main/
    java/
      ihm/sokoban/
        Main.java              -> Point d'entree
        SokobanApp.java        -> Application JavaFX (menu principal au demarrage)
        controller/
          MenuController.java  -> Controleur du menu principal
          SokobanController.java -> Controleur principal du jeu
          SoundManager.java    -> Gestionnaire de sons (click, levelup)
        model/                 -> [FOURNI - NE PAS TOUCHER]
        util/                  -> [FOURNI - NE PAS TOUCHER]
    resources/
      ihm/sokoban/
        fxml/
          menu.fxml            -> Ecran d'accueil (choix des niveaux)
          sokoban.fxml         -> Vue principale du jeu
        css/
          sokoban.css          -> Theme Minecraft complet
        fonts/
          Minecraftia-Regular.ttf -> Police pixel art Minecraft
        images/
          mur.png              -> Texture cobblestone (murs)
          sol.png              -> Texture briques (sol)
          case.png             -> Texture planches (caisse)
          cible.png            -> Texture cible
          caisse_cible.png     -> Texture emerald block (caisse bien placee)
          joueur.png           -> Steve (joueur)
          joueur_cible.png     -> Steve sur cible
          fond.png             -> Yellow terracotta (fond)
          title.png            -> Logo titre "Sokoban JavaFX"
          btn_normal.png       -> Texture bouton Minecraft (normal)
          btn_hover.png        -> Texture bouton Minecraft (hover)
        sounds/
          click.mp3            -> Son de clic bouton
          levelup.mp3          -> Son de victoire niveau
```

> **Regle stricte** : zero logique metier dans les controleurs. Toute logique passe par `JeuSokoban`.

---

## API du moteur — Reference rapide

### Instanciation

```java
// Palier A — 5 niveaux tutoriel 8x8
JeuSokoban jeu = new JeuSokoban(NiveauxTutoriel.getNiveaux(), NiveauxTutoriel.getNoms(), 0);

// Palier B — 10 niveaux varies
JeuSokoban jeu = new JeuSokoban(0);

// Palier C — dossier .xsb
Banque b = LoaderNiveauxXSB.chargerDepuisDossier(Path.of("niveaux_xsb_exemple"));
JeuSokoban jeu = new JeuSokoban(b.niveaux, b.noms, 0);

// Changer de banque a la volee
jeu.setBanqueNiveaux(NiveauxSokoban.getNiveaux(), NiveauxSokoban.getNoms());
```

### Deplacement — TOUJOURS proteger avec peutJouer()

```java
if (jeu.peutJouer()) {
    ResultatMouvement r = jeu.deplacer(Direction.DROITE);
}
```

> `deplacer()` apres fin de partie -> `SokobanException(MOUVEMENT_APRES_FIN)`.

### Lecture d'etat

```java
jeu.getEtatPartie()          // EN_COURS | GAGNEE | PERDU
jeu.peutJouer()              // raccourci etat == EN_COURS
jeu.isNiveauTermine()        // raccourci etat == GAGNEE
jeu.isPerdu()                // raccourci etat == PERDU
jeu.estDernierNiveau()       // pour message differencie

jeu.getNbMouvements()
jeu.getNbPoussees()
jeu.getNbCaissesSurCible()
jeu.getNbCaisses()

jeu.getNbLignes()
jeu.getNbColonnes()
jeu.getCase(ligne, colonne)  // retourne TypeCase
```

### Navigation entre niveaux

```java
jeu.getNiveauCourant()        // index 0-based
jeu.getNbNiveaux()
jeu.getNomNiveauCourant()
jeu.niveauSuivant()           // false si dernier
jeu.niveauPrecedent()         // false si premier
jeu.chargerNiveauParIndex(n)
```

### Undo / Reset

```java
if (jeu.peutAnnuler()) jeu.annuler();
jeu.reset();
```

### TypeCase — methodes utilitaires

```java
TypeCase tc = jeu.getCase(l, c);
tc.estMur()     // MUR
tc.estCaisse()  // CAISSE ou CAISSE_SUR_CIBLE
tc.estCible()   // CIBLE, CAISSE_SUR_CIBLE ou JOUEUR_SUR_CIBLE
tc.estJoueur()  // JOUEUR ou JOUEUR_SUR_CIBLE
tc.estLibre()   // SOL ou CIBLE vide (marchable)
```

### Enums

```
Direction     : HAUT, BAS, GAUCHE, DROITE
TypeCase      : MUR, SOL, CIBLE, CAISSE, CAISSE_SUR_CIBLE, JOUEUR, JOUEUR_SUR_CIBLE, VIDE
EtatPartie    : EN_COURS, GAGNEE, PERDU
ResultatMouvement : DEPLACE, POUSSE, BLOQUE
```

---

## Gestion du clavier — Piege critique

**Ne pas utiliser `scene.setOnKeyPressed()`** -> EventFilter obligatoire.

```java
scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
    boolean traite = true;
    switch (event.getCode()) {
        case UP    -> deplacerJoueur(Direction.HAUT);
        case DOWN  -> deplacerJoueur(Direction.BAS);
        case LEFT  -> deplacerJoueur(Direction.GAUCHE);
        case RIGHT -> deplacerJoueur(Direction.DROITE);
        case R     -> handleRecommencer();
        case Z     -> handleAnnuler();
        case A     -> handleNiveauPrecedent();
        case E     -> handleNiveauSuivant();
        default    -> traite = false;
    }
    if (traite) event.consume();
});
```

---

## Fonctionnalites Palier A (obligatoires)

- [x] Affichage plateau avec textures Minecraft par TypeCase
- [x] Deplacement joueur aux fleches (EventFilter)
- [x] Poussee de caisses fonctionnelle + BLOQUE sans crash
- [x] Detection victoire (GAGNEE) -> message visible + son levelup
- [x] Detection defaite (PERDU) -> message + choix Annuler/Recommencer
- [x] Compteur de mouvements affiche
- [x] Bouton/touche Recommencer (reset())
- [x] Quitter avec confirmation (Alert)
- [x] Enchainement des niveaux (bouton "Niveau suivant")
- [x] Message differencie sur le dernier niveau

---

## Fonctionnalites Palier B (grille dynamique)

- [x] Grille construite dynamiquement via getNbLignes() / getNbColonnes()
- [x] Reconstruction complete du GridPane a chaque changement de niveau
- [x] Support des 10 niveaux NiveauxSokoban (de 5x5 a 10x9)

---

## Options implementees

### Jeu / Sokoban
- [x] Compteur de poussees (*)
- [x] Nom du niveau affiche (*)
- [x] Undo — bouton + touche Z (**)
- [x] Navigation precedent/suivant — boutons + touches A/E (**)
- [x] Sons — click bouton + levelup victoire (***)
- [x] Menu principal — choix Tutoriel / Normal / Quitter (***)

### JavaFX / IHM
- [x] Theme Minecraft complet — textures, boutons, police Minecraftia (***)
- [x] Menu "A propos" (*)
- [x] MenuBar structuree : Jeu / Edition / Aide (**)
- [x] Raccourcis clavier : R, Z, A, E (**)
- [x] Indicateur de progression "Caisses : X/Y" (***)
- [x] Fenetre maximisee au demarrage (*)

---

## CSS — Theme Minecraft

Les cases utilisent des textures PNG (images Minecraft) :
```css
.case-mur         { -fx-background-image: url("../images/mur.png"); }
.case-sol         { -fx-background-image: url("../images/sol.png"); }
.case-cible       { -fx-background-image: url("../images/cible.png"); }
.case-caisse      { -fx-background-image: url("../images/case.png"); }
.case-caisse-cible { -fx-background-image: url("../images/caisse_cible.png"); }
.case-joueur      { -fx-background-image: url("../images/joueur.png"); }
.case-joueur-cible { -fx-background-image: url("../images/joueur_cible.png"); }
.case-vide        { -fx-background-color: transparent; }
```

Boutons avec texture Minecraft (btn_normal.png / btn_hover.png).
Police Minecraftia chargee au demarrage via Font.loadFont().
Fond yellow terracotta en mosaique.

---

## Flow de l'application

1. `Main.main()` -> `SokobanApp.start()`
2. Chargement `menu.fxml` -> ecran d'accueil avec titre + boutons
3. Choix du mode (Tutoriel 5 niveaux / Normal 10 niveaux)
4. `MenuController.lancerJeu()` -> charge `sokoban.fxml`, injecte le `JeuSokoban` via `setJeu()`
5. Jeu en cours avec clavier (fleches + raccourcis) et boutons

---

## Livrables a rendre

1. Archive ZIP du projet (`mvn clean` avant de zipper)
2. JAR executable `*-shaded.jar` (genere par Maven)
3. Document de synthese PDF (1-2 pages)

---

## Checklist avant rendu

- [ ] `mvn clean` effectue
- [ ] Aucun `System.out.println` laisse
- [ ] Aucun fichier inutile / code mort
- [ ] JAR shaded genere et teste
- [ ] Toutes les `SokobanException` catchees (pas de crash)
- [ ] EventFilter clavier en place (pas setOnKeyPressed)
- [ ] `peutJouer()` verifie avant chaque `deplacer()`
- [ ] `peutAnnuler()` verifie avant chaque `annuler()`
- [ ] Message "dernier niveau reussi" differencie
- [ ] Document de synthese PDF present
- [ ] Packages model/ et util/ NON modifies

---

## Taches restantes

| # | Tache | Statut |
|---|---|---|
| 1 | Nettoyage : supprimer code mort, fichiers inutiles | A FAIRE |
| 2 | Generer le JAR shaded (`mvn package`) | A FAIRE |
| 3 | Document de synthese PDF (1-2 pages) | A FAIRE |
| 4 | `mvn clean` + ZIP du projet | A FAIRE |

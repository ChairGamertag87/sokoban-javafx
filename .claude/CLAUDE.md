# CLAUDE.md — Projet Sokoban JavaFX

## Vue d'ensemble

Application JavaFX permettant de jouer au Sokoban. L'IHM est entièrement à développer.
Le moteur de jeu est **fourni et intouchable** (packages `ihm.sokoban.model` et `ihm.sokoban.util`).

- **Palier visé** : B (10 niveaux variés, grille dynamique) + options
- **Stack** : Java 21, JavaFX, FXML, Maven (modèle HelloJavaFX)
- **Deadline** : vendredi 22 mai avant 18h45 (bonus +1 point si respecté)

---

## Contraintes absolues (pénalités lourdes si violation)

| Contrainte | Pénalité |
|---|---|
| Ne **jamais** modifier les packages `ihm.sokoban.model` et `ihm.sokoban.util` | −15 pts |
| FXML **obligatoire** (pas de UI full-code) | −10 pts |
| Pas de JAR exécutable (`*-shaded.jar`) | −4 pts |
| Exceptions non gérées / crashes | −4 pts |
| Code mort ou fichiers inutiles dans le rendu | −4 pts |
| `System.out.println(jeu)` ou logs de debug laissés | −2 pts |
| Pas de document de synthèse PDF | −2 pts |
| Retard de rendu | −2 pts/jour |

---

## Architecture du projet

```
src/
  main/
    java/
      ihm/sokoban/
        app/          → Main.java (Application JavaFX)
        controller/   → SokobanController.java (+ autres contrôleurs si besoin)
        model/        → [FOURNI - NE PAS TOUCHER]
        util/         → [FOURNI - NE PAS TOUCHER]
    resources/
      ihm/sokoban/
        fxml/         → sokoban.fxml (vue principale)
        css/          → sokoban.css
        images/       → sprites optionnels
```

> **Règle stricte** : zéro logique métier dans les contrôleurs. Toute logique passe par `JeuSokoban`.

---

## API du moteur — Référence rapide

### Instanciation

```java
// Palier A — 5 niveaux tutoriel 8×8
JeuSokoban jeu = new JeuSokoban(NiveauxTutoriel.getNiveaux(), NiveauxTutoriel.getNoms(), 0);

// Palier B — 10 niveaux variés
JeuSokoban jeu = new JeuSokoban(0);

// Palier C — dossier .xsb
Banque b = LoaderNiveauxXSB.chargerDepuisDossier(Path.of("niveaux_xsb_exemple"));
JeuSokoban jeu = new JeuSokoban(b.niveaux, b.noms, 0);

// Changer de banque à la volée
jeu.setBanqueNiveaux(NiveauxSokoban.getNiveaux(), NiveauxSokoban.getNoms());
```

### Déplacement — TOUJOURS protéger avec peutJouer()

```java
// Pattern obligatoire : guard avant chaque appel à deplacer()
if (jeu.peutJouer()) {
    ResultatMouvement r = jeu.deplacer(Direction.DROITE);
    // DEPLACE | POUSSE | BLOQUE
}
```

> `deplacer()` après fin de partie → `SokobanException(MOUVEMENT_APRES_FIN)`. Le moteur est correct, si ça crash c'est l'IHM.

### Lecture d'état

```java
jeu.getEtatPartie()          // EN_COURS | GAGNEE | PERDU
jeu.peutJouer()              // raccourci etat == EN_COURS
jeu.isNiveauTermine()        // raccourci etat == GAGNEE
jeu.isPerdu()                // raccourci etat == PERDU
jeu.estDernierNiveau()       // pour message différencié sur le dernier niveau

jeu.getNbMouvements()
jeu.getNbPoussees()
jeu.getNbCaissesSurCible()
jeu.getNbCaisses()

jeu.getNbLignes()
jeu.getNbColonnes()
jeu.getCase(ligne, colonne)  // retourne TypeCase
jeu.getGrille()              // TypeCase[][]

jeu.getJoueurLigne()
jeu.getJoueurColonne()
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
// Toujours protéger
if (jeu.peutAnnuler()) jeu.annuler();
jeu.reset(); // recommence le niveau courant
```

### TypeCase — méthodes utilitaires

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

### Exceptions attendues

| TypeErreur | Cause |
|---|---|
| `MOUVEMENT_APRES_FIN` | `deplacer()` après GAGNEE/PERDU |
| `RIEN_A_ANNULER` | `annuler()` sans historique |
| `NIVEAU_INEXISTANT` | index hors limites |
| `NIVEAU_INVALIDE` | chaîne null/vide ou dossier .xsb invalide |
| `AUCUN_JOUEUR` | pas de `@` ou `+` dans le niveau |
| `CAISSES_CIBLES_DIFFERENTES` | nb caisses ≠ nb cibles |

---

## Gestion du clavier — Piège critique

**Ne pas utiliser `scene.setOnKeyPressed()`** → les flèches cessent de fonctionner dès qu'un Button ou la MenuBar prend le focus.

**Pattern correct obligatoire (EventFilter) :**

```java
scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
    boolean traite = true;
    switch (event.getCode()) {
        case UP    -> deplacerJoueur(Direction.HAUT);
        case DOWN  -> deplacerJoueur(Direction.BAS);
        case LEFT  -> deplacerJoueur(Direction.GAUCHE);
        case RIGHT -> deplacerJoueur(Direction.DROITE);
        case R     -> jeu.reset();
        case Z     -> { if (jeu.peutAnnuler()) jeu.annuler(); }
        default    -> traite = false;
    }
    if (traite) event.consume();
});
```

---

## Fonctionnalités minimales Palier A (obligatoires)

- [ ] Affichage plateau avec couleurs/symboles distincts par `TypeCase` (caisse bien placée = visuellement différente)
- [ ] Déplacement joueur aux flèches (EventFilter, voir ci-dessus)
- [ ] Poussée de caisses fonctionnelle + BLOQUE sans crash
- [ ] Détection victoire (`GAGNEE`) → message visible
- [ ] Détection défaite (`PERDU`) → message + proposer Annuler ou Recommencer
- [ ] Compteur de mouvements affiché
- [ ] Bouton/touche Recommencer (`reset()`)
- [ ] Quitter avec confirmation (`Alert`)
- [ ] Enchaînement des 5 niveaux tutoriel (bouton "Niveau suivant")
- [ ] Message différencié sur le dernier niveau ("Bravo, dernier niveau réussi !")

---

## Fonctionnalités Palier B (grille dynamique)

- [ ] Grille construite dynamiquement via `jeu.getNbLignes()` / `jeu.getNbColonnes()`
- [ ] Reconstruction complète du `GridPane` à chaque changement de niveau
- [ ] Support des 10 niveaux `NiveauxSokoban` (de 5×5 à 10×9)

---

## Options prioritaires visées

### Jeu / Sokoban
- Compteur de poussées (★)
- Nom du niveau affiché (★)
- Undo — bouton + Ctrl+Z (★★)
- Navigation précédent/suivant (★★)
- Progression auto après victoire (★★★)

### JavaFX / IHM
- CSS personnalisé — couleurs et style des cases (★)
- Menu "À propos" (★)
- MenuBar structurée : Jeu / Édition / Aide (★★)
- Raccourcis clavier : R, Z, N, P, Ctrl+Z (★★)
- Indicateur de progression "Caisses placées : X/Y" (★★★)
- Compteurs réactifs avec `IntegerProperty` + binding (★★★)

---

## Structure du contrôleur principal

```java
public class SokobanController {
    @FXML private GridPane grid_plateau;
    @FXML private Label label_mouvements;
    @FXML private Label label_poussees;
    @FXML private Label label_niveau;
    @FXML private Label label_caisses;
    @FXML private Label label_message;

    private JeuSokoban jeu;

    // Appelé par initialize() ou changement de niveau
    private void chargerNiveau() { /* reconstruit le GridPane */ }

    // Pattern : guard + deplacer() + rafraîchirUI() + checkEtat()
    private void deplacerJoueur(Direction dir) { ... }

    private void rafraichirUI() { /* resynchronise toute la vue */ }

    private void checkEtat() { /* GAGNEE → Alert, PERDU → Alert */ }
}
```

> `rafraichirUI()` doit être appelé après **chaque** action (déplacement, undo, reset, changement de niveau).

---

## Rebuild du GridPane (Palier B)

```java
private void chargerNiveau() {
    grid_plateau.getChildren().clear();
    grid_plateau.getRowConstraints().clear();
    grid_plateau.getColumnConstraints().clear();

    int nb_lignes = jeu.getNbLignes();
    int nb_cols = jeu.getNbColonnes();

    for (int i = 0; i < nb_lignes; i++) {
        RowConstraints rc = new RowConstraints(TAILLE_CASE);
        grid_plateau.getRowConstraints().add(rc);
    }
    for (int j = 0; j < nb_cols; j++) {
        ColumnConstraints cc = new ColumnConstraints(TAILLE_CASE);
        grid_plateau.getColumnConstraints().add(cc);
    }

    for (int l = 0; l < nb_lignes; l++) {
        for (int c = 0; c < nb_cols; c++) {
            Region cellule = creerCellule(jeu.getCase(l, c));
            grid_plateau.add(cellule, c, l);
        }
    }
}
```

---

## CSS — Classes par TypeCase

```css
.case-mur             { -fx-background-color: #444; }
.case-sol             { -fx-background-color: #d4b483; }
.case-cible           { -fx-background-color: #d4b483; /* + indicateur */ }
.case-caisse          { -fx-background-color: #a0522d; }
.case-caisse-cible    { -fx-background-color: #228b22; }  /* visuellement différent */
.case-joueur          { -fx-background-color: #4169e1; }
.case-joueur-cible    { -fx-background-color: #4169e1; }
.case-vide            { -fx-background-color: transparent; }
```

Mapping `TypeCase` → classe CSS à centraliser dans une méthode utilitaire :
```java
private String getCssClass(TypeCase tc) {
    return switch (tc) {
        case MUR              -> "case-mur";
        case SOL              -> "case-sol";
        case CIBLE            -> "case-cible";
        case CAISSE           -> "case-caisse";
        case CAISSE_SUR_CIBLE -> "case-caisse-cible";
        case JOUEUR           -> "case-joueur";
        case JOUEUR_SUR_CIBLE -> "case-joueur-cible";
        case VIDE             -> "case-vide";
    };
}
```

---

## Gestion des fins de partie

```java
private void checkEtat() {
    if (jeu.isNiveauTermine()) {
        if (jeu.estDernierNiveau()) {
            showAlert("Bravo !", "Bravo, dernier niveau réussi !");
        } else {
            showAlert("Niveau terminé !", "Niveau réussi ! Passer au suivant ?");
            jeu.niveauSuivant();
            chargerNiveau();
        }
    } else if (jeu.isPerdu()) {
        showAlertPerdu(); // Annuler ou Recommencer
    }
}
```

---

## Livrables à rendre

1. Archive ZIP du projet (`mvn clean` avant de zipper) — sources + FXML + ressources
2. JAR exécutable `*-shaded.jar` (généré par Maven)
3. Document de synthèse PDF (1-2 pages) :
   - Palier visé
   - Liste des options implémentées (🎮 et 🎨)
   - Présentation rapide de l'interface
   - Paragraphe "Éléments remarquables"

---

## Checklist avant rendu

- [ ] `mvn clean` effectué
- [ ] Aucun `System.out.println` laissé
- [ ] Aucun fichier inutile / code mort
- [ ] JAR shaded généré et testé
- [ ] Toutes les `SokobanException` catchées (pas de crash)
- [ ] EventFilter clavier en place (pas setOnKeyPressed)
- [ ] `peutJouer()` vérifié avant chaque `deplacer()`
- [ ] `peutAnnuler()` vérifié avant chaque `annuler()`
- [ ] Message "dernier niveau réussi" différencié
- [ ] Document de synthèse PDF présent

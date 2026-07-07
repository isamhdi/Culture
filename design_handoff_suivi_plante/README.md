# Handoff: Suivi de croissance plante (chanvre / CBD, sans THC)

## Overview
Petite app mobile de suivi de croissance et floraison pour des plantes de chanvre sans THC. L'utilisateur gère une liste de plantes, chacune associée à un stade (Semis → Croissance → Floraison), voit sa progression et un graphique de croissance (hauteur en cm dans le temps), et peut ajouter de nouvelles plantes.

## About the Design Files
The file in this bundle (`suivi-de-croissance-plante.html`) is a **design reference built in HTML** — a prototype showing the intended look, layout, and interaction behavior. It is not production code to copy directly. The task is to **recreate this design in the target codebase's existing environment** (React Native, SwiftUI, Kotlin/Jetpack Compose, Flutter, or plain React/web — whichever the project already uses), following that environment's established component patterns, navigation, and state management. If no environment exists yet, choose the framework best suited to a mobile app-like experience and implement the design there.

## Fidelity
**High-fidelity.** Colors, typography, spacing, and component states shown are final-intent — recreate pixel-close using the values below, adapted to the target platform's UI primitives.

## Screens / Views

### 1. Liste des plantes (List)
- **Purpose**: Vue d'accueil — voir toutes les plantes et leur stade, filtrer par stade, accéder au détail ou ajouter une plante.
- **Layout**: Colonne pleine hauteur. En-tête (titre + bouton "+"), rangée de filtres scrollable horizontalement, liste verticale de cartes scrollable avec `gap: 12px`, padding latéral 20px.
- **Components**:
  - **Header**: eyebrow mono uppercase "CHANVRE · SANS THC" (12px, letter-spacing 0.12em, couleur verte atténuée) + titre "Mes plantes" (26px/700, Space Grotesk). Bouton "+" carré arrondi 44×44px, radius 14px, fond vert accent, texte noir/sombre.
  - **Filter chips**: pilules ("Toutes", "Semis", "Croissance", "Floraison"), padding 9px 16px, radius 20px, actif = fond vert plein + texte foncé ; inactif = fond surface + bordure 1px.
  - **Plant card**: fond surface (`oklch(0.2 0.015 155)`), bordure 1px, radius 18px, padding 16px. Contenu : avatar carré 44×44 radius 12 (initiale du nom, teinte de la couleur du stade) + nom (16px/600) + variété/cultivar (13px, gris) + badge de stade (pilule, couleur du stade) ; puis barre de progression fine (6px, radius 3) représentant le jour courant dans le stade / durée du stade + compteur mono "J{n}/{durée}" ; puis ligne de stats mono (hauteur en cm, jour total).
  - Empty state par filtre : texte centré gris "Aucune plante dans ce stade."

### 2. Détail d'une plante (Detail)
- **Purpose**: Voir la progression détaillée d'une plante, son historique de croissance, et faire avancer son stade.
- **Layout**: En-tête avec bouton retour (←), nom + variété, bouton supprimer (✕) à droite. Contenu scrollable avec `gap: 20px`.
- **Components**:
  - **Stage stepper**: 3 étapes horizontales (Semis/Croissance/Floraison), point 14px (plein si atteint/actif, contour si futur) relié par une ligne 2px (colorée si complétée), label mono 11px sous chaque point (coloré + gras si actif).
  - **Stats grid**: 3 colonnes égales, cartes radius 14px, padding 12px, centrées : "Jour total", "Hauteur" (cm), "J. de stade" (coloré selon le stade).
  - **Growth chart**: carte radius 16px padding 16px. Titre "Croissance" + libellé axes "cm / jour". SVG 300×140 : 3 lignes de grille pointillées horizontales, aire remplie sous la courbe (couleur du stade à faible opacité), ligne (polyline) 2.5px couleur du stade, point final marqué (cercle plein). Axe X : "J0" à gauche, "J{totalDay}" à droite.
  - **Advance button** (si stade ≠ Floraison) : pleine largeur, radius 14px, fond = couleur du stade, texte "Passer au stade : {stade suivant}".
  - **Done state** (si Floraison) : bandeau centré "Prête pour la récolte", fond violet translucide, bordure violette.

### 3. Ajouter une plante (Add)
- **Purpose**: Créer une nouvelle plante (nom, variété/cultivar, stade de départ).
- **Layout**: En-tête (← + titre "Nouvelle plante"), formulaire vertical `gap: 18px`.
- **Components**:
  - Champs texte (Nom, Variété/cultivar) : label mono 12px uppercase gris au-dessus, input pleine largeur, fond surface, bordure 1px, radius 12px, padding 14px.
  - Sélecteur de stade : 3 boutons égaux en ligne, actif = fond couleur du stade + texte foncé.
  - Bouton "Ajouter la plante" : pleine largeur, radius 14px, désactivé (gris) tant que le nom est vide, activé = vert accent.

## Interactions & Behavior
- Navigation simple à 3 écrans (list / detail / add), pas de routing externe — état local.
- Tap sur une carte plante → écran détail de cette plante.
- Bouton "+" (liste) → écran ajout ; bouton "←" → retour.
- Filtres de la liste : cliquer une pilule filtre les cartes affichées instantanément (pas d'animation requise, mais un fade/scale léger sur le tri est acceptable).
- Détail : bouton "Passer au stade suivant" fait avancer `stage` dans l'ordre Semis → Croissance → Floraison et remet `dayInStage` à 0. Disparaît une fois en Floraison (remplacé par le bandeau "Prête pour la récolte").
- Bouton "✕" (détail) supprime la plante et retourne à la liste.
- Formulaire d'ajout : le bouton de soumission est désactivé tant que le champ "Nom" est vide ; à la soumission, la nouvelle plante est ajoutée en tête de liste avec des valeurs de départ selon le stade choisi (hauteur ≈ 1cm en semis, 20cm en croissance, 60cm en floraison) et l'utilisateur revient à la liste.
- Pas d'animations de transition scriptées dans le prototype — les changements d'écran/état sont instantanés. Sur mobile natif, des transitions standard (push/pop, cross-fade) sont appropriées.

## State Management
- `plants`: liste des plantes, chacune `{ id, name, variety, stage, dayInStage, totalDay, heightCm, heightHistory: number[] }`.
- `screen`: 'list' | 'detail' | 'add'.
- `selectedId`: id de la plante affichée en détail.
- `filter`: stade actif dans la liste ('all' | 'semis' | 'croissance' | 'floraison').
- `form`: `{ name, variety, stage }` pour l'écran d'ajout.
- Données de démonstration : 5 plantes avec des cultivars de chanvre industriel réels et légaux (sans THC) : Fedora 17, Futura 75, Finola, Santhica 27, USO 31 — à remplacer par les vraies données utilisateur/API côté production.

## Design Tokens

### Colors (oklch)
- Fond app : `oklch(0.155 0.014 155)` (presque noir, teinte verte très légère)
- Fond page (hors device) : `oklch(0.13 0.01 155)`
- Surface carte : `oklch(0.2 0.015 155)`
- Bordure carte : `oklch(0.28 0.018 155)` (formulaires : `oklch(0.3 0.018 155)`)
- Texte primaire : `oklch(0.96 0.004 155)`
- Texte secondaire : `oklch(0.65 0.012 155)` / `oklch(0.6 0.012 155)`
- Texte tertiaire / labels : `oklch(0.55 0.012 155)`
- Accent Semis (bleu) : `oklch(0.78 0.13 230)`
- Accent Croissance (vert, aussi couleur d'action primaire) : `oklch(0.78 0.16 145)`
- Accent Floraison (violet/rose) : `oklch(0.78 0.15 320)`
- Texte sur fond accent : `oklch(0.15 0.01 155)`
- Suppression / danger : `oklch(0.68 0.13 25)`
- Variantes translucides (badges, aires de graphique) : même teinte, opacité 0.18–0.35 via `oklch(L C H / alpha)`.

### Typography
- Titres/UI générale : **Space Grotesk** (500/600/700).
- Données numériques / mono (compteurs de jours, hauteurs, libellés d'axes, badges de stade) : **IBM Plex Mono** (400/500/600).
- Échelle : eyebrow 12px, titre écran 26px/700 (liste) ou 19px/700 (détail/ajout), nom de plante 16px/600, sous-titre/variété 13px, corps/labels 13–15px, stats chiffrées 20px/600 (mono), micro-labels 10–11px uppercase.

### Spacing & Radius
- Padding latéral d'écran : 20px.
- Gap entre cartes/blocs : 12–20px selon contexte.
- Radius cartes : 18px (plante), 16px (graphique), 14px (stats, boutons, inputs), 12px (petits éléments), 20px (pilules de filtre — full pill).
- Boutons icône (retour, supprimer, ajouter) : 40–44px carré, radius 12–14px — respecter la cible tactile minimale de 44px sur les actions principales.

### Shadows / Borders
- Pas d'ombres portées ; séparation par bordures 1px translucides (`oklch(0.28–0.3 0.018 155)`) sur fond sombre — esthétique "dashboard technique" plate.

## Assets
Aucune image externe. Avatars de plantes = initiale du nom sur fond teinté (pas de photo). Icônes (flèche retour, croix, plus) dessinées en caractères/texte simples, pas de fichiers SVG externes à récupérer.

Le prototype utilise un habillage "device frame" (bezel iPhone) uniquement pour la présentation du mockup — **ne pas** le recréer dans l'app finale ; il s'agit du vrai écran de l'app, pas d'un élément UI.

## Files
- `suivi-de-croissance-plante.html` — prototype complet (3 écrans, données de démo, logique d'interaction) à consulter comme référence visuelle et comportementale.

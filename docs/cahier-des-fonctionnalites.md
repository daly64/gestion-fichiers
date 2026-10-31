# Cahier des fonctionnalités — Gestion Fichiers

| | |
|---|---|
| **Produit** | Gestion Fichiers, gestionnaire de fichiers pour Android |
| **Version** | 1.0 |
| **Plateforme** | Android 8.0 (API 26) et versions ultérieures |
| **Technologie** | Kotlin, Jetpack Compose, Material 3 |
| **Date** | 31 octobre 2026 |

## 1. Objet du document

Ce document décrit les fonctionnalités de l'application Gestion Fichiers, ses règles de fonctionnement et ses exigences de qualité. Il sert de référence pour le développement, les tests et les évolutions.

## 2. Présentation du produit

**Objectif.** Permettre de gérer les fichiers du téléphone simplement : parcourir, ouvrir, organiser et supprimer, sans risque de perte accidentelle.

**Utilisateur visé.** Un utilisateur de smartphone Android qui veut organiser ses documents, photos et téléchargements.

**Périmètre.** Le stockage interne du téléphone. L'application fonctionne hors ligne et ne transmet aucune donnée.

## 3. Exigences fonctionnelles

| ID | Fonctionnalité | Description | Critères d'acceptation |
|---|---|---|---|
| EF-01 | Navigation | Afficher le contenu d'un dossier en grille ou en liste ; entrer dans un dossier ; remonter au dossier parent. | Dossiers puis fichiers, triés par nom. Le fil d'Ariane permet de revenir à tout dossier parent en un appui. |
| EF-02 | Ouverture de fichier | Ouvrir un fichier avec une application compatible du téléphone. | Un message s'affiche si aucune application ne peut ouvrir le fichier. |
| EF-03 | Sélection | Sélectionner un ou plusieurs éléments, ou tous les éléments du dossier. | Le nombre d'éléments sélectionnés est affiché. Les actions n'apparaissent qu'avec une sélection. |
| EF-04 | Copie | Copier la sélection vers un dossier de destination. | Les dossiers sont copiés avec leur contenu. Un nom déjà présent, ou une copie d'un dossier dans lui-même, est refusé. |
| EF-05 | Déplacement | Déplacer la sélection vers un dossier, par bouton ou par glisser-déposer. | Pendant le glisser-déposer, le dossier visé est mis en évidence. Mêmes refus que pour la copie. |
| EF-06 | Suppression | Supprimer la sélection après confirmation. | Les éléments vont dans la corbeille ; rien n'est effacé définitivement depuis l'écran principal. |
| EF-07 | Création de dossier | Créer un dossier dans le dossier courant. | Un nom vide, contenant « / » ou déjà utilisé est refusé avec un message explicite. |
| EF-08 | Renommage | Renommer un élément sélectionné seul. | Mêmes règles de nom que la création. L'extension est conservée par défaut. |
| EF-09 | Recherche | Rechercher par nom dans le dossier courant et ses sous-dossiers. | Recherche insensible aux majuscules, limitée à 500 résultats. Chaque résultat indique son dossier. |
| EF-10 | Corbeille | Consulter, restaurer ou supprimer définitivement les éléments supprimés. | Restauration à l'emplacement d'origine. Effacement automatique après 30 jours. Suppression définitive et vidage soumis à confirmation. |
| EF-11 | Suivi des opérations | Afficher l'avancement des opérations longues et permettre de les annuler. | Pourcentage calculé sur la taille totale et nom du fichier en cours. L'annulation intervient entre deux fichiers. |

## 4. Interface et interactions

### 4.1 Écran principal

- **Barre supérieure** : nom du dossier et nombre d'éléments, retour, recherche, bouton « Dossier », menu (affichage grille ou liste, corbeille). En mode sélection : compteur, renommer, tout sélectionner.
- **Fil d'Ariane** : chemin du dossier courant, de « Stockage » au dossier ouvert.
- **Zone de contenu** : grille ou liste des éléments, avec une icône par type de fichier (dossier, image, vidéo, audio, PDF, document).
- **Barre inférieure**, selon le contexte : actions sur la sélection (Copier, Déplacer, Supprimer), « Coller ici » pendant une copie ou un déplacement, avancement pendant une opération.

### 4.2 Gestes

| Geste | Effet |
|---|---|
| Appui | Ouvre le dossier ou le fichier. En mode sélection, coche ou décoche l'élément. |
| Appui long | Sélectionne l'élément et active le mode sélection. |
| Appui long puis glisser | Déplace l'élément, ou toute la sélection, vers le dossier visé. |
| Retour | Annule la sélection ou la recherche, sinon remonte au dossier parent. |

### 4.3 Écran Corbeille

Liste des éléments supprimés avec leur date de suppression et leur dossier d'origine. Actions : restaurer, supprimer définitivement, vider la corbeille.

## 5. Exigences non fonctionnelles

| Domaine | Exigence |
|---|---|
| Compatibilité | Android 8.0 (API 26) à Android 15 (API 35). |
| Autorisations | Android 11 et plus : « Accès à tous les fichiers », demandé au premier lancement. Android 8 à 10 : lecture et écriture du stockage. |
| Performance | Opérations sur les fichiers et recherche exécutées en arrière-plan ; l'interface reste utilisable. |
| Sécurité des données | Toute suppression passe par la corbeille. Aucune donnée ne quitte le téléphone. |
| Apparence | Material 3 ; couleurs du système sur Android 12 et plus ; thème sombre pris en charge. |
| Accessibilité | Chaque bouton icône dispose d'une description lisible par les lecteurs d'écran. |

## 6. Distribution

- Chaque modification du code produit automatiquement, via GitHub Actions, une version de test (débogage) et une version release optimisée.
- La version release est signée avec la clé de publication de l'application. Cette clé est conservée hors du dépôt public et doit rester la même pour toutes les mises à jour.

## 7. Hors périmètre et évolutions envisagées

- Aperçu des images et vidéos.
- Tri personnalisé (par date, taille, type).
- Gestion des cartes SD et des clés USB.
- Partage de fichiers vers d'autres applications.
- Synchronisation avec un service en ligne.

## 8. Historique des versions

| Version | Contenu |
|---|---|
| 0.1 | Navigation, ouverture, sélection, copie, déplacement, suppression, création de dossier. |
| 0.2 | Bouton de création de dossier dans la barre supérieure, nouveau design. |
| 0.3 | Icône de l'application, affichage en grille. |
| 0.4 | Icônes de fichiers sans arrière-plan. |
| 0.5 | Renommage, recherche, corbeille, suivi des opérations. |
| 1.0 | Appui simple pour ouvrir, appui long pour sélectionner ; première version release signée. |

## Annexe — Organisation du code

| Fichier | Rôle |
|---|---|
| `MainActivity.kt` | Point d'entrée, demande d'accès aux fichiers. |
| `FileManagerViewModel.kt` | État de l'écran et enchaînement des actions. |
| `FileOps.kt` | Opérations sur les fichiers : lister, copier, déplacer, renommer, rechercher. |
| `Trash.kt` | Corbeille : mise à la corbeille, restauration, effacement. |
| `ui/FileManagerScreen.kt` | Interface : écran principal, corbeille, boîtes de dialogue. |
| `ui/Theme.kt` | Couleurs et thème. |

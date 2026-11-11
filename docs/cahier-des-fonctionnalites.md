# Cahier des fonctionnalités — Gestion Fichiers

| | |
|---|---|
| **Produit** | Gestion Fichiers, gestionnaire de fichiers pour Android |
| **Version** | 2.0 |
| **Plateforme** | Android 8.0 (API 26) et versions ultérieures |
| **Technologie** | Kotlin, Jetpack Compose, Material 3 |
| **Date** | 11 novembre 2026 |

## 1. Objet du document

Ce document décrit les fonctionnalités de l'application Gestion Fichiers, ses règles de fonctionnement et ses exigences de qualité. Il sert de référence pour le développement, les tests et les évolutions.

## 2. Présentation du produit

**Objectif.** Permettre de gérer les fichiers du téléphone simplement : parcourir, ouvrir, organiser et supprimer, sans risque de perte accidentelle.

**Utilisateur visé.** Un utilisateur de smartphone Android qui veut organiser ses documents, photos et téléchargements.

**Périmètre.** Le stockage interne du téléphone, les cartes SD et les clés USB. L'application fonctionne hors ligne et ne transmet aucune donnée.

## 3. Exigences fonctionnelles

| ID | Fonctionnalité | Description | Critères d'acceptation |
|---|---|---|---|
| EF-01 | Navigation | Afficher le contenu d'un dossier en grille ou en liste ; entrer dans un dossier ; remonter au dossier parent. | Dossiers puis fichiers, dans l'ordre de tri choisi (EF-12). Le fil d'Ariane permet de revenir à tout dossier parent en un appui. |
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
| EF-12 | Tri | Trier le contenu par nom, date, taille ou type, en ordre croissant ou décroissant. | Les dossiers restent toujours avant les fichiers. Le tri choisi est conservé à la réouverture de l'application. |
| EF-13 | Partage | Envoyer les fichiers sélectionnés vers une autre application (messagerie, e-mail, Bluetooth…). | Un ou plusieurs fichiers. Les dossiers ne peuvent pas être partagés directement : un message propose de les compresser en ZIP. |
| EF-14 | Cartes SD et clés USB | Parcourir les stockages amovibles branchés au téléphone. | Chaque stockage apparaît dans le menu latéral avec son nom et son espace libre. Un stockage retiré disparaît de la liste. |
| EF-15 | Accès rapide | Ouvrir en un appui les dossiers courants : Téléchargements, Photos (DCIM), Images, Documents, Musique, Vidéos. | Seuls les dossiers existants sont affichés. |
| EF-16 | Favoris | Ajouter un dossier aux favoris, l'ouvrir depuis le menu latéral, le retirer. | Ajout depuis la sélection (un dossier) ou depuis le menu pour le dossier courant. Un favori dont le dossier n'existe plus est signalé. Les favoris sont conservés. |
| EF-17 | Compression ZIP | Compresser la sélection dans une archive ZIP placée dans le dossier courant. | Les dossiers sont compressés avec leur contenu. Si le nom existe déjà, un numéro est ajouté (« Archive (2).zip »). Avancement et annulation comme EF-11 ; une archive annulée est effacée. |
| EF-18 | Extraction ZIP | Extraire une archive ZIP sélectionnée seule. | Le contenu va dans un nouveau dossier portant le nom de l'archive. Les chemins dangereux (« ../ ») sont refusés. Avancement et annulation comme EF-11. |
| EF-19 | Onglets | Ouvrir plusieurs dossiers dans des onglets et passer de l'un à l'autre. | Une copie ou un déplacement commencé dans un onglet peut être collé dans un autre. Un onglet se ferme d'un appui long ; le dernier ne peut pas être fermé. |
| EF-20 | Analyse du stockage | Afficher les dossiers et les fichiers qui prennent le plus de place dans le stockage courant. | Les 20 plus gros dossiers du premier niveau et les 50 plus gros fichiers, avec leur taille et une barre proportionnelle. Un appui ouvre le dossier, ou le dossier qui contient le fichier. La corbeille est comptée à part. |

## 4. Interface et interactions

### 4.1 Écran principal

- **Barre supérieure** : bouton du menu latéral, nom du dossier et nombre d'éléments, recherche, bouton « Dossier », menu (tri, affichage grille ou liste, nouvel onglet, ajouter aux favoris, analyse du stockage, corbeille). En mode sélection : compteur, renommer, favori, tout sélectionner.
- **Onglets** : affichés sous la barre dès qu'il y a plus d'un onglet.
- **Fil d'Ariane** : chemin du dossier courant, du stockage au dossier ouvert.
- **Jauge d'espace** : espace utilisé et espace libre du stockage courant.
- **Zone de contenu** : grille ou liste des éléments. Miniatures pour les images et les vidéos, icône colorée par type pour les autres fichiers. Taille et date sous le nom de chaque fichier ; nombre d'éléments sous chaque dossier.
- **Barre inférieure**, selon le contexte : actions sur la sélection (Copier, Déplacer, Partager, ZIP ou Extraire, Supprimer), « Coller ici » pendant une copie ou un déplacement, avancement pendant une opération.

### 4.2 Menu latéral

Ouvert par le bouton en haut à gauche ou en glissant depuis le bord gauche. Sections : **Stockages** (interne, cartes SD, clés USB), **Accès rapide**, **Favoris**, **Outils** (analyse du stockage, corbeille). Un appui long sur un favori propose de le retirer.

### 4.3 Couleurs par type de fichier

| Type | Couleur |
|---|---|
| Dossier | Ambre |
| Image | Vert |
| Vidéo | Rouge |
| Audio | Violet |
| PDF | Rouge foncé |
| Archive (ZIP, RAR, 7Z…) | Marron |
| Application (APK) | Vert clair |
| Document, tableur, présentation | Bleu |
| Autre | Gris |

### 4.4 Gestes

| Geste | Effet |
|---|---|
| Appui | Ouvre le dossier ou le fichier. En mode sélection, coche ou décoche l'élément. |
| Appui long | Sélectionne l'élément et active le mode sélection. |
| Appui long puis glisser | Déplace l'élément, ou toute la sélection, vers le dossier visé. |
| Retour | Ferme le menu latéral, annule la sélection ou la recherche, sinon remonte au dossier parent. |

### 4.5 Animations

Passage d'un dossier à l'autre en fondu glissé (vers la gauche pour entrer, vers la droite pour remonter). Les éléments ajoutés, retirés ou retriés se déplacent en douceur.

### 4.6 Écran Corbeille

Liste des éléments supprimés avec leur date de suppression et leur dossier d'origine. Actions : restaurer, supprimer définitivement, vider la corbeille.

### 4.7 Écran Analyse du stockage

Jauge du stockage, puis deux listes : « Plus gros dossiers » et « Plus gros fichiers ». Un indicateur s'affiche pendant le calcul, qui se fait en arrière-plan.

## 5. Exigences non fonctionnelles

| Domaine | Exigence |
|---|---|
| Compatibilité | Android 8.0 (API 26) à Android 15 (API 35). |
| Autorisations | Android 11 et plus : « Accès à tous les fichiers », demandé au premier lancement. Android 8 à 10 : lecture et écriture du stockage. |
| Performance | Opérations sur les fichiers, recherche, analyse et miniatures exécutées en arrière-plan ; l'interface reste utilisable. Les miniatures sont mises en cache. |
| Sécurité des données | Toute suppression passe par la corbeille. Aucune donnée ne quitte le téléphone, sauf partage demandé par l'utilisateur. |
| Apparence | Material 3 ; couleurs du système sur Android 12 et plus ; thème sombre pris en charge. |
| Accessibilité | Chaque bouton icône dispose d'une description lisible par les lecteurs d'écran. |
| Préférences | Tri, mode d'affichage et favoris conservés entre deux lancements. |

## 6. Distribution

- Chaque modification du code produit automatiquement, via GitHub Actions, une version de test (débogage) et une version release optimisée.
- La version release est signée avec la clé de publication de l'application. Cette clé est conservée hors du dépôt public et doit rester la même pour toutes les mises à jour.

## 7. Hors périmètre et évolutions envisagées

- Synchronisation avec un service en ligne.
- Double volet côte à côte sur tablette ou en mode paysage.
- Archives autres que ZIP (RAR, 7Z) et archives protégées par mot de passe.

## 8. Historique des versions

| Version | Contenu |
|---|---|
| 0.1 | Navigation, ouverture, sélection, copie, déplacement, suppression, création de dossier. |
| 0.2 | Bouton de création de dossier dans la barre supérieure, nouveau design. |
| 0.3 | Icône de l'application, affichage en grille. |
| 0.4 | Icônes de fichiers sans arrière-plan. |
| 0.5 | Renommage, recherche, corbeille, suivi des opérations. |
| 1.0 | Appui simple pour ouvrir, appui long pour sélectionner ; première version release signée. |
| 2.0 | Tri, partage, cartes SD et clés USB, accès rapide, favoris, ZIP, onglets, analyse du stockage ; miniatures, couleurs par type, taille et date, jauge d'espace, animations. |

## Annexe — Organisation du code

| Fichier | Rôle |
|---|---|
| `MainActivity.kt` | Point d'entrée, demande d'accès aux fichiers. |
| `FileManagerViewModel.kt` | État de l'écran et enchaînement des actions. |
| `FileOps.kt` | Opérations sur les fichiers : lister, trier, copier, déplacer, renommer, rechercher. |
| `Zip.kt` | Compression et extraction des archives ZIP. |
| `Storage.kt` | Stockages (interne, SD, USB), espace libre, accès rapide, analyse du stockage. |
| `Prefs.kt` | Préférences conservées : tri, affichage, favoris. |
| `Trash.kt` | Corbeille : mise à la corbeille, restauration, effacement. |
| `ui/FileManagerScreen.kt` | Écran principal, menu latéral, onglets, boîtes de dialogue. |
| `ui/FileItems.kt` | Cases et lignes de fichiers : miniatures, couleurs, détails. |
| `ui/StorageScreen.kt` | Écran d'analyse du stockage et jauge d'espace. |
| `ui/TrashScreen.kt` | Écran de la corbeille. |
| `ui/Theme.kt` | Couleurs et thème. |

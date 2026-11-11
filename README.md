# Gestion Fichiers

Gestionnaire de fichiers pour Android, écrit en Kotlin avec Jetpack Compose et Material 3.

Parcourir, ouvrir, copier, déplacer (y compris par glisser-déposer), renommer, trier, rechercher, partager et supprimer des fichiers, avec une corbeille qui conserve les éléments supprimés pendant 30 jours.

Version 2.0 : cartes SD et clés USB, accès rapide et favoris, compression et extraction ZIP, onglets, analyse du stockage, miniatures des photos et vidéos, couleurs par type de fichier et jauge d'espace libre.

- Cahier des fonctionnalités : [docs/cahier-des-fonctionnalites.md](docs/cahier-des-fonctionnalites.md)
- Téléchargements : onglet [Releases](../../releases)

## Compiler

Ouvrir le projet dans Android Studio, ou en ligne de commande :

```bash
./gradlew assembleDebug     # version de test
./gradlew assembleRelease   # version release (non signée)
```

Chaque envoi sur `main` compile automatiquement les deux versions via GitHub Actions.

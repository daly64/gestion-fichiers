# Gestion Fichiers

Gestionnaire de fichiers pour Android, écrit en Kotlin avec Jetpack Compose et Material 3.

Parcourir, ouvrir, copier, déplacer (y compris par glisser-déposer), renommer, rechercher et supprimer des fichiers, avec une corbeille qui conserve les éléments supprimés pendant 30 jours.

- Cahier des fonctionnalités : [docs/cahier-des-fonctionnalites.md](docs/cahier-des-fonctionnalites.md)
- Téléchargements : onglet [Releases](../../releases)

## Compiler

Ouvrir le projet dans Android Studio, ou en ligne de commande :

```bash
./gradlew assembleDebug     # version de test
./gradlew assembleRelease   # version release (non signée)
```

Chaque envoi sur `main` compile automatiquement les deux versions via GitHub Actions.

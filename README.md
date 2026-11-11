<p align="center">
  <img src="docs/images/icone.svg" width="112" alt="Icône de Gestion Fichiers">
</p>

<h1 align="center">Gestion Fichiers</h1>

<p align="center">
  Gestionnaire de fichiers pour Android, hors ligne, en Kotlin avec Jetpack Compose et Material 3.<br>
  Version 2.5 · Android 8.0 à 15 · Identité visuelle « Cobalt net »
</p>

## Fonctionnalités

**Parcourir et organiser**
- Navigation en grille ou en liste, fil d'Ariane, onglets, double volet sur écran large
- Tri par nom, date, taille ou type ; recherche dans les sous-dossiers
- Copier, déplacer (aussi par glisser-déposer), renommer, créer des dossiers
- Cartes SD et clés USB, accès rapide (Téléchargements, Photos, Documents…), favoris

**Fichiers**
- Miniatures des photos et vidéos, couleur et badge d'extension par type de fichier
- Ouverture avec l'application adaptée, partage vers d'autres applications
- Archives : compression ZIP ou 7Z, ZIP protégé par mot de passe (AES-256), extraction ZIP, 7Z et RAR (avec mot de passe)

**Sécurité et confort**
- Corbeille : tout fichier supprimé est récupérable pendant 30 jours
- Avancement et annulation des opérations longues
- Analyse du stockage : plus gros dossiers et plus gros fichiers, jauge d'espace libre
- Plein écran au démarrage, thèmes clair et sombre
- Vérification des mises à jour ; aucune donnée ne quitte le téléphone

Détail complet : [cahier des fonctionnalités](docs/cahier-des-fonctionnalites.md) · identité : [identité visuelle](docs/identite-visuelle.md)

## Installer

Télécharger l'APK depuis l'onglet [Releases](../../releases) :

- **Version officielle** (`GestionFichiers-<version>.apk`, étiquette `v<version>`) : signée avec la clé de publication. C'est celle à installer ; les versions suivantes s'installent par-dessus, en gardant favoris et réglages.
- **Version de test** (`GestionFichiers-<version>-test.apk`, étiquette `test-<n>`) : publiée à chaque envoi de code, installable à côté de la version officielle.

Au premier lancement, l'application demande l'autorisation « Accès à tous les fichiers » (Android 11 et plus).

## Compiler

Ouvrir le projet dans Android Studio, ou en ligne de commande :

```bash
./gradlew assembleDebug     # version de test (clé de test fixe)
./gradlew assembleRelease   # version release, optimisée
```

Chaque envoi sur `main` compile les deux versions avec GitHub Actions et les publie dans les Releases.

### Signature de la version officielle

La version release est signée seulement si ces deux secrets existent dans *Settings → Secrets and variables → Actions* :

| Secret | Contenu |
|---|---|
| `SIGNING_KEYSTORE_BASE64` | La clé de publication (`.jks`) encodée en base64 |
| `SIGNING_PASSWORD` | Son mot de passe |

La clé ne doit jamais changer ni être ajoutée au dépôt : sans elle, aucune mise à jour ne peut s'installer par-dessus. Pensez à augmenter `versionCode` dans `app/build.gradle.kts` à chaque nouvelle version.

## Organisation du code

| Fichier | Rôle |
|---|---|
| `MainActivity.kt` | Point d'entrée, accès aux fichiers, plein écran |
| `FileManagerViewModel.kt` | État de l'écran et enchaînement des actions |
| `FileOps.kt` | Lister, trier, copier, déplacer, renommer, rechercher |
| `Archives.kt` | Compression et extraction ZIP, 7Z, RAR, avec mot de passe |
| `Storage.kt` | Stockages, espace libre, accès rapide, analyse du stockage |
| `Trash.kt` | Corbeille |
| `Prefs.kt` | Préférences conservées |
| `Updates.kt` | Vérification des mises à jour |
| `ui/` | Écrans, composants et thème « Cobalt net » |

Bibliothèques : Coil, zip4j, Apache Commons Compress, XZ for Java, Junrar. Police [Manrope](docs/licences/Manrope-OFL.txt) (licence OFL).

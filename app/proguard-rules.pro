# Règles R8 de l'application.
# Jetpack Compose et AndroidX fournissent leurs propres règles ; rien de spécifique ici.

# Bibliothèques d'archives : dépendances optionnelles absentes sur Android
-dontwarn org.brotli.dec.**
-dontwarn com.github.luben.zstd.**
-dontwarn org.objectweb.asm.**
-dontwarn org.osgi.**
-dontwarn org.slf4j.impl.**
-dontwarn javax.annotation.**
-dontwarn java.awt.**
-dontwarn javax.crypto.**

package com.example.gestionfichiers

import android.content.Context

/** EF-12 : critères de tri. Les dossiers restent toujours avant les fichiers. */
enum class SortBy(val label: String) {
    NAME("Nom"),
    DATE("Date"),
    SIZE("Taille"),
    TYPE("Type")
}

/** Préférences conservées entre deux lancements : tri, affichage, favoris (EF-12, EF-16). */
class Prefs(context: Context) {

    private val sp = context.getSharedPreferences("gestion_fichiers", Context.MODE_PRIVATE)

    var sortBy: SortBy
        get() = runCatching { SortBy.valueOf(sp.getString(KEY_SORT, null) ?: "") }.getOrDefault(SortBy.NAME)
        set(value) = sp.edit().putString(KEY_SORT, value.name).apply()

    var sortDescending: Boolean
        get() = sp.getBoolean(KEY_SORT_DESC, false)
        set(value) = sp.edit().putBoolean(KEY_SORT_DESC, value).apply()

    var gridView: Boolean
        get() = sp.getBoolean(KEY_GRID, true)
        set(value) = sp.edit().putBoolean(KEY_GRID, value).apply()

    /** EF-22 : double volet sur écran large. */
    var dualPane: Boolean
        get() = sp.getBoolean(KEY_DUAL, true)
        set(value) = sp.edit().putBoolean(KEY_DUAL, value).apply()

    /** EF-23 : dernière vérification automatique (millisecondes) et version que l'utilisateur a choisi d'ignorer. */
    var lastUpdateCheck: Long
        get() = sp.getLong(KEY_LAST_CHECK, 0L)
        set(value) = sp.edit().putLong(KEY_LAST_CHECK, value).apply()

    var skippedVersion: String?
        get() = sp.getString(KEY_SKIPPED, null)
        set(value) = sp.edit().putString(KEY_SKIPPED, value).apply()

    /** Chemins absolus des dossiers favoris, dans l'ordre d'ajout. */
    var favorites: List<String>
        get() = sp.getString(KEY_FAVORITES, null)
            ?.split('\n')
            ?.filter { it.isNotBlank() }
            ?: emptyList()
        set(value) = sp.edit().putString(KEY_FAVORITES, value.joinToString("\n")).apply()

    private companion object {
        const val KEY_SORT = "sort_by"
        const val KEY_SORT_DESC = "sort_desc"
        const val KEY_GRID = "grid_view"
        const val KEY_FAVORITES = "favorites"
        const val KEY_DUAL = "dual_pane"
        const val KEY_LAST_CHECK = "last_update_check"
        const val KEY_SKIPPED = "skipped_version"
    }
}

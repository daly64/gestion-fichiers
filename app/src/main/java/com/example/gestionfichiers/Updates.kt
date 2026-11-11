package com.example.gestionfichiers

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** Nouvelle version publiée sur GitHub (EF-23). */
data class UpdateInfo(val version: String, val apkUrl: String, val pageUrl: String)

/**
 * EF-23 : vérification des mises à jour sur la page des publications GitHub du projet.
 * Seule communication réseau de l'application : une lecture publique, sans aucune donnée personnelle.
 */
object Updates {

    private const val LATEST_URL = "https://api.github.com/repos/daly64/gestion-fichiers/releases/latest"
    const val RELEASES_PAGE = "https://github.com/daly64/gestion-fichiers/releases"

    /** Dernière version officielle, ou null s'il n'y en a pas encore. Lève une exception sans connexion. */
    fun fetchLatest(): UpdateInfo? {
        val conn = (URL(LATEST_URL).openConnection() as HttpURLConnection).apply {
            connectTimeout = 10_000
            readTimeout = 10_000
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "GestionFichiers")
        }
        try {
            if (conn.responseCode == HttpURLConnection.HTTP_NOT_FOUND) return null
            check(conn.responseCode == HttpURLConnection.HTTP_OK) { "Réponse ${conn.responseCode}" }
            val json = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
            val version = json.optString("tag_name").removePrefix("v")
            val page = json.optString("html_url", RELEASES_PAGE)
            val assets = json.optJSONArray("assets")
            var apk = ""
            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val a = assets.getJSONObject(i)
                    if (a.optString("name").endsWith(".apk")) {
                        apk = a.optString("browser_download_url")
                        break
                    }
                }
            }
            if (version.isEmpty()) return null
            return UpdateInfo(version, apk.ifEmpty { page }, page)
        } finally {
            conn.disconnect()
        }
    }

    /** « 2.10 » est plus récent que « 2.9 » : comparaison nombre par nombre. */
    fun isNewer(remote: String, current: String): Boolean {
        fun parts(v: String) = v.substringBefore('-').split('.').map { it.toIntOrNull() ?: 0 }
        val r = parts(remote)
        val c = parts(current)
        for (i in 0 until maxOf(r.size, c.size)) {
            val a = r.getOrElse(i) { 0 }
            val b = c.getOrElse(i) { 0 }
            if (a != b) return a > b
        }
        return false
    }
}

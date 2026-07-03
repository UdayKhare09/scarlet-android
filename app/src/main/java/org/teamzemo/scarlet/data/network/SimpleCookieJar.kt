package org.teamzemo.scarlet.data.network

import android.content.Context
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import org.json.JSONArray
import org.json.JSONObject

class SimpleCookieJar(context: Context) : CookieJar {
    private val sharedPrefs = try {
        val masterKey = androidx.security.crypto.MasterKey.Builder(context)
            .setKeyScheme(androidx.security.crypto.MasterKey.KeyScheme.AES256_GCM)
            .build()
        androidx.security.crypto.EncryptedSharedPreferences.create(
            context,
            "scarlet_secure_cookies",
            masterKey,
            androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        context.getSharedPreferences("scarlet_cookies", Context.MODE_PRIVATE)
    }
    private val cookieStore = mutableListOf<Cookie>()

    init {
        val savedCookiesJson = sharedPrefs.getString("cookies", null)
        if (savedCookiesJson != null) {
            try {
                val array = JSONArray(savedCookiesJson)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val name = obj.getString("name")
                    val value = obj.getString("value")
                    val expiresAt = obj.getLong("expiresAt")
                    val domain = obj.getString("domain")
                    val path = obj.getString("path")
                    val secure = obj.getBoolean("secure")
                    val httpOnly = obj.getBoolean("httpOnly")

                    val builder = Cookie.Builder()
                        .name(name)
                        .value(value)
                        .expiresAt(expiresAt)
                        .domain(domain)
                        .path(path)
                    if (secure) builder.secure()
                    if (httpOnly) builder.httpOnly()

                    cookieStore.add(builder.build())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @Synchronized
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val now = System.currentTimeMillis()
        cookieStore.removeAll { old -> cookies.any { new -> new.name == old.name } }
        cookieStore.addAll(cookies)
        cookieStore.removeAll { it.expiresAt < now }
        saveToPrefs()
    }

    @Synchronized
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val now = System.currentTimeMillis()
        cookieStore.removeAll { it.expiresAt < now }
        return cookieStore.filter { it.matches(url) }
    }

    @Synchronized
    fun clear() {
        cookieStore.clear()
        sharedPrefs.edit().clear().apply()
    }

    private fun saveToPrefs() {
        val array = JSONArray()
        for (cookie in cookieStore) {
            val obj = JSONObject()
            obj.put("name", cookie.name)
            obj.put("value", cookie.value)
            obj.put("expiresAt", cookie.expiresAt)
            obj.put("domain", cookie.domain)
            obj.put("path", cookie.path)
            obj.put("secure", cookie.secure)
            obj.put("httpOnly", cookie.httpOnly)
            array.put(obj)
        }
        sharedPrefs.edit().putString("cookies", array.toString()).apply()
    }
}

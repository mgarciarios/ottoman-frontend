package ar.edu.uade.ottoman.data

import android.content.Context
import ar.edu.uade.ottoman.domain.HistoryItem
import org.json.JSONArray
import org.json.JSONObject

/** Persistencia aislada para poder probar el simulador con almacenamiento en memoria. */
interface HistoryStore {
    fun read(): List<HistoryItem>
    fun write(items: List<HistoryItem>)
}

class SharedPreferencesHistoryStore(context: Context) : HistoryStore {
    private val preferences = context.applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun read(): List<HistoryItem> = try {
        val array = JSONArray(preferences.getString(HISTORY_KEY, "[]"))
        (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            HistoryItem(item.optString("id"), item.optString("title"), item.optString("detail"), item.optLong("timestamp"))
        }
    } catch (_: Exception) {
        emptyList()
    }

    override fun write(items: List<HistoryItem>) {
        val array = JSONArray()
        items.forEach { item ->
            array.put(JSONObject().put("id", item.id).put("title", item.title).put("detail", item.detail).put("timestamp", item.timestamp))
        }
        preferences.edit().putString(HISTORY_KEY, array.toString()).apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "ottoman_history"
        const val HISTORY_KEY = "events"
    }
}

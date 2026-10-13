package com.example.data.local

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

data class SavedPersonItem(
    val id: Int,
    val name: String,
    val profilePath: String?,
    val department: String? = "Acting",
    val savedAt: Long = System.currentTimeMillis()
) {
    val fullProfileUrl: String?
        get() = profilePath?.let { if (it.startsWith("http")) it else "https://image.tmdb.org/t/p/w342$it" }
}

class SavedPersonStore(context: Context) {
    private val prefs = context.getSharedPreferences("saved_people_prefs", Context.MODE_PRIVATE)
    private val keySavedPeople = "saved_people_ids_set"
    private val keySavedPeopleDetails = "saved_people_details_json"

    private val _savedPersonIds = MutableStateFlow<Set<Int>>(loadSavedPersonIds())
    val savedPersonIds: StateFlow<Set<Int>> = _savedPersonIds.asStateFlow()

    private val _savedPeopleList = MutableStateFlow<List<SavedPersonItem>>(loadSavedPeopleList())
    val savedPeopleList: StateFlow<List<SavedPersonItem>> = _savedPeopleList.asStateFlow()

    private fun loadSavedPersonIds(): Set<Int> {
        val stringSet = prefs.getStringSet(keySavedPeople, emptySet()) ?: emptySet()
        return stringSet.mapNotNull { it.toIntOrNull() }.toSet()
    }

    private fun loadSavedPeopleList(): List<SavedPersonItem> {
        val raw = prefs.getString(keySavedPeopleDetails, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<SavedPersonItem>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    SavedPersonItem(
                        id = obj.getInt("id"),
                        name = obj.optString("name", "Unknown"),
                        profilePath = obj.optString("profile_path").takeIf { it.isNotBlank() && it != "null" },
                        department = obj.optString("department", "Acting"),
                        savedAt = obj.optLong("saved_at", System.currentTimeMillis())
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun isPersonSaved(personId: Int): Boolean {
        return _savedPersonIds.value.contains(personId)
    }

    fun toggleSavePerson(
        personId: Int,
        name: String = "",
        profilePath: String? = null,
        department: String? = "Acting"
    ): Boolean {
        val currentIds = _savedPersonIds.value.toMutableSet()
        val currentList = _savedPeopleList.value.toMutableList()

        val isNowSaved = if (currentIds.contains(personId)) {
            currentIds.remove(personId)
            currentList.removeAll { it.id == personId }
            false
        } else {
            currentIds.add(personId)
            currentList.removeAll { it.id == personId }
            currentList.add(
                0,
                SavedPersonItem(
                    id = personId,
                    name = name.ifBlank { "Actor $personId" },
                    profilePath = profilePath,
                    department = department ?: "Acting",
                    savedAt = System.currentTimeMillis()
                )
            )
            true
        }

        _savedPersonIds.value = currentIds
        _savedPeopleList.value = currentList

        // Persist IDs
        prefs.edit().putStringSet(keySavedPeople, currentIds.map { it.toString() }.toSet()).apply()

        // Persist Details JSON
        try {
            val jsonArr = JSONArray()
            for (item in currentList) {
                val obj = JSONObject()
                obj.put("id", item.id)
                obj.put("name", item.name)
                obj.put("profile_path", item.profilePath ?: "")
                obj.put("department", item.department ?: "Acting")
                obj.put("saved_at", item.savedAt)
                jsonArr.put(obj)
            }
            prefs.edit().putString(keySavedPeopleDetails, jsonArr.toString()).apply()
        } catch (_: Exception) {}

        return isNowSaved
    }
}

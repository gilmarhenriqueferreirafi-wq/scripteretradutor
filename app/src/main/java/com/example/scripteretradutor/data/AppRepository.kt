package com.example.scripteretradutor.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class Project(
    val id: String,
    val title: String,
    val units: List<WritingUnit>,
    val tags: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val notes: List<ProjectNote> = emptyList()
)
data class WritingUnit(val id: String, val title: String, val content: String, val note: String = "")
data class ProjectNote(val id: String, val text: String, val type: String = "Ideia", val done: Boolean = false)
data class TextVersion(val id: String, val projectId: String, val unitId: String, val content: String, val createdAt: Long)

/** Persistência local inicial. Pode ser substituída por Room sem alterar a UI. */
class AppRepository(context: Context) {
    private val preferences = context.getSharedPreferences("scripter_data", Context.MODE_PRIVATE)
    @Volatile private var projectsCache: List<Project>? = null

    fun loadProjects(): List<Project> {
        projectsCache?.let { return it }
        val raw = preferences.getString(KEY_PROJECTS, null) ?: return emptyList()
        val loaded = runCatching {
            val array = JSONArray(raw)
            List(array.length()) { index ->
                val project = array.getJSONObject(index)
                val unitsJson = project.optJSONArray("units") ?: JSONArray()
                Project(project.getString("id"), project.getString("title"), List(unitsJson.length()) { i ->
                    val unit = unitsJson.getJSONObject(i)
                    WritingUnit(unit.getString("id"), unit.getString("title"), unit.optString("content"), unit.optString("note"))
                }, tags = project.optJSONArray("tags")?.let { tags -> List(tags.length()) { tags.getString(it) } } ?: emptyList(),
                    isFavorite = project.optBoolean("favorite", false),
                    isArchived = project.optBoolean("archived", false),
                    notes = project.optJSONArray("notes")?.let { notes -> List(notes.length()) {
                        val note = notes.getJSONObject(it)
                        ProjectNote(note.getString("id"), note.getString("text"), note.optString("type", "Ideia"), note.optBoolean("done", false))
                    } } ?: emptyList())
            }
        }.getOrDefault(emptyList())
        projectsCache = loaded
        return loaded
    }

    fun createProject(title: String, template: String = "Livre"): Project {
        val unitTitles = when (template) {
            "Três Atos" -> listOf("Ato 1 — Apresentação", "Ato 2 — Confronto", "Ato 3 — Desfecho")
            "Jornada do Herói" -> listOf("Mundo comum", "Chamado à aventura", "Recusa do chamado", "Encontro com o mentor", "Travessia do limiar", "Provas e aliados", "Provação", "Recompensa", "Caminho de volta", "Ressurreição", "Retorno com o elixir")
            else -> listOf("Capítulo 1")
        }
        val project = Project(UUID.randomUUID().toString(), title, unitTitles.map(::newUnit))
        save(loadProjects() + project); return project
    }

    fun addUnit(projectId: String, title: String): WritingUnit {
        val unit = newUnit(title)
        save(loadProjects().map { if (it.id == projectId) it.copy(units = it.units + unit) else it })
        return unit
    }

    fun deleteUnit(projectId: String, unitId: String) {
        save(loadProjects().map { project ->
            if (project.id == projectId) project.copy(units = project.units.filterNot { it.id == unitId }) else project
        })
    }

    fun updateUnit(projectId: String, unitId: String, content: String) {
        save(loadProjects().map { project ->
            if (project.id != projectId) project else project.copy(
                units = project.units.map { if (it.id == unitId) it.copy(content = content) else it }
            )
        })
    }

    fun updateUnitNote(projectId: String, unitId: String, note: String) {
        save(loadProjects().map { project ->
            if (project.id != projectId) project else project.copy(units = project.units.map { if (it.id == unitId) it.copy(note = note) else it })
        })
    }

    fun updateTags(projectId: String, tags: List<String>) {
        save(loadProjects().map { if (it.id == projectId) it.copy(tags = tags.distinct()) else it })
    }

    fun addNote(projectId: String, text: String, type: String) {
        val note = ProjectNote(UUID.randomUUID().toString(), text, type)
        save(loadProjects().map { if (it.id == projectId) it.copy(notes = it.notes + note) else it })
    }

    fun toggleNote(projectId: String, noteId: String) {
        save(loadProjects().map { project ->
            if (project.id == projectId) project.copy(notes = project.notes.map { if (it.id == noteId) it.copy(done = !it.done) else it }) else project
        })
    }

    fun updateProjectState(projectId: String, favorite: Boolean? = null, archived: Boolean? = null) {
        save(loadProjects().map { if (it.id == projectId) it.copy(isFavorite = favorite ?: it.isFavorite, isArchived = archived ?: it.isArchived) else it })
    }

    fun duplicateProject(projectId: String): Project? {
        val source = loadProjects().firstOrNull { it.id == projectId } ?: return null
        val copy = source.copy(id = UUID.randomUUID().toString(), title = "${source.title} (cópia)", units = source.units.map { it.copy(id = UUID.randomUUID().toString()) })
        save(loadProjects() + copy)
        return copy
    }

    fun deleteProject(projectId: String) {
        save(loadProjects().filterNot { it.id == projectId })
    }

    fun saveVersion(projectId: String, unitId: String, content: String) {
        val versions = loadVersions().filterNot { it.projectId == projectId && it.unitId == unitId && it.content == content } +
            TextVersion(UUID.randomUUID().toString(), projectId, unitId, content, System.currentTimeMillis())
        val array = JSONArray()
        versions.takeLast(50).forEach { version -> array.put(JSONObject().apply {
            put("id", version.id); put("projectId", version.projectId); put("unitId", version.unitId); put("content", version.content); put("createdAt", version.createdAt)
        }) }
        preferences.edit().putString(KEY_VERSIONS, array.toString()).apply()
    }

    fun loadVersions(): List<TextVersion> {
        val raw = preferences.getString(KEY_VERSIONS, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            List(array.length()) { val item = array.getJSONObject(it); TextVersion(item.getString("id"), item.getString("projectId"), item.getString("unitId"), item.optString("content"), item.optLong("createdAt")) }
        }.getOrDefault(emptyList())
    }

    fun exportBackup(): String = preferences.getString(KEY_PROJECTS, "[]") ?: "[]"
    fun importBackup(json: String): Boolean = runCatching {
        val array = JSONArray(json)
        preferences.edit().putString(KEY_PROJECTS, json).apply()
        projectsCache = null
        array.length() >= 0
    }.getOrDefault(false)

    fun isReminderEnabled(): Boolean = preferences.getBoolean(KEY_REMINDER, false)
    fun setReminderEnabled(enabled: Boolean) { preferences.edit().putBoolean(KEY_REMINDER, enabled).apply() }

    fun isDarkTheme(): Boolean = preferences.getBoolean(KEY_DARK_THEME, true)
    fun setDarkTheme(enabled: Boolean) { preferences.edit().putBoolean(KEY_DARK_THEME, enabled).apply() }

    fun loadTagCatalog(): List<String> = (preferences.getStringSet(KEY_TAGS, null)?.toList() ?: DEFAULT_TAGS).sorted()
    fun loadCustomTags(): List<String> = loadTagCatalog().filterNot { it in DEFAULT_TAGS }
    fun addCatalogTag(tag: String) { if (tag.isNotBlank()) preferences.edit().putStringSet(KEY_TAGS, (loadTagCatalog() + tag.trim()).toSet()).apply() }
    fun removeCatalogTag(tag: String) { if (tag !in DEFAULT_TAGS) preferences.edit().putStringSet(KEY_TAGS, loadTagCatalog().filterNot { it == tag }.toSet()).apply() }

    fun loadIgnoredWords(): List<String> = (preferences.getStringSet(KEY_IGNORED_WORDS, emptySet()) ?: emptySet()).sorted()
    fun addIgnoredWord(word: String) {
        val normalized = word.trim()
        if (normalized.isNotBlank()) preferences.edit().putStringSet(KEY_IGNORED_WORDS, (loadIgnoredWords() + normalized).toSet()).apply()
    }
    fun removeIgnoredWord(word: String) {
        preferences.edit().putStringSet(KEY_IGNORED_WORDS, loadIgnoredWords().filterNot { it == word }.toSet()).apply()
    }

    private fun newUnit(title: String) = WritingUnit(UUID.randomUUID().toString(), title, "")

    private fun save(projects: List<Project>) {
        val array = JSONArray()
        projects.forEach { project -> array.put(JSONObject().apply {
            put("id", project.id); put("title", project.title)
            put("favorite", project.isFavorite); put("archived", project.isArchived)
            put("tags", JSONArray(project.tags))
            put("notes", JSONArray().apply { project.notes.forEach { note -> put(JSONObject().apply { put("id", note.id); put("text", note.text); put("type", note.type); put("done", note.done) }) } })
            put("units", JSONArray().apply { project.units.forEach { unit -> put(JSONObject().apply {
                put("id", unit.id); put("title", unit.title); put("content", unit.content); put("note", unit.note)
            }) } })
        }) }
        projectsCache = projects
        preferences.edit().putString(KEY_PROJECTS, array.toString()).apply()
    }

    private companion object {
        const val KEY_PROJECTS = "projects"; const val KEY_VERSIONS = "versions"; const val KEY_REMINDER = "reminder"; const val KEY_DARK_THEME = "dark_theme"; const val KEY_TAGS = "tag_catalog"; const val KEY_IGNORED_WORDS = "ignored_words"
        val DEFAULT_TAGS = listOf("YouTube", "Instagram", "TikTok", "Terror", "Comédia", "Romance", "Drama", "Ação", "Livre", "+16", "NSFW/+18")
    }

}

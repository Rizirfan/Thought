package com.thoughts.app.data

import com.thoughts.app.domain.model.ScriptProject
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.io.File

actual object LocalStorage {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = true
        prettyPrint = false
    }

    private val storageDir: File by lazy {
        val home = System.getProperty("user.home")
        File(home, ".thoughts_studio").apply { mkdirs() }
    }

    private fun fileFor(projectId: String): File {
        return File(storageDir, "project_$projectId.json")
    }

    actual suspend fun loadProject(projectId: String): ScriptProject? {
        return try {
            val file = fileFor(projectId)
            if (!file.exists()) return null
            val jsonText = file.readText()
            json.decodeFromString<ScriptProject>(jsonText)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    actual suspend fun loadAllProjects(): List<ScriptProject> {
        return try {
            if (!storageDir.exists()) return emptyList()
            val files = storageDir.listFiles { _, name -> name.startsWith("project_") && name.endsWith(".json") } ?: emptyArray()
            files.mapNotNull { file ->
                try {
                    val text = file.readText()
                    json.decodeFromString<ScriptProject>(text)
                } catch (e: Exception) {
                    null
                }
            }.sortedByDescending { it.updatedAt }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    actual suspend fun saveProject(project: ScriptProject) {
        try {
            if (!storageDir.exists()) storageDir.mkdirs()
            val file = fileFor(project.id)
            val jsonText = json.encodeToString(project)
            file.writeText(jsonText)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual suspend fun deleteProject(projectId: String) {
        try {
            val file = fileFor(projectId)
            if (file.exists()) file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual suspend fun loadDefaultProject(): ScriptProject? {
        return loadAllProjects().firstOrNull()
    }
}

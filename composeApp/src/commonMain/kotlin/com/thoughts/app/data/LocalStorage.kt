package com.thoughts.app.data

import com.thoughts.app.domain.model.ScriptProject

expect object LocalStorage {
    suspend fun loadProject(projectId: String): ScriptProject?
    suspend fun loadAllProjects(): List<ScriptProject>
    suspend fun saveProject(project: ScriptProject)
    suspend fun deleteProject(projectId: String)
    suspend fun loadDefaultProject(): ScriptProject?
}

package com.thoughts.app.data.repository

import com.thoughts.app.data.LocalStorage
import com.thoughts.app.domain.engine.FountainParser
import com.thoughts.app.domain.engine.ScreenplayRules
import com.thoughts.app.domain.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ScreenplayRepository {

    private val repoScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val initialProject = newBlankProject()

    private val _projects = MutableStateFlow<List<ScriptProject>>(listOf(initialProject))
    val projects: StateFlow<List<ScriptProject>> = _projects.asStateFlow()

    private val _activeProjectId = MutableStateFlow<String>(initialProject.id)
    val activeProjectId: StateFlow<String> = _activeProjectId.asStateFlow()

    private val _currentProject = MutableStateFlow(initialProject)
    val currentProject: StateFlow<ScriptProject> = _currentProject.asStateFlow()

    private val _activeSceneIndex = MutableStateFlow(0)
    val activeSceneIndex: StateFlow<Int> = _activeSceneIndex.asStateFlow()

    init {
        loadSavedProjects()
    }

    private fun loadSavedProjects() {
        repoScope.launch {
            try {
                val loaded = LocalStorage.loadAllProjects()
                if (loaded.isNotEmpty()) {
                    _projects.value = loaded
                    _currentProject.value = loaded.first()
                    _activeProjectId.value = loaded.first().id
                    _activeSceneIndex.value = 0
                } else {
                    LocalStorage.saveProject(initialProject)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ---------------------------------------------------------------------
    // Project lifecycle
    // ---------------------------------------------------------------------

    private fun newBlankProject(): ScriptProject {
        val draftId = "draft_${randomSuffix()}"
        val draft = ScriptDraft(
            id = draftId,
            name = "First Draft",
            versionNumber = 1,
            scenes = emptyList()
        )

        return ScriptProject(
            id = "proj_${randomSuffix()}",
            title = "My Screenplay",
            author = "",
            logline = "",
            drafts = listOf(draft),
            activeDraftId = draftId,
            characters = emptyList(),
            createdAt = now(),
            updatedAt = now()
        )
    }

    fun createNamedProject(title: String, author: String = ""): ScriptProject {
        val draftId = "draft_${randomSuffix()}"
        val draft = ScriptDraft(
            id = draftId,
            name = "First Draft",
            versionNumber = 1,
            scenes = emptyList()
        )

        val project = ScriptProject(
            id = "proj_${randomSuffix()}",
            title = title.trim().ifBlank { "New Screenplay" },
            author = author,
            logline = "",
            drafts = listOf(draft),
            activeDraftId = draftId,
            characters = emptyList(),
            createdAt = now(),
            updatedAt = now()
        )

        _projects.value = listOf(project) + _projects.value.filterNot { it.id == project.id }
        _currentProject.value = project
        _activeProjectId.value = project.id
        _activeSceneIndex.value = 0

        repoScope.launch {
            try {
                LocalStorage.saveProject(project)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return project
    }

    /** Creates another empty screenplay and makes it current. */
    fun createBlankProject(): ScriptProject {
        val project = newBlankProject()
        _projects.value = listOf(project) + _projects.value.filterNot { it.id == project.id }
        _currentProject.value = project
        _activeProjectId.value = project.id
        _activeSceneIndex.value = 0

        repoScope.launch {
            try {
                LocalStorage.saveProject(project)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return project
    }

    fun openProject(projectId: String) {
        val project = _projects.value.find { it.id == projectId }
        if (project != null) {
            _currentProject.value = project
            _activeProjectId.value = project.id
            _activeSceneIndex.value = 0
            return
        }

        repoScope.launch {
            try {
                val loaded = LocalStorage.loadProject(projectId)
                if (loaded != null) {
                    _projects.value = listOf(loaded) + _projects.value.filterNot { it.id == loaded.id }
                    _currentProject.value = loaded
                    _activeProjectId.value = loaded.id
                    _activeSceneIndex.value = 0
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteProject(projectId: String) {
        val remaining = _projects.value.filterNot { it.id == projectId }
        _projects.value = remaining

        repoScope.launch {
            try {
                LocalStorage.deleteProject(projectId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (_activeProjectId.value == projectId) {
            val next = remaining.firstOrNull()
            if (next != null) {
                openProject(next.id)
            } else {
                createBlankProject()
            }
        }
    }

    fun duplicateProject(projectId: String) {
        val source = _projects.value.find { it.id == projectId } ?: return
        val copy = source.copy(
            id = "proj_${randomSuffix()}",
            title = "${source.title} COPY",
            createdAt = now(),
            updatedAt = now()
        )
        _projects.value = listOf(copy) + _projects.value

        repoScope.launch {
            try {
                LocalStorage.saveProject(copy)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        openProject(copy.id)
    }

    fun saveCurrentProject(onComplete: ((Boolean) -> Unit)? = null) {
        val current = _currentProject.value
        repoScope.launch {
            try {
                LocalStorage.saveProject(current)
                onComplete?.invoke(true)
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete?.invoke(false)
            }
        }
    }

    fun updateProjectDetails(title: String, author: String, logline: String) {
        updateActive { current ->
            current.copy(
                title = title.uppercase(),
                author = author,
                logline = logline,
                updatedAt = now()
            )
        }
    }

    fun selectScene(index: Int) {
        val scenes = _currentProject.value.activeDraft?.scenes.orEmpty()
        if (index in scenes.indices) {
            _activeSceneIndex.value = index
        }
    }

    // ---------------------------------------------------------------------
    // Scene editing
    // ---------------------------------------------------------------------

    fun updateSceneRawText(sceneIndex: Int, newRawText: String) {
        updateScene(sceneIndex) { oldScene ->
            val blocks = FountainParser.parseToBlocks(newRawText)
            oldScene.copy(
                blocks = blocks,
                estimatedPageDuration = ScreenplayRules.calculatePageEstimate(blocks.size, newRawText.length)
            )
        }
    }

    fun updateSceneMeta(sceneIndex: Int, transform: (ScriptScene) -> ScriptScene) {
        updateScene(sceneIndex) { scene -> transform(scene) }
    }

    fun updateBlock(sceneIndex: Int, blockId: String, text: String) {
        updateBlocks(sceneIndex) { blocks ->
            blocks.map { if (it.id == blockId) it.copy(text = text) else it }
        }
    }

    fun changeBlockType(sceneIndex: Int, blockId: String, type: ElementType) {
        updateBlocks(sceneIndex) { blocks ->
            blocks.map {
                if (it.id == blockId) {
                    it.copy(
                        type = type,
                        text = when (type) {
                            ElementType.CHARACTER, ElementType.SCENE_HEADING, ElementType.TRANSITION, ElementType.SHOT ->
                                it.text.uppercase()
                            ElementType.PARENTHETICAL -> ScreenplayRules.formatCasing(it.text, type)
                            else -> it.text
                        }
                    )
                } else {
                    it
                }
            }
        }
    }

    /** Inserts a block after [afterBlockId] and returns the new block id. */
    fun insertBlockAfter(
        sceneIndex: Int,
        afterBlockId: String?,
        type: ElementType,
        text: String = ""
    ): String {
        val newId = "blk_${randomSuffix()}"
        updateBlocks(sceneIndex) { blocks ->
            val index = blocks.indexOfFirst { it.id == afterBlockId }
            val insertAt = if (index < 0) blocks.size else index + 1
            val mutable = blocks.toMutableList()
            mutable.add(insertAt, ScriptElementBlock(id = newId, type = type, text = text))
            mutable
        }
        return newId
    }

    /** Appends a block at the end of the scene and returns the new block id. */
    fun appendBlock(sceneIndex: Int, type: ElementType, text: String = ""): String {
        return insertBlockAfter(sceneIndex, null, type, text)
    }

    fun removeBlock(sceneIndex: Int, blockId: String) {
        updateBlocks(sceneIndex) { blocks -> blocks.filterNot { it.id == blockId } }
    }

    /**
     * Adds a scene containing only its slugline. No placeholder action, character
     * or dialogue: the writer starts from a clean page, exactly like the blank
     * opening project.
     */
    fun addScene(heading: String = "INT. NEW LOCATION - DAY", act: Int = 1) {
        updateActive { project ->
            val draft = project.activeDraft ?: return@updateActive project
            val scenes = draft.scenes.toMutableList()
            val newNumber = scenes.size + 1

            val upper = heading.uppercase()
            val headingType = if (upper.startsWith("INT./EXT.")) HeadingType.INT_EXT
                else if (upper.startsWith("EXT.")) HeadingType.EXT
                else HeadingType.INT
            val prefix = when (headingType) {
                HeadingType.INT -> "INT."
                HeadingType.EXT -> "EXT."
                HeadingType.INT_EXT -> "INT./EXT."
            }
            val rest = heading.substringAfter(prefix).trim()
            val parts = rest.split("-", limit = 2)
            val location = parts.firstOrNull()?.trim() ?: ""
            val timeOfDay = parts.getOrNull(1)?.trim() ?: "DAY"

            val headingBlock = ScriptElementBlock(
                id = "blk_${randomSuffix()}",
                type = ElementType.SCENE_HEADING,
                text = heading
            )
            val newScene = ScriptScene(
                id = "scn_${randomSuffix()}",
                sceneNumber = newNumber,
                headingType = headingType,
                location = location,
                timeOfDay = timeOfDay,
                synopsis = "",
                actNumber = act,
                blocks = listOf(headingBlock),
                estimatedPageDuration = ScreenplayRules.calculatePageEstimate(1, heading.length)
            )
            scenes.add(newScene)

            val updatedDraft = draft.copy(scenes = scenes)
            val drafts = project.drafts.map { if (it.id == draft.id) updatedDraft else it }
            project.copy(drafts = drafts, updatedAt = now())
        }
        _activeSceneIndex.value = (_currentProject.value.activeDraft?.scenes?.size ?: 1) - 1
    }

    fun deleteScene(sceneIndex: Int) {
        updateActive { project ->
            val draft = project.activeDraft ?: return@updateActive project
            if (sceneIndex !in draft.scenes.indices) return@updateActive project
            val scenes = draft.scenes.toMutableList()
            scenes.removeAt(sceneIndex)
            val renumbered = scenes.mapIndexed { index, scene -> scene.copy(sceneNumber = index + 1) }
            val updatedDraft = draft.copy(scenes = renumbered)
            val drafts = project.drafts.map { if (it.id == draft.id) updatedDraft else it }
            project.copy(drafts = drafts, updatedAt = now())
        }
        val size = _currentProject.value.activeDraft?.scenes?.size ?: 0
        _activeSceneIndex.value = (sceneIndex - 1).coerceIn(0, (size - 1).coerceAtLeast(0))
    }

    fun reorderScene(fromIndex: Int, toIndex: Int) {
        updateActive { project ->
            val draft = project.activeDraft ?: return@updateActive project
            val scenes = draft.scenes.toMutableList()
            if (fromIndex !in scenes.indices || toIndex !in scenes.indices) return@updateActive project

            val item = scenes.removeAt(fromIndex)
            scenes.add(toIndex, item)

            val renumbered = scenes.mapIndexed { index, scene ->
                scene.copy(sceneNumber = index + 1)
            }

            val updatedDraft = draft.copy(scenes = renumbered)
            val drafts = project.drafts.map { if (it.id == draft.id) updatedDraft else it }
            project.copy(drafts = drafts, updatedAt = now())
        }
        _activeSceneIndex.value = toIndex
    }

    // ---------------------------------------------------------------------
    // Internals
    // ---------------------------------------------------------------------

    private fun updateActive(transform: (ScriptProject) -> ScriptProject) {
        val updated = transform(_currentProject.value)
        _currentProject.value = updated
        _projects.update { list ->
            if (list.any { it.id == updated.id }) {
                list.map { if (it.id == updated.id) updated else it }
            } else {
                listOf(updated) + list
            }
        }
        repoScope.launch {
            try {
                LocalStorage.saveProject(updated)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateScene(sceneIndex: Int, transform: (ScriptScene) -> ScriptScene) {
        updateActive { project ->
            val draft = project.activeDraft ?: return@updateActive project
            val scenes = draft.scenes.toMutableList()
            if (sceneIndex !in scenes.indices) return@updateActive project

            val updatedScene = transform(scenes[sceneIndex])
            scenes[sceneIndex] = updatedScene

            val updatedDraft = draft.copy(scenes = scenes)
            val drafts = project.drafts.map { if (it.id == draft.id) updatedDraft else it }
            project.copy(
                drafts = drafts,
                characters = extractCharacters(scenes),
                updatedAt = now()
            )
        }
    }

    private fun updateBlocks(sceneIndex: Int, transform: (List<ScriptElementBlock>) -> List<ScriptElementBlock>) {
        updateScene(sceneIndex) { scene ->
            val blocks = transform(scene.blocks)
            val rawLength = blocks.sumOf { it.text.length }
            scene.copy(
                blocks = blocks,
                estimatedPageDuration = ScreenplayRules.calculatePageEstimate(blocks.size, rawLength)
            )
        }
    }

    private fun extractCharacters(scenes: List<ScriptScene>): List<CharacterProfile> {
        val map = mutableMapOf<String, Int>()
        for (scene in scenes) {
            for (block in scene.blocks) {
                if (block.type == ElementType.CHARACTER) {
                    val name = block.text.trim().uppercase()
                    if (name.isNotBlank()) {
                        map[name] = (map[name] ?: 0) + 1
                    }
                }
            }
        }
        return map.map { (name, count) ->
            CharacterProfile(
                id = "char_${name.hashCode()}",
                name = name,
                dialogueCount = count
            )
        }.sortedByDescending { it.dialogueCount }
    }

    private fun randomSuffix(): String = kotlin.random.Random.nextInt(100000, 999999).toString()

    private fun now(): Long = 1731150000000L
}

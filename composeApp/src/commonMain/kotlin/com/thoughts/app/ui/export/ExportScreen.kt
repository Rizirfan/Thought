package com.thoughts.app.ui.export

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thoughts.app.data.rememberFileExporter
import com.thoughts.app.data.repository.ScreenplayRepository
import com.thoughts.app.domain.engine.ScreenplayPdfRenderer
import com.thoughts.app.ui.AppState
import com.thoughts.app.ui.common.SectionLabel
import com.thoughts.app.ui.common.VerticalRule
import com.thoughts.app.ui.navigation.ScreenplayNavDestination
import com.thoughts.app.ui.theme.*
import org.jetbrains.compose.resources.painterResource
import thoughts.composeapp.generated.resources.Res
import thoughts.composeapp.generated.resources.ic_script_folder

@Composable
fun ExportScreen(
    repository: ScreenplayRepository,
    appState: AppState,
    modifier: Modifier = Modifier
) {
    val project by repository.currentProject.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val exporter = rememberFileExporter()
    var status by remember { mutableStateOf<String?>(null) }
    val colors = StudioTheme.colors

    val scenes = project.activeDraft?.scenes.orEmpty()
    val pages = scenes.sumOf { it.estimatedPageDuration.toDouble() }.toFloat()

    val formattedScript = remember(project) {
        ScreenplayPdfRenderer.renderFormattedScript(project, includeTitlePage = true)
    }

    val fileName = remember(project) {
        val base = project.title.ifBlank { "screenplay" }
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
            .ifBlank { "screenplay" }
        "$base.txt"
    }

    val pdfFileName = remember(project) {
        val base = project.title.ifBlank { "screenplay" }
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
            .ifBlank { "screenplay" }
        "$base.pdf"
    }

    // Full-screen dedicated Script Preview Page
    if (appState.isExportPreviewOpen) {
        ScreenplayPreviewPage(
            title = project.title.ifBlank { "UNTITLED SCREENPLAY" },
            author = project.author.ifBlank { "Writer" },
            pages = pages,
            scenesCount = scenes.size,
            text = formattedScript,
            status = status,
            onBack = { appState.isExportPreviewOpen = false },
            onEdit = {
                appState.isExportPreviewOpen = false
                appState.navigate(ScreenplayNavDestination.EDITOR)
            },
            onSave = {
                repository.saveCurrentProject { success ->
                    status = if (success) "Screenplay saved to local storage" else "Error saving"
                }
            },
            modifier = modifier
        )
        return
    }

    // Main Export Page
    BoxWithConstraints(
        modifier = modifier.fillMaxSize().background(colors.canvas),
        contentAlignment = Alignment.TopCenter
    ) {
        val compact = maxWidth < 640.dp
        val pagePadding = if (compact) Spacing.screenSide else Spacing.xxl

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = pagePadding, vertical = Spacing.section)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(Spacing.xs))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 840.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Document Header: 3D Script Folder
                Box(
                    modifier = Modifier.size(88.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(Res.drawable.ic_script_folder),
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                Spacer(Modifier.height(Spacing.sm))
                Text(
                    text = project.title.ifBlank { "UNTITLED SCREENPLAY" },
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = colors.ink,
                    textAlign = TextAlign.Center
                )
                if (project.author.isNotBlank()) {
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        text = "by ${project.author}",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                        color = colors.inkSecondary,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(Modifier.height(Spacing.md))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${scenes.size} scene${if (scenes.size == 1) "" else "s"}",
                        style = StudioType.metric,
                        color = colors.ink
                    )
                    Spacer(Modifier.width(Spacing.md))
                    VerticalRule()
                    Spacer(Modifier.width(Spacing.md))
                    Text(
                        text = "%.1f pages".format(pages),
                        style = StudioType.metric,
                        color = colors.ink
                    )
                }

                Spacer(Modifier.height(Spacing.section))

                // Option: Preview Screenplay in separate page
                SectionLabel("Preview")
                Spacer(Modifier.height(Spacing.sm))

                Surface(
                    onClick = { appState.isExportPreviewOpen = true },
                    shape = ButtonShape,
                    color = colors.surfaceAlt,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Sizes.inputHeight)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Visibility,
                                contentDescription = "Preview",
                                tint = colors.ink,
                                modifier = Modifier.size(Sizes.icon)
                            )
                            Text(
                                text = "Preview Screenplay",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                ),
                                color = colors.ink
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                            contentDescription = "Open preview",
                            tint = colors.inkTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(Modifier.height(Spacing.section))

                // Export Options
                SectionLabel("Export")
                Spacer(Modifier.height(Spacing.sm))

                if (compact) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        Button(
                            onClick = {
                                exporter.save(pdfFileName, "application/pdf", formattedScript)
                                status = "Exported as $pdfFileName"
                            },
                            shape = ButtonShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.ink,
                                contentColor = colors.canvas
                            ),
                            modifier = Modifier.fillMaxWidth().height(Sizes.primaryButton)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PictureAsPdf,
                                contentDescription = null,
                                modifier = Modifier.size(Sizes.icon)
                            )
                            Spacer(Modifier.width(Spacing.sm))
                            Text("Export to PDF", fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = {
                                exporter.save(fileName, "text/plain", formattedScript)
                                status = "Saved as $fileName"
                            },
                            shape = ButtonShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.surfaceAlt,
                                contentColor = colors.ink
                            ),
                            modifier = Modifier.fillMaxWidth().height(Sizes.primaryButton)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Save,
                                contentDescription = null,
                                modifier = Modifier.size(Sizes.icon)
                            )
                            Spacer(Modifier.width(Spacing.sm))
                            Text("Save File", fontWeight = FontWeight.SemiBold)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        Button(
                            onClick = {
                                exporter.save(pdfFileName, "application/pdf", formattedScript)
                                status = "Exported as $pdfFileName"
                            },
                            shape = ButtonShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.ink,
                                contentColor = colors.canvas
                            ),
                            modifier = Modifier.weight(1f).height(Sizes.primaryButton)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PictureAsPdf,
                                contentDescription = null,
                                modifier = Modifier.size(Sizes.iconSmall)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Export PDF", fontWeight = FontWeight.SemiBold)
                        }
                        Button(
                            onClick = {
                                exporter.save(fileName, "text/plain", formattedScript)
                                status = "Saved as $fileName"
                            },
                            shape = ButtonShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.surfaceAlt,
                                contentColor = colors.ink
                            ),
                            modifier = Modifier.weight(1f).height(Sizes.primaryButton)
                        ) {
                            Icon(Icons.Outlined.Save, contentDescription = null, modifier = Modifier.size(Sizes.iconSmall))
                            Spacer(Modifier.width(6.dp))
                            Text("Save File", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Status Toast Banner
                val currentStatus = status
                if (currentStatus != null) {
                    Spacer(Modifier.height(Spacing.md))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(InputShape)
                            .background(colors.success.copy(alpha = 0.1f))
                            .padding(horizontal = Spacing.sm, vertical = 7.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = colors.success,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = currentStatus,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.success
                        )
                    }
                }

                Spacer(Modifier.height(Spacing.section))
            }

            Spacer(Modifier.height(Spacing.xxl))
        }
    }
}

/**
 * Separate full-screen preview page for the screenplay.
 * Features dedicated "Edit Screenplay" and "Save to Storage" buttons.
 */
@Composable
private fun ScreenplayPreviewPage(
    title: String,
    author: String,
    pages: Float,
    scenesCount: Int,
    text: String,
    status: String?,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = StudioTheme.colors

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.canvas)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        // Clean top navigation bar for preview
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(Sizes.topBar)
                .padding(horizontal = Spacing.screenSide),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back to export",
                        tint = colors.ink,
                        modifier = Modifier.size(Sizes.icon)
                    )
                }

                Column {
                    Text(
                        text = "Script Preview",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = colors.ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$scenesCount scenes · ${"%.1f".format(pages)} pages",
                        style = StudioType.caption,
                        color = colors.inkSecondary
                    )
                }
            }

            // Small icons inside script preview replacing copy and pdf: Save and Edit
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onSave() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Save,
                        contentDescription = "Save screenplay",
                        tint = colors.ink,
                        modifier = Modifier.size(Sizes.icon)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onEdit() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit screenplay",
                        tint = colors.ink,
                        modifier = Modifier.size(Sizes.icon)
                    )
                }
            }
        }

        // Status banner if user copied, saved, or exported
        if (status != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.success.copy(alpha = 0.12f))
                    .padding(horizontal = Spacing.screenSide, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = colors.success,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.success
                )
            }
        }

        // Full-page reading canvas
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(colors.surface)
        ) {
            Text(
                text = text.ifBlank { "Nothing to preview yet." },
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.5.sp,
                    lineHeight = 17.sp
                ),
                color = colors.ink
            )
        }
    }
}

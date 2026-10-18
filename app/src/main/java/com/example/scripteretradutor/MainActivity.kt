package com.example.scripteretradutor

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import java.util.Calendar
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.scripteretradutor.data.AppRepository
import com.example.scripteretradutor.data.Project
import com.example.scripteretradutor.data.SimplePtBrCorrectionEngine
import com.example.scripteretradutor.data.TextVersion
import com.example.scripteretradutor.data.CorrectionIssue
import com.example.scripteretradutor.data.LanguageToolCorrectionEngine
import kotlinx.coroutines.launch
import java.io.OutputStreamWriter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import com.example.scripteretradutor.data.WritingUnit
import com.example.scripteretradutor.ui.theme.ScripterETradutorTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = AppRepository(this)
        setContent {
            var darkTheme by rememberSaveable { mutableStateOf(repository.isDarkTheme()) }
            ScripterETradutorTheme(darkTheme = darkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ScripterApp(repository, darkTheme) {
                        darkTheme = !darkTheme
                        repository.setDarkTheme(darkTheme)
                    }
                }
            }
        }
    }
}

@Composable
private fun ScripterApp(repository: AppRepository, darkTheme: Boolean, onToggleTheme: () -> Unit) {
    val appContext = LocalContext.current
    var projects by remember { mutableStateOf(repository.loadProjects()) }
    var reminderEnabled by remember { mutableStateOf(repository.isReminderEnabled()) }
    var ignoredWords by remember { mutableStateOf(repository.loadIgnoredWords()) }
    var selectedProjectId by remember { mutableStateOf<String?>(null) }
    var selectedUnitId by remember { mutableStateOf<String?>(null) }
    var showSettings by remember { mutableStateOf(false) }
    var showCreateProject by remember { mutableStateOf(false) }
    var newProjectName by remember { mutableStateOf("") }
    var selectedTemplate by remember { mutableStateOf("Livre") }
    val selectedProject = projects.firstOrNull { it.id == selectedProjectId }
    val selectedUnit = selectedProject?.units?.firstOrNull { it.id == selectedUnitId }

    fun refresh() { projects = repository.loadProjects() }

    BackHandler(enabled = (selectedProjectId != null && selectedUnitId == null) || showSettings) {
        when {
            selectedProjectId != null -> selectedProjectId = null
            showSettings -> showSettings = false
        }
    }

    if (selectedProject != null && selectedUnit != null) {
        EditorScreen(selectedProject, selectedUnit, darkTheme, ignoredWords, { refresh(); selectedUnitId = null }, { note ->
            repository.updateUnitNote(selectedProject.id, selectedUnit.id, note)
        }) { content ->
            repository.updateUnit(selectedProject.id, selectedUnit.id, content)
            repository.saveVersion(selectedProject.id, selectedUnit.id, content)
        }
    } else if (selectedProject != null) {
        ProjectScreen(
            project = selectedProject,
            onBack = { selectedProjectId = null },
            onOpenUnit = { selectedUnitId = it.id },
            onTagsChanged = { tags -> repository.updateTags(selectedProject.id, tags); refresh() },
            availableTags = repository.loadTagCatalog(),
            ignoredWords = ignoredWords,
            onAddNote = { text, type -> repository.addNote(selectedProject.id, text, type); refresh() },
            onToggleNote = { noteId -> repository.toggleNote(selectedProject.id, noteId); refresh() },
            onRestoreVersion = { version -> repository.updateUnit(selectedProject.id, version.unitId, version.content); refresh() },
            onShareProject = {
                val fullText = selectedProject.units.joinToString("\n\n") { "${it.title}\n\n${it.content}" }
                appContext.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"; putExtra(Intent.EXTRA_TEXT, fullText); putExtra(Intent.EXTRA_SUBJECT, selectedProject.title)
                }, "Compartilhar projeto"))
            },
            versions = repository.loadVersions().filter { it.projectId == selectedProject.id },
            darkTheme = darkTheme,
            onAddUnit = {
                val unit = repository.addUnit(selectedProject.id, "Nova cena")
                refresh(); selectedUnitId = unit.id
            },
            onDeleteUnit = { unit -> repository.deleteUnit(selectedProject.id, unit.id); refresh() }
        )
    } else if (showSettings) {
        SettingsScreen(
            darkTheme = darkTheme,
            reminderEnabled = reminderEnabled,
            onReminderChanged = { enabled ->
                reminderEnabled = enabled
                repository.setReminderEnabled(enabled)
                scheduleDailyReminder(appContext, enabled)
            },
            onToggleTheme = onToggleTheme,
            tags = repository.loadTagCatalog(),
            customTags = repository.loadCustomTags(),
            ignoredWords = ignoredWords,
            onAddIgnoredWord = { repository.addIgnoredWord(it); ignoredWords = repository.loadIgnoredWords() },
            onRemoveIgnoredWord = { repository.removeIgnoredWord(it); ignoredWords = repository.loadIgnoredWords() },
            onAddTag = { repository.addCatalogTag(it) },
            onRemoveTag = { repository.removeCatalogTag(it) },
            onBackupRestored = { json -> if (repository.importBackup(json)) { refresh(); showSettings = false } },
            onBack = { showSettings = false }
        )
    } else {
        HomeScreen(projects, darkTheme, {
            newProjectName = ""; selectedTemplate = "Livre"; showCreateProject = true
        }, { project ->
            selectedProjectId = project.id; selectedUnitId = null
        }, { project ->
            val unit = repository.addUnit(project.id, "Nova cena")
            refresh(); selectedProjectId = project.id; selectedUnitId = unit.id
        }, { project -> repository.updateProjectState(project.id, favorite = !project.isFavorite); refresh() },
            { project -> repository.updateProjectState(project.id, archived = !project.isArchived); refresh() },
            { project -> repository.duplicateProject(project.id); refresh() },
            { project -> repository.deleteProject(project.id); refresh() },
            { repository.exportBackup() }, reminderEnabled, { enabled ->
                reminderEnabled = enabled
                repository.setReminderEnabled(enabled)
                scheduleDailyReminder(appContext, enabled)
            }, onToggleTheme) { showSettings = true }
    }
    if (showCreateProject) {
        AlertDialog(
            onDismissRequest = { showCreateProject = false },
            title = { Text("Novo projeto") },
            text = {
                Column {
                    OutlinedTextField(newProjectName, { newProjectName = it }, label = { Text("Nome da história") }, singleLine = true)
                    Spacer(Modifier.height(12.dp))
                    Text("Escolha um modelo", style = MaterialTheme.typography.labelLarge)
                    listOf("Livre", "Três Atos", "Jornada do Herói").forEach { template ->
                        OutlinedButton(onClick = { selectedTemplate = template }, modifier = Modifier.fillMaxWidth()) {
                            Text(if (selectedTemplate == template) "✓ $template" else template)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = {
                if (newProjectName.isNotBlank()) {
                    val project = repository.createProject(newProjectName.trim(), selectedTemplate)
                    refresh(); selectedProjectId = project.id; selectedUnitId = null; showCreateProject = false
                }
            }) { Text("Criar") } },
            dismissButton = { TextButton(onClick = { showCreateProject = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun ProjectScreen(
    project: Project,
    darkTheme: Boolean,
    onBack: () -> Unit,
    onOpenUnit: (WritingUnit) -> Unit,
    onTagsChanged: (List<String>) -> Unit,
    availableTags: List<String>,
    ignoredWords: List<String>,
    onAddNote: (String, String) -> Unit,
    onToggleNote: (String) -> Unit,
    onRestoreVersion: (TextVersion) -> Unit,
    onShareProject: () -> Unit,
    versions: List<TextVersion>,
    onAddUnit: () -> Unit,
    onDeleteUnit: (WritingUnit) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var noteText by remember { mutableStateOf("") }
    var tagText by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var showTagPicker by remember { mutableStateOf(false) }
    var tags by remember(project.id) { mutableStateOf(project.tags) }
    var onlineIssues by remember(project.id) { mutableStateOf<List<CorrectionIssue>>(emptyList()) }
    val correctionScope = rememberCoroutineScope()
    val tabs = listOf("Texto", "Correções", "Ideias e tarefas", "Versões")

    NotebookBackground {
        Scaffold { padding ->
            Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = onBack) { Text("Projetos") }
                Text(project.title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 10.dp))
            }
            Spacer(Modifier.height(16.dp))
            if (tags.isNotEmpty()) {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    tags.forEach { tag -> OutlinedButton(onClick = { tags = tags - tag; onTagsChanged(tags) }) { Text("#$tag ×") } }
                }
                Spacer(Modifier.height(8.dp))
            }
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tabs.forEachIndexed { index, tab ->
                    if (selectedTab == index) Button(onClick = { selectedTab = index }) { Text(tab) }
                    else OutlinedButton(onClick = { selectedTab = index }) { Text(tab) }
                }
            }
            Spacer(Modifier.height(16.dp))
            when (selectedTab) {
                0 -> {
                    OutlinedTextField(tagText, { tagText = it }, label = { Text("Nova tag") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(6.dp))
                    OutlinedButton(onClick = { if (tagText.isNotBlank()) { tags = (tags + tagText.trim()).distinct(); onTagsChanged(tags); tagText = "" } }, modifier = Modifier.fillMaxWidth()) { Text("Adicionar tag") }
                    Spacer(Modifier.height(6.dp))
                    OutlinedButton(onClick = { showTagPicker = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Escolher tags sugeridas")
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Capítulos e cenas", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = onShareProject, modifier = Modifier.fillMaxWidth()) { Text("Compartilhar projeto inteiro") }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(searchQuery, { searchQuery = it }, label = { Text("Buscar nesta história") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        itemsIndexed(project.units.filter { searchQuery.isBlank() || it.title.contains(searchQuery, true) || it.content.contains(searchQuery, true) }, key = { _, unit -> unit.id }) { index, unit ->
                            Card(Modifier.fillMaxWidth()) {
                                Column(Modifier.fillMaxWidth().padding(14.dp)) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column { Text("${index + 1}. ${unit.title}"); Text("${unit.content.wordCount()} palavras"); if (unit.note.isNotBlank()) Text("Nota: ${unit.note}", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                        Button(onClick = { onOpenUnit(unit) }) { Text("Escrever") }
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    OutlinedButton(onClick = { onDeleteUnit(unit) }) { Text("Excluir cena") }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = onAddUnit, modifier = Modifier.fillMaxWidth()) { Text("+ Nova cena") }
                }
                1 -> {
                    Text("Correções pendentes", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {
                        correctionScope.launch {
                            val text = project.units.joinToString("\n\n") { it.content }
                            onlineIssues = filterIgnored(text, LanguageToolCorrectionEngine().analyze(text), ignoredWords)
                        }
                    }) { Text("Analisar com LanguageTool") }
                    Spacer(Modifier.height(8.dp))
                    val issues = remember(project.id, project.units) {
                        project.units.flatMap { unit ->
                            filterIgnored(unit.content, SimplePtBrCorrectionEngine.analyzeNow(unit.content), ignoredWords).map { issue -> unit.title to issue }
                        }
                    }
                    val allIssues = issues.map { it.second } + onlineIssues
                    if (allIssues.isEmpty()) Text("Nenhuma sugestão encontrada.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    else LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(issues) { (unitTitle, issue) ->
                            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) {
                                Text(unitTitle, style = MaterialTheme.typography.labelLarge)
                                Text("${issue.message} Sugestão: ${issue.replacement ?: "revisar"}")
                            } }
                        }
                        items(onlineIssues) { issue ->
                            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) {
                                Text("LanguageTool · ${issue.category}", style = MaterialTheme.typography.labelLarge)
                                Text("${issue.message} Sugestão: ${issue.replacement ?: "revisar"}")
                            } }
                        }
                    }
                }
                else -> {
                    if (selectedTab == 3) {
                        Text("Histórico de versões", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        if (versions.isEmpty()) Text("As versões aparecerão depois dos primeiros salvamentos.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        else LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(versions.reversed(), key = { it.id }) { version ->
                                Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) {
                                    Text(version.createdAt.toString(), style = MaterialTheme.typography.labelLarge)
                                    Text("${version.content.wordCount()} palavras")
                                    Spacer(Modifier.height(6.dp))
                                    OutlinedButton(onClick = { onRestoreVersion(version) }) { Text("Restaurar esta versão") }
                                } }
                            }
                        }
                    } else {
                    Text("Ideias e tarefas", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(noteText, { noteText = it }, label = { Text("Nova ideia ou tarefa") }, modifier = Modifier.weight(1f))
                        Button(onClick = { if (noteText.isNotBlank()) { onAddNote(noteText.trim(), "Ideia"); noteText = "" } }) { Text("Adicionar") }
                    }
                    Spacer(Modifier.height(12.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(project.notes, key = { it.id }) { note ->
                            Card(Modifier.fillMaxWidth()) {
                                OutlinedButton(onClick = { onToggleNote(note.id) }, modifier = Modifier.fillMaxWidth()) {
                                    Text(if (note.done) "☑  ${note.text}" else "☐  ${note.text}")
                                }
                            }
                        }
                    }
                    }
                }
            }
            }
        }
    }
    if (showTagPicker) {
        AlertDialog(
            onDismissRequest = { showTagPicker = false },
            title = { Text("Escolher tags") },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(availableTags, key = { it }) { tag ->
                        OutlinedButton(
                            onClick = {
                                tags = (tags + tag).distinct()
                                onTagsChanged(tags)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(if (tag in tags) "✓  $tag" else tag) }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showTagPicker = false }) { Text("Concluir") } }
        )
    }
}

@Composable
private fun SettingsScreen(
    darkTheme: Boolean,
    reminderEnabled: Boolean,
    ignoredWords: List<String>,
    onAddIgnoredWord: (String) -> Unit,
    onRemoveIgnoredWord: (String) -> Unit,
    onReminderChanged: (Boolean) -> Unit,
    onToggleTheme: () -> Unit,
    tags: List<String>,
    customTags: List<String>,
    onAddTag: (String) -> Unit,
    onRemoveTag: (String) -> Unit,
    onBackupRestored: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val backupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) onBackupRestored(context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: "")
    }
    NotebookBackground {
        Scaffold { padding ->
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(padding).padding(20.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = onBack) { Text("Voltar") }
                Text("Configurações", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 8.dp))
            }
            Spacer(Modifier.height(24.dp))
            Text("Aparência", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onToggleTheme, modifier = Modifier.fillMaxWidth()) { Text("Alternar modo claro/escuro") }
            Spacer(Modifier.height(20.dp))
            Text("Produtividade", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { onReminderChanged(!reminderEnabled) }, modifier = Modifier.fillMaxWidth()) {
                Text(if (reminderEnabled) "Desativar lembrete diário" else "Ativar lembrete diário")
            }
            Spacer(Modifier.height(20.dp))
            Text("Tags", style = MaterialTheme.typography.titleMedium)
            Text("Tags sugeridas são fixas. Tags personalizadas podem ser removidas.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                var newTag by remember { mutableStateOf("") }
                OutlinedTextField(newTag, { newTag = it }, label = { Text("Nova tag") }, modifier = Modifier.weight(1f), singleLine = true)
                Button(onClick = { if (newTag.isNotBlank()) { onAddTag(newTag); newTag = "" } }) { Text("Adicionar") }
            }
            Spacer(Modifier.height(8.dp))
            tags.forEach { tag ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("#$tag", modifier = Modifier.padding(vertical = 3.dp))
                    if (tag in customTags) TextButton(onClick = { onRemoveTag(tag) }) { Text("Remover") }
                    else Text("Sugerida", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 5.dp))
                }
            }
            Spacer(Modifier.height(20.dp))
            Text("Palavras ignoradas na correção", style = MaterialTheme.typography.titleMedium)
            Text("Alertas que tocarem nesses termos serão ocultados.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            var ignoredWordText by remember { mutableStateOf("") }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(ignoredWordText, { ignoredWordText = it }, label = { Text("Palavra ou frase") }, modifier = Modifier.weight(1f), singleLine = true)
                Button(onClick = { if (ignoredWordText.isNotBlank()) { onAddIgnoredWord(ignoredWordText); ignoredWordText = "" } }) { Text("Adicionar") }
            }
            ignoredWords.forEach { word ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(word, modifier = Modifier.padding(vertical = 4.dp))
                    TextButton(onClick = { onRemoveIgnoredWord(word) }) { Text("Remover") }
                }
            }
            Spacer(Modifier.height(20.dp))
            Text("Backup", style = MaterialTheme.typography.titleMedium)
            OutlinedButton(onClick = { backupLauncher.launch(arrayOf("application/json", "text/plain")) }, modifier = Modifier.fillMaxWidth()) { Text("Restaurar backup JSON") }
            Spacer(Modifier.height(20.dp))
            Text("Idioma da correção: Português do Brasil (pt-BR)")
            Spacer(Modifier.height(8.dp))
            Text("Seus textos ficam armazenados localmente. A análise online só é enviada quando você solicita LanguageTool.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(24.dp))
            Text("Scripter · versão de desenvolvimento", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun HomeScreen(
    projects: List<Project>, darkTheme: Boolean, onCreateProject: () -> Unit,
    onOpenProject: (Project) -> Unit, onAddUnit: (Project) -> Unit,
    onToggleFavorite: (Project) -> Unit, onArchive: (Project) -> Unit, onDuplicate: (Project) -> Unit,
    onDelete: (Project) -> Unit
    , onBackup: () -> String, reminderEnabled: Boolean, onReminderChanged: (Boolean) -> Unit, onToggleTheme: () -> Unit, onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    var filter by remember { mutableStateOf<String?>(null) }
    var showArchived by remember { mutableStateOf(false) }
    var projectToDelete by remember { mutableStateOf<Project?>(null) }
    var deleteConfirmationText by remember { mutableStateOf("") }
    val allTags = projects.flatMap { it.tags }.distinct().sorted()
    val visibleProjects = projects.filter { it.isArchived == showArchived && (filter == null || filter in it.tags) }
    NotebookBackground {
        Scaffold { padding ->
            Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp, vertical = 28.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Scripter", style = MaterialTheme.typography.headlineLarge)
                    Text("Seu espaço para histórias e roteiros", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                OutlinedButton(onClick = onOpenSettings) {
                    Image(painterResource(com.example.scripteretradutor.R.drawable.configuracoes), contentDescription = "Configurações", modifier = Modifier.padding(2.dp).size(28.dp))
                }
            }
            Spacer(Modifier.height(24.dp))
            Button(onClick = onCreateProject, modifier = Modifier.fillMaxWidth()) { Text("+  Criar projeto") }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = {
                context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"; putExtra(Intent.EXTRA_TEXT, onBackup())
                }, "Fazer backup dos projetos"))
            }, modifier = Modifier.fillMaxWidth()) { Text("Backup dos projetos") }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { onReminderChanged(!reminderEnabled) }, modifier = Modifier.fillMaxWidth()) {
                Text(if (reminderEnabled) "Desativar lembrete diário" else "Ativar lembrete diário")
            }
            Spacer(Modifier.height(8.dp))
            Spacer(Modifier.height(24.dp))
            Text("Meus projetos", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            if (allTags.isNotEmpty()) {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (filter == null) Button(onClick = { filter = null }) { Text("Todos") }
                    else OutlinedButton(onClick = { filter = null }) { Text("Todos") }
                    allTags.forEach { tag ->
                        if (filter == tag) Button(onClick = { filter = null }) { Text("#$tag") }
                        else OutlinedButton(onClick = { filter = tag }) { Text("#$tag") }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            OutlinedButton(onClick = { showArchived = !showArchived }) { Text(if (showArchived) "Projetos ativos" else "Ver arquivados") }
            Spacer(Modifier.height(8.dp))
            if (visibleProjects.isEmpty()) Text("Crie seu primeiro projeto para começar a escrever.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            else LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(visibleProjects, key = { it.id }) { ProjectCard(it, onOpenProject, onAddUnit, onToggleFavorite, onArchive, onDuplicate, { projectToDelete = it; deleteConfirmationText = "" }) }
            }
            }
        }
    }
    projectToDelete?.let { project ->
        AlertDialog(
            onDismissRequest = { projectToDelete = null },
            title = { Text("Excluir projeto?") },
            text = {
                Column {
                    Text("Esta ação remove permanentemente ${project.title}. Digite delete para confirmar.")
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = deleteConfirmationText,
                        onValueChange = { deleteConfirmationText = it },
                        label = { Text("Digite delete") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = deleteConfirmationText == "delete",
                    onClick = { onDelete(project); projectToDelete = null }
                ) { Text("Excluir") }
            },
            dismissButton = { TextButton(onClick = { projectToDelete = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun ProjectCard(
    project: Project, onOpen: (Project) -> Unit, onAddUnit: (Project) -> Unit,
    onToggleFavorite: (Project) -> Unit, onArchive: (Project) -> Unit, onDuplicate: (Project) -> Unit,
    onDelete: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(project.title, style = MaterialTheme.typography.titleMedium)
            if (project.tags.isNotEmpty()) Text(project.tags.joinToString("  ") { "#$it" }, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            Text("${project.units.size} unidade(s) · ${project.units.sumOf { it.content.wordCount() }} palavras")
            Spacer(Modifier.height(12.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onOpen(project) }) { Text("Abrir") }
                OutlinedButton(onClick = { onAddUnit(project) }) { Text("Nova cena") }
            }
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { onToggleFavorite(project) }) { Text(if (project.isFavorite) "★ Favorito" else "☆ Fixar") }
                OutlinedButton(onClick = { onDuplicate(project) }) { Text("Duplicar") }
                OutlinedButton(onClick = { onArchive(project) }) { Text(if (project.isArchived) "Desarquivar" else "Arquivar") }
                OutlinedButton(onClick = onDelete) { Text("Excluir") }
            }
        }
    }
}

@Composable
private fun EditorScreen(
    project: Project, unit: WritingUnit, darkTheme: Boolean, ignoredWords: List<String>, onBack: () -> Unit, onNoteChanged: (String) -> Unit, onContentChanged: (String) -> Unit
) {
    var title by remember(unit.id) { mutableStateOf(unit.title) }
    var note by remember(unit.id) { mutableStateOf(unit.note) }
    var content by remember(unit.id) { mutableStateOf(unit.content) }
    var isSaved by remember(unit.id) { mutableStateOf(true) }
    var isReading by remember(unit.id) { mutableStateOf(false) }
    var showWritingCorrections by remember { mutableStateOf(false) }
    var showSavePrompt by remember { mutableStateOf(false) }
    var writingIssues by remember { mutableStateOf<List<CorrectionIssue>>(emptyList()) }
    var correctionInProgress by remember { mutableStateOf(false) }
    val correctionScope = rememberCoroutineScope()
    fun requestExit() {
        if (content != unit.content || note != unit.note) showSavePrompt = true else onBack()
    }
    BackHandler { requestExit() }
    val context = LocalContext.current
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
        if (uri != null) context.contentResolver.openOutputStream(uri)?.use { output ->
            OutputStreamWriter(output, Charsets.UTF_8).use { it.write(content) }
        }
    }
    val pdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        if (uri != null) context.contentResolver.openOutputStream(uri)?.use { output -> writePdf(content, output) }
    }
    val docxLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) { uri ->
        if (uri != null) context.contentResolver.openOutputStream(uri)?.use { output -> writeDocx(content, output) }
    }
    val speechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val spoken = result.data?.getStringArrayListExtra("android.speech.extra.RESULTS")?.firstOrNull()
        if (!spoken.isNullOrBlank()) content = listOf(content.trim(), spoken.trim()).filter { it.isNotEmpty() }.joinToString(" ")
    }

    LaunchedEffect(note, unit.id) {
        if (note != unit.note) {
            isSaved = false
        }
    }

    NotebookBackground {
        Scaffold { padding ->
            Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { requestExit() }) { Text("Voltar") }
                OutlinedButton(onClick = {
                    if (content != unit.content) onContentChanged(content)
                    if (note != unit.note) onNoteChanged(note)
                    isSaved = true
                }) { Text("Salvar") }
                OutlinedButton(onClick = { isReading = !isReading }) { Text(if (isReading) "Editar" else "Leitura") }
                OutlinedButton(onClick = { exportLauncher.launch("${unit.title}.txt") }) { Text("TXT") }
                OutlinedButton(onClick = { pdfLauncher.launch("${unit.title}.pdf") }) { Text("PDF") }
                OutlinedButton(onClick = { docxLauncher.launch("${unit.title}.docx") }) { Text("DOCX") }
                OutlinedButton(onClick = {
                    showWritingCorrections = true
                    correctionInProgress = true
                    correctionScope.launch {
                        writingIssues = filterIgnored(content, LanguageToolCorrectionEngine().analyze(content), ignoredWords)
                        correctionInProgress = false
                    }
                }) { Text("Corrigir") }
                if (!isReading) OutlinedButton(onClick = {
                    speechLauncher.launch(Intent("android.speech.action.RECOGNIZE_SPEECH").apply {
                        putExtra("android.speech.extra.LANGUAGE_MODEL", "free_form")
                        putExtra("android.speech.extra.LANGUAGE", "pt-BR")
                        putExtra("android.speech.extra.PROMPT", "Dite sua história")
                    })
                }) { Text("Ditado") }
                OutlinedButton(onClick = {
                    context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"; putExtra(Intent.EXTRA_TEXT, content); putExtra(Intent.EXTRA_SUBJECT, unit.title)
                    }, "Compartilhar trecho"))
                }) { Text("Compartilhar") }
            }
            Text(if (isSaved) "Salvo" else "Salvando…", Modifier.fillMaxWidth().padding(top = 4.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            Text(project.title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            OutlinedTextField(title, { title = it }, label = { Text("Título da cena") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(note, { note = it; isSaved = false }, label = { Text("Nota privada da cena") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
            if (isReading) {
                Column(Modifier.fillMaxWidth().weight(1f).padding(12.dp)) {
                    Text(content.ifBlank { "Ainda não há texto nesta cena." }, style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                OutlinedTextField(
                    content, { content = it; writingIssues = emptyList(); isSaved = false }, label = { Text("Escreva sua história…") },
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    visualTransformation = CorrectionVisualTransformation(writingIssues)
                )
            }
            Spacer(Modifier.height(8.dp)); HorizontalDivider(); Spacer(Modifier.height(8.dp))
            Column(Modifier.fillMaxWidth()) {
                Text("${content.wordCount()} palavras · ${content.length} caracteres")
                Text("Correção pt-BR pronta para conectar", color = MaterialTheme.colorScheme.primary)
            }
            }
        }
    }

    if (showSavePrompt) {
        AlertDialog(
            onDismissRequest = { showSavePrompt = false },
            title = { Text("Salvar alterações?") },
            text = { Text("Você tem alterações não salvas nesta cena.") },
            confirmButton = {
                TextButton(onClick = {
                    if (content != unit.content) onContentChanged(content)
                    if (note != unit.note) onNoteChanged(note)
                    showSavePrompt = false
                    isSaved = true
                    onBack()
                }) { Text("Salvar") }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { showSavePrompt = false; onBack() }) { Text("Descartar") }
                    TextButton(onClick = { showSavePrompt = false }) { Text("Cancelar") }
                }
            }
        )
    }
    if (showWritingCorrections) {
        AlertDialog(
            onDismissRequest = { showWritingCorrections = false },
            title = { Text("Correção de escrita") },
            text = {
                if (correctionInProgress) Text("Analisando o texto…")
                else if (writingIssues.isEmpty()) Text("Nenhum problema foi encontrado.")
                else LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(writingIssues) { issue ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(10.dp)) {
                                Text(issue.message, style = MaterialTheme.typography.labelLarge)
                                issue.replacement?.let { replacement ->
                                    Spacer(Modifier.height(4.dp))
                                    OutlinedButton(onClick = {
                                        val start = issue.start.coerceIn(0, content.length)
                                        val end = issue.end.coerceIn(start, content.length)
                                        content = content.replaceRange(start, end, replacement)
                                        writingIssues = emptyList()
                                        showWritingCorrections = false
                                    }) { Text("Aplicar sugestão") }
                                }
                                issue.replacement?.let { Text("Sugestão: $it") }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!correctionInProgress && writingIssues.any { it.replacement != null }) {
                        TextButton(onClick = {
                            var corrected = content
                            writingIssues.filter { it.replacement != null }.sortedByDescending { it.start }.forEach { issue ->
                                val replacement = issue.replacement ?: return@forEach
                                val start = issue.start.coerceIn(0, corrected.length)
                                val end = issue.end.coerceIn(start, corrected.length)
                                corrected = corrected.replaceRange(start, end, replacement)
                            }
                            content = corrected
                            writingIssues = emptyList()
                            showWritingCorrections = false
                        }) { Text("Corrigir tudo") }
                    }
                    TextButton(onClick = { showWritingCorrections = false }) { Text("Fechar") }
                }
            }
        )
    }
}

private class CorrectionVisualTransformation(
    private val issues: List<CorrectionIssue>
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val builder = AnnotatedString.Builder(text.text)
        issues.forEach { issue ->
            val start = issue.start.coerceIn(0, text.text.length)
            val end = issue.end.coerceIn(start, text.text.length)
            if (start < end) {
                builder.addStyle(
                    SpanStyle(color = Color.Red, textDecoration = TextDecoration.Underline),
                    start,
                    end
                )
            }
        }
        return TransformedText(builder.toAnnotatedString(), OffsetMapping.Identity)
    }
}

@Composable
private fun NotebookBackground(content: @Composable () -> Unit) {
    content()
}

private fun filterIgnored(text: String, matches: List<CorrectionIssue>, ignoredWords: List<String>): List<CorrectionIssue> {
    val ignored = ignoredWords.map { it.trim().lowercase() }.filter { it.isNotEmpty() }
    if (ignored.isEmpty()) return matches
    val normalizedText = text.lowercase()
    return matches.filter { match ->
        ignored.none { word ->
            var fromIndex = 0
            var overlaps = false
            while (true) {
                val wordStart = normalizedText.indexOf(word, fromIndex)
                if (wordStart < 0) break
                val wordEnd = wordStart + word.length
                if (match.start < wordEnd && match.end > wordStart) {
                    overlaps = true
                    break
                }
                fromIndex = wordStart + 1
            }
            overlaps
        }
    }
}

private fun String.wordCount(): Int = trim().takeIf { it.isNotEmpty() }?.split(Regex("\\s+"))?.size ?: 0

private fun writePdf(text: String, output: java.io.OutputStream) {
    val document = PdfDocument()
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.BLACK; textSize = 14f; typeface = android.graphics.Typeface.DEFAULT }
    val pageWidth = 595
    val pageHeight = 842
    val margin = 48f
    val lineHeight = 21f
    val maxChars = 78
    val lines = text.lines().flatMap { line -> if (line.isBlank()) listOf("") else line.chunked(maxChars) }
    var pageNumber = 1
    var index = 0
    while (index < lines.size || (lines.isEmpty() && pageNumber == 1)) {
        val page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        var y = margin + 10f
        while (index < lines.size && y < pageHeight - margin) {
            page.canvas.drawText(lines[index], margin, y, paint)
            y += lineHeight
            index++
        }
        document.finishPage(page)
        pageNumber++
    }
    document.writeTo(output)
    document.close()
}

private fun writeDocx(text: String, output: java.io.OutputStream) {
    val documentXml = buildString {
        append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
        append("<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:body>")
        text.lines().forEach { line ->
            append("<w:p><w:r><w:t xml:space=\"preserve\">")
            append(escapeXml(line))
            append("</w:t></w:r></w:p>")
        }
        append("<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/><w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>")
        append("</w:body></w:document>")
    }
    ZipOutputStream(output).use { zip ->
        fun entry(name: String, value: String) {
            zip.putNextEntry(ZipEntry(name))
            zip.write(value.toByteArray(Charsets.UTF_8))
            zip.closeEntry()
        }
        entry("[Content_Types].xml", "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\"><Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/><Default Extension=\"xml\" ContentType=\"application/xml\"/><Override PartName=\"/word/document.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml\"/></Types>")
        entry("_rels/.rels", "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"word/document.xml\"/></Relationships>")
        entry("word/document.xml", documentXml)
    }
}

private fun escapeXml(value: String): String = value
    .replace("&", "&amp;")
    .replace("<", "&lt;")
    .replace(">", "&gt;")
    .replace("\"", "&quot;")
    .replace("'", "&apos;")

private fun scheduleDailyReminder(context: android.content.Context, enabled: Boolean) {
    val alarmManager = context.getSystemService(AlarmManager::class.java)
    val intent = Intent(context, ReminderReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context, 7001, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    if (!enabled) {
        alarmManager.cancel(pendingIntent)
        return
    }
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 20)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
    }
    alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)
}

package com.example

import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.util.Locale
import coil.compose.AsyncImage
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AdmissionRecord
import com.example.data.SkinLesionRow
import com.example.ui.AdmissionViewModel
import com.example.ui.ScreenState
import com.example.ui.theme.MyApplicationTheme
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle

class MainActivity : ComponentActivity() {
    private val viewModel: AdmissionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
            MyApplicationTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
                    val admissions by viewModel.admissions.collectAsStateWithLifecycle()
                    val currentRecord by viewModel.currentRecord.collectAsStateWithLifecycle()

                    when (val state = screenState) {
                        is ScreenState.List -> {
                            AdmissionListScreen(
                                admissions = admissions,
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = { viewModel.toggleTheme() },
                                onAddClick = { viewModel.navigateToNewForm() },
                                onEditClick = { viewModel.navigateToEditForm(it) },
                                onDeleteClick = { viewModel.deleteRecord(it) },
                                onViewClick = { viewModel.navigateToReportPreview(it) }
                            )
                        }
                        is ScreenState.Form -> {
                            AdmissionFormScreen(
                                record = currentRecord,
                                isEditMode = state.isEditMode,
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = { viewModel.toggleTheme() },
                                onUpdateRecord = { viewModel.updateRecord(it) },
                                onSaveClick = { viewModel.saveCurrentRecord() },
                                onBackClick = { viewModel.navigateToList() }
                            )
                        }
                        is ScreenState.ReportPreview -> {
                            val aiLoading by viewModel.aiLoading.collectAsStateWithLifecycle()
                            ReportPreviewScreen(
                                record = currentRecord, // Use currentRecord from ViewModel to get live AI updates!
                                aiLoading = aiLoading,
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = { viewModel.toggleTheme() },
                                onGenerateAiClick = { recordToEval ->
                                    viewModel.generateAiEvaluationForRecord(recordToEval) { _ -> }
                                },
                                onBackClick = { viewModel.navigateToList() },
                                onEditClick = { viewModel.navigateToEditForm(currentRecord) },
                                onDeleteClick = { viewModel.deleteRecord(currentRecord) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Option definitions for Clinical Scales
val bradenPercepcaoSensorialOptions = listOf(
    1 to "Totalmente limitado (Não reage a estímulos dolorosos)",
    2 to "Muito limitado (Reage apenas a estímulos dolorosos)",
    3 to "Levemente limitado (Responde a comandos verbais, mas não sente em partes)",
    4 to "Nenhuma limitação (Responde perfeitamente a comandos verbais)"
)

val bradenUmidadeOptions = listOf(
    1 to "Completamente úmida (Constantemente úmida/suor/urina)",
    2 to "Muito úmida (Frequente, mas não constante)",
    3 to "Ocasionalmente úmida (Necessita troca adicional de lençóis diária)",
    4 to "Raramente úmida (Pele geralmente seca, lençóis trocados de rotina)"
)

val bradenAtividadeOptions = listOf(
    1 to "Acamado (Restrito à cama)",
    2 to "Na cadeira (Incapaz de deambular, senta em cadeira)",
    3 to "Deambula ocasionalmente (Anda distâncias curtas com/sem ajuda)",
    4 to "Deambula frequentemente (Anda frequentemente fora do quarto)"
)

val bradenMobilidadeOptions = listOf(
    1 to "Totalmente imóvel (Não faz pequenos ajustes de posição)",
    2 to "Muito limitada (Faz pequenos ajustes corporais frequentes)",
    3 to "Levemente limitada (Faz frequentes e pequenas modificações na posição sozinho)",
    4 to "Nenhuma limitação (Faz mudanças de posição frequentes e significativas sozinho)"
)

val bradenNutricaoOptions = listOf(
    1 to "Muito pobre (Nunca come refeição completa, jejum prolongado)",
    2 to "Provavelmente inadequada (Raramente come refeição completa, dieta líquida)",
    3 to "Adequada (Come mais de metade da maioria das refeições)",
    4 to "Excelente (Come a maioria das refeições, ingere lanches)"
)

val bradenFriccaoCisalhamentoOptions = listOf(
    1 to "Problema (Requer assistência moderada/máxima para mover-se, escorrega)",
    2 to "Problema potencial (Move-se com fraqueza, requer alguma assistência mínima)",
    3 to "Sem problema aparente (Move-se na cama e cadeira independentemente)"
)

val fugulinEstadoMentalOptions = listOf(
    1 to "Orientado no tempo e no espaço",
    2 to "Períodos de desorientação",
    3 to "Desorientado no tempo e no espaço",
    4 to "Inconsciente"
)

val fugulinOxigenacaoOptions = listOf(
    1 to "Ar ambiente / Não depende de oxigênio",
    2 to "Uso intermitente de O2 / Nebulização de resgate",
    3 to "Uso contínuo de O2 (máscara ou cateter)",
    4 to "Ventilação mecânica / Traqueostomia / CTI"
)

val fugulinSinaisVitaisOptions = listOf(
    1 to "Controle de rotina (diário)",
    2 to "Controle de 4/4h ou 6/6h",
    3 to "Controle de 2/2h",
    4 to "Controle frequente ou contínuo"
)

val fugulinMotilidadeOptions = listOf(
    1 to "Movimenta todos os membros normalmente",
    2 to "Limitação parcial de movimentos (paresia)",
    3 to "Paralisia de um ou mais membros (plegia)",
    4 to "Tetraplegia ou imobilização total"
)

val fugulinLocomocaoOptions = listOf(
    1 to "Deambula (anda sozinho ou com apoio)",
    2 to "Necessita de auxílio / Cadeira de rodas",
    3 to "Restrito ao leito (acamado)",
    4 to "Totalmente dependente / Transporte em maca"
)

val fugulinCuidadoCorporalOptions = listOf(
    1 to "Auto-suficiente / Toma banho sozinho",
    2 to "Auxílio de chuveiro ou higiene",
    3 to "Banho de leito auxiliado",
    4 to "Banho de leito integral realizado totalmente pela enfermagem"
)

val fugulinEliminacaoOptions = listOf(
    1 to "Auto-suficiente / Usa sanitário",
    2 to "Uso de comadre ou papagaio no leito com auxílio",
    3 to "Incontinência urinária ou de fezes",
    4 to "Sonda vesical ou ostomias de eliminação"
)

val fugulinNutricaoHidratacaoOptions = listOf(
    1 to "Auto-suficiente / Alimenta-se sozinho por via oral",
    2 to "Necessita auxílio para via oral",
    3 to "Alimentação enteral (sonda nasoenteral / gastrostomia)",
    4 to "Nutrição parenteral total (NPT) ou jejum absoluto"
)

val fugulinTerapeuticaOptions = listOf(
    1 to "Apenas via oral ou nenhuma medicação",
    2 to "Via intramuscular (IM) ou subcutânea (SC)",
    3 to "Via endovenosa (EV) intermitente",
    4 to "Via endovenosa contínua / Drogas vasoativas"
)

val morseHistoricoQuedasOptions = listOf(
    0 to "Não (Sem histórico nos últimos 3 meses)",
    25 to "Sim (Queda registrada nos últimos 3 meses)"
)

val morseDiagnosticoSecundarioOptions = listOf(
    0 to "Não (Apenas um diagnóstico ativo)",
    15 to "Sim (Mais de um diagnóstico ativo em prontuário)"
)

val morseAuxilioLocomocaoOptions = listOf(
    0 to "Sem auxílio / Acamado / Cadeira de rodas",
    15 to "Usa muleta, bengala ou andador",
    30 to "Apoia-se em móveis ou paredes para caminhar"
)

val morseTerapiaEVOptions = listOf(
    0 to "Não (Sem acesso salinizado/heparinizado ou equipo)",
    20 to "Sim (Possui acesso venoso heparinizado ou em infusão)"
)

val morseMarchaOptions = listOf(
    0 to "Marcha normal, acamado ou cadeira de rodas",
    10 to "Marcha fraca (lenta, passos curtos, inclinação leve)",
    20 to "Marcha limitante / cambaleante (apoia-se em objetos)"
)

val morseEstadoMentalOptions = listOf(
    0 to "Orientado e consciente das limitações",
    15 to "Superestima capacidade ou esquece as limitações"
)

val glasgowAberturaOcularOptions = listOf(
    4 to "Espontânea",
    3 to "À estimulação mecânica sonora / Voz",
    2 to "À estimulação física / Dor",
    1 to "Ausente (Sem resposta)"
)

val glasgowRespostaVerbalOptions = listOf(
    5 to "Orientada (Responde corretamente)",
    4 to "Confusa (Frases conexas, desorientação temporal)",
    3 to "Palavras inapropriadas (Incoerência)",
    2 to "Sons incompreensíveis (Gemidos, balbucios)",
    1 to "Ausente (Sem resposta)"
)

val glasgowRespostaMotoraOptions = listOf(
    6 to "Obedece a comandos verbais",
    5 to "Localiza estímulo de dor física",
    4 to "Flexão normal / Retirada do membro ao estímulo",
    3 to "Flexão anormal / Decorticação",
    2 to "Extensão anormal / Descerebração",
    1 to "Ausente (Sem resposta)"
)

// List UI Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdmissionListScreen(
    admissions: List<AdmissionRecord>,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onAddClick: () -> Unit,
    onEditClick: (AdmissionRecord) -> Unit,
    onDeleteClick: (AdmissionRecord) -> Unit,
    onViewClick: (AdmissionRecord) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredAdmissions = remember(admissions, searchQuery) {
        if (searchQuery.isBlank()) admissions
        else admissions.filter { it.nome.contains(searchQuery, ignoreCase = true) || it.prontuario.contains(searchQuery) }
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Pacientes Cirúrgicos",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Admissão e Evolução de Enfermagem",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onToggleTheme,
                        modifier = Modifier.testTag("toggle_theme_button")
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Alternar Tema",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddClick,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nova Admissão") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("nova_admissao_fab")
            )
        },
        contentWindowInsets = WindowInsets.statusBars
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("busca_paciente_input"),
                placeholder = { Text("Buscar paciente por nome ou prontuário...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpar")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredAdmissions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(80.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxSize(),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isBlank()) "Nenhum paciente admitido" else "Nenhum resultado encontrado",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isBlank()) "Registre a primeira admissão cirúrgica de enfermagem para começar." else "Tente buscar por outro termo.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        if (searchQuery.isBlank()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = onAddClick) {
                                Text("Iniciar Admissão")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredAdmissions, key = { it.id }) { admission ->
                        PatientAdmissionCard(
                            admission = admission,
                            onClick = { onViewClick(admission) },
                            onEdit = { onEditClick(admission) },
                            onDelete = { onDeleteClick(admission) }
                        )
                    }
                }
            }
        }
    }
}

// Compact clinical badges
@Composable
fun ScoreBadge(
    label: String,
    score: Int,
    color: Color
) {
    Surface(
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.7f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label: ",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Normal
            )
            Text(
                text = "$score",
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// Compact Patient view item
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientAdmissionCard(
    admission: AdmissionRecord,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("patient_card_${admission.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = admission.nome.ifBlank { "Sem Nome Informado" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Idade: ${admission.idade.ifBlank { "N/A" }} • Sexo: ${admission.sexo.ifBlank { "N/A" }} • Prontuário: ${admission.prontuario.ifBlank { "N/A" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.secondary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Cirurgia: ${admission.tipoCirurgia.ifBlank { "Não descrita" }}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Enf/Leito: ${admission.enfermaria.ifBlank { "N/A" }} / ${admission.leito.ifBlank { "N/A" }}",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                Text(
                    text = dateFormat.format(java.util.Date(admission.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Horizon list of scale summaries
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val bColor = when (admission.bradenClassification()) {
                    "Risco Alto" -> Color(0xFFD32F2F)
                    "Risco Moderado" -> Color(0xFFF57C00)
                    else -> Color(0xFF388E3C)
                }
                ScoreBadge(label = "Braden", score = admission.bradenScore(), color = bColor)

                val fColor = when (admission.fugulinClassification()) {
                    "Cuidado Intensivo", "Cuidado Semi-Intensivo" -> Color(0xFFD32F2F)
                    "Cuidado de Alta Dependência" -> Color(0xFFF57C00)
                    else -> Color(0xFF388E3C)
                }
                ScoreBadge(label = "Fugulin", score = admission.fugulinScore(), color = fColor)

                val mColor = when (admission.morseClassification()) {
                    "Alto Risco" -> Color(0xFFD32F2F)
                    "Risco Moderado" -> Color(0xFFF57C00)
                    else -> Color(0xFF388E3C)
                }
                ScoreBadge(label = "Morse", score = admission.morseScore(), color = mColor)

                val gColor = when (admission.glasgowClassification()) {
                    "Grave" -> Color(0xFFD32F2F)
                    "Moderado" -> Color(0xFFF57C00)
                    else -> Color(0xFF388E3C)
                }
                ScoreBadge(label = "Glasgow", score = admission.glasgowScore(), color = gColor)

                val pColor = when {
                    admission.dorNivel >= 7 -> Color(0xFFD32F2F)
                    admission.dorNivel >= 4 -> Color(0xFFF57C00)
                    else -> Color(0xFF388E3C)
                }
                ScoreBadge(label = "Dor", score = admission.dorNivel, color = pColor)
            }
        }
    }
}


// Collapsible section layout
@Composable
fun FormSectionCard(
    title: String,
    sectionIndex: Int,
    expandedIndex: Int?,
    onHeaderClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val isExpanded = sectionIndex == expandedIndex
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        border = BorderStroke(
            1.dp, 
            if (isExpanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) 
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) MaterialTheme.colorScheme.surface 
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onHeaderClick() }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val sectionColor = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(sectionColor.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${sectionIndex + 1}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = sectionColor
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                ) {
                    Divider(modifier = Modifier.padding(bottom = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    content()
                }
            }
        }
    }
}


// Helper to toggle a clinical option inside a comma-separated list
fun toggleSystemOption(current: String, option: String): String {
    val parts = current.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
    val matchIndex = parts.indexOfFirst { it.equals(option, ignoreCase = true) }
    if (matchIndex != -1) {
        parts.removeAt(matchIndex)
    } else {
        parts.add(option)
    }
    return parts.joinToString(", ")
}


fun toggleUrinaryOption(current: String, option: String): String {
    val parts = current.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
    if (option == "Cateterismo vesical intermitente") {
        val idx = parts.indexOfFirst { it.startsWith("Cateterismo vesical intermitente", ignoreCase = true) }
        if (idx != -1) {
            parts.removeAt(idx)
        } else {
            parts.add("Cateterismo vesical intermitente")
        }
    } else {
        val idx = parts.indexOfFirst { it.equals(option, ignoreCase = true) }
        if (idx != -1) {
            parts.removeAt(idx)
        } else {
            parts.add(option)
        }
    }
    return parts.joinToString(", ")
}


fun toggleIntestinalOption(current: String, option: String): String {
    val parts = current.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
    if (option == "Evacuações ausentes") {
        val idx = parts.indexOfFirst { it.startsWith("Evacuações ausentes", ignoreCase = true) }
        if (idx != -1) {
            parts.removeAt(idx)
        } else {
            parts.add("Evacuações ausentes")
            val presIdx = parts.indexOfFirst { it.equals("Evacuações presentes", ignoreCase = true) }
            if (presIdx != -1) parts.removeAt(presIdx)
        }
    } else if (option == "Evacuações presentes") {
        val idx = parts.indexOfFirst { it.equals("Evacuações presentes", ignoreCase = true) }
        if (idx != -1) {
            parts.removeAt(idx)
        } else {
            parts.add("Evacuações presentes")
            val absIdx = parts.indexOfFirst { it.startsWith("Evacuações ausentes", ignoreCase = true) }
            if (absIdx != -1) parts.removeAt(absIdx)
        }
    } else {
        val idx = parts.indexOfFirst { it.equals(option, ignoreCase = true) }
        if (idx != -1) {
            parts.removeAt(idx)
        } else {
            parts.add(option)
        }
    }
    return parts.joinToString(", ")
}


fun toggleSkinOption(current: String, option: String): String {
    val parts = current.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
    if (option == "Pele íntegra") {
        val idx = parts.indexOfFirst { it.equals("Pele íntegra", ignoreCase = true) }
        if (idx != -1) {
            parts.removeAt(idx)
        } else {
            parts.clear()
            parts.add("Pele íntegra")
        }
    } else {
        val intIdx = parts.indexOfFirst { it.equals("Pele íntegra", ignoreCase = true) }
        if (intIdx != -1) {
            parts.removeAt(intIdx)
        }
        val idx = parts.indexOfFirst { it.equals(option, ignoreCase = true) }
        if (idx != -1) {
            parts.removeAt(idx)
        } else {
            parts.add(option)
        }
    }
    return parts.joinToString(", ")
}


fun selectUrinaryInterval(current: String, interval: String): String {
    val parts = current.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
    val matchIdx = parts.indexOfFirst { it.startsWith("Cateterismo vesical intermitente", ignoreCase = true) }
    if (matchIdx != -1) {
        parts[matchIdx] = "Cateterismo vesical intermitente ($interval)"
    } else {
        parts.add("Cateterismo vesical intermitente ($interval)")
    }
    return parts.joinToString(", ")
}


fun selectEvacuationDays(current: String, days: String): String {
    val parts = current.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
    val matchIdx = parts.indexOfFirst { it.startsWith("Evacuações ausentes", ignoreCase = true) }
    if (matchIdx != -1) {
        parts[matchIdx] = "Evacuações ausentes ($days)"
    } else {
        parts.add("Evacuações ausentes ($days)")
    }
    val presIdx = parts.indexOfFirst { it.equals("Evacuações presentes", ignoreCase = true) }
    if (presIdx != -1) parts.removeAt(presIdx)
    return parts.joinToString(", ")
}

@Composable
fun SystemOptionsGrid(
    title: String,
    options: List<String>,
    currentValue: String,
    onValueChange: (String) -> Unit,
    testTagPrefix: String
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
    )

    val selectedOptions = options.filter { option ->
        currentValue.split(",").any { it.trim().equals(option, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (i in options.indices step 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val opt1 = options[i]
                val isChecked1 = selectedOptions.contains(opt1)
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .clickable {
                            val newValue = toggleSystemOption(currentValue, opt1)
                            onValueChange(newValue)
                        }
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isChecked1,
                        onCheckedChange = {
                            val newValue = toggleSystemOption(currentValue, opt1)
                            onValueChange(newValue)
                        },
                        modifier = Modifier.testTag("${testTagPrefix}_check_${i}")
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = opt1,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (i + 1 < options.size) {
                    val opt2 = options[i + 1]
                    val isChecked2 = selectedOptions.contains(opt2)
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {
                                val newValue = toggleSystemOption(currentValue, opt2)
                                onValueChange(newValue)
                            }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked2,
                            onCheckedChange = {
                                val newValue = toggleSystemOption(currentValue, opt2)
                                onValueChange(newValue)
                            },
                            modifier = Modifier.testTag("${testTagPrefix}_check_${i + 1}")
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = opt2,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}


// Reusable form fields with speech-to-text voice transcription
@Composable
fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    testTag: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    showMic: Boolean = true
) {
    val context = LocalContext.current
    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenResults = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = spokenResults?.firstOrNull()
            if (!spokenText.isNullOrEmpty()) {
                val newVal = if (value.isNotBlank()) "$value $spokenText" else spokenText
                onValueChange(newVal)
            }
        }
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
            .padding(vertical = 4.dp),
        label = { Text(label) },
        placeholder = { if (placeholder.isNotEmpty()) Text(placeholder) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = singleLine,
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
        ),
        trailingIcon = if (showMic) {
            {
                IconButton(
                    onClick = {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "pt-BR")
                            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, true)
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Fale agora em português brasileiro...")
                        }
                        try {
                            voiceLauncher.launch(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Microfone/reconhecedor indisponível. Por favor digite.", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Falar para transcrever em $label",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        } else null
    )
}

// Reusable scale score card with classification
@Composable
fun ScaleScoreCard(
    title: String,
    score: Int,
    classification: String,
    color: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Surface(
                    color = color.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, color.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Escore: $score ($classification)",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}


// Interactive Scale Option Picker
@Composable
fun ScaleItemSelector(
    label: String,
    selectedValue: Int,
    options: List<Pair<Int, String>>,
    onValueSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedOption = options.find { it.first == selectedValue } ?: options.first()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = label,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Selecionado: ${selectedOption.first} - ${selectedOption.second}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Divider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    options.forEach { option ->
                        val isSelected = option.first == selectedValue
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                .clickable {
                                    onValueSelected(option.first)
                                    expanded = false
                                }
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    onValueSelected(option.first)
                                    expanded = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${option.first} • ${option.second}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}


// Centralized Form Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdmissionFormScreen(
    record: AdmissionRecord,
    isEditMode: Boolean,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onUpdateRecord: ((AdmissionRecord) -> AdmissionRecord) -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var expandedSection by remember { mutableStateOf<Int?>(0) }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Editar Admissão" else "Nova Admissão") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(
                        onClick = onToggleTheme,
                        modifier = Modifier.testTag("toggle_theme_button")
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Alternar Tema",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = onSaveClick,
                        modifier = Modifier.testTag("salvar_admissao_botao")
                    ) {
                        Text(
                            text = "SALVAR",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.statusBars
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Card 1: Paciente
            FormSectionCard(
                title = "Identificação do Paciente",
                sectionIndex = 0,
                expandedIndex = expandedSection,
                onHeaderClick = { expandedSection = if (expandedSection == 0) null else 0 }
            ) {
                FormTextField(
                    value = record.nome,
                    onValueChange = { new -> onUpdateRecord { it.copy(nome = new) } },
                    label = "Nome Completo",
                    placeholder = "Informe o nome do paciente",
                    testTag = "nome_paciente_input"
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        FormTextField(
                            value = record.idade,
                            onValueChange = { new -> onUpdateRecord { it.copy(idade = new) } },
                            label = "Idade",
                            placeholder = "Anos",
                            keyboardType = KeyboardType.Number,
                            testTag = "idade_paciente_input"
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        FormTextField(
                            value = record.sexo,
                            onValueChange = { new -> onUpdateRecord { it.copy(sexo = new) } },
                            label = "Sexo",
                            placeholder = "F / M",
                            testTag = "sexo_paciente_input"
                        )
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        FormTextField(
                            value = record.religiao,
                            onValueChange = { new -> onUpdateRecord { it.copy(religiao = new) } },
                            label = "Religião",
                            placeholder = "Crença religiosa"
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        FormTextField(
                            value = record.estadoCivil,
                            onValueChange = { new -> onUpdateRecord { it.copy(estadoCivil = new) } },
                            label = "Estado Civil",
                            placeholder = "Ex. Solteiro"
                        )
                    }
                }
                FormTextField(
                    value = record.funcaoLaboral,
                    onValueChange = { new -> onUpdateRecord { it.copy(funcaoLaboral = new) } },
                    label = "Função Laboral",
                    placeholder = "Profissão / Ocupação"
                )
                FormTextField(
                    value = record.proveniencia,
                    onValueChange = { new -> onUpdateRecord { it.copy(proveniencia = new) } },
                    label = "Proveniência",
                    placeholder = "Ex. Pronto Socorro, Ambulatório, Casa"
                )
                FormTextField(
                    value = record.prontuario,
                    onValueChange = { new -> onUpdateRecord { it.copy(prontuario = new) } },
                    label = "Número do Prontuário",
                    placeholder = "Registro hospitalar",
                    keyboardType = KeyboardType.Number
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        FormTextField(
                            value = record.enfermaria,
                            onValueChange = { new -> onUpdateRecord { it.copy(enfermaria = new) } },
                            label = "Enfermaria",
                            placeholder = "Ala / Bloco"
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        FormTextField(
                            value = record.leito,
                            onValueChange = { new -> onUpdateRecord { it.copy(leito = new) } },
                            label = "Leito",
                            placeholder = "Nº do leito"
                        )
                    }
                }
                FormTextField(
                    value = record.tipoCirurgia,
                    onValueChange = { new -> onUpdateRecord { it.copy(tipoCirurgia = new) } },
                    label = "Tipo de Cirurgia planejada",
                    placeholder = "Ex. Apendicectomia, Colecistectomia"
                )
            }

            // Card 2: Sistemas Orgânicos & Dispositivos
            FormSectionCard(
                title = "Sistemas Orgânicos & Dispositivos",
                sectionIndex = 1,
                expandedIndex = expandedSection,
                onHeaderClick = { expandedSection = if (expandedSection == 1) null else 1 }
            ) {
                Text(
                    text = "Preencha as condições específicas de cada sistema orgânico do paciente:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                SystemOptionsGrid(
                    title = "Sistema Locomotor - Mobilidade/Locomoção",
                    options = listOf(
                        "Deambula sem auxílio",
                        "Deambula com auxílio",
                        "Cadeirante",
                        "Uso de muletas",
                        "Uso de andador",
                        "Restrito ao leito"
                    ),
                    currentValue = record.sistemaLocomotor,
                    onValueChange = { newVal -> onUpdateRecord { it.copy(sistemaLocomotor = newVal) } },
                    testTagPrefix = "locomotor"
                )

                FormTextField(
                    value = record.sistemaLocomotor,
                    onValueChange = { new -> onUpdateRecord { it.copy(sistemaLocomotor = new) } },
                    label = "Outros detalhes do Sistema Locomotor",
                    placeholder = "Edite a descrição acima ou digite observações adicionais aqui",
                    singleLine = false,
                    testTag = "sistema_locomotor_input",
                    showMic = false
                )

                SystemOptionsGrid(
                    title = "Sistema Cardiovascular",
                    options = listOf(
                        "Alterações nas bulhas cardíacas",
                        "Uso de marcapasso ou presença de cabos de marcapasso",
                        "Paciente com válvula cardíaca"
                    ),
                    currentValue = record.sistemaCardiovascular,
                    onValueChange = { newVal -> onUpdateRecord { it.copy(sistemaCardiovascular = newVal) } },
                    testTagPrefix = "cardio"
                )

                FormTextField(
                    value = record.sistemaCardiovascular,
                    onValueChange = { new -> onUpdateRecord { it.copy(sistemaCardiovascular = new) } },
                    label = "Outros detalhes do Cardiovascular",
                    placeholder = "Edite a descrição acima ou digite observações adicionais aqui",
                    singleLine = false,
                    testTag = "sistema_cardio_input",
                    showMic = false
                )

                SystemOptionsGrid(
                    title = "Sistema Respiratório",
                    options = listOf(
                        "Eupneico",
                        "Taquipneico",
                        "Dispneico",
                        "Uso de cateter nasal",
                        "Máscara de Venturi",
                        "Traqueostomia",
                        "Tubo endotraqueal"
                    ),
                    currentValue = record.sistemaRespiratorio,
                    onValueChange = { newVal -> onUpdateRecord { it.copy(sistemaRespiratorio = newVal) } },
                    testTagPrefix = "resp"
                )

                FormTextField(
                    value = record.sistemaRespiratorio,
                    onValueChange = { new -> onUpdateRecord { it.copy(sistemaRespiratorio = new) } },
                    label = "Outros detalhes do Respiratório",
                    placeholder = "Edite a descrição acima ou digite observações adicionais aqui",
                    singleLine = false,
                    testTag = "sistema_resp_input",
                    showMic = false
                )

                SystemOptionsGrid(
                    title = "Sistema Neurológico",
                    options = listOf(
                        "Alerta",
                        "Consciente",
                        "Orientado",
                        "Desorientado",
                        "Torporoso",
                        "Agitado",
                        "Deprimido",
                        "Sonolento",
                        "In-responsivo"
                    ),
                    currentValue = record.sistemaNeurologico,
                    onValueChange = { newVal -> onUpdateRecord { it.copy(sistemaNeurologico = newVal) } },
                    testTagPrefix = "neuro"
                )

                FormTextField(
                    value = record.sistemaNeurologico,
                    onValueChange = { new -> onUpdateRecord { it.copy(sistemaNeurologico = new) } },
                    label = "Outros detalhes do Neurológico",
                    placeholder = "Edite a descrição acima ou digite observações adicionais aqui",
                    singleLine = false,
                    testTag = "sistema_neuro_input",
                    showMic = false
                )

                Text(
                    text = "Nutrição",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )

                Text(
                    text = "Via de Administração / Tipo de Dieta",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )

                val adminNutritionOptions = listOf("Oral", "Nasoenteral", "Parenteral")
                val selectedAdminNutrition = adminNutritionOptions.filter { option ->
                    record.sistemaNutricao.split(",").any { it.trim().equals(option, ignoreCase = true) }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    adminNutritionOptions.forEach { option ->
                        val isSelected = selectedAdminNutrition.contains(option)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                val newValue = toggleSystemOption(record.sistemaNutricao, option)
                                onUpdateRecord { it.copy(sistemaNutricao = newValue) }
                            },
                            label = { Text(option) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("nutrition_admin_chip_$option")
                        )
                    }
                }

                Text(
                    text = "Consistência da Dieta/Opções",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )

                val dietOptionsList = listOf(
                    "Líquida restrita",
                    "Líquida completa",
                    "Pastosa",
                    "Branda",
                    "Geral"
                )

                val selectedDiets = dietOptionsList.filter { option ->
                    record.sistemaNutricao.split(",").any { it.trim().equals(option, ignoreCase = true) }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (i in dietOptionsList.indices step 2) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val opt1 = dietOptionsList[i]
                            val isChecked1 = selectedDiets.contains(opt1)
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable {
                                        val newValue = toggleSystemOption(record.sistemaNutricao, opt1)
                                        onUpdateRecord { it.copy(sistemaNutricao = newValue) }
                                    }
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked1,
                                    onCheckedChange = {
                                        val newValue = toggleSystemOption(record.sistemaNutricao, opt1)
                                        onUpdateRecord { it.copy(sistemaNutricao = newValue) }
                                    },
                                    modifier = Modifier.testTag("nutrition_check_$i")
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(opt1, style = MaterialTheme.typography.bodySmall)
                            }

                            if (i + 1 < dietOptionsList.size) {
                                val opt2 = dietOptionsList[i + 1]
                                val isChecked2 = selectedDiets.contains(opt2)
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable {
                                            val newValue = toggleSystemOption(record.sistemaNutricao, opt2)
                                            onUpdateRecord { it.copy(sistemaNutricao = newValue) }
                                        }
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isChecked2,
                                        onCheckedChange = {
                                            val newValue = toggleSystemOption(record.sistemaNutricao, opt2)
                                            onUpdateRecord { it.copy(sistemaNutricao = newValue) }
                                        },
                                        modifier = Modifier.testTag("nutrition_check_${i + 1}")
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(opt2, style = MaterialTheme.typography.bodySmall)
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                FormTextField(
                    value = record.sistemaNutricao,
                    onValueChange = { new -> onUpdateRecord { it.copy(sistemaNutricao = new) } },
                    label = "Outros detalhes da Nutrição / Dieta",
                    placeholder = "Especificar jejum, restrições hídricas, etc.",
                    singleLine = false,
                    testTag = "sistema_nutricao_input",
                    showMic = false
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Eliminações",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )

                Text(
                    text = "1. Eliminações Urinárias",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )

                val urinaryOptions = listOf(
                    "Diurese espontânea",
                    "Sonda vesical de demora",
                    "Cistostomia",
                    "Urostomia",
                    "Nefrostomia",
                    "Cateterismo vesical intermitente"
                )

                val selectedUrinary = urinaryOptions.filter { option ->
                    record.sistemaUrinario.split(",").any { it.trim().startsWith(option, ignoreCase = true) }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (i in urinaryOptions.indices step 2) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val opt1 = urinaryOptions[i]
                            val isChecked1 = selectedUrinary.contains(opt1)
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable {
                                        val newValue = toggleUrinaryOption(record.sistemaUrinario, opt1)
                                        onUpdateRecord { it.copy(sistemaUrinario = newValue) }
                                    }
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked1,
                                    onCheckedChange = {
                                        val newValue = toggleUrinaryOption(record.sistemaUrinario, opt1)
                                        onUpdateRecord { it.copy(sistemaUrinario = newValue) }
                                    },
                                    modifier = Modifier.testTag("urinary_check_$i")
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(opt1, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                            }

                            if (i + 1 < urinaryOptions.size) {
                                val opt2 = urinaryOptions[i + 1]
                                val isChecked2 = selectedUrinary.contains(opt2)
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable {
                                            val newValue = toggleUrinaryOption(record.sistemaUrinario, opt2)
                                            onUpdateRecord { it.copy(sistemaUrinario = newValue) }
                                        }
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isChecked2,
                                        onCheckedChange = {
                                            val newValue = toggleUrinaryOption(record.sistemaUrinario, opt2)
                                            onUpdateRecord { it.copy(sistemaUrinario = newValue) }
                                        },
                                        modifier = Modifier.testTag("urinary_check_${i + 1}")
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(opt2, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    val isCatheterizationSelected = record.sistemaUrinario.contains("Cateterismo vesical intermitente", ignoreCase = true)
                    if (isCatheterizationSelected) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Frequência do Cateterismo Vesical Intermitente:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                        val intervals = listOf(
                            "de quatro em quatro horas",
                            "de seis em seis horas",
                            "de oito em oito horas",
                            "de doze em doze horas"
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            intervals.forEachIndexed { index, interval ->
                                val isSelectedInterval = record.sistemaUrinario.contains(interval, ignoreCase = true)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val newValue = selectUrinaryInterval(record.sistemaUrinario, interval)
                                            onUpdateRecord { it.copy(sistemaUrinario = newValue) }
                                        }
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelectedInterval,
                                        onClick = {
                                            val newValue = selectUrinaryInterval(record.sistemaUrinario, interval)
                                            onUpdateRecord { it.copy(sistemaUrinario = newValue) }
                                        },
                                        modifier = Modifier.testTag("urinary_interval_radio_$index")
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    val readableInterval = when(interval) {
                                        "de quatro em quatro horas" -> "De 4 em 4 horas (4/4h)"
                                        "de seis em seis horas" -> "De 6 em 6 horas (6/6h)"
                                        "de oito em oito horas" -> "De 8 em 8 horas (8/8h)"
                                        "de doze em doze horas" -> "De 12 em 12 horas (12/12h)"
                                        else -> interval
                                    }
                                    Text(readableInterval, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                }

                FormTextField(
                    value = record.sistemaUrinario,
                    onValueChange = { new -> onUpdateRecord { it.copy(sistemaUrinario = new) } },
                    label = "Outros detalhes - Urinário",
                    placeholder = "Edite a descrição acima ou digite observações adicionais aqui",
                    singleLine = false,
                    testTag = "sistema_urinario_input",
                    showMic = false
                )

                Text(
                    text = "2. Eliminações Intestinais",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                )

                val intestinalOptions = listOf(
                    "Evacuações presentes",
                    "Evacuações ausentes",
                    "Colostomia",
                    "Ileostomia",
                    "Fístula intestinal"
                )

                val selectedIntestinal = intestinalOptions.filter { option ->
                    record.sistemaIntestinal.split(",").any { it.trim().startsWith(option, ignoreCase = true) }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (i in intestinalOptions.indices step 2) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val opt1 = intestinalOptions[i]
                            val isChecked1 = selectedIntestinal.contains(opt1)
                            val displayOpt1 = opt1
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable {
                                        val newValue = toggleIntestinalOption(record.sistemaIntestinal, opt1)
                                        onUpdateRecord { it.copy(sistemaIntestinal = newValue) }
                                    }
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked1,
                                    onCheckedChange = {
                                        val newValue = toggleIntestinalOption(record.sistemaIntestinal, opt1)
                                        onUpdateRecord { it.copy(sistemaIntestinal = newValue) }
                                    },
                                    modifier = Modifier.testTag("intestinal_check_$i")
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(displayOpt1, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                            }

                            if (i + 1 < intestinalOptions.size) {
                                val opt2 = intestinalOptions[i + 1]
                                val isChecked2 = selectedIntestinal.contains(opt2)
                                val displayOpt2 = opt2
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable {
                                            val newValue = toggleIntestinalOption(record.sistemaIntestinal, opt2)
                                            onUpdateRecord { it.copy(sistemaIntestinal = newValue) }
                                        }
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isChecked2,
                                        onCheckedChange = {
                                            val newValue = toggleIntestinalOption(record.sistemaIntestinal, opt2)
                                            onUpdateRecord { it.copy(sistemaIntestinal = newValue) }
                                        },
                                        modifier = Modifier.testTag("intestinal_check_${i + 1}")
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(displayOpt2, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    val isAbsentSelected = record.sistemaIntestinal.contains("Evacuações ausentes", ignoreCase = true)
                    if (isAbsentSelected) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Quantidade de dias ausentes:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                        val daysOptions = listOf(
                            "1 dia",
                            "2 dias",
                            "3 dias",
                            "4 dias",
                            "5 ou mais dias"
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            daysOptions.forEachIndexed { index, opt ->
                                val isSelectedDay = record.sistemaIntestinal.contains(opt, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isSelectedDay) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                        .clickable {
                                            val newValue = selectEvacuationDays(record.sistemaIntestinal, opt)
                                            onUpdateRecord { it.copy(sistemaIntestinal = newValue) }
                                        }
                                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = opt.split(" ")[0] + if (opt.contains("ou mais")) "+" else "",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelectedDay) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                FormTextField(
                    value = record.sistemaIntestinal,
                    onValueChange = { new -> onUpdateRecord { it.copy(sistemaIntestinal = new) } },
                    label = "Outros detalhes - Intestinal",
                    placeholder = "Edite a descrição acima ou digite observações adicionais aqui",
                    singleLine = false,
                    testTag = "sistema_intestinal_input",
                    showMic = false
                )

                Text(
                    text = "Integridade da Pele",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                )

                val skinOptions = listOf(
                    "Pele íntegra",
                    "Lesão por pressão",
                    "Úlcera arterial",
                    "Úlcera venosa",
                    "Lesão por fricção",
                    "Lesão por adesividade",
                    "Outras lesões"
                )

                val selectedSkin = skinOptions.filter { option ->
                    record.sistemaIntegridadePele.split(",").any { it.trim().equals(option, ignoreCase = true) }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (i in skinOptions.indices step 2) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val opt1 = skinOptions[i]
                            val isChecked1 = selectedSkin.contains(opt1)
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable {
                                        val newValue = toggleSkinOption(record.sistemaIntegridadePele, opt1)
                                        onUpdateRecord { it.copy(sistemaIntegridadePele = newValue) }
                                    }
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked1,
                                    onCheckedChange = {
                                        val newValue = toggleSkinOption(record.sistemaIntegridadePele, opt1)
                                        onUpdateRecord { it.copy(sistemaIntegridadePele = newValue) }
                                    },
                                    modifier = Modifier.testTag("skin_check_$i")
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(opt1, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                            }

                            if (i + 1 < skinOptions.size) {
                                val opt2 = skinOptions[i + 1]
                                val isChecked2 = selectedSkin.contains(opt2)
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable {
                                            val newValue = toggleSkinOption(record.sistemaIntegridadePele, opt2)
                                            onUpdateRecord { it.copy(sistemaIntegridadePele = newValue) }
                                        }
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isChecked2,
                                        onCheckedChange = {
                                            val newValue = toggleSkinOption(record.sistemaIntegridadePele, opt2)
                                            onUpdateRecord { it.copy(sistemaIntegridadePele = newValue) }
                                        },
                                        modifier = Modifier.testTag("skin_check_${i + 1}")
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(opt2, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                FormTextField(
                    value = record.sistemaIntegridadePele,
                    onValueChange = { new -> onUpdateRecord { it.copy(sistemaIntegridadePele = new) } },
                    label = "Outros detalhes da Pele",
                    placeholder = "Edite a descrição acima ou digite observações adicionais aqui",
                    singleLine = false,
                    testTag = "sistema_pele_input",
                    showMic = false
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Planimetria & Foto Canvas
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Planimetria e Registro de Lesão",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Planimetria e Foto da Lesão de Pele",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Dimensions input
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = record.lesionWidthCm,
                                onValueChange = { newVal ->
                                    val cleaned = newVal.replace(",", ".")
                                    val wVal = cleaned.toDoubleOrNull()
                                    val hVal = record.lesionHeightCm.replace(",", ".").toDoubleOrNull()
                                    val areaStr = if (wVal != null && hVal != null) {
                                        String.format(Locale.US, "%.2f", wVal * hVal)
                                    } else ""
                                    onUpdateRecord { 
                                        it.copy(
                                            lesionWidthCm = newVal,
                                            lesionAreaSquareCm = areaStr
                                        )
                                    }
                                },
                                label = { Text("Largura (cm)") },
                                placeholder = { Text("Ex. 2.5") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f).testTag("lesion_width_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )

                            OutlinedTextField(
                                value = record.lesionHeightCm,
                                onValueChange = { newVal ->
                                    val cleaned = newVal.replace(",", ".")
                                    val hVal = cleaned.toDoubleOrNull()
                                    val wVal = record.lesionWidthCm.replace(",", ".").toDoubleOrNull()
                                    val areaStr = if (wVal != null && hVal != null) {
                                        String.format(Locale.US, "%.2f", wVal * hVal)
                                    } else ""
                                    onUpdateRecord { 
                                        it.copy(
                                            lesionHeightCm = newVal,
                                            lesionAreaSquareCm = areaStr
                                        )
                                    }
                                },
                                label = { Text("Comprimento (cm)") },
                                placeholder = { Text("Ex. 3.0") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f).testTag("lesion_height_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )
                        }

                        if (record.lesionAreaSquareCm.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SquareFoot,
                                    contentDescription = "Área da lesão",
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Área Estimada da Lesão: ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "${record.lesionAreaSquareCm} cm²",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        FormTextField(
                            value = record.lesionDescriptionPlanimetria,
                            onValueChange = { new -> onUpdateRecord { it.copy(lesionDescriptionPlanimetria = new) } },
                            label = "Descrição/Localização da lesão",
                            placeholder = "Localização anatômica, presença de exsudato, tecido de granulação, etc.",
                            singleLine = false,
                            testTag = "lesion_description_planimetria_input"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Photo Picker launcher
                        val photoLauncher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.GetContent()
                        ) { uri: Uri? ->
                            if (uri != null) {
                                onUpdateRecord { it.copy(lesionPhotoUri = uri.toString()) }
                            }
                        }

                        Text(
                            text = "Foto da Lesão",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { photoLauncher.launch("image/*") },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.testTag("upload_lesion_photo_button")
                            ) {
                                Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Registrar Foto", style = MaterialTheme.typography.labelMedium)
                            }

                            Button(
                                onClick = {
                                    onUpdateRecord { it.copy(lesionPhotoUri = "mock_lesion_pattern_1") }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                modifier = Modifier.testTag("mock_lesion_photo_button")
                            ) {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Gerar Mock", style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Render preview
                        if (record.lesionPhotoUri != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                if (record.lesionPhotoUri!!.startsWith("mock_lesion_pattern_1")) {
                                    androidx.compose.foundation.Canvas(
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        drawRect(
                                            color = Color(0xFFFFECEE)
                                        )
                                        val step = 40f
                                        for (x in 0..(size.width.toInt()) step step.toInt()) {
                                            drawLine(
                                                color = Color(0xFFFFCAD1),
                                                start = androidx.compose.ui.geometry.Offset(x.toFloat(), 0f),
                                                end = androidx.compose.ui.geometry.Offset(x.toFloat(), size.height),
                                                strokeWidth = 1f
                                            )
                                        }
                                        for (y in 0..(size.height.toInt()) step step.toInt()) {
                                            drawLine(
                                                color = Color(0xFFFFCAD1),
                                                start = androidx.compose.ui.geometry.Offset(0f, y.toFloat()),
                                                end = androidx.compose.ui.geometry.Offset(size.width, y.toFloat()),
                                                strokeWidth = 1f
                                            )
                                        }
                                        drawCircle(
                                            color = Color(0xFFD32F2F),
                                            radius = 70f,
                                            center = center,
                                            alpha = 0.85f
                                        )
                                        drawCircle(
                                            color = Color(0xFFFF8A80),
                                            radius = 45f,
                                            center = center,
                                            alpha = 0.9f
                                        )
                                        drawCircle(
                                            color = Color(0xFFF48FB1),
                                            radius = 20f,
                                            center = center,
                                            alpha = 0.95f
                                        )
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .fillMaxWidth()
                                            .background(Color.Black.copy(alpha = 0.65f))
                                            .padding(4.dp)
                                    ) {
                                        Text(
                                            text = "Esquema da Lesão (Simulado)",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                } else {
                                    AsyncImage(
                                        model = record.lesionPhotoUri,
                                        contentDescription = "Foto da Lesão",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                }

                                IconButton(
                                    onClick = { onUpdateRecord { it.copy(lesionPhotoUri = null) } },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                        .size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remover foto",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Acessos Vasculares & Dispositivos Ativos",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                )

                val deviceOptionsList = listOf(
                    "Acesso periférico em membro superior direito",
                    "Acesso periférico em membro superior esquerdo",
                    "Acesso central jugular direita",
                    "Acesso central jugular esquerda",
                    "Acesso central subclávia direita",
                    "Acesso central subclávia esquerda",
                    "Acesso central femoral direita",
                    "Acesso central femoral esquerda",
                    "Cateter de diálise",
                    "FAV"
                )

                val selectedDevices = deviceOptionsList.filter { option ->
                    record.dispositivos.split(",").any { it.trim().equals(option, ignoreCase = true) }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    deviceOptionsList.forEachIndexed { idx, opt ->
                        val isChecked = selectedDevices.contains(opt)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .clickable {
                                    val newValue = toggleSystemOption(record.dispositivos, opt)
                                    onUpdateRecord { it.copy(dispositivos = newValue) }
                                }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = {
                                    val newValue = toggleSystemOption(record.dispositivos, opt)
                                    onUpdateRecord { it.copy(dispositivos = newValue) }
                                },
                                modifier = Modifier.testTag("device_check_$idx")
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(opt, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                FormTextField(
                    value = record.dispositivos,
                    onValueChange = { new -> onUpdateRecord { it.copy(dispositivos = new) } },
                    label = "Outros Dispositivos / Detalhes dos Acessos",
                    placeholder = "Especificar calibres, datas de inserção, drenos, etc.",
                    singleLine = false,
                    testTag = "dispositivos_input",
                    showMic = false
                )
            }

            // Card 3: Histórico Clínico & Alergias
            FormSectionCard(
                title = "Histórico Clínico, Alergias & Exames",
                sectionIndex = 2,
                expandedIndex = expandedSection,
                onHeaderClick = { expandedSection = if (expandedSection == 2) null else 2 }
            ) {
                FormTextField(
                    value = record.doencasAnteriores,
                    onValueChange = { new -> onUpdateRecord { it.copy(doencasAnteriores = new) } },
                    label = "Doenças Anteriores",
                    placeholder = "Ex. HAS, Diabetes, Insuficiência Renal, Asma",
                    singleLine = false
                )
                FormTextField(
                    value = record.condicoesAnteriores,
                    onValueChange = { new -> onUpdateRecord { it.copy(condicoesAnteriores = new) } },
                    label = "Condições Anteriores",
                    placeholder = "Ex. Gestante, Tabagista, Obesidade, Imunossuprimido",
                    singleLine = false
                )
                FormTextField(
                    value = record.cirurgiasAnteriores,
                    onValueChange = { new -> onUpdateRecord { it.copy(cirurgiasAnteriores = new) } },
                    label = "Cirurgias Anteriores",
                    placeholder = "Descreva procedimentos cirúrgicos passados",
                    singleLine = false
                )
                FormTextField(
                    value = record.alergias,
                    onValueChange = { new -> onUpdateRecord { it.copy(alergias = new) } },
                    label = "Alergias conhecidas",
                    placeholder = "Ex. Dipirona, Látex, Contraste, Iodo, Penicilina",
                    singleLine = false
                )
                FormTextField(
                    value = record.medicacoesUso,
                    onValueChange = { new -> onUpdateRecord { it.copy(medicacoesUso = new) } },
                    label = "Medicações em uso contínuo",
                    placeholder = "Ex. Insulina, Captopril, AAS, Anticoagulante",
                    singleLine = false
                )
                FormTextField(
                    value = record.exames,
                    onValueChange = { new -> onUpdateRecord { it.copy(exames = new) } },
                    label = "Exames pré-operatórios",
                    placeholder = "Ex. Hemograma, ECG, Coagulograma, Raio-X",
                    singleLine = false
                )
            }

            // Card 4: Acuidades Sensoriais
            FormSectionCard(
                title = "Acuidades Sensoriais",
                sectionIndex = 3,
                expandedIndex = expandedSection,
                onHeaderClick = { expandedSection = if (expandedSection == 3) null else 3 }
            ) {
                FormTextField(
                    value = record.acuidadeVisual,
                    onValueChange = { new -> onUpdateRecord { it.copy(acuidadeVisual = new) } },
                    label = "Acuidade Visual",
                    placeholder = "Ex. Normal, Uso de óculos, Cegueira OD/OE",
                    singleLine = false
                )
                FormTextField(
                    value = record.acuidadeAuditiva,
                    onValueChange = { new -> onUpdateRecord { it.copy(acuidadeAuditiva = new) } },
                    label = "Acuidade Auditiva",
                    placeholder = "Ex. Normal, Uso de prótese, Hipoacusia bilateral",
                    singleLine = false
                )
            }

            // Card 5: Escalas Clínicas
            FormSectionCard(
                title = "Escalas Clínicas de Enfermagem",
                sectionIndex = 4,
                expandedIndex = expandedSection,
                onHeaderClick = { expandedSection = if (expandedSection == 4) null else 4 }
            ) {
                // Braden Scale
                val bradenClassificationColor = when (record.bradenClassification()) {
                    "Risco Alto" -> Color(0xFFD32F2F)
                    "Risco Moderado" -> Color(0xFFF57C00)
                    "Risco Baixo" -> Color(0xFF1976D2)
                    else -> Color(0xFF388E3C)
                }
                ScaleScoreCard(
                    title = "A) Escala de Braden",
                    score = record.bradenScore(),
                    classification = record.bradenClassification(),
                    color = bradenClassificationColor
                ) {
                    Text(
                        text = "Avalia risco para Lesão por Pressão (LPP). Um score menor indica maior risco de desenvolvimento.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ScaleItemSelector(
                        label = "Percepção Sensorial",
                        selectedValue = record.bradenPercepcaoSensorial,
                        options = bradenPercepcaoSensorialOptions,
                        onValueSelected = { new -> onUpdateRecord { it.copy(bradenPercepcaoSensorial = new) } }
                    )
                    ScaleItemSelector(
                        label = "Umidade",
                        selectedValue = record.bradenUmidade,
                        options = bradenUmidadeOptions,
                        onValueSelected = { new -> onUpdateRecord { it.copy(bradenUmidade = new) } }
                    )
                    ScaleItemSelector(
                        label = "Atividade",
                        selectedValue = record.bradenAtividade,
                        options = bradenAtividadeOptions,
                        onValueSelected = { new -> onUpdateRecord { it.copy(bradenAtividade = new) } }
                    )
                    ScaleItemSelector(
                        label = "Mobilidade",
                        selectedValue = record.bradenMobilidade,
                        options = bradenMobilidadeOptions,
                        onValueSelected = { new -> onUpdateRecord { it.copy(bradenMobilidade = new) } }
                    )
                    ScaleItemSelector(
                        label = "Nutrição",
                        selectedValue = record.bradenNutricao,
                        options = bradenNutricaoOptions,
                        onValueSelected = { new -> onUpdateRecord { it.copy(bradenNutricao = new) } }
                    )
                    ScaleItemSelector(
                        label = "Fricção e Cisalhamento",
                        selectedValue = record.bradenFriccaoCisalhamento,
                        options = bradenFriccaoCisalhamentoOptions,
                        onValueSelected = { new -> onUpdateRecord { it.copy(bradenFriccaoCisalhamento = new) } }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Fugulin Scale
                val fugulinClassificationColor = when (record.fugulinClassification()) {
                    "Cuidado Intensivo" -> Color(0xFFD32F2F)
                    "Cuidado Semi-Intensivo" -> Color(0xFFF57C00)
                    "Cuidado de Alta Dependência" -> Color(0xFFFBC02D)
                    "Cuidado Intermediário" -> Color(0xFF1976D2)
                    else -> Color(0xFF388E3C)
                }
                ScaleScoreCard(
                    title = "B) Escala de Fugulin",
                    score = record.fugulinScore(),
                    classification = record.fugulinClassification(),
                    color = fugulinClassificationColor
                ) {
                    Text(
                        text = "Classificação de dependência do paciente. Quanto maior o escore, maior o nível de dependência da enfermagem.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ScaleItemSelector(
                        label = "Estado Mental",
                        selectedValue = record.fugulinEstadoMental,
                        options = fugulinEstadoMentalOptions,
                        onValueSelected = { new -> onUpdateRecord { it.copy(fugulinEstadoMental = new) } }
                    )
                    ScaleItemSelector(
                        label = "Oxigenação",
                        selectedValue = record.fugulinOxigenacao,
                        options = fugulinOxigenacaoOptions,
                        onValueSelected = { new -> onUpdateRecord { it.copy(fugulinOxigenacao = new) } }
                    )
                    ScaleItemSelector(
                        label = "Sinais Vitais",
                        selectedValue = record.fugulinSinaisVitais,
                        options = fugulinSinaisVitaisOptions,
                        onValueSelected = { new -> onUpdateRecord { it.copy(fugulinSinaisVitais = new) } }
                    )
                    ScaleItemSelector(
                        label = "Motilidade",
                        selectedValue = record.fugulinMotilidade,
                        options = fugulinMotilidadeOptions,
                        onValueSelected = { new -> onUpdateRecord { it.copy(fugulinMotilidade = new) } }
                    )
                    ScaleItemSelector(
                        label = "Locomoção",
                        selectedValue = record.fugulinLocomocao,
                        options = fugulinLocomocaoOptions,
                        onValueSelected = { new -> onUpdateRecord { it.copy(fugulinLocomocao = new) } }
                    )
                    ScaleItemSelector(
                        label = "Cuidado Corporal",
                        selectedValue = record.fugulinCuidadoCorporal,
                        options = fugulinCuidadoCorporalOptions,
                        onValueSelected = { new -> onUpdateRecord { it.copy(fugulinCuidadoCorporal = new) } }
                    )
                    ScaleItemSelector(
                        label = "Eliminação",
                        selectedValue = record.fugulinEliminacao,
                        options = fugulinEliminacaoOptions,
                        onValueSelected = { new -> onUpdateRecord { it.copy(fugulinEliminacao = new) } }
                    )
                    ScaleItemSelector(
                        label = "Nutrição e Hidratação",
                        selectedValue = record.fugulinNutricaoHidratacao,
                        options = fugulinNutricaoHidratacaoOptions,
                        onValueSelected = { new -> onUpdateRecord { it.copy(fugulinNutricaoHidratacao = new) } }
                    )
                    ScaleItemSelector(
                        label = "Terapêutica",
                        selectedValue = record.fugulinTerapeutica,
                        options = fugulinTerapeuticaOptions,
                        onValueSelected = { new -> onUpdateRecord { it.copy(fugulinTerapeutica = new) } }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Morse Scale
                val morseClassificationColor = when (record.morseClassification()) {
                    "Alto Risco" -> Color(0xFFD32F2F)
                    "Risco Moderado" -> Color(0xFFF57C00)
                    else -> Color(0xFF388E3C)
                }
                ScaleScoreCard(
                    title = "C) Escala de Morse",
                    score = record.morseScore(),
                    classification = record.morseClassification(),
                    color = morseClassificationColor
                ) {
                    Text(
                        text = "Avalia o risco de quedas. Um escore de 45 ou superior indica alto risco físico.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ScaleItemSelector(
                        label = "Histórico de Quedas nos Últimos 3 Meses",
                        selectedValue = record.morseHistoricoQuedas,
                        options = morseHistoricoQuedasOptions,
                        onValueSelected = { new -> onUpdateRecord { it.copy(morseHistoricoQuedas = new) } }
                    )
                    ScaleItemSelector(
                        label = "Diagnóstico Secundário",
                        selectedValue = record.morseDiagnosticoSecundario,
                        options = morseDiagnosticoSecundarioOptions,
                        onValueSelected = { new -> onUpdateRecord { it.copy(morseDiagnosticoSecundario = new) } }
                    )
                    ScaleItemSelector(
                        label = "Auxílio na Locomoção (Deambulação)",
                        selectedValue = record.morseAuxilioLocomocao,
                        options = morseAuxilioLocomocaoOptions,
                        onValueSelected = { new -> onUpdateRecord { it.copy(morseAuxilioLocomocao = new) } }
                    )
                    ScaleItemSelector(
                        label = "Terapia Endovenosa / Dispositivo Venoso",
                        selectedValue = record.morseTerapiaEV,
                        options = morseTerapiaEVOptions,
                        onValueSelected = { new -> onUpdateRecord { it.copy(morseTerapiaEV = new) } }
                    )
                    ScaleItemSelector(
                        label = "Padrão de Marcha / Movimentação",
                        selectedValue = record.morseMarcha,
                        options = morseMarchaOptions,
                        onValueSelected = { new -> onUpdateRecord { it.copy(morseMarcha = new) } }
                    )
                    ScaleItemSelector(
                        label = "Estado Mental",
                        selectedValue = record.morseEstadoMental,
                        options = morseEstadoMentalOptions,
                        onValueSelected = { new -> onUpdateRecord { it.copy(morseEstadoMental = new) } }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Glasgow Scale
                val glasgowClassificationColor = when (record.glasgowClassification()) {
                    "Grave" -> Color(0xFFD32F2F)
                    "Moderado" -> Color(0xFFF57C00)
                    else -> Color(0xFF388E3C)
                }
                ScaleScoreCard(
                    title = "D) Escala de Glasgow",
                    score = record.glasgowScore(),
                    classification = record.glasgowClassification(),
                    color = glasgowClassificationColor
                ) {
                    Text(
                        text = "Avalia neurologicamente o nível de consciência (GCS). De 3 a 15 pontos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ScaleItemSelector(
                        label = "Abertura Ocular",
                        selectedValue = record.glasgowAberturaOcular,
                        options = glasgowAberturaOcularOptions,
                        onValueSelected = { new -> onUpdateRecord { it.copy(glasgowAberturaOcular = new) } }
                    )
                    ScaleItemSelector(
                        label = "Resposta Verbal",
                        selectedValue = record.glasgowRespostaVerbal,
                        options = glasgowRespostaVerbalOptions,
                        onValueSelected = { new -> onUpdateRecord { it.copy(glasgowRespostaVerbal = new) } }
                    )
                    ScaleItemSelector(
                        label = "Resposta Motora",
                        selectedValue = record.glasgowRespostaMotora,
                        options = glasgowRespostaMotoraOptions,
                        onValueSelected = { new -> onUpdateRecord { it.copy(glasgowRespostaMotora = new) } }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Pain Scale
                val painClassificationColor = when {
                    record.dorNivel >= 7 -> Color(0xFFD32F2F)
                    record.dorNivel >= 4 -> Color(0xFFF57C00)
                    record.dorNivel >= 1 -> Color(0xFF1976D2)
                    else -> Color(0xFF388E3C)
                }
                ScaleScoreCard(
                    title = "E) Escala de Dor",
                    score = record.dorNivel,
                    classification = record.dorClassification(),
                    color = painClassificationColor
                ) {
                    Text(
                        text = "Mensura o nível de dor referida pelo paciente de 0 (Sem Dor) a 10 (Dor Intensa, pior possível).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Slider(
                        value = record.dorNivel.toFloat(),
                        onValueChange = { onUpdateRecord { rec -> rec.copy(dorNivel = it.toInt()) } },
                        valueRange = 0f..10f,
                        steps = 9,
                        modifier = Modifier.testTag("dor_nivel_slider")
                    )
                    FormTextField(
                        value = record.dorLocalizacao,
                        onValueChange = { new -> onUpdateRecord { it.copy(dorLocalizacao = new) } },
                        label = "Localização da Dor",
                        placeholder = "Ex. Quadrante inferior direito do abdômen"
                    )
                    FormTextField(
                        value = record.dorCaracteristicas,
                        onValueChange = { new -> onUpdateRecord { it.copy(dorCaracteristicas = new) } },
                        label = "Características",
                        placeholder = "Ex. Pontada, Latejante, Constante, Queimação"
                    )
                }
            }

            // Card 6: Planilha de Lesões de Pele & Registro Fotográfico
            FormSectionCard(
                title = "6. Planilha de Lesões de Pele & Registro Fotográfico",
                sectionIndex = 5,
                expandedIndex = expandedSection,
                onHeaderClick = { expandedSection = if (expandedSection == 5) null else 5 }
            ) {
                val lesions = record.getLesionsList()
                val editingLesionState = remember { mutableStateOf<SkinLesionRow?>(null) }
                val editingLesion = editingLesionState.value
                
                if (editingLesion != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (lesions.none { it.id == editingLesion.id }) "Adicionar Linha na Planilha" else "Editar Linha da Planilha",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            FormTextField(
                                value = editingLesion.dataRegistro,
                                onValueChange = { editingLesionState.value = editingLesion.copy(dataRegistro = it) },
                                label = "Data do Registro",
                                placeholder = "Ex. 17/06/2026",
                                showMic = true
                            )
                            
                            FormTextField(
                                value = editingLesion.localizacao,
                                onValueChange = { editingLesionState.value = editingLesion.copy(localizacao = it) },
                                label = "Localização Anatômica (Região)",
                                placeholder = "Ex. Calcâneo Direito, Sacral, etc.",
                                showMic = true
                            )
                            
                            FormTextField(
                                value = editingLesion.tipoLesao,
                                onValueChange = { editingLesionState.value = editingLesion.copy(tipoLesao = it) },
                                label = "Tipo de Lesão",
                                placeholder = "Ex. LPP Estágio 2, Lesão por fricção",
                                showMic = true
                            )
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(modifier = Modifier.weight(1f)) {
                                    FormTextField(
                                        value = editingLesion.larguraCm,
                                        onValueChange = { input -> 
                                            val cleanInput = input.replace(",", ".")
                                            val length = editingLesion.comprimentoCm
                                            val calculatedArea = try {
                                                val w = cleanInput.toDouble()
                                                val h = length.toDouble()
                                                String.format(java.util.Locale.US, "%.2f", w * h)
                                            } catch(e: Exception) { "" }
                                            editingLesionState.value = editingLesion.copy(larguraCm = cleanInput, areaCm2 = calculatedArea) 
                                        },
                                        label = "Largura (cm)",
                                        placeholder = "Ex. 3.5",
                                        keyboardType = KeyboardType.Number,
                                        showMic = false
                                    )
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    FormTextField(
                                        value = editingLesion.comprimentoCm,
                                        onValueChange = { input -> 
                                            val cleanInput = input.replace(",", ".")
                                            val width = editingLesion.larguraCm
                                            val calculatedArea = try {
                                                val w = width.toDouble()
                                                val h = cleanInput.toDouble()
                                                String.format(java.util.Locale.US, "%.2f", w * h)
                                            } catch(e: Exception) { "" }
                                            editingLesionState.value = editingLesion.copy(comprimentoCm = cleanInput, areaCm2 = calculatedArea) 
                                        },
                                        label = "Comprimento (cm)",
                                        placeholder = "Ex. 4.2",
                                        keyboardType = KeyboardType.Number,
                                        showMic = false
                                    )
                                }
                            }
                            
                            if (editingLesion.areaCm2.isNotBlank()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.GridOn, 
                                            contentDescription = null, 
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Área Planimétrica: ${editingLesion.areaCm2} cm²",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                            
                            FormTextField(
                                value = editingLesion.tipoTecido,
                                onValueChange = { editingLesionState.value = editingLesion.copy(tipoTecido = it) },
                                label = "Tipo de Tecido Dominante",
                                placeholder = "Ex. Granulação, Necrose, Esfacelo",
                                showMic = true
                            )
                            
                            FormTextField(
                                value = editingLesion.exsudato,
                                onValueChange = { editingLesionState.value = editingLesion.copy(exsudato = it) },
                                label = "Exsudato / Secreção",
                                placeholder = "Ex. Ausente, Seroso, Purulento",
                                showMic = true
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Registro Fotográfico da Lesão",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            
                            val photoLauncher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.GetContent()
                            ) { uri: Uri? ->
                                uri?.let {
                                    editingLesionState.value = editingLesion.copy(fotoUri = it.toString())
                                }
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (editingLesion.fotoUri != null) {
                                    if (editingLesion.fotoUri!!.startsWith("simulated_camera_wound_")) {
                                        Box(
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
                                                .border(1.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(8.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.GridOn, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(24.dp))
                                        }
                                    } else {
                                        AsyncImage(
                                            model = editingLesion.fotoUri,
                                            contentDescription = "Foto da Lesão",
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                                    }
                                }
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Button(
                                        onClick = { photoLauncher.launch("image/*") },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                    ) {
                                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Selecionar Foto", style = MaterialTheme.typography.labelSmall)
                                    }
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    Button(
                                        onClick = {
                                            editingLesionState.value = editingLesion.copy(fotoUri = "simulated_camera_wound_${System.currentTimeMillis()}")
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                                    ) {
                                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Simular Foto Câmera", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { editingLesionState.value = null },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Cancelar")
                                }
                                Button(
                                    onClick = {
                                        val currentList = lesions.toMutableList()
                                        val index = currentList.indexOfFirst { it.id == editingLesion.id }
                                        if (index >= 0) {
                                            currentList[index] = editingLesion
                                        } else {
                                            currentList.add(editingLesion)
                                        }
                                        onUpdateRecord { it.withUpdatedLesions(currentList) }
                                        editingLesionState.value = null
                                    },
                                    modifier = Modifier.weight(1.5f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Salvar na Planilha", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        text = "REGISTROS PLANILHADOS DA ADMISSÃO",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    
                    if (lesions.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)), RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                                Icon(Icons.Default.GridOn, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Planilha de lesões vazia. Adicione novas linhas para evolução planimétrica.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("Região / Tipo", Modifier.weight(1.3f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                                    Text("Dimensões", Modifier.weight(1f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                                    Text("Tecido/Exs", Modifier.weight(1.2f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                                    Text("Foto", Modifier.weight(0.6f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
                                    Text("Ações", Modifier.weight(0.7f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.End)
                                }
                                
                                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                                
                                lesions.forEachIndexed { idx, lesion ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                if (idx % 2 == 0) Color.Transparent 
                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                                            )
                                            .padding(vertical = 8.dp, horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Column(Modifier.weight(1.3f)) {
                                            Text(lesion.localizacao.ifBlank { "Sacra" }, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text(lesion.tipoLesao.ifBlank { "LPP" }, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                        
                                        Column(Modifier.weight(1f)) {
                                            Text("${lesion.larguraCm}x${lesion.comprimentoCm} cm", style = MaterialTheme.typography.bodySmall)
                                            if (lesion.areaCm2.isNotBlank()) {
                                                Text("${lesion.areaCm2} cm²", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                        
                                        Column(Modifier.weight(1.2f)) {
                                            Text(lesion.tipoTecido.ifBlank { "Granulação" }, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text(lesion.exsudato.ifBlank { "Nenhum" }, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                        
                                        Box(Modifier.weight(0.6f), contentAlignment = Alignment.Center) {
                                            if (lesion.fotoUri != null) {
                                                if (lesion.fotoUri!!.startsWith("simulated_camera_wound_")) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(24.dp)
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
                                                            .border(1.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(4.dp)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(Icons.Default.GridOn, contentDescription = "Simulado", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(12.dp))
                                                    }
                                                } else {
                                                    AsyncImage(
                                                        model = lesion.fotoUri,
                                                        contentDescription = "Foto da Lesão",
                                                        modifier = Modifier
                                                            .size(24.dp)
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                                                    )
                                                }
                                            } else {
                                                Icon(Icons.Default.NoPhotography, contentDescription = "Sem Foto", tint = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                        
                                        Row(
                                            modifier = Modifier.weight(0.7f),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            IconButton(
                                                onClick = { editingLesionState.value = lesion },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                            }
                                            Spacer(modifier = Modifier.width(4.dp))
                                            IconButton(
                                                onClick = {
                                                    val newList = lesions.filter { it.id != lesion.id }
                                                    onUpdateRecord { it.withUpdatedLesions(newList) }
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Button(
                        onClick = { 
                            editingLesionState.value = SkinLesionRow(
                                dataRegistro = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
                            ) 
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Adicionar Linha na Planilha", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Button(
                onClick = onSaveClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("salvar_formulario_botao"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Gerar Relatório de Admissão", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}


// Final Report copyable Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportPreviewScreen(
    record: AdmissionRecord,
    aiLoading: Boolean,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onGenerateAiClick: (AdmissionRecord) -> Unit,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val report = remember(record) { record.generateReport() }

    var isSpeaking by remember { mutableStateOf(false) }
    val tts = remember {
        var ttsInstance: android.speech.tts.TextToSpeech? = null
        ttsInstance = android.speech.tts.TextToSpeech(context) { status ->
            if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                ttsInstance?.language = Locale("pt", "BR")
            }
        }
        ttsInstance
    }

    DisposableEffect(Unit) {
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Relatório Clínico") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(
                        onClick = onToggleTheme,
                        modifier = Modifier.testTag("toggle_theme_button")
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Alternar Tema",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir")
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.statusBars
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // AI Smart Evaluation Panel
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .border(
                        width = if (record.aiInterventionsResult.isNotBlank()) 2.dp else 1.dp,
                        brush = if (record.aiInterventionsResult.isNotBlank()) {
                            androidx.compose.ui.graphics.Brush.linearGradient(
                                listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                            )
                        } else {
                            androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant)
                        },
                        shape = RoundedCornerShape(12.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) Color(0xFF131B30) else Color(0xFFEEF3FE)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Análise Clínica por IA",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Prescrição de Riscos por IA (Gemini)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        if (aiLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (record.aiInterventionsResult.isBlank()) {
                        Text(
                            text = "Gere uma análise diagnóstica avançada baseada nas escalas de risco (Braden, Fugulin, Morse, Glasgow) para identificar pontos críticos e prescrever ações preventivas automaticamente.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onGenerateAiClick(record) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            enabled = !aiLoading,
                            modifier = Modifier.fillMaxWidth().testTag("analisar_ia_botao")
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Gerar Prescrição Inteligente de Risco", style = MaterialTheme.typography.labelMedium)
                        }
                    } else {
                        Text(
                            text = "Evolução e intervenções preventivas traçadas com sucesso! Elas foram anexadas no final do sumário abaixo.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = { onGenerateAiClick(record) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            ),
                            enabled = !aiLoading,
                            modifier = Modifier.align(Alignment.End).testTag("reatualizar_ia_botao")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Recalcular Cuidados IA", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Resumo Formatado",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = "Pronto para cópia",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Scrollable report field
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape = RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        val reportScrollState = rememberScrollState()
                        Text(
                            text = report,
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(reportScrollState)
                                .testTag("relatorio_texto"),
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Row: Copy and Audio TTS accessibility
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(report))
                        Toast.makeText(context, "Relatório copiado com sucesso para a área de transferência!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .testTag("copiar_relatorio_botao"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Copiar")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copiar Sumário", fontWeight = FontWeight.Bold, maxLines = 1)
                }

                Button(
                    onClick = {
                        if (isSpeaking) {
                            tts?.stop()
                            isSpeaking = false
                        } else {
                            val cleanSpeakText = report
                                .replace("==================================================", " ")
                                .replace("--------------------------------------------------", " ")
                                .replace("•", " ")
                                .replace("*", " ")
                            tts?.speak(cleanSpeakText, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null)
                            isSpeaking = true
                            Toast.makeText(context, "Iniciando leitura auditiva do relatório...", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSpeaking) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                    ),
                    modifier = Modifier
                        .width(130.dp)
                        .height(54.dp)
                        .testTag("audio_relatorio_botao"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = if (isSpeaking) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = "Ouvir Relatório"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isSpeaking) "Parar" else "Ouvir", fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

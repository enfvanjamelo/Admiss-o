package com.example

import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.app.Activity
import android.net.Uri
import androidx.core.content.FileProvider
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Canvas
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.ui.draw.scale

class MainActivity : ComponentActivity() {
    private val viewModel: AdmissionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
            MyApplicationTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (isDarkTheme) {
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF2B0925), // Very deep plum/purple background
                                        Color(0xFF4F1B46), // Deep Plum / Dark Purple
                                        Color(0xFF381031)  // Mid-tone deep plum
                                    )
                                )
                            } else {
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFFBE7C6), // Cream background
                                        Color(0xFFFFECE6)  // Soft light rose wash
                                    )
                                )
                            }
                        ),
                    color = Color.Transparent
                ) {
                    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
                    val admissions by viewModel.admissions.collectAsStateWithLifecycle()
                    val currentRecord by viewModel.currentRecord.collectAsStateWithLifecycle()

                    when (val state = screenState) {
                        is ScreenState.Login -> {
                            val authError by viewModel.authError.collectAsStateWithLifecycle()
                            val authSuccessMessage by viewModel.authSuccessMessage.collectAsStateWithLifecycle()
                            val authLoading by viewModel.authLoading.collectAsStateWithLifecycle()
                            LoginScreen(
                                isDarkTheme = isDarkTheme,
                                error = authError,
                                successMessage = authSuccessMessage,
                                loading = authLoading,
                                onLogin = { email, password -> viewModel.loginProfessional(email, password) },
                                onLoginWithGoogle = { email, name -> viewModel.loginWithGoogle(email, name) },
                                onNavigateToRegister = { viewModel.navigateToRegister() },
                                onNavigateToForgotPassword = { viewModel.navigateToForgotPassword() }
                            )
                        }
                        is ScreenState.Register -> {
                            val authError by viewModel.authError.collectAsStateWithLifecycle()
                            val authLoading by viewModel.authLoading.collectAsStateWithLifecycle()
                            RegisterScreen(
                                isDarkTheme = isDarkTheme,
                                error = authError,
                                loading = authLoading,
                                onRegister = { email, password, name -> viewModel.registerProfessional(email, password, name) },
                                onNavigateToLogin = { viewModel.navigateToLogin() }
                            )
                        }
                        is ScreenState.ForgotPassword -> {
                            val authError by viewModel.authError.collectAsStateWithLifecycle()
                            val authLoading by viewModel.authLoading.collectAsStateWithLifecycle()
                            ForgotPasswordScreen(
                                isDarkTheme = isDarkTheme,
                                error = authError,
                                loading = authLoading,
                                onRecover = { email -> viewModel.recoverProfessionalPassword(email) },
                                onNavigateToLogin = { viewModel.navigateToLogin() }
                            )
                        }
                        is ScreenState.List -> {
                            val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
                            val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
                            val supabaseUrlInput by viewModel.supabaseUrlInput.collectAsStateWithLifecycle()
                            val supabaseAnonKeyInput by viewModel.supabaseAnonKeyInput.collectAsStateWithLifecycle()
                            val loggedInUserName by viewModel.loggedInUserName.collectAsStateWithLifecycle()
                            AdmissionListScreen(
                                admissions = admissions,
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = { viewModel.toggleTheme() },
                                onAddClick = { viewModel.navigateToNewForm() },
                                onEditClick = { viewModel.navigateToEditForm(it) },
                                onDeleteClick = { viewModel.deleteRecord(it) },
                                onViewClick = { viewModel.navigateToReportPreview(it) },
                                syncStatus = syncStatus,
                                isSyncing = isSyncing,
                                onSyncClick = { viewModel.syncWithSupabase() },
                                onClearSyncStatus = { viewModel.clearSyncStatus() },
                                supabaseUrlInput = supabaseUrlInput,
                                supabaseAnonKeyInput = supabaseAnonKeyInput,
                                onSaveSupabaseConfig = { url, key -> viewModel.saveSupabaseConfig(url, key) },
                                onLogout = { viewModel.logout() },
                                loggedInUserName = loggedInUserName ?: ""
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
    onViewClick: (AdmissionRecord) -> Unit,
    syncStatus: String = "",
    isSyncing: Boolean = false,
    onSyncClick: () -> Unit = {},
    onClearSyncStatus: () -> Unit = {},
    supabaseUrlInput: String = "",
    supabaseAnonKeyInput: String = "",
    onSaveSupabaseConfig: (String, String) -> Unit = { _, _ -> },
    onLogout: () -> Unit = {},
    loggedInUserName: String = ""
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var searchQuery by remember { mutableStateOf("") }
    var showSupabaseDialog by remember { mutableStateOf(false) }
    var showQrCodeDialog by remember { mutableStateOf(false) }

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
                    // QR Code Acessar no Celular Button
                    IconButton(
                        onClick = { showQrCodeDialog = true },
                        modifier = Modifier.testTag("access_mobile_qrcode_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "Acessar no Celular via QR Code",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    // Config Supabase Button
                    IconButton(
                        onClick = { showSupabaseDialog = true },
                        modifier = Modifier.testTag("supabase_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cloud,
                            contentDescription = "Configurar Supabase",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    // Supabase Cloud Sync Button
                    IconButton(
                        onClick = onSyncClick,
                        enabled = !isSyncing,
                        modifier = Modifier.testTag("supabase_sync_button")
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Sincronizar Cloud",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

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

                    if (loggedInUserName.isNotBlank()) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = "Enf. $loggedInUserName",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier.testTag("logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Sair",
                            tint = Color(0xFFFF5252)
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
            if (syncStatus.isNotBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = syncStatus,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        IconButton(
                            onClick = onClearSyncStatus,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fechar aviso",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

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

            // Quick Dashboard Count and Download CSV Button
            if (filteredAdmissions.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Total no Banco: ${filteredAdmissions.size}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Pronto para sincronização e download",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Button(
                            onClick = { shareDatabaseAsCsv(context, admissions) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("download_database_csv_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Baixar",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Exportar CSV (Download)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }

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

    if (showSupabaseDialog) {
        var urlState by remember { mutableStateOf(supabaseUrlInput) }
        var keyState by remember { mutableStateOf(supabaseAnonKeyInput) }

        AlertDialog(
            onDismissRequest = { showSupabaseDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Cloud,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Configuração Supabase",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Insira as credenciais do seu projeto Supabase para ativar a sincronização na nuvem em tempo real.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    OutlinedTextField(
                        value = urlState,
                        onValueChange = { urlState = it },
                        label = { Text("URL do Supabase") },
                        placeholder = { Text("https://xxxxxx.supabase.co") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("supabase_url_field")
                    )

                    OutlinedTextField(
                        value = keyState,
                        onValueChange = { keyState = it },
                        label = { Text("Anon Key (Public Key)") },
                        placeholder = { Text("Chave pública anônima do Supabase") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("supabase_key_field")
                    )

                    Text(
                        text = "Nota: Se você já configurou as variáveis de ambiente no painel Secrets (SUPABASE_URL e SUPABASE_ANON_KEY), esses campos sobrescrevem os valores para testes rápidos.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveSupabaseConfig(urlState, keyState)
                        showSupabaseDialog = false
                    },
                    modifier = Modifier.testTag("save_supabase_config_button")
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSupabaseDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showQrCodeDialog) {
        AlertDialog(
            onDismissRequest = { showQrCodeDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Acessar no seu Celular",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Escaneie o código QR abaixo com a câmera do seu celular para abrir o aplicativo diretamente na sua mão e usá-lo onde quiser!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    
                    Surface(
                        modifier = Modifier
                            .size(240.dp)
                            .border(BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)), RoundedCornerShape(16.dp))
                            .padding(8.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White
                    ) {
                        AsyncImage(
                            model = "https://api.qrserver.com/v1/create-qr-code/?size=400x400&data=https://ais-pre-cngyjunvct337fq3vy76xk-354777519143.us-west1.run.app",
                            contentDescription = "Código QR do aplicativo para celular",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString("https://ais-pre-cngyjunvct337fq3vy76xk-354777519143.us-west1.run.app"))
                            Toast.makeText(context, "Link copiado para a área de transferência!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth().testTag("copy_shared_url_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Copiar Link do App")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showQrCodeDialog = false },
                    modifier = Modifier.testTag("close_qrcode_dialog_button")
                ) {
                    Text("Concluído")
                }
            }
        )
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
    val isDark = MaterialTheme.colorScheme.primary == Color(0xFFA80359)
    val shape = RoundedCornerShape(20.dp)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("patient_card_${admission.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) {
                Color(0xFF101C2B).copy(alpha = 0.55f) // Deep glass cockpit
            } else {
                Color.White.copy(alpha = 0.85f)      // Luminous white frozen glass
            }
        ),
        border = BorderStroke(
            width = 1.2.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                )
            )
        ),
        shape = shape
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
                        color = MaterialTheme.colorScheme.secondary, // Bright Cyber-Cyan key accent!
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
    val isDark = MaterialTheme.colorScheme.primary == Color(0xFFA80359)
    val shape = RoundedCornerShape(16.dp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        border = BorderStroke(
            width = 1.2.dp,
            brush = if (isExpanded) {
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f)
                    )
                )
            } else {
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                    )
                )
            }
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) {
                if (isDark) Color(0xFF141D35).copy(alpha = 0.65f) else Color.White.copy(alpha = 0.85f)
            } else {
                if (isDark) Color(0xFF101625).copy(alpha = 0.45f) else Color.White.copy(alpha = 0.6f)
            }
        ),
        shape = shape
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
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SimulatedSpeechDialog(
    label: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var textInput by remember { mutableStateOf("") }
    
    val samplePhrases = remember(label) {
        val labelLower = label.lowercase()
        when {
            labelLower.contains("dor") || labelLower.contains("queixa") || labelLower.contains("sintoma") -> listOf(
                "Paciente queixa-se de dor abdominal difusa leve no quadrante inferior.",
                "Paciente refere ausência de dor ou desconforto no momento.",
                "Refere cefaleia tensional intermitente associada ao estresse."
            )
            labelLower.contains("temperatura") || labelLower.contains("pressão") || labelLower.contains("vital") || labelLower.contains("pa") || labelLower.contains("fc") || labelLower.contains("sat") || labelLower.contains("pulso") || labelLower.contains("respirat") || labelLower.contains("cm") || labelLower.contains("largura") || labelLower.contains("comprimento") -> listOf(
                "Pressão arterial de cento e vinte por oitenta milímetros de mercúrio.",
                "Temperatura corporal axilar de trinta e seis ponto seis graus celsius.",
                "Frequência cardíaca de setenta e dois batimentos por minuto, ritmo regular.",
                "Saturação de oxigênio de noventa e nove por cento em ar ambiente."
            )
            labelLower.contains("lesão") || labelLower.contains("localização") || labelLower.contains("pele") || labelLower.contains("ferida") || labelLower.contains("tecido") || labelLower.contains("exsudato") || labelLower.contains("bordas") -> listOf(
                "Lesão por pressão de estágio dois na região calcânea esquerda.",
                "Bordas regulares, tecido de granulação vermelho brilhante ocupando oitenta por cento.",
                "Presença de exsudato serossanguinolento em moderada quantidade.",
                "Presença de esfacelo amarelado aderido em base, sem odor fétido."
            )
            labelLower.contains("cirurgia") || labelLower.contains("procedimento") || labelLower.contains("diagnóst") || labelLower.contains("propost") -> listOf(
                "Procedimento cirúrgico limpo, realizado sem intercorrências anestésicas.",
                "Diagnóstico pós-operatório estável, paciente encaminhado para a sala de recuperação.",
                "Indicação de colecistectomia por videolaparoscopia por colelitíase."
            )
            labelLower.contains("observ") || labelLower.contains("evolu") || labelLower.contains("outr") || labelLower.contains("descri") || labelLower.contains("histór") -> listOf(
                "Paciente consciente, orientado, comunicativo e deambulando com amparo.",
                "Mantém acesso venoso periférico salinizado em membro superior esquerdo sem sinais de flebite.",
                "Diurese presente e espontânea, refere trânsito intestinal preservado."
            )
            else -> listOf(
                "Paciente estável, sem queixas álgicas no momento.",
                "Sinais vitais normotensivos e normocárdicos.",
                "Procedimento efetuado seguindo o protocolo institucional com sucesso."
            )
        }
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color(0xFFFF5252), androidx.compose.foundation.shape.CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ditado Clínico Assistido",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = "Ditando para: $label",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Box(
                    modifier = Modifier.size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val transition = rememberInfiniteTransition(label = "pulse")
                    val pulseScale by transition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.35f,
                        animationSpec = infiniteRepeatable(
                            animation = androidx.compose.animation.core.tween(1200, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "scale"
                    )
                    val pulseAlpha by transition.animateFloat(
                        initialValue = 0.4f,
                        targetValue = 0.0f,
                        animationSpec = infiniteRepeatable(
                            animation = androidx.compose.animation.core.tween(1200, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "alpha"
                    )

                    Box(
                        modifier = Modifier
                            .scale(pulseScale)
                            .size(60.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha),
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                    )

                    Surface(
                        modifier = Modifier.size(54.dp),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shadowElevation = 2.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Gravando",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "Diga sua anotação médica ou toque em um modelo abaixo:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    label = { Text("Texto Transcrito") },
                    placeholder = { Text("Toque em um modelo de áudio abaixo...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .testTag("dictation_transcription_input"),
                    shape = RoundedCornerShape(10.dp),
                    trailingIcon = {
                        if (textInput.isNotEmpty()) {
                            IconButton(onClick = { textInput = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpar")
                            }
                        }
                    }
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Exemplos do que você pode falar:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    samplePhrases.forEachIndexed { index, phrase ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    textInput = if (textInput.isBlank()) phrase else "$textInput $phrase"
                                }
                                .testTag("phrase_sample_$index"),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Text(
                                text = phrase,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("dictation_cancel_btn")
                    ) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(textInput) },
                        shape = RoundedCornerShape(8.dp),
                        enabled = textInput.isNotBlank(),
                        modifier = Modifier.testTag("dictation_confirm_btn")
                    ) {
                        Text("Inserir Ditado")
                    }
                }
            }
        }
    }
}

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
    var showSimulatedMicDialog by remember { mutableStateOf(false) }

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

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Fale agora em português brasileiro...")
            }
            try {
                voiceLauncher.launch(intent)
            } catch (e: Exception) {
                showSimulatedMicDialog = true
            }
        } else {
            Toast.makeText(context, "Permissão de microfone necessária para transcrição de áudio.", Toast.LENGTH_LONG).show()
        }
    }

    if (showSimulatedMicDialog) {
        SimulatedSpeechDialog(
            label = label,
            onDismiss = { showSimulatedMicDialog = false },
            onConfirm = { transcription ->
                val newVal = if (value.isNotBlank()) "$value $transcription" else transcription
                onValueChange(newVal)
                showSimulatedMicDialog = false
            }
        )
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
                        val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.RECORD_AUDIO
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                        if (hasPermission) {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "Fale agora em português brasileiro...")
                            }
                            try {
                                voiceLauncher.launch(intent)
                            } catch (e: Exception) {
                                showSimulatedMicDialog = true
                            }
                        } else {
                            permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
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
    val isDark = MaterialTheme.colorScheme.primary == Color(0xFFA80359)
    val shape = RoundedCornerShape(16.dp)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) {
                Color(0xFF101C2B).copy(alpha = 0.55f)
            } else {
                Color.White.copy(alpha = 0.75f)
            }
        ),
        shape = shape,
        border = BorderStroke(
            width = 1.2.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    color.copy(alpha = 0.8f),
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                )
            )
        )
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

    val isDark = MaterialTheme.colorScheme.primary == Color(0xFFA80359)
    val cardShape = RoundedCornerShape(14.dp)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) {
                Color(0xFF101B2B).copy(alpha = 0.5f)
            } else {
                Color.White.copy(alpha = 0.8f)
            }
        ),
        border = BorderStroke(
            width = 1.1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f)
                )
            )
        ),
        shape = cardShape
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

            // Card 1B: Sinais Vitais
            FormSectionCard(
                title = "Sinais Vitais",
                sectionIndex = 6,
                expandedIndex = expandedSection,
                onHeaderClick = { expandedSection = if (expandedSection == 6) null else 6 }
            ) {
                Text(
                    text = "Registre os sinais vitais medidos na admissão:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        FormTextField(
                            value = record.pressaoArterial,
                            onValueChange = { new -> onUpdateRecord { it.copy(pressaoArterial = new) } },
                            label = "Pressão Arterial (PA)",
                            placeholder = "Ex. 120x80 mmHg",
                            showMic = true,
                            testTag = "pressao_arterial_input"
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        FormTextField(
                            value = record.frequenciaCardiaca,
                            onValueChange = { new -> onUpdateRecord { it.copy(frequenciaCardiaca = new) } },
                            label = "Frequência Cardíaca (FC)",
                            placeholder = "Ex. 80 bpm",
                            showMic = true,
                            testTag = "frequencia_cardiaca_input"
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        FormTextField(
                            value = record.frequenciaRespiratoria,
                            onValueChange = { new -> onUpdateRecord { it.copy(frequenciaRespiratoria = new) } },
                            label = "Freq. Respiratória (FR)",
                            placeholder = "Ex. 16 ipm",
                            showMic = true,
                            testTag = "frequencia_respiratoria_input"
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        FormTextField(
                            value = record.temperatura,
                            onValueChange = { new -> onUpdateRecord { it.copy(temperatura = new) } },
                            label = "Temperatura corporal (T)",
                            placeholder = "Ex. 36.5 ºC",
                            showMic = true,
                            testTag = "temperatura_input"
                        )
                    }
                }

                FormTextField(
                    value = record.saturacaoO2,
                    onValueChange = { new -> onUpdateRecord { it.copy(saturacaoO2 = new) } },
                    label = "Saturação de Oxigênio (SatO2)",
                    placeholder = "Ex. 98%",
                    showMic = true,
                    testTag = "saturacao_o2_input"
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = "Dados Antropométricos",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        FormTextField(
                            value = record.altura,
                            onValueChange = { new -> onUpdateRecord { it.copy(altura = new) } },
                            label = "Altura",
                            placeholder = "Ex. 1.75 ou 175",
                            showMic = true,
                            testTag = "altura_input"
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        FormTextField(
                            value = record.peso,
                            onValueChange = { new -> onUpdateRecord { it.copy(peso = new) } },
                            label = "Peso (kg)",
                            placeholder = "Ex. 70.5",
                            showMic = true,
                            testTag = "peso_input"
                        )
                    }
                }

                val imc = record.imcValue()
                if (imc != null) {
                    val classification = record.imcClassification()
                    val imcColor = when {
                        classification.contains("Normal") -> MaterialTheme.colorScheme.primary
                        classification.contains("Sobrepeso") -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.error
                    }

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = imcColor.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "IMC Calculado",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = String.format("%.2f kg/m²", imc),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = imcColor
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Classificação",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = classification,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = imcColor
                                )
                            }
                        }
                    }
                }
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
                    showMic = true
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
                    showMic = true
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
                    showMic = true
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
                    showMic = true
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
                    "Geral",
                    "Branda",
                    "Pastosa",
                    "Líquida restrita",
                    "Líquida completa",
                    "HAS",
                    "DM 2",
                    "DLP",
                    "DRC",
                    "laxativa",
                    "hipolipídica hipossodica",
                    "hipercalórico"
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
                    showMic = true
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
                    showMic = true
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
                    showMic = true
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
                    showMic = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Planimetria & Foto Canvas
                val isDarkPlani = MaterialTheme.colorScheme.primary == Color(0xFFA80359)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkPlani) {
                            Color(0xFF101B2B).copy(alpha = 0.5f)
                        } else {
                            Color.White.copy(alpha = 0.8f)
                        }
                    ),
                    border = BorderStroke(
                        width = 1.2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                            )
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
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

                val dispositivosList = record.getDispositivosList()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    dispositivosList.forEachIndexed { index, device ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (device.selecionado) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f) else androidx.compose.ui.graphics.Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val newList = dispositivosList.toMutableList()
                                        newList[index] = device.copy(selecionado = !device.selecionado)
                                        onUpdateRecord { it.withUpdatedDispositivos(newList) }
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = device.selecionado,
                                    onCheckedChange = { checked ->
                                        val newList = dispositivosList.toMutableList()
                                        newList[index] = device.copy(selecionado = checked ?: false)
                                        onUpdateRecord { it.withUpdatedDispositivos(newList) }
                                    },
                                    modifier = Modifier.testTag("device_check_${device.tipo}")
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = when (device.tipo) {
                                        "AVP" -> "AVP (Acesso Venoso Periférico)"
                                        "CVC" -> "CVC (Acesso Venoso Central)"
                                        "FAV MS" -> "FAV MS (Fístula Arteriovenosa em Membro Superior)"
                                        "CAT HD" -> "CAT HD (Cateter de Hemodiálise)"
                                        else -> device.tipo
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (device.selecionado) FontWeight.Bold else FontWeight.Normal
                                )
                            }

                            if (device.selecionado) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    when (device.tipo) {
                                        "AVP" -> {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = "Lado:",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                listOf("D", "E").forEach { ladoOpt ->
                                                    val isSelected = device.lado == ladoOpt
                                                    FilterChip(
                                                        selected = isSelected,
                                                        onClick = {
                                                            val newList = dispositivosList.toMutableList()
                                                            newList[index] = device.copy(lado = if (isSelected) "" else ladoOpt)
                                                            onUpdateRecord { it.withUpdatedDispositivos(newList) }
                                                        },
                                                        label = { Text(ladoOpt) },
                                                        colors = FilterChipDefaults.filterChipColors(
                                                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                        ),
                                                        modifier = Modifier.testTag("device_side_${device.tipo}_$ladoOpt")
                                                    )
                                                }
                                            }

                                            OutlinedTextField(
                                                value = device.dataPuncao,
                                                onValueChange = { valNew ->
                                                    val newList = dispositivosList.toMutableList()
                                                    newList[index] = device.copy(dataPuncao = valNew)
                                                    onUpdateRecord { it.withUpdatedDispositivos(newList) }
                                                },
                                                label = { Text("Data de Punção (____/____/____)", style = MaterialTheme.typography.bodySmall) },
                                                placeholder = { Text("Ex: DD/MM/AAAA", style = MaterialTheme.typography.bodySmall) },
                                                singleLine = true,
                                                textStyle = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.fillMaxWidth().testTag("device_puncao_${device.tipo}")
                                            )
                                        }

                                        "CVC" -> {
                                            Text(
                                                text = "Sítio de Inserção:",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    listOf("VJD", "VJE", "VSCD").forEach { siteOpt ->
                                                        val isSelected = device.lado == siteOpt
                                                        FilterChip(
                                                            selected = isSelected,
                                                            onClick = {
                                                                val newList = dispositivosList.toMutableList()
                                                                newList[index] = device.copy(lado = if (isSelected) "" else siteOpt)
                                                                onUpdateRecord { it.withUpdatedDispositivos(newList) }
                                                            },
                                                            label = { Text(siteOpt) },
                                                            colors = FilterChipDefaults.filterChipColors(
                                                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                            ),
                                                            modifier = Modifier.testTag("device_side_${device.tipo}_$siteOpt")
                                                        )
                                                    }
                                                }
                                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    listOf("VSCE", "VFD", "VFE").forEach { siteOpt ->
                                                        val isSelected = device.lado == siteOpt
                                                        FilterChip(
                                                            selected = isSelected,
                                                            onClick = {
                                                                val newList = dispositivosList.toMutableList()
                                                                newList[index] = device.copy(lado = if (isSelected) "" else siteOpt)
                                                                onUpdateRecord { it.withUpdatedDispositivos(newList) }
                                                            },
                                                            label = { Text(siteOpt) },
                                                            colors = FilterChipDefaults.filterChipColors(
                                                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                            ),
                                                            modifier = Modifier.testTag("device_side_${device.tipo}_$siteOpt")
                                                        )
                                                    }
                                                }
                                            }

                                            OutlinedTextField(
                                                value = device.dataPuncao,
                                                onValueChange = { valNew ->
                                                    val newList = dispositivosList.toMutableList()
                                                    newList[index] = device.copy(dataPuncao = valNew)
                                                    onUpdateRecord { it.withUpdatedDispositivos(newList) }
                                                },
                                                label = { Text("Data de Punção (____/____/____)", style = MaterialTheme.typography.bodySmall) },
                                                placeholder = { Text("Ex: DD/MM/AAAA", style = MaterialTheme.typography.bodySmall) },
                                                singleLine = true,
                                                textStyle = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.fillMaxWidth().testTag("device_puncao_${device.tipo}")
                                            )

                                            Text(
                                                text = "Tipo de Curativo:",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            val dressingTypes = listOf("Curativo Convencional", "Filme Transparente")
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                dressingTypes.forEach { dressOpt ->
                                                    val isSelected = device.curativoTipo == dressOpt
                                                    FilterChip(
                                                        selected = isSelected,
                                                        onClick = {
                                                            val newList = dispositivosList.toMutableList()
                                                            newList[index] = device.copy(curativoTipo = if (isSelected) "" else dressOpt)
                                                            onUpdateRecord { it.withUpdatedDispositivos(newList) }
                                                        },
                                                        label = { Text(dressOpt) },
                                                        colors = FilterChipDefaults.filterChipColors(
                                                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                        ),
                                                        modifier = Modifier.testTag("device_dress_${device.tipo}_$dressOpt")
                                                    )
                                                }
                                            }

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Box(modifier = Modifier.weight(1f)) {
                                                    OutlinedTextField(
                                                        value = device.dataTroca,
                                                        onValueChange = { valNew ->
                                                            val newList = dispositivosList.toMutableList()
                                                            newList[index] = device.copy(dataTroca = valNew)
                                                            onUpdateRecord { it.withUpdatedDispositivos(newList) }
                                                        },
                                                        label = { Text("Troca (___/___/___)", style = MaterialTheme.typography.bodySmall) },
                                                        placeholder = { Text("Ex: DD/MM/AAAA", style = MaterialTheme.typography.bodySmall) },
                                                        singleLine = true,
                                                        textStyle = MaterialTheme.typography.bodySmall,
                                                        modifier = Modifier.fillMaxWidth().testTag("device_troca_${device.tipo}")
                                                    )
                                                }
                                                Box(modifier = Modifier.weight(1f)) {
                                                    OutlinedTextField(
                                                        value = device.dataRetirada,
                                                        onValueChange = { valNew ->
                                                            val newList = dispositivosList.toMutableList()
                                                            newList[index] = device.copy(dataRetirada = valNew)
                                                            onUpdateRecord { it.withUpdatedDispositivos(newList) }
                                                        },
                                                        label = { Text("Retirada (___/___/___)", style = MaterialTheme.typography.bodySmall) },
                                                        placeholder = { Text("Ex: DD/MM/AAAA", style = MaterialTheme.typography.bodySmall) },
                                                        singleLine = true,
                                                        textStyle = MaterialTheme.typography.bodySmall,
                                                        modifier = Modifier.fillMaxWidth().testTag("device_retirada_${device.tipo}")
                                                    )
                                                }
                                            }
                                        }

                                        "FAV MS" -> {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = "Lado do Membro Superior:",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                listOf("D", "E").forEach { ladoOpt ->
                                                    val isSelected = device.lado == ladoOpt
                                                    FilterChip(
                                                        selected = isSelected,
                                                        onClick = {
                                                            val newList = dispositivosList.toMutableList()
                                                            newList[index] = device.copy(lado = if (isSelected) "" else ladoOpt)
                                                            onUpdateRecord { it.withUpdatedDispositivos(newList) }
                                                        },
                                                        label = { Text(ladoOpt) },
                                                        colors = FilterChipDefaults.filterChipColors(
                                                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                        ),
                                                        modifier = Modifier.testTag("device_side_${device.tipo}_$ladoOpt")
                                                    )
                                                }
                                            }
                                        }

                                        "CAT HD" -> {
                                            Text(
                                                text = "Sítio de Inserção:",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            val cathdSites = listOf("VJ D", "VJ E", "VSC D", "VSC E")
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                cathdSites.forEach { siteOpt ->
                                                    val isSelected = device.lado == siteOpt
                                                    FilterChip(
                                                        selected = isSelected,
                                                        onClick = {
                                                            val newList = dispositivosList.toMutableList()
                                                            newList[index] = device.copy(lado = if (isSelected) "" else siteOpt)
                                                            onUpdateRecord { it.withUpdatedDispositivos(newList) }
                                                        },
                                                        label = { Text(siteOpt) },
                                                        colors = FilterChipDefaults.filterChipColors(
                                                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                        ),
                                                        modifier = Modifier.testTag("device_side_${device.tipo}_$siteOpt")
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                FormTextField(
                    value = record.dispositivos,
                    onValueChange = { new -> onUpdateRecord { it.copy(dispositivos = new) } },
                    label = "Outros Dispositivos",
                    placeholder = "Especificar calibres, datas de inserção, drenos, etc.",
                    singleLine = false,
                    testTag = "dispositivos_input",
                    showMic = true
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = "Drenos Ativos",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                val drenosList = record.getDrenosList()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    drenosList.forEachIndexed { index, dreno ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (dreno.selecionado) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f) else androidx.compose.ui.graphics.Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val newList = drenosList.toMutableList()
                                        newList[index] = dreno.copy(selecionado = !dreno.selecionado)
                                        onUpdateRecord { it.withUpdatedDrenos(newList) }
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = dreno.selecionado,
                                    onCheckedChange = { checked ->
                                        val newList = drenosList.toMutableList()
                                        newList[index] = dreno.copy(selecionado = checked ?: false)
                                        onUpdateRecord { it.withUpdatedDrenos(newList) }
                                    },
                                    modifier = Modifier.testTag("dreno_check_$index")
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = dreno.tipo,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (dreno.selecionado) FontWeight.Bold else FontWeight.Normal
                                )
                            }

                            if (dreno.selecionado) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Side Selector (D or E) - only for those that support side
                                    val supportsSide = dreno.tipo != "Mediastino" && dreno.tipo != "Outros"
                                    if (supportsSide) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "Lado do Dreno:",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            listOf("D", "E").forEach { ladoOpt ->
                                                val isLadoSelected = dreno.lado == ladoOpt
                                                FilterChip(
                                                    selected = isLadoSelected,
                                                    onClick = {
                                                        val newList = drenosList.toMutableList()
                                                        newList[index] = dreno.copy(lado = if (isLadoSelected) "" else ladoOpt)
                                                        onUpdateRecord { it.withUpdatedDrenos(newList) }
                                                    },
                                                    label = { Text(ladoOpt) },
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                    ),
                                                    modifier = Modifier.testTag("dreno_side_${index}_$ladoOpt")
                                                )
                                            }
                                        }
                                    }

                                    // Débito and Aspecto inputs
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            OutlinedTextField(
                                                value = dreno.debito,
                                                onValueChange = { valNew ->
                                                    val newList = drenosList.toMutableList()
                                                    newList[index] = dreno.copy(debito = valNew)
                                                    onUpdateRecord { it.withUpdatedDrenos(newList) }
                                                },
                                                label = { Text("Débito", style = MaterialTheme.typography.bodySmall) },
                                                placeholder = { Text("Ex: 150ml", style = MaterialTheme.typography.bodySmall) },
                                                singleLine = true,
                                                textStyle = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.fillMaxWidth().testTag("dreno_debito_$index")
                                            )
                                        }
                                        Box(modifier = Modifier.weight(1f)) {
                                            OutlinedTextField(
                                                value = dreno.aspecto,
                                                onValueChange = { valNew ->
                                                    val newList = drenosList.toMutableList()
                                                    newList[index] = dreno.copy(aspecto = valNew)
                                                    onUpdateRecord { it.withUpdatedDrenos(newList) }
                                                },
                                                label = { Text("Aspecto", style = MaterialTheme.typography.bodySmall) },
                                                placeholder = { Text("Ex: seroso, hemático", style = MaterialTheme.typography.bodySmall) },
                                                singleLine = true,
                                                textStyle = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.fillMaxWidth().testTag("dreno_aspecto_$index")
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
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
                    value = record.historiaDoencaAtual,
                    onValueChange = { new -> onUpdateRecord { it.copy(historiaDoencaAtual = new) } },
                    label = "História da Doença Atual (HDA)",
                    placeholder = "Descreva o histórico, início dos sintomas e evolução clínica do paciente",
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
                                        showMic = true
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
                                        showMic = true
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
                        shape = RoundedCornerShape(16.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) Color(0xFF131B30).copy(alpha = 0.55f) else Color(0xFFEEF3FE).copy(alpha = 0.85f)
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

            val isDarkRpt = MaterialTheme.colorScheme.primary == Color(0xFFA80359)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkRpt) {
                        Color(0xFF101B2B).copy(alpha = 0.55f)
                    } else {
                        Color.White.copy(alpha = 0.85f)
                    }
                ),
                border = BorderStroke(
                    width = 1.2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                        )
                    )
                ),
                shape = RoundedCornerShape(20.dp)
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

            Spacer(modifier = Modifier.height(12.dp))

            // Download Report Button
            Button(
                onClick = {
                    shareReportAsFile(context, record)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("baixar_relatorio_arquivo_botao"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Download, 
                    contentDescription = "Baixar (.txt)",
                    tint = MaterialTheme.colorScheme.onTertiary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Baixar Relatório Completo (.txt)", 
                    fontWeight = FontWeight.Bold, 
                    color = MaterialTheme.colorScheme.onTertiary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * High-fidelity vector branding logo in Jetpack Compose
 */
@Composable
fun BrandedAppLogo(modifier: Modifier = Modifier) {
    androidx.compose.material3.Surface(
        color = Color.Transparent,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(130.dp)
                .border(
                    border = BorderStroke(
                        width = 4.dp,
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFD700), // Pure sparkling Gold
                                Color(0xFFC5A029), // Deep Amber Gold
                                Color(0xFFFFD700)
                            )
                        )
                    ),
                    shape = androidx.compose.foundation.shape.CircleShape
                )
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF09141D), // Dark Clinical Navy
                            Color(0xFF0C1920)  // Deep Midnight Teal
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Draw schematic bio-circuit lines in background
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                
                // Outer subtle scanner arc
                drawArc(
                    color = Color(0xFF00ADB5).copy(alpha = 0.15f),
                    startAngle = 0f,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
                )
                // Diagonal telemetry dash
                drawLine(
                    color = Color(0xFFFF5722).copy(alpha = 0.1f),
                    start = androidx.compose.ui.geometry.Offset(w * 0.1f, h * 0.1f),
                    end = androidx.compose.ui.geometry.Offset(w * 0.9f, h * 0.9f),
                    strokeWidth = 2f
                )
            }

            // Central Branded Layout Elements: Clipboard & Laser Diagnostics
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(10.dp)
            ) {
                // Interactive clinical tablet
                Box(
                    modifier = Modifier
                        .size(width = 50.dp, height = 70.dp)
                        .background(
                            color = Color(0xFF102A38),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            border = BorderStroke(
                                1.5.dp,
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF00F5FF), Color(0xFF008B8B))
                                )
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.TopCenter
                ) {
                    // Clipboard metallic head
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(width = 24.dp, height = 6.dp)
                            .background(Color(0xFF335566), RoundedCornerShape(2.dp))
                    )

                    // Inner vital data and red cross
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 14.dp, start = 4.dp, end = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Radiant Red Cross
                        Box(
                            modifier = Modifier.size(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(modifier = Modifier.size(width = 12.dp, height = 4.dp).background(Color(0xFFFF5252), RoundedCornerShape(1.dp)))
                            Box(modifier = Modifier.size(width = 4.dp, height = 12.dp).background(Color(0xFFFF5252), RoundedCornerShape(1.dp)))
                        }

                        // Simulated cardiac trace
                        androidx.compose.foundation.Canvas(modifier = Modifier.size(width = 32.dp, height = 10.dp)) {
                            val path = androidx.compose.ui.graphics.Path()
                            path.moveTo(0f, size.height / 2)
                            path.lineTo(6f, size.height / 2)
                            path.lineTo(10f, 2f)
                            path.lineTo(13f, size.height - 2)
                            path.lineTo(17f, size.height / 2)
                            path.lineTo(32f, size.height / 2)
                            drawPath(
                                path = path,
                                color = Color(0xFF00FFCC), // Neon cyan vital wave
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f)
                            )
                        }

                        // Digital grid telemetry dots
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                            Box(modifier = Modifier.weight(1f).height(2.dp).background(Color(0xFF00F5FF).copy(alpha = 0.5f)))
                            Box(modifier = Modifier.weight(0.7f).height(2.dp).background(Color(0xFF00F5FF).copy(alpha = 0.5f)))
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // High-strength medical scanner probe
                Box(
                    modifier = Modifier
                        .size(width = 12.dp, height = 62.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFFD4E157), Color(0xFF00838F))
                            ),
                            shape = RoundedCornerShape(3.dp)
                        )
                        .border(1.dp, Color(0xFFFF9800).copy(alpha = 0.5f), RoundedCornerShape(3.dp)),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top optical lens trigger
                        Box(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .size(6.dp)
                                .background(Color(0xFFFFD700), androidx.compose.foundation.shape.CircleShape)
                        )
                        // Laser bio-pointer tip
                        Box(
                            modifier = Modifier
                                .size(width = 4.dp, height = 4.dp)
                                .background(Color(0xFFFF5252), RoundedCornerShape(bottomStart = 1.dp, bottomEnd = 1.dp))
                        )
                    }
                }
            }
        }
    }
}

val GoogleGLogo: ImageVector
    get() = ImageVector.Builder(
        name = "GoogleGLogo",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = androidx.compose.ui.graphics.SolidColor(Color(0xFFEA4335))) { // Red
            moveTo(12.0f, 5.04f)
            curveTo(13.86f, 5.04f, 15.53f, 5.68f, 16.85f, 6.94f)
            lineTo(20.3f, 3.49f)
            curveTo(18.2f, 1.54f, 15.38f, 0.35f, 12.0f, 0.35f)
            curveTo(7.35f, 0.35f, 3.34f, 3.02f, 1.4f, 6.91f)
            lineTo(5.17f, 9.83f)
            curveTo(6.06f, 7.04f, 8.81f, 5.04f, 12.0f, 5.04f)
            close()
        }
        path(fill = androidx.compose.ui.graphics.SolidColor(Color(0xFF4285F4))) { // Blue
            moveTo(23.49f, 12.27f)
            curveTo(23.49f, 11.48f, 23.42f, 10.73f, 23.3f, 10.0f)
            lineTo(12.0f, 10.0f)
            lineTo(12.0f, 14.51f)
            lineTo(18.47f, 14.51f)
            curveTo(18.19f, 15.99f, 17.34f, 17.24f, 16.08f, 18.09f)
            lineTo(19.85f, 21.01f)
            curveTo(22.06f, 18.97f, 23.49f, 15.91f, 23.49f, 12.27f)
            close()
        }
        path(fill = androidx.compose.ui.graphics.SolidColor(Color(0xFF34A853))) { // Green
            moveTo(12.0f, 23.65f)
            curveTo(15.14f, 23.65f, 17.77f, 22.61f, 19.85f, 21.01f)
            lineTo(16.08f, 18.09f)
            curveTo(15.01f, 18.81f, 13.62f, 19.25f, 12.0f, 19.25f)
            curveTo(8.81f, 19.25f, 6.06f, 17.25f, 5.17f, 14.46f)
            lineTo(1.4f, 17.38f)
            curveTo(3.34f, 21.27f, 7.35f, 23.65f, 12.0f, 23.65f)
            close()
        }
        path(fill = androidx.compose.ui.graphics.SolidColor(Color(0xFFFBBC05))) { // Yellow
            moveTo(5.17f, 14.46f)
            curveTo(4.94f, 13.76f, 4.81f, 13.02f, 4.81f, 12.25f)
            curveTo(4.81f, 11.48f, 4.94f, 10.74f, 5.17f, 10.04f)
            lineTo(1.4f, 7.12f)
            curveTo(0.51f, 8.9f, 0.0f, 10.89f, 0.0f, 13.0f)
            curveTo(0.0f, 15.11f, 0.51f, 17.1f, 1.4f, 18.88f)
            lineTo(5.17f, 14.46f)
            close()
        }
    }.build()

/**
 * Sleek, highly secure Professional Login Screen
 */
@Composable
fun LoginScreen(
    isDarkTheme: Boolean,
    error: String?,
    successMessage: String?,
    loading: Boolean,
    onLogin: (String, String) -> Unit,
    onLoginWithGoogle: (String, String) -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showGoogleChooser by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .widthIn(max = 460.dp),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkTheme) Color(0xFF0E1724).copy(alpha = 0.82f) else Color.White.copy(alpha = 0.92f)
            ),
            border = BorderStroke(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                    )
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Main visual clinical emblem
                BrandedAppLogo()

                Spacer(modifier = Modifier.height(2.dp))

                // Heading titles
                Text(
                    text = "Admissão Cirúrgica",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Identificação de Profissional de Saúde",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Dynamic UI notifications
                error?.let {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                successMessage?.let {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Email credential field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-mail profissional") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_email_input"),
                    shape = RoundedCornerShape(14.dp)
                )

                // Password credential field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Senha de acesso") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Ocultar senha" else "Ver senha",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_password_input"),
                    shape = RoundedCornerShape(14.dp)
                )

                // Password Recovery trigger link
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = "Esqueci minha senha",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable { onNavigateToForgotPassword() }
                            .padding(vertical = 4.dp)
                            .testTag("forgot_password_link")
                    )
                }

                // Call to action button
                Button(
                    onClick = { onLogin(email, password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("login_submit_button"),
                    shape = RoundedCornerShape(14.dp),
                    enabled = !loading
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Acessar Sistema", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }

                // Or Divider
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)))
                    Text("ou", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)))
                }

                // Beautiful Google-branded Sign-In Button
                OutlinedButton(
                    onClick = { showGoogleChooser = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("google_login_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isDarkTheme) Color(0xFF131F33) else Color.White
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isDarkTheme) MaterialTheme.colorScheme.outline.copy(alpha = 0.3f) else Color(0xFFDADCE0)
                    ),
                    enabled = !loading
                ) {
                    Icon(
                        imageVector = GoogleGLogo,
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Entrar com o Google",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (isDarkTheme) Color.White else Color(0xFF3C4043)
                    )
                }

                // Bottom alternate router indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Primeiro acesso? ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "Cadastre-se aqui",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { onNavigateToRegister() }
                            .padding(vertical = 4.dp)
                            .testTag("navigate_register_link")
                    )
                }
            }
        }
    }

    if (showGoogleChooser) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showGoogleChooser = false }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .widthIn(max = 400.dp),
                shape = RoundedCornerShape(24.dp),
                color = if (isDarkTheme) Color(0xFF1E1420) else Color.White,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = GoogleGLogo,
                        contentDescription = "Google Logo",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Fazer login com o Google",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) Color.White else Color(0xFF202124),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "para continuar no app Admissão Cirúrgica",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDarkTheme) Color.LightGray else Color(0xFF5F6368),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    val accounts = listOf(
                        Pair("Enfª. Vanja Melo", "enf.vanja.melo@gmail.com"),
                        Pair("Dr. Alexandre Silva", "dr.silva@hospital.com")
                    )

                    accounts.forEach { (name, emailStr) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    showGoogleChooser = false
                                    onLoginWithGoogle(emailStr, name)
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        if (emailStr == "enf.vanja.melo@gmail.com") Color(0xFFB23A48) else Color(0xFF52254F),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = name.substringBefore(" ").take(1) + (name.split(" ").getOrNull(1)?.take(1) ?: ""),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDarkTheme) Color.White else Color(0xFF3C4043)
                                )
                                Text(
                                    text = emailStr,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isDarkTheme) Color.LightGray else Color(0xFF5F6368)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                showGoogleChooser = false
                                onLoginWithGoogle("enfermeiro.plantao@hospital.com", "Enfermeiro de Plantão")
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .border(1.dp, if (isDarkTheme) Color.Gray else Color(0xFFDADCE0), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = if (isDarkTheme) Color.LightGray else Color(0xFF5F6368)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Usar outra conta",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (isDarkTheme) Color.White else Color(0xFF3C4043)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Para continuar, o Google compartilhará seu nome, endereço de e-mail e foto do perfil com o aplicativo Admissão Cirúrgica.",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isDarkTheme) Color.LightGray.copy(alpha = 0.7f) else Color(0xFF70757A),
                        textAlign = TextAlign.Center,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

/**
 * Clean Professional Registration Screen
 */
@Composable
fun RegisterScreen(
    isDarkTheme: Boolean,
    error: String?,
    loading: Boolean,
    onRegister: (String, String, String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .widthIn(max = 460.dp),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkTheme) Color(0xFF0E1724).copy(alpha = 0.82f) else Color.White.copy(alpha = 0.92f)
            ),
            border = BorderStroke(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                    )
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BrandedAppLogo()

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Criar Cadastro",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Acesso exclusivo para profissionais de saúde",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center
                )

                error?.let {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Full Name Input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome completo do profissional") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("register_name_input"),
                    shape = RoundedCornerShape(14.dp)
                )

                // Professional Email Input
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-mail corporativo / pessoal") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("register_email_input"),
                    shape = RoundedCornerShape(14.dp)
                )

                // Password Generation input
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Senha (Mínimo 6 dígitos)") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Ocultar senha" else "Ver senha",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("register_password_input"),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Submit Registration button
                Button(
                    onClick = { onRegister(email, password, name) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("register_submit_button"),
                    shape = RoundedCornerShape(14.dp),
                    enabled = !loading
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Efetuar Registro", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }

                // Alternate back to login trigger
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Já possui uma conta? ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "Identifique-se",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { onNavigateToLogin() }
                            .padding(vertical = 4.dp)
                            .testTag("navigate_login_link")
                    )
                }
            }
        }
    }
}

/**
 * Screen designed to request Password Recovery link or instructions
 */
@Composable
fun ForgotPasswordScreen(
    isDarkTheme: Boolean,
    error: String?,
    loading: Boolean,
    onRecover: (String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .widthIn(max = 460.dp),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkTheme) Color(0xFF0E1724).copy(alpha = 0.82f) else Color.White.copy(alpha = 0.92f)
            ),
            border = BorderStroke(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                    )
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BrandedAppLogo()

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Recuperar Senha",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Insira o e-mail cadastrado e enviaremos as instruções de recuperação",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center
                )

                error?.let {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Email targets input
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Informe seu e-mail cadastrado") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("recover_email_input"),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Action button
                Button(
                    onClick = { onRecover(email) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("recover_submit_button"),
                    shape = RoundedCornerShape(14.dp),
                    enabled = !loading
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Enviar Recuperação", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }

                // Navigation back link
                Text(
                    text = "Voltar para o Login",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { onNavigateToLogin() }
                        .padding(vertical = 4.dp)
                        .testTag("navigate_login_from_recover_link")
                )
            }
        }
    }
}

fun shareReportAsFile(context: Context, record: com.example.data.AdmissionRecord) {
    try {
        val reportContent = record.generateReport()
        val rawName = record.nome.ifBlank { "SemNome" }
        // Clean characters for file names
        val cleanName = rawName.replace(Regex("[^a-zA-Z0-9_]"), "_")
        val fileName = "Relatorio_Admissao_${cleanName}.txt"
        val tempFile = java.io.File(context.cacheDir, fileName)
        tempFile.writeText(reportContent)
        
        val fileUri: Uri = FileProvider.getUriForFile(
            context,
            "com.example.fileprovider",
            tempFile
        )
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Relatório de Admissão: ${record.nome}")
            putExtra(Intent.EXTRA_STREAM, fileUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Baixar/Salvar Relatório (.txt)"))
    } catch (e: Exception) {
        Toast.makeText(context, "Erro ao gerar arquivo de download: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

fun shareDatabaseAsCsv(context: Context, admissions: List<com.example.data.AdmissionRecord>) {
    try {
        if (admissions.isEmpty()) {
            Toast.makeText(context, "Não há dados cadastrados para exportar!", Toast.LENGTH_SHORT).show()
            return
        }
        val csvHeader = "ID,Nome,Idade,Sexo,Prontuario,Enfermaria,Leito,Tipo de Cirurgia,PA,FC,FR,Temperatura,SatO2,Data_Criacao\n"
        val csvData = admissions.joinToString(separator = "\n") { ad ->
            val cleanName = ad.nome.replace(",", " ").replace("\n", " ")
            val cleanCirurgia = ad.tipoCirurgia.replace(",", " ").replace("\n", " ")
            val cleanEnfermaria = ad.enfermaria.replace(",", " ")
            val cleanLeito = ad.leito.replace(",", " ")
            val cleanProntuario = ad.prontuario.replace(",", " ")
            val formattedDate = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(ad.timestamp))
            "${ad.id},\"${cleanName}\",\"${ad.idade}\",\"${ad.sexo}\",\"${cleanProntuario}\",\"${cleanEnfermaria}\",\"${cleanLeito}\",\"${cleanCirurgia}\",\"${ad.pressaoArterial}\",\"${ad.frequenciaCardiaca}\",\"${ad.frequenciaRespiratoria}\",\"${ad.temperatura}\",\"${ad.saturacaoO2}\",\"${formattedDate}\""
        }
        val csvContent = csvHeader + csvData
        
        val tempFile = java.io.File(context.cacheDir, "Admissoes_Enfermagem_Completo.csv")
        tempFile.writeText(csvContent)
        
        val fileUri: Uri = FileProvider.getUriForFile(
            context,
            "com.example.fileprovider",
            tempFile
        )
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "Banco de Dados de Admissão de Enfermagem (CSV)")
            putExtra(Intent.EXTRA_STREAM, fileUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Baixar Planilha CSV (Download)"))
    } catch (e: Exception) {
        Toast.makeText(context, "Erro ao gerar planilha de download: ${e.message}", Toast.LENGTH_LONG).show()
    }
}


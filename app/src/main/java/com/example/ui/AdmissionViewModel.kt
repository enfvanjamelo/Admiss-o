package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AdmissionDatabase
import com.example.data.AdmissionRecord
import com.example.data.AdmissionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject
import org.json.JSONArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Helper function to call the Gemini REST API directly
suspend fun callGeminiApi(prompt: String): String = withContext(Dispatchers.IO) {
    try {
        val apiKey = com.example.BuildConfig.GEMINI_API_KEY
        if (apiKey == "MY_GEMINI_API_KEY" || apiKey.isBlank()) {
            return@withContext "Para obter uma análise clínica personalizada, configure a sua chave de API do Gemini nas configurações do aplicativo. (GEMINI_API_KEY)"
        }
        val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true

        // Build the request body structure for Gemini REST API
        val partObj = JSONObject().put("text", prompt)
        val partsArr = JSONArray().put(partObj)
        val contentObj = JSONObject().put("parts", partsArr)
        val contentsArr = JSONArray().put(contentObj)
        val requestObj = JSONObject().put("contents", contentsArr)

        val writer = OutputStreamWriter(conn.outputStream)
        writer.write(requestObj.toString())
        writer.flush()
        writer.close()

        val responseCode = conn.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val responseText = conn.inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(responseText)
            val candidates = root.getJSONArray("candidates")
            if (candidates.length() > 0) {
                val contents = candidates.getJSONObject(0).getJSONObject("content")
                val parts = contents.getJSONArray("parts")
                if (parts.length() > 0) {
                    return@withContext parts.getJSONObject(0).getString("text")
                }
            }
            "Não foi possível extrair a sugestão do robô de IA."
        } else {
            val errText = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: "Sem resposta de erro"
            "Erro do Servidor Gemini ($responseCode): $errText"
        }
    } catch (e: Exception) {
        "Falha ao conectar à inteligência artificial Gemini: ${e.message}"
    }
}

sealed interface ScreenState {
    object List : ScreenState
    data class Form(val isEditMode: Boolean) : ScreenState
    data class ReportPreview(val record: AdmissionRecord) : ScreenState
}

class AdmissionViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AdmissionRepository

    init {
        val database = AdmissionDatabase.getDatabase(application)
        repository = AdmissionRepository(database.admissionDao())
    }

    val admissions: StateFlow<List<AdmissionRecord>> = repository.allAdmissions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _screenState = MutableStateFlow<ScreenState>(ScreenState.List)
    val screenState: StateFlow<ScreenState> = _screenState.asStateFlow()

    private val _currentRecord = MutableStateFlow(AdmissionRecord())
    val currentRecord: StateFlow<AdmissionRecord> = _currentRecord.asStateFlow()

    private val _aiLoading = MutableStateFlow(false)
    val aiLoading: StateFlow<Boolean> = _aiLoading.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun navigateToList() {
        _screenState.value = ScreenState.List
    }

    fun navigateToNewForm() {
        _currentRecord.value = AdmissionRecord()
        _screenState.value = ScreenState.Form(isEditMode = false)
    }

    fun navigateToEditForm(record: AdmissionRecord) {
        _currentRecord.value = record
        _screenState.value = ScreenState.Form(isEditMode = true)
    }

    fun navigateToReportPreview(record: AdmissionRecord) {
        _screenState.value = ScreenState.ReportPreview(record)
    }

    fun updateRecord(update: (AdmissionRecord) -> AdmissionRecord) {
        _currentRecord.value = update(_currentRecord.value)
    }

    fun saveCurrentRecord() {
        viewModelScope.launch {
            val recordToSave = _currentRecord.value.copy(timestamp = System.currentTimeMillis())
            val id = repository.insert(recordToSave)
            val savedRecord = recordToSave.copy(id = id)
            navigateToReportPreview(savedRecord)
        }
    }

    fun generateAiEvaluationForRecord(record: AdmissionRecord, onComplete: (AdmissionRecord) -> Unit) {
        viewModelScope.launch {
            _aiLoading.value = true
            val prompt = """
                Você é um enfermeiro perito especialista em auditoria e segurança do paciente. Analise a seguinte ficha de admissão de enfermagem de um paciente cirúrgico brasileiro e recomende um plano estruturado de cuidados preventivos e evolução clínica baseado nas escalas de risco (Braden, Fugulin, Morse, Glasgow, Escala da Dor).

                DADOS DO PACIENTE:
                - Nome: ${record.nome.ifBlank { "Não informado" }}
                - Idade: ${record.idade.ifBlank { "Não informada" }} anos
                - Sexo: ${record.sexo.ifBlank { "Não informado" }}
                - Religião: ${record.religiao.ifBlank { "Não especificada" }}
                - Tipo de Cirurgia Planejada: ${record.tipoCirurgia.ifBlank { "Não informado" }}

                AVALIAÇÕES DE ESCALAS CLÍNICAS:
                - Escala de Braden (Risco de Lesões por Pressão): Escore ${record.bradenScore()} / 23 (${record.bradenClassification()})
                - Escala de Fugulin (Grau de Dependência de Enfermagem): Escore ${record.fugulinScore()} / 36 (${record.fugulinClassification()})
                - Escala de Morse (Risco de Quedas): Escore ${record.morseScore()} (${record.morseClassification()})
                - Escala de Glasgow (Nível de Consciência): Escore ${record.glasgowScore()} / 15 (${record.glasgowClassification()})
                - Escala da Dor: Nível ${record.dorNivel} / 10 (${record.dorClassification()}) na localização: ${record.dorLocalizacao.ifBlank { "Não informada" }} com características: ${record.dorCaracteristicas.ifBlank { "Não informadas" }}

                PELE E INTEGRIDADE:
                - Integridade de Pele: ${record.sistemaIntegridadePele.ifBlank { "Íntegra" }}
                ${if (record.lesionWidthCm.isNotBlank()) "- Planimetria da Lesão: ${record.lesionWidthCm}x${record.lesionHeightCm} cm (Área: ${record.lesionAreaSquareCm} cm²). Descrição: ${record.lesionDescriptionPlanimetria}" else ""}

                HISTÓRICO CLÍNICO E ALERGIAS:
                - Doenças Anteriores: ${record.doencasAnteriores.ifBlank { "Nenhuma" }}
                - Alergias: ${record.alergias.ifBlank { "Negadas" }}
                - Medicações em Uso: ${record.medicacoesUso.ifBlank { "Nenhuma" }}

                Instruções de Resposta:
                Sua resposta deve ser escrita de forma profissional, em português, direta e baseada em evidências científicas de enfermagem. Divida a análise estritamente nos seguintes 3 blocos bem identificados usando markdown simples:
                1. 🔴 **PONTOS DE ATENÇÃO IMEDIATA (SINALIZADORES DE ALERTA)**: Liste riscos graves baseados nas escalas e no histórico.
                2. 🛡️ **PLANO DE CUIDADOS PREVENTIVO (BRADEN & MORSE)**: Liste intervenções práticas contra LPP (Lesão por Pressão) e Quedas aplicadas a este escore.
                3. 📋 **PRESCRIÇÃO DE ENFERMAGEM COMPLEMENTAR**: Outros cuidados específicos (controle da dor, dispositivos, oxigenação, dieta).
            """.trimIndent()

            val response = callGeminiApi(prompt)
            val updatedRecord = record.copy(aiInterventionsResult = response)
            repository.insert(updatedRecord) // persist update
            
            // If the record updated is the currently active record, sync it
            if (_currentRecord.value.id == record.id) {
                _currentRecord.value = updatedRecord
            }
            _aiLoading.value = false
            onComplete(updatedRecord)
        }
    }

    fun deleteRecord(record: AdmissionRecord) {
        viewModelScope.launch {
            repository.delete(record)
            if (_screenState.value is ScreenState.ReportPreview && 
                (_screenState.value as ScreenState.ReportPreview).record.id == record.id) {
                navigateToList()
            }
        }
    }
}

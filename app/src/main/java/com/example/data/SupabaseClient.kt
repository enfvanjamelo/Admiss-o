package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

/**
 * Extension to convert AdmissionRecord to Supabase-compatible JSON object
 */
fun AdmissionRecord.toJsonObject(): JSONObject {
    val obj = JSONObject()
    obj.put("id", id)
    obj.put("timestamp", timestamp)
    obj.put("nome", nome)
    obj.put("idade", idade)
    obj.put("sexo", sexo)
    obj.put("religiao", religiao)
    obj.put("estado_civil", estadoCivil)
    obj.put("funcao_laboral", funcaoLaboral)
    obj.put("proveniencia", proveniencia)
    obj.put("prontuario", prontuario)
    obj.put("enfermaria", enfermaria)
    obj.put("leito", leito)
    obj.put("tipo_cirurgia", tipoCirurgia)
    obj.put("pressao_arterial", pressaoArterial)
    obj.put("frequencia_cardiaca", frequenciaCardiaca)
    obj.put("frequencia_respiratoria", frequenciaRespiratoria)
    obj.put("temperatura", temperatura)
    obj.put("saturacao_o2", saturacaoO2)
    obj.put("sistema_locomotor", sistemaLocomotor)
    obj.put("sistema_cardiovascular", sistemaCardiovascular)
    obj.put("sistema_respiratorio", sistemaRespiratorio)
    obj.put("sistema_neurologico", sistemaNeurologico)
    obj.put("sistema_nutricao", sistemaNutricao)
    obj.put("sistema_urinario", sistemaUrinario)
    obj.put("sistema_intestinal", sistemaIntestinal)
    obj.put("sistema_integridade_pele", sistemaIntegridadePele)
    obj.put("dispositivos", dispositivos)
    obj.put("lesion_photo_uri", lesionPhotoUri ?: JSONObject.NULL)
    obj.put("lesion_width_cm", lesionWidthCm)
    obj.put("lesion_height_cm", lesionHeightCm)
    obj.put("lesion_area_square_cm", lesionAreaSquareCm)
    obj.put("lesion_description_planimetria", lesionDescriptionPlanimetria)
    obj.put("ai_interventions_result", aiInterventionsResult)
    obj.put("lesions_spreadsheet_json", lesionsSpreadsheetJson)
    obj.put("doencas_anteriores", doencasAnteriores)
    obj.put("condicoes_anteriores", condicoesAnteriores)
    obj.put("cirurgias_anteriores", cirurgiasAnteriores)
    obj.put("alergias", alergias)
    obj.put("medicacoes_uso", medicacoesUso)
    obj.put("exames", exames)
    obj.put("acuidade_visual", acuidadeVisual)
    obj.put("acuidade_auditiva", acuidadeAuditiva)
    obj.put("locomocao_mobilidade", locomocaoMobilidade)
    obj.put("integridade_pele", integridadePele)
    obj.put("apresenta_lesoes", apresentaLesoes)
    obj.put("descricao_lesoes", descricaoLesoes)
    obj.put("braden_percepcao_sensorial", bradenPercepcaoSensorial)
    obj.put("braden_umidade", bradenUmidade)
    obj.put("braden_atividade", bradenAtividade)
    obj.put("braden_mobilidade", bradenMobilidade)
    obj.put("braden_nutricao", bradenNutricao)
    obj.put("braden_friccao_cisalhamento", bradenFriccaoCisalhamento)
    obj.put("fugulin_estado_mental", fugulinEstadoMental)
    obj.put("fugulin_oxigenacao", fugulinOxigenacao)
    obj.put("fugulin_sinais_vitais", fugulinSinaisVitais)
    obj.put("fugulin_motilidade", fugulinMotilidade)
    obj.put("fugulin_locomocao", fugulinLocomocao)
    obj.put("fugulin_cuidado_corporal", fugulinCuidadoCorporal)
    obj.put("fugulin_eliminacao", fugulinEliminacao)
    obj.put("fugulin_nutricao_hidratacao", fugulinNutricaoHidratacao)
    obj.put("fugulin_terapeutica", fugulinTerapeutica)
    obj.put("morse_historico_quedas", morseHistoricoQuedas)
    obj.put("morse_diagnostico_secundario", morseDiagnosticoSecundario)
    obj.put("morse_auxilio_locomocao", morseAuxilioLocomocao)
    obj.put("morse_terapia_ev", morseTerapiaEV)
    obj.put("morse_marcha", morseMarcha)
    obj.put("morse_estado_mental", morseEstadoMental)
    obj.put("glasgow_abertura_ocular", glasgowAberturaOcular)
    obj.put("glasgow_resposta_verbal", glasgowRespostaVerbal)
    obj.put("glasgow_resposta_motora", glasgowRespostaMotora)
    obj.put("dor_nivel", dorNivel)
    obj.put("dor_localizacao", dorLocalizacao)
    obj.put("dor_caracteristicas", dorCaracteristicas)
    return obj
}

/**
 * Convert JSONObject representing an admission record from Supabase back to AdmissionRecord
 */
fun JSONObject.toAdmissionRecord(): AdmissionRecord {
    return AdmissionRecord(
        id = this.optLong("id", 0),
        timestamp = this.optLong("timestamp", System.currentTimeMillis()),
        nome = this.optString("nome", ""),
        idade = this.optString("idade", ""),
        sexo = this.optString("sexo", ""),
        religiao = this.optString("religiao", ""),
        estadoCivil = this.optString("estado_civil", ""),
        funcaoLaboral = this.optString("funcao_laboral", ""),
        proveniencia = this.optString("proveniencia", ""),
        prontuario = this.optString("prontuario", ""),
        enfermaria = this.optString("enfermaria", ""),
        leito = this.optString("leito", ""),
        tipoCirurgia = this.optString("tipo_cirurgia", ""),
        pressaoArterial = this.optString("pressao_arterial", ""),
        frequenciaCardiaca = this.optString("frequencia_cardiaca", ""),
        frequenciaRespiratoria = this.optString("frequencia_respiratoria", ""),
        temperatura = this.optString("temperatura", ""),
        saturacaoO2 = this.optString("saturacao_o2", ""),
        sistemaLocomotor = this.optString("sistema_locomotor", ""),
        sistemaCardiovascular = this.optString("sistema_cardiovascular", ""),
        sistemaRespiratorio = this.optString("sistema_respiratorio", ""),
        sistemaNeurologico = this.optString("sistema_neurologico", ""),
        sistemaNutricao = this.optString("sistema_nutricao", ""),
        sistemaUrinario = this.optString("sistema_urinario", ""),
        sistemaIntestinal = this.optString("sistema_intestinal", ""),
        sistemaIntegridadePele = this.optString("sistema_integridade_pele", ""),
        dispositivos = this.optString("dispositivos", ""),
        lesionPhotoUri = if (this.isNull("lesion_photo_uri")) null else this.optString("lesion_photo_uri", null),
        lesionWidthCm = this.optString("lesion_width_cm", ""),
        lesionHeightCm = this.optString("lesion_height_cm", ""),
        lesionAreaSquareCm = this.optString("lesion_area_square_cm", ""),
        lesionDescriptionPlanimetria = this.optString("lesion_description_planimetria", ""),
        aiInterventionsResult = this.optString("ai_interventions_result", ""),
        lesionsSpreadsheetJson = this.optString("lesions_spreadsheet_json", ""),
        doencasAnteriores = this.optString("doencas_anteriores", ""),
        condicoesAnteriores = this.optString("condicoes_anteriores", ""),
        cirurgiasAnteriores = this.optString("cirurgias_anteriores", ""),
        alergias = this.optString("alergias", ""),
        medicacoesUso = this.optString("medicacoes_uso", ""),
        exames = this.optString("exames", ""),
        acuidadeVisual = this.optString("acuidade_visual", ""),
        acuidadeAuditiva = this.optString("acuidade_auditiva", ""),
        locomocaoMobilidade = this.optString("locomocao_mobilidade", ""),
        integridadePele = this.optString("integridade_pele", ""),
        apresentaLesoes = this.optBoolean("apresenta_lesoes", false),
        descricaoLesoes = this.optString("descricao_lesoes", ""),
        bradenPercepcaoSensorial = this.optInt("braden_percepcao_sensorial", 4),
        bradenUmidade = this.optInt("braden_umidade", 4),
        bradenAtividade = this.optInt("braden_atividade", 4),
        bradenMobilidade = this.optInt("braden_mobilidade", 4),
        bradenNutricao = this.optInt("braden_nutricao", 4),
        bradenFriccaoCisalhamento = this.optInt("braden_friccao_cisalhamento", 3),
        fugulinEstadoMental = this.optInt("fugulin_estado_mental", 1),
        fugulinOxigenacao = this.optInt("fugulin_oxigenacao", 1),
        fugulinSinaisVitais = this.optInt("fugulin_sinais_vitais", 1),
        fugulinMotilidade = this.optInt("fugulin_motilidade", 1),
        fugulinLocomocao = this.optInt("fugulin_locomocao", 1),
        fugulinCuidadoCorporal = this.optInt("fugulin_cuidado_corporal", 1),
        fugulinEliminacao = this.optInt("fugulin_eliminacao", 1),
        fugulinNutricaoHidratacao = this.optInt("fugulin_nutricao_hidratacao", 1),
        fugulinTerapeutica = this.optInt("fugulin_terapeutica", 1),
        morseHistoricoQuedas = this.optInt("morse_historico_quedas", 0),
        morseDiagnosticoSecundario = this.optInt("morse_diagnostico_secundario", 0),
        morseAuxilioLocomocao = this.optInt("morse_auxilio_locomocao", 0),
        morseTerapiaEV = this.optInt("morse_terapia_ev", 0),
        morseMarcha = this.optInt("morse_marcha", 0),
        morseEstadoMental = this.optInt("morse_estado_mental", 0),
        glasgowAberturaOcular = this.optInt("glasgow_abertura_ocular", 4),
        glasgowRespostaVerbal = this.optInt("glasgow_resposta_verbal", 5),
        glasgowRespostaMotora = this.optInt("glasgow_resposta_motora", 6),
        dorNivel = this.optInt("dor_nivel", 0),
        dorLocalizacao = this.optString("dor_localizacao", ""),
        dorCaracteristicas = this.optString("dor_caracteristicas", "")
    )
}

object SupabaseClient {
    private const val TAG = "SupabaseClient"

    private var customUrl: String? = null
    private var customAnonKey: String? = null

    fun updateConfig(url: String, anonKey: String) {
        customUrl = if (url.isBlank()) null else url.trim()
        customAnonKey = if (anonKey.isBlank()) null else anonKey.trim()
    }

    val supabaseUrl: String
        get() = customUrl ?: try {
            BuildConfig.SUPABASE_URL
        } catch (e: Exception) {
            "https://your-project.supabase.co"
        }

    val supabaseAnonKey: String
        get() = customAnonKey ?: try {
            BuildConfig.SUPABASE_ANON_KEY
        } catch (e: Exception) {
            "your-supabase-public-anon-key"
        }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    fun isConfigured(): Boolean {
        val url = supabaseUrl
        val key = supabaseAnonKey
        return url.isNotBlank() &&
                url != "https://your-project.supabase.co" &&
                key.isNotBlank() &&
                key != "your-supabase-public-anon-key"
    }

    private fun getFullUrl(path: String): String {
        val sanitizedBase = supabaseUrl.trim().trimEnd('/')
        return if (sanitizedBase.contains("/rest/v1")) {
            val endpoint = if (path.startsWith("/")) path else "/$path"
            if (sanitizedBase.endsWith("/rest/v1")) {
                "$sanitizedBase$endpoint"
            } else {
                // E.g. base already contains /rest/v1/
                val cleanedBase = sanitizedBase.substringBefore("/rest/v1") + "/rest/v1"
                "$cleanedBase$endpoint"
            }
        } else {
            val formattedPath = if (path.startsWith("/")) path else "/$path"
            "$sanitizedBase/rest/v1$formattedPath"
        }
    }

    /**
     * Upsert a single record into Supabase (insert or replace if ID conflicts)
     */
    suspend fun upsertRecord(record: AdmissionRecord): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured()) {
            Log.w(TAG, "Supabase details are not configured. Working locally.")
            return@withContext false
        }

        try {
            val jsonObject = record.toJsonObject()
            val url = getFullUrl("admissions")
            val requestBody = jsonObject.toString().toRequestBody(jsonMediaType)

            val request = Request.Builder()
                .url(url)
                .header("apikey", supabaseAnonKey)
                .header("Authorization", "Bearer $supabaseAnonKey")
                .header("Content-Type", "application/json")
                .header("Prefer", "resolution=merge-duplicates")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Log.i(TAG, "Successfully synced admission ${record.id} with Supabase.")
                    true
                } else {
                    val errMsg = response.body?.string() ?: ""
                    Log.e(TAG, "Failed syncing with Supabase (HTTP ${response.code}): $errMsg")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Supabase upsert: ${e.message}", e)
            false
        }
    }

    /**
     * Download all admissions from Supabase
     */
    suspend fun fetchAllRecords(): List<AdmissionRecord> = withContext(Dispatchers.IO) {
        if (!isConfigured()) {
            return@withContext emptyList()
        }

        try {
            val url = getFullUrl("admissions?order=timestamp.desc")
            val request = Request.Builder()
                .url(url)
                .header("apikey", supabaseAnonKey)
                .header("Authorization", "Bearer $supabaseAnonKey")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val rawJson = response.body?.string() ?: "[]"
                    val jsonArray = JSONArray(rawJson)
                    val recordsList = mutableListOf<AdmissionRecord>()
                    for (i in 0 until jsonArray.length()) {
                        val recordObj = jsonArray.getJSONObject(i)
                        recordsList.add(recordObj.toAdmissionRecord())
                    }
                    recordsList
                } else {
                    val errorPayload = response.body?.string() ?: ""
                    Log.e(TAG, "Failed fetching from Supabase (HTTP ${response.code}): $errorPayload")
                    emptyList()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Supabase fetch: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Delete an admission from Supabase by ID
     */
    suspend fun deleteRecord(id: Long): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext false

        try {
            val url = getFullUrl("admissions?id=eq.$id")
            val request = Request.Builder()
                .url(url)
                .header("apikey", supabaseAnonKey)
                .header("Authorization", "Bearer $supabaseAnonKey")
                .delete()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Log.i(TAG, "Successfully deleted admission $id on Supabase cloud.")
                    true
                } else {
                    val errMsg = response.body?.string() ?: ""
                    Log.e(TAG, "Failed deleting record from Supabase: $errMsg")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Supabase delete: ${e.message}", e)
            false
        }
    }

    private fun getAuthUrl(path: String): String {
        val sanitizedBase = supabaseUrl.trim().trimEnd('/')
        return if (sanitizedBase.contains("/rest/v1")) {
            val cleanedBase = sanitizedBase.substringBefore("/rest/v1")
            "$cleanedBase/auth/v1/$path"
        } else {
            "$sanitizedBase/auth/v1/$path"
        }
    }

    /**
     * Authenticated Sign Up
     */
    suspend fun signUp(email: String, password: String): Result<String> = withContext(Dispatchers.IO) {
        if (!isConfigured()) {
            return@withContext Result.failure(Exception("Supabase não configurado. Por favor, insira as chaves nas configurações."))
        }
        try {
            val jsonObject = JSONObject().apply {
                put("email", email.trim())
                put("password", password.trim())
            }
            val requestBody = jsonObject.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(getAuthUrl("signup"))
                .header("apikey", supabaseAnonKey)
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    Result.success(bodyStr)
                } else {
                    val message = try {
                        JSONObject(bodyStr).optString("msg", bodyStr)
                    } catch (e: Exception) {
                        bodyStr
                    }
                    Result.failure(Exception(message.ifBlank { "Erro desconhecido HTTP ${response.code}" }))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Authenticated Sign In
     */
    suspend fun signIn(email: String, password: String): Result<String> = withContext(Dispatchers.IO) {
        if (!isConfigured()) {
            return@withContext Result.failure(Exception("Supabase não configurado. Por favor, insira as chaves nas configurações."))
        }
        try {
            val jsonObject = JSONObject().apply {
                put("email", email.trim())
                put("password", password.trim())
            }
            val requestBody = jsonObject.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(getAuthUrl("token?grant_type=password"))
                .header("apikey", supabaseAnonKey)
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    Result.success(bodyStr)
                } else {
                    val message = try {
                        val obj = JSONObject(bodyStr)
                        val errorDesc = obj.optString("error_description", "")
                        if (errorDesc.isNotBlank()) errorDesc else obj.optString("error", bodyStr)
                    } catch (e: Exception) {
                        bodyStr
                    }
                    Result.failure(Exception(message.ifBlank { "Erro desconhecido HTTP ${response.code}" }))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Authenticated Password Recovery
     */
    suspend fun recoverPassword(email: String): Result<String> = withContext(Dispatchers.IO) {
        if (!isConfigured()) {
            return@withContext Result.failure(Exception("Supabase não configurado."))
        }
        try {
            val jsonObject = JSONObject().apply {
                put("email", email.trim())
            }
            val requestBody = jsonObject.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(getAuthUrl("recover"))
                .header("apikey", supabaseAnonKey)
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    Result.success(bodyStr)
                } else {
                    val message = try {
                        JSONObject(bodyStr).optString("msg", bodyStr)
                    } catch (e: Exception) {
                        bodyStr
                    }
                    Result.failure(Exception(message.ifBlank { "Erro desconhecido HTTP ${response.code}" }))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

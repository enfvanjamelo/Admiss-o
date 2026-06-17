package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "admissions")
data class AdmissionRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    
    // 1. Dados do Paciente
    val nome: String = "",
    val idade: String = "",
    val sexo: String = "",
    val religiao: String = "",
    val estadoCivil: String = "",
    val funcaoLaboral: String = "",
    val proveniencia: String = "",
    val prontuario: String = "",
    val enfermaria: String = "",
    val leito: String = "",
    val tipoCirurgia: String = "",
    
    // 2. Sistemas Orgânicos & Dispositivos
    val sistemaLocomotor: String = "",
    val sistemaCardiovascular: String = "",
    val sistemaRespiratorio: String = "",
    val sistemaNeurologico: String = "",
    val sistemaNutricao: String = "",
    val sistemaUrinario: String = "",
    val sistemaIntestinal: String = "",
    val sistemaIntegridadePele: String = "",
    val dispositivos: String = "",
    
    // Planimetria & Foto da Lesão
    val lesionPhotoUri: String? = null,
    val lesionWidthCm: String = "",
    val lesionHeightCm: String = "",
    val lesionAreaSquareCm: String = "",
    val lesionDescriptionPlanimetria: String = "",
    val aiInterventionsResult: String = "",
    val lesionsSpreadsheetJson: String = "",
    
    // 3. Histórico Médico
    val doencasAnteriores: String = "",
    val condicoesAnteriores: String = "",
    val cirurgiasAnteriores: String = "",
    val alergias: String = "",
    val medicacoesUso: String = "",
    val exames: String = "",
    
    // 4. Sensório, Mobilidade & Pele
    val acuidadeVisual: String = "",
    val acuidadeAuditiva: String = "",
    val locomocaoMobilidade: String = "",
    val integridadePele: String = "",
    val apresentaLesoes: Boolean = false,
    val descricaoLesoes: String = "",
    
    // 5. Escala de Braden
    val bradenPercepcaoSensorial: Int = 4,
    val bradenUmidade: Int = 4,
    val bradenAtividade: Int = 4,
    val bradenMobilidade: Int = 4,
    val bradenNutricao: Int = 4,
    val bradenFriccaoCisalhamento: Int = 3,
    
    // 6. Escala de Fugulin
    val fugulinEstadoMental: Int = 1,
    val fugulinOxigenacao: Int = 1,
    val fugulinSinaisVitais: Int = 1,
    val fugulinMotilidade: Int = 1,
    val fugulinLocomocao: Int = 1,
    val fugulinCuidadoCorporal: Int = 1,
    val fugulinEliminacao: Int = 1,
    val fugulinNutricaoHidratacao: Int = 1,
    val fugulinTerapeutica: Int = 1,
    
    // 7. Escala de Morse
    val morseHistoricoQuedas: Int = 0,
    val morseDiagnosticoSecundario: Int = 0,
    val morseAuxilioLocomocao: Int = 0,
    val morseTerapiaEV: Int = 0,
    val morseMarcha: Int = 0,
    val morseEstadoMental: Int = 0,
    
    // 8. Escala de Glasgow
    val glasgowAberturaOcular: Int = 4,
    val glasgowRespostaVerbal: Int = 5,
    val glasgowRespostaMotora: Int = 6,
    
    // 9. Escala de Dor
    val dorNivel: Int = 0,
    val dorLocalizacao: String = "",
    val dorCaracteristicas: String = ""
) {
    // Calculators
    fun bradenScore(): Int = bradenPercepcaoSensorial + bradenUmidade + bradenAtividade +
            bradenMobilidade + bradenNutricao + bradenFriccaoCisalhamento

    fun bradenClassification(): String {
        val score = bradenScore()
        return when {
            score <= 12 -> "Risco Alto"
            score in 13..14 -> "Risco Moderado"
            score in 15..18 -> "Risco Baixo"
            else -> "Sem Risco"
        }
    }

    fun fugulinScore(): Int = fugulinEstadoMental + fugulinOxigenacao + fugulinSinaisVitais +
            fugulinMotilidade + fugulinLocomocao + fugulinCuidadoCorporal +
            fugulinEliminacao + fugulinNutricaoHidratacao + fugulinTerapeutica

    fun fugulinClassification(): String {
        val score = fugulinScore()
        return when {
            score in 9..14 -> "Cuidado Mínimo"
            score in 15..20 -> "Cuidado Intermediário"
            score in 21..26 -> "Cuidado de Alta Dependência"
            score in 27..31 -> "Cuidado Semi-Intensivo"
            else -> "Cuidado Intensivo"
        }
    }

    fun morseScore(): Int = morseHistoricoQuedas + morseDiagnosticoSecundario +
            morseAuxilioLocomocao + morseTerapiaEV + morseMarcha + morseEstadoMental

    fun morseClassification(): String {
        val score = morseScore()
        return when {
            score <= 24 -> "Baixo Risco"
            score in 25..44 -> "Risco Moderado"
            else -> "Alto Risco"
        }
    }

    fun glasgowScore(): Int = glasgowAberturaOcular + glasgowRespostaVerbal + glasgowRespostaMotora

    fun glasgowClassification(): String {
        val score = glasgowScore()
        return when {
            score in 13..15 -> "Leve"
            score in 9..12 -> "Moderado"
            else -> "Grave"
        }
    }

    fun dorClassification(): String {
        return when {
            dorNivel == 0 -> "Sem Dor"
            dorNivel in 1..3 -> "Dor Leve"
            dorNivel in 4..6 -> "Dor Moderada"
            dorNivel in 7..9 -> "Dor Forte"
            else -> "Dor Intensa / Pior Possível"
        }
    }

    fun getLesionsList(): List<SkinLesionRow> {
        if (lesionsSpreadsheetJson.isBlank()) return emptyList()
        return try {
            val list = mutableListOf<SkinLesionRow>()
            val arr = org.json.JSONArray(lesionsSpreadsheetJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    SkinLesionRow(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        dataRegistro = obj.optString("dataRegistro", ""),
                        localizacao = obj.optString("localizacao", ""),
                        tipoLesao = obj.optString("tipoLesao", ""),
                        larguraCm = obj.optString("larguraCm", ""),
                        comprimentoCm = obj.optString("comprimentoCm", ""),
                        areaCm2 = obj.optString("areaCm2", ""),
                        tipoTecido = obj.optString("tipoTecido", ""),
                        exsudato = obj.optString("exsudato", ""),
                        fotoUri = if (obj.isNull("fotoUri")) null else obj.optString("fotoUri")
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun withUpdatedLesions(list: List<SkinLesionRow>): AdmissionRecord {
        val arr = org.json.JSONArray()
        for (item in list) {
            val obj = org.json.JSONObject().apply {
                put("id", item.id)
                put("dataRegistro", item.dataRegistro)
                put("localizacao", item.localizacao)
                put("tipoLesao", item.tipoLesao)
                put("larguraCm", item.larguraCm)
                put("comprimentoCm", item.comprimentoCm)
                put("areaCm2", item.areaCm2)
                put("tipoTecido", item.tipoTecido)
                put("exsudato", item.exsudato)
                put("fotoUri", item.fotoUri)
            }
            arr.put(obj)
        }
        return this.copy(lesionsSpreadsheetJson = arr.toString())
    }

    fun generateReport(): String {
        val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        val dataFormatada = dateFormat.format(java.util.Date(timestamp))
        
        val lesions = getLesionsList()
        val lesionsTable = if (lesions.isNotEmpty()) {
            val sb = java.lang.StringBuilder("\n6. REGISTRO & PLANILHA DE LESÕES DE PELE\n--------------------------------------------------\n")
            sb.append(String.format("%-10s | %-12s | %-12s | %-11s | %-10s\n", "Data", "Região", "Tipo Lesão", "Dimensões", "Tecido"))
            sb.append("--------------------------------------------------\n")
            for (l in lesions) {
                val dims = "${l.larguraCm}x${l.comprimentoCm} cm"
                sb.append(String.format("%-10s | %-12s | %-12s | %-11s | %-10s\n", 
                    l.dataRegistro.take(10), 
                    l.localizacao.take(12), 
                    l.tipoLesao.take(12), 
                    dims.take(11), 
                    l.tipoTecido.take(10)
                ))
            }
            sb.append("--------------------------------------------------\n")
            sb.toString()
        } else ""

        return """
            ==================================================
              ADMISSÃO DE ENFERMAGEM - PACIENTE CIRÚRGICO
            ==================================================

            1. DADOS DE IDENTIFICAÇÃO DO PACIENTE
            --------------------------------------------------
            Nome: ${nome.ifBlank { "Não informado" }}
            Idade: ${idade.ifBlank { "Não informada" }} anos | Sexo: ${sexo.ifBlank { "Não informado" }}
            Religião: ${religiao.ifBlank { "Não informada" }} | Estado Civil: ${estadoCivil.ifBlank { "Não informado" }}
            Função Laboral: ${funcaoLaboral.ifBlank { "Não informada" }}
            Proveniência: ${proveniencia.ifBlank { "Não informada" }}
            Prontuário: ${prontuario.ifBlank { "Não informado" }}
            Enfermaria: ${enfermaria.ifBlank { "Não informada" }} | Leito: ${leito.ifBlank { "Não informado" }}
            Tipo de Cirurgia Planejada: ${tipoCirurgia.ifBlank { "Não informado" }}

            2. HISTÓRICO CLÍNICO & ALERGIAS
            --------------------------------------------------
            Doenças Anteriores: ${doencasAnteriores.ifBlank { "Nenhuma" }}
            Condições Anteriores: ${condicoesAnteriores.ifBlank { "Nenhuma" }}
            Cirurgias Anteriores: ${cirurgiasAnteriores.ifBlank { "Nenhuma" }}
            Alergias: ${alergias.ifBlank { "Negadas" }}
            Medicações em Uso: ${medicacoesUso.ifBlank { "Nenhuma de uso contínuo" }}
            Exames de Admissão: ${exames.ifBlank { "Nenhum anexado ou realizado" }}

            3. SISTEMAS ORGÂNICOS & DISPOSITIVOS
            --------------------------------------------------
            • Sistema Locomotor: ${sistemaLocomotor.ifBlank { "Sem alterações" }}
            • Cardiovascular: ${sistemaCardiovascular.ifBlank { "Sem alterações" }}
            • Respiratório: ${sistemaRespiratorio.ifBlank { "Sem alterações" }}
            • Neurológico: ${sistemaNeurologico.ifBlank { "Sem alterações" }}
            • Nutrição: ${sistemaNutricao.ifBlank { "Dieta Geral Oral" }}
            • Urinário: ${sistemaUrinario.ifBlank { "Sem alterações" }}
            • Intestinal: ${sistemaIntestinal.ifBlank { "Sem alterações" }}
            • Integridade da Pele: ${sistemaIntegridadePele.ifBlank { "Íntegra" }}
            ${if (!lesionWidthCm.isNullOrBlank()) "   --> Planimetria da Lesão Principal: ${lesionWidthCm}x${lesionHeightCm} cm (Área: ${lesionAreaSquareCm} cm²)\n   --> Detalhes: $lesionDescriptionPlanimetria" else ""}
            • Dispositivos em Uso: ${dispositivos.ifBlank { "Nenhum dispositivo ativo" }}

            4. EXAME FÍSICO & ACUIDADES SENSORIAIS
            --------------------------------------------------
            Acuidade Visual: ${acuidadeVisual.ifBlank { "Normal/Inalterada" }}
            Acuidade Auditiva: ${acuidadeAuditiva.ifBlank { "Normal/Inalterada" }}

            5. ESCALAS DE AVALIAÇÃO CLÍNICA
            --------------------------------------------------
            A) ESCALA DE BRADEN (Risco de Lesão por Pressão):
               • Percepção Sensorial: $bradenPercepcaoSensorial
               • Umidade: $bradenUmidade
               • Atividade: $bradenAtividade
               • Mobilidade: $bradenMobilidade
               • Nutrição: $bradenNutricao
               • Fricção/Cisalhamento: $bradenFriccaoCisalhamento
               TOTAL ESCORE BRADEN: ${bradenScore()} / 23 (${bradenClassification()})

            B) ESCALA DE FUGULIN (Grau de Dependência):
               • Estado Mental: $fugulinEstadoMental
               • Oxigenação: $fugulinOxigenacao
               • Sinais Vitais: $fugulinSinaisVitais
               • Motilidade: $fugulinMotilidade
               • Locomoção: $fugulinLocomocao
               • Cuidado Corporal: $fugulinCuidadoCorporal
               • Eliminação: $fugulinEliminacao
               • Nutrição/Hidratação: $fugulinNutricaoHidratacao
               • Terapêutica: $fugulinTerapeutica
               TOTAL ESCORE FUGULIN: ${fugulinScore()} / 36 (${fugulinClassification()})

            C) ESCALA DE MORSE (Risco de Quedas):
               • Histórico de Quedas: $morseHistoricoQuedas
               • Diagnóstico Secundário: $morseDiagnosticoSecundario
               • Auxílio na Locomoção: $morseAuxilioLocomocao
               • Terapia Endovenosa: $morseTerapiaEV
               • Marcha: $morseMarcha
               • Estado Mental: $morseEstadoMental
               TOTAL ESCORE MORSE: ${morseScore()} (${morseClassification()})

            D) ESCALA DE GLASGOW (Nível de Consciência):
               • Abertura Ocular: $glasgowAberturaOcular
               • Resposta Verbal: $glasgowRespostaVerbal
               • Resposta Motora: $glasgowRespostaMotora
               TOTAL ESCORE GLASGOW: ${glasgowScore()} / 15 (${glasgowClassification()})

            E) ESCALA ANALÓGICA DA DOR:
               • Intensidade da Dor: $dorNivel / 10 (${dorClassification()})
               • Localização da Dor: ${dorLocalizacao.ifBlank { "Não informada" }}
               • Características da Dor: ${dorCaracteristicas.ifBlank { "Não informadas" }}
            $lesionsTable
            ${if (aiInterventionsResult.isNotBlank()) "\n            ==================================================\n              RECOMENDAÇÕES CLÍNICAS E INTERVENÇÕES (IA)\n            ==================================================\n            $aiInterventionsResult" else ""}

            --------------------------------------------------
            Relatório gerado automaticamente em: $dataFormatada
            ==================================================
        """.trimIndent()
    }
}

data class SkinLesionRow(
    val id: String = java.util.UUID.randomUUID().toString(),
    val dataRegistro: String = "",
    val localizacao: String = "",
    val tipoLesao: String = "",
    val larguraCm: String = "",
    val comprimentoCm: String = "",
    val areaCm2: String = "",
    val tipoTecido: String = "",
    val exsudato: String = "",
    val fotoUri: String? = null
)

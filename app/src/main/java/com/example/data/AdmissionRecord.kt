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
    
    // Sinais Vitais (Vitals)
    val pressaoArterial: String = "",
    val frequenciaCardiaca: String = "",
    val frequenciaRespiratoria: String = "",
    val temperatura: String = "",
    val saturacaoO2: String = "",
    
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
            val sb = java.lang.StringBuilder("\nVI. MONITORAMENTO E PLANILHA DE LESÕES CUTÂNEAS\n--------------------------------------------------------------------------------\n")
            sb.append(String.format("%-11s | %-15s | %-15s | %-12s | %-15s\n", "Data Reg.", "Localização", "Tipo de Lesão", "Área/Dimens.", "Padrão Tecidual"))
            sb.append("--------------------------------------------------------------------------------\n")
            for (l in lesions) {
                val dims = "${l.larguraCm}x${l.comprimentoCm} cm"
                val areaStr = if (l.areaCm2.isNotBlank()) "(${l.areaCm2} cm²)" else ""
                val dimFull = "$dims $areaStr"
                sb.append(String.format("%-11s | %-15s | %-15s | %-12s | %-15s\n", 
                    l.dataRegistro.take(11), 
                    l.localizacao.take(15), 
                    l.tipoLesao.take(15), 
                    dimFull.take(12), 
                    l.tipoTecido.take(15)
                ))
            }
            sb.append("--------------------------------------------------------------------------------\n")
            sb.toString()
        } else ""

        // Helper technical mappings for blank fields in organ systems
        val loc = if (sistemaLocomotor.isBlank()) "Deambulação ativa, sem déficits motores ou limitações de amplitude nas articulações (eumotricidade). Marcha de padrão estável, força muscular preservada globalmente." else sistemaLocomotor
        val cv = if (sistemaCardiovascular.isBlank()) "Bulhas rítmicas e normofonéticas em dois tempos (BRNF2T), ausência de sopros ou ruídos extras. Hemodinamicamente estável, perfusão periférica adequada (TEC < 2s). Eufígmico." else sistemaCardiovascular
        val resp = if (sistemaRespiratorio.isBlank()) "Eupneico em ar ambiente (AA). Expansibilidade torácica mantida de forma simétrica; murmúrio vesicular universalmente audível (MVUA) sem ruídos adventícios." else sistemaRespiratorio
        val neur = if (sistemaNeurologico.isBlank()) "Consciente, orientado no tempo e espaço (COTE). Escala de Glasgow = 15. Pupilas isocóricas e fotorreagentes (MIFR). Ausência de déficits neurológicos focais apparentes." else sistemaNeurologico
        val nutr = if (sistemaNutricao.isBlank()) "Eutrófico, mucosa oral corada, íntegra e hidratada. Ingestão dietética líquida/sólida por via oral (VO) preservada, sem queixas de disfagia ou sialorreia." else sistemaNutricao
        val urin = if (sistemaUrinario.isBlank()) "Fisiologia geniturinária preservada. Eliminações vesicais presentes, diurese espontânea de coloração amarelada e aspecto límpido, sem relatos de disúria ou polaciúria." else sistemaUrinario
        val intest = if (sistemaIntestinal.isBlank()) "Abdômen plano, flácido, indolor à palpação superficial e profunda. Ruídos hidroaéreos (RHA) presentes nos quatro quadrantes. Eliminações intestinais presentes." else sistemaIntestinal
        val integ = if (sistemaIntegridadePele.isBlank()) "Tegumento íntegro, turgor cutâneo e elasticidade preservados. Ausência de lesões elementares, hematomas, equimoses ou sinais flogísticos na admissão hospitalar." else sistemaIntegridadePele
        val disp = if (dispositivos.isBlank()) "Livre de acessos venosos invasivos, cateterizações ou sondagens (ausência de SVD, SNE ou drenos ativos no momento da admissão)." else dispositivos

        return """
            ================================================================================
                     PARECER DE ENFERMAGEM ADMISSIONAL - PACIENTE CIRÚRGICO (SAE)
            ================================================================================
            REGISTRO DE SISTEMATIZAÇÃO DA ASSISTÊNCIA DE ENFERMAGEM | DATA/HORA: $dataFormatada

            I. IDENTIFICAÇÃO E DADOS CLÍNICOS DO PACIENTE
            --------------------------------------------------------------------------------
            • Paciente: ${nome.ifBlank { "Não especificado" }}
            • Faixa Etária/Idade: ${idade.ifBlank { "Não informada" }} anos | Gênero: ${sexo.ifBlank { "Não informado" }}
            • Crença Religiosa: ${religiao.ifBlank { "Não referida" }} | Status Civil: ${estadoCivil.ifBlank { "Não informado" }}
            • Atividade Laboral: ${funcaoLaboral.ifBlank { "Não cadastrada" }}
            • Proveniência Clínica: ${proveniencia.ifBlank { "Não informada" }}
            • Prontuário Clínico: nº ${prontuario.ifBlank { "Não especificado" }}
            • Localização Hospitalar: Enfermaria: ${enfermaria.ifBlank { "N/A" }} | Leito: ${leito.ifBlank { "N/A" }}
            • Procedimento Cirúrgico Proposto: ${tipoCirurgia.ifBlank { "Não informado/A definir" }}

            * SINAIS VITAIS (REGISTRO FISIOLÓGICO DE ADMISSÃO):
              - Pressão Arterial (PA): ${pressaoArterial.ifBlank { "Não registrada" }} mmHg
              - Frequência Cardíaca (FC): ${frequenciaCardiaca.ifBlank { "Não aferida" }} bpm (${if (frequenciaCardiaca.isNotBlank()) {
                  val fcVal = frequenciaCardiaca.replace(Regex("[^0-9]"), "").toIntOrNull()
                  if (fcVal != null) {
                      when {
                          fcVal < 60 -> "bradicardia"
                          fcVal > 100 -> "taquicardia"
                          else -> "normotensivo/eucardia"
                      }
                  } else "regularidade ritmica"
              } else "parâmetro ausente"})
              - Frequência Respiratória (FR): ${frequenciaRespiratoria.ifBlank { "Não aferida" }} ipm (${if (frequenciaRespiratoria.isNotBlank()) {
                  val frVal = frequenciaRespiratoria.replace(Regex("[^0-9]"), "").toIntOrNull()
                  if (frVal != null) {
                      when {
                          frVal < 12 -> "bradipcneia"
                          frVal > 20 -> "taquipneia"
                          else -> "eupneia/padrão ventilatório estável"
                      }
                  } else "ritmicidade respiratória"
              } else "parâmetro ausente"})
              - Temperatura Corporal (T): ${temperatura.ifBlank { "Não aferida" }} ºC (${if (temperatura.isNotBlank()) {
                  val tVal = temperatura.replace(",", ".").replace(Regex("[^0-9.]"), "").toDoubleOrNull()
                  if (tVal != null) {
                      when {
                          tVal < 35.0 -> "hipotermia severa/moderada"
                          tVal >= 37.8 -> "estado febril/piréxia"
                          tVal >= 37.3 -> "subfebril"
                          else -> "afebril/normotermia"
                      }
                  } else "termorregulação cutânea"
              } else "parâmetro ausente"})
              - Saturação de Oxigênio (SatO2): ${saturacaoO2.ifBlank { "Não aferida" }} % (${if (saturacaoO2.isNotBlank()) {
                  val satVal = saturacaoO2.replace(Regex("[^0-9]"), "").toIntOrNull()
                  if (satVal != null) {
                      if (satVal < 95) "indício de hipoxemia" else "perfusão periférica de oxigênio adequada"
                  } else "preservação tecidual"
              } else "parâmetro ausente"})

            II. ANAMNESE E HISTÓRICO PATOLÓGICO SELECIONADO
            --------------------------------------------------------------------------------
            • Doenças Prévias/Comorbidades: ${doencasAnteriores.ifBlank { "Sem comorbidades prévias relatadas." }}
            • Condições Clínicas Coexistentes: ${condicoesAnteriores.ifBlank { "Sem condições clínicas agudas crônicas coexistentes associadas." }}
            • Antecedentes Cirúrgicos: ${cirurgiasAnteriores.ifBlank { "Sem histórico de procedimentos cirúrgicos prévios." }}
            • Quadro Alérgico (Hipersensibilidade): ${alergias.ifBlank { "Alerta: Sem reações de hipersensibilidade orais ou medicamentosas conhecidas." }}
            • Farmacologia em Uso Contínuo: ${medicacoesUso.ifBlank { "Nega terapia farmacológica contínua domiciliar ou de uso recente." }}
            • Investigação Diagnóstica (Exames): ${exames.ifBlank { "Sem exames complementares de admissão anexados no momento." }}

            III. EXAME FÍSICO COM PREDOMÍNIO DE TERMOS TÉCNICOS (EXAME SISTÊMICO)
            --------------------------------------------------------------------------------
            • Sistema Locomotor / Mobilidade:
              $loc
            
            • Sistema Cardiovascular / Hemodinâmica:
              $cv
            
            • Sistema Respiratório / Ventilação:
              $resp
            
            • Sistema Neurológico / Psiquismo:
              $neur
            
            • Nutrição, Hidratação e Cavidade Oral:
              $nutr
            
            • Sistema Geniturinário:
              $urin
            
            • Sistema Gastrointestinal e Eliminações:
              $intest
            
            • Sistema Tegumentar / Integridade Cutâneo-Mucosa:
              $integ
              ${if (!lesionWidthCm.isNullOrBlank()) "--> Planimetria Volumétrica da Lesão Principal: ${lesionWidthCm}x${lesionHeightCm} cm (Área estimada: ${lesionAreaSquareCm} cm²)\n  --> Características Morfológicas descritas: $lesionDescriptionPlanimetria" else ""}
            
            • Dispositivos Invasivos e Acessos:
              $disp

            IV. AVALIAÇÃO SENSÓRIO-COGNITIVA
            --------------------------------------------------------------------------------
            • Acuidade Visual: ${acuidadeVisual.ifBlank { "Preservada, sem necessidade de lentes corretivas." }}
            • Acuidade Auditiva: ${acuidadeAuditiva.ifBlank { "Preservada, comunicação verbal sem barreiras (eufasia)." }}

            V. ESTRATIFICAÇÃO DE RISCO CLÍNICO E ESCALAS MULTIDIMENSIONAL
            --------------------------------------------------------------------------------
            A) ESCALA DE BRADEN (Avaliação de Risco para Lesão por Pressão - LPP):
               • Critério Sensorial: $bradenPercepcaoSensorial | Umidade: $bradenUmidade | Atividade: $bradenAtividade
               • Mobilidade: $bradenMobilidade | Nutrição: $bradenNutricao | Fricção e Cisalhamento: $bradenFriccaoCisalhamento
               • ESCORE TOTAL DE BRADEN: ${bradenScore()} / 23 pontos
               • DIAGNÓSTICO DE RISCO (Braden): ${bradenClassification().uppercase()}

            B) ESCALA DE FUGULIN (Estratificação de Grau de Dependência Assistencial da Enfermagem):
               • Estado Mental: $fugulinEstadoMental | Oxigenação: $fugulinOxigenacao | Sinais Vitais: $fugulinSinaisVitais
               • Motilidade: $fugulinMotilidade | Locomoção: $fugulinLocomocao | Cuidado Corporal: $fugulinCuidadoCorporal
               • Eliminação: $fugulinEliminacao | Nutrição/Hidratação: $fugulinNutricaoHidratacao | Terapêutica: $fugulinTerapeutica
               • ESCORE TOTAL DE FUGULIN: ${fugulinScore()} / 36 pontos
               • COMPLEXIDADE DA ASSISTÊNCIA: ${fugulinClassification().uppercase()}

            C) ESCALA DE MORSE (Predisposição e Risco de Evento de Quedas):
               • Historial de Queda Recente: $morseHistoricoQuedas | Diagnóstico Secundário: $morseDiagnosticoSecundario
               • Suporte para Deambulação: $morseAuxilioLocomocao | Terapia Endovenosa Ativa: $morseTerapiaEV
               • Padrão de Marcha Corporal: $morseMarcha | Estado Mental Cognitivo: $morseEstadoMental
               • ESCORE TOTAL DE MORSE: ${morseScore()} pontos
               • PROTOCOLO DE PREVENÇÃO (Morse): ${morseClassification().uppercase()}

            D) ESCALA DE COMA DE GLASGOW (Nível de Reatividade Neurológica):
               • Abertura Ocular: $glasgowAberturaOcular | Resposta Verbal: $glasgowRespostaVerbal | Resposta Motora: $glasgowRespostaMotora
               • ESCORE TOTAL DE GLASGOW: ${glasgowScore()} / 15 pontos (${glasgowClassification().uppercase()})

            E) ESCALA VISUAL ANALÓGICA DA DOR (EVA - Semioticamente Estruturada):
               • Nível de Intensidade Álgica: $dorNivel / 10 (${dorClassification().uppercase()})
               • Localização Anatômica: ${dorLocalizacao.ifBlank { "Sem foco de dor ativo de acordo com relato verbal espontâneo." }}
               • Características Clínicas da Dor: ${dorCaracteristicas.ifBlank { "Nega queixas álgicas no momento." }}
            $lesionsTable
            ${if (aiInterventionsResult.isNotBlank()) "\n            ================================================================================\n              DIRETRIZES TERAPÊUTICAS E INTERVENÇÕES DA ASSISTÊNCIA (IA - GEMINI)\n            ================================================================================\n            $aiInterventionsResult" else ""}

            --------------------------------------------------------------------------------
            Relatório de Enfermagem padronizado e gerado eletronicamente em: $dataFormatada
            ================================================================================
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

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
    val altura: String = "",
    val peso: String = "",
    
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
    val drenosJson: String = "",
    val dispositivosJson: String = "",
    
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
    val historiaDoencaAtual: String = "",
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

    fun imcValue(): Double? {
        val h = altura.replace(",", ".").toDoubleOrNull()
        val w = peso.replace(",", ".").toDoubleOrNull()
        if (h != null && w != null && h > 0) {
            val heightInMeters = if (h > 3.0) h / 100.0 else h
            return w / (heightInMeters * heightInMeters)
        }
        return null
    }

    fun imcClassification(): String {
        val imc = imcValue() ?: return "Dados insuficientes"
        return when {
            imc < 16.0 -> "Desnutrição Grau III"
            imc in 16.0..16.99 -> "Desnutrição Grau II"
            imc in 17.0..18.49 -> "Desnutrição Grau I"
            imc in 18.5..24.99 -> "Peso Normal"
            imc in 25.0..29.99 -> "Sobrepeso"
            imc in 30.0..34.99 -> "Obesidade Grau I"
            imc in 35.0..39.99 -> "Obesidade Grau II"
            else -> "Obesidade Grau III (Mórbida)"
        }
    }

    fun getDrenosList(): List<DrenoItem> {
        val defaultList = listOf(
            DrenoItem("Blake", false, "", "", ""),
            DrenoItem("Portovac", false, "", "", ""),
            DrenoItem("Tórax sem selo d'água", false, "", "", ""),
            DrenoItem("Tórax com selo d'água", false, "", "", ""),
            DrenoItem("Tubular", false, "", "", ""),
            DrenoItem("Tubolaminar", false, "", "", ""),
            DrenoItem("Mediastino", false, "", "", ""),
            DrenoItem("Outros", false, "", "", "")
        )
        if (drenosJson.isBlank()) {
            return defaultList
        }
        return try {
            val list = mutableListOf<DrenoItem>()
            val arr = org.json.JSONArray(drenosJson)
            val map = mutableMapOf<String, DrenoItem>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val tipo = obj.optString("tipo", "")
                if (tipo.isNotBlank()) {
                    map[tipo] = DrenoItem(
                        tipo = tipo,
                        selecionado = obj.optBoolean("selecionado", false),
                        lado = obj.optString("lado", ""),
                        debito = obj.optString("debito", ""),
                        aspecto = obj.optString("aspecto", "")
                    )
                }
            }
            defaultList.map { defaultItem ->
                map[defaultItem.tipo] ?: defaultItem
            }
        } catch (e: Exception) {
            defaultList
        }
    }

    fun withUpdatedDrenos(list: List<DrenoItem>): AdmissionRecord {
        val arr = org.json.JSONArray()
        for (item in list) {
            val obj = org.json.JSONObject().apply {
                put("tipo", item.tipo)
                put("selecionado", item.selecionado)
                put("lado", item.lado)
                put("debito", item.debito)
                put("aspecto", item.aspecto)
            }
            arr.put(obj)
        }
        return this.copy(drenosJson = arr.toString())
    }

    fun getDispositivosList(): List<DispositivoItem> {
        val defaultList = listOf(
            DispositivoItem("AVP", false, "", "", "", "", ""),
            DispositivoItem("CVC", false, "", "", "", "", ""),
            DispositivoItem("FAV MS", false, "", "", "", "", ""),
            DispositivoItem("CAT HD", false, "", "", "", "", "")
        )
        if (dispositivosJson.isBlank()) {
            return defaultList
        }
        return try {
            val list = mutableListOf<DispositivoItem>()
            val arr = org.json.JSONArray(dispositivosJson)
            val map = mutableMapOf<String, DispositivoItem>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val tipo = obj.optString("tipo", "")
                if (tipo.isNotBlank()) {
                    map[tipo] = DispositivoItem(
                        tipo = tipo,
                        selecionado = obj.optBoolean("selecionado", false),
                        lado = obj.optString("lado", ""),
                        dataPuncao = obj.optString("dataPuncao", ""),
                        curativoTipo = obj.optString("curativoTipo", ""),
                        dataTroca = obj.optString("dataTroca", ""),
                        dataRetirada = obj.optString("dataRetirada", "")
                    )
                }
            }
            defaultList.map { defaultItem ->
                map[defaultItem.tipo] ?: defaultItem
            }
        } catch (e: Exception) {
            defaultList
        }
    }

    fun withUpdatedDispositivos(list: List<DispositivoItem>): AdmissionRecord {
        val arr = org.json.JSONArray()
        for (item in list) {
            val obj = org.json.JSONObject().apply {
                put("tipo", item.tipo)
                put("selecionado", item.selecionado)
                put("lado", item.lado)
                put("dataPuncao", item.dataPuncao)
                put("curativoTipo", item.curativoTipo)
                put("dataTroca", item.dataTroca)
                put("dataRetirada", item.dataRetirada)
            }
            arr.put(obj)
        }
        return this.copy(dispositivosJson = arr.toString())
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

        // Section I: Identification and Sinais Vitais (Only non-blank items)
        val idDetails = mutableListOf<String>()
        if (nome.isNotBlank()) idDetails.add("• Paciente: $nome")
        if (idade.isNotBlank()) idDetails.add("• Faixa Etária/Idade: $idade anos")
        if (sexo.isNotBlank()) idDetails.add("• Gênero: $sexo")
        if (religiao.isNotBlank()) idDetails.add("• Crença Religiosa: $religiao")
        if (estadoCivil.isNotBlank()) idDetails.add("• Status Civil: $estadoCivil")
        if (funcaoLaboral.isNotBlank()) idDetails.add("• Atividade Laboral: $funcaoLaboral")
        if (proveniencia.isNotBlank()) idDetails.add("• Proveniência Clínica: $proveniencia")
        if (prontuario.isNotBlank()) idDetails.add("• Prontuário Clínico: nº $prontuario")
        if (enfermaria.isNotBlank()) idDetails.add("• Enfermaria: $enfermaria")
        if (leito.isNotBlank()) idDetails.add("• Leito: $leito")
        if (tipoCirurgia.isNotBlank()) idDetails.add("• Procedimento Cirúrgico Proposto: $tipoCirurgia")

        val svDetails = mutableListOf<String>()
        if (pressaoArterial.isNotBlank()) svDetails.add("  - Pressão Arterial (PA): $pressaoArterial mmHg")
        if (frequenciaCardiaca.isNotBlank()) {
            val fcVal = frequenciaCardiaca.replace(Regex("[^0-9]"), "").toIntOrNull()
            val classification = if (fcVal != null) {
                when {
                    fcVal < 60 -> "bradicardia"
                    fcVal > 100 -> "taquicardia"
                    else -> "normotensivo/eucardia"
                }
            } else "regularidade rítmica"
            svDetails.add("  - Frequência Cardíaca (FC): $frequenciaCardiaca bpm ($classification)")
        }
        if (frequenciaRespiratoria.isNotBlank()) {
            val frVal = frequenciaRespiratoria.replace(Regex("[^0-9]"), "").toIntOrNull()
            val classification = if (frVal != null) {
                when {
                    frVal < 12 -> "bradipcneia"
                    frVal > 20 -> "taquipneia"
                    else -> "eupneia/padrão ventilatório estável"
                }
            } else "ritmicidade respiratória"
            svDetails.add("  - Frequência Respiratória (FR): $frequenciaRespiratoria ipm ($classification)")
        }
        if (temperatura.isNotBlank()) {
            val tVal = temperatura.replace(",", ".").replace(Regex("[^0-9.]"), "").toDoubleOrNull()
            val classification = if (tVal != null) {
                when {
                    tVal < 35.0 -> "hipotermia severa/moderada"
                    tVal >= 37.8 -> "estado febril/piréxia"
                    tVal >= 37.3 -> "subfebril"
                    else -> "afebril/normotermia"
                }
            } else "termorregulação cutânea"
            svDetails.add("  - Temperatura Corporal (T): $temperatura ºC ($classification)")
        }
        if (saturacaoO2.isNotBlank()) {
            val satVal = saturacaoO2.replace(Regex("[^0-9]"), "").toIntOrNull()
            val classification = if (satVal != null) {
                if (satVal < 95) "indício de hipoxemia" else "perfusão periférica de oxigênio adequada"
            } else "preservação tecidual"
            svDetails.add("  - Saturação de Oxigênio (SatO2): $saturacaoO2% ($classification)")
        }
        if (altura.isNotBlank()) svDetails.add("  - Altura: $altura")
        if (peso.isNotBlank()) svDetails.add("  - Peso: $peso")
        if (imcValue() != null) {
            svDetails.add("  - IMC: ${String.format("%.2f kg/m²", imcValue())} (${imcClassification()})")
        }

        // Section II: Anamnese / Historico (Only non-blank items)
        val histDetails = mutableListOf<String>()
        if (doencasAnteriores.isNotBlank()) histDetails.add("• Doenças Prévias/Comorbidades: $doencasAnteriores")
        if (historiaDoencaAtual.isNotBlank()) histDetails.add("• História da Doença Atual (HDA): $historiaDoencaAtual")
        if (cirurgiasAnteriores.isNotBlank()) histDetails.add("• Antecedentes Cirúrgicos: $cirurgiasAnteriores")
        if (alergias.isNotBlank()) histDetails.add("• Quadro Alérgico (Hipersensibilidade): $alergias")
        if (medicacoesUso.isNotBlank()) histDetails.add("• Farmacologia em Uso Contínuo: $medicacoesUso")
        if (exames.isNotBlank()) histDetails.add("• Investigação Diagnóstica (Exames): $exames")

        // Section III: Exame Fisico (Exame Sistemico) concatenated
        val activeDispositivos = getDispositivosList().filter { it.selecionado }
        val dispText = if (activeDispositivos.isNotEmpty()) {
            val dList = activeDispositivos.map { item ->
                when (item.tipo) {
                    "AVP" -> "AVP (Acesso Venoso Periférico) lado ${item.lado.ifBlank { "N/I" }} puncionado em ${item.dataPuncao.ifBlank { "N/I" }}"
                    "CVC" -> {
                        val details = mutableListOf<String>()
                        if (item.lado.isNotBlank()) details.add("sítio ${item.lado}")
                        if (item.dataPuncao.isNotBlank()) details.add("punção em ${item.dataPuncao}")
                        if (item.curativoTipo.isNotBlank()) details.add("curativo ${item.curativoTipo}")
                        if (item.dataTroca.isNotBlank()) details.add("troca em ${item.dataTroca}")
                        if (item.dataRetirada.isNotBlank()) details.add("retirada em ${item.dataRetirada}")
                        "CVC (Acesso Venoso Central) com ${details.joinToString(", ")}"
                    }
                    "FAV MS" -> "FAV MS (Fístula Arteriovenosa em Membro Superior) lado ${item.lado.ifBlank { "N/I" }}"
                    "CAT HD" -> "CAT HD (Cateter de Hemodiálise) sítio ${item.lado.ifBlank { "N/I" }}"
                    else -> item.tipo
                }
            }
            "dispositivos invasivos: ${dList.joinToString(", ")}" + if (dispositivos.isNotBlank()) ", outros: $dispositivos" else ""
        } else {
            if (dispositivos.isNotBlank()) "dispositivos invasivos: $dispositivos" else ""
        }

        val activeDrenos = getDrenosList().filter { it.selecionado }
        val drenosText = if (activeDrenos.isNotEmpty()) {
            val drList = activeDrenos.map { item ->
                val ladoStr = if (item.lado.isNotBlank()) " lado ${item.lado}" else ""
                "dreno ${item.tipo}$ladoStr com débito ${item.debito.ifBlank { "não informado" }} e aspecto ${item.aspecto.ifBlank { "não informado" }}"
            }
            "drenos ativos: ${drList.joinToString(", ")}"
        } else ""

        val examItems = mutableListOf<String>()
        if (sistemaLocomotor.isNotBlank()) examItems.add(sistemaLocomotor.trim().removeSuffix(";").removeSuffix("."))
        if (sistemaCardiovascular.isNotBlank()) examItems.add(sistemaCardiovascular.trim().removeSuffix(";").removeSuffix("."))
        if (sistemaRespiratorio.isNotBlank()) examItems.add(sistemaRespiratorio.trim().removeSuffix(";").removeSuffix("."))
        if (sistemaNeurologico.isNotBlank()) examItems.add(sistemaNeurologico.trim().removeSuffix(";").removeSuffix("."))
        if (sistemaNutricao.isNotBlank()) examItems.add(sistemaNutricao.trim().removeSuffix(";").removeSuffix("."))
        if (sistemaUrinario.isNotBlank()) examItems.add(sistemaUrinario.trim().removeSuffix(";").removeSuffix("."))
        if (sistemaIntestinal.isNotBlank()) examItems.add(sistemaIntestinal.trim().removeSuffix(";").removeSuffix("."))
        if (sistemaIntegridadePele.isNotBlank()) {
            var skinText = sistemaIntegridadePele.trim().removeSuffix(";").removeSuffix(".")
            if (!lesionWidthCm.isNullOrBlank()) {
                skinText += ", planimetria volumétrica da lesão principal: ${lesionWidthCm}x${lesionHeightCm} cm (área estimada: ${lesionAreaSquareCm} cm²), características morfológicas: $lesionDescriptionPlanimetria"
            }
            examItems.add(skinText)
        }
        if (dispText.isNotBlank()) examItems.add(dispText)
        if (drenosText.isNotBlank()) examItems.add(drenosText)

        // Section IV: Sensório-Cognitiva
        val sensoryDetails = mutableListOf<String>()
        if (acuidadeVisual.isNotBlank()) sensoryDetails.add("• Acuidade Visual: $acuidadeVisual")
        if (acuidadeAuditiva.isNotBlank()) sensoryDetails.add("• Acuidade Auditiva: $acuidadeAuditiva")

        // Section V: Escalas - Dor details
        val dorDetails = mutableListOf<String>()
        dorDetails.add("• Nível de Intensidade Álgica: $dorNivel / 10 (${dorClassification().uppercase()})")
        if (dorLocalizacao.isNotBlank()) dorDetails.add("• Localização Anatômica: $dorLocalizacao")
        if (dorCaracteristicas.isNotBlank()) dorDetails.add("• Características Clínicas da Dor: $dorCaracteristicas")

        val sb = java.lang.StringBuilder()
        sb.append("================================================================================\n")
        sb.append("         PARECER DE ENFERMAGEM ADMISSIONAL - PACIENTE CIRÚRGICO (SAE)\n")
        sb.append("================================================================================\n")
        sb.append("REGISTRO DE SISTEMATIZAÇÃO DA ASSISTÊNCIA DE ENFERMAGEM | DATA/HORA: $dataFormatada\n\n")

        if (idDetails.isNotEmpty() || svDetails.isNotEmpty()) {
            sb.append("I. IDENTIFICAÇÃO E DADOS CLÍNICOS DO PACIENTE\n")
            sb.append("--------------------------------------------------------------------------------\n")
            idDetails.forEach { sb.append(it).append("\n") }
            if (svDetails.isNotEmpty()) {
                sb.append("\n* SINAIS VITAIS (REGISTRO FISIOLÓGICO DE ADMISSÃO):\n")
                svDetails.forEach { sb.append(it).append("\n") }
            }
            sb.append("\n")
        }

        if (histDetails.isNotEmpty()) {
            sb.append("II. ANAMNESE E HISTÓRICO PATOLÓGICO SELECIONADO\n")
            sb.append("--------------------------------------------------------------------------------\n")
            histDetails.forEach { sb.append(it).append("\n") }
            sb.append("\n")
        }

        if (examItems.isNotEmpty()) {
            sb.append("III. EXAME FÍSICO COM PREDOMÍNIO DE TERMOS TÉCNICOS (EXAME SISTÊMICO)\n")
            sb.append("--------------------------------------------------------------------------------\n")
            sb.append(examItems.joinToString("; ")).append(".\n\n")
        }

        if (sensoryDetails.isNotEmpty()) {
            sb.append("IV. AVALIAÇÃO SENSÓRIO-COGNITIVA\n")
            sb.append("--------------------------------------------------------------------------------\n")
            sensoryDetails.forEach { sb.append(it).append("\n") }
            sb.append("\n")
        }

        sb.append("V. ESTRATIFICAÇÃO DE RISCO CLÍNICO E ESCALAS MULTIDIMENSIONAL\n")
        sb.append("--------------------------------------------------------------------------------\n")
        sb.append("A) ESCALA DE BRADEN (Avaliação de Risco para Lesão por Pressão - LPP):\n")
        sb.append("   • Critério Sensorial: $bradenPercepcaoSensorial | Umidade: $bradenUmidade | Atividade: $bradenAtividade\n")
        sb.append("   • Mobilidade: $bradenMobilidade | Nutrição: $bradenNutricao | Fricção e Cisalhamento: $bradenFriccaoCisalhamento\n")
        sb.append("   • ESCORE TOTAL DE BRADEN: ${bradenScore()} / 23 pontos\n")
        sb.append("   • DIAGNÓSTICO DE RISCO (Braden): ${bradenClassification().uppercase()}\n\n")

        sb.append("B) ESCALA DE FUGULIN (Estratificação de Grau de Dependência Assistencial da Enfermagem):\n")
        sb.append("   • Estado Mental: $fugulinEstadoMental | Oxigenação: $fugulinOxigenacao | Sinais Vitais: $fugulinSinaisVitais\n")
        sb.append("   • Motilidade: $fugulinMotilidade | Locomoção: $fugulinLocomocao | Cuidado Corporal: $fugulinCuidadoCorporal\n")
        sb.append("   • Eliminação: $fugulinEliminacao | Nutrição/Hidratação: $fugulinNutricaoHidratacao | Terapêutica: $fugulinTerapeutica\n")
        sb.append("   • ESCORE TOTAL DE FUGULIN: ${fugulinScore()} / 36 pontos\n")
        sb.append("   • COMPLEXIDADE DA ASSISTÊNCIA: ${fugulinClassification().uppercase()}\n\n")

        sb.append("C) ESCALA DE MORSE (Predisposição e Risco de Evento de Quedas):\n")
        sb.append("   • Historial de Queda Recente: $morseHistoricoQuedas | Diagnóstico Secundário: $morseDiagnosticoSecundario\n")
        sb.append("   • Suporte para Deambulação: $morseAuxilioLocomocao | Terapia Endovenosa Ativa: $morseTerapiaEV\n")
        sb.append("   • Padrão de Marcha Corporal: $morseMarcha | Estado Mental Cognitivo: $morseEstadoMental\n")
        sb.append("   • ESCORE TOTAL DE MORSE: ${morseScore()} pontos\n")
        sb.append("   • PROTOCOLO DE PREVENÇÃO (Morse): ${morseClassification().uppercase()}\n\n")

        sb.append("D) ESCALA DE COMA DE GLASGOW (Nível de Reatividade Neurológica):\n")
        sb.append("   • Abertura Ocular: $glasgowAberturaOcular | Resposta Verbal: $glasgowRespostaVerbal | Resposta Motora: $glasgowRespostaMotora\n")
        sb.append("   • ESCORE TOTAL DE GLASGOW: ${glasgowScore()} / 15 pontos (${glasgowClassification().uppercase()})\n\n")

        sb.append("E) ESCALA VISUAL ANALÓGICA DA DOR (EVA - Semioticamente Estruturada):\n")
        dorDetails.forEach { sb.append("   ").append(it).append("\n") }

        if (lesionsTable.isNotBlank()) {
            sb.append("\n").append(lesionsTable)
        }

        if (aiInterventionsResult.isNotBlank()) {
            sb.append("\n================================================================================\n")
            sb.append("  DIRETRIZES TERAPÊUTICAS E INTERVENÇÕES DA ASSISTÊNCIA (IA - GEMINI)\n")
            sb.append("================================================================================\n")
            sb.append(aiInterventionsResult).append("\n")
        }

        sb.append("\n--------------------------------------------------------------------------------\n")
        sb.append("Relatório de Enfermagem padronizado e gerado eletronicamente em: $dataFormatada\n")
        sb.append("================================================================================\n")

        return sb.toString()
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

data class DrenoItem(
    val tipo: String,
    val selecionado: Boolean = false,
    val lado: String = "", // "D", "E", or ""
    val debito: String = "",
    val aspecto: String = ""
)

data class DispositivoItem(
    val tipo: String, // "AVP", "CVC", "FAV MS", "CAT HD"
    val selecionado: Boolean = false,
    val lado: String = "", // "D", "E", or specific site like "VJD", "VSCD"
    val dataPuncao: String = "",
    val curativoTipo: String = "", // "convencional" or "filme"
    val dataTroca: String = "",
    val dataRetirada: String = ""
)

package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "admissions")
data class AdmissionRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val recordType: String = "Admissão",
    
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
    val glicemia: String = "",
    val altura: String = "",
    val peso: String = "",
    val hemocomponentesJson: String = "",
    
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
    val dorCaracteristicas: String = "",
    
    // 10. Escala de Bates-Jensen (BWAT)
    val bwatProfundidade: Int = 1,
    val bwatBordas: Int = 1,
    val bwatTecidoNecroticoTipo: Int = 1,
    val bwatTecidoNecroticoQtd: Int = 1,
    val bwatExsudatoTipo: Int = 1,
    val bwatExsudatoQtd: Int = 1,
    
    val condutas: String = ""
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

    fun bwatScore(): Int = bwatProfundidade + bwatBordas + bwatTecidoNecroticoTipo +
            bwatTecidoNecroticoQtd + bwatExsudatoTipo + bwatExsudatoQtd

    fun bwatClassification(): String {
        val score = bwatScore()
        return when {
            score <= 10 -> "Regeneração Ativa / Saudável"
            score in 11..20 -> "Regeneração Estagnada"
            else -> "Ferida Grave / Degenerativa"
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

    fun getHemocomponentesList(): List<HemocomponenteItem> {
        val defaultList = listOf(
            HemocomponenteItem("CH"),
            HemocomponenteItem("CP"),
            HemocomponenteItem("PFC"),
            HemocomponenteItem("CRIO"),
            HemocomponenteItem("CG")
        )
        if (hemocomponentesJson.isBlank()) {
            return defaultList
        }
        return try {
            val list = mutableListOf<HemocomponenteItem>()
            val arr = org.json.JSONArray(hemocomponentesJson)
            val map = mutableMapOf<String, HemocomponenteItem>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                var tipo = obj.optString("tipo", "")
                if (tipo.trim().uppercase() == "CHORAR") {
                    tipo = "CRIO"
                }
                if (tipo.isNotBlank()) {
                    map[tipo] = HemocomponenteItem(
                        tipo = tipo,
                        selecionado = obj.optBoolean("selecionado", false),
                        diaHemotransfusao = obj.optString("diaHemotransfusao", ""),
                        numeroBolsa = obj.optString("numeroBolsa", ""),
                        validade = obj.optString("validade", ""),
                        tipagemSanguinea = obj.optString("tipagemSanguinea", ""),
                        volumeInfundido = obj.optString("volumeInfundido", ""),
                        reacaoTransfusional = obj.optString("reacaoTransfusional", "Não"),
                        reacaoQuais = obj.optString("reacaoQuais", ""),
                        inicioPA = obj.optString("inicioPA", ""),
                        inicioFC = obj.optString("inicioFC", ""),
                        inicioFR = obj.optString("inicioFR", ""),
                        inicioT = obj.optString("inicioT", ""),
                        inicioSatO2 = obj.optString("inicioSatO2", ""),
                        fimPA = obj.optString("fimPA", ""),
                        fimFC = obj.optString("fimFC", ""),
                        fimFR = obj.optString("fimFR", ""),
                        fimT = obj.optString("fimT", ""),
                        fimSatO2 = obj.optString("fimSatO2", "")
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

    fun withUpdatedHemocomponentes(list: List<HemocomponenteItem>): AdmissionRecord {
        val arr = org.json.JSONArray()
        for (item in list) {
            val obj = org.json.JSONObject().apply {
                put("tipo", item.tipo)
                put("selecionado", item.selecionado)
                put("diaHemotransfusao", item.diaHemotransfusao)
                put("numeroBolsa", item.numeroBolsa)
                put("validade", item.validade)
                put("tipagemSanguinea", item.tipagemSanguinea)
                put("volumeInfundido", item.volumeInfundido)
                put("reacaoTransfusional", item.reacaoTransfusional)
                put("reacaoQuais", item.reacaoQuais)
                put("inicioPA", item.inicioPA)
                put("inicioFC", item.inicioFC)
                put("inicioFR", item.inicioFR)
                put("inicioT", item.inicioT)
                put("inicioSatO2", item.inicioSatO2)
                put("fimPA", item.fimPA)
                put("fimFC", item.fimFC)
                put("fimFR", item.fimFR)
                put("fimT", item.fimT)
                put("fimSatO2", item.fimSatO2)
            }
            arr.put(obj)
        }
        return this.copy(hemocomponentesJson = arr.toString())
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
                        fotoUri = if (obj.isNull("fotoUri")) null else obj.optString("fotoUri"),
                        descricaoAudio = obj.optString("descricaoAudio", ""),
                        tipoCobertura = obj.optString("tipoCobertura", ""),
                        dataTroca = obj.optString("dataTroca", ""),
                        classificacaoJanetJensey = obj.optString("classificacaoJanetJensey", "")
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
                put("descricaoAudio", item.descricaoAudio)
                put("tipoCobertura", item.tipoCobertura)
                put("dataTroca", item.dataTroca)
                put("classificacaoJanetJensey", item.classificacaoJanetJensey)
            }
            arr.put(obj)
        }
        return this.copy(lesionsSpreadsheetJson = arr.toString())
    }

    fun generateReport(): String {
        val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val hourFormat = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        val dataStr = dateFormat.format(java.util.Date(timestamp))
        val horaStr = hourFormat.format(java.util.Date(timestamp))
        val dataFormatada = "$dataStr $horaStr"
        
        val isEvolucao = recordType == "Evolução"
        val lesions = getLesionsList()

        val sb = java.lang.StringBuilder()
        sb.append("================================================================================\n")
        if (isEvolucao) {
            sb.append("                   EVOLUÇÃO DE ENFERMAGEM - PACIENTE CIRÚRGICO\n")
        } else {
            sb.append("         PARECER DE ENFERMAGEM ADMISSIONAL - PACIENTE CIRÚRGICO (SAE)\n")
        }
        sb.append("================================================================================\n")
        sb.append("REGISTRO DE SISTEMATIZAÇÃO DA ASSISTÊNCIA DE ENFERMAGEM | DATA/HORA: $dataFormatada\n\n")

        sb.append("EVOLUÇÃO CLÍNICA NARRATIVA E RELATÓRIO PADRONIZADO:\n")
        sb.append("--------------------------------------------------------------------------------\n")

        val nomeStr = nome.ifBlank { "_____" }
        val idadeStr = idade.ifBlank { "____" }
        val sexoStr = sexo.ifBlank { "_____" }
        val estadoCivilStr = estadoCivil.ifBlank { "_____" }
        val profissaoStr = funcaoLaboral.ifBlank { "_____" }
        val religiaoStr = religiao.ifBlank { "_____" }
        val provTexto = proveniencia.ifBlank { "domicílio" }
        val tipoCirurgiaStr = tipoCirurgia.ifBlank { "_____" }
        val enfermariaStr = enfermaria.ifBlank { "____" }
        val leitoStr = leito.ifBlank { "____" }

        // 1. Cabecalho Identificacao
        if (isEvolucao) {
            sb.append("$nomeStr, $idadeStr anos, sexo $sexoStr (Enfermaria: $enfermariaStr, Leito: $leitoStr), admitido nesta unidade de internação cirúrgica para realização de $tipoCirurgiaStr em $dataStr às ${horaStr}h.\n\n")
        } else {
            sb.append("$nomeStr, $idadeStr anos, sexo $sexoStr, estado civil $estadoCivilStr, profissão $profissaoStr, religião $religiaoStr, veio proveniente do $provTexto, admitido nesta unidade de internação cirúrgica para realização de $tipoCirurgiaStr em $dataStr às ${horaStr}h.\n\n")
        }

        // 2. Itens Cadastrados
        val temProtese = dispositivos.contains("prótese", ignoreCase = true) || 
                         sistemaNeurologico.contains("prótese", ignoreCase = true) || 
                         sistemaLocomotor.contains("prótese", ignoreCase = true)
        val temTabagismo = historiaDoencaAtual.contains("tabac", ignoreCase = true) || 
                           historiaDoencaAtual.contains("etil", ignoreCase = true) || 
                           doencasAnteriores.contains("tabac", ignoreCase = true) || 
                           doencasAnteriores.contains("etil", ignoreCase = true)

        sb.append("- CMB: ${doencasAnteriores.ifBlank { "Não informado" }}\n")
        sb.append("- Cirurgias anteriores: ${cirurgiasAnteriores.ifBlank { "Nenhuma relatada" }}\n")
        sb.append("- Alergias: ${alergias.ifBlank { "Sem alergias conhecidas" }}\n")
        sb.append("- Próteses dentária e outras: ${if (temProtese) "Sim" else "Nenhuma relatada" }\n")
        sb.append("- Acuidade visual e auditiva: Visual: ${acuidadeVisual.ifBlank { "Normal" }} | Auditiva: ${acuidadeAuditiva.ifBlank { "Normal" }}\n")
        sb.append("- Tabagista/Etilista: ${if (temTabagismo) "Sim" else "Não informado ou Nega" }\n")
        sb.append("- Medicações em uso: ${medicacoesUso.ifBlank { "Não informado" }}\n")
        sb.append("- Episódio de queda recente: ${if (morseHistoricoQuedas == 25) "Sim" else "Não" }\n\n")

        // 3. Exame Fisico e Evolutivo Narrativo
        val neurologicoStr = sistemaNeurologico.ifBlank { "consciência, orientado, verbalizando suas necessidades" }
        val locomotorStr = locomocaoMobilidade.ifBlank { "deambulando sem auxílio" }
        val peleIntegridadeStr = sistemaIntegridadePele.ifBlank { "Pele íntegra para LP" }
        val dietaStr = sistemaNutricao.ifBlank { "Dieta VO líquida restrita, com boa aceitação" }
        val urinarioStr = sistemaUrinario.ifBlank { "Diurese espontânea" }
        val intestinalStr = sistemaIntestinal.ifBlank { "evacuações ausentes" }

        val pressaoStr = pressaoArterial.ifBlank { "120/80" }
        val tempStr = temperatura.ifBlank { "36.5" }
        val frStr = frequenciaRespiratoria.ifBlank { "18" }
        val fcStr = frequenciaCardiaca.ifBlank { "80" }
        val satStr = saturacaoO2.ifBlank { "98" }

        sb.append("Evolui ${neurologicoStr.lowercase().trim().removeSuffix(".")}, ${locomotorStr.lowercase().trim().removeSuffix(".")}, normocorado, anictérico, normotenso ($pressaoStr mmHg), afebril ($tempStr ºC), eupneico ($frStr ipm) em AA (SatO2: $satStr%).\n")
        sb.append("Pele: ${peleIntegridadeStr.trim().removeSuffix(".")}.\n")
        sb.append("Nutrição: ${dietaStr.trim().removeSuffix(".")}.\n")
        sb.append("Eliminações: ${urinarioStr.trim().removeSuffix(".")} e ${intestinalStr.trim().removeSuffix(".")}.\n")
        sb.append("Sono e repouso satisfatórios.\n\n")

        // 4. Dispositivos e Acessos Venosos
        val activeDispositivos = getDispositivosList().filter { it.selecionado }
        if (activeDispositivos.isNotEmpty()) {
            val dispLines = activeDispositivos.map { item ->
                val dateVal = item.dataPuncao.ifBlank { "____/____/____" }
                when (item.tipo) {
                    "AVP" -> "AVP em ${item.lado.ifBlank { "MS" }} ($dateVal), pérvio e sem sinais flogísticos"
                    "CVC" -> "CVC em ${item.lado.ifBlank { "sítio" }} ($dateVal), curativo ${item.curativoTipo.ifBlank { "convencional" }}, pérvio"
                    "FAV MS" -> "FAV MS em ${item.lado.ifBlank { "Membro Superior" }} ($dateVal)"
                    "CAT HD" -> "CAT HD em ${item.lado.ifBlank { "sítio" }} ($dateVal)"
                    else -> "${item.tipo} em ${item.lado.ifBlank { "sítio" }} ($dateVal)"
                }
            }
            sb.append("${dispLines.joinToString(" ou ")}.\n")
        } else {
            sb.append("AVP em MD (____/____/____), pérvio e sem sinais flogísticos.\n")
        }

        // 5. Drenos
        val activeDrenos = getDrenosList().filter { it.selecionado }
        if (activeDrenos.isNotEmpty()) {
            sb.append("- Presença de dreno: Sim\n")
            for (d in activeDrenos) {
                sb.append("- Presença de dreno tipo ${d.tipo} (Local: ${d.lado.ifBlank { "região do procedimento" }}, aspecto ${d.aspecto.ifBlank { "seroso" }} com débito de ${d.debito.ifBlank { "____" }} ml)\n")
            }
        } else {
            sb.append("- Presença de dreno: Não\n")
        }

        // 6. Ferida Operatoria (FO)
        if (lesions.isNotEmpty()) {
            val foLines = lesions.map { l ->
                val loc = l.localizacao.ifBlank { "região cirúrgica" }
                val cob = l.tipoCobertura.ifBlank { "cobertura convencional" }
                val tr = l.dataTroca.ifBlank { "conforme protocolo" }
                val janet = if (l.classificacaoJanetJensey.isNotBlank()) " Classificação: ${l.classificacaoJanetJensey}." else ""
                "FO $loc: curativo tipo $cob, próxima troca $tr, padrão: ${l.tipoTecido.ifBlank { "limpo" }}, exsudato: ${l.exsudato.ifBlank { "ausente" }}.$janet"
            }
            sb.append("FO ${foLines.joinToString("; ")}\n\n")
        } else {
            sb.append("FO: Região cirúrgica íntegra, sem ferida operatória exposta.\n\n")
        }

        // 7. Queixas e Alteracoes
        val qpVal = if (historiaDoencaAtual.isNotBlank()) historiaDoencaAtual else if (dorLocalizacao.isNotBlank()) "Dor nível $dorNivel em $dorLocalizacao ($dorCaracteristicas)" else "Estável, sem queixas álgicas no momento."
        val altVal = if (descricaoLesoes.isNotBlank()) descricaoLesoes else "Sem intercorrências ou alterações declaradas."
        sb.append("- Queixas principais: $qpVal\n")
        sb.append("- Alguma alteração: $altVal\n\n")

        // 8. Cuidados de Enfermagem, Exames, Escalas, Altura/Peso, Condutas
        sb.append("Segue em cuidado de Enfermagem\n")
        sb.append("- Exames a realizar/apresentados: ${exames.ifBlank { "Sem exames pendentes ou informados" }}\n")
        sb.append("- Dados das escalas:\n")
        sb.append("  • Escala de Braden (Risco de LPP): ${bradenScore()} pontos (${bradenClassification().uppercase()})\n")
        sb.append("  • Escala de Fugulin (Dependência): ${fugulinScore()} pontos (${fugulinClassification().uppercase()})\n")
        sb.append("  • Escala de Quedas (Morse): ${morseScore()} pontos (${morseClassification().uppercase()})\n")
        sb.append("  • Escala de Bates-Jensen (Feridas): ${bwatScore()} pontos (${bwatClassification().uppercase()})\n")
        sb.append("  • Escala de Coma de Glasgow: ${glasgowScore()} / 15 (${glasgowClassification().uppercase()})\n")
        sb.append("  • Escala de Dor (EVA): ${dorNivel} / 10 (${dorClassification().uppercase()})\n")
        sb.append("Peso e Altura: ${peso.ifBlank { "___" }} kg | ${altura.ifBlank { "___" }} m\n\n")

        val condutasTexto = if (condutas.isNotBlank()) {
            condutas
        } else if (recordType == "Encaminhamento Centro Cirúrgico") {
            "Realizado escalas de Braden, Fugulin e de Morse. Realizado banho com clorexidina degermante, esvaziamento da bexiga, retirada de adornos, peças íntimas e próteses. Anexado exames. Encaminhado paciente para o centro cirúrgico."
        } else {
            "Orientado quanto a prevenção de quedas, realizado escalas e explicado preparo pré-operatório conforme protocolo do hospital. Preenchido escalas de Braden, Fugulin e Quedas. Feito SAE diária."
        }
        sb.append("- Condutas: $condutasTexto\n")

        // 9. Extra detailed parts if available (Gemini AI interventions and Hemotherapy)
        if (aiInterventionsResult.isNotBlank()) {
            sb.append("\n================================================================================\n")
            sb.append("  DIRETRIZES TERAPÊUTICAS E INTERVENÇÕES DA ASSISTÊNCIA (IA - GEMINI)\n")
            sb.append("================================================================================\n")
            sb.append(aiInterventionsResult).append("\n")
        }

        val selectedHemocomponentes = getHemocomponentesList().filter { it.selecionado }
        if (selectedHemocomponentes.isNotEmpty()) {
            sb.append("\n================================================================================\n")
            sb.append("                  REGISTRO DE HEMOCONCENTRADOS / HEMOTERAPIA\n")
            sb.append("================================================================================\n")
            for (hc in selectedHemocomponentes) {
                sb.append("• ${hc.tipo} (${getHemocomponenteFullName(hc.tipo)}):\n")
                sb.append("  - Data da Hemotransfusão: ${hc.diaHemotransfusao.ifBlank { "Não informada" }}\n")
                sb.append("  - Nº da Bolsa: ${hc.numeroBolsa.ifBlank { "Não informado" }} | Validade: ${hc.validade.ifBlank { "Não informada" }}\n")
                sb.append("  - Tipagem Sanguínea: ${hc.tipagemSanguinea.ifBlank { "Não informada" }} | Volume Infundido: ${hc.volumeInfundido.ifBlank { "Não informado" }}\n")
                sb.append("  - Reação Transfusional: ${hc.reacaoTransfusional}${if (hc.reacaoTransfusional == "Sim" && hc.reacaoQuais.isNotBlank()) " (${hc.reacaoQuais})" else ""}\n")
                sb.append("  - Sinais Vitais no Início: PA: ${hc.inicioPA.ifBlank { "N/I" }} | FC: ${hc.inicioFC.ifBlank { "N/I" }} | FR: ${hc.inicioFR.ifBlank { "N/I" }} | T: ${hc.inicioT.ifBlank { "N/I" }} | SatO2: ${hc.inicioSatO2.ifBlank { "N/I" }}\n")
                sb.append("  - Sinais Vitais no Final: PA: ${hc.fimPA.ifBlank { "N/I" }} | FC: ${hc.fimFC.ifBlank { "N/I" }} | FR: ${hc.fimFR.ifBlank { "N/I" }} | T: ${hc.fimT.ifBlank { "N/I" }} | SatO2: ${hc.fimSatO2.ifBlank { "N/I" }}\n")
                sb.append("\n")
            }
        }

        sb.append("\n--------------------------------------------------------------------------------\n")
        if (isEvolucao) {
            sb.append("Relatório de Evolução de Enfermagem padronizado e gerado eletronicamente em: $dataFormatada\n")
        } else {
            sb.append("Relatório de Enfermagem padronizado e gerado eletronicamente em: $dataFormatada\n")
        }
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
    val fotoUri: String? = null,
    val descricaoAudio: String = "",
    val tipoCobertura: String = "",
    val dataTroca: String = "",
    val classificacaoJanetJensey: String = ""
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

data class HemocomponenteItem(
    val tipo: String, // "CH", "CP", "PFC", "CRIO", "CG"
    val selecionado: Boolean = false,
    val diaHemotransfusao: String = "",
    val numeroBolsa: String = "",
    val validade: String = "",
    val tipagemSanguinea: String = "",
    val volumeInfundido: String = "",
    val reacaoTransfusional: String = "Não", // "Sim" or "Não"
    val reacaoQuais: String = "",
    // Vital signs at start
    val inicioPA: String = "",
    val inicioFC: String = "",
    val inicioFR: String = "",
    val inicioT: String = "",
    val inicioSatO2: String = "",
    // Vital signs at end
    val fimPA: String = "",
    val fimFC: String = "",
    val fimFR: String = "",
    val fimT: String = "",
    val fimSatO2: String = ""
)

fun getHemocomponenteFullName(tipo: String): String {
    return when (tipo) {
        "CH" -> "Concentrado de Hemácias"
        "CP" -> "Concentrado de Plaquetas"
        "PFC" -> "Plasma Fresco Congelado"
        "CRIO" -> "Crioprecipitado"
        "CG" -> "Concentrado de Granulócitos"
        else -> tipo
    }
}

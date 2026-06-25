package com.example.obsidian.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Motor de juego para "La Carga del Silencio".
 * Contiene funciones puras para procesar las acciones del detective y devolver
 * el estado de juego actualizado sin mutaciones secundarias.
 */
class GameEngine(val state: GameState = GameState()) {

    // Funciones de conveniencia que delegan en el companion object
    fun exploreLocation(locationId: String): GameState = Companion.exploreLocation(state, locationId)
    fun askQuestion(suspectId: String, questionId: String): GameState = Companion.askQuestion(state, suspectId, questionId)
    fun endInterrogation(suspectId: String): GameState = Companion.endInterrogation(state, suspectId)
    fun linkClueToSuspect(clueId: String, suspectId: String): GameState = Companion.linkClueToSuspect(state, clueId, suspectId)
    fun makeAccusation(suspectId: String, clueId: String): DeductionResult = Companion.makeAccusation(state, suspectId, clueId)
    fun submitAccusation(suspectId: String, clueId: String): GameState = Companion.submitAccusation(state, suspectId, clueId)
    fun applyMinigameResult(locationName: String, result: String): GameState = Companion.applyMinigameResult(state, locationName, result)
    fun evaluatePhaseTransition(): GameState = Companion.evaluatePhaseTransition(state)
    fun navigateToPhase(phase: GamePhase): GameState = Companion.navigateToPhase(state, phase)

    companion object {

        private fun getCurrentTime(): String {
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            return sdf.format(Date())
        }

        /**
         * Explora una ubicación específica.
         */
        fun exploreLocation(state: GameState, locationId: String): GameState {
            val case = state.currentCase ?: return state

            // Buscamos si hay pistas disponibles y no recolectadas en la ubicación/ID proporcionado
            val matchingClues = case.clues.filter { clue ->
                (clue.locationName.equals(locationId, ignoreCase = true) || clue.id == locationId) &&
                        clue.isAvailable && !clue.isFound
            }

            // El explorationCount solo incrementa al encontrar una pista nueva, no al visitar una ubicación sin pista
            if (matchingClues.isEmpty()) {
                return state
            }

            // Marcamos las pistas correspondientes como encontradas
            val updatedClues = case.clues.map { clue ->
                if (matchingClues.any { it.id == clue.id }) {
                    clue.copy(isFound = true)
                } else {
                    clue
                }
            }

            val newDiscoveredClues = (state.discoveredClues + matchingClues.map { it.copy(isFound = true) })
                .distinctBy { it.id }

            val newExplorationCount = state.explorationCount + 1

            val updatedState = state.copy(
                currentCase = case.copy(clues = updatedClues),
                discoveredClues = newDiscoveredClues,
                explorationCount = newExplorationCount,
                score = state.score + (matchingClues.size * 150),
                progress = state.progress.copy(
                    discoveredClues = newDiscoveredClues.size,
                    solvedMinigames = state.progress.solvedMinigames + matchingClues.size,
                    explorationCount = newExplorationCount
                )
            )

            return evaluatePhaseTransition(evaluateChainUnlocks(updatedState))
        }

        /**
         * Realiza una pregunta a un sospechoso.
         */
        fun askQuestion(state: GameState, suspectId: String, questionId: String): GameState {
            val case = state.currentCase ?: return state
            val suspect = case.suspects.find { it.id == suspectId } ?: return state
            val question = suspect.availableQuestions.find { it.id == questionId } ?: return state

            // Bloquear nuevas preguntas si el trustLevel llega a 0 o menos en esa sesión
            if (suspect.trustLevel <= 0) {
                val currentMsgs = state.messages[suspectId] ?: emptyList()
                val systemMsg = InterrogationMessage(
                    sender = "SISTEMA",
                    text = "${suspect.name} se niega a cooperar.",
                    isDetective = false,
                    time = getCurrentTime()
                )
                return state.copy(messages = state.messages + (suspectId to (currentMsgs + systemMsg)))
            }

            var updatedSuspect = suspect
            var pointsToAdd = 0
            var contradictionFound = false

            // Lógica de efecto condicional de la pregunta de Valentina sobre Logística del Caribe
            if (suspectId == "suspect_valentina" && questionId == "q_valentina_4") {
                val carlosInterrogated = state.interrogatedSuspects.contains("suspect_carlos")
                if (!carlosInterrogated) {
                    val change = -15
                    val newTrust = (suspect.trustLevel + change).coerceIn(0, 100)
                    updatedSuspect = suspect.copy(trustLevel = newTrust)
                } else {
                    pointsToAdd += 50
                }
            } else {
                when (question.effectType) {
                    "TRUST" -> {
                        val change = question.effectValue.toIntOrNull() ?: 0
                        val newTrust = (suspect.trustLevel + change).coerceIn(0, 100)
                        updatedSuspect = suspect.copy(trustLevel = newTrust)
                    }
                    "CONTRADICTION" -> {
                        updatedSuspect = suspect.copy(
                            contradictions = (suspect.contradictions + question.effectValue).distinct()
                        )
                        pointsToAdd += 200
                        contradictionFound = true
                    }
                    "UNLOCK_CLUE" -> pointsToAdd += 50
                }
            }

            val updatedSuspects = case.suspects.map { if (it.id == suspectId) updatedSuspect else it }
            val currentMsgs = state.messages[suspectId] ?: emptyList()
            val detMsg = InterrogationMessage("INVESTIGADOR", question.text, true, getCurrentTime())
            val replyText = getSuspectStaticReply(state, suspectId, questionId)
            val suspectMsg = InterrogationMessage(
                sender = suspect.name,
                text = replyText,
                isDetective = false,
                time = getCurrentTime(),
                isContradiction = question.effectType == "CONTRADICTION"
            )

            val updatedMessages = state.messages + (suspectId to (currentMsgs + detMsg + suspectMsg))
            val newInterrogated = (state.interrogatedSuspects + suspectId).distinct()

            val nextState = state.copy(
                currentCase = case.copy(suspects = updatedSuspects),
                messages = updatedMessages,
                interrogatedSuspects = newInterrogated,
                score = state.score + pointsToAdd,
                progress = state.progress.copy(
                    interrogationsCompleted = newInterrogated.size,
                    contradictionsFound = state.progress.contradictionsFound + (if (contradictionFound) 1 else 0)
                )
            )

            return evaluatePhaseTransition(evaluateChainUnlocks(nextState))
        }

        private fun getSuspectStaticReply(state: GameState, suspectId: String, questionId: String): String {
            return when (suspectId) {
                "suspect_carlos" -> when (questionId) {
                    "q_carlos_1" -> "Yo... firmo muchos documentos al día, detective. No recuerdo cada uno de ellos."
                    "q_carlos_2" -> "¿Logística del Caribe? No, no me suena... Debe ser una de las tantas empresas satélite del sector."
                    "q_carlos_3" -> "Estaba terminando de inventariar el almacén principal. Me quedé allí hasta tarde solo."
                    "q_carlos_4" -> "No teníamos mucha relación fuera del trabajo. Pero a veces nos reuníamos a hablar del negocio en su residencia."
                    "q_carlos_5" -> "¿A la hija de Don Aurelio? No, solo de vista... Nunca he tenido trato con ella."
                    "q_carlos_6" -> "¿Borrador en la basura? ¡Eso es ridículo! Cualquiera pudo haber falsificado mi letra o tirado papeles viejos. No prueba nada."
                    else -> "No tengo nada que declarar sobre eso."
                }
                "suspect_valentina" -> when (questionId) {
                    "q_valentina_1" -> "Son transacciones internas rutinarias de la empresa. Todo está en regla, aunque no puedo darle detalles sin revisar los libros."
                    "q_valentina_2" -> "Yo no fui. En la oficina de finanzas hay mucho movimiento, cualquiera pudo haber usado mi computadora si me descuidé."
                    "q_valentina_3" -> "Un inversionista... bueno, en realidad era un consultor independiente. No recuerdo su nombre ahora mismo."
                    "q_valentina_4" -> {
                        if (state.interrogatedSuspects.contains("suspect_carlos")) {
                            "Sí, es una firma con la que Carlos solía hacer transacciones..."
                        } else {
                            "No tengo autorización para hablar de cuentas de terceros."
                        }
                    }
                    "q_valentina_5" -> "¿Un abogado? No tenía idea... Don Aurelio no me comentó nada al respecto. ¿Pasaba algo malo?"
                    "q_valentina_6" -> "¿E-Esa cuenta...? (se pone pálida) No... yo no firmé ese descargo. Debe ser un error administrativo. Yo no sabía que era offshore."
                    else -> "No tengo nada que declarar sobre eso."
                }
                "suspect_tomas" -> when (questionId) {
                    "q_tomas_1" -> "El sistema aduanero falló esa tarde. Tuvimos un apagón de red y tuvimos que procesar todo manualmente. No es mi culpa."
                    "q_tomas_2" -> "No, no lo conozco. He tratado con cientos de jefes de logística, pero no tengo ninguna relación personal con ese señor."
                    "q_tomas_3" -> "¿De qué depósitos habla? Mis finanzas personales son privadas. ¡Esto es un atropello, no toleraré estas acusaciones sin pruebas!"
                    "q_tomas_4" -> "Ese video... no muestra todo el contexto. Está bien, yo... yo solo seguía instrucciones de arriba."
                    "q_tomas_5" -> "¡Le aseguro que nadie me dijo que ignorara ESE contenedor! ... Quiero decir, ningún contenedor."
                    else -> "No tengo nada que declarar sobre eso."
                }
                "suspect_marisol" -> when (questionId) {
                    "q_marisol_1" -> "Sí, mi padre estaba muy preocupado últimamente. Sentía que alguien lo estaba traicionando desde adentro."
                    "q_marisol_2" -> "No, solo sé que trabaja para mi padre. Nunca he salido con él ni tenemos ninguna relación."
                    "q_marisol_3" -> "Es lo que cualquier persona responsable haría tras la muerte de su padre para proteger el patrimonio familiar. ¿Tiene algún problema con eso?"
                    "q_marisol_4" -> "Sí... me dijo que sospechaba de Carlos y guardaba todo anotado en su agenda. Debería buscarla en nuestra residencia."
                    "q_marisol_5" -> "Yo... no sé de qué habla. Es solo... un término contable que Carlos me mencionó... (guarda silencio)."
                    else -> "No tengo nada que declarar sobre eso."
                }
                else -> "Solo quiero que se sepa la verdad."
            }
        }

        fun endInterrogation(state: GameState, suspectId: String): GameState {
            val newInterrogated = (state.interrogatedSuspects + suspectId).distinct()
            val nextState = state.copy(
                interrogatedSuspects = newInterrogated,
                progress = state.progress.copy(
                    interrogationsCompleted = newInterrogated.size
                )
            )
            return evaluatePhaseTransition(evaluateChainUnlocks(nextState))
        }

        fun linkClueToSuspect(state: GameState, clueId: String, suspectId: String): GameState {
            val updatedAssignments = state.clueAssignments + (clueId to suspectId)
            val nextState = state.copy(clueAssignments = updatedAssignments)
            return evaluatePhaseTransition(evaluateChainUnlocks(nextState))
        }

        fun makeAccusation(state: GameState, suspectId: String, clueId: String): DeductionResult {
            val case = state.currentCase ?: return DeductionResult(false, "No hay ningún caso cargado.")
            val isGuiltyCorrect = suspectId == case.solution.guiltySuspectId
            val isClueCorrect = clueId == case.solution.keyEvidenceId

            return when {
                isGuiltyCorrect && isClueCorrect -> DeductionResult(true, "¡Correcto! Las piezas encajan perfectamente.", suspectId)
                isGuiltyCorrect -> DeductionResult(false, "Sospechoso correcto, pero la prueba reina no es suficiente.", suspectId)
                else -> DeductionResult(false, "Acusación errónea. Las pruebas no incriminan a este sospechoso.", suspectId)
            }
        }

        fun submitAccusation(state: GameState, suspectId: String, clueId: String): GameState {
            val case = state.currentCase ?: return state
            val isCorrect = suspectId == case.solution.guiltySuspectId && clueId == case.solution.keyEvidenceId
            val guilty = case.suspects.find { it.id == case.solution.guiltySuspectId }
            val accused = case.suspects.find { it.id == suspectId }

            var correctAssignments = 0
            state.clueAssignments.forEach { (cId, sId) ->
                val clue = case.clues.find { it.id == cId }
                if (clue?.linkedSuspects?.contains(sId) == true) correctAssignments++
            }

            val rank = when {
                !isCorrect -> "CASO CERRADO EN FRÍO"
                correctAssignments >= 5 -> "DETECTIVE EXPERTO"
                correctAssignments >= 3 -> "DETECTIVE COMPETENTE"
                else -> "DETECTIVE NOVATO"
            }

            val missingClues = case.clues.filter { !it.isFound }
            val missingText = if (missingClues.isNotEmpty()) {
                " Pistas no encontradas: ${missingClues.joinToString { it.title }}."
            } else {
                ""
            }

            val epilogue = if (isCorrect) {
                val letterDetail = if (state.hasSafeSecretLetter) {
                    " La carta secreta firmada por Don Aurelio entregada por Oscar Ríos selló su destino definitivamente, sirviendo como prueba formal incontestable en el juicio."
                } else {
                    " Desafortunadamente, la carta de advertencia manuscrita por Don Aurelio nunca fue recuperada de su caja fuerte, por lo que la fiscalía tuvo dificultades para demostrar premeditación total en el juicio."
                }
                "¡CASO RESUELTO CON ÉXITO! ${accused?.name} fue detenido. Confesó todo ante el peso de las evidencias. El puerto vuelve a estar en paz.$letterDetail$missingText"
            } else {
                "FALLO EN LA INVESTIGACIÓN. ${accused?.name} fue liberado por falta de pruebas. El verdadero culpable, ${guilty?.name}, logró escapar del país.$missingText"
            }

            val result = GameResult(
                isCorrect = isCorrect,
                accusedSuspectName = accused?.name ?: "Desconocido",
                actualGuiltyName = guilty?.name ?: "Desconocido",
                epilogue = epilogue,
                correctClueAssignments = correctAssignments,
                totalClues = case.clues.size,
                rank = rank,
                foundContradictions = state.progress.contradictionsFound,
                interrogatedCount = state.interrogatedSuspects.size,
                explorationCount = state.explorationCount,
                missingHints = missingClues.map { it.title }
            )

            return state.copy(phase = GamePhase.VERDICT, gameResult = result)
        }

        fun evaluatePhaseTransition(state: GameState): GameState {
            val nextPhase = when (state.phase) {
                GamePhase.INTRO -> state.phase
                GamePhase.EXPLORE_SCENE -> if (state.explorationCount >= 1) GamePhase.INTERROGATION else state.phase
                GamePhase.INTERROGATION -> if (state.interrogatedSuspects.size >= 2) GamePhase.ANALYZE_EVIDENCE else state.phase
                GamePhase.ANALYZE_EVIDENCE -> if (state.clueAssignments.size >= 3) GamePhase.ACCUSATION else state.phase
                else -> state.phase
            }
            return state.copy(phase = nextPhase)
        }

        fun navigateToPhase(state: GameState, phase: GamePhase): GameState {
            // Se restringe la navegación manual si no se cumplen las condiciones lógicas mínimas
            when (phase) {
                GamePhase.ANALYZE_EVIDENCE -> {
                    if (state.interrogatedSuspects.size < 2) return state
                }
                GamePhase.ACCUSATION -> {
                    if (state.clueAssignments.size < 3) return state
                }
                else -> {}
            }
            return state.copy(phase = phase)
        }
        fun evaluateChainUnlocks(state: GameState): GameState {
            val case = state.currentCase ?: return state
            val updatedClues = case.clues.map { clue ->
                val available = when (clue.id) {
                    "clue_usb" -> state.explorationCount >= 3
                    "clue_contrato" -> state.clueAssignments.containsKey("clue_agenda")
                    else -> clue.isAvailable
                }
                clue.copy(isAvailable = available)
            }
            return state.copy(currentCase = case.copy(clues = updatedClues))
        }

        fun applyMinigameResult(state: GameState, locationName: String, result: String): GameState {
            val case = state.currentCase ?: return state
            val updatedMinigames = state.minigameResults + (locationName to result)
            var updatedState = state.copy(minigameResults = updatedMinigames)

            when (locationName) {
                "Almacén del Puerto" -> {
                    if (result == "WON") {
                        val updatedClues = case.clues.map {
                            if (it.id == "clue_manifiesto") it.copy(isFound = true, isAvailable = true) else it
                        }
                        val extra = "El número de serie del contenedor aparece en facturas de otras 3 empresas distintas en los últimos 6 meses."
                        val newDiscovered = (updatedState.discoveredClues + case.clues.first { it.id == "clue_manifiesto" }.copy(isFound = true, isAvailable = true)).distinctBy { it.id }
                        updatedState = updatedState.copy(
                            currentCase = case.copy(clues = updatedClues),
                            discoveredClues = newDiscovered,
                            extraInfoFound = (updatedState.extraInfoFound + extra).distinct(),
                            score = updatedState.score + 300
                        )
                    } else {
                        val unlockAt = updatedState.explorationCount + 2
                        val updatedBlocked = updatedState.blockedLocations + (locationName to unlockAt)
                        val newExploration = updatedState.explorationCount + 1
                        updatedState = updatedState.copy(
                            blockedLocations = updatedBlocked,
                            explorationCount = newExploration,
                            progress = updatedState.progress.copy(explorationCount = newExploration)
                        )
                    }
                }
                "Oficina de Importaciones Atlántico" -> {
                    if (result == "WON") {
                        val extra = "Borrador de instrucciones de Don Aurelio en la basura: indica declarar peso doble para cubrir faltantes."
                        val updatedSuspects = case.suspects.map { suspect ->
                            if (suspect.id == "suspect_carlos") {
                                val exists = suspect.availableQuestions.any { it.id == "q_carlos_6" }
                                if (!exists) {
                                    val extraQ = Question(
                                        id = "q_carlos_6",
                                        text = "Encontramos un borrador con su letra en la basura.",
                                        effectType = "CONTRADICTION",
                                        effectValue = "el borrador prueba que dio la orden de falsificar pesos (dice que no lo escribió)"
                                    )
                                    suspect.copy(availableQuestions = suspect.availableQuestions + extraQ)
                                } else suspect
                            } else suspect
                        }
                        updatedState = updatedState.copy(
                            currentCase = case.copy(suspects = updatedSuspects),
                            extraInfoFound = (updatedState.extraInfoFound + extra).distinct(),
                            score = updatedState.score + 300
                        )
                    } else {
                        val newExploration = updatedState.explorationCount + 1
                        updatedState = updatedState.copy(
                            explorationCount = newExploration,
                            progress = updatedState.progress.copy(explorationCount = newExploration)
                        )
                    }
                }
                "Caseta de Aduanas" -> {
                    if (result == "WON") {
                        val updatedClues = case.clues.map {
                            if (it.id == "clue_camara") it.copy(isAvailable = true) else it
                        }
                        val extra = "Tomás firmó 11 contenedores esa noche, excepto el 7-BETA, y cerró sesión 4 minutos después del despacho."
                        updatedState = updatedState.copy(
                            currentCase = case.copy(clues = updatedClues),
                            extraInfoFound = (updatedState.extraInfoFound + extra).distinct(),
                            score = updatedState.score + 300
                        )
                    } else {
                        val updatedSuspects = case.suspects.map { suspect ->
                            if (suspect.id == "suspect_tomas") {
                                suspect.copy(trustLevel = (suspect.trustLevel + 10).coerceIn(0, 100))
                            } else suspect
                        }
                        updatedState = updatedState.copy(
                            currentCase = case.copy(suspects = updatedSuspects)
                        )
                    }
                }
                "Residencia Mendoza" -> {
                    if (result == "WON") {
                        val updatedClues = case.clues.map {
                            if (it.id == "clue_agenda") it.copy(isFound = true, isAvailable = true, description = "Agenda descifrada: Don Aurelio sospechaba de Carlos y su esquema con Logística del Caribe y Tomás. Menciona al abogado OR (Oscar Ríos).") else it
                        }
                        val extra = "El abogado Oscar Ríos (OR) confirma que Don Aurelio lo llamó esa noche con temor, acordando reunirse al día siguiente."
                        val newDiscovered = (updatedState.discoveredClues + case.clues.first { it.id == "clue_agenda" }.copy(isFound = true, isAvailable = true, description = "Agenda descifrada: Don Aurelio sospechaba de Carlos y su esquema con Logística del Caribe y Tomás. Menciona al abogado OR (Oscar Ríos).")).distinctBy { it.id }
                        updatedState = updatedState.copy(
                            currentCase = case.copy(clues = updatedClues),
                            discoveredClues = newDiscovered,
                            extraInfoFound = (updatedState.extraInfoFound + extra).distinct(),
                            score = updatedState.score + 300
                        )
                    } else if (result == "PARTIAL") {
                        val updatedClues = case.clues.map {
                            if (it.id == "clue_agenda") it.copy(isFound = true, isAvailable = true, description = "Agenda parcialmente descifrada: Notas confusas sobre LdC, TG y CH.") else it
                        }
                        val newDiscovered = (updatedState.discoveredClues + case.clues.first { it.id == "clue_agenda" }.copy(isFound = true, isAvailable = true, description = "Agenda parcialmente descifrada: Notas confusas sobre LdC, TG y CH.")).distinctBy { it.id }
                        updatedState = updatedState.copy(
                            currentCase = case.copy(clues = updatedClues),
                            discoveredClues = newDiscovered,
                            score = updatedState.score + 150
                        )
                    } else {
                        val newExploration = updatedState.explorationCount + 1
                        updatedState = updatedState.copy(
                            explorationCount = newExploration,
                            progress = updatedState.progress.copy(explorationCount = newExploration)
                        )
                    }
                }
                "Fiscalía" -> {
                    if (result == "WON") {
                        val updatedClues = case.clues.map {
                            if (it.id == "clue_bancos") it.copy(isFound = true, isAvailable = true, description = "Registros bancarios completos: Muestran transacciones periódicas de dinero ilícito desde Logística del Caribe SAS a las cuentas de Carlos y Tomás. Flujo de $287,000 en 8 meses.") else it
                        }
                        val extra = "Se detectó una transferencia de $40,000 hacia una cuenta anónima no identificada (posible implicación de Marisol)."
                        val newDiscovered = (updatedState.discoveredClues + case.clues.first { it.id == "clue_bancos" }.copy(isFound = true, isAvailable = true, description = "Registros bancarios completos: Muestran transacciones periódicas de dinero ilícito desde Logística del Caribe SAS a las cuentas de Carlos y Tomás. Flujo de $287,000 en 8 meses.")).distinctBy { it.id }
                        
                        val updatedSuspects = case.suspects.map { suspect ->
                            if (suspect.id == "suspect_valentina") {
                                val exists = suspect.availableQuestions.any { it.id == "q_valentina_6" }
                                if (!exists) {
                                    val extraQ = Question(
                                        id = "q_valentina_6",
                                        text = "¿Reconoce esta cuenta offshore?",
                                        effectType = "CONTRADICTION",
                                        effectValue = "la cuenta offshore de Panamá tiene transferencias que usted misma autorizó financieramente"
                                    )
                                    suspect.copy(availableQuestions = suspect.availableQuestions + extraQ)
                                } else suspect
                            } else suspect
                        }

                        updatedState = updatedState.copy(
                            currentCase = case.copy(clues = updatedClues, suspects = updatedSuspects),
                            discoveredClues = newDiscovered,
                            extraInfoFound = (updatedState.extraInfoFound + extra).distinct(),
                            score = updatedState.score + 300
                        )
                    } else {
                        val updatedClues = case.clues.map {
                            if (it.id == "clue_bancos") it.copy(isFound = true, isAvailable = true, description = "Registros bancarios parciales: Muestran solo los depósitos a Tomás, sin la cadena completa de flujo de dinero.") else it
                        }
                        val newDiscovered = (updatedState.discoveredClues + case.clues.first { it.id == "clue_bancos" }.copy(isFound = true, isAvailable = true, description = "Registros bancarios parciales: Muestran solo los depósitos a Tomás, sin la cadena completa de flujo de dinero.")).distinctBy { it.id }
                        updatedState = updatedState.copy(
                            currentCase = case.copy(clues = updatedClues),
                            discoveredClues = newDiscovered
                        )
                    }
                }
                "Estudio de Abogados" -> {
                    if (result == "WON") {
                        val updatedClues = case.clues.map {
                            if (it.id == "clue_contrato") it.copy(isFound = true, isAvailable = true, description = "Contrato de ganancias: Documento privado que pacta el 20% para Marisol y contiene una carta adjunta de Don Aurelio delegando responsabilidad interna a Carlos.") else it
                        }
                        val extra = "Carta adjunta de Don Aurelio: 'si algo me ocurre, el culpable está adentro y es de total confianza en la empresa'."
                        val newDiscovered = (updatedState.discoveredClues + case.clues.first { it.id == "clue_contrato" }.copy(isFound = true, isAvailable = true, description = "Contrato de ganancias: Documento privado que pacta el 20% para Marisol y contiene una carta adjunta de Don Aurelio delegando responsabilidad interna a Carlos.")).distinctBy { it.id }
                        updatedState = updatedState.copy(
                            currentCase = case.copy(clues = updatedClues),
                            discoveredClues = newDiscovered,
                            hasSafeSecretLetter = true,
                            extraInfoFound = (updatedState.extraInfoFound + extra).distinct(),
                            score = updatedState.score + 300
                        )
                    } else {
                        val updatedClues = case.clues.map {
                            if (it.id == "clue_contrato") it.copy(isAvailable = true, description = "Contrato de ganancias ordinario sin carta adjunta.") else it
                        }
                        updatedState = updatedState.copy(
                            currentCase = case.copy(clues = updatedClues),
                            hasSafeSecretLetter = false
                        )
                    }
                }
            }

            return evaluatePhaseTransition(evaluateChainUnlocks(updatedState))
        }
    }
}

package com.example.obsidian.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

/**
 * Motor de juego para "La Carga del Silencio".
 * Contiene funciones puras para procesar las acciones del detective y devolver
 * el estado de juego actualizado sin mutaciones secundarias.
 */
class GameEngine(val state: GameState = GameState()) {

    // Funciones de conveniencia que delegan en el companion object
    fun exploreLocation(locationId: String): GameState = Companion.exploreLocation(state, locationId)
    fun askQuestion(suspectId: String, questionId: String): GameState = Companion.askQuestion(state, suspectId, questionId)
    fun startInterrogation(suspectId: String): GameState = Companion.startInterrogation(state, suspectId)
    fun tickInterrogationTime(suspectId: String): GameState = Companion.tickInterrogationTime(state, suspectId)
    fun handleTimeExpired(suspectId: String): GameState = Companion.handleTimeExpired(state, suspectId)
    fun getQuestionsForRound(suspectId: String): List<Question> = Companion.getQuestionsForRound(state, suspectId)
    fun endInterrogation(suspectId: String): GameState = Companion.endInterrogation(state, suspectId)
    fun linkClueToSuspect(clueId: String, suspectId: String): GameState = Companion.linkClueToSuspect(state, clueId, suspectId)
    fun makeAccusation(suspectId: String, clueId: String): DeductionResult = Companion.makeAccusation(state, suspectId, clueId)
    fun submitAccusation(suspectId: String, clueId: String): GameState = Companion.submitAccusation(state, suspectId, clueId)
    fun applyMinigameResult(locationName: String, result: String): GameState = Companion.applyMinigameResult(state, locationName, result)
    fun evaluatePhaseTransition(): GameState = Companion.evaluatePhaseTransition(state)
    fun navigateToPhase(phase: GamePhase): GameState = Companion.navigateToPhase(state, phase)

    companion object {

        private const val INTERROGATION_TIME_SECONDS = 90
        private const val MAX_QUESTIONS_PER_ROUND = 3

        /** Override para tests: devuelve valor 0..1; si null usa Random. */
        var randomFloatForTest: (() -> Float)? = null

        private fun nextRandomFloat(): Float = randomFloatForTest?.invoke() ?: Random.nextFloat()

        private val SUSPECT_TOPICS = mapOf(
            "suspect_carlos" to listOf("logística portuaria", "su coartada", "relaciones personales", "operaciones financieras"),
            "suspect_valentina" to listOf("contabilidad", "transacciones sospechosas", "su coartada", "conexiones empresariales"),
            "suspect_tomas" to listOf("procedimientos aduaneros", "inspecciones", "relaciones con logística", "movimientos bancarios"),
            "suspect_marisol" to listOf("su padre", "relaciones con empleados", "herencia y patrimonio", "acuerdos privados")
        )

        private val TOPIC_CHANGE_MESSAGES = listOf(
            "Prefiero no hablar de eso. Cambiemos de tema.",
            "Ya le dije lo que sé sobre eso. Pregunte otra cosa.",
            "*(Se incomoda)* Eso no tiene nada que ver con el caso.",
            "No voy a seguir hablando de eso. Siguiente pregunta."
        )

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
         * Calcula la probabilidad de revelar información según compatibilidad de personalidad y enfoque.
         */
        fun calculateRevealProbability(question: Question, state: GameState, suspectId: String): Float {
            val personalityBonus = personalityApproachBonus(suspectId, question.approach)
            var coherence = 40 + personalityBonus
            val discoveredIds = state.discoveredClues.map { it.id }.toSet()
            val evidenceBoost = when (question.id) {
                "q_carlos_1" -> if ("clue_manifiesto" in discoveredIds) 15 else 0
                "q_carlos_2" -> if ("clue_bancos" in discoveredIds) 10 else 0
                "q_carlos_3" -> if ("clue_camara" in discoveredIds) 15 else 0
                "q_tomas_2" -> if ("clue_usb" in discoveredIds || "clue_bancos" in discoveredIds) 15 else 0
                "q_tomas_4" -> if ("clue_camara" in discoveredIds) 20 else 0
                "q_valentina_4" -> if (state.interrogatedSuspects.contains("suspect_carlos")) 20 else -15
                "q_marisol_2" -> if ("clue_bancos" in discoveredIds) 10 else 0
                else -> 0
            }
            coherence = (coherence + evidenceBoost).coerceIn(0, 100)
            return when {
                coherence >= 80 -> 0.95f
                coherence >= 60 -> 0.65f
                coherence >= 40 -> 0.30f
                else -> 0.08f
            }
        }

        fun isApproachCompatibleWithPersonality(suspectId: String, approach: String): Boolean {
            return personalityApproachBonus(suspectId, approach) >= 20
        }

        private fun personalityApproachBonus(suspectId: String, approach: String): Int {
            return when (suspectId) {
                // Nervioso y evasivo: se abre con calma y detalle técnico; la presión lo cierra
                "suspect_carlos" -> when (approach) {
                    "EMPÁTICO" -> 45
                    "TÉCNICO" -> 25
                    "DIRECTO" -> -10
                    "PRESIÓN" -> -45
                    else -> 0
                }
                // Fría y calculadora: respeta hechos y precisión; rechaza emoción y gritos
                "suspect_valentina" -> when (approach) {
                    "TÉCNICO" -> 45
                    "DIRECTO" -> 25
                    "NEUTRAL" -> 5
                    "EMPÁTICO" -> -25
                    "PRESIÓN" -> -45
                    else -> 0
                }
                // Autoritario e intimidante: respeta franqueza entre pares; desprecia debilidad y desafíos
                "suspect_tomas" -> when (approach) {
                    "DIRECTO" -> 40
                    "TÉCNICO" -> 30
                    "EMPÁTICO" -> -30
                    "PRESIÓN" -> -40
                    else -> 0
                }
                // Afligida y temperamental: necesita contención emocional; la agresión la hunde
                "suspect_marisol" -> when (approach) {
                    "EMPÁTICO" -> 45
                    "DIRECTO" -> 5
                    "TÉCNICO" -> -20
                    "PRESIÓN" -> -45
                    else -> 0
                }
                else -> 0
            }
        }

        fun startInterrogation(state: GameState, suspectId: String): GameState {
            val case = state.currentCase ?: return state
            val suspect = case.suspects.find { it.id == suspectId } ?: return state
            if (state.interrogatedSuspects.contains(suspectId)) return state

            val session = InterrogationSession(
                suspectId = suspectId,
                timeRemainingSeconds = INTERROGATION_TIME_SECONDS,
                isActive = true
            )
            val currentMsgs = state.messages[suspectId] ?: emptyList()
            val startMsg = InterrogationMessage(
                sender = "SISTEMA",
                text = "INTERROGATORIO INICIADO — Tienes ${INTERROGATION_TIME_SECONDS}s por ronda. Máximo $MAX_QUESTIONS_PER_ROUND preguntas por diálogo.",
                isDetective = false,
                time = getCurrentTime()
            )
            return state.copy(
                interrogationSessions = state.interrogationSessions + (suspectId to session),
                messages = state.messages + (suspectId to (currentMsgs + startMsg))
            )
        }

        fun tickInterrogationTime(state: GameState, suspectId: String): GameState {
            val session = state.interrogationSessions[suspectId] ?: return state
            if (!session.isActive) return state
            if (session.timeRemainingSeconds <= 0) return handleTimeExpired(state, suspectId)

            val updatedSession = session.copy(timeRemainingSeconds = session.timeRemainingSeconds - 1)
            return state.copy(
                interrogationSessions = state.interrogationSessions + (suspectId to updatedSession)
            )
        }

        fun handleTimeExpired(state: GameState, suspectId: String): GameState {
            val case = state.currentCase ?: return state
            val suspect = case.suspects.find { it.id == suspectId } ?: return state
            val session = state.interrogationSessions[suspectId] ?: return state
            if (!session.isActive) return state

            val topics = SUSPECT_TOPICS[suspectId] ?: listOf("otro tema")
            val nextTopicIndex = (session.currentTopicIndex + 1) % topics.size
            val newTopic = topics[nextTopicIndex]
            val changeMsg = TOPIC_CHANGE_MESSAGES[session.currentTopicIndex % TOPIC_CHANGE_MESSAGES.size]

            val currentMsgs = state.messages[suspectId] ?: emptyList()
            val topicMsg = InterrogationMessage(
                sender = suspect.name,
                text = "$changeMsg *(Cambia el tema a: $newTopic)*",
                isDetective = false,
                time = getCurrentTime()
            )
            val systemMsg = InterrogationMessage(
                sender = "SISTEMA",
                text = "⏱ TIEMPO AGOTADO — El sospechoso cambió de tema. Nueva ronda: ${INTERROGATION_TIME_SECONDS}s.",
                isDetective = false,
                time = getCurrentTime()
            )

            val updatedSession = session.copy(
                timeRemainingSeconds = INTERROGATION_TIME_SECONDS,
                questionsThisRound = 0,
                currentTopicIndex = nextTopicIndex
            )

            return state.copy(
                interrogationSessions = state.interrogationSessions + (suspectId to updatedSession),
                messages = state.messages + (suspectId to (currentMsgs + topicMsg + systemMsg))
            )
        }

        fun advanceDialogueRound(state: GameState, suspectId: String): GameState {
            val case = state.currentCase ?: return state
            val suspect = case.suspects.find { it.id == suspectId } ?: return state
            val session = state.interrogationSessions[suspectId] ?: return state

            val topics = SUSPECT_TOPICS[suspectId] ?: listOf("otro tema")
            val nextTopicIndex = (session.currentTopicIndex + 1) % topics.size
            val newTopic = topics[nextTopicIndex]

            val currentMsgs = state.messages[suspectId] ?: emptyList()
            val topicMsg = InterrogationMessage(
                sender = suspect.name,
                text = "Ya respondí suficiente sobre eso. Hablemos de ${newTopic}.",
                isDetective = false,
                time = getCurrentTime()
            )

            val updatedSession = session.copy(
                questionsThisRound = 0,
                currentTopicIndex = nextTopicIndex
            )

            return state.copy(
                interrogationSessions = state.interrogationSessions + (suspectId to updatedSession),
                messages = state.messages + (suspectId to (currentMsgs + topicMsg))
            )
        }

        fun getQuestionsForRound(state: GameState, suspectId: String): List<Question> {
            val case = state.currentCase ?: return emptyList()
            val suspect = case.suspects.find { it.id == suspectId } ?: return emptyList()
            val session = state.interrogationSessions[suspectId] ?: return emptyList()
            if (!session.isActive) return emptyList()

            val remaining = suspect.availableQuestions.filter { it.id !in session.askedQuestionIds }
            if (remaining.isEmpty()) return emptyList()

            val selected = mutableListOf<Question>()
            val usedApproaches = mutableSetOf<String>()

            for (question in remaining) {
                if (selected.size >= MAX_QUESTIONS_PER_ROUND) break
                if (question.approach !in usedApproaches || remaining.size <= MAX_QUESTIONS_PER_ROUND) {
                    selected.add(question)
                    usedApproaches.add(question.approach)
                }
            }
            if (selected.size < MAX_QUESTIONS_PER_ROUND) {
                remaining.filter { it !in selected }.take(MAX_QUESTIONS_PER_ROUND - selected.size).forEach {
                    selected.add(it)
                }
            }
            return selected.take(MAX_QUESTIONS_PER_ROUND)
        }

        /**
         * Realiza una pregunta a un sospechoso.
         */
        private fun getPressuredReply(suspectId: String, approach: String, reply: String, trustLevel: Int, revealsInfo: Boolean): String {
            if (!revealsInfo) return reply

            return when (suspectId) {
                "suspect_carlos" -> when (approach) {
                    "EMPÁTICO" -> "(Se relaja un poco, habla más despacio) $reply"
                    "TÉCNICO" -> "(Se enfoca en los detalles, menos nervioso) $reply"
                    "PRESIÓN" -> "(A regañadientes, entre dientes) $reply"
                    else -> if (trustLevel < 40) "(Titubea) $reply" else reply
                }
                "suspect_valentina" -> when (approach) {
                    "TÉCNICO" -> "(Consulta mentalmente sus registros) $reply"
                    "DIRECTO" -> "(Con tono clínico, sin emoción) $reply"
                    else -> reply
                }
                "suspect_tomas" -> when (approach) {
                    "DIRECTO" -> "(Lo mira a los ojos, respetando la franqueza) $reply"
                    "TÉCNICO" -> "(Como colega profesional) $reply"
                    else -> reply
                }
                "suspect_marisol" -> when (approach) {
                    "EMPÁTICO" -> "(Se seca una lágrima, voz quebrada) $reply"
                    else -> reply
                }
                else -> reply
            }
        }

        private fun getStatementForQuestion(suspectId: String, questionId: String): String? {
            return when (suspectId) {
                "suspect_carlos" -> when (questionId) {
                    "q_carlos_1" -> "Yo jamás he firmado ningún manifiesto de importación para ese contenedor, detective."
                    "q_carlos_2" -> "No tengo absolutamente nada que ver con Logística del Caribe SAS, ni conozco esa firma."
                    "q_carlos_3" -> "Estaba trabajando solo en el almacén a las 9 PM."
                    "q_carlos_5" -> "Nunca he visto ni hablado con Marisol Mendoza, la hija del jefe."
                    "q_carlos_6" -> "Yo no escribí ese borrador."
                    "q_carlos_7" -> "Gracias... bueno, estaba haciendo el inventario como siempre. Nada fuera de lo normal."
                    "q_carlos_9" -> "Primero reviso los manifiestos, luego el sello aduanero, y finalmente firmo la salida."
                    else -> null
                }
                "suspect_tomas" -> when (questionId) {
                    "q_tomas_1" -> "Hice la ronda habitual, todo estaba correcto."
                    "q_tomas_2" -> "Nunca he cruzado una palabra con ese tal Carlos Herrera, es un completo desconocido para mí."
                    "q_tomas_3" -> "Esos depósitos son ahorros personales."
                    "q_tomas_4" -> "Yo jamás he omitido ninguna inspección ni he salido en ninguna grabación del puerto."
                    "q_tomas_5" -> "¡Le aseguro que nadie me dijo que ignorara ESE contenedor! ... Quiero decir, ningún contenedor."
                    else -> null
                }
                "suspect_valentina" -> when (questionId) {
                    "q_valentina_1" -> "Fueron transacciones operativas normales."
                    "q_valentina_2" -> "El seguro se gestionó de forma automática desde mi terminal, cualquiera pudo usarla."
                    "q_valentina_4" -> "Tengo registros de que Carlos coordinaba y realizaba las transferencias directamente con Logística del Caribe SAS."
                    "q_valentina_6" -> "No tengo relación alguna con cuentas offshore en Panamá."
                    else -> null
                }
                "suspect_marisol" -> when (questionId) {
                    "q_marisol_2" -> "Yo jamás he hecho ningún trato ni tengo negocios de ningún tipo con Carlos Herrera."
                    "q_marisol_5" -> "Son dividendos acordados de forma privada."
                    else -> null
                }
                else -> null
            }
        }

        fun applyDynamicQuestionResponse(
            state: GameState,
            suspectId: String,
            questionId: String,
            dialog: String,
            effectType: String,
            effectValue: String,
            narratorNote: String
        ): GameState {
            val case = state.currentCase ?: return state
            val suspect = case.suspects.find { it.id == suspectId } ?: return state
            val question = suspect.availableQuestions.find { it.id == questionId } ?: return state
            val session = state.interrogationSessions[suspectId] ?: return state

            if (state.interrogatedSuspects.contains(suspectId) ||
                questionId in session.askedQuestionIds ||
                session.questionsThisRound >= 3
            ) {
                return state
            }

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
            var updatedClues = case.clues
            var keyPoint: String? = null

            when (effectType.uppercase()) {
                "TRUST" -> {
                    val change = effectValue.toIntOrNull() ?: 0
                    val newTrust = (suspect.trustLevel + change).coerceIn(0, 100)
                    updatedSuspect = suspect.copy(trustLevel = newTrust)
                    keyPoint = "Efecto Confianza: $change. Confianza actual: $newTrust."
                }
                "CONTRADICTION" -> {
                    updatedSuspect = suspect.copy(
                        contradictions = (suspect.contradictions + effectValue).distinct()
                    )
                    pointsToAdd += 200
                    contradictionFound = true
                    keyPoint = "⚠ CONTRADICCIÓN: $effectValue"
                }
                "UNLOCK_CLUE" -> {
                    pointsToAdd += 50
                    updatedClues = case.clues.map {
                        if (it.id == effectValue) it.copy(isAvailable = true) else it
                    }
                    val clueTitle = case.clues.find { it.id == effectValue }?.title ?: effectValue
                    keyPoint = "🔓 Pista desbloqueada: $clueTitle"
                }
                else -> {
                    keyPoint = "Información obtenida."
                }
            }

            val updatedSuspects = case.suspects.map { if (it.id == suspectId) updatedSuspect else it }
            val currentMsgs = state.messages[suspectId] ?: emptyList()
            val detMsg = InterrogationMessage("INVESTIGADOR", question.text, true, getCurrentTime())

            val replyText = if (narratorNote.isNotBlank()) {
                "($narratorNote) $dialog"
            } else {
                dialog
            }

            val suspectMsg = InterrogationMessage(
                sender = suspect.name,
                text = replyText,
                isDetective = false,
                time = getCurrentTime(),
                isContradiction = effectType.uppercase() == "CONTRADICTION"
            )

            val updatedMessages = state.messages + (suspectId to (currentMsgs + detMsg + suspectMsg))

            val statementText = getStatementForQuestion(suspectId, questionId) ?: dialog
            val updatedStatements = if (state.statements.none { it.questionId == questionId }) {
                state.statements + Statement(
                    id = "stmt_$questionId",
                    suspectId = suspectId,
                    text = statementText,
                    questionId = questionId
                )
            } else {
                state.statements
            }

            val baseSusp = when (suspectId) {
                "suspect_carlos" -> 65
                "suspect_tomas" -> 50
                "suspect_marisol" -> 35
                "suspect_valentina" -> 40
                else -> 0
            }
            val currentSusp = state.suspicionLevels[suspectId] ?: baseSusp
            val suspicionIncrease = if (effectType.uppercase() == "CONTRADICTION") 10 else 5
            val newSusp = (currentSusp + suspicionIncrease).coerceAtMost(100)
            val updatedSuspicion = state.suspicionLevels + (suspectId to newSusp)

            val newContradictionKey = if (effectType.uppercase() == "CONTRADICTION") {
                when (questionId) {
                    "q_carlos_1" -> "carlos_manifiesto"
                    "q_carlos_3" -> "carlos_9pm"
                    "q_carlos_6" -> "carlos_borrador"
                    "q_valentina_2" -> "valentina_seguro"
                    "q_valentina_6" -> "valentina_offshore"
                    "q_tomas_2" -> "tomas_carlos"
                    "q_tomas_4" -> "tomas_camara"
                    "q_tomas_5" -> "tomas_ignorar"
                    "q_marisol_2" -> "marisol_carlos_contrato"
                    else -> questionId
                }
            } else null

            val updatedContradictions = if (newContradictionKey != null) {
                state.discoveredContradictions + newContradictionKey
            } else {
                state.discoveredContradictions
            }

            val updatedSession = session.copy(
                askedQuestionIds = session.askedQuestionIds + questionId,
                questionsThisRound = session.questionsThisRound + 1,
                sessionKeyPoints = if (keyPoint != null) session.sessionKeyPoints + keyPoint else session.sessionKeyPoints
            )

            var nextState = state.copy(
                currentCase = case.copy(clues = updatedClues, suspects = updatedSuspects),
                messages = updatedMessages,
                statements = updatedStatements,
                suspicionLevels = updatedSuspicion,
                discoveredContradictions = updatedContradictions,
                interrogationSessions = state.interrogationSessions + (suspectId to updatedSession),
                score = state.score + pointsToAdd,
                progress = state.progress.copy(
                    contradictionsFound = state.progress.contradictionsFound + (if (contradictionFound) 1 else 0)
                )
            )

            if (updatedSession.questionsThisRound >= MAX_QUESTIONS_PER_ROUND) {
                val remaining = suspect.availableQuestions.count { it.id !in updatedSession.askedQuestionIds }
                if (remaining > 0) {
                    nextState = advanceDialogueRound(nextState, suspectId)
                }
            }

            return evaluatePhaseTransition(evaluateChainUnlocks(nextState))
        }

        fun askQuestion(state: GameState, suspectId: String, questionId: String): GameState {
            val case = state.currentCase ?: return state
            val suspect = case.suspects.find { it.id == suspectId } ?: return state
            val question = suspect.availableQuestions.find { it.id == questionId } ?: return state

            if (state.interrogatedSuspects.contains(suspectId)) {
                return state
            }

            val session = state.interrogationSessions[suspectId]
            if (session == null || !session.isActive) {
                return state
            }

            if (questionId in session.askedQuestionIds) {
                return state
            }

            if (session.questionsThisRound >= MAX_QUESTIONS_PER_ROUND) {
                return state
            }

            val roundQuestions = getQuestionsForRound(state, suspectId)
            if (questionId !in roundQuestions.map { it.id }) {
                return state
            }

            // Bloquear nuevas preguntas si el trustLevel llega a 0 o menos
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

            val revealProbability = calculateRevealProbability(question, state, suspectId)
            val revealsInfo = nextRandomFloat() <= revealProbability

            var updatedSuspect = suspect
            var pointsToAdd = 0
            var contradictionFound = false
            var updatedClues = case.clues
            var keyPoint: String? = null

            if (revealsInfo) {
                if (suspectId == "suspect_valentina" && questionId == "q_valentina_4") {
                    val carlosInterrogated = state.interrogatedSuspects.contains("suspect_carlos")
                    if (!carlosInterrogated) {
                        val change = -15
                        val newTrust = (suspect.trustLevel + change).coerceIn(0, 100)
                        updatedSuspect = suspect.copy(trustLevel = newTrust)
                        keyPoint = "Valentina se mostró evasiva sobre Logística del Caribe"
                    } else {
                        pointsToAdd += 50
                        updatedClues = case.clues.map {
                            if (it.id == "clue_bancos") it.copy(isAvailable = true) else it
                        }
                        keyPoint = "Valentina confirmó transacciones con Logística del Caribe SAS"
                    }
                } else {
                    when (question.effectType) {
                        "TRUST" -> {
                            val change = question.effectValue.toIntOrNull() ?: 0
                            val newTrust = (suspect.trustLevel + change).coerceIn(0, 100)
                            updatedSuspect = suspect.copy(trustLevel = newTrust)
                            keyPoint = "Respuesta sobre: ${question.text.take(40)}..."
                        }
                        "CONTRADICTION" -> {
                            updatedSuspect = suspect.copy(
                                contradictions = (suspect.contradictions + question.effectValue).distinct()
                            )
                            pointsToAdd += 200
                            contradictionFound = true
                            keyPoint = "⚠ CONTRADICCIÓN: ${question.effectValue}"
                        }
                        "UNLOCK_CLUE" -> {
                            pointsToAdd += 50
                            val clueId = question.effectValue
                            updatedClues = case.clues.map {
                                if (it.id == clueId) it.copy(isAvailable = true) else it
                            }
                            val clueTitle = case.clues.find { it.id == clueId }?.title ?: clueId
                            keyPoint = "🔓 Pista desbloqueada: $clueTitle"
                        }
                        else -> {
                            keyPoint = "Información obtenida sobre: ${question.text.take(40)}..."
                        }
                    }
                }
            } else {
                val trustPenalty = -5
                updatedSuspect = suspect.copy(trustLevel = (suspect.trustLevel + trustPenalty).coerceIn(0, 100))
                keyPoint = "El sospechoso cerró el discurso — ese enfoque no encaja con su personalidad"
            }

            val updatedSuspects = case.suspects.map { if (it.id == suspectId) updatedSuspect else it }
            val currentMsgs = state.messages[suspectId] ?: emptyList()
            val detMsg = InterrogationMessage("INVESTIGADOR", question.text, true, getCurrentTime())

            val rawReplyText = if (revealsInfo) {
                getSuspectStaticReply(state, suspectId, questionId)
            } else {
                getEvasiveReply(suspectId, question.approach, question.topic)
            }
            val replyText = getPressuredReply(suspectId, question.approach, rawReplyText, suspect.trustLevel, revealsInfo)
            val suspectMsg = InterrogationMessage(
                sender = suspect.name,
                text = replyText,
                isDetective = false,
                time = getCurrentTime(),
                isContradiction = revealsInfo && question.effectType == "CONTRADICTION"
            )

            val updatedMessages = state.messages + (suspectId to (currentMsgs + detMsg + suspectMsg))

            val newStatementText = if (revealsInfo) getStatementForQuestion(suspectId, questionId) else null
            val updatedStatements = if (newStatementText != null && state.statements.none { it.questionId == questionId }) {
                state.statements + Statement(
                    id = "stmt_$questionId",
                    suspectId = suspectId,
                    text = newStatementText,
                    questionId = questionId
                )
            } else {
                state.statements
            }

            val baseSusp = when (suspectId) {
                "suspect_carlos" -> 65
                "suspect_tomas" -> 50
                "suspect_marisol" -> 35
                "suspect_valentina" -> 40
                else -> 0
            }
            val currentSusp = state.suspicionLevels[suspectId] ?: baseSusp
            val suspicionIncrease = if (revealsInfo && question.effectType == "CONTRADICTION") 10 else if (revealsInfo) 5 else 2
            val newSusp = (currentSusp + suspicionIncrease).coerceAtMost(100)
            val updatedSuspicion = state.suspicionLevels + (suspectId to newSusp)

            val newContradictionKey = if (revealsInfo && question.effectType == "CONTRADICTION") {
                when (questionId) {
                    "q_carlos_1" -> "carlos_manifiesto"
                    "q_carlos_3" -> "carlos_9pm"
                    "q_carlos_6" -> "carlos_borrador"
                    "q_valentina_2" -> "valentina_seguro"
                    "q_valentina_6" -> "valentina_offshore"
                    "q_tomas_2" -> "tomas_carlos"
                    "q_tomas_5" -> "tomas_ignorar"
                    "q_marisol_2" -> "marisol_carlos"
                    "q_carlos_usb_3" -> "carlos_usb_fraude"
                    else -> questionId
                }
            } else null

            val updatedContradictions = if (newContradictionKey != null) {
                state.discoveredContradictions + newContradictionKey
            } else {
                state.discoveredContradictions
            }

            val updatedSession = session.copy(
                askedQuestionIds = session.askedQuestionIds + questionId,
                questionsThisRound = session.questionsThisRound + 1,
                sessionKeyPoints = if (keyPoint != null) session.sessionKeyPoints + keyPoint else session.sessionKeyPoints
            )

            var nextState = state.copy(
                currentCase = case.copy(clues = updatedClues, suspects = updatedSuspects),
                messages = updatedMessages,
                statements = updatedStatements,
                suspicionLevels = updatedSuspicion,
                discoveredContradictions = updatedContradictions,
                interrogationSessions = state.interrogationSessions + (suspectId to updatedSession),
                score = state.score + pointsToAdd,
                progress = state.progress.copy(
                    contradictionsFound = state.progress.contradictionsFound + (if (contradictionFound) 1 else 0)
                )
            )

            if (updatedSession.questionsThisRound >= MAX_QUESTIONS_PER_ROUND) {
                val remaining = suspect.availableQuestions.count { it.id !in updatedSession.askedQuestionIds }
                if (remaining > 0) {
                    nextState = advanceDialogueRound(nextState, suspectId)
                }
            }

            return evaluatePhaseTransition(evaluateChainUnlocks(nextState))
        }

        private fun getEvasiveReply(suspectId: String, approach: String, topic: String): String {
            return when (suspectId) {
                "suspect_carlos" -> when (approach) {
                    "PRESIÓN" -> "(Se pone rígido, voz temblorosa de ira) No... no voy a hablar así. Pídame las cosas con respeto o llamo a mi abogado."
                    "DIRECTO" -> "(Evita la mirada, se revuelve en la silla) Eso no... no tiene que ver con $topic. Prefiero quedarme callado."
                    "TÉCNICO" -> "Los protocolos están en el manual del almacén. No recuerdo los detalles de memoria, lo siento."
                    "EMPÁTICO" -> "(Suspira, pero se cierra) Gracias por el tono, detective, pero de verdad no sé nada más sobre $topic."
                    else -> "No quiero hablar de $topic."
                }
                "suspect_valentina" -> when (approach) {
                    "EMPÁTICO" -> "(Fría) Detective, los sentimientos no aparecen en los libros contables. Limítese a los hechos verificables."
                    "PRESIÓN" -> "(Se endereza, gélida) Le advierto que cualquier acusación sin fundamento será contrademandada. Siguiente pregunta."
                    "DIRECTO" -> "Esa información es confidencial. No tengo obligación de responder sobre $topic sin una orden judicial."
                    "TÉCNICO" -> "Necesitaría revisar los libros auxiliares antes de confirmar algo sobre $topic. No improviso cifras."
                    else -> "No tengo nada que declarar sobre $topic."
                }
                "suspect_tomas" -> when (approach) {
                    "EMPÁTICO" -> "(Desdeñoso) No necesito su compasión, inspector. Hago mi trabajo. $topic no es asunto suyo."
                    "PRESIÓN" -> "(Se inclina hacia adelante, intimidante) ¿Sabe quién soy yo? Llevo veinte años en aduanas. No me venga con esas."
                    "DIRECTO" -> "Ya respondí lo que había que responder sobre $topic. No tengo nada que agregar."
                    "TÉCNICO" -> "El procedimiento está documentado. Si quiere detalles, pida los formularios oficiales."
                    else -> "Esa pregunta no procede."
                }
                "suspect_marisol" -> when (approach) {
                    "PRESIÓN" -> "(Se levanta, furiosa, con los ojos rojos) ¡¿Cómo se atreve?! ¡Acabo de perder a mi padre! ¡Quiero a mi abogado ahora mismo!"
                    "TÉCNICO" -> "(Seca las lágrimas con fastidio) No voy a hablar de papeles y números. Mi padre acaba de morir."
                    "DIRECTO" -> "(Cruza los brazos) Eso es muy personal. No le debo explicaciones sobre $topic a un desconocido."
                    "EMPÁTICO" -> "(Baja la voz, pero se retrae) Lo siento... no puedo hablar más de $topic ahora. Es demasiado doloroso."
                    else -> "No quiero seguir hablando de esto."
                }
                else -> "No tengo nada que decir sobre eso."
            }
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
                    "q_carlos_7" -> "(Suspira aliviado) Bueno... sí, estaba en el almacén hasta tarde. Solo yo y los guardias de turno. Pueden preguntarles."
                    "q_carlos_8" -> "¡Eso es una acusación infundada! Tengo derecho a un abogado. No voy a seguir hablando así."
                    "q_carlos_9" -> "El protocolo es claro: revisión de manifiesto, verificación de sellos, registro en el sistema y firma de salida. Lo hice todo correctamente."
                    "q_carlos_usb_1" -> "¡Eso es mentira! No hay pruebas de que yo coordinara nada... (ve la USB y se calla)... Está bien, hablábamos de logística de vez en cuando, pero nada ilegal."
                    "q_carlos_usb_2" -> "El dinero... el dinero era para pagar los sobrecostos de las operaciones portuarias. Yo no me quedaba con nada, todo era para mantener la empresa a flote."
                    "q_carlos_usb_3" -> "¡No! Don Aurelio no sabía... (empieza a sudar frío)... él... él confiaba en mí. Fue un accidente. ¡Yo no quería que terminara así!"
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
                    "q_valentina_7" -> "Mi jefe era un buen hombre. Solo hago mi trabajo contable, no sé nada de lo que pasó esa noche."
                    "q_valentina_8" -> "(Se endereza, fría) Detective, le sugiero que revise sus acusaciones con su abogado antes de hablar conmigo así."
                    else -> "No tengo nada que declarar sobre eso."
                }
                "suspect_tomas" -> when (questionId) {
                    "q_tomas_1" -> "El sistema aduanero falló esa tarde. Tuvimos un apagón de red y tuvimos que procesar todo manualmente. No es mi culpa."
                    "q_tomas_2" -> "No, no lo conozco. He tratado con cientos de jefes de logística, pero no tengo ninguna relación personal con ese señor."
                    "q_tomas_3" -> "¿De qué depósitos habla? Mis finanzas personales son privadas. ¡Esto es un atropello, no toleraré estas acusaciones sin pruebas!"
                    "q_tomas_4" -> "Ese video... no muestra todo el contexto. Está bien, yo... yo solo seguía instrucciones de arriba."
                    "q_tomas_5" -> "¡Le aseguro que nadie me dijo que ignorara ESE contenedor! ... Quiero decir, ningún contenedor."
                    "q_tomas_6" -> "Mire, detective, el puerto es un infierno de presión. A veces las cosas se hacen como se pueden, no como deberían."
                    "q_tomas_7" -> "¡Cuidado con sus acusaciones! Yo tengo 20 años de servicio y conozco a mis superiores. No me intimida."
                    else -> "No tengo nada que declarar sobre eso."
                }
                "suspect_marisol" -> when (questionId) {
                    "q_marisol_1" -> "Sí, mi padre estaba muy preocupado últimamente. Sentía que alguien lo estaba traicionando desde adentro."
                    "q_marisol_2" -> "No, solo sé que trabaja para mi padre. Nunca he salido con él ni tenemos ninguna relación."
                    "q_marisol_3" -> "Es lo que cualquier persona responsable haría tras la muerte de su padre para proteger el patrimonio familiar. ¿Tiene algún problema con eso?"
                    "q_marisol_4" -> "Sí... me dijo que sospechaba de Carlos y guardaba todo anotado en su agenda. Debería buscarla en nuestra residencia."
                    "q_marisol_5" -> "Yo... no sé de qué habla. Es solo... un término contable que Carlos me mencionó... (guarda silencio)."
                    "q_marisol_6" -> "(Llora un momento) Gracias... Mi padre era bueno. No merecía morir así. Alguien lo traicionó."
                    "q_marisol_7" -> "¡Cómo se atreve! Acabo de perder a mi padre y usted me trata como sospechosa. ¡Quiero hablar con mi abogado!"
                    else -> "No tengo nada que declarar sobre eso."
                }
                else -> "Solo quiero que se sepa la verdad."
            }
        }

        fun endInterrogation(state: GameState, suspectId: String): GameState {
            val case = state.currentCase ?: return state
            val suspect = case.suspects.find { it.id == suspectId } ?: return state
            if (state.interrogatedSuspects.contains(suspectId)) return state

            val session = state.interrogationSessions[suspectId]
            val newInterrogated = (state.interrogatedSuspects + suspectId).distinct()

            val suspectStatements = state.statements.filter { it.suspectId == suspectId }
            val sessionKeyPoints = session?.sessionKeyPoints ?: emptyList()
            val contradictions = suspect.contradictions
            val cluesUnlocked = case.clues.filter { clue ->
                clue.unlockConditionType == "INTERROGATION" &&
                        clue.unlockConditionValue == suspectId &&
                        clue.isAvailable
            }.map { it.title }

            val summaryKeyPoints = buildList {
                addAll(sessionKeyPoints)
                suspectStatements.forEach { stmt ->
                    add("Declaró: \"${stmt.text.take(60)}...\"")
                }
                if (isEmpty()) add("El sospechoso no reveló información significativa.")
            }

            val summary = InterrogationSummary(
                suspectId = suspectId,
                suspectName = suspect.name,
                keyPoints = summaryKeyPoints.distinct(),
                contradictionsFound = contradictions,
                cluesUnlocked = cluesUnlocked,
                questionsAsked = session?.askedQuestionIds?.size ?: 0,
                completedAt = getCurrentTime()
            )

            val currentMsgs = state.messages[suspectId] ?: emptyList()
            val endMsg = InterrogationMessage(
                sender = "SISTEMA",
                text = "INTERROGATORIO FINALIZADO — ${suspect.name} ha sido liberado. Revisa el registro de puntos clave.",
                isDetective = false,
                time = getCurrentTime()
            )

            val closedSession = session?.copy(isActive = false) ?: InterrogationSession(suspectId = suspectId, isActive = false)

            var nextState = state.copy(
                interrogatedSuspects = newInterrogated,
                interrogationSessions = state.interrogationSessions + (suspectId to closedSession),
                interrogationSummaries = state.interrogationSummaries + (suspectId to summary),
                messages = state.messages + (suspectId to (currentMsgs + endMsg)),
                progress = state.progress.copy(
                    interrogationsCompleted = newInterrogated.size
                )
            )

            nextState = evaluateUnlockConditions(nextState, suspectId)
            return evaluatePhaseTransition(evaluateChainUnlocks(nextState))
        }

        private fun evaluateUnlockConditions(state: GameState, completedSuspectId: String): GameState {
            val case = state.currentCase ?: return state
            val updatedClues = case.clues.map { clue ->
                val shouldUnlock = when (clue.unlockConditionType) {
                    "INTERROGATION" -> clue.unlockConditionValue == completedSuspectId
                    else -> false
                }
                if (shouldUnlock && !clue.isAvailable) clue.copy(isAvailable = true) else clue
            }
            return state.copy(currentCase = case.copy(clues = updatedClues))
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

        fun confrontStatementWithClue(state: GameState, statementId: String, clueId: String): GameState {
            val case = state.currentCase ?: return state

            val contradictionKey = when {
                statementId == "stmt_q_carlos_2" && clueId == "clue_bancos" -> "carlos_bancos"
                statementId == "stmt_q_carlos_1" && clueId == "clue_manifiesto" -> "carlos_manifiesto"
                statementId == "stmt_q_carlos_3" && clueId == "clue_camara" -> "carlos_9pm"
                statementId == "stmt_q_tomas_2" && clueId == "clue_usb" -> "tomas_carlos"
                statementId == "stmt_q_tomas_4" && clueId == "clue_camara" -> "tomas_camara"
                statementId == "stmt_q_tomas_5" && clueId == "clue_usb" -> "tomas_ignorar"
                statementId == "stmt_q_valentina_2" && clueId == "clue_bancos" -> "valentina_seguro"
                statementId == "stmt_q_valentina_6" && clueId == "clue_bancos" -> "valentina_offshore"
                statementId == "stmt_q_marisol_2" && clueId == "clue_contrato" -> "marisol_carlos_contrato"
                else -> null
            }

            val targetSuspectId = when {
                statementId.contains("carlos") -> "suspect_carlos"
                statementId.contains("tomas") -> "suspect_tomas"
                statementId.contains("valentina") -> "suspect_valentina"
                statementId.contains("marisol") -> "suspect_marisol"
                else -> ""
            }

            val currentMsgs = state.messages[targetSuspectId] ?: emptyList()

            if (contradictionKey == null) {
                // Wrong confrontation
                val updatedSuspects = case.suspects.map { suspect ->
                    if (suspect.id == targetSuspectId) {
                        suspect.copy(trustLevel = (suspect.trustLevel - 15).coerceIn(0, 100))
                    } else suspect
                }
                val systemMsg = InterrogationMessage(
                    sender = "SISTEMA",
                    text = "CONFRONTACIÓN FALLIDA: Las pruebas no contradicen esta declaración directamente. (-15 Confianza)",
                    isDetective = false,
                    time = getCurrentTime()
                )
                return state.copy(
                    currentCase = case.copy(suspects = updatedSuspects),
                    messages = state.messages + (targetSuspectId to (currentMsgs + systemMsg))
                )
            }

            if (state.discoveredContradictions.contains(contradictionKey)) {
                return state
            }

            // Correct confrontation!
            val updatedSuspects = case.suspects.map { suspect ->
                if (suspect.id == targetSuspectId) {
                    suspect.copy(trustLevel = (suspect.trustLevel + 15).coerceIn(0, 100))
                } else suspect
            }

            val currentSusp = state.suspicionLevels[targetSuspectId] ?: when (targetSuspectId) {
                "suspect_carlos" -> 65
                "suspect_tomas" -> 50
                "suspect_marisol" -> 35
                "suspect_valentina" -> 40
                else -> 0
            }
            val newSusp = (currentSusp + 15).coerceAtMost(100)
            val updatedSuspicion = state.suspicionLevels + (targetSuspectId to newSusp)

            val systemText = when (contradictionKey) {
                "carlos_bancos" -> "¡CONTRADICCIÓN! Los registros bancarios demuestran que Carlos recibió fondos de Logística del Caribe, desmintiendo su declaración. (+15 Confianza)"
                "carlos_manifiesto" -> "¡CONTRADICCIÓN! El manifiesto falsificado lleva la firma de Carlos, lo cual contradice su afirmación de no recordarlo. (+15 Confianza)"
                "carlos_9pm" -> "¡CONTRADICCIÓN! El video de seguridad lo sitúa fuera del almacén a las 9 PM, rompiendo su alibi. (+15 Confianza)"
                "tomas_carlos" -> "¡CONTRADICCIÓN! Los correos electrónicos de la USB demuestran que Tomás y Carlos se conocían y coordinaban operaciones. (+15 Confianza)"
                "tomas_camara" -> "¡CONTRADICCIÓN! El video de la cámara del puerto muestra claramente a Tomás validando el contenedor sin inspección. (+15 Confianza)"
                "tomas_ignorar" -> "¡CONTRADICCIÓN! La USB revela que Tomás recibió instrucciones específicas de Carlos para ignorar este contenedor. (+15 Confianza)"
                "valentina_seguro" -> "¡CONTRADICCIÓN! Los registros muestran que el seguro fue tramitado desde el terminal y cuenta de Valentina, implicando su firma. (+15 Confianza)"
                "valentina_offshore" -> "¡CONTRADICCIÓN! Valentina mintió: la cuenta offshore en Panamá posee autorizaciones financieras directas hechas por ella. (+15 Confianza)"
                "marisol_carlos_contrato" -> "¡CONTRADICCIÓN! Marisol afirmó no tener relación con Carlos, pero el contrato privado de ganancias firmado por ambos demuestra lo contrario. (+15 Confianza)"
                else -> "¡CONTRADICCIÓN DETECTADA! La evidencia desmiente la declaración. (+15 Confianza)"
            }

            val systemMsg = InterrogationMessage(
                sender = "SISTEMA",
                text = systemText,
                isDetective = false,
                time = getCurrentTime(),
                isContradiction = true
            )

            val extraReveal = when (contradictionKey) {
                "carlos_9pm" -> "[REVELACIÓN ADICIONAL] El guardia nocturno del almacén testificó: 'Vi a Carlos salir apurado con una mochila negra pesada a las 9 PM. Parecía sumamente tenso y nervioso'. Esto sitúa a Carlos en la escena de la huida."
                "carlos_bancos" -> "[REVELACIÓN ADICIONAL] Los logs del sistema registran que Logística del Caribe SAS fue dada de alta desde la dirección IP de la oficina privada de Carlos Herrera."
                "carlos_manifiesto" -> "[REVELACIÓN ADICIONAL] Una pericia caligráfica oficial confirma que la firma estampada en el Manifiesto de Importación Falsificado corresponde de forma inequívoca al puño y letra de Carlos Herrera."
                "valentina_seguro" -> "[PISTA FALSA / DESPISTE] Se descubrió un correo encriptado enviado por Valentina Ríos a un contacto de Panamá la tarde del crimen: 'El seguro del contenedor ya fue procesado, desvía los fondos a la cuenta especial. El viejo (Don Aurelio) ya no será un obstáculo'. Esto sugiere que Valentina planificó el desfalco y tenía motivos financieros."
                "valentina_offshore" -> "[PISTA FALSA / DESPISTE] Se localizó un pasaporte de Valentina Ríos con tres sellos de entrada a Ciudad de Panamá en los últimos 60 días, coincidiendo con las fechas de las transferencias no respaldadas de la empresa."
                "marisol_carlos_contrato" -> "[PISTA FALSA / DESPISTE] Marisol Mendoza retiró $50,000 en efectivo la tarde del crimen. Un empleado de finanzas declara haberla escuchado gritarle a su padre Don Aurelio: '¡Si no me das lo que me corresponde por las buenas, lo tomaré yo misma!'"
                "tomas_carlos" -> "[PISTA FALSA / DESPISTE] Tomás Guerrero tenía una deuda de juego clandestino de $120,000 que fue saldada en efectivo en su totalidad al día siguiente de la muerte de Don Aurelio. El inspector tenía motivos financieros."
                "tomas_camara" -> "[REVELACIÓN ADICIONAL] El video de alta definición de la caseta muestra al inspector Tomás Guerrero recibiendo un sobre abultado de manos de Carlos Herrera minutos antes de dar paso libre al contenedor."
                else -> null
            }

            val updatedExtraInfo = if (extraReveal != null) {
                (state.extraInfoFound + extraReveal).distinct()
            } else {
                state.extraInfoFound
            }

            val nextState = state.copy(
                currentCase = case.copy(suspects = updatedSuspects),
                discoveredContradictions = state.discoveredContradictions + contradictionKey,
                suspicionLevels = updatedSuspicion,
                messages = state.messages + (targetSuspectId to (currentMsgs + systemMsg)),
                score = state.score + 200,
                extraInfoFound = updatedExtraInfo,
                progress = state.progress.copy(
                    contradictionsFound = state.progress.contradictionsFound + 1
                )
            )

            return evaluatePhaseTransition(evaluateChainUnlocks(nextState))
        }

        fun compareStatements(state: GameState, statementId1: String, statementId2: String): GameState {
            val case = state.currentCase ?: return state

            val contradictionKey = when {
                (statementId1 == "stmt_q_carlos_5" && statementId2 == "stmt_q_marisol_2") ||
                (statementId1 == "stmt_q_marisol_2" && statementId2 == "stmt_q_carlos_5") -> "carlos_marisol_relation"

                (statementId1 == "stmt_q_carlos_2" && statementId2 == "stmt_q_valentina_4") ||
                (statementId1 == "stmt_q_valentina_4" && statementId2 == "stmt_q_carlos_2") -> "carlos_valentina_relation"

                else -> null
            }

            if (contradictionKey == null) {
                val systemMsg = InterrogationMessage(
                    sender = "SISTEMA",
                    text = "COMPARACIÓN FALLIDA: Los testimonios no se contradicen mutuamente. (-5 Confianza a sospechosos involucrados)",
                    isDetective = false,
                    time = getCurrentTime()
                )
                val stmt1 = state.statements.find { it.id == statementId1 }
                val stmt2 = state.statements.find { it.id == statementId2 }
                val updatedSuspects = case.suspects.map { suspect ->
                    if (suspect.id == stmt1?.suspectId || suspect.id == stmt2?.suspectId) {
                        suspect.copy(trustLevel = (suspect.trustLevel - 5).coerceIn(0, 100))
                    } else suspect
                }
                val updatedMessages = state.messages.mapValues { it.value + systemMsg }
                return state.copy(
                    currentCase = case.copy(suspects = updatedSuspects),
                    messages = updatedMessages
                )
            }

            if (state.discoveredContradictions.contains(contradictionKey)) {
                return state
            }

            // Success comparison
            val updatedSuspects = case.suspects.map { suspect ->
                when (contradictionKey) {
                    "carlos_marisol_relation" -> {
                        if (suspect.id == "suspect_carlos" || suspect.id == "suspect_marisol") {
                            suspect.copy(trustLevel = (suspect.trustLevel + 15).coerceIn(0, 100))
                        } else suspect
                    }
                    "carlos_valentina_relation" -> {
                        if (suspect.id == "suspect_carlos" || suspect.id == "suspect_valentina") {
                            suspect.copy(trustLevel = (suspect.trustLevel + 15).coerceIn(0, 100))
                        } else suspect
                    }
                    else -> suspect
                }
            }

            val systemText = when (contradictionKey) {
                "carlos_marisol_relation" -> "¡CONTRADICCIÓN DETECTADA ENTRE TESTIMONIOS! Carlos y Marisol mintieron sobre su relación personal. (+15 Confianza)"
                "carlos_valentina_relation" -> "¡CONTRADICCIÓN DETECTADA ENTRE TESTIMONIOS! Carlos negó vínculos con Logística del Caribe SAS, pero Valentina confirmó que él coordinaba transferencias directamente. (+15 Confianza)"
                else -> "¡CONTRADICCIÓN DETECTADA ENTRE TESTIMONIOS! (+15 Confianza)"
            }

            val systemMsg = InterrogationMessage(
                sender = "SISTEMA",
                text = systemText,
                isDetective = false,
                time = getCurrentTime(),
                isContradiction = true
            )

            val updatedMessages = state.messages.toMutableMap()
            when (contradictionKey) {
                "carlos_marisol_relation" -> {
                    updatedMessages["suspect_carlos"] = (updatedMessages["suspect_carlos"] ?: emptyList()) + systemMsg
                    updatedMessages["suspect_marisol"] = (updatedMessages["suspect_marisol"] ?: emptyList()) + systemMsg
                }
                "carlos_valentina_relation" -> {
                    updatedMessages["suspect_carlos"] = (updatedMessages["suspect_carlos"] ?: emptyList()) + systemMsg
                    updatedMessages["suspect_valentina"] = (updatedMessages["suspect_valentina"] ?: emptyList()) + systemMsg
                }
            }

            val carlosSuspBonus = if (contradictionKey == "carlos_marisol_relation") 15 else 10
            val marisolSuspBonus = if (contradictionKey == "carlos_marisol_relation") 10 else 0
            val valentinaSuspBonus = if (contradictionKey == "carlos_valentina_relation") 15 else 0

            val carlosSusp = (state.suspicionLevels["suspect_carlos"] ?: 65) + carlosSuspBonus
            val marisolSusp = (state.suspicionLevels["suspect_marisol"] ?: 35) + marisolSuspBonus
            val valentinaSusp = (state.suspicionLevels["suspect_valentina"] ?: 40) + valentinaSuspBonus

            val updatedSuspicion = state.suspicionLevels + mapOf(
                "suspect_carlos" to carlosSusp.coerceAtMost(100),
                "suspect_marisol" to marisolSusp.coerceAtMost(100),
                "suspect_valentina" to valentinaSusp.coerceAtMost(100)
            )

            val compReveal = when (contradictionKey) {
                "carlos_marisol_relation" -> "[PISTA FALSA / DESPISTE] Un testigo afirma que Carlos Herrera y Marisol Mendoza se reunieron en secreto en una cafetería cercana al puerto la noche del crimen, discutiendo sobre el testamento de Don Aurelio."
                "carlos_valentina_relation" -> "[REVELACIÓN ADICIONAL] Valentina Ríos declaró formalmente ante los fiscales: 'Carlos Herrera me presionó para autorizar las transferencias a Logística del Caribe. Me dijo que era un acuerdo directo con Don Aurelio, pero descubrí que las firmas del jefe estaban falsificadas'."
                else -> null
            }

            val updatedExtraInfoComp = if (compReveal != null) {
                (state.extraInfoFound + compReveal).distinct()
            } else {
                state.extraInfoFound
            }

            val nextState = state.copy(
                currentCase = case.copy(suspects = updatedSuspects),
                discoveredContradictions = state.discoveredContradictions + contradictionKey,
                suspicionLevels = updatedSuspicion,
                messages = updatedMessages,
                score = state.score + 250,
                extraInfoFound = updatedExtraInfoComp,
                progress = state.progress.copy(
                    contradictionsFound = state.progress.contradictionsFound + 1
                )
            )

            return evaluatePhaseTransition(evaluateChainUnlocks(nextState))
        }

        fun pressSuspect(state: GameState, suspectId: String): GameState {
            val case = state.currentCase ?: return state
            if (suspectId != "suspect_carlos") return state

            val carlosContradictions = state.discoveredContradictions.filter { it.startsWith("carlos_") }
            val currentMsgs = state.messages[suspectId] ?: emptyList()

            if (carlosContradictions.size < 3) {
                val systemMsg = InterrogationMessage(
                    sender = "SISTEMA",
                    text = "El sospechoso se muestra firme. Necesitas descubrir al menos 3 contradicciones para quebrarlo.",
                    isDetective = false,
                    time = getCurrentTime()
                )
                return state.copy(
                    messages = state.messages + (suspectId to (currentMsgs + systemMsg))
                )
            }

            // Confession!
            val updatedSuspects = case.suspects.map { suspect ->
                if (suspect.id == "suspect_carlos") {
                    suspect.copy(trustLevel = 0)
                } else suspect
            }

            val updatedSuspicion = state.suspicionLevels + ("suspect_carlos" to 100)

            val confessionMsg = InterrogationMessage(
                sender = "Carlos Herrera",
                text = "Está bien... Sí conocía la empresa. Don Aurelio la fundó hace años, pero yo empecé a usarla para el contrabando. Él lo descubrió y... se volvió una amenaza para mi negocio. Fue un forcejeo, yo no quería matarlo, pero no tenía opción.",
                isDetective = false,
                time = getCurrentTime()
            )

            val systemMsg = InterrogationMessage(
                sender = "SISTEMA",
                text = "¡CONFESIÓN OBTENIDA! Carlos Herrera ha admitido el desvío de fondos y el homicidio involuntario de Don Aurelio Mendoza.",
                isDetective = false,
                time = getCurrentTime()
            )

            return state.copy(
                currentCase = case.copy(suspects = updatedSuspects),
                suspicionLevels = updatedSuspicion,
                messages = state.messages + (suspectId to (currentMsgs + confessionMsg + systemMsg)),
                hasPressedCarlos = true,
                score = state.score + 500
            )
        }

        fun forceCooperation(state: GameState, suspectId: String): GameState {
            val case = state.currentCase ?: return state
            val suspect = case.suspects.find { it.id == suspectId } ?: return state
            val currentMsgs = state.messages[suspectId] ?: emptyList()

            val penaltyPoints = 150
            val newScore = (state.score - penaltyPoints).coerceAtLeast(0)

            val updatedSuspects = case.suspects.map {
                if (it.id == suspectId) {
                    it.copy(trustLevel = 30)
                } else it
            }

            val systemMsg = InterrogationMessage(
                sender = "SISTEMA",
                text = "⚖ ORDEN JUDICIAL APLICADA — Has forzado la cooperación de ${suspect.name} mediante orden de la fiscalía. Penalización: -$penaltyPoints Puntos.",
                isDetective = false,
                time = getCurrentTime()
            )

            val updatedMessages = state.messages + (suspectId to (currentMsgs + systemMsg))

            return state.copy(
                currentCase = case.copy(suspects = updatedSuspects),
                score = newScore,
                messages = updatedMessages
            )
        }

        fun submitAccusation(state: GameState, suspectId: String, clueId: String): GameState {
            val case = state.currentCase ?: return state
            val guilty = case.suspects.find { it.id == case.solution.guiltySuspectId }
            val accused = case.suspects.find { it.id == suspectId }

            val carlosContradictions = state.discoveredContradictions.filter { it.startsWith("carlos_") }
            val hasEnoughContradictions = carlosContradictions.size >= 3
            val isCorrect = suspectId == case.solution.guiltySuspectId && 
                            clueId == case.solution.keyEvidenceId && 
                            hasEnoughContradictions

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
                "\n\nPistas no recuperadas durante la investigación: ${missingClues.joinToString { it.title }}."
            } else {
                "\n\n¡Excelente trabajo de campo! Lograste recuperar el 100% de las evidencias físicas del caso."
            }

            val epilogue = when {
                isCorrect -> {
                    val sb = java.lang.StringBuilder()
                    sb.append("¡CASO RESUELTO CON ÉXITO! Carlos Herrera fue detenido y condenado por homicidio agravado y contrabando aduanero masivo.\n\n")
                    sb.append("El tribunal de Barranquilla validó plenamente las pruebas presentadas. Tu labor de interrogatorio fue fundamental para estructurar el caso penal:\n\n")
                    
                    sb.append("⚖ DESMONTAJE DE LA COARTADA DE CARLOS:\n")
                    if (state.discoveredContradictions.contains("carlos_9pm")) {
                        sb.append("• Se expuso que mintió sobre su ubicación a las 9 PM. El video del CCTV del puerto captó su auto saliendo de las instalaciones en la hora exacta del crimen, destruyendo su defensa.\n")
                    } else {
                        sb.append("• Al no demostrar la contradicción de su horario de salida en el interrogatorio, la fiscalía tuvo dificultades para situarlo en la escena, basándose puramente en indicios circunstanciales.\n")
                    }
                    if (state.discoveredContradictions.contains("carlos_manifiesto")) {
                        sb.append("• Se probó la falsificación intelectual al confrontar su firma manuscrita directa en el Manifiesto de Importación.\n")
                    }
                    if (state.discoveredContradictions.contains("carlos_borrador")) {
                        sb.append("• El borrador de alteración de pesos recuperado en su oficina demostró que la operación ilegal venía planeándose con semanas de anticipación.\n")
                    }
                    
                    sb.append("\n⚖ DESMANTELAMIENTO DE LA RED DE CÓMPLICES:\n")
                    var accomplicesProved = 0
                    if (state.discoveredContradictions.contains("tomas_carlos") || state.discoveredContradictions.contains("tomas_camara")) {
                        sb.append("• Tomás Guerrero (Inspector): Condenado a 8 años de prisión por cohecho y prevaricato, tras probarse mediante tus careos que omitió las inspecciones a cambio de sobornos.\n")
                        accomplicesProved++
                    }
                    if (state.discoveredContradictions.contains("carlos_valentina_relation") || state.discoveredContradictions.contains("valentina_offshore") || state.discoveredContradictions.contains("valentina_seguro")) {
                        sb.append("• Valentina Ríos (Contadora): Procesada por lavado de activos tras evidenciarse que coordinaba con Carlos transferencias a una cuenta fantasma en Panamá y gestionó de forma fraudulenta el seguro de la carga.\n")
                        accomplicesProved++
                    }
                    if (state.discoveredContradictions.contains("carlos_marisol_relation") || state.discoveredContradictions.contains("marisol_carlos_contrato")) {
                        sb.append("• Marisol Mendoza (Hija de la víctima): Recibió una condena de ejecución condicional por complicidad pasiva al probarse el acuerdo del 20% de ganancias ilícitas a cambio de su silencio.\n")
                        accomplicesProved++
                    }
                    if (accomplicesProved == 0) {
                        sb.append("• No lograste vincular a ningún cómplice en el tribunal mediante careos directos. Aunque Carlos fue condenado, el inspector de aduanas, la contadora y la hija de la víctima evadieron cargos graves por falta de pruebas vinculantes.\n")
                    }

                    sb.append("\n⚖ VALOR DE LA EVIDENCIA DOCUMENTAL:\n")
                    sb.append("• La USB con correos (clue_usb) fue la prueba reina para demostrar el desvío de fondos aduaneros mediante la empresa fantasma 'Logística del Caribe'.\n")
                    if (state.hasSafeSecretLetter) {
                        sb.append("• La carta manuscrita por Don Aurelio obtenida en el estudio de abogados acreditó ante el juez la premeditación del crimen, demostrando que Don Aurelio iba a denunciar a Carlos al día siguiente de su muerte.")
                    } else {
                        sb.append("• Al no recuperar la carta manuscrita de la caja fuerte de los abogados, la premeditación del homicidio quedó en duda, catalogándose judicialmente como homicidio simple con agravantes.")
                    }
                    
                    sb.append(missingText)
                    sb.toString()
                }
                suspectId == "suspect_carlos" && clueId == "clue_usb" && !hasEnoughContradictions -> {
                    val sb = java.lang.StringBuilder()
                    sb.append("FALLO EN LA ACUSACIÓN: Aunque identificaste a Carlos Herrera como culpable y presentaste la USB con correos, la fiscalía desestimó el caso penal principal.\n\n")
                    sb.append("La defensa de Carlos sostuvo con éxito su coartada de que estuvo toda la noche en el almacén debido a que no descubriste suficientes contradicciones de Carlos durante los interrogatorios (se requieren al menos 3). Carlos Herrera quedó libre de cargos por duda razonable y el crimen de Don Aurelio quedó impune.")
                    sb.append(missingText)
                    sb.toString()
                }
                else -> {
                    val sb = java.lang.StringBuilder()
                    sb.append("FALLO EN LA INVESTIGACIÓN. Acusaste formalmente a ${accused?.name ?: "un sospechoso erróneo"} sin sustento probatorio contundente.\n\n")
                    sb.append("Las pruebas fueron declaradas insuficientes o impertinentes por el juez, ordenando su inmediata libertad. El verdadero culpable, ${guilty?.name ?: "Carlos Herrera"}, al percatarse del desvío de la investigación, vació las cuentas de la empresa y logró escapar del país con rumbo desconocido.")
                    sb.append(missingText)
                    sb.toString()
                }
            }

            val result = GameResult(
                isCorrect = isCorrect,
                accusedSuspectName = accused?.name ?: "Desconocido",
                actualGuiltyName = guilty?.name ?: "Desconocido",
                epilogue = epilogue,
                correctClueAssignments = correctAssignments,
                totalClues = case.clues.size,
                rank = rank,
                totalContradictions = 11,
                foundContradictions = state.progress.contradictionsFound,
                interrogatedCount = state.interrogatedSuspects.size,
                explorationCount = state.explorationCount,
                missingHints = missingClues.map { it.title },
                score = state.score
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
                    "clue_usb" -> state.explorationCount >= 3 || state.discoveredContradictions.contains("carlos_bancos")
                    "clue_contrato" -> state.clueAssignments.containsKey("clue_agenda") || state.discoveredContradictions.contains("marisol_carlos_contrato")
                    "clue_bancos" -> clue.isAvailable || state.discoveredContradictions.contains("carlos_manifiesto") || state.discoveredContradictions.contains("carlos_valentina_relation")
                    "clue_camara" -> clue.isAvailable || state.discoveredContradictions.contains("tomas_carlos")
                    else -> clue.isAvailable
                }
                clue.copy(isAvailable = available)
            }

            // Check USB questions injection
            val updatedSuspects = case.suspects.map { suspect ->
                if (suspect.id == "suspect_carlos") {
                    val hasUsb = updatedClues.any { it.id == "clue_usb" && it.isFound }
                    if (hasUsb) {
                        val usbQuestions = listOf(
                            Question(
                                id = "q_carlos_usb_1",
                                text = "¿Por qué coordinaba envíos con Tomás?",
                                effectType = "TRUST",
                                effectValue = "-20",
                                approach = "DIRECTO"
                            ),
                            Question(
                                id = "q_carlos_usb_2",
                                text = "¿Quién recibía el dinero?",
                                effectType = "TRUST",
                                effectValue = "-25",
                                approach = "DIRECTO"
                            ),
                            Question(
                                id = "q_carlos_usb_3",
                                text = "¿Don Aurelio descubrió el fraude?",
                                effectType = "CONTRADICTION",
                                effectValue = "carlos_usb_fraude",
                                approach = "PRESIÓN"
                            )
                        )
                        val currentIds = suspect.availableQuestions.map { it.id }.toSet()
                        val newQuestions = usbQuestions.filter { it.id !in currentIds }
                        suspect.copy(availableQuestions = suspect.availableQuestions + newQuestions)
                    } else suspect
                } else suspect
            }

            return state.copy(currentCase = case.copy(clues = updatedClues, suspects = updatedSuspects))
        }

        fun unlockAndDiscoverClue(state: GameState, clueId: String): GameState {
            val case = state.currentCase ?: return state
            val clue = case.clues.find { it.id == clueId } ?: return state
            if (clue.isFound) return state // already discovered

            val updatedClue = clue.copy(isFound = true, isAvailable = true)
            val updatedClues = case.clues.map { if (it.id == clueId) updatedClue else it }
            val newDiscovered = (state.discoveredClues + updatedClue).distinctBy { it.id }

            val nextState = state.copy(
                currentCase = case.copy(clues = updatedClues),
                discoveredClues = newDiscovered,
                progress = state.progress.copy(
                    discoveredClues = newDiscovered.size
                )
            )
            return evaluateChainUnlocks(nextState)
        }

        fun applyMinigameResult(state: GameState, locationName: String, result: String): GameState {
            val case = state.currentCase ?: return state
            val updatedMinigames = state.minigameResults + (locationName to result)
            var updatedState = state.copy(minigameResults = updatedMinigames)

            when (locationName) {
                "Almacén del Puerto" -> {
                    if (result == "WON") {
                        val updatedClues = case.clues.map {
                            if (it.id == "clue_manifiesto") it.copy(isFound = true, isAvailable = true)
                            else if (it.id == "clue_camara") it.copy(isAvailable = true)
                            else it
                        }
                        val extra = "El número de serie del contenedor aparece en facturas de otras 3 empresas distintas en los últimos 6 meses."
                        val manifiestoClue = case.clues.first { it.id == "clue_manifiesto" }.copy(isFound = true, isAvailable = true)
                        val newDiscovered = (updatedState.discoveredClues + manifiestoClue).distinctBy { it.id }
                        val newExploration = updatedState.explorationCount + 1
                        updatedState = updatedState.copy(
                            currentCase = case.copy(clues = updatedClues),
                            discoveredClues = newDiscovered,
                            explorationCount = newExploration,
                            extraInfoFound = (updatedState.extraInfoFound + extra).distinct(),
                            score = updatedState.score + 300,
                            progress = updatedState.progress.copy(
                                explorationCount = newExploration,
                                discoveredClues = newDiscovered.size
                            )
                        )
                    } else {
                        val unlockAt = updatedState.explorationCount + 2
                        updatedState = updatedState.copy(
                            blockedLocations = updatedState.blockedLocations + (locationName to unlockAt)
                        )
                    }
                }
                "Oficina de Importaciones Atlántico" -> {
                    if (result == "WON") {
                        val updatedClues = case.clues.map {
                            if (it.id == "clue_agenda") it.copy(isAvailable = true) else it
                        }
                        val extra = "Borrador de instrucciones de Don Aurelio en la basura: indica declarar peso doble para cubrir faltantes."
                        val updatedSuspects = case.suspects.map { suspect ->
                            if (suspect.id == "suspect_carlos") {
                                val exists = suspect.availableQuestions.any { it.id == "q_carlos_6" }
                                if (!exists) {
                                    val extraQ = Question(
                                        id = "q_carlos_6",
                                        text = "Encontramos un borrador con su letra en la basura.",
                                        effectType = "CONTRADICTION",
                                        effectValue = "el borrador prueba que dio la orden de falsificar pesos (dice que no lo escribió)",
                                        approach = "DIRECTO"
                                    )
                                    suspect.copy(availableQuestions = suspect.availableQuestions + extraQ)
                                } else suspect
                            } else suspect
                        }
                        updatedState = updatedState.copy(
                            currentCase = case.copy(suspects = updatedSuspects, clues = updatedClues),
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
                            if (it.id == "clue_camara") it.copy(isFound = true, isAvailable = true)
                            else if (it.id == "clue_usb") it.copy(isAvailable = true)
                            else it
                        }
                        val extra = "Tomás firmó 11 contenedores esa noche, excepto el 7-BETA, y cerró sesión 4 minutos después del despacho."
                        val camaraClue = case.clues.first { it.id == "clue_camara" }.copy(isFound = true, isAvailable = true)
                        val newDiscovered = (updatedState.discoveredClues + camaraClue).distinctBy { it.id }
                        updatedState = updatedState.copy(
                            currentCase = case.copy(clues = updatedClues),
                            discoveredClues = newDiscovered,
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
                            if (it.id == "clue_agenda") it.copy(isFound = true, isAvailable = true, description = "Agenda descifrada: Don Aurelio sospechaba de Carlos y su esquema con Logística del Caribe y Tomás. Menciona al abogado OR (Oscar Ríos).")
                            else if (it.id == "clue_contrato") it.copy(isAvailable = true)
                            else it
                        }
                        val extra = "El abogado Oscar Ríos (OR) confirma que Don Aurelio lo llamó esa noche con temor, acordando reunirse al día siguiente."
                        val agendaClue = case.clues.first { it.id == "clue_agenda" }.copy(isFound = true, isAvailable = true, description = "Agenda descifrada: Don Aurelio sospechaba de Carlos y su esquema con Logística del Caribe y Tomás. Menciona al abogado OR (Oscar Ríos).")
                        val newDiscovered = (updatedState.discoveredClues + agendaClue).distinctBy { it.id }
                        updatedState = updatedState.copy(
                            currentCase = case.copy(clues = updatedClues),
                            discoveredClues = newDiscovered,
                            extraInfoFound = (updatedState.extraInfoFound + extra).distinct(),
                            score = updatedState.score + 300
                        )
                    } else if (result == "PARTIAL") {
                        val updatedClues = case.clues.map {
                            if (it.id == "clue_agenda") it.copy(isFound = true, isAvailable = true, description = "Agenda descifrada: Don Aurelio sospechaba de Carlos y su esquema con Logística del Caribe y Tomás. Menciona al abogado OR (Oscar Ríos).") else it
                        }
                        val newDiscovered = (updatedState.discoveredClues + case.clues.first { it.id == "clue_agenda" }.copy(isFound = true, isAvailable = true, description = "Agenda descifrada: Don Aurelio sospechaba de Carlos y su esquema con Logística del Caribe y Tomás. Menciona al abogado OR (Oscar Ríos).")).distinctBy { it.id }
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
                            if (it.id == "clue_bancos") it.copy(isFound = true, isAvailable = true, description = "Registros bancarios completos: Muestran transacciones periódicas de dinero ilícito desde Logística del Caribe SAS a las cuentas de Carlos y Tomás. Flujo de $287,000 en 8 meses.")
                            else if (it.id == "clue_usb") it.copy(isFound = true, isAvailable = true)
                            else it
                        }
                        val extra = "Se detectó una transferencia de $40,000 hacia una cuenta anónima no identificada (posible implicación de Marisol)."
                        val banksClue = case.clues.first { it.id == "clue_bancos" }.copy(isFound = true, isAvailable = true, description = "Registros bancarios completos: Muestran transacciones periódicas de dinero ilícito desde Logística del Caribe SAS a las cuentas de Carlos y Tomás. Flujo de $287,000 en 8 meses.")
                        val usbClue = case.clues.first { it.id == "clue_usb" }.copy(isFound = true, isAvailable = true)
                        val newDiscovered = (updatedState.discoveredClues + listOf(banksClue, usbClue)).distinctBy { it.id }

                        val updatedSuspects = case.suspects.map { suspect ->
                            if (suspect.id == "suspect_valentina") {
                                val exists = suspect.availableQuestions.any { it.id == "q_valentina_6" }
                                if (!exists) {
                                    val extraQ = Question(
                                        id = "q_valentina_6",
                                        text = "¿Reconoce esta cuenta offshore?",
                                        effectType = "CONTRADICTION",
                                        effectValue = "la cuenta offshore de Panamá tiene transferencias que usted misma autorizó financieramente",
                                        approach = "TÉCNICO"
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
                        val contratoClue = case.clues.first { it.id == "clue_contrato" }.copy(isFound = true, isAvailable = true, description = "Contrato de ganancias: Documento privado que pacta el 20% para Marisol y contiene una carta adjunta de Don Aurelio delegando responsabilidad interna a Carlos.")
                        val newDiscovered = (updatedState.discoveredClues + contratoClue).distinctBy { it.id }
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

package com.example.obsidian

import com.example.obsidian.data.model.*
import org.junit.Assert.*
import org.junit.Test

class GameEngineTest {

    private fun createInitialState(): GameState {
        return GameState(
            currentCase = CaseRepository.laCargaDelSilencio,
            phase = GamePhase.INTRO
        )
    }

    @Test
    fun testInitialClueAvailability() {
        val state = createInitialState()
        val case = state.currentCase
        assertNotNull("Case should not be null", case)

        val manifiesto = case!!.clues.find { it.id == "clue_manifiesto" }
        assertNotNull("Manifiesto clue should exist", manifiesto)
        assertTrue("Manifiesto should be available at start", manifiesto!!.isAvailable)

        val otherClues = case.clues.filter { it.id != "clue_manifiesto" }
        for (clue in otherClues) {
            assertFalse("Clue ${clue.id} should NOT be available at start", clue.isAvailable)
        }
    }

    @Test
    fun testInterrogationUnlocks() {
        var state = createInitialState()

        state = GameEngine.endInterrogation(state, "suspect_carlos")
        val bancos = state.currentCase!!.clues.find { it.id == "clue_bancos" }
        assertTrue("clue_bancos should be available after Carlos interrogation", bancos!!.isAvailable)

        state = GameEngine.endInterrogation(state, "suspect_marisol")
        val agenda = state.currentCase!!.clues.find { it.id == "clue_agenda" }
        assertTrue("clue_agenda should be available after Marisol interrogation", agenda!!.isAvailable)

        state = GameEngine.endInterrogation(state, "suspect_tomas")
        val camara = state.currentCase!!.clues.find { it.id == "clue_camara" }
        assertTrue("clue_camara should be available after Tomás interrogation", camara!!.isAvailable)
    }

    @Test
    fun testInterrogationSessionLimits() {
        GameEngine.randomFloatForTest = { 0.0f }
        try {
            var state = createInitialState()
            state = GameEngine.startInterrogation(state, "suspect_carlos")

            state = GameEngine.askQuestion(state, "suspect_carlos", "q_carlos_1")
            assertEquals(1, state.interrogationSessions["suspect_carlos"]?.questionsThisRound)

            state = GameEngine.askQuestion(state, "suspect_carlos", "q_carlos_2")
            state = GameEngine.askQuestion(state, "suspect_carlos", "q_carlos_3")
            assertEquals(0, state.interrogationSessions["suspect_carlos"]?.questionsThisRound)

            assertFalse("Should not be marked interrogated until endInterrogation",
                state.interrogatedSuspects.contains("suspect_carlos"))

            state = GameEngine.endInterrogation(state, "suspect_carlos")
            assertTrue(state.interrogatedSuspects.contains("suspect_carlos"))
            assertNotNull(state.interrogationSummaries["suspect_carlos"])
        } finally {
            GameEngine.randomFloatForTest = null
        }
    }

    @Test
    fun testWarehouseMinigameExplorationCount() {
        var state = createInitialState()
        assertEquals(0, state.explorationCount)

        state = GameEngine.applyMinigameResult(state, "Almacén del Puerto", "WON")
        assertEquals("Winning warehouse should increment explorationCount", 1, state.explorationCount)
        assertTrue(state.discoveredClues.any { it.id == "clue_manifiesto" })

        var stateLost = createInitialState()
        stateLost = GameEngine.applyMinigameResult(stateLost, "Almacén del Puerto", "LOST")
        assertEquals("Losing warehouse should NOT increment explorationCount", 0, stateLost.explorationCount)
        assertTrue(stateLost.blockedLocations.containsKey("Almacén del Puerto"))
    }

    @Test
    fun testExplorationCountRule() {
        var state = createInitialState()
        assertEquals(0, state.explorationCount)

        // Try to explore a location without any available clue
        state = GameEngine.exploreLocation(state, "Fiscalía") // clue_bancos and clue_usb are not available yet
        assertEquals("Exploration count should NOT increment when no clue is found", 0, state.explorationCount)

        // Explore location with available clue (Almacén del Puerto -> clue_manifiesto)
        state = GameEngine.exploreLocation(state, "Almacén del Puerto")
        assertEquals("Exploration count should increment when clue is found", 1, state.explorationCount)
        assertTrue("Manifiesto clue should be found", state.currentCase!!.clues.find { it.id == "clue_manifiesto" }!!.isFound)

        // Try exploring same location again -> already found
        state = GameEngine.exploreLocation(state, "Almacén del Puerto")
        assertEquals("Exploration count should NOT increment for already found clues", 1, state.explorationCount)
    }

    @Test
    fun testExplorationCountUnlocksUsb() {
        var state = createInitialState()

        // Manually increment explorationCount to 3
        state = state.copy(explorationCount = 3)
        state = GameEngine.evaluateChainUnlocks(state)

        val usb = state.currentCase!!.clues.find { it.id == "clue_usb" }
        assertTrue("clue_usb should be available when explorationCount >= 3", usb!!.isAvailable)
    }

    @Test
    fun testAgendaLinkingUnlocksContrato() {
        var state = createInitialState()

        // Link clue_agenda to suspect_carlos
        state = GameEngine.linkClueToSuspect(state, "clue_agenda", "suspect_carlos")

        val contrato = state.currentCase!!.clues.find { it.id == "clue_contrato" }
        assertTrue("clue_contrato should be available after clue_agenda is linked", contrato!!.isAvailable)
    }

    @Test
    fun testValentinaQuestionConditionalEffect() {
        GameEngine.randomFloatForTest = { 0.0f }
        try {
            var stateA = createInitialState()
            val initialTrust = stateA.currentCase!!.suspects.find { it.id == "suspect_valentina" }!!.trustLevel

            stateA = GameEngine.startInterrogation(stateA, "suspect_valentina")
            stateA = GameEngine.askQuestion(stateA, "suspect_valentina", "q_valentina_1")
            stateA = GameEngine.askQuestion(stateA, "suspect_valentina", "q_valentina_2")
            stateA = GameEngine.askQuestion(stateA, "suspect_valentina", "q_valentina_3")
            stateA = GameEngine.askQuestion(stateA, "suspect_valentina", "q_valentina_4")
            val banksA = stateA.currentCase!!.clues.find { it.id == "clue_bancos" }
            val updatedTrustA = stateA.currentCase!!.suspects.find { it.id == "suspect_valentina" }!!.trustLevel

            assertFalse("clue_bancos should NOT unlock if Carlos was not interrogated", banksA!!.isAvailable)
            assertTrue("Valentina trust should decrease", updatedTrustA < initialTrust)

            var stateB = createInitialState()
            stateB = GameEngine.endInterrogation(stateB, "suspect_carlos")

            stateB = GameEngine.startInterrogation(stateB, "suspect_valentina")
            stateB = GameEngine.askQuestion(stateB, "suspect_valentina", "q_valentina_1")
            stateB = GameEngine.askQuestion(stateB, "suspect_valentina", "q_valentina_2")
            stateB = GameEngine.askQuestion(stateB, "suspect_valentina", "q_valentina_3")
            stateB = GameEngine.askQuestion(stateB, "suspect_valentina", "q_valentina_4")
            val banksB = stateB.currentCase!!.clues.find { it.id == "clue_bancos" }

            assertTrue("clue_bancos should unlock if Carlos was interrogated", banksB!!.isAvailable)
        } finally {
            GameEngine.randomFloatForTest = null
        }
    }

    @Test
    fun testSuspectTrustLock() {
        GameEngine.randomFloatForTest = { 0.0f }
        try {
            var state = createInitialState()

            val case = state.currentCase!!
            val lockedCarlos = case.suspects.find { it.id == "suspect_carlos" }!!.copy(trustLevel = 0)
            state = state.copy(
                currentCase = case.copy(
                    suspects = case.suspects.map { if (it.id == "suspect_carlos") lockedCarlos else it }
                )
            )

            state = GameEngine.startInterrogation(state, "suspect_carlos")
            val nextState = GameEngine.askQuestion(state, "suspect_carlos", "q_carlos_2")

            val finalCarlos = nextState.currentCase!!.suspects.find { it.id == "suspect_carlos" }!!
            assertEquals("Trust should remain 0", 0, finalCarlos.trustLevel)

            val lastMessage = nextState.messages["suspect_carlos"]?.last()
            assertNotNull(lastMessage)
            assertEquals("SISTEMA", lastMessage!!.sender)
            assertTrue(lastMessage.text.contains("se niega a cooperar"))
        } finally {
            GameEngine.randomFloatForTest = null
        }
    }

    @Test
    fun testPersonalityApproachCompatibility() {
        assertTrue(GameEngine.isApproachCompatibleWithPersonality("suspect_carlos", "EMPÁTICO"))
        assertFalse(GameEngine.isApproachCompatibleWithPersonality("suspect_carlos", "PRESIÓN"))
        assertTrue(GameEngine.isApproachCompatibleWithPersonality("suspect_valentina", "TÉCNICO"))
        assertFalse(GameEngine.isApproachCompatibleWithPersonality("suspect_valentina", "EMPÁTICO"))
        assertTrue(GameEngine.isApproachCompatibleWithPersonality("suspect_tomas", "DIRECTO"))
        assertFalse(GameEngine.isApproachCompatibleWithPersonality("suspect_tomas", "EMPÁTICO"))
        assertTrue(GameEngine.isApproachCompatibleWithPersonality("suspect_marisol", "EMPÁTICO"))
        assertFalse(GameEngine.isApproachCompatibleWithPersonality("suspect_marisol", "PRESIÓN"))

        GameEngine.randomFloatForTest = { 0.5f }
        try {
            var state = createInitialState()
            state = GameEngine.startInterrogation(state, "suspect_carlos")
            state = GameEngine.askQuestion(state, "suspect_carlos", "q_carlos_1")
            val afterEmpatico = state.currentCase!!.suspects.find { it.id == "suspect_carlos" }!!
            assertTrue(
                "Empathetic approach should reveal on nervous Carlos",
                afterEmpatico.contradictions.isNotEmpty()
            )

            state = GameEngine.startInterrogation(state, "suspect_valentina")
            state = GameEngine.askQuestion(state, "suspect_valentina", "q_valentina_7")
            val valentinaMsg = state.messages["suspect_valentina"]?.last()?.text.orEmpty()
            assertTrue(
                "Emotional approach should get cold evasive reply on Valentina",
                valentinaMsg.contains("sentimientos") || valentinaMsg.contains("libros")
            )
        } finally {
            GameEngine.randomFloatForTest = null
        }
    }

    @Test
    fun testContradictionRecording() {
        GameEngine.randomFloatForTest = { 0.0f }
        try {
            var state = createInitialState()
            state = GameEngine.startInterrogation(state, "suspect_carlos")
            state = GameEngine.askQuestion(state, "suspect_carlos", "q_carlos_1")

            val carlos = state.currentCase!!.suspects.find { it.id == "suspect_carlos" }!!
            assertTrue("Contradiction list should contain the contradiction details",
                carlos.contradictions.contains("su firma está en el manifiesto (dice no recordarlo)"))
            assertEquals(1, state.progress.contradictionsFound)
        } finally {
            GameEngine.randomFloatForTest = null
        }
    }

    @Test
    fun testPhaseTransitionRestrictions() {
        var state = createInitialState()
        
        // Try to navigate directly to ANALYZE_EVIDENCE with 0 interrogations
        var navigated = GameEngine.navigateToPhase(state, GamePhase.ANALYZE_EVIDENCE)
        assertEquals("Should block navigation to ANALYZE_EVIDENCE with < 2 interrogations", 
            GamePhase.INTRO, navigated.phase)

        // Interrogate Carlos
        state = GameEngine.endInterrogation(state, "suspect_carlos")
        // Try again with 1 interrogation
        navigated = GameEngine.navigateToPhase(state, GamePhase.ANALYZE_EVIDENCE)
        assertEquals("Should block navigation to ANALYZE_EVIDENCE with < 2 interrogations", 
            GamePhase.INTRO, navigated.phase)

        // Interrogate Valentina
        state = GameEngine.endInterrogation(state, "suspect_valentina")
        // Try again with 2 interrogations
        navigated = GameEngine.navigateToPhase(state, GamePhase.ANALYZE_EVIDENCE)
        assertEquals("Should allow navigation to ANALYZE_EVIDENCE with >= 2 interrogations", 
            GamePhase.ANALYZE_EVIDENCE, navigated.phase)

        // Try to navigate to ACCUSATION with 0 clues assigned
        navigated = GameEngine.navigateToPhase(navigated, GamePhase.ACCUSATION)
        assertEquals("Should block navigation to ACCUSATION with < 3 clues linked", 
            GamePhase.ANALYZE_EVIDENCE, navigated.phase)

        // Link 3 clues
        state = navigated
        state = GameEngine.linkClueToSuspect(state, "clue_manifiesto", "suspect_carlos")
        state = GameEngine.linkClueToSuspect(state, "clue_bancos", "suspect_carlos")
        state = GameEngine.linkClueToSuspect(state, "clue_usb", "suspect_carlos")

        // Try again to navigate to ACCUSATION
        navigated = GameEngine.navigateToPhase(state, GamePhase.ACCUSATION)
        assertEquals("Should allow navigation to ACCUSATION with >= 3 clues linked", 
            GamePhase.ACCUSATION, navigated.phase)
    }

    @Test
    fun testSubmitAccusationVerdict() {
        var state = createInitialState()
        
        // Link clues correctly and incorrectly
        state = GameEngine.linkClueToSuspect(state, "clue_manifiesto", "suspect_carlos") // correct
        state = GameEngine.linkClueToSuspect(state, "clue_bancos", "suspect_carlos") // correct
        state = GameEngine.linkClueToSuspect(state, "clue_usb", "suspect_carlos") // correct
        state = GameEngine.linkClueToSuspect(state, "clue_agenda", "suspect_marisol") // incorrect (should be carlos/marisol, wait: clue_agenda is linked to carlos & marisol, so it is correct!)
        
        // A: Correct accusation (requires 3 Carlos contradictions)
        state = state.copy(
            discoveredContradictions = setOf("carlos_manifiesto", "carlos_9pm", "carlos_borrador")
        )
        val finalStateCorrect = GameEngine.submitAccusation(state, "suspect_carlos", "clue_usb")
        assertEquals(GamePhase.VERDICT, finalStateCorrect.phase)
        val resultA = finalStateCorrect.gameResult
        assertNotNull(resultA)
        assertTrue(resultA!!.isCorrect)
        assertEquals("Carlos Herrera", resultA.accusedSuspectName)
        assertTrue(resultA.epilogue.contains("¡CASO RESUELTO CON ÉXITO!"))

        // B: Incorrect accusation
        val finalStateIncorrect = GameEngine.submitAccusation(state, "suspect_valentina", "clue_usb")
        val resultB = finalStateIncorrect.gameResult
        assertNotNull(resultB)
        assertFalse(resultB!!.isCorrect)
        assertEquals("Valentina Ríos", resultB.accusedSuspectName)
        assertTrue(resultB.epilogue.contains("FALLO EN LA INVESTIGACIÓN"))
    }
}

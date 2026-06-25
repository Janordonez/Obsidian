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

        // 1. Interrogating Carlos unlocks clue_bancos
        state = GameEngine.endInterrogation(state, "suspect_carlos")
        val bancos = state.currentCase!!.clues.find { it.id == "clue_bancos" }
        assertTrue("clue_bancos should be available after Carlos interrogation", bancos!!.isAvailable)

        // 2. Interrogating Marisol unlocks clue_agenda
        state = GameEngine.endInterrogation(state, "suspect_marisol")
        val agenda = state.currentCase!!.clues.find { it.id == "clue_agenda" }
        assertTrue("clue_agenda should be available after Marisol interrogation", agenda!!.isAvailable)

        // 3. Interrogating Tomás unlocks clue_camara
        state = GameEngine.endInterrogation(state, "suspect_tomas")
        val camara = state.currentCase!!.clues.find { it.id == "clue_camara" }
        assertTrue("clue_camara should be available after Tomás interrogation", camara!!.isAvailable)
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
        // CASE A: Carlos NOT interrogated before asking Valentina
        var stateA = createInitialState()
        val initialTrust = stateA.currentCase!!.suspects.find { it.id == "suspect_valentina" }!!.trustLevel

        stateA = GameEngine.askQuestion(stateA, "suspect_valentina", "q_valentina_4") // ask about Logística
        val banksA = stateA.currentCase!!.clues.find { it.id == "clue_bancos" }
        val updatedTrustA = stateA.currentCase!!.suspects.find { it.id == "suspect_valentina" }!!.trustLevel

        assertFalse("clue_bancos should NOT unlock if Carlos was not interrogated", banksA!!.isAvailable)
        assertTrue("Valentina trust should decrease", updatedTrustA < initialTrust)

        // CASE B: Carlos HAS been interrogated before asking Valentina
        var stateB = createInitialState()
        stateB = GameEngine.endInterrogation(stateB, "suspect_carlos") // Interrogate Carlos
        
        stateB = GameEngine.askQuestion(stateB, "suspect_valentina", "q_valentina_4") // ask about Logística
        val banksB = stateB.currentCase!!.clues.find { it.id == "clue_bancos" }
        
        assertTrue("clue_bancos should unlock if Carlos was interrogated", banksB!!.isAvailable)
    }

    @Test
    fun testSuspectTrustLock() {
        var state = createInitialState()
        
        // Force Carlos trust level to 0
        val case = state.currentCase!!
        val lockedCarlos = case.suspects.find { it.id == "suspect_carlos" }!!.copy(trustLevel = 0)
        state = state.copy(
            currentCase = case.copy(
                suspects = case.suspects.map { if (it.id == "suspect_carlos") lockedCarlos else it }
            )
        )

        // Ask question
        val nextState = GameEngine.askQuestion(state, "suspect_carlos", "q_carlos_2")
        
        // Verify no changes to trust or messages other than system lock notice
        val finalCarlos = nextState.currentCase!!.suspects.find { it.id == "suspect_carlos" }!!
        assertEquals("Trust should remain 0", 0, finalCarlos.trustLevel)
        
        val lastMessage = nextState.messages["suspect_carlos"]?.last()
        assertNotNull(lastMessage)
        assertEquals("SISTEMA", lastMessage!!.sender)
        assertTrue(lastMessage.text.contains("se niega a cooperar"))
    }

    @Test
    fun testContradictionRecording() {
        var state = createInitialState()
        
        state = GameEngine.askQuestion(state, "suspect_carlos", "q_carlos_1") // Contradiction: signature
        
        val carlos = state.currentCase!!.suspects.find { it.id == "suspect_carlos" }!!
        assertTrue("Contradiction list should contain the contradiction details", 
            carlos.contradictions.contains("su firma está en el manifiesto (dice no recordarlo)"))
        assertEquals(1, state.progress.contradictionsFound)
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
        
        // A: Correct accusation
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

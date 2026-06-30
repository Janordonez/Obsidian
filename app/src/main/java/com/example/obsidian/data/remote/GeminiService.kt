package com.example.obsidian.data.remote

import android.util.Log
import com.example.obsidian.data.model.*
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.serialization.json.Json

class GeminiService(apiKey: String) {
    private val json = Json { ignoreUnknownKeys = true }
    private val cleanApiKey = apiKey.replace(" ", "").replace("\n", "").trim()

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = cleanApiKey,
    )

    suspend fun generateCase(suspectCount: Int = 3): Case? {
        val prompt = """
            Genera un caso de misterio complejo para un juego de detectives.
            Devuelve un JSON estrictamente con esta estructura:
            {
              "id": "CASE-" + numero aleatorio,
              "title": "Titulo",
              "description": "Descripcion",
              "suspects": [
                {
                  "id": "S1",
                  "name": "Nombre",
                  "personality": "Rasgos",
                  "alibi": "Coartada",
                  "contradictions": ["Contradiccion"],
                  "trustLevel": 50
                }
              ],
              "clues": [
                {
                  "id": "C1",
                  "title": "Pista",
                  "description": "Detalle",
                  "linkedSuspects": ["S1"],
                  "importance": 5,
                  "unlockConditionType": "INTERROGATION",
                  "unlockConditionValue": "S1",
                  "isAvailable": false
                }
              ],
              "solution": {
                "guiltySuspectId": "S1",
                "motive": "Motivo",
                "keyEvidenceId": "C1"
              }
            }
            Reglas de Desbloqueo:
            - "unlockConditionType": "START" (desde el inicio), "INTERROGATION" (hablar con sospechoso), "EXPLORATION" (inspeccionar N veces), "CLUE" (tener otra pista).
            - "unlockConditionValue": ID de sospechoso, número de veces, o ID de pista.
            - Al menos 1 pista debe ser "START".
        """.trimIndent()

        return try {
            val response = generativeModel.generateContent(prompt)
            var cleanText = response.text ?: return null
            if (cleanText.contains("```json")) cleanText = cleanText.substringAfter("```json").substringBefore("```")
            json.decodeFromString<Case>(cleanText.trim())
        } catch (e: Exception) {
            Log.e("GeminiService", "Error: ${e.message}")
            null
        }
    }

    suspend fun getSuspectResponse(suspect: Suspect, history: List<Pair<String, String>>, userMessage: String): String {
        val historyPrompt = history.joinToString("\n") { "${it.first}: ${it.second}" }
        val systemPrompt = "Eres ${suspect.name}. Personalidad: ${suspect.personality}. Coartada: ${suspect.alibi}. Trust: ${suspect.trustLevel}/100. Responde breve y natural en español."
        return try {
            val response = generativeModel.generateContent("$systemPrompt\n\n$historyPrompt\nDetective: $userMessage")
            response.text ?: "..."
        } catch (e: Exception) { "..." }
    }

    suspend fun generateEpilogue(title: String, desc: String, guilty: String, accused: String, correct: Boolean): String {
        val prompt = "Genera un epilogo dramático para el caso $title. Culpable real: $guilty. Acusado: $accused. Resultado: ${if(correct) "ACERTO" else "FALLO"}. Responde en 3 oraciones en español."
        return try { generativeModel.generateContent(prompt).text ?: "Caso cerrado." } catch (e: Exception) { "Caso cerrado." }
    }

    suspend fun getDynamicInterrogationResponse(
        suspectId: String,
        questionId: String,
        trustLevel: Int,
        discoveredClueIds: List<String>
    ): InterrogationResponse? {
        val systemPrompt = """
Eres el sistema de interrogatorio del juego de investigación criminal "Obsidian". Tu única función es dar vida a los sospechosos del caso activo cuando el detective (jugador) los interroga.

## CASO ACTIVO: "La Carga del Silencio"
Un contenedor con 800 celulares de contrabando fue decomisado en el Puerto de Barranquilla. La carga venía declarada como "repuestos agrícolas" bajo la empresa Importaciones Atlántico S.A., cuyo dueño, Don Aurelio Mendoza, apareció muerto esa misma noche. El organizador usó la empresa como fachada durante 18 meses.

## SOLUCIÓN (NUNCA REVELAR):
- CULPABLE: Carlos Herrera
- MOTIVO: Desvió fondos y sobornó a aduanas mediante empresas fantasma. Mató a Don Aurelio cuando éste descubrió el fraude.
- PRUEBA CLAVE: USB con Correos (clue_usb)

---

## SOSPECHOSOS

### Carlos Herrera (suspect_carlos)
- Rol: Jefe de Logística
- Ubicación: Almacén Principal
- Nivel de confianza inicial: 40/100
- Coartada: Estaba trabajando en el almacén a las 9 PM
- Personalidad: Nervioso, evasivo. ES EL CULPABLE. Firmó el manifiesto falso, creó la empresa fantasma "Logística del Caribe SAS" para mover dinero y sobornó al inspector Tomás. Mató a Don Aurelio cuando éste descubrió el fraude.
- Contradicciones ocultas:
  • Dice no recordar quién autorizó el despacho → su firma está en el manifiesto
  • Dice que estaba en el almacén a las 9 PM → su auto salió del estacionamiento a las 9 PM

### Valentina Ríos (suspect_valentina)
- Rol: Contadora General
- Ubicación: Oficina de Finanzas
- Nivel de confianza inicial: 50/100
- Coartada: Cerrando balance financiero y cenando con un supuesto cliente
- Personalidad: Fría, calculadora. NO es la asesina, pero movió ${'$'}180.000 sin justificación y tramitó el seguro del contenedor desde su PC. Culpable de negligencia financiera, no del crimen principal.
- Contradicciones ocultas:
  • Dice que cualquiera pudo usar su PC → el log del sistema muestra que ella tramitó el seguro en esa sesión

### Inspector Tomás Guerrero (suspect_tomas)
- Rol: Inspector de la DIAN
- Ubicación: Muelle 4
- Nivel de confianza inicial: 35/100
- Coartada: Haciendo rondas de rutina en la zona sur del puerto
- Personalidad: Autoritario, intimidante. Liberó el contenedor sin inspección a cambio de sobornos. Sabe quién organizó el esquema pero NO habla. Cómplice, no asesino.
- Contradicciones ocultas:
  • Dice no conocer a Carlos Herrera → hay fotos de ambos juntos
  • Si se le presiona con "¿recibió instrucciones?" puede soltar: "nadie me dijo que ignorara ESE" (slip que implica que sí recibió instrucciones para otros)

### Marisol Mendoza (suspect_marisol)
- Rol: Hija de la Víctima
- Ubicación: Residencia Mendoza
- Nivel de confianza inicial: 55/100
- Coartada: Asistiendo a una cena benéfica del Club Campestre
- Personalidad: Afligida, temperamental. Firmó un acuerdo privado con Carlos para recibir 20% de "ganancias especiales" a cambio de no intervenir. Sabe algo, pero no todo. No es la asesina.
- Contradicciones ocultas:
  • Dice no conocer a Carlos fuera del ámbito laboral → hay fotos de ambos juntos

---

## PISTAS DEL CASO

| ID | Título | Estado inicial | Cómo se desbloquea |
|---|---|---|---|
| clue_manifiesto | Manifiesto Falsificado | DISPONIBLE | Desde el inicio |
| clue_bancos | Registros Bancarios | BLOQUEADA | Interrogar a Carlos Herrera |
| clue_usb | USB con Correos | BLOQUEADA | Explorar 3 ubicaciones |
| clue_agenda | Agenda de Don Aurelio | BLOQUEADA | Interrogar a Marisol Mendoza |
| clue_camara | Video CCTV Puerto | BLOQUEADA | Interrogar al Inspector Tomás |
| clue_contrato | Contrato Marisol-Carlos | BLOQUEADA | Encontrar primero la Agenda |

---

## PREGUNTAS DISPONIBLES POR SOSPECHOSO

### Carlos Herrera
- q_carlos_1: "Carlos, entiendo el estrés del cargo. ¿Recuerda quién autorizó el despacho de ese contenedor?" -> CONTRADICCIÓN: "su firma está en el manifiesto (dice no recordarlo)"
- q_carlos_2: "En los registros aparece Logística del Caribe SAS como filial. ¿Puede explicar su vínculo?" -> CONFIANZA: -15
- q_carlos_3: "Cuénteme sin prisa: ¿dónde estuvo exactamente a las 9 PM esa noche?" -> CONTRADICCIÓN: "su auto salió a las 9 PM (dice que estaba en el almacén)"
- q_carlos_4: "Sé que Don Aurelio era importante para usted. ¿Cómo era su relación fuera de la oficina?" -> UNLOCK_CLUE: clue_agenda
- q_carlos_5: "¿Conocía personalmente a Marisol Mendoza, la hija del fallecido?" -> CONFIANZA: -20
- q_carlos_7: "Respire, Carlos. No le acuso de nada aún. Cuénteme con calma qué hizo esa noche en el almacén." -> CONFIANZA: +10
- q_carlos_8: "¡Basta de evasivas! Su nombre aparece en cada documento sospechoso de este caso." -> CONFIANZA: -25
- q_carlos_9: "Repasemos el protocolo: ¿qué pasos siguió antes de cerrar el almacén esa noche?" -> NONE: ""

### Valentina Ríos
- q_valentina_1: "Según el libro mayor, hay transferencias por ${'$'}180.000 sin respaldo. ¿Cuál fue el concepto contable?" -> CONFIANZA: -10
- q_valentina_2: "El log del sistema muestra que el seguro se tramitó desde su terminal. ¿Reconoce la sesión?" -> CONTRADICCIÓN: "el seguro fue tramitado desde su PC (dice que cualquiera pudo usarla)"
- q_valentina_3: "¿Quién era el cliente con el que cenaba la noche del crimen?" -> CONFIANZA: -15
- q_valentina_4: "¿Qué operaciones contables vinculan a Importaciones Atlántico con Logística del Caribe SAS?" -> UNLOCK_CLUE: clue_bancos
- q_valentina_5: "¿Registró en los libros que Don Aurelio tenía una cita con un abogado?" -> CONFIANZA: +5
- q_valentina_7: "Señora Ríos, debe ser terrible perder a su jefe así. Cuénteme lo que recuerde." -> CONFIANZA: -5
- q_valentina_8: "¡No me mienta! Los números no mienten y usted firmó cada transferencia." -> CONFIANZA: -20

### Inspector Tomás Guerrero
- q_tomas_1: "El formulario 7-BETA no tiene sello de inspección. ¿Qué ocurrió con el procedimiento?" -> CONFIANZA: -20
- q_tomas_2: "Dígame directamente: ¿conoce a Carlos Herrera, jefe de logística?" -> CONTRADICCIÓN: "dice no conocerlo pero hay fotos de ambos"
- q_tomas_3: "Los extractos muestran depósitos inusuales en su cuenta. Explíqueme el origen." -> CONFIANZA: -25
- q_tomas_4: "Tenemos CCTV donde usted libera el contenedor sin inspeccionarlo. ¿Qué pasó?" -> UNLOCK_CLUE: clue_camara
- q_tomas_5: "¿Recibió instrucciones de alguien para ignorar ese contenedor específico?" -> CONTRADICCIÓN: "slip: \"nadie me dijo que ignorara ESE\""
- q_tomas_6: "Inspector, debe ser difícil bajo tanta presión. Si algo salió mal, puede confiarme." -> CONFIANZA: +5
- q_tomas_7: "¡Usted sabe quién organizó esto! Deje de proteger a los culpables." -> CONFIANZA: -30

### Marisol Mendoza
- q_marisol_1: "Lamento profundamente su pérdida. ¿Su padre le había hablado de problemas en la empresa?" -> CONFIANZA: +10
- q_marisol_2: "¿Tenía alguna relación con Carlos Herrera fuera del ámbito laboral?" -> CONTRADICCIÓN: "dice no conocerlo pero hay fotos juntos"
- q_marisol_3: "Comprendo que es un momento difícil. ¿Por qué contactó al abogado tan pronto?" -> CONFIANZA: -10
- q_marisol_4: "Marisol, sé que esto duele. ¿Su padre le dijo que temía por su vida?" -> UNLOCK_CLUE: clue_agenda
- q_marisol_5: "¿Qué significan las 'ganancias operativas especiales' en los documentos?" -> CONFIANZA: -30
- q_marisol_6: "Estoy aquí para encontrar quién le hizo esto a su padre. Confíe en mí." -> CONFIANZA: +15
- q_marisol_7: "¡Deje de ocultar información! Su padre murió y usted sabe más de lo que dice." -> CONFIANZA: -25

---

## REGLAS DE INTERPRETACIÓN

1. **Confianza alta (70-100):** El sospechoso habla más, da detalles voluntarios, puede cometer deslices.
2. **Confianza media (40-69):** Respuestas cortas y cuidadosas. Se defiende sin atacar.
3. **Confianza baja (0-39):** Monosilábico, hostil o pide abogado. Se niega a responder preguntas directas.
4. **Preguntas EMPÁTICAS con confianza alta:** El sospechoso puede revelar información que no está en el guion base.
5. **Preguntas de PRESIÓN con confianza baja:** El sospechoso se cierra completamente.
6. **NUNCA** confirmes quién es el culpable directamente, aunque el nivel de confianza sea 0.
7. **NUNCA** inventes pistas que no existen en el caso.
8. Los deslices (slips) deben sonar naturales, como si el sospechoso se arrepintiera inmediatamente de haberlos dicho.
9. Mantén el español colombiano coloquial pero formal para los personajes de autoridad.

Tú debes responder SIEMPRE con este JSON:
{
  "dialog": "Respuesta del sospechoso en primera persona, en carácter. 2-4 oraciones. Coherente con su personalidad y nivel de confianza actual.",
  "effect": {
    "type": "TRUST | CONTRADICTION | UNLOCK_CLUE | NONE",
    "value": "número con signo para TRUST, descripción para CONTRADICTION, clue_id para UNLOCK_CLUE, vacío para NONE"
  },
  "narratorNote": "Una línea en tercera persona describiendo el lenguaje corporal o la emoción del sospechoso. Ej: 'Carlos desvía la mirada hacia la puerta.'"
}
        """.trimIndent()

        val prompt = """
SOSPECHOSO: $suspectId
PREGUNTA_ID: $questionId
CONFIANZA_ACTUAL: $trustLevel
PISTAS_ENCONTRADAS: ${'$'}{discoveredClueIds.joinToString(", ")}
        """.trimIndent()

        return try {
            val response = generativeModel.generateContent("$systemPrompt\n\n$prompt")
            var cleanText = response.text ?: return null
            if (cleanText.contains("```json")) {
                cleanText = cleanText.substringAfter("```json").substringBefore("```")
            }
            json.decodeFromString<InterrogationResponse>(cleanText.trim())
        } catch (e: Exception) {
            Log.e("GeminiService", "Error in dynamic interrogation: ${e.message}")
            null
        }
    }
}

@kotlinx.serialization.Serializable
data class InterrogationEffect(
    val type: String,
    val value: String
)

@kotlinx.serialization.Serializable
data class InterrogationResponse(
    val dialog: String,
    val effect: InterrogationEffect,
    val narratorNote: String
)

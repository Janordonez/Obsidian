package com.example.obsidian.data.model

/**
 * Repositorio del caso "La Carga del Silencio".
 * Contiene la definición estática de la investigación, incluyendo los sospechosos,
 * sus preguntas disponibles, las pistas y la solución correcta.
 */
object CaseRepository {

    val laCargaDelSilencio = Case(
        id = "case_carga_silencio",
        title = "La Carga del Silencio",
        description = "Un contenedor con 800 celulares de contrabando fue decomisado en el Puerto de Barranquilla. La carga venía declarada como 'repuestos agrícolas' bajo la empresa Importaciones Atlántico S.A., de Don Aurelio Mendoza, quien apareció muerto esa misma noche. El organizador del esquema usó la empresa como fachada durante 18 meses.",
        suspects = listOf(
            Suspect(
                id = "suspect_carlos",
                name = "Carlos Herrera",
                gender = "MALE",
                personality = "Jefe de Logística. Nervioso, evasivo. Firmó el manifiesto falso, creó la empresa fantasma Logística del Caribe SAS para mover dinero y sobornó al inspector. Mató a Don Aurelio cuando este descubrió el fraude.",
                alibi = "Estaba trabajando en el almacén de la empresa a las 9 PM.",
                contradictions = emptyList(),
                trustLevel = 40,
                relation = "Jefe de Logística de la empresa",
                room = "Almacén Principal",
                availableQuestions = listOf(
                    Question(
                        id = "q_carlos_1",
                        text = "¿Quién autorizó el despacho?",
                        effectType = "CONTRADICTION",
                        effectValue = "su firma está en el manifiesto (dice no recordarlo)"
                    ),
                    Question(
                        id = "q_carlos_2",
                        text = "¿Conoce Logística del Caribe SAS?",
                        effectType = "TRUST",
                        effectValue = "-15"
                    ),
                    Question(
                        id = "q_carlos_3",
                        text = "¿Dónde estaba a las 9 PM?",
                        effectType = "CONTRADICTION",
                        effectValue = "su auto salió a las 9PM (dice que estaba en el almacén)"
                    ),
                    Question(
                        id = "q_carlos_4",
                        text = "¿Relación con Don Aurelio fuera de la empresa?",
                        effectType = "UNLOCK_CLUE",
                        effectValue = "clue_agenda"
                    ),
                    Question(
                        id = "q_carlos_5",
                        text = "¿Conoce a Marisol personalmente?",
                        effectType = "TRUST",
                        effectValue = "-20"
                    )
                )
            ),
            Suspect(
                id = "suspect_valentina",
                name = "Valentina Ríos",
                gender = "FEMALE",
                personality = "Contadora. Fría, calculadora. Movió $180,000 sin justificación y tramitó el seguro del contenedor desde su PC, pero no sabía del contrabando. Es culpable de negligencia financiera, no del crimen principal.",
                alibi = "Cerrando balance financiero y cenando con un supuesto cliente.",
                contradictions = emptyList(),
                trustLevel = 50,
                relation = "Contadora General",
                room = "Oficina de Finanzas",
                availableQuestions = listOf(
                    Question(
                        id = "q_valentina_1",
                        text = "Explique las transferencias de $180,000",
                        effectType = "TRUST",
                        effectValue = "-10"
                    ),
                    Question(
                        id = "q_valentina_2",
                        text = "¿Quién tramitó el seguro desde su PC?",
                        effectType = "CONTRADICTION",
                        effectValue = "el seguro fue tramitado desde su PC (dice que cualquiera pudo usarla)"
                    ),
                    Question(
                        id = "q_valentina_3",
                        text = "¿Quién es el cliente de la cena?",
                        effectType = "TRUST",
                        effectValue = "-15"
                    ),
                    Question(
                        id = "q_valentina_4",
                        text = "¿Sabe qué es Logística del Caribe SAS?",
                        effectType = "UNLOCK_CLUE",
                        effectValue = "clue_bancos"
                    ),
                    Question(
                        id = "q_valentina_5",
                        text = "¿Sabía que Don Aurelio planeaba ver a un abogado?",
                        effectType = "TRUST",
                        effectValue = "5"
                    )
                )
            ),
            Suspect(
                id = "suspect_tomas",
                name = "Inspector Tomás Guerrero",
                gender = "MALE",
                personality = "Inspector de Aduanas. Autoritario, intimidante. Liberó el contenedor sin inspección a cambio de sobornos. Sabe quién organizó el esquema pero no habla.",
                alibi = "Haciendo rondas de rutina en la zona sur del puerto.",
                contradictions = emptyList(),
                trustLevel = 35,
                relation = "Inspector de la DIAN",
                room = "Muelle 4",
                availableQuestions = listOf(
                    Question(
                        id = "q_tomas_1",
                        text = "¿Por qué no hay registro de inspección?",
                        effectType = "TRUST",
                        effectValue = "-20"
                    ),
                    Question(
                        id = "q_tomas_2",
                        text = "¿Conoce a Carlos Herrera personalmente?",
                        effectType = "CONTRADICTION",
                        effectValue = "dice no conocer a Carlos Herrera pero hay fotos de ambos"
                    ),
                    Question(
                        id = "q_tomas_3",
                        text = "Explique los depósitos en su cuenta",
                        effectType = "TRUST",
                        effectValue = "-25"
                    ),
                    Question(
                        id = "q_tomas_4",
                        text = "Tenemos video de usted liberando el contenedor",
                        effectType = "UNLOCK_CLUE",
                        effectValue = "clue_camara"
                    ),
                    Question(
                        id = "q_tomas_5",
                        text = "¿Alguien le pidió ignorar ese contenedor?",
                        effectType = "CONTRADICTION",
                        effectValue = "slip: 'nadie me dijo que ignorara ESE'"
                    )
                )
            ),
            Suspect(
                id = "suspect_marisol",
                name = "Marisol Mendoza",
                gender = "FEMALE",
                personality = "Hija heredera de Don Aurelio. Afligida, temperamental. Firmó un acuerdo privado con Carlos para recibir 20% de 'ganancias especiales' a cambio de no intervenir. Sabe algo, pero no todo.",
                alibi = "Asistiendo a una cena benéfica del Club Campestre.",
                contradictions = emptyList(),
                trustLevel = 55,
                relation = "Hija de la Víctima",
                room = "Residencia Mendoza",
                availableQuestions = listOf(
                    Question(
                        id = "q_marisol_1",
                        text = "¿Sabía su padre que la empresa tenía problemas?",
                        effectType = "TRUST",
                        effectValue = "10"
                    ),
                    Question(
                        id = "q_marisol_2",
                        text = "¿Conoce a Carlos fuera del trabajo?",
                        effectType = "CONTRADICTION",
                        effectValue = "dice no conocer a Carlos pero hay fotos juntos"
                    ),
                    Question(
                        id = "q_marisol_3",
                        text = "¿Por qué contactó al abogado tan pronto?",
                        effectType = "TRUST",
                        effectValue = "-10"
                    ),
                    Question(
                        id = "q_marisol_4",
                        text = "¿Su padre le mencionó tener miedo de alguien?",
                        effectType = "UNLOCK_CLUE",
                        effectValue = "clue_agenda"
                    ),
                    Question(
                        id = "q_marisol_5",
                        text = "¿Qué son las ganancias operativas especiales?",
                        effectType = "TRUST",
                        effectValue = "-30"
                    )
                )
            )
        ),
        clues = listOf(
            Clue(
                id = "clue_manifiesto",
                title = "Manifiesto Falsificado",
                description = "El manifiesto de importación del contenedor donde se declaraban repuestos agrícolas en vez de celulares. Tiene la firma de Carlos Herrera.",
                linkedSuspects = listOf("suspect_carlos", "suspect_valentina"),
                importance = 50,
                locationName = "Almacén del Puerto",
                isAvailable = true,
                isFound = false,
                unlockConditionType = "START",
                unlockConditionValue = ""
            ),
            Clue(
                id = "clue_bancos",
                title = "Registros Bancarios",
                description = "Extractos bancarios que revelan transferencias periódicas de dinero ilícito desde Logística del Caribe SAS a las cuentas de Carlos y el inspector Tomás.",
                linkedSuspects = listOf("suspect_tomas", "suspect_carlos"),
                importance = 80,
                locationName = "Fiscalía",
                isAvailable = false,
                isFound = false,
                unlockConditionType = "INTERROGATION",
                unlockConditionValue = "suspect_carlos"
            ),
            Clue(
                id = "clue_usb",
                title = "USB con Correos",
                description = "Memoria USB con correos encriptados entre Carlos Herrera y Tomás Guerrero planeando el envío del contrabando y cómo evadir aduanas.",
                linkedSuspects = listOf("suspect_carlos", "suspect_tomas"),
                importance = 100,
                locationName = "Fiscalía",
                isAvailable = false,
                isFound = false,
                unlockConditionType = "EXPLORATION",
                unlockConditionValue = "3"
            ),
            Clue(
                id = "clue_agenda",
                title = "Agenda de Don Aurelio",
                description = "Agenda personal con notas manuscritas sobre su sospecha de fraude y de que Carlos lo estaba utilizando como testaferro.",
                linkedSuspects = listOf("suspect_carlos", "suspect_marisol"),
                importance = 70,
                locationName = "Residencia Mendoza",
                isAvailable = false,
                isFound = false,
                unlockConditionType = "INTERROGATION",
                unlockConditionValue = "suspect_marisol"
            ),
            Clue(
                id = "clue_camara",
                title = "Video CCTV Puerto",
                description = "Grabación de la cámara de seguridad del puerto que capta al Inspector Tomás Guerrero validando el contenedor de contrabando sin revisarlo.",
                linkedSuspects = listOf("suspect_tomas"),
                importance = 60,
                locationName = "Caseta de Aduanas",
                isAvailable = false,
                isFound = false,
                unlockConditionType = "INTERROGATION",
                unlockConditionValue = "suspect_tomas"
            ),
            Clue(
                id = "clue_contrato",
                title = "Contrato Marisol-Carlos",
                description = "Documento privado donde se pacta que Marisol recibirá un 20% de ganancias operativas especiales de Importaciones Atlántico.",
                linkedSuspects = listOf("suspect_marisol", "suspect_carlos"),
                importance = 90,
                locationName = "Estudio de Abogados",
                isAvailable = false,
                isFound = false,
                unlockConditionType = "CLUE",
                unlockConditionValue = "clue_agenda"
            )
        ),
        solution = Solution(
            guiltySuspectId = "suspect_carlos",
            motive = "Carlos desvió fondos y sobornó a aduanas mediante empresas fantasma. Mató a Don Aurelio Mendoza cuando éste descubrió todo el fraude.",
            keyEvidenceId = "clue_usb"
        )
    )
}

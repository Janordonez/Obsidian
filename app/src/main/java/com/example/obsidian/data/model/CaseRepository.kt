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
                        text = "Carlos, entiendo el estrés del cargo. ¿Recuerda quién autorizó el despacho de ese contenedor?",
                        effectType = "CONTRADICTION",
                        effectValue = "su firma está en el manifiesto (dice no recordarlo)",
                        coherence = 50,
                        topic = "logística portuaria",
                        approach = "EMPÁTICO"
                    ),
                    Question(
                        id = "q_carlos_2",
                        text = "En los registros aparece Logística del Caribe SAS como filial. ¿Puede explicar su vínculo?",
                        effectType = "TRUST",
                        effectValue = "-15",
                        coherence = 50,
                        topic = "operaciones financieras",
                        approach = "TÉCNICO"
                    ),
                    Question(
                        id = "q_carlos_3",
                        text = "Cuénteme sin prisa: ¿dónde estuvo exactamente a las 9 PM esa noche?",
                        effectType = "CONTRADICTION",
                        effectValue = "su auto salió a las 9PM (dice que estaba en el almacén)",
                        coherence = 50,
                        topic = "su coartada",
                        approach = "EMPÁTICO"
                    ),
                    Question(
                        id = "q_carlos_4",
                        text = "Sé que Don Aurelio era importante para usted. ¿Cómo era su relación fuera de la oficina?",
                        effectType = "UNLOCK_CLUE",
                        effectValue = "clue_agenda",
                        coherence = 50,
                        topic = "relaciones personales",
                        approach = "EMPÁTICO"
                    ),
                    Question(
                        id = "q_carlos_5",
                        text = "¿Conocía personalmente a Marisol Mendoza, la hija del fallecido?",
                        effectType = "TRUST",
                        effectValue = "-20",
                        coherence = 50,
                        topic = "relaciones personales",
                        approach = "DIRECTO"
                    ),
                    Question(
                        id = "q_carlos_7",
                        text = "Respire, Carlos. No le acuso de nada aún. Cuénteme con calma qué hizo esa noche en el almacén.",
                        effectType = "TRUST",
                        effectValue = "10",
                        coherence = 50,
                        topic = "su coartada",
                        approach = "EMPÁTICO"
                    ),
                    Question(
                        id = "q_carlos_8",
                        text = "¡Basta de evasivas! Su nombre aparece en cada documento sospechoso de este caso.",
                        effectType = "TRUST",
                        effectValue = "-25",
                        coherence = 50,
                        topic = "logística portuaria",
                        approach = "PRESIÓN"
                    ),
                    Question(
                        id = "q_carlos_9",
                        text = "Repasemos el protocolo: ¿qué pasos siguió antes de cerrar el almacén esa noche?",
                        effectType = "NONE",
                        effectValue = "",
                        coherence = 50,
                        topic = "logística portuaria",
                        approach = "TÉCNICO"
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
                        text = "Según el libro mayor, hay transferencias por $180,000 sin respaldo. ¿Cuál fue el concepto contable?",
                        effectType = "TRUST",
                        effectValue = "-10",
                        coherence = 50,
                        topic = "transacciones sospechosas",
                        approach = "TÉCNICO"
                    ),
                    Question(
                        id = "q_valentina_2",
                        text = "El log del sistema muestra que el seguro se tramitó desde su terminal. ¿Reconoce la sesión?",
                        effectType = "CONTRADICTION",
                        effectValue = "el seguro fue tramitado desde su PC (dice que cualquiera pudo usarla)",
                        coherence = 50,
                        topic = "contabilidad",
                        approach = "TÉCNICO"
                    ),
                    Question(
                        id = "q_valentina_3",
                        text = "¿Quién era el cliente con el que cenaba la noche del crimen?",
                        effectType = "TRUST",
                        effectValue = "-15",
                        coherence = 50,
                        topic = "su coartada",
                        approach = "DIRECTO"
                    ),
                    Question(
                        id = "q_valentina_4",
                        text = "¿Qué operaciones contables vinculan a Importaciones Atlántico con Logística del Caribe SAS?",
                        effectType = "UNLOCK_CLUE",
                        effectValue = "clue_bancos",
                        coherence = 50,
                        topic = "conexiones empresariales",
                        approach = "TÉCNICO"
                    ),
                    Question(
                        id = "q_valentina_5",
                        text = "¿Registró en los libros que Don Aurelio tenía una cita con un abogado?",
                        effectType = "TRUST",
                        effectValue = "5",
                        coherence = 50,
                        topic = "conexiones empresariales",
                        approach = "DIRECTO"
                    ),
                    Question(
                        id = "q_valentina_7",
                        text = "Señora Ríos, debe ser terrible perder a su jefe así. Cuénteme lo que recuerde.",
                        effectType = "TRUST",
                        effectValue = "-5",
                        coherence = 50,
                        topic = "contabilidad",
                        approach = "EMPÁTICO"
                    ),
                    Question(
                        id = "q_valentina_8",
                        text = "¡No me mienta! Los números no mienten y usted firmó cada transferencia.",
                        effectType = "TRUST",
                        effectValue = "-20",
                        coherence = 50,
                        topic = "transacciones sospechosas",
                        approach = "PRESIÓN"
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
                        text = "El formulario 7-BETA no tiene sello de inspección. ¿Qué ocurrió con el procedimiento?",
                        effectType = "TRUST",
                        effectValue = "-20",
                        coherence = 50,
                        topic = "procedimientos aduaneros",
                        approach = "TÉCNICO"
                    ),
                    Question(
                        id = "q_tomas_2",
                        text = "Dígame directamente: ¿conoce a Carlos Herrera, jefe de logística?",
                        effectType = "CONTRADICTION",
                        effectValue = "dice no conocer a Carlos Herrera pero hay fotos de ambos",
                        coherence = 50,
                        topic = "relaciones con logística",
                        approach = "DIRECTO"
                    ),
                    Question(
                        id = "q_tomas_3",
                        text = "Los extractos muestran depósitos inusuales en su cuenta. Explíqueme el origen.",
                        effectType = "TRUST",
                        effectValue = "-25",
                        coherence = 50,
                        topic = "movimientos bancarios",
                        approach = "DIRECTO"
                    ),
                    Question(
                        id = "q_tomas_4",
                        text = "Tenemos CCTV donde usted libera el contenedor sin inspeccionarlo. ¿Qué pasó?",
                        effectType = "UNLOCK_CLUE",
                        effectValue = "clue_camara",
                        coherence = 50,
                        topic = "inspecciones",
                        approach = "DIRECTO"
                    ),
                    Question(
                        id = "q_tomas_5",
                        text = "¿Recibió instrucciones de alguien para ignorar ese contenedor específico?",
                        effectType = "CONTRADICTION",
                        effectValue = "slip: 'nadie me dijo que ignorara ESE'",
                        coherence = 50,
                        topic = "procedimientos aduaneros",
                        approach = "DIRECTO"
                    ),
                    Question(
                        id = "q_tomas_6",
                        text = "Inspector, debe ser difícil bajo tanta presión. Si algo salió mal, puede confiarme.",
                        effectType = "TRUST",
                        effectValue = "5",
                        coherence = 50,
                        topic = "procedimientos aduaneros",
                        approach = "EMPÁTICO"
                    ),
                    Question(
                        id = "q_tomas_7",
                        text = "¡Usted sabe quién organizó esto! Deje de proteger a los culpables.",
                        effectType = "TRUST",
                        effectValue = "-30",
                        coherence = 50,
                        topic = "relaciones con logística",
                        approach = "PRESIÓN"
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
                        text = "Lamento profundamente su pérdida. ¿Su padre le había hablado de problemas en la empresa?",
                        effectType = "TRUST",
                        effectValue = "10",
                        coherence = 50,
                        topic = "su padre",
                        approach = "EMPÁTICO"
                    ),
                    Question(
                        id = "q_marisol_2",
                        text = "¿Tenía alguna relación con Carlos Herrera fuera del ámbito laboral?",
                        effectType = "CONTRADICTION",
                        effectValue = "dice no conocer a Carlos pero hay fotos juntos",
                        coherence = 50,
                        topic = "relaciones con empleados",
                        approach = "DIRECTO"
                    ),
                    Question(
                        id = "q_marisol_3",
                        text = "Comprendo que es un momento difícil. ¿Por qué contactó al abogado tan pronto?",
                        effectType = "TRUST",
                        effectValue = "-10",
                        coherence = 50,
                        topic = "herencia y patrimonio",
                        approach = "EMPÁTICO"
                    ),
                    Question(
                        id = "q_marisol_4",
                        text = "Marisol, sé que esto duele. ¿Su padre le dijo que temía por su vida?",
                        effectType = "UNLOCK_CLUE",
                        effectValue = "clue_agenda",
                        coherence = 50,
                        topic = "su padre",
                        approach = "EMPÁTICO"
                    ),
                    Question(
                        id = "q_marisol_5",
                        text = "¿Qué significan las 'ganancias operativas especiales' en los documentos?",
                        effectType = "TRUST",
                        effectValue = "-30",
                        coherence = 50,
                        topic = "acuerdos privados",
                        approach = "DIRECTO"
                    ),
                    Question(
                        id = "q_marisol_6",
                        text = "Estoy aquí para encontrar quién le hizo esto a su padre. Confíe en mí.",
                        effectType = "TRUST",
                        effectValue = "15",
                        coherence = 50,
                        topic = "su padre",
                        approach = "EMPÁTICO"
                    ),
                    Question(
                        id = "q_marisol_7",
                        text = "¡Deje de ocultar información! Su padre murió y usted sabe más de lo que dice.",
                        effectType = "TRUST",
                        effectValue = "-25",
                        coherence = 50,
                        topic = "herencia y patrimonio",
                        approach = "PRESIÓN"
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
                description = "Registros Bancarios completos: Muestran transacciones periódicas de dinero ilícito desde Logística del Caribe SAS a las cuentas de Carlos y Tomás, transferencias directas a una cuenta offshore en Panamá a nombre de Valentina Ríos, y la autorización del pago del seguro del contenedor.",
                linkedSuspects = listOf("suspect_tomas", "suspect_carlos", "suspect_valentina"),
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
                description = "Grabación de la cámara de seguridad del puerto que capta al Inspector Tomás Guerrero validando el contenedor de contrabando sin revisarlo, y muestra el auto de Carlos Herrera saliendo de las instalaciones a las 9:00 PM en punto.",
                linkedSuspects = listOf("suspect_tomas", "suspect_carlos"),
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

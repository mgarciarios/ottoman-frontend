# Matriz de requisitos y trazabilidad

Trazabilidad de la consigna del profesor y de las decisiones adicionales del paquete. “Implementado” describe el código o documento presente; “probado” se limita a las acciones efectivamente ejecutadas que figuran en [`docs/verificacion.md`](../../verificacion.md). Los contratos JSON son propuestos, no protocolos oficiales de Unitree.

| ID | Requisito | Implementación / evidencia | Estado actual |
|---|---|---|---|
| R01 | Problema, solución actual, actores, misión y entorno | [`01_caso_uso_y_robot.md`](01_caso_uso_y_robot.md); caso y guion en [`guia_demo_y_defensa.md`](../../guia_demo_y_defensa.md) | Implementado en documentación |
| R02 | Elegir G1/Go2, sensores, movilidad, autonomía, límites y alternativa sin robot | [`01_caso_uso_y_robot.md`](01_caso_uso_y_robot.md), con ficha/enlaces de Unitree y límites de uso | Documentado; cifras sujetas a la versión del fabricante |
| R03 | HMI para operador y HRI para visitante | [`OttomanApp.kt`](../../../app/src/main/java/ar/edu/uade/ottoman/presentation/ui/OttomanApp.kt): estado del operador y tarjeta de mensajes HRI simulados en Inicio/Misiones | Implementado; sin audio ni interacción física |
| R04 | Supervisar misiones y contingencias | `Dashboard`, `Missions`, `Alerts`; evidencia manual de misión y fallas en [`docs/evidence/`](../../evidence/) | Implementado; flujo normal y dos alertas verificados en AVD |
| R05 | Conectividad, batería, operación, progreso, ubicación, movimiento, alertas y última actualización | `Dashboard` y `Telemetry` en `OttomanApp.kt`; offline banner y captura [`telemetria.png`](../../evidence/telemetria.png) | Implementado; desconexión completa pendiente de prueba manual |
| R06 | Tres variables de servicio | Destino, presencia del visitante y tramos recorridos en dashboard/telemetría | Implementado |
| R07 | Iniciar, pausar, reanudar, cancelar, volver seguro, detención operativa y reconocer alertas | `CommandControls`, `AlertCard`, `CommandGuard`, `SimulatedOttomanRepository` | Implementado; cancelación/detención tienen diálogo de confirmación |
| R08 | Habilitación segura y outcomes pendiente/confirmada/rechazada/sin respuesta | `RobotCommand`, `CommandGuard`, `send` y `RecentCommands`; cinco pruebas unitarias de reglas | Implementado; no todos los outcomes se probaron manualmente |
| R09 | Envío distinto de ejecución; detención software distinta de emergencia física | Respuesta del simulador seguida por telemetría; textos en `AlertDialog`, Demo y decisiones técnicas | Implementado |
| R10 | Figma navegable y mapa de navegación | [Archivo Figma real](https://www.figma.com/design/CJjvcobWKX7X3vDSwoNfNv) y [`docs/figma.md`](../../figma.md) | Parcial: 8 frames y 5 reacciones existen; falta revisar visualmente la vista final |
| R11 | Componentes reutilizables y estados visuales | Componentes Compose reutilizados y tokens/componentes del Figma | Parcial: nodos existen; la vista final Figma requiere revisión |
| R12 | Robots, dashboard, telemetría, misiones/control, alertas, historial y conexión | `RobotSelection`, `Dashboard`, `Telemetry`, `Missions`, `Alerts`, `History`; conectividad se representa en estados online/offline | Implementado en Android; pantalla Robot añadida como selector de Ottoman-01 |
| R13 | Flujo normal y flujo de falla | Reacciones Figma; evidencia Android normal y alerta en [`docs/evidence/`](../../evidence/) | Parcial: no se completó en AVD el recorrido de falla de punta a punta |
| R14 | Kotlin, Compose, MVVM, ViewModel y StateFlow | Paquetes `presentation`, `domain`, `data`; [`OttomanViewModel.kt`](../../../app/src/main/java/ar/edu/uade/ottoman/presentation/OttomanViewModel.kt) | Implementado |
| R15 | UI solo observa estado y emite eventos | `OttomanApp` usa `collectAsState` y envía acciones al ViewModel | Implementado; sin acceso directo al simulador desde composables |
| R16 | Repository desacopla telemetría, comandos y eventos | Contrato `OttomanRepository` y `SimulatedOttomanRepository` | Implementado |
| R17 | Separar modelos, Repository, ViewModel y Compose | `domain/`, `data/`, `presentation/` | Implementado |
| R18 | Historial local persistente | `SharedPreferencesHistoryStore`; `HistoryStore` aislado y probado con un almacén en memoria; alternativa a Room justificada en [`docs/decisiones_tecnicas.md`](../../decisiones_tecnicas.md) | Implementado; persistencia Android tras cierre/reinicio aún no probada |
| R19 | APK offline sin hardware, servidor ni Internet | Simulador local; manifiesto sin permiso `INTERNET`; instrucciones en [`README.md`](../../../README.md) | Implementado por diseño; modo avión no se probó manualmente |
| R20 | Batería dinámica, telemetría, misión, alertas, desconexión, rechazo y fallas forzables | Acciones de laboratorio en `DemoLab` y repositorio; reinicio conserva historial | Implementado; falta ejecutar manualmente todos los escenarios |
| R21 | JSON propuesto de telemetría/comandos/respuestas | Siete ejemplos en [`docs/brief/contratos/`](../contratos/) y descargo en su README | Implementado |
| R22 | Escenario normal | Inicio confirmado y misión completada al 100% en AVD; capturas [`orden-confirmada.png`](../../evidence/orden-confirmada.png) y [`mision-completada.png`](../../evidence/mision-completada.png) | Probado manualmente en AVD |
| R23 | Batería baja | Test de inicio bloqueado; alerta a 18% y botón deshabilitado en [`alerta-bateria.png`](../../evidence/alerta-bateria.png) y [`bateria-inicio-bloqueado.png`](../../evidence/bateria-inicio-bloqueado.png) | Probado por unidad y manualmente en la versión anterior al último ajuste de reanudación |
| R24 | Desconexión, datos antiguos, orden no confirmada y recuperación | `noResponseMarksCommandAndStaleTelemetryUntilReconnect` verifica `NO_RESPONSE`, STALE y reconexión | Probado en unit test; recorrido visual completo pendiente |
| R25 | Obstáculo con decisión humana | `obstacleMustBeResolvedBeforeResume`; bloqueo/alerta capturados en [`alerta-obstaculo.png`](../../evidence/alerta-obstaculo.png) | Probado en unit test y alerta manualmente; flujo visual de resolver/reanudar pendiente |
| R26 | Escenario propio de servicio: visitante separado | `visitorMustReuniteBeforeResume`, mensaje HRI y captura [`alerta-visitante-separado.png`](../../evidence/alerta-visitante-separado.png) | Separación/reunión y reanudación probadas en unit test; alerta y HRI vistas manualmente |
| R27 | Cinco riesgos y medidas | [`07_pruebas_riesgos_entregables.md`](07_pruebas_riesgos_entregables.md) y [`guia_demo_y_defensa.md`](../../guia_demo_y_defensa.md) | Documentado |
| R28 | Preentrega documental y Figma | PDF original, caso, requisitos, arquitectura, riesgos, contratos y Figma | Parcial: material preparado; revisión visual final del Figma pendiente |
| R29 | Figma final, APK, fuente, arquitectura, Repository, mensajes, evidencias, decisiones y video | APK debug, fuente, documentación, prototipo y capturas reales presentes | Parcial: no se grabó video; quedan pendiente Figma visual, persistencia tras reinicio y pruebas de escenarios indicadas |

## Decisiones adicionales del paquete

| ID | Decisión | Evidencia |
|---|---|---|
| D01 | Unitree G1 simulado para guía universitaria | [`01_caso_uso_y_robot.md`](01_caso_uso_y_robot.md), UI y decisiones técnicas |
| D02 | Umbrales, velocidades, tramos y destinos ficticios | `CommandGuard.kt`, `SimulatedOttomanRepository.kt`, [`docs/decisiones_tecnicas.md`](../../decisiones_tecnicas.md) |
| D03 | Laboratorio de escenarios | `DemoLab` y capturas [`laboratorio.png`](../../evidence/laboratorio.png) |
| D04 | Android-only, separación basada en la arquitectura de Bookie | [`05_arquitectura_y_referencia_bookie.md`](05_arquitectura_y_referencia_bookie.md), estructura de paquetes y decisiones técnicas |

## Criterio de aceptación

No marcar como probado un escenario solo porque existe un botón o una prueba de regla aislada. Las pruebas automatizadas, la ejecución manual, el Figma y el video tienen estados separados; los pendientes se mantienen explícitos arriba y en [`docs/verificacion.md`](../../verificacion.md).

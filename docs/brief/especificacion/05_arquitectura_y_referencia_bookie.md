# Arquitectura Android y uso de Bookie como referencia

## Hallazgos verificados en Bookie (GitHub rama main, 16/11/2026)
- `androidApp/`, `iosApp/`, `shared/`, `gradle/libs.versions.toml`.
- `README.md`: Kotlin Multiplatform con Compose Multiplatform, UI compartida, Android/iOS.
- `docs/architecture.md`: presentación / dominio / datos, contratos en dominio, implementaciones en datos, separación DTO y modelos, dependencias hacia dominio.
- `shared/build.gradle.kts`: Compose, ViewModel, Ktor, coroutines y SQLDelight.
- `androidApp/build.gradle.kts`: entrada Android.
**El proyecto de Ottoman NO necesita KMP, iOS, Bookie, auth, búsqueda de libros, Ktor, Supabase ni conexión a backend.** Reusar criterios arquitectónicos y componentes, no copiar fuente ni marcas ajenas indiscriminadamente. El repo Ottoman estaba vacío al revisar, así que crear base Android limpia es más directo.

## Stack sugerido
Kotlin, Android Gradle Plugin compatible con SDK disponible, Jetpack Compose Material 3, AndroidX Lifecycle ViewModel, StateFlow/coroutines, Room para historia de misiones y alertas/eventos, Navigation Compose si aporta, pruebas JUnit y coroutines-test. Sin permiso de Internet y sin backend; no requerir APIs de Unitree. Priorizar versiones estables compatibles en el entorno en vez de copiar números de versión no verificados de Bookie.

## Paquetes/árbol propuesto
```
app/src/main/java/ar/edu/uade/ottoman/
  MainActivity.kt
  domain/model/{Robot.kt,Mission.kt,Telemetry.kt,RobotCommand.kt,RobotAlert.kt}
  domain/repository/RobotRepository.kt
  domain/usecase/{StartMission.kt,SendRobotCommand.kt,ObserveRobot.kt}
  data/simulation/{SimulatedRobotEngine.kt,SimulationConfig.kt,ScenarioInjector.kt}
  data/repository/SimulatedRobotRepository.kt
  data/local/{OttomanDatabase.kt,MissionEventDao.kt,MissionEventEntity.kt}
  presentation/{OttomanViewModel.kt,OttomanUiState.kt,OttomanAction.kt}
  presentation/navigation/OttomanNav.kt
  presentation/screens/{RobotsScreen.kt,DashboardScreen.kt,MissionsScreen.kt,TelemetryScreen.kt,AlertsScreen.kt,HistoryScreen.kt,SimulationLabScreen.kt}
  presentation/components/{StatusChip.kt,TelemetryTile.kt,CommandButton.kt,RobotMap.kt,AlertCard.kt,CommandResultCard.kt}
  presentation/theme/{Color.kt,Type.kt,Theme.kt}
app/src/test/.../{SimulatedRobotEngineTest.kt,RobotCommandTest.kt,RobotRepositoryTest.kt}
docs/{caso_uso.md,robot.md,arquitectura.md,hmi_hri.md,contratos.md,riesgos.md,decisiones.md,pruebas.md,figma.md,video_demo.md}
```
No es obligatorio crear exactamente cada archivo; sí respetar límites de capas y pruebas.

## Flujo
`Composable → ViewModel.send(Action) → domain/usecase → RobotRepository (interface) → SimulatedRobotRepository → SimulatedRobotEngine`.
`Engine → Repository StateFlow/Flow → ViewModel StateFlow<UiState> → Composable`.
`Repository → Room DAO (historial persistido)`.
La UI jamás usa `SimulatedRobotEngine` ni Room DAO directamente. Ningún trabajo prolongado se inicia por recomposición; usar ViewModel scope y engine lifecycle explícito. Un `SupervisorJob` controlado por application scope para simulador o un único servicio de demo con repositorio retenido en contenedor de aplicación, no recreado por navegación. Estado Compose local solamente para opciones efímeras de UI (diálogos y tabs); estados relevantes en VM/Repository.

## Entidades sugeridas
- `Robot`: id/name/model/availability.
- `Mission`: id/destination/route/status/progress/createdAt/startedAt/endedAt.
- `Telemetry`: robotId, connection, batteryPercent, operatingState, missionId, progressPct, positionXY, velocity, heading, lastUpdateAt, stale, service metrics.
- `RobotCommand`: id, type, status, reason, missionId, requestedAt, respondedAt.
- `RobotAlert`: id, severity, type, message, acknowledgedAt, resolvedAt.
- `MissionEventEntity`: primary key/eventType/timestamp/missionId/description; Room.

## Pruebas importantes
Máquina de estados pura y separada de Compose; falsos relojes/scheduler o parámetros de tiempo; inyección de fallas reproducible; tests de idempotencia y comandos; test historia de Room o instrumento, si viable; test de `StateFlow` y restauración de UI, si viable.

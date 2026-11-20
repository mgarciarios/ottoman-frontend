# PROMPT MAESTRO — IMPLEMENTAR OTTOMAN CONTROL END-TO-END

Actuá como desarrollador/a senior Android/Kotlin, diseñador/a de UX para HMI/HRI y responsable de QA. Debés implementar un proyecto académico REAL en el repositorio destino `https://github.com/mgarciarios/ottoman-frontend`, siguiendo íntegramente la consigna `consigna_original/TP-HMI-Ottoman.pdf` y toda la documentación del paquete. No entregues pseudocódigo, maquetas vacías, TODOs ni afirmaciones de verificación no realizadas.

## Contexto y alcance
El producto «Ottoman Control» es una aplicación para un OPERADOR HUMANO que supervisa un **robot Unitree G1 simulado**, encargado de acompañar visitantes desde Recepción hasta Aula Magna, Aula 204 o Laboratorio de Tecnología (destinos y plano DEMOSTRATIVOS, no un mapa verificado de UADE). La app también contempla HRI: cómo el robot comunica progreso, pausa y contingencias a la persona acompañada. Se requieren órdenes discretas; no se exige joystick.

## Archivos a leer (obligatorio)
1. `consigna_original/TP-HMI-Ottoman.pdf` (requisitos del profesor; prioridad máxima)
2. `especificacion/01_caso_uso_y_robot.md`
3. `especificacion/02_requisitos_y_trazabilidad.md`
4. `especificacion/03_estados_y_comandos.md`
5. `especificacion/04_simulador_escenarios.md`
6. `especificacion/05_arquitectura_y_referencia_bookie.md`
7. `especificacion/06_diseno_figma_y_pantallas.md`
8. `especificacion/07_pruebas_riesgos_entregables.md`
9. `contratos/*.json`

## Repositorio y adaptación de Bookie
- Revisá el estado actual de `ottoman-frontend`. El 16/11/2026 estaba vacío, pero respetá los cambios que hayan surgido después.
- Inspeccioná **como referencia** `https://github.com/tomasbondUade/bookie-frontend`, especialmente `README.md`, `docs/architecture.md`, `androidApp/`, `shared/`, `presentation/`, `domain/`, `data/`, Theme y componentes. Es una app Kotlin Multiplatform/Compose, con ViewModel, StateFlow, Ktor y SQLDelight; **NO copies Bookie completo** ni su contenido funcional. Reutilizá separación de responsabilidades, convenciones de paquetes, UI y pruebas cuando tenga sentido.
- **Decisión técnica preferida:** proyecto Android-only de Kotlin + Jetpack Compose + Material 3 + MVVM + AndroidX ViewModel + StateFlow + coroutines + Room. Es lo mínimo compatible con el TP; no introducir iOS/KMP ni Ktor, Retrofit o servidor solo por imitar Bookie. Si el repositorio ya tiene infraestructura útil, adaptarla de manera justificada, minimizando complejidad.
- Nombre del paquete sugerido `ar.edu.uade.ottoman` (configurar correctamente applicationId/namespace). UI en español. Código Kotlin legible y separado en `domain/`, `data/`, `presentation/`, `simulation/` (simulación dentro de data o módulo propio), `ui/theme/`.

## Implementación obligatoria
1. Proyecto Android ejecutable/instalable sin hardware real, internet ni backend; assets y datos de ejemplo locales.
2. Selección de robot (aunque exista solo Ottoman G1) y dashboard con conectividad, batería, estado operativo, misión, progreso, posición/mapa, movimiento, alertas y hora de última telemetría. Añadir tres métricas del servicio: destino, visitante acompañado, puntos del recorrido completados (u otras definidas coherentemente).
3. Selección de destino y gestión de misiones: iniciar, pausar, reanudar, cancelar, regresar a punto seguro y solicitar detención operativa. Reconocimiento de alertas. Estados/confirmaciones de cada orden: pendiente, confirmada, rechazada y sin respuesta. **No equiparar envío y ejecución**.
4. Simulador reproducible por eventos: telemetría periódica, movimiento, progreso, batería, éxito, alerta de batería baja, desconexión y datos desactualizados, fallo/obstáculo, rechazo de comando y escenario propio «visitante se separa del robot». Permitir forzar escenarios desde una pantalla de laboratorio/demo.
5. Reglas de habilitación y transiciones coherentes, bloqueo de comandos inseguros, confirmación para acciones destructivas y manejo de comandos durante desconexión. Tratar «detención operativa» como solicitud de software simulada, NO paro de emergencia físico.
6. Historial local persistente de misiones o eventos (preferir Room) que sobrevive a cerrar la app. Sincronizar con Flow/StateFlow; no exponer al composable el simulador directamente.
7. UI Compose usable, navegable, con pantallas de robots, dashboard, misiones, control, telemetría, alertas, historial y conexión; estados normal/deshabilitado/advertencia/error y accesibilidad básica.
8. Documentación JSON que describa contratos PROPUESTOS y no oficiales Unitree.
9. Pruebas unitarias del motor de estados y escenarios, tests de repositorio/VM cuando sea posible, instrucciones para ejecutar demo y capturas/evidencias reales si se dispone de emulador. No inventar evidencias.
10. Documentos listos para preentrega y entrega final; preparar materiales/guiones exportables para Figma y video, pero NO afirmar que existe un enlace Figma real ni un video si no se produjeron.

## Plan de trabajo de ejecución
- Fase A: inspeccionar repos y acordar estructura/Gradle/JDK; registrar decisiones y limitaciones.
- Fase B: dominio, estados y simulador con tests.
- Fase C: Repository, ViewModel, StateFlow, Room y persistencia.
- Fase D: Compose, navegación, comandos, avisos, mapa offline esquemático, laboratorio y accesibilidad.
- Fase E: pruebas de los cinco escenarios, errores, reinstalación/cierre, calidad visual.
- Fase F: Figma navegable (si herramienta accesible) o especificación de pantallas/conexiones exportable; documentos, diagramas, guion de demo; APK.

## Criterios de finalización (no negociables)
- `./gradlew assembleDebug` completa sin errores y existe un APK instalable **si el ambiente dispone de SDK/JDK**. Si no, detallar impedimento preciso y lo que sí se verificó.
- Cada escenario de `especificacion/04_simulador_escenarios.md` es demostrable en menos de 2 minutos desde estado reiniciable.
- Matriz de trazabilidad cubierta con archivos y capturas/tests reales; sin funcionalidades falsas.
- El repositorio destino contiene código fuente, README, documentación, contratos, pruebas, guía de Figma y guion de video. No modificar el repo Bookie.
- Comandos siempre tienen estados de solicitud y outcome; alertas deben ser reconocibles, no desaparecen por navegar.
- Reportar archivos modificados, pruebas efectivamente ejecutadas, resultados y faltantes antes de declarar completado.

Si no tenés acceso de escritura GitHub, generá archivos/parches completos listos para pegar en `ottoman-frontend` y especificá cómo subirlos. No inventes commits, PRs, Figma, APKs ni enlaces. Trabajá autonomamente siguiendo las propuestas del paquete y anotá cualquier supuesto faltante.

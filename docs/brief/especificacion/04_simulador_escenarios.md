# Motor de simulación y cinco escenarios de defensa

## Funcionamiento offline y determinista
Usar Kotlin coroutines + `StateFlow` (por ej. ticker cada 1 segundo en `CoroutineScope` con cancelación correcta). Telemetría inmutable y emisiones al Repository; Compose no accede al engine. Batería decrece durante ejecución y regreso; progreso crece siguiendo waypoints y nunca supera 100 %. Ubicación y velocidad se actualizan coherentemente en un mapa local esquemático. Tiempo de simulación configurable para que demos duren 30–90 segundos. Asegurar simultaneidad segura (Mutex/actor/flujo de eventos único). Mantener una sola misión activa por robot. Registrar todos los eventos importantes de órdenes, alertas, fallas, finalización.

**Valores propuestos DEMO, no especificaciones G1:** batería inicial 82 %, baja ≤20 %, crítica ≤10 %, mínimo inicio 25 %, velocidad de guía 0.4 m/s equivalente simulado, pérdida de telemetría obsoleta >5 s, timeout comando 4 s, tick 1 s, waypoint 5–10 s (modo rápido). Parámetros en una clase `SimulationConfig` fácilmente editable. La batería y la posición son ficticias, para la evaluación académica.

## Panel demo/laboratorio
Botones: «Restablecer robot», «Misión normal», «Forzar batería baja», «Desconectar», «Reconectar», «Forzar obstáculo», «Rechazar próxima orden», «Simular orden sin respuesta», «Visitante se separa», «Batería crítica» (opcional). Todos visibles solo en modo demo identificado; no confundir al operador con botones reales de emergencia. Cada acción deja huella en el log. Reset no borra historial.

## Escenario 1 — Normal
**Preparación:** robot conectado, batería 82 %, READY, recepción, destino Aula Magna.
**Pasos:** iniciar; visualizar PENDING, CONFIRMED; movimiento y progreso con waypoints; llegada y fin.
**Esperado:** 100 %, COMPLETED, batería menor que inicial, último timestamp fresco, historial persistido y texto HRI «Llegamos».

## Escenario 2 — Batería baja
**Preparación:** misión activa, inyectar batería 18 %.
**Esperado:** WARNING visible; opción regreso a punto seguro cuando factible y cancelación/pausa; bloquear inicio de nuevas misiones si batería <25%; no desaparición de alerta tras reconocerla; si batería crítica, requerir operador.

## Escenario 3 — Desconexión
**Preparación:** misión en curso y comando pendiente o emitido.
**Pasos:** inyectar desconexión, avanzar reloj, visualizar última telemetría congelada/antigua; comando pendiente llega a NO_RESPONSE al timeout; reconectar y recibir datos nuevos.
**Esperado:** no mostrar movimiento como confirmado durante desconexión, mensaje persistente hasta reconexión, progreso/hora consistente, comandos normales deshabilitados mientras desconectado; recovery sin duplicar misión.

## Escenario 4 — Obstáculo
**Preparación:** RUNNING con camino ocupado.
**Pasos:** inyectar obstáculo; bloquear movimiento, emitir alerta, solicitar decisión; operador reconoce y pausa/cancela o decide resolver obstáculo (acción demo), luego reanuda.
**Esperado:** no continuar automáticamente ante peligro; transiciones válidas, historial con intervención.

## Escenario 5 — Propio: visitante se separa
**Preparación:** acompañamiento activo y `visitorPresent=true`.
**Pasos:** botón de laboratorio «Visitante se separa» fija `visitorPresent=false` y emite alerta. Robot pausa misión e informa operador. El operador puede cancelar o solicitar regreso seguro; para reanudar solo tras `visitorPresent=true` restablecido manualmente.
**Esperado:** la app no afirma detección real de personas; el evento es simulado, con historial y HRI adecuado.

## Casos especiales
- Rechazo de comando: comando `REJECTED` con razón clara, sin cambio de misión.
- Tap doble en START: un solo commandId activo y una sola misión.
- Rotación: ViewModel no recrea simulador ni duplica el ticker.
- Cierre y reapertura: historial persiste; puede reiniciarse estado simulado del robot documentadamente.
- Reglas de separación de tiempos: reconectar actualiza `lastUpdate` solamente al recibir nuevo snapshot, no al apretar reconectar.

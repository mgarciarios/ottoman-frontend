# Estados, órdenes y reglas de seguridad simulada

## Estados sugeridos de misión (enum)
`IDLE`, `PREPARING`, `RUNNING`, `PAUSED`, `BLOCKED`, `RETURNING`, `COMPLETED`, `CANCELLED`, `FAILED`, `STOPPED`.

## Estado operativo y conectividad
Operativo: `READY`, `EXECUTING`, `PAUSED`, `NEEDS_OPERATOR`, `STOPPED`. Conectividad: `CONNECTED` / `DISCONNECTED` / `RECOVERING`; timestamp de última telemetría confirmada; flag `isStale` calculado si timeout superado. Durante desconexión congelar último dato recibido y mostrar edad; no simular que sigue recibiéndose telemetría fresca.

## Estados de cada orden (OBLIGATORIOS)
`PENDING`, `CONFIRMED`, `REJECTED`, `NO_RESPONSE`. Es importante separar outcome del canal de comunicación (ACK/REJECT/timeout) y estado de ejecución física. `CONFIRMED` significa que el simulador confirmó la **aceptación** del comando; la UI debe verificar por telemetría si el estado de misión realmente cambió. Evitar decir «orden ejecutada» solo por ACK. Correlacionar por `commandId`, `missionId`, `createdAt` y respuesta.

## Reglas sugeridas (aceptación por comando)
| Comando | Habilitación | Resultado coherente |
|---|---|---|
| START | CONNECTED, READY/IDLE, destino y batería suficiente, no hay otra misión activa | PENDING → CONFIRMED → PREPARING → RUNNING |
| PAUSE | RUNNING o RETURNING, CONNECTED | PENDING → CONFIRMED → PAUSED |
| RESUME | PAUSED y sin incidente activo, CONNECTED, batería apta | PENDING → CONFIRMED → RUNNING/RETURNING |
| CANCEL | misión activa, CONNECTED | confirmar acción UI → PENDING → CONFIRMED → CANCELLED |
| RETURN_SAFE | misión activa o finalizada fuera de recepción, CONNECTED, batería suficiente | PENDING → CONFIRMED → RETURNING → IDLE en Recepción |
| OPERATIONAL_STOP | CONNECTED y robot no detenido | confirmación UI → PENDING → CONFIRMED → STOPPED, bloquea mover; NO «E-STOP» real |
| ACK_ALERT | alerta no reconocida; conexión no necesariamente requerida si es acción local del operador | registro local de reconocimiento, sin borrar causa/alarma automáticamente |

Notas de decisiones: ante desconexión, no habilitar enviar comandos normales; para DEMOSTRAR `NO_RESPONSE` incluir un modo de simulación que deja una orden previamente enviada en `PENDING` hasta timeout y lo transforma en `NO_RESPONSE`; el robot real podría comportarse distinto. Una orden `REJECTED` debe explicar el motivo y mantener estado previo. Ninguna orden se aplica dos veces al duplicarse el mismo `commandId`. Para `RETURN_SAFE` con batería crítica, el simulador debe solicitar intervención y evitar prometer que el desplazamiento será seguro. El modo de laboratorio puede resetear el simulador a estado base explícitamente, pero nunca borrar el historial persistido salvo confirmación.

## Transiciones normales
- IDLE → PREPARING → RUNNING → COMPLETED
- RUNNING → PAUSED → RUNNING
- RUNNING → BLOCKED → PAUSED (requiere operador) → RUNNING o CANCELLED
- RUNNING/PAUSED → CANCELLED
- RUNNING/PAUSED/BLOCKED → RETURNING → IDLE
- RUNNING/PAUSED/etc → STOPPED, sin reanudación automática; reset supervisado en demo.

## Alertas
Campos: ID, gravedad (`INFO`,`WARNING`,`CRITICAL`), tipo, texto, misión relacionada, fecha, reconocimiento (quién/cuándo), causa activa, resolución. Reconocer ≠ resolver. Persistir en historial. Mostrar banner + bandeja; botón «Reconocer» en alerta; las críticas no deben ocultarse al reconocer.

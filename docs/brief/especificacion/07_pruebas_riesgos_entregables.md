# QA, riesgos y entregables

## Pruebas mínimas
| ID | Acción | Resultado esperado |
|---|---|---|
| T01 | Inicio misión con batería suficiente | PENDING → CONFIRMED y RUNNING |
| T02 | Completar ruta | COMPLETED, 100 %, historial |
| T03 | Forzar batería 18 % | WARNING, nuevas misiones bloqueadas, acción segura disponible |
| T04 | Desconectar tras emitir orden | último dato se torna STALE; solicitud NO_RESPONSE; reconectar recupera |
| T05 | Obstáculo en RUNNING | BLOCKED, alerta, no progreso hasta decisión |
| T06 | Visitante se separa | pausa, alerta, HRI y requiere operador |
| T07 | Rechazar próxima orden | REJECTED, motivo y sin transición indebida |
| T08 | Doble click START | no duplica misión ni comando |
| T09 | Rotar Activity | no duplicar simulador/ticker/alertas |
| T10 | Cerrar/reabrir app | historial persiste Room |
| T11 | Comando estando desconectado | deshabilitado o indica explícitamente no enviado |
| T12 | Reconocer alerta | queda ACK sin borrar causa activa |
| T13 | CANCEL/STOP | requieren confirmación explícita |
| T14 | Offline completo | la app abre y funciona con internet desactivado |
| T15 | Estabilidad | compila, sin crashes conocidos en demo |

## Cinco riesgos operativos y mitigaciones propuestas
1. **Colisión con personas u obstáculos.** UI muestra bloqueos y alerta, avance no continúa solo, rutas de baja velocidad; en hardware sería necesario sistema de seguridad validado.
2. **Orden accidental de cancelación o detención.** Confirmación doble, estados y etiquetas diferenciados, feedback de respuesta; sin afirmar paro físico.
3. **Pérdida de conectividad / telemetría vieja.** Datos congelados con timestamp y STALE, control inhibido, timeout NO_RESPONSE, revalidación al reconectar.
4. **Privacidad de visitantes.** No almacenar nombres, imágenes o voz; usar un booleano ficticio de presencia, almacenamiento mínimo local; consentimiento/retención para despliegue real.
5. **Batería baja a mitad de acompañamiento.** Umbrales y alerta temprana, impedir nuevas misiones, retorno solo bajo condiciones supervisadas, intervención humana en crítico.
Opcionales: accesibilidad, caminos bloqueados, sobreconfianza del operador, falsa confirmación, ambigüedad de rutas.

## Evidencias obligatorias (registrar, no inventar)
- Cinco screenshots o secuencias/capturas del emulador, uno por escenario.
- Log de comandos con cuatro outcomes; última telemetría y alertas.
- Capturas de Figma navegable normal y falla; URL compartible del prototipo real.
- APK `app-debug.apk` instalable; captura/registro `assembleDebug` exitoso; versión/SDK.
- Historial Room verificado tras cierre y reapertura; tests y comandos ejecutados.

## Preentrega (§6.1)
Documento breve con definición del problema, entorno, actores, misión, elección de G1 y fuentes técnicas, esquema del recorrido, telemetría, comandos, riesgos y Figma navegable (normal + falla).

## Final (§6.2)
Figma definitivo; APK instalable y fuente; diagrama arquitectura; Repository simulado; contratos JSON propuestos; evidencia cinco escenarios; decisiones/limitaciones; video de demostración.

## Guion sugerido de video (3–5 minutos, duración propuesta)
1. Contexto del problema + por qué G1 y no mapa solamente.
2. Arquitectura MVVM/Repository/Simulador/Room.
3. Misión Aula Magna con ACK, progreso y fin.
4. Forzar batería baja, desconexión y obstáculo.
5. Escenario propio de separación del visitante.
6. Historial persistente, pruebas y limitaciones.

## Lista de pendientes externos
- **Figma:** hacer prototipo real y conseguir URL (herramienta externa).
- **APK:** compilar con SDK/JDK compatible tras implementar código.
- **Evidencias:** capturar ejecución real; no generarlas fingiendo pruebas.
- **Video:** grabar demostración real.
- **Mapa universitario real:** sustituir recorrido ficticio con autorización / información verificable si corresponde.

# Registro de verificación

Este registro separa las pruebas ejecutadas de los escenarios todavía pendientes. La app y sus datos son una simulación académica; no se conectó a Ottoman físico ni a Unitree G1.

## Entorno

- JDK 21, Android SDK en `/home/bond/Android/Sdk`.
- Gradle wrapper 8.13; Android Gradle Plugin 8.12.0; Kotlin 2.1.20.
- AVD `telefonito`, emulador `emulator-5554`, imagen Android 37.2 beta, x86_64.
- APK debug de 9.6 MB (9,577,090 bytes en la última compilación).

## Verificaciones ejecutadas

| Fecha | Acción | Resultado |
|---|---|---|
| 2026-11-19 | `./gradlew clean testDebugUnitTest assembleDebug` | Correcto desde cero; se ejecutaron 5 pruebas de `CommandGuardTest` y se generó el APK. Gradle incluyó `libandroidx.graphics.path.so` sin quitar símbolos; el empaquetado terminó correctamente. |
| 2026-11-19 | `./gradlew testDebugUnitTest assembleDebug` tras completar la selección de robot, alinear la regla de reanudación y aislar `HistoryStore` | **Correcto**. 14 pruebas (6 de reglas + 8 del repositorio/simulador), 0 fallos; APK reconstruido. |
| 2026-11-19 | `adb install -r app/build/outputs/apk/debug/app-debug.apk` en `emulator-5554` | La instalación del APK final respondió `Success`; Android identificó el paquete `ar.edu.uade.ottoman`. |
| 2026-11-19 | Iniciar `MainActivity` con ADB | Una iteración respondió `Status: ok`. Después de una compilación posterior, `am start -W` terminó con `Status: timeout` (39.7 s) y una captura de pantalla excedió 8 s. ADB aún listaba el AVD como `device`, pero no respondió a la inspección; la interfaz del último APK no se pudo revalidar visualmente. |
| 2026-11-19 | Recorrido manual de la interfaz antes de que el AVD dejara de responder | Se probaron inicio, selección del robot, misión, batería, alertas, escenario de visitante y telemetría. Las capturas de abajo corresponden a ejecuciones reales. |
| 2026-11-19 | `adb logcat` filtrado por excepción fatal de Ottoman | No se encontró `FATAL EXCEPTION` de Ottoman en el tramo leído. En el primer arranque hubo un aviso “System UI isn't responding”; no se atribuyó a la app. |
| 2026-11-19 | `adb devices -l` y `lsusb` para el teléfono físico | El teléfono no fue detectado; el usuario indicó dejarlo para otro momento. |
| 2026-11-19 | Instalación del APK en el teléfono | El usuario informó que instaló la app manualmente. Falta confirmar el arranque y el recorrido visual en ese dispositivo; no se verificó mediante ADB. |
| 2026-11-19 | Recorrido manual del usuario en su teléfono | El usuario confirmó que abrió la app y funcionaron misión normal, batería baja, obstáculo, visitante separado, orden sin respuesta y reconexión. Resultado reportado por el usuario; sin captura de pantalla/video ni inspección por ADB. |
| 2026-11-19 | Video de demostración silencioso | Revisado `docs/demo-ottoman-silenciosa.mp4`: 142 s, 390 × 850, H.264; la pista AAC está prácticamente en silencio (pico medido −91 dB). Se ven misión normal, batería baja, obstáculo y mensaje HRI de visitante separado. No se distingue claramente la orden `NO_RESPONSE` y la reconexión. |
| 2026-11-19 | Metadatos del prototipo Figma | Confirmados 8 frames de 390 × 844 y 5 reacciones. La vista final no se pudo revisar por el límite de llamadas MCP Starter. |
| 2026-11-19 | Primer `git push -u origin main` al repositorio solicitado | Rechazado con HTTP 403 por falta de permiso de escritura de la cuenta autenticada. |
| 2026-11-19 | Publicación después de habilitar permisos | `main` se publicó en `https://github.com/mgarciarios/ottoman-frontend`; commit `f6db601` (`Document team and phone validation`). |

## Recorrido manual en el AVD

- Abrí Inicio, Misiones, Alertas, Historial, Demo y Telemetría; entré al selector desde “Ottoman-01”.
- Seleccioné **Aula 204** e inicié el acompañamiento. La orden apareció confirmada y la misión llegó al 100 %, con batería simulada de 82 % a 77 %.
- Forcé batería al 18 %. Apareció la alerta y se vio deshabilitado **Iniciar acompañamiento** con el motivo del umbral de 25 %.
- Reconocí la alerta de batería; quedó reconocida mientras la causa siguió activa.
- Forcé un obstáculo; apareció la alerta crítica y la misión requirió decisión del operador.
- Forcé separación del visitante; aparecieron la alerta crítica y el mensaje HRI simulado. Reuní al visitante, pero el intento manual de reanudar se hizo mientras la batería seguía en 18 % y el simulador lo rechazó por batería insuficiente. A raíz de eso, el código ahora deshabilita **Reanudar** bajo el umbral; ese ajuste no se pudo volver a mirar en pantalla porque el AVD dejó de responder.
- La suite unitaria prueba los flujos de misión normal, batería, timeout/desconexión/reconexión, obstáculo y visitante reunido. Estas pruebas del repositorio no sustituyen la ejecución manual completa ni prueban el almacenamiento Android real.

Capturas tomadas directamente del AVD:

| Pantalla o estado | Captura |
|---|---|
| Dashboard | [dashboard-inicial.png](evidence/dashboard-inicial.png) |
| Selección de Ottoman G1 | [seleccion-robot.png](evidence/seleccion-robot.png) |
| Orden confirmada | [orden-confirmada.png](evidence/orden-confirmada.png) |
| Misión completada | [mision-completada.png](evidence/mision-completada.png) |
| Batería al 18 % y alerta | [alerta-bateria.png](evidence/alerta-bateria.png) |
| Inicio deshabilitado por batería | [bateria-inicio-bloqueado.png](evidence/bateria-inicio-bloqueado.png) |
| Alerta reconocida con causa activa | [alerta-reconocida.png](evidence/alerta-reconocida.png) |
| Alerta de obstáculo | [alerta-obstaculo.png](evidence/alerta-obstaculo.png) |
| Visitante separado | [alerta-visitante-separado.png](evidence/alerta-visitante-separado.png) |
| Mensaje HRI simulado | [comunicacion-hri.png](evidence/comunicacion-hri.png) |
| Misiones | [misiones-destino.png](evidence/misiones-destino.png) |
| Telemetría | [telemetria.png](evidence/telemetria.png) |
| Laboratorio de escenarios | [laboratorio.png](evidence/laboratorio.png) |

## Límites pendientes

- El último APK se compiló y se instaló, pero no se pudo completar una validación visual posterior porque el AVD dejó de responder. Las capturas reflejan la versión ejecutada antes de esa última iteración de código.
- No se recorrieron manualmente todos los casos T01–T15. Los 14 tests cubren reglas y el repositorio simulado, no Activity rotation, modo avión, todos los diálogos ni persistencia Android real.
- El reinicio probado en unit tests usa un `HistoryStore` en memoria; no se comprobó el historial de `SharedPreferences` tras cerrar y reabrir el proceso.
- El usuario probó en su teléfono la misión normal, batería baja, obstáculo, visitante separado, orden sin respuesta y reconexión, e informó que todo funcionó. No contamos con evidencia visual de ese recorrido ni inspección por ADB. La revisión visual del último APK en el AVD sigue pendiente.
- El Figma real existe, pero falta una revisión visual final.
- El video dura 2:22, por debajo de los 3–5 minutos sugeridos en esta guía. Tampoco demuestra claramente desconexión/`NO_RESPONSE` y reconexión; agregar ese recorrido o repetir la grabación si la cátedra exige los cinco escenarios en el video.
- La rama `main` está publicada en el repositorio solicitado.

## Artefacto

APK: `app/build/outputs/apk/debug/app-debug.apk`. Para reconstruirlo y ejecutar las pruebas: `./gradlew testDebugUnitTest assembleDebug`.

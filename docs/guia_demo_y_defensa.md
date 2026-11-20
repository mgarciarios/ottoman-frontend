# Guía de demostración y defensa

## Integrantes

- Tomás Bond
- Martina María García Ríos
- Julieta Ballestrero
- Facundo Maldonado

## Caso de uso

Ottoman es un Unitree G1 simulado que acompaña a una persona desde Recepción hasta Aula Magna, Aula 204 o Laboratorio de Tecnología. Una persona operadora en recepción observa misión y telemetría, reconoce alertas y decide cuándo pausar, reanudar, cancelar o pedir el regreso. El mapa representa un campus ficticio de un piso; hay que reemplazarlo por un plano autorizado si la cátedra lo requiere.

El humanoide se propone por su forma de interacción presencial. La app no demuestra navegación autónoma, reconocimiento de visitantes ni operación segura. Frente a señalética o un mapa digital, el robot agrega una hipótesis de acompañamiento físico, pero cuesta más y suma riesgos; la elección se plantea como prototipo académico, no como ventaja económica probada.

## Recorrido oral sugerido (3–5 min)

1. Presentar el problema, actores, campus demostrativo y límites del simulador.
2. Explicar arquitectura Android: Compose → ViewModel/StateFlow → contrato de Repository → simulador; mostrar la persistencia local.
3. Ejecutar misión normal, distinguir `PENDING` y `CONFIRMED`, observar progreso y finalización.
4. Mostrar batería baja y bloqueo de nuevas misiones; reconocer una alerta sin ocultar su causa.
5. Forzar obstáculo, resolverlo manualmente y reanudar; mostrar visitante separado como escenario propio.
6. Desconectar, mostrar edad de telemetría y orden sin respuesta; reconectar y revisar historial.
7. Cerrar con riesgos, límites de hardware y trabajo pendiente de Figma/video si todavía no se generaron.

## Video de demo sin audio (3–5 min)

Grabar la pantalla del teléfono en vertical, sin narración. Dejar cada estado visible unos segundos para que se lean las etiquetas y alertas; no hace falta agregar voz ni editar el video.

1. **Inicio y misión normal:** mostrar Inicio; abrir Demo → **Ejecutar misión normal**; volver a Inicio o Misiones y dejar que se vea el progreso y la llegada.
2. **Batería baja:** Demo → **Batería baja · 18%**; mostrar la alerta y que el inicio queda bloqueado.
3. **Obstáculo:** Demo → **Restablecer robot** → **Ejecutar misión normal** → **Forzar obstáculo**; mostrar la alerta, tocar **Resolver obstáculo** y luego reanudar desde Misiones.
4. **Visitante separado:** restablecer e iniciar otra misión; tocar **Visitante se separa**, mostrar la alerta/mensaje HRI y tocar **Visitante vuelve**.
5. **Desconexión:** restablecer e iniciar una misión; tocar **Orden sin respuesta**, esperar el timeout, mostrar desconexión/orden sin respuesta y tocar **Reconectar robot**.
6. **Cierre:** mostrar Historial con los eventos del recorrido.

El contenido de la aplicación deja visible que es una simulación; no presentar los estados como telemetría de un G1 físico.

## Escenarios del TP

| Escenario | Preparación / acción | Resultado observable |
|---|---|---|
| Normal | Demo → Ejecutar misión normal | Inicio confirmado; progreso; 100% y evento de llegada. |
| Batería baja | Iniciar misión y luego Demo → Batería baja | 18%, alerta, nueva misión bloqueada; reconocer no resuelve. |
| Desconexión | Demo → Orden sin respuesta | PAUSE pendiente, conexión perdida, telemetría envejece y termina `NO_RESPONSE`; reconectar actualiza el snapshot. |
| Obstáculo | Demo → Forzar obstáculo | Misión bloqueada; alerta crítica; no avanza; resolver causa y reanudar manualmente. |
| Propio | Demo → Visitante se separa | Misión pausada y alerta; reunificar manualmente antes de reanudar. El evento no es detección física. |

## Riesgos y mitigaciones de interfaz

1. **Choque con personas/obstáculos:** detener avance simulado, escalar alerta y exigir decisión; hardware requeriría validación independiente.
2. **Orden accidental:** confirmación explícita para cancelar y detener; respuesta y estado de misión separados.
3. **Conexión perdida:** congelar datos, mostrar antigüedad, bloquear control y registrar timeout.
4. **Privacidad:** sin cámara, micrófono, nombres ni datos identificables; presencia es un booleano ficticio.
5. **Batería insuficiente:** impedir iniciar bajo el umbral y alertar; batería crítica exige intervención en vez de prometer un regreso seguro.

## Preguntas para preparar

- ¿Qué diferencia hay entre ACK confirmado y misión completada? El primero dice que el simulador aceptó una orden; el segundo se verifica en telemetría.
- ¿Por qué SharedPreferences y no Room? El alcance solo requiere guardar un log local pequeño, sin consultas relacionales; la alternativa se documentó y se puede sustituir si el docente exige Room.
- ¿Por qué Android-only? La consigna solicita APK Android; separar capas conserva opciones de evolución sin implementar iOS innecesariamente.
- ¿Qué está simulado? Todo movimiento, ubicación, batería, sensores, conectividad, presencia y confirmaciones.
- ¿Qué falta para operar un G1 real? Mapeo, navegación, SDK/licencias, validación de seguridad, pruebas en campus, interacción accesible y procedimientos.

# Ottoman Control

Aplicación Android nativa para supervisar y operar un **Unitree G1 simulado** que acompaña visitantes dentro de un campus universitario demostrativo. Proyecto de Desarrollo de Aplicaciones I, UADE.

## Equipo

- Tomás Bond
- Martina María García Ríos
- Julieta Ballestrero
- Facundo Maldonado

> La aplicación no se conecta a un robot ni representa una parada física de emergencia. Los sensores, posiciones, batería, acompañamiento y respuestas son simulados. El mapa es ilustrativo y no es un plano verificado de UADE.

## Ejecutar

Requisitos: Android Studio o JDK 17+, Android SDK API 35 y conexión inicial para descargar dependencias.

```bash
./gradlew testDebugUnitTest assembleDebug
```

APK generado: `app/build/outputs/apk/debug/app-debug.apk`.

Para instalar en un dispositivo conectado con ADB:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

La app no declara permiso de Internet y el simulador funciona sin robot, servidor ni conexión de red. El historial se guarda localmente en las preferencias privadas de la aplicación. El estado de la simulación vuelve al estado inicial al cerrar el proceso.

## Demostración rápida

1. En **Demo**, tocar **Ejecutar misión normal**.
2. Abrir **Inicio** o **Misiones** para ver el progreso simulado, el destino y la respuesta de cada orden.
3. En **Demo**, probar batería baja, obstáculo, visitante separado, rechazo, desconexión y timeout sin respuesta.
4. Reconocer una alerta en **Alertas**; reconocerla no elimina una causa activa.
5. Revisar los eventos persistidos en **Historial**. El botón de restablecer conserva ese historial.

El avance de la misión se acelera para la defensa: 5 puntos porcentuales por segundo; los valores no son especificaciones reales del G1.

## Arquitectura

```text
Compose UI → OttomanViewModel (StateFlow) → OttomanRepository (dominio)
                                               ↓
                                  SimulatedOttomanRepository
                                  ├─ motor de telemetría y órdenes
                                  └─ HistoryStore → SharedPreferences
```

Se sigue la separación presentación/dominio/datos descrita en `docs/brief/especificacion/05_arquitectura_y_referencia_bookie.md`, tomando Bookie solo como referencia arquitectónica. Ottoman usa Android nativo y elimina KMP/iOS, Ktor, SQLDelight y cualquier servicio remoto porque no hacen falta para un simulador offline. Decisiones detalladas: [`docs/decisiones_tecnicas.md`](docs/decisiones_tecnicas.md).

## Requisitos y materiales

- Consigna original, prompt, matriz académica, especificaciones y contratos JSON: [`docs/brief/`](docs/brief/).
- Guion de escenarios y riesgos: [`docs/guia_demo_y_defensa.md`](docs/guia_demo_y_defensa.md).
- Video de demostración silencioso: [`docs/demo-ottoman-silenciosa.mp4`](docs/demo-ottoman-silenciosa.mp4).
- Resultados de compilación y verificación: [`docs/verificacion.md`](docs/verificacion.md).
- Prototipo navegable real (ocho pantallas): [Ottoman Control — HMI Android](https://www.figma.com/design/CJjvcobWKX7X3vDSwoNfNv).
- Estado del prototipo: [`docs/figma.md`](docs/figma.md).

Los JSON bajo `docs/brief/contratos/` son contratos propuestos para el TP, no un protocolo oficial de Unitree.

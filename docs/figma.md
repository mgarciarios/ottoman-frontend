# Prototipo Figma Ottoman Control

Archivo creado: [Ottoman Control — HMI Android](https://www.figma.com/design/CJjvcobWKX7X3vDSwoNfNv).

Incluye ocho pantallas móviles editables: selección del robot, dashboard, creación de misión, control, telemetría, alertas, historial y laboratorio de fallas. Incluye componentes reutilizables de telemetría y navegación, colecciones de tokens primitivos/semánticos, un botón Material 3 y cinco enlaces de prototipo para los flujos normales y de falla.

## Navegación

Robot → Inicio → Misiones/Control → Telemetría → Alertas → Historial; Demo agrupa los cinco escenarios académicos. Navegación inferior: Inicio, Misiones, Alertas, Historial y Demo.

## Pantallas y estados

- Inicio: conectividad, batería, estado operativo, progreso, mapa, destino, visitante y tramos completados.
- Misiones: selección de destino y órdenes discretas con estado pendiente/confirmada/rechazada/sin respuesta.
- Telemetría: última actualización, bandera de datos antiguos, ubicación, movimiento y tres variables del servicio.
- Alertas: gravedad, causa activa, reconocimiento y resolución separados.
- Historial: línea temporal de eventos persistidos.
- Demo: escenario normal, batería baja, desconexión, obstáculo, rechazo, timeout y visitante separado.
- Componentes con estados normal, deshabilitado, advertencia, error y pendiente.

## Flujos que debe conectar el prototipo

1. **Normal:** seleccionar Ottoman → elegir Aula Magna → enviar inicio → pendiente → confirmada → progreso → completar → historial.
2. **Falla:** misión activa → obstáculo → alerta → reconocer → resolver causa manualmente → reanudar o cancelar → historial.

Tokens sugeridos: azul petróleo `#174A67`, fondo `#F3F7F9`, superficies blancas, verde `#167A61`, ámbar `#B16A00`, rojo `#B3261E`; etiquetas además de color, targets de 48 dp o más.

## Estado de entrega y verificación

- El archivo Figma y los ocho frames existen; se verificaron sus IDs y dimensiones (390 × 844) mediante metadatos.
- Se añadieron enlaces de prototipo y se comprobó que cada acción enlazada tiene una reacción configurada.
- Se revisó una captura inicial del dashboard. Luego de ajustar el contraste, la vista previa de Figma empezó a renderizar incompleta. El plan Starter alcanzó el límite de llamadas MCP antes de poder volver a capturarla; los nodos y reacciones están presentes, pero el render visual final queda pendiente de revisión manual al abrir el archivo.
- Este archivo no equivale a capturas de ejecución Android ni a un video de demostración.

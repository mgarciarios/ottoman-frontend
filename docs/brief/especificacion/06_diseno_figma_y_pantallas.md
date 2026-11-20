# Diseño UI/UX y plan para Figma

**La consigna pide un Figma navegable REAL.** Este archivo es especificación de diseño, no sustituye el link/prototipo terminado.

## Concepto visual
Nombre Ottoman Control, estética tecnológica universitaria limpia, versión clara como base, alto contraste, estados no solo por color, tipografía legible, acciones críticas con confirmación. Branding original de Ottoman, NO Bookie. Diseñar para un móvil Android vertical ~393x852 dp y adaptar a tablets si hay tiempo.

## Mapa de navegación
Entrada / Selección robot → Dashboard principal → Misiones (elegir destino, crear) → Control (comandos) → Telemetría detallada → Alertas → Historial → Laboratorio de simulación → Ajustes/Conexión. Acceso persistente a robot elegido y estado de conexión. Pestañas inferiores: Inicio, Misiones, Alertas, Historial; más telemetría y laboratorio desde menú/dashboard.

## Especificación de pantallas
1. **Seleccionar robot**: tarjeta Ottoman-01 · Unitree G1 · Simulado · Conectado; CTA Supervisar. Posible segunda tarjeta `sin robots` (estado vacío de ejemplo), sin prometer multi-robot real.
2. **Dashboard**: estado global, batería, conectividad, alerta activa, última actualización, misión y progreso, mini-mapa, tres variables del servicio, botones de pausa/regreso y acceso a control/telemetría.
3. **Nueva misión**: cards Aula Magna / Aula 204 / Laboratorio; preview de ruta/waypoints, origen Recepción, CTA iniciar con validación.
4. **Control**: START/PAUSE/RESUME/CANCEL/RETURN_SAFE/OPERATIONAL_STOP; estado habilitado; diálogos para CANCEL/STOP; listado comandos recientes con PENDING/CONFIRMED/REJECTED/NO_RESPONSE y motivo.
5. **Telemetría**: conexión, última actualización/antigüedad, batería, operativo, misión/progreso, posición (esquema), movimiento/velocidad, alertas y métricas: destino, visitante acompañado (booleano simulado), waypoints completados/total.
6. **Alertas**: bandeja persistente, severidad, descripción, fecha, causa activa, reconocer, acción sugerida; estados vacíos.
7. **Historial**: lista local de eventos/misiones, fecha, origen, destino, resultado, batería, incidentes; detalle por misión; persistencia después de reiniciar app.
8. **Laboratorio**: preset 5 escenarios y botones para fallas; etiqueta SIMULACIÓN; reset seguro; no confundir con controles operativos.
9. **Desconexión**: banner oscuro/ámbar, último dato con antigüedad, comandos bloqueados, comando anterior NO_RESPONSE; reconexión posible en laboratorio.

## Componentes reutilizables
TopAppBar, NavigationBar, StatusBadge, TelemetryCard, BatteryBar, MissionProgress, OfflineBanner, CriticalAlert, CommandButton (normal/deshabilitado/pendiente/error), ConfirmDialog, TimelineEvent, WaypointMap, EmptyState, ScenarioButton.

## Tokens propuestos
- Primario azul petróleo `#174A67` (tema claro), fondo `#F7F9FA`, superficie `#FFFFFF`, texto `#172430`.
- Normal `#167A61`, advertencia `#B16A00`, crítico `#B3261E`, desconectado gris oscuro con etiqueta.
- Bordes 12–16 dp, espaciado 8/16/24 dp, touch targets ≥48 dp, textos/íconos + etiquetas.
- Estados de cada componente: normal, disabled, warning, error, loading.

## Prototipos obligatorios
- **Flujo normal:** Seleccionar robot → Dashboard → Seleccionar Aula Magna → Enviar inicio pendiente → Confirmado → Progreso → Completado → Historial.
- **Flujo de falla:** Dashboard en progreso → Inyectar obstáculo → Alerta → Reconocer → Pausa → Resolución manual → Reanudar/Cancelar → Historial.
- Diseñar y conectar ambos en Figma con destinos navegables, estados funcionales y mapa de navegación; exportar screenshots/figma link para la entrega.

## Accesibilidad/HRI
Mensajes claros, no solo colores, jerarquía de alertas, números visibles, prohibir «seguro»/«detenido físicamente» sin telemetría, privacidad (sin cámaras reales). Mensajes al visitante visibles en panel HRI o modo espejo de demostración.

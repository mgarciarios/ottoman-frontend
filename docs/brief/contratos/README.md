# Contratos de integración de la SIMULACIÓN
Todos los JSON son **contratos propuestos para el TP**, no representan protocolo oficial de Unitree ni endpoints disponibles. Concretar tipos, campos y validaciones dentro del código; la comunicación simulada puede ser en memoria sin serialización real. Mantener la documentación consistente con la implementación.

- Telemetría: último snapshot fechado y flag stale derivado de tiempo sin nuevas lecturas.
- Orden: identificación, tipo, parámetros, momento y misión.
- Respuesta: `PENDING`, `CONFIRMED`, `REJECTED`, `NO_RESPONSE`; *ACK no garantiza ejecución*.
- Evento/alerta: auditoría persistida, reconocimiento separado de resolución.

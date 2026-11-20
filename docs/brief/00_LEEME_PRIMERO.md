# Ottoman Control — paquete de implementación para IA

**Objetivo:** construir y documentar un APK Android funcional de control y supervisión de un robot simulado **Ottoman**, concebido como **Unitree G1** que guía visitantes de la recepción universitaria al Aula Magna, aulas o laboratorios.

- **Repositorio DESTINO:** https://github.com/mgarciarios/ottoman-frontend (estaba vacío al revisarlo el 16/11/2026).
- **Repositorio REFERENCIA (solo arquitectura y prácticas):** https://github.com/tomasbondUade/bookie-frontend (proyecto Bookie, Kotlin Multiplatform; NO copiar sus funcionalidades ni reescribirlo).
- **Consigna ORIGINAL:** `consigna_original/TP-HMI-Ottoman.pdf` (fuente de verdad de exigencias académicas).
- **Instrucción a IA:** empezar por `01_PROMPT_MAESTRO_PARA_IA.md`, después leer todo `especificacion/` y `contratos/`.

**IMPORTANTE:** este ZIP es un paquete de especificaciones, decisiones y recursos para implementar. No es el APK ni una app ya desarrollada; tampoco contiene un Figma publicado o un video grabado. El proyecto real debe implementarse en el repositorio destino y comprobarse.

## Qué está confirmado y qué es propuesta
- **Exigido por el PDF:** tecnologías, telemetría, comandos, simulación offline, cinco escenarios, riesgos, Figma, APK, documentación, video.
- **Decisiones de este paquete (propuestas de diseño):** Unitree G1, universidad como entorno, cuatro destinos, HRI de guía, umbrales, tiempos, mapa ficticio, modelo de datos, interfaz y criterios detallados.
- **Capacidades del hardware:** se basan en ficha oficial, no implican que la navegación guiada sea una característica lista para usar sin integración adicional.

## Entregables incluidos
- Prompt maestro y reglas para la IA
- Requisitos y matriz de trazabilidad completos
- Caso de uso y recorrido simulado; HRI/HMI; telemetría y comandos
- Máquina de estados, simulador, escenarios deterministas
- Arquitectura y adaptación de Bookie; guía de Figma
- Casos de prueba, cinco riesgos y documentación requerida
- JSON de ejemplo y esquema conceptual de contratos
- Listas de verificación para preentrega y entrega final
- PDF original de la consigna

# Caso de uso, HRI y elección de plataforma

## Problema
En una universidad grande, los visitantes y alumnos ingresantes pueden no conocer la ubicación de aulas, auditorios y laboratorios. La orientación humana en recepción implica interrupciones y recorridos repetitivos. La solución actual suele ser señalética, mapas estáticos, información web o empleados que indican el camino; no asumimos datos estadísticos ni problemas probados en un campus específico.

## Servicio propuesto
Ottoman, un Unitree G1 **simulado**, acompaña a una persona por un circuito conocido de planta baja hasta un destino seleccionado. El operador humano de recepción planifica, monitorea e interviene. El robot comunica a la persona de forma simulada destino, comienzo, pausas, obstáculo y finalización.

## Entorno
Campus universitario MODELO ficticio inspirado en una universidad real, con espacios ilustrativos que deben reemplazarse por un plano autorizado/real para preentrega si se exige: Recepción (punto seguro), Hall central, Pasillo A, Aula Magna, Aula 204 y Laboratorio de Tecnología. No afirmar que el recorrido coincide con la geografía real de UADE. Evitar escaleras; ruta accesible, ancho despejado, zonas de cruce y velocidad reducida. Un solo piso, rutas discretas y posiciones esquemáticas 2D offline.

Rutas demostrativas:
- Recepción -> Hall central -> Pasillo A -> Aula Magna
- Recepción -> Hall central -> Aula 204
- Recepción -> Hall central -> Laboratorio de Tecnología
- Regreso seguro: posición actual -> puntos anteriores -> Recepción (simulado)

## Actores
- Operador/a de recepción (usuario autenticado NO requerido para demo): asigna destinos, observa telemetría, reconoce alertas, toma decisiones.
- Visitante: persona acompañada; interacción HRI en mensajes del robot; NO identifica físicamente a la persona ni capta datos personales reales.
- Supervisor/a o docente: ejecuta laboratorio de pruebas y revisa historial.

## Justificación de G1 y comparativa sin robot
G1 es un humanoide con presencia adecuada para interacción presencial; la ficha oficial indica cámara de profundidad + LiDAR 3D, matriz de cuatro micrófonos, parlante 5W, WiFi 6/Bluetooth 5.2, autonomía aproximada de 2 horas, aproximadamente 35 kg y altura 1,32 m. Estos elementos facilitan imaginar un guía de servicio, PERO no garantizan navegación autónoma completa segura en un campus. El control de rutas, mapeo indoor, detección de visitantes, integración con sistemas del lugar, voz accesible y certificación de seguridad requieren software e integración adicionales. La variante G1 base puede tener limitaciones para desarrollo secundario; evaluar G1 EDU y acceso a SDK antes de cualquier despliegue físico. Valores sujetos a versión/ficha actualizada.

- Alternativa señalética/mapa/app sin robot: costo, mantenimiento y riesgos físicos menores; sería la opción preferible si solo se necesita dar instrucciones.
- Aporte hipotético del robot: acompañamiento físico, orientación contextual y demostración de HRI. Se justifica como piloto/ejercicio de investigación de interacción, NO se afirma superioridad costo-beneficio real.
- Restricciones: batería limitada, conectividad, circulación entre personas, obstáculos, privacidad, acceso a ascensores, accesibilidad, eventual autonomía y supervisión; no operar físicamente sin validación.

**Fuentes:** https://www.unitree.com/g1/ (ficha oficial), https://www.unitree.com/go2/ (comparación alternativa) y PDF consigna.

## HRI: mensajes al visitante (simulados, no audio obligado)
- Inicio: «Hola, te acompaño a [destino]. Caminá a una distancia segura». 
- Pausa: «Estamos detenidos. Un operador está revisando el recorrido».
- Alerta de obstáculo: «Hay un obstáculo. Esperá instrucciones».
- Visitante se separa: «No puedo continuar el acompañamiento; avisaré a recepción» (evento disparado en demo, NO detección física real).
- Finalización: «Llegamos a [destino]. Gracias».
- Desconexión: mostrar al operador la falta de confirmación; en la simulación, comportamiento físico real desconocido/no garantizado.

## Criterios de servicio propuestos
Solo una misión activa por robot; destino obligatorio; no iniciar si desconectado o batería por debajo de umbral; registro de duración y resultado; movimiento de ruta simulado a velocidad baja. No captura real de imagen, voz ni datos personales.

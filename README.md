# IoT Temperatura — Sumativa U2 (TI3042)

App Android (Kotlin + Firebase). Dos roles en dos dispositivos con la misma cuenta:

- **Dispositivo A – Sensor:** publica una lectura simulada cada 10 s en Realtime Database.
- **Dispositivo B – Monitor:** recibe en tiempo real, muestra gráfico, alerta por umbral y tabla de estadísticas (última, mín, máx, promedio, desv. estándar, n.º de lecturas, lecturas sobre el umbral).

## Puesta en marcha
1. En https://console.firebase.google.com crea un proyecto.
2. Agrega una app Android con el paquete `com.example.iottemp` y descarga `google-services.json` a la carpeta `app/`.
3. Authentication → Sign-in method → habilita **Correo/contraseña**.
4. Realtime Database → crear base de datos → pestaña **Reglas** → pega el contenido de `database.rules.json` y publica.
5. Abre la carpeta en Android Studio (o crea un proyecto "Empty Views Activity" con ese paquete y copia `app/src`, `app/build.gradle.kts` y los archivos Gradle de la raíz), sincroniza Gradle y ejecuta en dos dispositivos/emuladores.
6. En ambos inicia sesión con la misma cuenta. Uno elige **Sensor** → "Iniciar envío"; el otro elige **Monitor**.

## Guion de demo (Paso 4 de la guía)
1. Registro e inicio de sesión (Firebase Authentication).
2. Sensor enviando cada 10 s; el Monitor se actualiza solo (interconexión entre apps).
3. Tabla de estadísticas y gráfico cambiando en vivo; datos visibles también en la consola de Firebase.
4. Bajar el umbral en el Monitor para disparar la alerta.
5. Detener el sensor → pasados 30 s aparece "Sensor sin señal".
6. Seguridad: reglas por usuario (intentar leer sin sesión falla), HTTPS/TLS de Firebase, `allowBackup=false`, `usesCleartextTraffic=false`, validación de datos en las reglas.

## Pruebas recomendadas
- Login: correo inválido, clave corta, clave incorrecta, cuenta nueva.
- Conexión: modo avión en el sensor y volver a conectar (Firebase reenvía las lecturas pendientes).
- Datos: comparar la tabla con los valores en la consola de Firebase.

## Seguridad / GitHub
`app/google-services.json` está en `.gitignore`. Si el repo es público, no lo subas; las reglas de la base ya impiden accesos sin autenticación.

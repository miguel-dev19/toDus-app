# Registro de Cambios

## v0.6.44 - 19/07/2026

### Correcciones
- AudioRecorder imports, ChatScreen lambda syntax
- Fix S3Uploader: manejar String? de extractAttribute con ?: ""
- callback antes de collect, launch separado para no bloquear, timeout 10s de seguridad

### Otros Cambios
- Actualizar changelog y version
- Grabadora de voz: AudioRecorder (OPUS), VoiceRecorderBar con waveform y duración, botón mic en chat
- Diseño final: X dentro del círculo de progreso, tamaño al lado (2.4 MB)
- Quitar % del progreso, solo mostrar tamaño (2.4 MB, 52.4 MB, 120 KB, etc.)
- UX completa: progreso+tamaño en audio/archivo/sticker, duración en video, X cancelar en todo
- Multimedia UX: progreso circular con X en centro, duración en esquina video, tamaño en subida, sin título en imagen/video
- Multimedia completo: selectores de imagen/video/audio/archivo + pipeline de subida S3 + envío XML
- Actualizar changelog y version
- WelcomeScreen: fondo blanco, logo centrado, titulo y terminos
- Protocolo completo según guía oficial: edit/delete/forward, multimedia builders, grupos IQ, privacidad, bloqueo
- MessageBubble final: placeholder/error en imágenes, soporte para image/video/audio/file/contact/sticker, checks siempre visibles
- Pipeline multimedia completo: ImageCompressor → S3Uploader → MessageBubble con imagen/video/audio + progreso de subida
- Pipeline de imágenes: ImageCompressor (EXIF, escala, 300KB), selector nativo PickVisualMedia, menú adjuntar, progreso de subida
- ContactProfileScreen completa: foto grande, botones acción, tarjeta info, opciones (silenciar, eliminar, bloquear)
- Pro-Tip: auto-scroll inteligente (solo si está al final) + menú adjuntar animado + AnimatedContent enviar/mic + estado dinámico
- ChatScreen WhatsApp-style: TopAppBar roja con avatar, input bar con mic/adjuntar, auto-scroll, burbujas dinámicas
- UI WhatsApp-style: TopAppBar roja, FAB circular, ChatListItem con ticks de estado y badge
- Reactivo: eliminar delays y bucles while, usar LaunchedEffect(connectionState) y .collect()


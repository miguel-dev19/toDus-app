# Registro de Cambios

## v0.6.42 - 19/07/2026

### Correcciones
- callback antes de collect, launch separado para no bloquear, timeout 10s de seguridad

### Otros Cambios
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


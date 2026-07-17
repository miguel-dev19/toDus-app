# Registro de Cambios

## v0.6.38 - 17/07/2026

### Mejoras
- Mejoras: sincronización chats, indicador escribiendo al enviar, eliminar chats con swipe, animaciones

### Correcciones
- eliminar referencia a extractDeliveryAckMsgId, usar receiptMsgId del mensaje
- añadir extractDeliveryAckMsgId a ToDusProtocol
- extractDeliveryAckMsgId import + @OptIn ExperimentalMaterial3Api en HomeScreen
- Fix SQL: escapar 'exists' con @ColumnInfo y comillas dobles en ProfileDao
- Arreglar carga de perfil y contactos: procesar respuestas IQ del servidor

### Otros Cambios
- Actualizar changelog y version
- Actualizar changelog y version
- Actualizar changelog y version
- Actualizar changelog y version
- Separadores por fecha en chat: '16 de julio de 2026' entre días diferentes
- NetworkMonitor integrado: detecta pérdida de red y reconecta automáticamente al recuperar
- Perfil del servidor solo se pide UNA vez (profile_fetched flag en SharedPreferences)
- Cache de imágenes offline: ImageCache + CachedAsyncImage con Coil + limpieza automática 50MB/7días
- Contactos: solo Avatar + Alias, sin punto verde ni número de teléfono
- HomeScreen y HomeTopBar: todo funcional, lista reactiva, punto de conexión, FAB, navegación
- ChatScreen completo: csp/escribiendo, csc/en linea, presencia, RD/DD, tdack
- ChatScreen funcional: estados en linea/escribiendo, confirmaciones RD/DD, checks actualizados, presencia detectada
- Estado (en linea, escribiendo, última vez) en color negro
- Burbujas: enviadas=blancas, recibidas=rojas. Checks: reloj=gris, check=gris, doble=gris, leído=rojo. Sin botón info ni adjuntar ni cargando
- Perfil de contacto simplificado: solo foto, nombre, @usuario, teléfono y bio
- Actualizar changelog y version
- Verificación final: ChatScreen sin AsyncImage innecesario, NavGraph completo
- Actualizar changelog y version
- Iconos de estado en mensajes: reloj=enviando, check=enviado, doble check=entregado, doble check verde=leído
- Actualizar changelog y version
- Revertir a UI original exacta manteniendo solo estilo Telegram (colores, bordes, sombras)
- Actualizar changelog y version
- Actualizar slogan: Una aplicación de mensajería pensada para ti
- Actualizar changelog y version
- Logo toDus oficial en WelcomeScreen + Vector drawable
- Actualizar changelog y version
- UI Telegram completa: Welcome, PhoneInput, Chat, MessageBubble, ChatListItem rediseñados
- Actualizar changelog y version
- UI tipo Telegram: Mi Perfil, Editar Perfil, Perfil de Contacto con gradiente y tarjetas
- Actualizar changelog y version
- ProfileDao: guardar perfiles de usuarios en Room + cache offline de 1 hora
- Actualizar changelog y version
- NetworkMonitor: detección de cambios de red + reconexión con backoff exponencial
- Actualizar changelog y version
- Roster completo: listar, eliminar, hash, versión + cache offline en Room
- Actualizar changelog y version
- Contactos: solo muestra usuarios que usan ToDus (verificados con getInfo) + fotos + @usuario + bio
- Actualizar changelog y version
- Queries reales: roster solo muestra contactos con número válido (usan toDus)
- Actualizar changelog y version
- Eliminar pantalla Mi Perfil: redirigir directo a Home tras login
- Actualizar changelog y version


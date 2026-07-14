#!/usr/bin/env python3
"""
Diagnóstico completo de mensajes offline de toDus
Uso: python3 test_offline.py 5350321300
"""

import socket, ssl, base64, uuid, re, sys, http.client, json
from datetime import datetime

def authenticate(phone):
    """Obtiene JWT"""
    secret = uuid.uuid4().hex
    phone_bytes = phone.encode()
    secret_bytes = secret.encode()[:32]
    body = bytes([0x0a, len(phone_bytes)]) + phone_bytes + bytes([0x12, len(secret_bytes)]) + secret_bytes
    
    c = http.client.HTTPSConnection('auth.todus.cu', 443, timeout=30)
    c.request('POST', '/v2/auth/token', body, {
        'Content-Type': 'application/x-protobuf',
        'User-Agent': 'ToDus 2.1.2 Auth'
    })
    r = c.getresponse().read()
    jwt = ''.join(chr(x) for x in r if 32 <= x < 127)
    c.close()
    return jwt

def connect_xmpp(phone, jwt):
    """Conecta al XMPP"""
    ctx = ssl.create_default_context()
    ctx.check_hostname = False
    ctx.verify_mode = ssl.CERT_NONE
    
    sock = socket.create_connection(('ws.todus.cu', 1756), 30)
    tls = ctx.wrap_socket(sock, server_hostname='ws.todus.cu')
    
    # Stream
    tls.send(b'<?xml version="1.0"?><stream:stream to="im.todus.cu" xmlns="jc" xmlns:stream="x1" version="1.0">')
    tls.recv(4096)
    
    # SASL
    auth = base64.b64encode(f'\x00{phone}\x00{jwt}'.encode()).decode()
    tls.send(f'<auth xmlns="urn:ietf:params:xml:ns:xmpp-sasl" mechanism="PLAIN">{auth}</auth>'.encode())
    tls.recv(4096)
    
    # Reiniciar stream
    tls.send(b'<?xml version="1.0"?><stream:stream to="im.todus.cu" xmlns="jc" xmlns:stream="x1" version="1.0">')
    tls.recv(4096)
    
    # Bind
    i = uuid.uuid4().hex[:8]
    tls.send(f'<iq type="set" id="{i}"><bind xmlns="urn:ietf:params:xml:ns:xmpp-bind"><resource>diag</resource></bind></iq>'.encode())
    tls.recv(4096)
    
    # Session
    i = uuid.uuid4().hex[:8]
    tls.send(f'<iq type="set" id="{i}"><session xmlns="urn:ietf:params:xml:ns:xmpp-session"/></iq>'.encode())
    tls.recv(4096)
    
    # Presence
    tls.send(b'<presence/>')
    tls.recv(4096)
    
    return tls

def get_offline(tls):
    """Solicita mensajes offline"""
    i = uuid.uuid4().hex[:8]
    tls.send(f'<iq type="get" id="{i}"><query xmlns="t:offline"/></iq>'.encode())
    
    data = b''
    while not b'</iq>' in data:
        try:
            chunk = tls.recv(4096)
            if not chunk:
                break
            data += chunk
        except:
            break
    
    return data.decode(errors='ignore')

def analyze_xml(xml):
    """Analiza el XML en detalle"""
    print("\n" + "=" * 60)
    print("  ANÁLISIS DETALLADO DEL XML")
    print("=" * 60)
    
    # Total de elementos
    total_m = len(re.findall(r'<m\s', xml))
    total_dd = len(re.findall(r'<dd\s', xml))
    total_b = len(re.findall(r'<b>', xml))
    total_image = len(re.findall(r'<image\s', xml))
    total_video = len(re.findall(r'<video\s', xml))
    total_offline = len(re.findall(r'<todus_offline\s', xml))
    total_end = len(re.findall(r'<todus_end_offline', xml))
    
    print(f"  Total elementos <m>:        {total_m}")
    print(f"  Total <dd> (confirmaciones): {total_dd}")
    print(f"  Total <b> (mensajes texto):  {total_b}")
    print(f"  Total <image>:              {total_image}")
    print(f"  Total <video>:              {total_video}")
    print(f"  Total <todus_offline>:      {total_offline}")
    print(f"  Total <todus_end_offline>:  {total_end}")
    
    # Tipos de mensaje
    types = re.findall(r"<m[^>]*t='([^']+)'", xml)
    type_counts = {}
    for t in types:
        type_counts[t] = type_counts.get(t, 0) + 1
    
    print(f"\n  Tipos de mensaje:")
    for t, count in type_counts.items():
        label = {"c": "Chat privado", "gc": "Grupo", "ch": "Canal"}.get(t, t)
        print(f"    t='{t}' ({label}): {count}")
    
    # Mensajes de texto reales
    text_msgs = re.findall(r"<m[^>]*t='c'[^>]*>.*?<b>(.*?)</b>", xml, re.DOTALL)
    print(f"\n  Chats privados con texto: {len(text_msgs)}")
    for i, msg in enumerate(text_msgs[:5]):
        print(f"    [{i+1}] {msg.strip()[:100]}")
    
    if len(text_msgs) > 5:
        print(f"    ... y {len(text_msgs) - 5} más")
    
    # Guardar XML completo
    filename = f"offline_diag_{datetime.now().strftime('%Y%m%d_%H%M%S')}.xml"
    with open(filename, 'w') as f:
        f.write(xml)
    print(f"\n  [✓] XML completo guardado en: {filename}")
    
    return {
        "total_m": total_m,
        "total_text": total_b,
        "total_images": total_image,
        "total_videos": total_video,
        "private_chats": len(text_msgs),
        "types": type_counts
    }

def main():
    if len(sys.argv) < 2:
        print("Uso: python3 test_offline.py 5350321300")
        sys.exit(1)
    
    phone = sys.argv[1]
    
    print("=" * 60)
    print("  DIAGNÓSTICO DE MENSAJES OFFLINE - TODUS")
    print("=" * 60)
    print(f"  Número: {phone}")
    print(f"  Hora:   {datetime.now().strftime('%H:%M:%S')}")
    print("=" * 60)
    
    # 1. Autenticar
    print("\n[1/4] Autenticando...")
    jwt = authenticate(phone)
    print(f"  [✓] JWT: {jwt[:50]}...")
    
    # 2. Conectar
    print("\n[2/4] Conectando XMPP...")
    tls = connect_xmpp(phone, jwt)
    print("  [✓] Conectado")
    
    # 3. Obtener offline
    print("\n[3/4] Solicitando mensajes offline...")
    xml = get_offline(tls)
    print(f"  [✓] Recibidos {len(xml)} bytes")
    tls.close()
    
    # 4. Analizar
    stats = analyze_xml(xml)
    
    # Resumen final
    print("\n" + "=" * 60)
    print("  RESUMEN PARA LA APK")
    print("=" * 60)
    print(f"  Chats privados con texto: {stats['private_chats']}")
    print(f"  Total mensajes de texto:  {stats['total_text']}")
    print(f"  Total imágenes:           {stats['total_images']}")
    print(f"  Total videos:             {stats['total_videos']}")
    print(f"  Confirmaciones (dd):      {stats['total_m'] - stats['total_text']}")
    print("=" * 60)
    
    if stats['private_chats'] == 0:
        print("\n  ⚠️  NO HAY CHATS PRIVADOS OFFLINE")
        print("  Solo se encontraron mensajes de grupo o confirmaciones.")
        print("  La app filtrará estos y mostrará 0 mensajes offline.")
    else:
        print(f"\n  ✅ Se descargarán {stats['private_chats']} mensajes de chats privados")

if __name__ == "__main__":
    main()

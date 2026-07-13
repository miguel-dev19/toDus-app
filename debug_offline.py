import socket,ssl,base64,uuid,re,sys,http.client

p = sys.argv[1]
s = uuid.uuid4().hex

# 1. Autenticar
print("=" * 60)
print("  DIAGNÓSTICO DE MENSAJES OFFLINE")
print("=" * 60)
print(f"\n[1/5] Autenticando {p}...")

b = bytes([0x0a,len(p)]) + p.encode() + bytes([0x12,32]) + s.encode()[:32]
c = http.client.HTTPSConnection('auth.todus.cu', 443, timeout=30)
c.request('POST', '/v2/auth/token', b, {
    'Content-Type': 'application/x-protobuf',
    'User-Agent': 'ToDus 2.1.2 Auth'
})
r = c.getresponse().read()
jwt = ''.join(chr(x) for x in r if 32 <= x < 127)
print(f"[✓] JWT: {jwt[:60]}...")

# 2. Conectar XMPP
print(f"\n[2/5] Conectando a ws.todus.cu:5222...")
k = ssl.create_default_context()
k.check_hostname = False
k.verify_mode = ssl.CERT_NONE
t = k.wrap_socket(socket.create_connection(('ws.todus.cu', 5222), 30), server_hostname='ws.todus.cu')

# Stream open
t.send(b'<?xml version="1.0"?><stream:stream to="im.todus.cu" xmlns="jc" xmlns:stream="x1" version="1.0">')
d = t.recv(4096)
print(f"[✓] Stream: {d[:80]}...")

# SASL
a = base64.b64encode(f'\x00{p}\x00{jwt}'.encode()).decode()
t.send(f'<auth xmlns="urn:ietf:params:xml:ns:xmpp-sasl" mechanism="PLAIN">{a}</auth>'.encode())
d = t.recv(4096)
print(f"[✓] SASL: {'OK' if b'<success' in d else 'FAIL'}")

# Reiniciar stream
t.send(b'<?xml version="1.0"?><stream:stream to="im.todus.cu" xmlns="jc" xmlns:stream="x1" version="1.0">')
t.recv(4096)

# Bind
i = uuid.uuid4().hex[:8]
t.send(f'<iq type="set" id="{i}"><bind xmlns="urn:ietf:params:xml:ns:xmpp-bind"><resource>debug</resource></bind></iq>'.encode())
t.recv(4096)

# Session
i = uuid.uuid4().hex[:8]
t.send(f'<iq type="set" id="{i}"><session xmlns="urn:ietf:params:xml:ns:xmpp-session"/></iq>'.encode())
t.recv(4096)

# Presence
t.send(b'<presence/>')
t.recv(4096)
print(f"[✓] Conectado")

# 3. Solicitar offline
print(f"\n[3/5] Solicitando mensajes offline...")
i = uuid.uuid4().hex[:8]
t.send(f'<iq type="get" id="{i}"><query xmlns="t:offline"/></iq>'.encode())

d = b''
while not b'</iq>' in d:
    try:
        d += t.recv(4096)
    except:
        break

xml = d.decode(errors='ignore')
t.close()

# 4. Mostrar XML crudo
print(f"\n[4/5] XML RECIBIDO DEL SERVIDOR:")
print("=" * 60)
print(xml[:2000])  # Primeros 2000 caracteres
if len(xml) > 2000:
    print(f"\n... (truncado, total: {len(xml)} bytes)")
print("=" * 60)

# Guardar XML completo
with open(f"offline_raw_{p}.xml", "w") as f:
    f.write(xml)
print(f"\n[✓] XML guardado en offline_raw_{p}.xml")

# 5. Intentar parsear
print(f"\n[5/5] Intentando parsear mensajes...")
patterns = [
    (r'<m[^>]*f=[\'"]([^\'"]+)[\'"][^>]*>.*?<b>(.*?)</b>', "texto simple"),
    (r'<image[^>]*url=[\'"]([^\'"]+)[\'"][^>]*n=[\'"]([^\'"]+)[\'"]', "imagen"),
    (r'<video[^>]*url=[\'"]([^\'"]+)[\'"][^>]*n=[\'"]([^\'"]+)[\'"]', "video"),
]

for pattern, tipo in patterns:
    matches = re.findall(pattern, xml, re.DOTALL)
    if matches:
        print(f"\n  [{tipo}] {len(matches)} encontrados:")
        for m in matches[:3]:
            print(f"    {m}")
    else:
        print(f"  [{tipo}] 0 encontrados")

# Contar mensajes totales
total = len(re.findall(r'<m[^>]*f=[\'"]', xml))
print(f"\n  TOTAL MENSAJES ENCONTRADOS: {total}")
print(f"  TOTAL BYTES XML: {len(xml)}")

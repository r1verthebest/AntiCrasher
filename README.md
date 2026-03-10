# ☀️ Sunshine Shield | Advanced Network Security & Anti-Crasher

O **Sunshine Shield** é uma solução de segurança de alta performance desenvolvida em **2023**, projetada especificamente para o ecossistema Minecraft (protocolo 1.8.9). O foco central do projeto é a mitigação de ataques de negação de serviço (DoS) e exploits de pacotes maliciosos.

Diferente de soluções convencionais baseadas apenas em listeners de alto nível, o Shield opera diretamente na camada **Netty**, interceptando e filtrando `ByteBuf` brutos antes que alcancem a *Main Thread* do servidor. Isso garante proteção robusta com o menor overhead possível.

---

### 🛡️ Módulos de Proteção

O sistema é dividido em quatro pilares fundamentais, totalmente modulares via `config.yml`:

#### 1. Netty Layer Decode (Low-Level)
* **Packet Sizing:** Filtra pacotes com tamanho anômalo (default > 8000 bytes) para mitigar estouro de memória (Heap).
* **Capacity Control:** Limita a capacidade de leitura de bytes, neutralizando ataques de pacotes brutos malformados.

#### 2. DoS & Anti-Bot
* **CPS Control:** Gestão de conexões por segundo (global e por IP).
* **Auto-Blacklist:** Sistema automatizado de banimento temporário de IPs suspeitos.
* **Real-time Monitoring:** Dashboard via Actionbar para monitoramento de tráfego e tentativas de ataque em tempo real.

#### 3. NBT & Book Exploit Filter
* **Anti-Book Crash:** Validação rigorosa de caracteres por página, limite de bytes e contagem de páginas.
* **NBT Deep Inspection:** Sanitização de metadados em itens como Mob Spawners, Mapas e Signs, prevenindo lag de renderização ou crash do cliente/servidor.

#### 4. Interaction & Physics Awareness
* **Reach Validation:** Validação de distância física em interações para prevenir exploits de pacotes de interação.
* **Adaptive Security:** O sistema ajusta a sensibilidade dos filtros dinamicamente com base no **TPS** (Ticks Per Second) atual do servidor.

---

### ⚙️ Configuração Base (`config.yml`)

O plugin oferece controle granular sobre todos os módulos ativos:

```yaml
checks:
  DosCheck: true  # Proteção contra Bot Attack / CPS Overload
  NBTCheck: true  # Filtro de pacotes de itens maliciosos
  BookCheck: true # Mitigação de Book & Quill exploits

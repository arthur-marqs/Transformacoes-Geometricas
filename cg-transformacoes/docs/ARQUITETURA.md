# Arquitetura — Transformações 2D + Ponto Médio (Java Swing, JDK 11+)

## Princípio central
Todo desenho passa por **um único caminho**: algoritmo → `PixelSink` → `View.plot` → `Framebuffer` (1 `int` por **pixel real** da tela) → Swing copia 1:1 para a janela (compensa a escala de DPI do Windows). Com zoom 1, uma célula do plano = 1 pixel físico. Nenhuma primitiva do Swing/Java2D desenha formas.

```
mouse → ferramenta (Tool) → Scene ← TransformService ← painel / terminal
                              │
            Renderer (eixos, formas, prévia) → MidpointLine → View → Framebuffer → tela
```

## Arquivos (`src/cg/`)

| Pacote / arquivo | Função |
|---|---|
| `Main` | Entrada; fixa `Locale.US` (ponto decimal). |
| **app** | |
| `MainWindow` | Monta a janela e **registra** ferramentas e seções. Único lugar a editar para plugar algo novo. |
| `AppContext` | Objetos compartilhados (tema, vista, cena, terminal, serviços, estado da barra de status). |
| `DisplayControl` | Contrato painel → display (resolução automática/fixa). |
| **raster** | |
| `MidpointLine` | Ponto médio (G3) em todos os octantes, por simetria; opcionalmente registra a tabela d/x/y. |
| `PixelSink` | Interface "receber um pixel (x,y)". |
| **render** | |
| `Framebuffer` | Vetor de pixels reais; `set`, `fillRect`, `clear`. |
| `View` | Mundo ↔ tela: zoom (1–64×), pan, origem no centro, bloco z×z por célula. |
| `Renderer` | Monta o quadro: fundo, eixos com marcas, fantasma do original, formas, guias, prévia, cursor. |
| `Theme` | Cores (padrão preto/verde/vermelho), presets, avisa mudanças. |
| `PixelFont` | Números dos eixos em fonte bitmap 3×5 (sem `drawString`). |
| **geometry** | |
| `Matrix3` | Matriz homogênea 3×3, produto, aplicar ponto, formatação. |
| `Shape2D` | Polígono ou reta (vértices `double`), original guardado, centróide, distância. |
| `ShapeFactory` | Polígono regular / irregular **já centrado na origem**. |
| **transform** | |
| `Transforms` | Translação, escala, rotação, reflexão (x, y, origem, y=x), cisalhamento (x/y), composição com ponto fixo, janela→viewport. |
| `TransformOp` | Uma transformação = lista de matrizes (passos); `total()` = Mn·…·M1. |
| `TransformService` | Aplica na forma ativa, **imprime as contas no terminal**, desfaz/reseta, fila de composição. |
| **scene** | |
| `Scene` | Formas, forma ativa, guias, pilha de desfazer. |
| **tools** (mouse orientado a eventos) | |
| `Tool` / `ToolEvent` | Base: `pressed/released/dragged/moved/keyPressed/drawPreview`. |
| `ToolRegistry` | Lista e ferramenta ativa. |
| `SelectTool`, `LineTool`, `PolygonTool`, `PanTool` | Selecionar, reta (arrastar ou clique-clique), polígono (cliques), mover vista. |
| **terminal** | |
| `Console` | Saída de texto; qualquer classe escreve, o painel escuta. |
| `CommandInterpreter` | Comandos digitados (`rotacionar 30 40 20`, `fila add …`, `linha …`; `ajuda`). |
| `Report` | Texto das contas do ponto médio e listagem de vértices. |
| **ui** | |
| `CanvasPanel` | O display: framebuffer 1:1, traduz mouse → célula, roda = zoom, botão do meio/setas = mover. |
| `Sidebar` | Painel lateral em sanfona (uma seção aberta por vez). |
| `TerminalPanel` | Terminal (segue as cores do tema). |
| `StatusBar` | Coordenadas ao vivo (mundo e pixel de tela), zoom, resolução real. |
| `sections/ToolSection` | Contrato de uma seção do painel. |
| `sections/DrawSection` | Botões das ferramentas (geradas do `ToolRegistry`) + gerador de polígono. |
| `sections/TransformSection` | Todas as transformações G5, fila de composição. |
| `sections/ViewSection` | Zoom, separação de pixels, **resolução** (automática ou fixa). |
| `sections/ColorSection` | Presets e seletor de cada cor. |

`test/cg/SelfTest.java` testa sem interface (slide 28 do G3, slide 25 do G5, interpretador, mapeamento para pixel real). `test/cg/GuiShot.java` usa o mouse via `Robot` e salva capturas.

## Como estender
- **Nova ferramenta de mouse** (ex.: círculo): classe que estende `Tool` + 1 linha `ctx.tools.register(...)` em `MainWindow`. O botão aparece sozinho.
- **Nova seção no painel**: classe que implementa `ToolSection` + 1 linha em `MainWindow`.
- **Nova transformação**: método em `Transforms` devolvendo `TransformOp`; depois um `case` em `CommandInterpreter` e uma opção em `TransformSection`.
- **Novo algoritmo de rasterização**: classe em `raster` que chama `PixelSink`; o `Renderer` e as ferramentas só trocam a chamada.

## Display e resolução
- **Automática** (padrão): o framebuffer ocupa toda a área ao lado do painel; redimensiona com a janela.
- **Fixa** (640×480 … 1920×1080 ou L×A livre): framebuffer exato nesse tamanho em pixels reais; se não couber, aparece rolagem e a seção avisa se *display + painel* cabem na tela.
- O painel (esquerda) e o terminal nunca ficam sobre o display.

## Executar
`compilar.bat` / `executar.bat` (Windows) ou `./compilar.sh && ./executar.sh`. Requer JDK 11+.

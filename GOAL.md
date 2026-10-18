# GOAL — Aplicativo de escrita de histórias e roteiros

**Projeto:** `scripteretradutor`  
**Plataforma:** Android  
**Documento:** objetivo e especificação inicial do produto  
**Status:** proposta para implementação do MVP  
**Idioma padrão:** português do Brasil (`pt-BR`)

## 1. Objetivo geral

Criar um aplicativo Android dedicado à escrita de histórias e roteiros, oferecendo uma experiência confortável para textos longos e ajudando a escritora a produzir textos mais claros e corretos em português do Brasil. O aplicativo deve corrigir ou sinalizar automaticamente erros de ortografia, gramática, concordância, crase e pontuação durante a escrita, sem interromper o fluxo criativo, além de organizar o conteúdo em projetos, capítulos, cenas e atos, preservar o trabalho por meio de salvamento automático e permitir a exportação do resultado em formatos comuns.

## 2. Público-alvo e principal caso de uso

### Público-alvo

- Escritoras de ficção, roteiros, novelas, contos e fanfics.
- Autoras independentes que precisam escrever e revisar no celular ou tablet.
- Pessoas que desejam uma ferramenta de escrita em português, com organização de projetos e revisão integrada.

### Principal caso de uso

A usuária cria um projeto de história ou roteiro, estrutura-o em capítulos, cenas ou atos e escreve no editor por longos períodos. Enquanto digita, o aplicativo analisa o texto em segundo plano, destaca possíveis problemas de português e oferece sugestões que podem ser aceitas ou ignoradas. O conteúdo é salvo automaticamente, pode ser recuperado em versões anteriores e pode ser exportado para compartilhamento, revisão externa ou publicação.

## 3. Escopo do MVP

### 3.1 Editor de texto para escrita longa

- Editor otimizado para textos extensos, com rolagem estável e foco no conteúdo.
- Suporte a projetos compostos por capítulos, cenas e/ou atos.
- Criação, edição, renomeação, reordenação e exclusão de unidades de escrita.
- Formatação mínima necessária para o MVP: parágrafos, quebras de linha, negrito, itálico e título da unidade.
- Preservação de acentuação, pontuação, quebras de linha e formatação durante o salvamento e a exportação.
- Retomada da última posição de edição quando a usuária voltar ao texto.

### 3.2 Correção automática de português

- Usar `pt-BR` como idioma padrão da análise.
- Detectar, no mínimo:
  - erros ortográficos e palavras desconhecidas;
  - problemas gramaticais comuns;
  - concordância verbal e nominal;
  - uso inadequado de crase;
  - pontuação, incluindo vírgulas e sinais incompatíveis;
  - espaços duplicados e erros simples de digitação.
- Analisar o texto de forma incremental ou por trechos alterados, evitando reprocessar desnecessariamente um documento inteiro a cada tecla.
- Não substituir automaticamente uma frase de modo irreversível. Toda alteração sugerida deve poder ser revisada pela usuária.
- Diferenciar visualmente categorias de alerta quando a tecnologia de correção fornecer essa informação.
- Permitir desativar temporariamente a correção para o documento atual e reativá-la depois.
- Respeitar nomes próprios, termos inventados, regionalismos e palavras adicionadas ao dicionário personalizado.

### 3.3 Sugestões destacadas no texto

- Marcar o trecho problemático com sublinhado, cor ou outra indicação acessível.
- Ao tocar na marcação, abrir uma pequena área com:
  - sugestão de substituição;
  - explicação curta do problema, quando disponível;
  - ação **Aceitar**;
  - ação **Ignorar**;
  - ação **Adicionar ao dicionário**, quando aplicável.
- Aplicar a correção sem deslocar indevidamente o cursor ou apagar texto vizinho.
- Manter ignoradas, durante a sessão ou no documento, as ocorrências que a usuária decidiu ignorar.
- Garantir que sugestões desatualizadas sejam descartadas quando o trecho correspondente for editado.

### 3.4 Organização por projetos, capítulos, cenas e atos

- Tela inicial com a lista de projetos.
- Criar, abrir, renomear, duplicar e excluir projetos, com confirmação antes de exclusões.
- Estrutura hierárquica ou ordenável para capítulos, cenas e atos.
- Metadados mínimos: título, tipo, ordem, data de criação e data de atualização.
- Navegação rápida entre unidades sem perder o texto em edição.
- Estado vazio orientando a criação do primeiro projeto e da primeira unidade.

### 3.5 Contadores e metas de escrita

- Exibir contador de palavras e caracteres do documento atual.
- Exibir, opcionalmente, contadores do projeto inteiro.
- Permitir definir meta de palavras para o dia e/ou para o projeto.
- Mostrar progresso da meta de forma simples, sem distrair durante a escrita.
- Considerar claramente como palavras com hífen, números e espaços serão contabilizadas.

### 3.6 Salvamento automático e histórico de versões

- Salvar alterações automaticamente após um pequeno intervalo sem edição e ao sair da tela.
- Persistir localmente o conteúdo e o estado da edição.
- Exibir indicação discreta de estado: salvando, salvo ou erro ao salvar.
- Criar versões recuperáveis em eventos relevantes, como fechamento da unidade, passagem de tempo ou solicitação manual.
- Permitir visualizar data/hora de uma versão e restaurá-la com confirmação.
- Evitar perda de dados em rotação da tela, encerramento do processo ou falta temporária de conexão.

### 3.7 Exportação

- Exportar o projeto ou uma unidade selecionada em:
  - PDF;
  - DOCX;
  - TXT.
- Preservar títulos, ordem e conteúdo das unidades.
- Para TXT, produzir texto limpo e legível, sem marcações internas da correção.
- Para PDF e DOCX, aplicar uma formatação básica consistente e adequada para leitura.
- Usar o mecanismo de compartilhamento do Android para salvar ou enviar o arquivo.
- Informar erros de exportação de forma compreensível e sem perder o conteúdo original.

### 3.8 Espaço de ideias e tarefas

- Cada projeto deve ter uma área separada do texto principal para ideias soltas e tarefas pendentes.
- Permitir criar uma anotação com título/texto, tipo (`Ideia` ou `Tarefa`) e status concluído/não concluído.
- Permitir editar, concluir, reabrir e excluir uma ideia ou tarefa.
- Exemplos de uso: registrar uma revelação futura de personagem, lembrar uma pesquisa ou marcar a revisão de um diálogo.
- Ideias e tarefas não devem aparecer na exportação do texto, salvo se a usuária escolher exportá-las explicitamente.

### 3.9 Navegação interna do projeto

- Dentro de um projeto, oferecer seções claramente separadas para **Texto**, **Correções pendentes** e **Ideias/Tarefas**.
- Permitir voltar à lista de projetos a qualquer momento, preservando o conteúdo já salvo.
- A seção Texto deve oferecer navegação por capítulos/cenas e busca no conteúdo do projeto.
- A seção Correções deve listar pendências do projeto e permitir abrir o trecho correspondente no editor.

### 3.10 Consistência, backup e segurança

- Dicionário do projeto para personagens, lugares, organizações e termos inventados, com notas de referência opcionais.
- Busca dentro do projeto por palavra, nome de personagem ou trecho.
- Backup local exportável e, em versão futura, sincronização opcional com Google Drive ou outro provedor autorizado.
- Bloqueio opcional por PIN ou senha para projetos privados.
- Estatísticas de correção, incluindo quantidade de alertas e distribuição por categoria.

### 3.11 Tags e classificação de projetos

- Ao criar ou editar um projeto, permitir adicionar uma ou mais tags.
- Oferecer tags sugeridas por grupo, sem limitar tags customizadas:
  - plataforma/destino: YouTube, Instagram, TikTok;
  - gênero: Terror, Comédia, Romance, Drama, Ação;
  - classificação: Livre, +16, NSFW/+18.
- Permitir criar, renomear e remover tags personalizadas.
- Exibir tags como chips coloridos na lista inicial e dentro do projeto.
- Permitir filtrar a lista inicial por uma ou mais tags.
- Projetos com tag NSFW/+18 devem ter badge visual distinto e poderão exigir confirmação/PIN antes de abrir, conforme configuração da usuária.

### 3.12 Organização e variações

- Favoritar ou fixar projetos, mantendo-os no topo da lista.
- Arquivar projetos sem apagá-los; a lista deve possuir acesso aos arquivados.
- Duplicar um projeto com sua estrutura e conteúdo para criar versões para diferentes plataformas.
- Exibir nome, última edição, tags e uma prévia curta do texto de cada projeto na tela inicial.

### 3.13 Estrutura e experiência de escrita

- Oferecer templates opcionais ao criar um projeto, inicialmente Jornada do Herói e Estrutura em 3 Atos.
- Permitir adicionar uma nota de cena/capítulo separada do texto final.
- Disponibilizar modo leitura, sem sublinhados ou menus de correção, para avaliar o capítulo como texto pronto.
- Oferecer atalhos simples de formatação para negrito, itálico e título de capítulo.

### 3.14 Produtividade e compartilhamento

- Lembrete diário opcional e configurável, com mensagem simples como “Você ainda não escreveu hoje”.
- Modo ditado por voz usando as APIs de reconhecimento de fala do Android, com a correção de português aplicada ao texto transcrito.
- Permitir selecionar um trecho e compartilhá-lo pelo mecanismo padrão do Android, incluindo WhatsApp, Instagram e outros apps instalados, sem exportar o projeto inteiro.

## 4. Funcionalidades desejáveis — versão 2

Estas funcionalidades não são necessárias para declarar o MVP pronto:

- Sugestões de estilo, incluindo repetição excessiva de palavras, frases muito longas, construções pouco claras e clichês.
- Modo offline completo para análise de português, sem depender de servidor externo.
- Dicionário de personagens, lugares, organizações e termos do universo ficcional.
- Alertas de consistência para nomes, grafias, relações e informações cadastradas.
- Estatísticas de progresso: palavras por dia, sequência de escrita, tempo de sessão e evolução por projeto.
- Metas configuráveis por dia, semana, capítulo ou projeto.
- Temas claro, escuro e modo de baixo contraste/distração.
- Busca e substituição no documento ou no projeto.
- Comentários e anotações da autora sem alterar o texto principal.

As seguintes funcionalidades também podem ser priorizadas na versão 2, caso o tempo do MVP seja limitado:

- Tags avançadas com filtros combinados, cores configuráveis e edição em lote.
- PIN para projetos com conteúdo adulto e confirmação de abertura por classificação.
- Templates narrativos editáveis e templates específicos por plataforma.
- Ditado contínuo com revisão por parágrafo e indicação de incerteza da transcrição.
- Lembretes com calendário, horário configurável e opção de silenciar dias.
- Compartilhamento rápido com seleção de formato, título e prévia.

## 5. Requisitos não funcionais

### Experiência e interface

- Interface simples, limpa e sem distrações, priorizando a área de escrita.
- Navegação clara entre projetos, estrutura do texto e editor.
- Ações principais acessíveis com poucos toques.
- Feedback visual e textual para salvamento, correções e exportação.
- Suporte a tamanhos de fonte maiores e contraste adequado.
- Não ocultar o texto ou interromper a digitação para mostrar uma sugestão.

### Idioma e qualidade linguística

- `pt-BR` deve ser o idioma padrão, incluindo mensagens da interface e revisão.
- A solução de correção deve ser avaliada com exemplos reais de narrativa e roteiro, não apenas frases isoladas.
- A revisão é assistiva: o aplicativo deve sinalizar possibilidades e permitir decisão da autora, especialmente em textos literários.
- O texto original nunca deve ser perdido por uma correção automática.

### Performance e estabilidade

- A digitação deve permanecer responsiva em documentos longos.
- A análise gramatical deve ocorrer fora da thread principal, com debounce e cancelamento de análises antigas.
- O editor deve suportar, como referência inicial, projetos com pelo menos 100.000 palavras sem travamentos perceptíveis nas operações comuns.
- O aplicativo deve continuar utilizável enquanto a análise estiver pendente, indisponível ou falhar.
- Falhas no serviço de correção não podem impedir a edição ou o salvamento local.
- Testar rotação, retomada do app, pouca memória e interrupção inesperada.

### Compatibilidade e segurança de dados

- Projeto compatível com Android Studio, Kotlin, Gradle e Jetpack.
- Definir `minSdk` com base no conjunto de APIs necessário; recomendação inicial: Android 8.0 / API 26, sujeita à validação do projeto e da cobertura de dispositivos.
- Armazenar o conteúdo localmente por padrão.
- Se um serviço remoto de correção for usado, deixar claro quando o texto será enviado e não enviar conteúdo sem consentimento da usuária.
- Não registrar em logs o texto integral da autora em builds de produção.

## 6. Stack sugerida

### Base

- **Linguagem:** Kotlin.
- **UI:** Jetpack Compose, por facilitar estados reativos, marcações no editor e evolução da interface. XML pode ser adotado se a biblioteca de editor escolhida oferecer integração significativamente melhor.
- **Arquitetura:** camadas `UI`, `ViewModel`/estado, domínio e dados; preferencialmente MVVM ou uma variação de Clean Architecture simples.
- **Concorrência:** Kotlin Coroutines e Flow.
- **Injeção de dependência:** Hilt ou solução equivalente, se a quantidade de componentes justificar.

### Correção gramatical

Avaliar antes de fechar a implementação:

1. **LanguageTool self-hosted:** boa cobertura de regras e possibilidade de controlar dados, mas exige avaliar tamanho do mecanismo, licenciamento, consumo de memória e integração Android.
2. **LanguageTool via API:** reduz o trabalho local, mas exige internet, tratamento de privacidade, limites de uso, latência e disponibilidade.
3. **Biblioteca offline em português:** oferece privacidade e funcionamento sem conexão, mas deve ser validada quanto à cobertura real de gramática, concordância, crase e pontuação em textos longos.

Decisão recomendada para o MVP: criar uma abstração `GrammarCorrectionEngine`, permitindo começar com o mecanismo mais viável para testes e trocar entre implementação remota e offline sem alterar o editor. A escolha final deve ser feita após um spike técnico que meça precisão, latência, tamanho do aplicativo e privacidade.

### Persistência e arquivos

- **Banco local:** Room para projetos, unidades de escrita, configurações, dicionário e metadados de versões.
- **Arquivos de versão/exportação:** armazenamento interno ou `MediaStore`/Storage Access Framework, conforme o fluxo de compartilhamento.
- **Preferências:** DataStore para configurações, idioma, meta padrão e preferências de interface.
- **Exportação:** biblioteca compatível com Android para gerar PDF e DOCX; TXT pode ser gerado diretamente. A biblioteca específica deve ser escolhida após teste de licença, tamanho e qualidade do arquivo gerado.

## 7. Estrutura de pastas sugerida

Raiz do projeto:

```text
C:\Users\gilma\Projects\android studio\scripteretradutor\
├── app/
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/example/scripteretradutor/
│       │   │   ├── MainActivity.kt
│       │   │   ├── ScripteretradutorApplication.kt
│       │   │   ├── core/
│       │   │   │   ├── common/
│       │   │   │   ├── dispatchers/
│       │   │   │   ├── result/
│       │   │   │   └── text/
│       │   │   ├── data/
│       │   │   │   ├── local/
│       │   │   │   │   ├── AppDatabase.kt
│       │   │   │   │   ├── dao/
│       │   │   │   │   └── entity/
│       │   │   │   ├── correction/
│       │   │   │   │   ├── GrammarCorrectionEngine.kt
│       │   │   │   │   ├── LanguageToolCorrectionEngine.kt
│       │   │   │   │   └── OfflineCorrectionEngine.kt
│       │   │   │   ├── export/
│       │   │   │   └── repository/
│       │   │   ├── domain/
│       │   │   │   ├── model/
│       │   │   │   ├── repository/
│       │   │   │   └── usecase/
│       │   │   ├── feature/
│       │   │   │   ├── home/
│       │   │   │   ├── project/
│       │   │   │   ├── editor/
│       │   │   │   ├── versions/
│       │   │   │   ├── export/
│       │   │   │   └── settings/
│       │   │   └── ui/
│       │   │       ├── components/
│       │   │       ├── navigation/
│       │   │       └── theme/
│       │   └── res/
│       │       ├── drawable/
│       │       ├── mipmap-*/
│       │       ├── values/
│       │       └── xml/
│       ├── test/
│       │   └── java/com/example/scripteretradutor/
│       └── androidTest/
│           └── java/com/example/scripteretradutor/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradle/
├── gradlew
├── gradlew.bat
└── GOAL.md
```

### Entidades mínimas sugeridas

- `Project`: projeto, título, descrição opcional, idioma, datas e meta.
- `WritingUnit`: capítulo, cena ou ato, título, tipo, ordem e conteúdo.
- `DocumentVersion`: unidade/projeto, conteúdo, data, origem da versão e tamanho.
- `CorrectionIssue`: intervalo do texto, categoria, mensagem, sugestão, estado e identificador da análise.
- `DictionaryEntry`: termo, tipo, projeto associado e ativo/inativo.
- `WritingGoal`: período, quantidade-alvo e progresso calculado.

## 8. Fluxo principal do MVP

1. A usuária abre o app e cria ou seleciona um projeto.
2. Cria um capítulo, cena ou ato e entra no editor.
3. Digita ou cola o texto; o app salva automaticamente.
4. A análise `pt-BR` ocorre em segundo plano e marca possíveis problemas.
5. A usuária toca em uma marcação, aceita a sugestão, ignora ou adiciona o termo ao dicionário.
6. O contador e a meta são atualizados sem sair do editor.
7. A usuária consulta versões anteriores ou exporta o projeto em PDF, DOCX ou TXT.

## 9. Critérios de aceite do MVP

O MVP será considerado pronto quando todos os critérios abaixo forem demonstrados em um dispositivo/emulador suportado:

- [ ] É possível criar, abrir, renomear, ordenar e excluir projetos e unidades de escrita.
- [ ] A usuária consegue escrever e editar um documento longo sem travamentos que impeçam a digitação.
- [ ] O conteúdo permanece após fechar e reabrir o app, rotação da tela e retomada do processo.
- [ ] O salvamento automático ocorre sem ação manual e apresenta estado compreensível para a usuária.
- [ ] O editor exibe contador de palavras e caracteres correto para um conjunto de casos de teste definidos.
- [ ] É possível configurar uma meta de escrita e visualizar o progresso.
- [ ] A correção está configurada para `pt-BR` e identifica, em um conjunto de frases de teste, exemplos de ortografia, gramática, concordância, crase e pontuação.
- [ ] Cada sugestão aparece associada ao trecho correto e oferece aceitar e ignorar.
- [ ] Aceitar uma sugestão altera somente o trecho esperado; ignorar não altera o texto.
- [ ] Uma análise antiga não sobrescreve marcações ou conteúdo de uma edição mais recente.
- [ ] Palavras adicionadas ao dicionário não continuam sendo marcadas indevidamente no mesmo contexto.
- [ ] A edição e o salvamento continuam funcionando se o mecanismo de correção estiver lento, indisponível ou sem conexão.
- [ ] Existe histórico de versões, com visualização de data/hora e restauração confirmada.
- [ ] O projeto ou unidade pode ser exportado para PDF, DOCX e TXT, e os arquivos abrem em leitores compatíveis.
- [ ] A exportação não inclui sublinhados, menus ou metadados internos das sugestões.
- [ ] É possível criar, editar e remover tags livres, e projetos exibem seus chips na lista e na tela interna.
- [ ] A lista inicial filtra projetos por tag e permite distinguir projetos NSFW/+18.
- [ ] Favoritar, arquivar e duplicar um projeto funcionam sem apagar o original.
- [ ] O projeto pode ser criado a partir de um template narrativo ou sem template.
- [ ] Cada capítulo/cena pode ter uma nota separada, e o modo leitura oculta marcações de correção.
- [ ] Atalhos de negrito, itálico e título funcionam sem interromper a escrita.
- [ ] O lembrete diário é opcional, pode ser desativado e não é exibido quando a meta do dia já foi cumprida.
- [ ] O ditado insere texto transcrito no capítulo atual e continua sujeito à revisão da autora.
- [ ] Um trecho selecionado pode ser enviado ao compartilhamento do Android sem exportar o projeto inteiro.
- [ ] Testes unitários cobrem contagem, autosave, versões, aplicação de sugestões e regras de estado.
- [ ] Testes instrumentados cobrem criação de projeto, edição, navegação, restauração e exportação básica.
- [ ] Não há perda de texto em testes de interrupção, reabertura e falha do corretor.
- [ ] O app informa claramente quando a correção usa serviço remoto e respeita a decisão de não enviar texto, caso essa opção esteja disponível.

## 10. Fora do escopo do MVP

- Colaboração simultânea entre várias escritoras.
- Sincronização automática obrigatória com nuvem ou login (backup manual/exportável continua sendo requisito futuro próximo).
- Publicação direta em plataformas externas.
- Correção literária subjetiva ou reescrita automática integral.
- Dicionário completo de personagens e análise avançada de consistência.
- Estatísticas avançadas e sugestões de estilo da versão 2.

## 11. Riscos e decisões pendentes

- **Qualidade do corretor:** validar falsos positivos em diálogos, regionalismos, nomes próprios e linguagem ficcional.
- **Dependência de rede:** definir se o MVP aceita correção remota com fallback para edição sem correção ou se exige uma solução offline.
- **Editor Compose:** confirmar se a implementação suporta sublinhados interativos, seleção de texto, cursor, texto longo e acessibilidade com qualidade suficiente.
- **Exportação DOCX/PDF:** validar fidelidade de formatação, licenças e tamanho final do APK.
- **Privacidade:** documentar o tratamento de texto caso qualquer trecho seja processado por API externa.
- **Versões:** definir política de retenção para não consumir armazenamento indefinidamente.

## 12. Primeiros incrementos recomendados

1. Validar o projeto Android existente e configurar a arquitetura base.
2. Implementar Room, projetos e unidades de escrita.
3. Construir editor, autosave, contadores e navegação.
4. Criar a interface `GrammarCorrectionEngine` e executar o spike de LanguageTool/offline.
5. Integrar marcações, aceitar/ignorar e dicionário mínimo.
6. Adicionar histórico de versões.
7. Implementar exportação e compartilhamento.
8. Executar testes de desempenho, privacidade, interrupção e aceitação com textos reais da escritora.

## 13. Estado atual da implementação

### Implementado e compilado

- [x] Projeto Android Studio em Kotlin + Jetpack Compose compilando em `assembleDebug`.
- [x] Criação e abertura de projetos.
- [x] Capítulos/cenas com editor de texto longo.
- [x] Contadores de palavras e caracteres.
- [x] Salvamento automático local.
- [x] Ideias e tarefas persistentes, com conclusão/reabertura.
- [x] Tags sugeridas e tags personalizadas.
- [x] Catálogo de tags nas configurações: tags sugeridas são fixas; tags criadas pela usuária podem ser removidas.
- [x] Filtro por tags, favoritos, arquivamento e duplicação de projetos.
- [x] Histórico local de versões.
- [x] Restauração de uma versão anterior diretamente pela aba Versões.
- [x] Compartilhamento rápido de texto.
- [x] Exportação TXT, PDF nativo e DOCX básico.
- [x] Modo leitura.
- [x] Ditado em português do Brasil usando o reconhecedor do Android.
- [x] Regras locais iniciais de correção pt-BR.
- [x] Integração opcional com LanguageTool via API, acionada manualmente.
- [x] Modo claro com aparência de bloco de notas e modo escuro.
- [x] Lembrete diário opcional via notificação do Android.
- [x] Backup manual compartilhável em JSON.
- [x] PIN opcional por projeto, armazenado como hash, com bloqueio antes da abertura.
- [x] Tela de configurações acessível pelo botão de engrenagem.
- [x] Busca por título ou conteúdo dentro do projeto.
- [x] Nota separada por cena/capítulo, sem misturar com o texto final.
- [x] Ícone amarelo do app integrado.
- [x] Ícones de configuração nos temas amarelo/claro e preto/escuro integrados em `drawable` e `drawable-night`.
- [x] Catálogo de tags com tags sugeridas fixas e tags personalizadas removíveis.
- [x] Abas e ações do editor adaptadas para rolagem horizontal em telas estreitas.
- [x] Criação de projetos com modelos Livre, Três Atos e Jornada do Herói.

### Ainda falta implementar ou validar

- [ ] Aplicar sublinhado interativo diretamente dentro do editor e ações Aceitar/Ignorar por ocorrência.
- [ ] Associar uma sugestão do LanguageTool ao trecho exato e permitir substituição com preservação do cursor.
- [ ] Validar a cobertura e os falsos positivos em textos reais de narrativa, diálogos, nomes próprios e regionalismos.
- [ ] Restaurar uma versão histórica diretamente pelo histórico, com confirmação.
- [ ] Exportar um projeto inteiro, e não apenas uma cena, preservando capítulos e estrutura em PDF/DOCX.
- [ ] Templates de Jornada do Herói e Três Atos na criação de projetos.
- [ ] Notas individuais por capítulo/cena, separadas das ideias gerais do projeto.
- [ ] Busca dentro do projeto.
- [ ] Dicionário de personagens, lugares e termos com notas de referência.
- [ ] Estatísticas de escrita e estatísticas de erros por categoria.
- [ ] Solicitar permissão de notificações em tempo de execução no Android 13+.
- [ ] Confirmar que o lembrete diário respeita a meta cumprida e registrar a escrita diária.
- [ ] PIN com recuperação segura e proteção contra tentativas repetidas.
- [ ] Backup por arquivo selecionável e restauração do JSON, além do compartilhamento atual.
- [ ] Sincronização opcional com Google Drive ou outro provedor, com consentimento explícito.
- [ ] Testes instrumentados em telefone real, incluindo PDF/DOCX, ditado, notificação, PIN e recuperação após encerramento.

## 14. Próxima ordem recomendada

1. Melhorar o editor para marcações interativas e aceitar/ignorar correções.
2. Implementar restauração de versões e busca no projeto.
3. Adicionar templates, notas por cena e dicionário de consistência.
4. Criar exportação do projeto completo e restauração de backup.
5. Adicionar estatísticas, validação de metas e permissão de notificações.
6. Avaliar sincronização em nuvem apenas depois de validar privacidade e recuperação local.

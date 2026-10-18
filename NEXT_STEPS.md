# Próximos passos — Scripter

## Estado atual

O projeto está em:

`C:\Users\gilma\Projects\android studio\scripteretradutor`

O APK debug é gerado em:

`app\build\outputs\apk\debug\app-debug.apk`

O build usado para validar:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
& .\gradlew.bat :app:assembleDebug --no-daemon
```

## Já implementado

- Kotlin + Jetpack Compose.
- Projetos e capítulos/cenas.
- Editor, contador de palavras/caracteres e autosave local.
- Ideias e tarefas persistentes.
- Tags sugeridas fixas e tags personalizadas removíveis.
- Filtro por tags, favoritos, arquivar e duplicar projetos.
- Busca por título/conteúdo dentro do projeto.
- Nota privada por cena/capítulo.
- Histórico local e restauração de versões.
- Exportação TXT, PDF nativo e DOCX básico.
- Compartilhamento rápido.
- Compartilhamento do projeto inteiro, reunindo todas as cenas na ordem.
- Modo leitura.
- Ditado em pt-BR.
- Regras locais simples de correção.
- Integração manual opcional com LanguageTool via API.
- Tema amarelo/claro e tema preto/escuro.
- Ícone amarelo do app e ícones de configuração em `drawable`/`drawable-night`.
- Tela de configurações com engrenagem.
- Lembrete diário opcional.
- Backup JSON compartilhável e restauração de backup JSON.
- PIN opcional por projeto, armazenado como hash.
- Templates: Livre, Três Atos e Jornada do Herói.

## Próxima prioridade: corrigir e validar a UI

As capturas recebidas mostraram que a interface precisa de uma revisão visual antes de novas funcionalidades:

1. Testar no telefone a última versão.
2. Conferir se o ícone de configurações aparece com tamanho pequeno.
3. Conferir se a barra do editor rola horizontalmente sem cortar “Salvo”.
4. Conferir se as abas do projeto não quebram a palavra “Versões”.
5. Ajustar espaçamentos, tamanhos de botão e contraste conforme novas capturas.
6. Evitar `Row` com muitos botões sem rolagem horizontal.
7. Manter tags e ações em componentes compactos.

## Funcionalidades ainda pendentes

### Correção de português

- Implementar marcação/sublinhado dentro do editor.
- Permitir tocar na ocorrência e escolher Aceitar, Ignorar ou Adicionar ao dicionário.
- Aplicar substituição sem perder cursor/seleção.
- Associar resultados do LanguageTool ao capítulo correto e ao intervalo correto.
- Tratar nomes próprios, gírias e termos inventados.
- Adicionar dicionário de personagens, lugares e termos.

### Organização e produtividade

- Restaurar título editado da cena no banco.
- Templates editáveis.
- Estatísticas de palavras, metas e erros por categoria.
- Busca com navegação direta para o trecho encontrado.
- Notificação de lembrete somente quando a meta diária não foi cumprida.
- Solicitar permissão de notificações no Android 13+.

### Exportação e backup

- Exportar projeto inteiro, preservando capítulos/cenas, em TXT/PDF/DOCX.
- Adicionar confirmação e mensagem de sucesso/erro na restauração de backup.
- Validar DOCX em Word/LibreOffice e PDF em leitores Android.
- Sincronização opcional com Google Drive somente depois de definir consentimento e privacidade.

### Segurança

- Adicionar limite de tentativas do PIN.
- Confirmar PIN antes de remover ou trocar o PIN.
- Considerar Android Keystore para proteger dados sensíveis.
- Garantir que projetos NSFW/+18 possam exigir PIN antes da abertura.

## Teste manual obrigatório

- Criar projeto Livre, Três Atos e Jornada do Herói.
- Criar tag personalizada, usar em projeto e removê-la nas configurações.
- Confirmar que tags sugeridas não podem ser removidas.
- Escrever texto, fechar o app, reabrir e confirmar autosave.
- Criar nota de cena e confirmar persistência.
- Criar versão, alterar o texto e restaurar a versão anterior.
- Exportar TXT, PDF e DOCX e abrir os três arquivos.
- Testar ditado, modo leitura e LanguageTool com conexão e sem conexão.
- Ativar/desativar lembrete.
- Fazer backup JSON, apagar/alterar um projeto e restaurar o backup.
- Definir PIN, sair do projeto e confirmar bloqueio.
- Testar em tela estreita para detectar cortes e overflow.

## Arquivos principais

- `app/src/main/java/com/example/scripteretradutor/MainActivity.kt`: telas Compose e navegação atual.
- `app/src/main/java/com/example/scripteretradutor/data/AppRepository.kt`: persistência local e modelos.
- `app/src/main/java/com/example/scripteretradutor/data/LanguageToolCorrectionEngine.kt`: integração online.
- `app/src/main/java/com/example/scripteretradutor/data/SimplePtBrCorrectionEngine.kt`: regras locais iniciais.
- `app/src/main/java/com/example/scripteretradutor/ReminderReceiver.kt`: notificação diária.
- `app/src/main/java/com/example/scripteretradutor/ui/theme/`: temas claro/escuro.
- `app/src/main/res/drawable-nodpi/`: assets amarelos.
- `app/src/main/res/drawable-night-nodpi/`: assets pretos.
- `GOAL.md`: especificação completa e checklist.

## Observações para continuidade

- Não remover mudanças existentes do usuário.
- Sempre executar `assembleDebug` após alterações de Kotlin/recursos.
- Usar o Java do Android Studio indicado acima.
- O corretor online só deve enviar texto quando a usuária solicitar.
- Não declarar o produto completo antes de validar a UI no telefone e implementar os itens pendentes acima.

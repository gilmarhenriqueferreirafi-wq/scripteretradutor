package com.example.scripteretradutor.data

/** Regras locais iniciais; a interface pode ser trocada por LanguageTool posteriormente. */
object SimplePtBrCorrectionEngine : GrammarCorrectionEngine {
    private val rules = listOf(
        Triple(Regex("\\bmenas\\b", RegexOption.IGNORE_CASE), "menos", "Ortografia"),
        Triple(Regex("\\ba gente fomos\\b", RegexOption.IGNORE_CASE), "a gente foi", "Concordância"),
        Triple(Regex("\\bhouveram muitos\\b", RegexOption.IGNORE_CASE), "houve muitos", "Concordância"),
        Triple(Regex("\\bconcerteza\\b", RegexOption.IGNORE_CASE), "com certeza", "Ortografia"),
        Triple(Regex(" {2,}"), " ", "Espaçamento")
    )

    override suspend fun analyze(text: String, language: String): List<CorrectionIssue> = analyzeNow(text)

    fun analyzeNow(text: String): List<CorrectionIssue> = rules.flatMap { (pattern, replacement, category) ->
        pattern.findAll(text).map { match ->
            CorrectionIssue(match.range.first, match.range.last + 1, "Verifique este trecho em português ($category).", replacement, category)
        }
    }.sortedBy { it.start }
}

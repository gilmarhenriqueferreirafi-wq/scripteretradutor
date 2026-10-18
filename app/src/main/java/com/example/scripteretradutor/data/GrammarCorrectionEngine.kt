package com.example.scripteretradutor.data

/** Contrato para integrar LanguageTool, uma API ou um mecanismo offline futuramente. */
interface GrammarCorrectionEngine {
    suspend fun analyze(text: String, language: String = "pt-BR"): List<CorrectionIssue>
}

data class CorrectionIssue(
    val start: Int, val end: Int, val message: String,
    val replacement: String?, val category: String
)

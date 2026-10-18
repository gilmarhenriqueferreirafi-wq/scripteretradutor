package com.example.scripteretradutor.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/** Integração opcional com o endpoint público do LanguageTool. O texto só é enviado quando solicitado. */
class LanguageToolCorrectionEngine : GrammarCorrectionEngine {
    override suspend fun analyze(text: String, language: String): List<CorrectionIssue> = withContext(Dispatchers.IO) {
        if (text.isBlank()) return@withContext emptyList()
        runCatching {
            val connection = URL("https://api.languagetool.org/v2/check").openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            val body = "text=${URLEncoder.encode(text, "UTF-8")}&language=$language"
            connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val matches = JSONObject(response).optJSONArray("matches") ?: return@runCatching emptyList()
            List(matches.length()) { index ->
                val match = matches.getJSONObject(index)
                val replacements = match.optJSONArray("replacements")
                val rule = match.optJSONObject("rule")
                val category = rule?.optJSONObject("category")?.optString("name")
                    ?: rule?.optString("category", "Português")
                    ?: "Português"
                CorrectionIssue(
                    start = match.optInt("offset"),
                    end = match.optInt("offset") + match.optInt("length"),
                    message = match.optString("message", "Verifique este trecho."),
                    replacement = replacements?.optJSONObject(0)?.optString("value"),
                    category = category
                )
            }
        }.getOrDefault(emptyList())
    }
}

package com.cristian.ecoresiduos.data

/** Conversión de registros a CSV (compatible con Excel). */
object CsvRecords {
    private val header = listOf("tipo", "pesoKg", "origen", "fecha", "hora", "composta", "observaciones")

    fun export(records: List<WasteRecord>): String = buildString {
        append("\uFEFF") // Excel reconoce correctamente los acentos.
        appendLine(header.joinToString(","))
        records.forEach { item ->
            val fields = listOf(
                item.type.name, item.weightKg.toString(), item.source, item.date,
                item.time, item.composted.toString(), item.notes
            )
            appendLine(fields.joinToString(",") { escape(it) })
        }
    }

    private fun escape(text: String): String =
        "\"" + text.replace("\"", "\"\"") + "\""

    /** También admite comas, saltos de línea y comillas dentro de observaciones. */
    fun import(csv: String): List<WasteRecord> {
        val rows = parse(csv.removePrefix("\uFEFF")).filter { row -> row.any { it.isNotBlank() } }
        require(rows.isNotEmpty() && rows.first().map { it.trim() } == header) {
            "El CSV no tiene el encabezado de EcoResiduos."
        }
        return rows.drop(1).mapIndexed { index, row ->
            require(row.size == 7) { "Fila ${index + 2}: número incorrecto de columnas." }
            val weight = row[1].toDoubleOrNull()
            require(weight != null && weight.isFinite() && weight > 0 && row[2].isNotBlank()) {
                "Fila ${index + 2}: peso u origen incorrecto."
            }
            WasteRecord(
                type = WasteType.valueOf(row[0].trim()),
                weightKg = weight,
                source = row[2].trim(),
                date = row[3],
                time = row[4],
                composted = row[5].trim().toBooleanStrict(),
                notes = row[6]
            )
        }
    }

    private fun parse(text: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        var row = mutableListOf<String>()
        val field = StringBuilder()
        var quoted = false
        var index = 0
        while (index < text.length) {
            val char = text[index]
            when {
                char == '\"' && quoted && index + 1 < text.length && text[index + 1] == '\"' -> {
                    field.append('\"')
                    index++
                }
                char == '\"' -> quoted = !quoted
                char == ',' && !quoted -> {
                    row.add(field.toString())
                    field.clear()
                }
                (char == '\n' || char == '\r') && !quoted -> {
                    if (char == '\r' && index + 1 < text.length && text[index + 1] == '\n') index++
                    row.add(field.toString())
                    rows.add(row)
                    row = mutableListOf()
                    field.clear()
                }
                else -> field.append(char)
            }
            index++
        }
        require(!quoted) { "El CSV contiene comillas sin cerrar." }
        if (field.isNotEmpty() || row.isNotEmpty()) {
            row.add(field.toString())
            rows.add(row)
        }
        return rows
    }
}

package com.cristian.ecoresiduos

import com.cristian.ecoresiduos.data.CsvRecords
import com.cristian.ecoresiduos.data.WasteRecord
import com.cristian.ecoresiduos.data.WasteType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class CsvRecordsTest {
    @Test fun exportAndImportPreserveAccentsCommasAndQuotes() {
        val row = WasteRecord(
            type = WasteType.FOOD, weightKg = 2.5, source = "Cafetería, planta 1",
            date = "05/11/2026", time = "12:00",
            composted = true, notes = "cáscaras con \\"comillas\\""
        )
        val imported = CsvRecords.import(CsvRecords.export(listOf(row))).single()
        assertEquals(row.type, imported.type)
        assertEquals(row.source, imported.source)
        assertEquals(row.notes, imported.notes)
        assertEquals(row.weightKg, imported.weightKg, 0.001)
    }

    @Test fun invalidDataIsRejected() {
        val wrong = "tipo,pesoKg,origen,fecha,hora,composta,observaciones\nFOOD,-2,Cocina,05/11/2026,12:00,false,algo"
        assertThrows(IllegalArgumentException::class.java) { CsvRecords.import(wrong) }
    }
}

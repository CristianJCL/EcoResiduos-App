package com.cristian.ecoresiduos.data

/**
 * Implementación temporal del Repository para el primer avance de UI.
 *
 * Mantiene una colección en memoria y permite demostrar altas, consultas y
 * eliminaciones sin mezclar la capa de datos con los composables.
 */
class InMemoryWasteRepository : WasteRepository {

    private val records = mutableListOf(
        WasteRecord(
            id = 1,
            type = WasteType.FRUIT_VEGETABLE,
            weightKg = 2.5,
            source = "Cafetería escolar",
            date = "04/10/2026",
            time = "10:15",
            composted = true,
            notes = "Cáscaras de fruta"
        ),
        WasteRecord(
            id = 2,
            type = WasteType.GARDEN,
            weightKg = 1.2,
            source = "Área verde",
            date = "04/10/2026",
            time = "12:30",
            composted = false,
            notes = "Hojas secas"
        )
    )

    override fun getAll(): List<WasteRecord> = records.toList()

    override fun add(record: WasteRecord) {
        records.add(0, record)
    }

    override fun delete(id: Long) {
        records.removeAll { it.id == id }
    }
}

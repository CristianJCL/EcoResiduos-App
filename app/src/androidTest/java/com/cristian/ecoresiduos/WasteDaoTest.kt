package com.cristian.ecoresiduos

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cristian.ecoresiduos.data.EcoDatabase
import com.cristian.ecoresiduos.data.WasteRecord
import com.cristian.ecoresiduos.data.WasteType
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Comprueba altas, consultas, ediciones y eliminación en una base SQLite real. */
@RunWith(AndroidJUnit4::class)
class WasteDaoTest {
    private lateinit var db: EcoDatabase

    @Before fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), EcoDatabase::class.java
        ).build()
    }

    @After fun close() { db.close() }

    @Test fun insertUpdateAndDelete() = runBlocking {
        val dao = db.wasteDao()
        val initial = WasteRecord(
            type = WasteType.FOOD, weightKg = 1.5,
            source = "Comedor", date = "29/10/2026",
            time = "12:00", composted = false
        )
        val id = dao.insert(initial)
        val inserted = dao.getById(id)
        assertEquals("Comedor", inserted?.source)
        assertEquals(1.5, inserted?.weightKg ?: 0.0, 0.001)

        dao.update(requireNotNull(inserted).copy(weightKg = 3.0))
        assertEquals(3.0, dao.getById(id)?.weightKg ?: 0.0, 0.001)

        dao.deleteById(id)
        assertNull(dao.getById(id))
    }
}

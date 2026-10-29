package com.cristian.ecoresiduos

import com.cristian.ecoresiduos.ui.WasteFormState
import org.junit.Assert.*
import org.junit.Test

/** Verifica las condiciones necesarias para guardar un residuo. */
class WasteFormStateTest {
    @Test fun acceptsValidRecord() {
        assertTrue(WasteFormState(weight = "2.5", source = "Comedor").isValid)
    }
    @Test fun rejectsEmptySource() {
        assertFalse(WasteFormState(weight = "2.5").isValid)
    }
    @Test fun rejectsInvalidWeight() {
        assertFalse(WasteFormState(weight = "0", source = "Comedor").isValid)
    }
}

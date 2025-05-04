package com.github.dsrees.inflekt

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InflektTest {
    @Test
    fun plural() {
        pluralTestCases.forEach { case ->
            Inflekt
                .plural(case.first)
                .also { pluralResult ->
                    assertEquals(case.pluralValue, pluralResult, "Tested: $case")
                }
        }
    }

    @Test
    fun isPlural() {
        pluralTestCases.forEach { case ->
            assertTrue(case.pluralValue.isPlural, "Tested: $case")
        }
    }

    @Test
    fun singular() {
        singularTestCases.forEach { case ->
            Inflekt
                .singular(case.pluralValue)
                .also { singularResult ->
                    assertEquals(case.singularValue, singularResult, "Tested: $case")
                }
        }
    }

    @Test
    fun isSingular() {
        singularTestCases.forEach { case ->
            assertTrue(case.singularValue.isSingular, "Tested: $case")
        }
    }
}

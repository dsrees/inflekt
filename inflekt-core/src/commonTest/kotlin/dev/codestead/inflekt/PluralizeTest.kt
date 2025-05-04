package dev.codestead.inflekt

import kotlin.test.Test
import kotlin.test.assertEquals

class PluralizeTest {
    @Test
    fun `automatically converts - plural`() {
        pluralTestCases.forEach { case ->
            // Make sure the word stays pluralized.
            case.pluralValue
                .pluralize(5)
                .also { pluralResult ->
                    assertEquals(case.pluralValue, pluralResult, "Tested: $case")
                }

            // Make sure the word becomes a plural.
            if (case.singularValue != case.pluralValue) {
                case.singularValue
                    .pluralize(5)
                    .also { pluralResult ->
                        assertEquals(case.pluralValue, pluralResult, "Tested: $case")
                    }
            }
        }
    }

    @Test
    fun `automatically converts - singular`() {
        singularTestCases.forEach { case ->
            // Make sure the word stays singular.
            case.singularValue
                .singularize()
                .also { singularResult ->
                    assertEquals(case.singularValue, singularResult, "Tested: $case")
                }

            // Make sure the word becomes singular.
            if (case.singularValue != case.pluralValue) {
                case.pluralValue
                    .singularize()
                    .also { singularResult ->
                        assertEquals(case.singularValue, singularResult, "Tested: $case")
                    }
            }
        }
    }

    @Test
    fun `prepend count - plural`() {
        "test"
            .pluralize(5, true)
            .also { result -> assertEquals("5 tests", result) }
    }

    @Test
    fun `prepend count - singular`() {
        "test"
            .singularize(true)
            .also { result -> assertEquals("1 test", result) }
    }

    @Test
    fun `adding new rules - uncountable rules`() {
        assertEquals("papers", "paper".pluralize())
        Inflekt.addUncountableRule("paper")
        assertEquals("paper", "paper".pluralize())

        Inflekt.resetRules()
        assertEquals("papers", "paper".pluralize())
    }

    @Test
    fun `adding new rules - should allow new irregular words`() {
        assertEquals("irregulars", "irregular".pluralize())
        Inflekt.addIrregularRule("irregular", "regular")
        assertEquals("regular", "irregular".pluralize())

        Inflekt.resetRules()
        assertEquals("irregulars", "irregular".pluralize())
    }

    @Test
    fun `adding new rules - should allow new plural matching rules`() {
        assertEquals("regexes", "regex".pluralize())
        Inflekt.addPluralRule(Regex("gex$", RegexOption.IGNORE_CASE), "gexii")
        assertEquals("regexii", "regex".pluralize())

        Inflekt.resetRules()
        assertEquals("regexes", "regex".pluralize())
    }

    @Test
    fun `adding new rules - should allow new singular matching rules`() {
        assertEquals("single", "singles".singularize())
        Inflekt.addSingularRule(Regex("singles$"), "singular")
        assertEquals("singular", "singles".singularize())

        Inflekt.resetRules()
        assertEquals("single", "singles".singularize())
    }

    @Test
    fun `adding new rules - should allow new plural matching rules to be strings`() {
        assertEquals("people", "person".pluralize())
        Inflekt.addPluralRule("person" to "peeps")
        assertEquals("peeps", "person".pluralize())

        Inflekt.resetRules()
        assertEquals("people", "person".pluralize())
    }

    @Test
    fun `adding new rules - should allow new singular matching rules to be strings`() {
        assertEquals("morning", "mornings".singularize())
        Inflekt.addSingularRule("mornings", "suck")
        assertEquals("suck", "mornings".singularize())

        Inflekt.resetRules()
        assertEquals("morning", "mornings".singularize())
    }
}

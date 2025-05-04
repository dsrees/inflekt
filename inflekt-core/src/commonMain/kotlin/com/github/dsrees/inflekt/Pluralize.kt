package com.github.dsrees.inflekt

import kotlin.math.abs

/**
 * Pluralize or singularize a word based on the passed in [count]. The default behavior
 * is to pluralize the word.
 *
 * ```kotlin
 * "duck".pluralize() // "ducks"
 * "duck".pluralize(3, true) // "3 ducks"
 * "ducks".pluralize(1) // "duck"
 * "ducks".pluralize(, true) // "1 duck"
 * ```
 *
 * @param count     How many of the word exist. Default 2
 * @param inclusive Whether to prefix with the number (e.g. 3 ducks). Default false
 * @return The inflected word.
 */
fun String.pluralize(
    count: Int = 2,
    inclusive: Boolean = false,
): String {
    val pluralized =
        when (abs(count)) {
            1 -> Inflekt.singular(word = this)
            else -> Inflekt.plural(word = this)
        }

    return when (inclusive) {
        true -> "$count $pluralized"
        false -> pluralized
    }
}

/**
 * Helper method of [pluralize] which will always singularize a word.
 *
 * ```kotlin
 * "ducks".singularize() // "duck"
 * "ducks".singularize(true) // "1 duck"
 * "duck".singularize() // "duck"
 * "duck".singularize(true) // "1 duck"
 * ```
 *
 * @param inclusive Whether to prefix with the number (e.g. 1 duck). Default false
 * @return The inflected word.
 */
fun String.singularize(inclusive: Boolean = false) = pluralize(1, inclusive)

/**
 * Check if a word is plural.
 *
 * ```kotlin
 * "duck".isPlural // false
 * "ducks".isPlural // true
 * ```
 */
val String.isPlural: Boolean
    get() = Inflekt.isPlural(this)

/**
 * Check if a word is singular.
 *
 * ```kotlin
 * "duck".isSingular // true
 * "ducks".isSingular // false
 * ```
 */
val String.isSingular: Boolean
    get() = Inflekt.isSingular(this)

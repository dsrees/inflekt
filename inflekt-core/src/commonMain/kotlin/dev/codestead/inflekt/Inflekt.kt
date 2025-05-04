package dev.codestead.inflekt

private data class Rule(
    val rule: Regex,
    val replacement: String,
) {
    fun test(word: String): Boolean = rule.containsMatchIn(word)
}

private typealias RuleSet = MutableList<Rule>
private typealias IrregularMap = MutableMap<String, String>

private val <A, B> Pair<A, B>.rule
    get() = first

private val <A, B> Pair<A, B>.replacement
    get() = second

object Inflekt {
    private val pluralRules: RuleSet = mutableListOf()
    private val singularRules: RuleSet = mutableListOf()
    private val uncountables = mutableSetOf<String>()
    private val irregularPlurals: IrregularMap = mutableMapOf()
    private val irregularSingles: IrregularMap = mutableMapOf()

    init {
        resetRules()
    }

    /**
     * Pluralize a [word].
     *
     * @param word
     * @return The inflected [word].
     */
    fun plural(word: String): String = replaceWord(word, irregularSingles, irregularPlurals, pluralRules)

    /**
     * Check if a word is plural.
     *
     * @param word
     * @return if the [word] is plural.
     */
    fun isPlural(word: String): Boolean = checkWord(word, irregularSingles, irregularPlurals, pluralRules)

    /**
     * Singularize a [word].
     *
     * @param word
     * @return The inflected [word].
     */
    fun singular(word: String): String = replaceWord(word, irregularPlurals, irregularSingles, singularRules)

    /**
     * Check if a [word] is singular.
     *
     * @param word
     * @return if the [word] is singular.
     */
    fun isSingular(word: String): Boolean = checkWord(word, irregularPlurals, irregularSingles, singularRules)

    private fun replaceWord(
        word: String,
        replace: IrregularMap,
        keep: IrregularMap,
        rules: RuleSet,
    ): String {
        // Get the correct token and case restoration functions.
        val token = word.lowercase()

        // Check against the keep object map.
        if (keep.containsKey(token)) {
            return restoreCase(word, token)
        }

        // Check against the replacement map for a direct word replacement.
        replace[token]?.let {
            return restoreCase(word, it)
        }

        // Run all the rules against the word.
        return sanitizeWord(token, word, rules)
    }

    private fun checkWord(
        word: String,
        replace: IrregularMap,
        keep: IrregularMap,
        rules: RuleSet,
    ): Boolean {
        val token = word.lowercase()

        return when {
            keep.containsKey(token) -> true
            replace.containsKey(token) -> false
            else -> sanitizeWord(token, token, rules) == token
        }
    }

    private fun sanitizeWord(
        token: String,
        word: String,
        rules: RuleSet,
    ): String {
        // Empty string or doesn't need fixing.
        if (token.isBlank() || uncountables.contains(token)) return word

        rules.reversed().forEach { rule ->
            if (rule.test(word)) return replace(word, rule)
        }

        return word
    }

    private fun sanitizeRule(rule: String): Regex = Regex("^$rule$", RegexOption.IGNORE_CASE)

    private fun replace(
        word: String,
        rule: Rule,
    ): String =
        word.replace(rule.rule) { matchResult ->
            val args = listOf(matchResult.value) + matchResult.groupValues.drop(1)
            val result = interpolate(rule.replacement, args)

            when {
                matchResult.value.isEmpty() -> {
                    val index = matchResult.range.first
                    val referenceChar = word.getOrNull(index - 1) ?: ' '
                    restoreCase(referenceChar.toString(), result)
                }

                else -> restoreCase(matchResult.value, result)
            }
        }

    private fun interpolate(
        str: String,
        args: List<String>,
    ): String =
        """\$(\d{1,2})"""
            .toRegex()
            .replace(str) { matchResult ->
                val index = matchResult.groupValues[1].toIntOrNull()
                if (index != null && index < args.size) args[index] else ""
            }

    private fun restoreCase(
        word: String,
        token: String,
    ): String {
        // Tokens are an exact match.
        if (word == token) return token

        // Lower cased words. E.g. "hello".
        if (word === word.lowercase()) return token.lowercase()

        // Upper cased words. E.g. "WHISKY".
        if (word === word.uppercase()) return token.uppercase()

        // Title cased words. E.g. "Title".
        if (word[0] == word[0].uppercaseChar()) {
            return token
                .replaceFirstChar { it.uppercase() }
        }

        // Lower cased words. E.g. "test".
        return token.lowercase()
    }

    /**
     * Add a pluralization rule to the collection. [RuleType] must be either
     * a [String] or [Regex]
     *
     * ```kotlin
     * addPluralRule("person" to "peeps")
     * addPluralRule(Regex("gex$", RegexOption.IGNORE_CASE) to "gexii")
     * ```
     *
     * @param pair
     * @throws [IllegalArgumentException] if [RuleType] was not a [String] or [Regex]
     */
    fun <RuleType> addPluralRule(pair: Pair<RuleType, String>) {
        when (val rule = pair.rule) {
            is Regex -> addPluralRule(rule, pair.replacement)
            is String -> addPluralRule(rule, pair.replacement)
            else -> throw IllegalArgumentException("A rule must be either String or Regex")
        }
    }

    /**
     * Add a pluralization rule to the collection.
     * Does not expect a regex pattern, pass a [Regex] instead.
     *
     * ```kotlin
     * addPluralRule("person", "peeps")
     * ```
     *
     * @param rule
     * @param replacement
     */
    fun addPluralRule(
        rule: String,
        replacement: String,
    ) = addPluralRule(sanitizeRule(rule) to replacement)

    /**
     * Add a pluralization rule to the collection.
     *
     * ```kotlin
     * addPluralRule(Regex("gex$", RegexOption.IGNORE_CASE), "gexii")
     * ```
     *
     * @param rule
     * @param replacement
     */
    fun addPluralRule(
        rule: Regex,
        replacement: String,
    ) = pluralRules.add(Rule(rule, replacement))

    /**
     * Add a singularization rule to the collection. [RuleType] must be either
     * a [String] or [Regex]
     *
     * ```kotlin
     * addSingularRule("singles" to "singular")
     * addSingularRule(Regex("singles$") to "singular")
     * ```
     *
     * @param pair
     * @
     * @throws [IllegalArgumentException] if [RuleType] was not a [String] or [Regex]
     */
    fun <RuleType> addSingularRule(pair: Pair<RuleType, String>) {
        when (val rule = pair.rule) {
            is Regex -> addSingularRule(rule, pair.replacement)
            is String -> addSingularRule(rule, pair.replacement)
            else -> throw IllegalArgumentException("A rule must be either String or Regex")
        }
    }

    /**
     * Add a singularization rule to the collection.
     * Does not expect a regex pattern, pass a [Regex] instead.
     *
     * ```kotlin
     * addSingularRule("singles", "singular")
     * ```
     *
     * @param rule
     * @param replacement
     */
    fun addSingularRule(
        rule: String,
        replacement: String,
    ) = addSingularRule(sanitizeRule(rule) to replacement)

    /**
     * Add a singularization rule to the collection.
     *
     * ```kotlin
     * addSingularRule(Regex("singles$"), "singular")
     * ```
     *
     * @param rule
     * @param replacement
     */
    fun addSingularRule(
        rule: Regex,
        replacement: String,
    ) = singularRules.add(Rule(rule, replacement))

    /**
     * Add a uncountable rule to the collection.
     *
     * ```kotlin
     * addUncountableRule("paper")
     * ```
     *
     * @param word
     */
    fun addUncountableRule(word: String) {
        uncountables.add(word.lowercase())
    }

    /**
     * Add a uncountable rule to the collection.
     *
     * ```kotlin
     * addUncountableRule(Regex("sheep$", RegexOption.IGNORE_CASE))
     * ```
     *
     * @param rule
     */
    fun addUncountableRule(rule: Regex) {
        addPluralRule(rule, "$0")
        addSingularRule(rule, "$0")
    }

    /**
     * Add an irregular word definition.
     *
     * ```kotlin
     * addIrregularRule("canvas" to "canvases")
     * ```
     *
     * @param pair
     */
    fun addIrregularRule(pair: Pair<String, String>) {
        addIrregularRule(pair.rule, pair.replacement)
    }

    /**
     * Add an irregular word definition.
     *
     * ```kotlin
     * addIrregularRule("canvas", "canvases")
     * ```
     *
     * @param single The singular version of the word. e.g. "I"
     * @param plural The plural version of the word. e.g. "we"
     */
    fun addIrregularRule(
        single: String,
        plural: String,
    ) {
        val pluralLc = plural.lowercase()
        val singleLc = single.lowercase()

        irregularSingles[singleLc] = pluralLc
        irregularPlurals[pluralLc] = singleLc
    }

    /**
     * Resets the ruleset, removing any added custom rules.
     */
    fun resetRules() {
        // Clear all default rules plus additionally added rules
        pluralRules.clear()
        singularRules.clear()
        uncountables.clear()
        irregularPlurals.clear()
        irregularSingles.clear()

        // Restore defaults
        seedIrregularRules()
        seedPluralizationRules()
        seedSingularizationRules()
        seedUncountableRules()
    }

    private fun seedIrregularRules() {
        listOf(
            "I" to "we",
            "me" to "us",
            "he" to "they",
            "she" to "they",
            "them" to "them",
            "myself" to "ourselves",
            "yourself" to "yourselves",
            "itself" to "themselves",
            "herself" to "themselves",
            "himself" to "themselves",
            "themself" to "themselves",
            "is" to "are",
            "was" to "were",
            "has" to "have",
            "this" to "these",
            "that" to "those",
            "my" to "our",
            "its" to "their",
            "his" to "their",
            "her" to "their",
            // Words ending in with a consonant and `o`.
            "echo" to "echoes",
            "dingo" to "dingoes",
            "volcano" to "volcanoes",
            "tornado" to "tornadoes",
            "torpedo" to "torpedoes",
            // Ends with `us`.
            "genus" to "genera",
            "viscus" to "viscera",
            // Ends with `ma`.
            "stigma" to "stigmata",
            "stoma" to "stomata",
            "dogma" to "dogmata",
            "lemma" to "lemmata",
            "schema" to "schemata",
            "anathema" to "anathemata",
            // Other irregular rules.
            "ox" to "oxen",
            "axe" to "axes",
            "die" to "dice",
            "yes" to "yeses",
            "foot" to "feet",
            "eave" to "eaves",
            "goose" to "geese",
            "tooth" to "teeth",
            "quiz" to "quizzes",
            "human" to "humans",
            "proof" to "proofs",
            "carve" to "carves",
            "valve" to "valves",
            "looey" to "looies",
            "thief" to "thieves",
            "groove" to "grooves",
            "pickaxe" to "pickaxes",
            "passerby" to "passersby",
            "canvas" to "canvases",
        ).forEach(::addIrregularRule)
    }

    private fun seedPluralizationRules() {
        listOf(
            "s?\$" to "s",
            "[^\\u0000-\\u007F]\$" to "\$0",
            "([^aeiou]ese)\$" to "\$1",
            "(ax|test)is\$" to "\$1es",
            "(alias|[^aou]us|t[lm]as|gas|ris)\$" to "\$1es",
            "(e[mn]u)s?\$" to "\$1s",
            "([^l]ias|[aeiou]las|[ejzr]as|[iu]am)\$" to "\$1",
            "(alumn|syllab|vir|radi|nucle|fung|cact|stimul|termin|bacill|foc|uter|loc|strat)(?:us|i)\$" to "\$1i",
            "(alumn|alg|vertebr)(?:a|ae)\$" to "\$1ae",
            "(seraph|cherub)(?:im)?\$" to "\$1im",
            "(her|at|gr)o\$" to "\$1oes",
            "(agend|addend|millenni|dat|extrem|bacteri|desiderat|strat|candelabr|errat|ov|symposi|curricul|automat|quor)(?:a|um)\$" to
                "\$1a",
            "(apheli|hyperbat|periheli|asyndet|noumen|phenomen|criteri|organ|prolegomen|hedr|automat)(?:a|on)\$" to "\$1a",
            "sis\$" to "ses",
            "(?:(kni|wi|li)fe|(ar|l|ea|eo|oa|hoo)f)\$" to "\$1\$2ves",
            "([^aeiouy]|qu)y\$" to "\$1ies",
            "([^ch][ieo][ln])ey\$" to "\$1ies",
            "(x|ch|ss|sh|zz)\$" to "\$1es",
            "(matr|cod|mur|sil|vert|ind|append)(?:ix|ex)\$" to "\$1ices",
            "\\b((?:tit)?m|l)(?:ice|ouse)\$" to "\$1ice",
            "(pe)(?:rson|ople)\$" to "\$1ople",
            "(child)(?:ren)?\$" to "\$1ren",
            "eaux\$" to "\$0",
            "m[ae]n\$" to "men",
        ).map { it.rule.toRegex(RegexOption.IGNORE_CASE) to it.replacement }
            .forEach(::addPluralRule)

        // Literal string rules
        listOf(
            "thou" to "you",
        ).forEach(::addPluralRule)
    }

    private fun seedSingularizationRules() {
        // Regex rules
        listOf(
            "s\$" to "",
            "(ss)\$" to "\$1",
            "(wi|kni|(?:after|half|high|low|mid|non|night|[^\\w]|^)li)ves\$" to "\$1fe",
            "(ar|(?:wo|[ae])l|[eo][ao])ves\$" to "\$1f",
            "ies\$" to "y",
            "(dg|ss|ois|lk|ok|wn|mb|th|ch|ec|oal|is|ck|ix|sser|ts|wb)ies\$" to "\$1ie",
            "\\b(l|(?:neck|cross|hog|aun)?t|coll|faer|food|gen|goon|group|hipp|junk|vegg|(?:pork)?p|charl|calor|cut)ies\$" to "\$1ie",
            "\\b(mon|smil)ies\$" to "\$1ey",
            "\\b((?:tit)?m|l)ice\$" to "\$1ouse",
            "(seraph|cherub)im\$" to "\$1",
            "(x|ch|ss|sh|zz|tto|go|cho|alias|[^aou]us|t[lm]as|gas|(?:her|at|gr)o|[aeiou]ris)(?:es)?\$" to "\$1",
            "(analy|diagno|parenthe|progno|synop|the|empha|cri|ne)(?:sis|ses)\$" to "\$1sis",
            "(movie|twelve|abuse|e[mn]u)s\$" to "\$1",
            "(test)(?:is|es)\$" to "\$1is",
            "(alumn|syllab|vir|radi|nucle|fung|cact|stimul|termin|bacill|foc|uter|loc|strat)(?:us|i)\$" to "\$1us",
            "(agend|addend|millenni|dat|extrem|bacteri|desiderat|strat|candelabr|errat|ov|symposi|curricul|quor)a\$" to "\$1um",
            "(apheli|hyperbat|periheli|asyndet|noumen|phenomen|criteri|organ|prolegomen|hedr|automat)a\$" to "\$1on",
            "(alumn|alg|vertebr)ae\$" to "\$1a",
            "(cod|mur|sil|vert|ind)ices\$" to "\$1ex",
            "(matr|append)ices\$" to "\$1ix",
            "(pe)(rson|ople)\$" to "\$1rson",
            "(child)ren\$" to "\$1",
            "(eau)x?\$" to "\$1",
            "men\$" to "man",
        ).map { it.rule.toRegex(RegexOption.IGNORE_CASE) to it.replacement }
            .forEach(::addSingularRule)
    }

    private fun seedUncountableRules() {
        // Literal string rules
        listOf(
            "adulthood",
            "advice",
            "agenda",
            "aid",
            "aircraft",
            "alcohol",
            "ammo",
            "analytics",
            "anime",
            "athletics",
            "audio",
            "bison",
            "blood",
            "bream",
            "buffalo",
            "butter",
            "carp",
            "cash",
            "chassis",
            "chess",
            "clothing",
            "cod",
            "commerce",
            "cooperation",
            "corps",
            "debris",
            "diabetes",
            "digestion",
            "elk",
            "energy",
            "equipment",
            "excretion",
            "expertise",
            "firmware",
            "flounder",
            "fun",
            "gallows",
            "garbage",
            "graffiti",
            "hardware",
            "headquarters",
            "health",
            "herpes",
            "highjinks",
            "homework",
            "housework",
            "information",
            "jeans",
            "justice",
            "kudos",
            "labour",
            "literature",
            "machinery",
            "mackerel",
            "mail",
            "media",
            "mews",
            "moose",
            "music",
            "mud",
            "manga",
            "news",
            "only",
            "personnel",
            "pike",
            "plankton",
            "pliers",
            "police",
            "pollution",
            "premises",
            "rain",
            "research",
            "rice",
            "salmon",
            "scissors",
            "series",
            "sewage",
            "shambles",
            "shrimp",
            "software",
            "staff",
            "swine",
            "tennis",
            "traffic",
            "transportation",
            "trout",
            "tuna",
            "wealth",
            "welfare",
            "whiting",
            "wildebeest",
            "wildlife",
            "you",
        ).forEach(::addUncountableRule)

        // Regex rules
        listOf(
            "pok[eé]mon$",
            "[^aeiou]ese$", // "chinese", "japanese"
            "deer$", // "deer", "reindeer"
            "fish$", // "fish", "blowfish", "angelfish"
            "measles$",
            "o[iu]s$", // "carnivorous"
            "pox$", // "chickpox", "smallpox"
            "sheep$",
        ).map { it.toRegex(RegexOption.IGNORE_CASE) }
            .forEach(::addUncountableRule)
    }
}

package dev.codestead.inflekt

private data class Rule(
    val rule: Regex,
    val replacement: String,
) {
    fun test(word: String): Boolean = rule.containsMatchIn(word)
}

private typealias RuleSet = MutableList<Rule>
private typealias IrregularMap = MutableMap<String, String>

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
     * Pluralize a word.
     *
     * @param word
     * @return
     */
    fun plural(word: String): String = replaceWord(word, irregularSingles, irregularPlurals, pluralRules)

    /**
     * Check if a word is plural.
     *
     * @param word
     * @return
     */
    fun isPlural(word: String): Boolean = checkWord(word, irregularSingles, irregularPlurals, pluralRules)

    /**
     * Singularize a word.
     *
     * @param word
     * @return
     */
    fun singular(word: String): String = replaceWord(word, irregularPlurals, irregularSingles, singularRules)

    /**
     * Check if a word is singular.
     *
     * @param word
     * @return
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
        replace[token]?.let { return restoreCase(word, it) }

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
     * Add a pluralization rule to the collection.
     *
     * @param rule
     * @param replacement
     */
    fun addPluralRule(
        rule: String,
        replacement: String,
    ) = addPluralRule(rule.toRegex(RegexOption.IGNORE_CASE), replacement)

    /**
     * Add a pluralization rule to the collection.
     *
     * @param pair
     */
    fun addPluralRule(pair: Pair<String, String>) = addPluralRule(pair.first, pair.second)

    /**
     * Add a pluralization rule to the collection.
     *
     * @param pair
     */
    fun addPluralRule(pair: Pair<Regex, String>) = addPluralRule(pair.first, pair.second)

    /**
     * Add a pluralization rule to the collection.
     *
     * @param rule
     * @param replacement
     */
    fun addPluralRule(
        rule: Regex,
        replacement: String,
    ) {
        Rule(rule, replacement)
            .also { pluralRules.add(it) }
    }

    /**
     * Add a singularization rule to the collection.
     *
     * @param rule
     * @param replacement
     */
    fun addSingularRule(
        rule: String,
        replacement: String,
    ) = addSingularRule(rule.toRegex(RegexOption.IGNORE_CASE), replacement)

    /**
     * Add a singularization rule to the collection.
     *
     * @param pair
     */
    fun addSingularRule(pair: Pair<String, String>) = addSingularRule(pair.first, pair.second)

    /**
     * Add a singularization rule to the collection.
     *
     * @param rule
     * @param replacement
     */
    fun addSingularRule(
        rule: Regex,
        replacement: String,
    ) {
        Rule(rule, replacement)
            .also { singularRules.add(it) }
    }

    /**
     * Add a uncountable rule to the collection.
     *
     * @param word
     */
    fun addUncountableRule(word: String) {
        uncountables.add(word.lowercase())
    }

    /**
     * Add a uncountable rule to the collection.
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
     * @param pair
     */
    fun addIrregularRule(pair: Pair<String, String>) {
        addIrregularRule(pair.first, pair.second)
    }

    /**
     * Add an irregular word definition.
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
     *
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
            "^thou\$" to "you",
        ).map { it.first.toRegex(RegexOption.IGNORE_CASE) to it.second }
            .forEach(::addPluralRule)

        listOf(
            "thou" to "you",
        ).map { it.first.toRegex(RegexOption.IGNORE_CASE) to it.second }
    }

    private fun seedSingularizationRules() {
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
        ).forEach(::addSingularRule)
    }

    private fun seedUncountableRules() {
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

        listOf(
            Regex("pok[eé]mon$", RegexOption.IGNORE_CASE),
            Regex("[^aeiou]ese$", RegexOption.IGNORE_CASE), // "chinese", "japanese"
            Regex("deer$", RegexOption.IGNORE_CASE), // "deer", "reindeer"
            Regex("fish$", RegexOption.IGNORE_CASE), // "fish", "blowfish", "angelfish"
            Regex("measles$", RegexOption.IGNORE_CASE),
            Regex("o[iu]s$", RegexOption.IGNORE_CASE), // "carnivorous"
            Regex("pox$", RegexOption.IGNORE_CASE), // "chickpox", "smallpox"
            Regex("sheep$", RegexOption.IGNORE_CASE),
        ).forEach(::addUncountableRule)
    }
}

# inflekt

A Kotlin Multiplatform inflection library.

> **Inflection** \
> /ĭn-flĕk′shən/
> 
> An alteration of the form of a word by the addition of an affix or by changing the form of a base that indicates grammatical features such as number, person, mood, or tense.

---

### Installation

<!-- x-release-please-start-version -->
```kotlin
implementation("com.github.dsrees:inflekt:0.2.0")
```
<!-- x-release-please-end -->

Supported targets:

- Android and JVM
- Apple: `iosArm64`, `iosSimulatorArm64`, `macosArm64`, `tvosArm64`, `tvosSimulatorArm64`,
  `watchosArm64`, `watchosSimulatorArm64`
- Linux: `linuxX64`, `linuxArm64`
- Windows: `mingwX64`

The same coordinate works from Kotlin Multiplatform, Android, and plain `kotlin("jvm")` modules;
Gradle picks the right variant automatically.

Requires Kotlin 2.4 or newer in consuming projects.

### How to Use

This library provides extensions for pluralizing and singularizing English words. It also supports
adding custom rules.

#### Pluralizing
```kotlin
// Simple Examples
"test".pluralize() // "tests"
"test".pluralize(0) // "tests"
"test".pluralize(1) // "test"
"test".pluralize(5) // "tests"
"test".pluralize(1, inclusive = true) // "1 test"
"test".pluralize(5, inclusive = true) // "5 tests"
"蘋果".pluralize(2, inclusive = true) // "2 蘋果"
"foot".pluralize() // "feet"
"Alumnus".pluralize() // "Alumni"
```

#### Singularizing

The `singularize` method can be used in place of `pluralize` with a `count` of `1`.

```kotlin
"tests".singularize() // "test"
"tests".singularize(inclusive = true) // "1 test"
"feet".singularize() // "foot"
"Alumni".singularize() // "Alumnus"
```

#### Adding Custom Rules

In addition to the default rules, you can add your own custom rules. Rules
can be defined as matching strings or custom Regex.

```kotlin
// Example of new plural rule
"regex".pluralize() // regexes
Inflekt.addPluralRule(Regex("gex$", RegexOption.IGNORE_CASE), "gexii")
"regex".pluralize() // "regexii"

// Example of new singular rule
"singles".singularize() // "single"
Inflekt.addSingularRule(Regex("singles$", RegexOption.IGNORE_CASE), "singular")
"singles".singularize() // "singular"

// Example of new irregular rule, e.g. "I" -> "we"
"irregular".pluralize() // "irregulars"
Inflekt.addIrregularRule("irregular", "regular")
"irregular".pluralize() // regular

// Example of uncountable rule (rules without singular/plural in context)
"paper".pluralize() // "papers"
Inflekt.addUncountableRule("paper")
"paper".pluralize() // "paper"
```

#### Checking Pluralization

Inflekt provides helper methods to check if a word is pluralized or not.

```kotlin
"test".isPlural // false
"test".isSingular // true
```

Additional examples can be found the in tests.

### Attribution

This library is almost a direct port [plurals/pluralize](https://github.com/plurals/pluralize) in 
both API surface and functionality. If you're familiar with `pluralize`, then this should feel 
familiar. Thanks to all who have contributed to that projected!

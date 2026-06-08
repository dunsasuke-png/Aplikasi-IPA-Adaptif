package com.app.manfaattumbuhan.domain.fuzzy

import kotlin.math.max
import kotlin.math.min

object FuzzyMamdani {

    data class FuzzyResult(
        val outputValue: Double,
        val outputLevel: String,
        val details: FuzzyDetails
    )

    data class FuzzyDetails(
        val ketepatan: Double,
        val kecepatan: Double,
        val tingkatSebelumnya: Double,
        val membershipKetepatan: Map<String, Double>,
        val membershipKecepatan: Map<String, Double>,
        val membershipTingkatSebelumnya: Map<String, Double>,
        val activeRules: List<ActiveRule>,
        val defuzzifiedValue: Double
    )

    data class ActiveRule(
        val ruleNumber: Int,
        val ketepatan: String,
        val kecepatan: String,
        val tingkatSebelumnya: String,
        val output: String,
        val firingStrength: Double
    )

    // Triangular membership function: (a, b, c)
    // a = lower bound (mu=0), b = peak (mu=1), c = upper bound (mu=0)
    private fun triangularMF(x: Double, a: Double, b: Double, c: Double): Double {
        return when {
            x < a || x > c -> 0.0
            x == b -> 1.0
            x >= a && x < b -> (x - a) / (b - a)
            x > b && x <= c -> (c - x) / (c - b)
            else -> 0.0
        }
    }

    // Trapezoidal membership function: (a, b, c, d)
    // Supports left shoulder (a==b && b==c) and right shoulder (b==c && c==d)
    private fun trapezoidalMF(x: Double, a: Double, b: Double, c: Double, d: Double): Double {
        return when {
            x < a || x > d -> 0.0
            x >= b && x <= c -> 1.0
            x >= a && x < b -> (x - a) / (b - a)
            x > c && x <= d -> (d - x) / (d - c)
            else -> 0.0
        }
    }

    // === MEMBERSHIP FUNCTIONS ===

    // Ketepatan (accuracy %, 0-100)
    private fun ketepatan_rendah(x: Double) = trapezoidalMF(x, 0.0, 0.0, 40.0, 50.0)
    private fun ketepatan_sedang(x: Double) = triangularMF(x, 40.0, 60.0, 80.0)
    private fun ketepatan_tinggi(x: Double) = trapezoidalMF(x, 70.0, 80.0, 100.0, 100.0)

    // Kecepatan (time in seconds per session, 0-1000)
    private fun kecepatan_cepat(x: Double) = trapezoidalMF(x, 0.0, 0.0, 360.0, 480.0)
    private fun kecepatan_sedang(x: Double) = triangularMF(x, 360.0, 480.0, 780.0)
    private fun kecepatan_lambat(x: Double) = trapezoidalMF(x, 480.0, 780.0, 1000.0, 1000.0)

    // Tingkat Kesulitan Sebelumnya (0-100)
    private fun tingkatSblm_mudah(x: Double) = trapezoidalMF(x, 0.0, 0.0, 30.0, 40.0)
    private fun tingkatSblm_sedang(x: Double) = triangularMF(x, 30.0, 50.0, 70.0)
    private fun tingkatSblm_sulit(x: Double) = trapezoidalMF(x, 60.0, 70.0, 100.0, 100.0)

    // Output - Tingkat Kesulitan (0-100)
    private fun output_mudah(x: Double) = trapezoidalMF(x, 0.0, 0.0, 30.0, 40.0)
    private fun output_sedang(x: Double) = triangularMF(x, 30.0, 50.0, 70.0)
    private fun output_sulit(x: Double) = trapezoidalMF(x, 60.0, 70.0, 100.0, 100.0)

    // Rule base: 27 rules
    // (ketepatan, kecepatan, tingkatSebelumnya) -> output
    private data class Rule(
        val number: Int,
        val ketepatan: String,
        val kecepatan: String,
        val tingkatSebelumnya: String,
        val output: String
    )

    private val rules = listOf(
        // Tingkat sebelumnya = Mudah (rules 1-9)
        Rule(1, "Tinggi", "Cepat", "Mudah", "Sedang"),
        Rule(2, "Tinggi", "Sedang", "Mudah", "Sedang"),
        Rule(3, "Tinggi", "Lambat", "Mudah", "Mudah"),
        Rule(4, "Sedang", "Cepat", "Mudah", "Mudah"),
        Rule(5, "Sedang", "Sedang", "Mudah", "Mudah"),
        Rule(6, "Sedang", "Lambat", "Mudah", "Mudah"),
        Rule(7, "Rendah", "Cepat", "Mudah", "Mudah"),
        Rule(8, "Rendah", "Sedang", "Mudah", "Mudah"),
        Rule(9, "Rendah", "Lambat", "Mudah", "Mudah"),

        // Tingkat sebelumnya = Sedang (rules 10-18)
        Rule(10, "Tinggi", "Cepat", "Sedang", "Sulit"),
        Rule(11, "Tinggi", "Sedang", "Sedang", "Sulit"),
        Rule(12, "Tinggi", "Lambat", "Sedang", "Sedang"),
        Rule(13, "Sedang", "Cepat", "Sedang", "Sedang"),
        Rule(14, "Sedang", "Sedang", "Sedang", "Sedang"),
        Rule(15, "Sedang", "Lambat", "Sedang", "Mudah"),
        Rule(16, "Rendah", "Cepat", "Sedang", "Mudah"),
        Rule(17, "Rendah", "Sedang", "Sedang", "Mudah"),
        Rule(18, "Rendah", "Lambat", "Sedang", "Mudah"),

        // Tingkat sebelumnya = Sulit (rules 19-27)
        Rule(19, "Tinggi", "Cepat", "Sulit", "Sulit"),
        Rule(20, "Tinggi", "Sedang", "Sulit", "Sulit"),
        Rule(21, "Tinggi", "Lambat", "Sulit", "Sulit"),
        Rule(22, "Sedang", "Cepat", "Sulit", "Sulit"),
        Rule(23, "Sedang", "Sedang", "Sulit", "Sedang"),
        Rule(24, "Sedang", "Lambat", "Sulit", "Sedang"),
        Rule(25, "Rendah", "Cepat", "Sulit", "Sedang"),
        Rule(26, "Rendah", "Sedang", "Sulit", "Sedang"),
        Rule(27, "Rendah", "Lambat", "Sulit", "Mudah")
    )

    // Pre-test rule base: 9 rules
    private val pretestRules = listOf(
        Rule(1, "Tinggi", "Cepat", "Mudah", "Sulit"),
        Rule(2, "Tinggi", "Sedang", "Mudah", "Sulit"),
        Rule(3, "Tinggi", "Lambat", "Mudah", "Sedang"),
        Rule(4, "Sedang", "Cepat", "Mudah", "Sedang"),
        Rule(5, "Sedang", "Sedang", "Mudah", "Sedang"),
        Rule(6, "Sedang", "Lambat", "Mudah", "Mudah"),
        Rule(7, "Rendah", "Cepat", "Mudah", "Mudah"),
        Rule(8, "Rendah", "Sedang", "Mudah", "Mudah"),
        Rule(9, "Rendah", "Lambat", "Mudah", "Mudah")
    )

    /**
     * Calculate fuzzy output
     * @param ketepatan Percentage of correct answers (0-100)
     * @param kecepatanDetik Total time spent in seconds (0-1000)
     * @param tingkatSebelumnya Previous difficulty level value (0-100). Use 0.0 for pre-test.
     */
    fun calculate(ketepatan: Double, kecepatanDetik: Double, tingkatSebelumnya: Double): FuzzyResult {
        // Step 1: Fuzzification
        val muKetepatan = mapOf(
            "Rendah" to ketepatan_rendah(ketepatan),
            "Sedang" to ketepatan_sedang(ketepatan),
            "Tinggi" to ketepatan_tinggi(ketepatan)
        )

        val muKecepatan = mapOf(
            "Cepat" to kecepatan_cepat(kecepatanDetik),
            "Sedang" to kecepatan_sedang(kecepatanDetik),
            "Lambat" to kecepatan_lambat(kecepatanDetik)
        )

        val muTingkatSblm = mapOf(
            "Mudah" to tingkatSblm_mudah(tingkatSebelumnya),
            "Sedang" to tingkatSblm_sedang(tingkatSebelumnya),
            "Sulit" to tingkatSblm_sulit(tingkatSebelumnya)
        )

        // Step 2: Rule evaluation (AND = min)
        val activeRules = mutableListOf<ActiveRule>()
        val outputAggregation = mutableMapOf(
            "Mudah" to 0.0,
            "Sedang" to 0.0,
            "Sulit" to 0.0
        )

        val isPretest = tingkatSebelumnya == 0.0
        val activeRulesList = if (isPretest) pretestRules else rules

        for (rule in activeRulesList) {
            val muK = muKetepatan[rule.ketepatan] ?: 0.0
            val muKec = muKecepatan[rule.kecepatan] ?: 0.0
            val muTS = if (isPretest) 1.0 else muTingkatSblm[rule.tingkatSebelumnya] ?: 0.0

            val firingStrength = min(muK, min(muKec, muTS))

            if (firingStrength > 0.0) {
                activeRules.add(
                    ActiveRule(
                        ruleNumber = rule.number,
                        ketepatan = rule.ketepatan,
                        kecepatan = rule.kecepatan,
                        tingkatSebelumnya = rule.tingkatSebelumnya,
                        output = rule.output,
                        firingStrength = firingStrength
                    )
                )

                // Aggregation (MAX for same output categories)
                outputAggregation[rule.output] = max(
                    outputAggregation[rule.output] ?: 0.0,
                    firingStrength
                )
            }
        }

        // Step 3: Defuzzification (Centroid method)
        val defuzzified = defuzzifyCentroid(outputAggregation)

        // Determine output level
        val outputLevel = when {
            defuzzified < 30.0 -> "Mudah"
            defuzzified < 60.0 -> "Sedang"
            else -> "Sulit"
        }

        return FuzzyResult(
            outputValue = defuzzified,
            outputLevel = outputLevel,
            details = FuzzyDetails(
                ketepatan = ketepatan,
                kecepatan = kecepatanDetik,
                tingkatSebelumnya = tingkatSebelumnya,
                membershipKetepatan = muKetepatan,
                membershipKecepatan = muKecepatan,
                membershipTingkatSebelumnya = muTingkatSblm,
                activeRules = activeRules,
                defuzzifiedValue = defuzzified
            )
        )
    }

    /**
     * Centroid defuzzification using numerical integration
     */
    private fun defuzzifyCentroid(aggregatedOutput: Map<String, Double>): Double {
        val step = 0.5
        var numerator = 0.0
        var denominator = 0.0

        var x = 0.0
        while (x <= 100.0) {
            // Calculate aggregated membership for this x
            val muMudah = min(aggregatedOutput["Mudah"] ?: 0.0, output_mudah(x))
            val muSedang = min(aggregatedOutput["Sedang"] ?: 0.0, output_sedang(x))
            val muSulit = min(aggregatedOutput["Sulit"] ?: 0.0, output_sulit(x))

            // Take max (union of all output sets)
            val mu = max(muMudah, max(muSedang, muSulit))

            numerator += x * mu
            denominator += mu

            x += step
        }

        return if (denominator > 0.0) numerator / denominator else 0.0
    }

    /**
     * Convert fuzzy output value to level string for display
     */
    fun outputValueToLevel(value: Double): String {
        return when {
            value < 30.0 -> "Mudah"
            value < 60.0 -> "Sedang"
            else -> "Sulit"
        }
    }
}

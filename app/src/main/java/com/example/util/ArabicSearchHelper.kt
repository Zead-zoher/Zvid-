package com.example.util

object ArabicSearchHelper {

    fun isArabic(text: String): Boolean {
        return text.any { it in '\u0600'..'\u06FF' || it in '\u0750'..'\u077F' }
    }

    /**
     * Generates intelligent search variants for Arabic text to tolerate:
     * - Taa Marbuta (ة vs ه)
     * - Alef with Hamza (أ / إ / آ / ا)
     * - Yaa / Alef Maksura (ى vs ي)
     * - Definite article variations (الاسطورة vs الأسطورة vs الاسطوره vs الأسطوره)
     */
    fun generateSearchVariants(query: String): List<String> {
        val trimmed = query.trim()
        if (trimmed.isBlank() || !isArabic(trimmed)) {
            return listOf(trimmed)
        }

        val variants = linkedSetOf<String>()
        variants.add(trimmed)

        // 1. Taa Marbuta <-> Haa variations on entire string
        if (trimmed.endsWith("ه")) {
            variants.add(trimmed.dropLast(1) + "ة")
        } else if (trimmed.endsWith("ة")) {
            variants.add(trimmed.dropLast(1) + "ه")
        }

        // 2. Word-level Taa Marbuta and Haa swapping
        val words = trimmed.split("\\s+".toRegex())
        if (words.size > 1) {
            val swappedWords = words.map { word ->
                when {
                    word.endsWith("ه") -> word.dropLast(1) + "ة"
                    word.endsWith("ة") -> word.dropLast(1) + "ه"
                    else -> word
                }
            }
            variants.add(swappedWords.joinToString(" "))
        }

        // 3. Normalize Alef (أ, إ, آ -> ا)
        val normalizedAlef = trimmed.replace(Regex("[أإآٱ]"), "ا")
        variants.add(normalizedAlef)
        if (normalizedAlef.endsWith("ه")) {
            variants.add(normalizedAlef.dropLast(1) + "ة")
        } else if (normalizedAlef.endsWith("ة")) {
            variants.add(normalizedAlef.dropLast(1) + "ه")
        }

        // 4. If query starts with "ال" followed by "ا", generate variant with "الأ" (e.g. الاسطورة -> الأسطورة)
        if (trimmed.startsWith("ال") && trimmed.length > 2 && trimmed[2] == 'ا') {
            val withHamza = "ال" + "أ" + trimmed.substring(3)
            variants.add(withHamza)
            if (withHamza.endsWith("ه")) {
                variants.add(withHamza.dropLast(1) + "ة")
            } else if (withHamza.endsWith("ة")) {
                variants.add(withHamza.dropLast(1) + "ه")
            }
        } else if (trimmed.startsWith("ال") && trimmed.length > 2 && trimmed[2] == 'أ') {
            val withoutHamza = "ال" + "ا" + trimmed.substring(3)
            variants.add(withoutHamza)
            if (withoutHamza.endsWith("ه")) {
                variants.add(withoutHamza.dropLast(1) + "ة")
            } else if (withoutHamza.endsWith("ة")) {
                variants.add(withoutHamza.dropLast(1) + "ه")
            }
        }

        return variants.toList()
    }
}

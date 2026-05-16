package com.example.chaithra.utils

import android.util.Log
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions

object TranslatorUtils {

    private val options = TranslatorOptions.Builder()
        .setSourceLanguage(TranslateLanguage.ENGLISH)
        .setTargetLanguage(TranslateLanguage.KANNADA)
        .build()

    private val englishKannadaTranslator = Translation.getClient(options)

    /**
     * Translates English text to Kannada.
     * Automatically handles model downloading if not present.
     */
    fun translate(text: String, onComplete: (String) -> Unit) {
        if (text.isBlank()) {
            onComplete(text)
            return
        }

        englishKannadaTranslator.downloadModelIfNeeded()
            .addOnSuccessListener {
                englishKannadaTranslator.translate(text)
                    .addOnSuccessListener { translatedText: String -> // Added ': String'
                        onComplete(translatedText)
                    }
                    .addOnFailureListener { exception: Exception -> // Added ': Exception'
                        onComplete(text)
                    }
            }
            .addOnFailureListener { exception: Exception -> // Added ': Exception'
                onComplete("Check Internet Connection")
            }
    }

    /**
     * Call this when the app starts (e.g., in MainActivity)
     * to pre-download the language pack so it's ready offline.
     */
    fun prepareModel() {
        englishKannadaTranslator.downloadModelIfNeeded()
            .addOnSuccessListener { Log.d("Translator", "Kannada model ready.") }
    }
}
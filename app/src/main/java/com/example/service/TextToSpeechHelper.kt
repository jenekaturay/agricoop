package com.example.service

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TextToSpeechHelper(context: Context) : TextToSpeech.OnInitListener {

    private val applicationContext = context.applicationContext
    private var tts: TextToSpeech? = TextToSpeech(applicationContext, this)

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w("TTSHelper", "US English Language is not supported or missing data on this device.")
            } else {
                _isReady.value = true
                tts?.setSpeechRate(0.95f) // Slightly slower for crisp clear listening
                tts?.setPitch(1.0f)
            }
        } else {
            Log.e("TTSHelper", "TextToSpeech initialization failed with status: $status")
        }
    }

    fun speakText(text: String, queueMode: Int = TextToSpeech.QUEUE_FLUSH) {
        if (tts == null || !_isReady.value) {
            Log.w("TTSHelper", "TTS not initialized yet. Skipping speak: $text")
            return
        }
        _isSpeaking.value = true
        tts?.speak(text, queueMode, null, "UTTERANCE_ID_${System.currentTimeMillis()}")
    }

    fun speakWeightConfirmation(
        farmerName: String,
        cropType: String,
        weightKg: Double,
        payoutLrd: Double,
        locationName: String = ""
    ) {
        val formattedCrop = when (cropType.uppercase()) {
            "CASSAVA" -> "Cassava tubers"
            "YAM" -> "Yam roots"
            "SWEET_POTATO" -> "Sweet potatoes"
            else -> cropType
        }
        val text = "Weight recorded! %.1f kilograms of %s for farmer %s. Total payout: %,.0f Liberian Dollars.%s".format(
            weightKg,
            formattedCrop,
            farmerName,
            payoutLrd,
            if (locationName.isNotBlank()) " Recorded at $locationName." else ""
        )
        speakText(text)
    }

    fun speakReceiptSummary(
        batchCode: String,
        farmerName: String,
        cropType: String,
        weightKg: Double,
        starchPct: Double,
        payoutLrd: Double
    ) {
        val text = "Receipt for Batch %s. Farmer: %s. Crop: %s. Weight: %.1f kilograms. Starch density: %.1f percent. Net payout: %,.0f Liberian Dollars.".format(
            batchCode.replace("-", " "),
            farmerName,
            cropType,
            weightKg,
            starchPct,
            payoutLrd
        )
        speakText(text)
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
            _isReady.value = false
            _isSpeaking.value = false
        } catch (e: Exception) {
            Log.e("TTSHelper", "Error shutting down TTS", e)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: TextToSpeechHelper? = null

        fun getInstance(context: Context): TextToSpeechHelper {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TextToSpeechHelper(context.applicationContext).also { INSTANCE = it }
            }
        }

        fun speak(context: Context, text: String) {
            try {
                getInstance(context).speakText(text)
            } catch (e: Exception) {
                Log.e("TTSHelper", "Error speaking text via helper", e)
            }
        }
    }
}

package com.example.core.voice

import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Production Crash-Safe Multi-Language Android TextToSpeech (TTS) Voice Assistant Engine for WarpDrop.
 * - By default, speaks standard international English: "Tap SEND to share files or scan QR code to connect."
 * - If the system locale is Hindi ("hi"), speaks Hindi: "फाइल भेजने के लिए Send दबाएं या दूसरे फोन का QR कोड स्कैन करें।"
 * - Never navigates away from the current screen and never attempts any device connection.
 */
class VayuVoiceAssistantEngine(context: Context) {

    companion object {
        const val ENGLISH_SPEECH_PHRASE =
            "Tap SEND to share files or scan QR code to connect."

        const val ENGLISH_VOICE_BANNER_MESSAGE =
            "🔊 WarpDrop Voice Assistant: Tap SEND to share files or scan QR code to connect."

        const val HINDI_SPEECH_PHRASE =
            "फाइल भेजने के लिए Send दबाएं या दूसरे फोन का QR कोड स्कैन करें।"

        const val HINDI_VOICE_BANNER_MESSAGE =
            "🔊 WarpDrop Voice Assistant: फाइल भेजने के लिए Send दबाएं या दूसरे फोन का QR कोड स्कैन करें।"

        const val DEFAULT_TRANSFER_GUIDE_TEXT = ENGLISH_VOICE_BANNER_MESSAGE
        const val HINDI_TRANSFER_GUIDE_TEXT = HINDI_VOICE_BANNER_MESSAGE

        fun isHindiLocale(locale: Locale = Locale.getDefault()): Boolean {
            return locale.language.equals("hi", ignoreCase = true)
        }

        fun resolveSpeechPhrase(locale: Locale = Locale.getDefault()): String {
            return if (isHindiLocale(locale)) HINDI_SPEECH_PHRASE else ENGLISH_SPEECH_PHRASE
        }

        fun resolveBannerMessage(locale: Locale = Locale.getDefault()): String {
            return if (isHindiLocale(locale)) HINDI_VOICE_BANNER_MESSAGE else ENGLISH_VOICE_BANNER_MESSAGE
        }
    }

    private val appContext = context.applicationContext
    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var isTtsInitAttempted = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _activePromptText = MutableStateFlow<String?>(null)
    val activePromptText: StateFlow<String?> = _activePromptText.asStateFlow()

    /**
     * Speaks voice guidance based on the provided or system [locale].
     * Defaults to standard international English unless the system locale is Hindi (`hi`).
     * Wrapped in a safe try-catch so if TTS is unavailable on the emulator, it gracefully
     * displays the floating banner without any crash or screen transition.
     */
    @Suppress("DEPRECATION")
    fun speakGuidance(locale: Locale = Locale.getDefault()): String {
        val bannerMessage = resolveBannerMessage(locale)
        val speechPhrase = resolveSpeechPhrase(locale)
        val useHindi = isHindiLocale(locale)
        speakWithLocale(
            bannerMessage = bannerMessage,
            speechPhrase = speechPhrase,
            targetLocale = if (useHindi) Locale("hi", "IN") else Locale.US
        )
        return bannerMessage
    }

    @Suppress("DEPRECATION")
    fun speakHindiGuidance(
        bannerMessage: String = resolveBannerMessage(Locale.getDefault()),
        speechPhrase: String = resolveSpeechPhrase(Locale.getDefault()),
        locale: Locale = Locale.getDefault()
    ) {
        val useHindi = isHindiLocale(locale)
        speakWithLocale(
            bannerMessage = bannerMessage,
            speechPhrase = speechPhrase,
            targetLocale = if (useHindi) Locale("hi", "IN") else Locale.US
        )
    }

    @Suppress("DEPRECATION")
    private fun speakWithLocale(
        bannerMessage: String,
        speechPhrase: String,
        targetLocale: Locale
    ) {
        _activePromptText.value = bannerMessage
        _isSpeaking.value = true

        try {
            val ttsServices = appContext.packageManager.queryIntentServices(
                Intent(TextToSpeech.Engine.INTENT_ACTION_TTS_SERVICE),
                0
            )
            if (ttsServices.isNullOrEmpty()) {
                return
            }

            if (tts != null && isTtsReady) {
                applyLocaleSafely(targetLocale)
                speakPhraseSafely(speechPhrase)
                return
            }

            if (!isTtsInitAttempted) {
                isTtsInitAttempted = true
                tts = TextToSpeech(appContext) { status ->
                    try {
                        if (status == TextToSpeech.SUCCESS) {
                            applyLocaleSafely(targetLocale)
                            tts?.setSpeechRate(0.98f)
                            tts?.setPitch(1.0f)
                            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                                override fun onStart(utteranceId: String?) {
                                    _isSpeaking.value = true
                                }

                                override fun onDone(utteranceId: String?) {
                                    _isSpeaking.value = false
                                }

                                @Deprecated("Deprecated in Java")
                                override fun onError(utteranceId: String?) {
                                    _isSpeaking.value = false
                                }
                            })
                            isTtsReady = true
                            speakPhraseSafely(speechPhrase)
                        }
                    } catch (_: Throwable) {
                        // Safe fallback: banner remains visible on screen
                    }
                }
            }
        } catch (_: Throwable) {
            // Safe fallback: banner remains visible on screen
        }
    }

    private fun applyLocaleSafely(targetLocale: Locale) {
        try {
            val result = tts?.setLanguage(targetLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED
            ) {
                tts?.setLanguage(Locale.US)
            }
        } catch (_: Throwable) {
        }
    }

    fun dismissVoiceBanner() {
        try {
            tts?.stop()
        } catch (_: Throwable) {
        }
        _isSpeaking.value = false
        _activePromptText.value = null
    }

    private fun speakPhraseSafely(phrase: String) {
        try {
            tts?.speak(
                phrase,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "warpdrop_voice_${System.currentTimeMillis()}"
            )
        } catch (_: Throwable) {
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
        } catch (_: Throwable) {
        }
    }
}

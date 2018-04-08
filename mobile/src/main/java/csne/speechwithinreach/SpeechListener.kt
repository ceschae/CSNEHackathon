package csne.speechwithinreach

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

import kotlinx.android.synthetic.main.activity_record_speech.*
import kotlinx.android.synthetic.main.content_record_speech.*

/**
 * Created by cesch on 4/7/2018.
 */
class SpeechListener(private val context: Context,
                     private val parent: MainActivity) : RecognitionListener {

    private lateinit var sRecognizer : SpeechRecognizer
    private lateinit var rIntent : Intent
    private var isListening = false
    public var currText = ""

    fun initialize() {
        rIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        rIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)
        rIntent.putExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES, true)
        rIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        rIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        rIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        rIntent.putExtra(RecognizerIntent.EXTRA_RESULTS, true)

        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            sRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            sRecognizer.setRecognitionListener(this)
        }
    }

    fun startListening() {
        currText = "in startListening..."
        if (!isListening) {
            currText = "isListening..."
            isListening = true
            sRecognizer.startListening(rIntent)
        }
    }

    fun stopRecognition() {
        sRecognizer.stopListening()
    }

    private fun onResults(results: List<String>, scores: FloatArray?) {
        parent.pushFinalResult(results, scores)
    }

    override fun onResults(results: Bundle) {
        val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val scores = results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
        if (matches != null) {
            onResults(matches, scores)
        }
        startListening()
    }

    override fun onReadyForSpeech(params: Bundle) {
        // why
    }

    override fun onRmsChanged(p0: Float) {
        // why
    }

    override fun onBufferReceived(p0: ByteArray?) {
        // why
    }

    override fun onPartialResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val scores = results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
        if (matches != null) {
            parent.pushPartial(matches, scores)
        }
    }

    override fun onError(p0: Int) {
        currText = "" + p0
    }

    override fun onEvent(p0: Int, p1: Bundle?) {
        currText = "" + p0
    }

    override fun onBeginningOfSpeech() {
        // why
    }

    override fun onEndOfSpeech() {
        currText = "hit the end!"
        isListening = false
    }
}
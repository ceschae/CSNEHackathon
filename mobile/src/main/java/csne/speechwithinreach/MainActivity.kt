package csne.speechwithinreach

import android.content.pm.PackageManager
import android.Manifest
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem

import kotlinx.android.synthetic.main.activity_record_speech.*
import kotlinx.android.synthetic.main.content_record_speech.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val AUDIO_PERMISSION_CODE = 101
    }

    private lateinit var sListener : SpeechListener
    private var results = ResultMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_speech)
        setSupportActionBar(toolbar)


        sListener = SpeechListener(this, this)
        sListener.initialize()

        fab.setOnClickListener {
            askFirst()
        }

        stopButton.setOnClickListener {
            stopRecord()
        }
    }

    fun askFirst() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), AUDIO_PERMISSION_CODE)
        } else {
            recordText()
        }
    }

    fun pushPartial(results: List<String>?, scores: FloatArray?) {
        textView.text = results?.joinToString(separator = "\n") + "\n" + scores?.joinToString(separator = "\n")
    }

    fun pushFinalResult(sentences: List<String>, scores: FloatArray?) {
        for (i in 0..(sentences.size - 1)) {
            var sentence = sentences.get(i)
            var score = scores?.get(i)
            if (score != null) {
                if (score > 0) {
                    sentence = sentence.toLowerCase()
                    var words = sentence.split(" ")
                    for (word in words) {
                        results.put(word, score)
                    }
                }
            }
        }
    }

    fun stopRecord() {
        textView.text = sListener.currText
        if (textView.text == "") {
            textView.text = "Hmm... no data"
        }
    }

    fun recordText() {
        textView.text = "Recording..."
        sListener.startListening()
        textView.text = sListener.currText
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_record_speech, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            AUDIO_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    recordText()
                }
            }
        }
    }

    private inner class ResultMap {
        val keySet = ResultHashSet()

        fun put(word: String, rating: Float) {
            var tempResult = keySet.get(word)
            if (tempResult == null) {
                keySet.add(SentenceResult(word, rating))
            } else {
                tempResult.addRating(rating)
            }
        }
    }

    private inner class ResultHashSet {
        private val size = 100663319
        private val timesPrime = 389
        val resultArr = arrayOfNulls<SentenceResult>(size)

        fun add(value: SentenceResult) {
            var hashCode = hashCode(value.word)
            var soFar = 1
            while (resultArr[hashCode] != null) {
                hashCode += soFar * soFar
                soFar++
            }
            resultArr[hashCode] = value
        }

        fun get(word: String): SentenceResult? {
            var hashCode = hashCode(word)
            var soFar = 1
            while (resultArr[hashCode] != null && !resultArr[hashCode]?.word.equals(word)) {
                hashCode += soFar * soFar
                soFar++
            }
            if (resultArr[hashCode] != null) {
                return resultArr[hashCode]
            } else {
                return null
            }
        }

        private fun hashCode(word: String): Int {
            var sum = 0
            for (i in 1..word.length) {
                sum += word[i].toInt() * i
            }
            return sum % size
        }
    }

    private inner class SentenceResult(val word: String,
                                       private var rating: Float) {
        private var countSaid = 1

        fun addRating(newRating: Float) {
            var totalFloat = rating * countSaid
            countSaid++
            totalFloat += newRating
            rating = totalFloat / countSaid
        }

        override fun equals(other: Any?): Boolean {
            if (other is SentenceResult) {
                return word.equals(other.word)
            } else {
                return false
            }
        }
    }
}

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
import android.media.MediaRecorder
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import org.json.JSONObject
import java.net.URL
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {

    companion object {
        const val AUDIO_PERMISSION_CODE = 101
        const val API_URL = "https://api.datamuse.com/words?"
        const val WORD_QUERY = "sl="
        const val MUST_HAVE_QUERY = "&max=1&md=r&qe=sl"
    }

    private lateinit var sListener : SpeechListener
    //private lateinit var recorder : MediaRecorder
    private lateinit var wordStatsRef : DatabaseReference
    private val wordMap : Map<String, WordInfo> = HashMap<String, WordInfo>()

    data class WordInfo(var word: String = "", var average: Double = 0.0, var count: Long = 0L)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_speech)
        setSupportActionBar(toolbar)

        val database = FirebaseDatabase.getInstance()
        wordStatsRef = database.getReference("word-stats")
        wordStatsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot!!.exists()){
                    val children = dataSnapshot!!.children
                    children.forEach {
                        var item : WordInfo? = it.getValue(WordInfo::class.java)
                        if (item != null) {
                            var tempWord = item.word
                            if (wordMap.containsKey(tempWord)) {
                                var tempItem = wordMap.get(tempWord)
                                tempItem?.average = item.average
                                tempItem?.count = item.count
                            } else {
                                (wordMap as HashMap<String, WordInfo>).put(tempWord, item)
                            }
                        }
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })

        sListener = SpeechListener(this, this)
        sListener.initialize()

        //recorder = MediaRecorder()
        //recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        //recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        //recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

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
                        if (wordMap.containsKey(word)) {
                            val item = wordMap.get(word)
                            var currTotal = item!!.count * item!!.average
                            currTotal += score.toDouble()
                            item!!.count++
                            item!!.average = currTotal / item!!.count
                        } else {
                            (wordMap as HashMap<String, WordInfo>).put(word, WordInfo(word, score.toDouble(), 1L))
                        }
                    }
                }
                wordStatsRef.updateChildren(wordMap)
            }
        }
    }

    fun stopRecord() {
        sListener.stopRecognition()
        textView.text = sListener.currText
        //recorder.stop();
        //recorder.reset();
        if (textView.text == "") {
            textView.text = "Hmm... no data"
        }
    }

    fun recordText() {
        textView.text = "Recording..."
        //recorder.prepare()
        //recorder.start()
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
}

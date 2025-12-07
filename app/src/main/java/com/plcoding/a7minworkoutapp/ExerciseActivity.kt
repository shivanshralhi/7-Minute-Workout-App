package com.plcoding.a7minworkoutapp

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.plcoding.a7minworkoutapp.databinding.ActivityExerciseBinding
import com.plcoding.a7minworkoutapp.databinding.DialogCustomBackConfirmationBinding
import java.util.Locale

class ExerciseActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private var binding: ActivityExerciseBinding? = null

    // pause tracking
    private var totalPauseTime: Long = 0L
    private var pauseStartTime: Long = 0L
    private var pausesCount = 0
    private var quitEarly = false

    // timers
    private var restTimer: CountDownTimer? = null
    private var exerciseTimer: CountDownTimer? = null

    // durations (seconds)
    private var restDuration: Long = 10
    private var exerciseDuration: Long = 30

    private var restTimeLeft: Long = 0
    private var exerciseTimeLeft: Long = 0

    // exercise list & state
    private var exerciseList: ArrayList<ExerciseModel>? = null
    private var currentExercisePosition = -1

    // TTS / media / voice
    private var tts: TextToSpeech? = null
    private var player: MediaPlayer? = null
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var listenIntent: Intent

    private var isRestPaused = false
    private var isExercisePaused = false

    private var exerciseAdapter: ExerciseStatusAdapter? = null

    companion object {
        private const val REQUEST_RECORD_AUDIO = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExerciseBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // check audio permission for speech recognizer (runtime)
        ensureAudioPermission()

        /** --------- Load User Settings --------- */
        val prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE)
        val bodyType = prefs.getString("BODY_TYPE", "Average")
        val strength = prefs.getString("STRENGTH_LEVEL", "Beginner")

        /** --------- Build exercise list based on body type --------- */
        exerciseList = when (bodyType) {
            "Slim" -> ArrayList(Constants.defaultExerciseList().take(8))
            "Average" -> ArrayList(Constants.defaultExerciseList().take(10))
            "Overweight" -> ArrayList(Constants.defaultExerciseList().take(12))
            "Athletic" -> {
                // If Athletic, add 2 more items (if available)
                val base = Constants.defaultExerciseList()
                val extra = base.take(2)
                ArrayList(base + extra)
            }
            else -> ArrayList(Constants.defaultExerciseList())
        }

        /** --------- Strength-based durations (seconds) --------- */
        when (strength) {
            "Beginner" -> {
                exerciseDuration = 20L
                restDuration = 12L
            }
            "Intermediate" -> {
                exerciseDuration = 30L
                restDuration = 10L
            }
            "Advanced" -> {
                exerciseDuration = 40L
                restDuration = 8L
            }
            else -> {
                exerciseDuration = 30L
                restDuration = 10L
            }
        }

        /** --------- Smart auto adjustment (reads last performance prefs) --------- */
        applySmartAdjustment()

        restTimeLeft = restDuration * 1000
        exerciseTimeLeft = exerciseDuration * 1000

        Log.d("PREF", "-- USER SETTINGS --")
        Log.d("PREF", "BodyType = $bodyType")
        Log.d("PREF", "Strength = $strength")
        Log.d("PREF", "Exercises = ${exerciseList!!.size}")
        Log.d("PREF", "ExerciseDuration = $exerciseDuration")
        Log.d("PREF", "RestDuration = $restDuration")

        /** ------- Toolbar ------- */
        setSupportActionBar(binding?.toolbarExercise)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding?.toolbarExercise?.setNavigationOnClickListener { customBackDialog() }

        tts = TextToSpeech(this, this)

        /** Setup UI pieces */
        setupExerciseRecycler()
        setupVoiceRecognition()

        // Start from rest view (first rest before exercise)
        setUpRestView()
    }

    private fun ensureAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO)
        }
    }

    /** REST VIEW */
    private fun setUpRestView() {
        runOnUiThread {
            // 1. Show the Rest View stuff
            binding?.flRestView?.visibility = View.VISIBLE
            binding?.tvTitle?.visibility = View.VISIBLE // "GET READY FOR"
            binding?.tvUpcomingLabel?.visibility = View.VISIBLE
            binding?.tvUpComingExerciseName?.visibility = View.VISIBLE

            // 2. HIDE the Exercise stuff (Image, Name, Exercise Timer)
            binding?.flExerciseView?.visibility = View.GONE
            binding?.ivImage?.visibility = View.INVISIBLE // <--- Hides the image
            binding?.tvExerciseName?.visibility = View.GONE

            // ... Set text for upcoming exercise ...
            val next = currentExercisePosition + 1
            if (next in 0 until (exerciseList?.size ?: 0)) {
                binding?.tvUpComingExerciseName?.text = exerciseList!![next].getName()
            } else {
                binding?.tvUpComingExerciseName?.text = ""
            }

            // ... Play sound code ...
            try {
                val soundUri = Uri.parse("android.resource://com.plcoding.a7minworkoutapp/" + R.raw.press_start)
                player?.reset()
                player = MediaPlayer.create(this, soundUri)
                player?.start()
            } catch (_: Exception) {}
        }

        restTimer?.cancel()
        restTimeLeft = restDuration * 1000
        setRestProgress()
    }


    private fun setRestProgress() {
        // set progressbar max once
        binding?.progressBar?.max = restDuration.toInt()

        restTimer = object : CountDownTimer(restTimeLeft, 1000) {
            override fun onTick(ms: Long) {
                restTimeLeft = ms
                val secLeft = (ms / 1000).toInt()

                // show remaining seconds
                binding?.progressBar?.progress = secLeft
                binding?.tvTimer?.text = secLeft.toString()
            }

            override fun onFinish() {
                // increment exercise index and start exercise
                currentExercisePosition++
                if (currentExercisePosition in 0 until (exerciseList?.size ?: 0)) {
                    exerciseList!![currentExercisePosition].setIsSelected(true)
                    exerciseAdapter?.notifyDataSetChanged()
                    setUpExerciseView()
                } else {
                    // nothing to start — finish
                    finish()
                }
            }
        }.start()
    }

    /** EXERCISE VIEW */
    private fun setUpExerciseView() {
        if (currentExercisePosition !in 0 until (exerciseList?.size ?: 0)) {
            finish()
            return
        }

        runOnUiThread {
            // 1. HIDE the Rest View stuff
            binding?.flRestView?.visibility = View.GONE
            binding?.tvTitle?.visibility = View.GONE
            binding?.tvUpcomingLabel?.visibility = View.GONE
            binding?.tvUpComingExerciseName?.visibility = View.GONE

            // 2. SHOW the Exercise stuff
            binding?.flExerciseView?.visibility = View.VISIBLE // Shows the timer
            binding?.ivImage?.visibility = View.VISIBLE        // <--- THIS WAS MISSING!
            binding?.tvExerciseName?.visibility = View.VISIBLE // <--- THIS WAS MISSING!

            val model = exerciseList!![currentExercisePosition]
            binding?.ivImage?.setImageResource(model.getImage())
            binding?.tvExerciseName?.text = model.getName()

            tts?.speak(model.getName(), TextToSpeech.QUEUE_FLUSH, null, model.getName())

            binding?.progressBarExercise?.max = exerciseDuration.toInt()
            binding?.progressBarExercise?.progress = exerciseDuration.toInt()
            binding?.tvTimerExercise?.text = exerciseDuration.toString()
        }

        exerciseTimer?.cancel()
        exerciseTimeLeft = exerciseDuration * 1000
        setExerciseProgress()
    }


    private fun setExerciseProgress() {
        exerciseTimer = object : CountDownTimer(exerciseTimeLeft, 1000) {
            override fun onTick(ms: Long) {
                exerciseTimeLeft = ms
                val secLeft = (ms / 1000).toInt()

                binding?.progressBarExercise?.progress = secLeft
                binding?.tvTimerExercise?.text = secLeft.toString()
            }

            override fun onFinish() {
                // mark completed
                if (currentExercisePosition in 0 until (exerciseList?.size ?: 0)) {
                    exerciseList!![currentExercisePosition].apply {
                        setIsCompleted(true)
                        setIsSelected(false)
                    }
                }
                exerciseAdapter?.notifyDataSetChanged()

                if (currentExercisePosition < (exerciseList!!.size - 1)) {
                    setUpRestView()
                } else {
                    quitEarly = false
                    savePerformanceData()
                    finish()
                    startActivity(Intent(this@ExerciseActivity, FinishActivity::class.java))
                }
            }
        }.start()
    }

    /** Recycler setup */
    private fun setupExerciseRecycler() {
        binding?.rvExerciseStatus?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        exerciseAdapter = ExerciseStatusAdapter(exerciseList ?: ArrayList())
        binding?.rvExerciseStatus?.adapter = exerciseAdapter
    }

    /** SMART ADJUST */
    private fun applySmartAdjustment() {
        val perfPrefs = getSharedPreferences("performance_prefs", MODE_PRIVATE)
        val lastPauses = perfPrefs.getInt("pauses", 0)
        val lastQuit = perfPrefs.getBoolean("quit_early", false)
        val lastCompleted = perfPrefs.getBoolean("completed", true)
        val lastPauseTimeSec = perfPrefs.getLong("pause_time", 0L) / 1000L

        if (lastQuit) {
            exerciseDuration = (exerciseDuration - 5).coerceAtLeast(15L)
            restDuration = (restDuration + 3).coerceAtMost(30L)
        } else if (lastPauseTimeSec > 10 || lastPauses >= 4) {
            exerciseDuration = (exerciseDuration - 3).coerceAtLeast(15L)
            restDuration = (restDuration + 2).coerceAtMost(30L)
        } else if (lastPauseTimeSec < 3 && lastPauses == 0 && lastCompleted) {
            exerciseDuration = (exerciseDuration + 5).coerceAtMost(60L)
            restDuration = (restDuration - 1).coerceAtLeast(3L)
        }

        perfPrefs.edit().clear().apply()
    }

    /** VOICE COMMAND */
    private fun setupVoiceRecognition() {
        // make sure permission exists
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // permission not granted; will request in ensureAudioPermission
            return
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        listenIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        listenIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        listenIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(bundle: Bundle?) {
                val cmd = bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.lowercase()
                if (cmd != null) handleVoiceCommand(cmd)
                // restart listening
                try { speechRecognizer.startListening(listenIntent) } catch (_: Exception) {}
            }

            override fun onError(error: Int) {
                try { speechRecognizer.startListening(listenIntent) } catch (_: Exception) {}
            }

            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onEndOfSpeech() {}
        })
    }

    override fun onResume() {
        super.onResume()
        // start listening only if permission granted & recognizer initialized
        if (::speechRecognizer.isInitialized && ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            try { speechRecognizer.startListening(listenIntent) } catch (_: Exception) {}
        }
    }

    private fun handleVoiceCommand(command: String) {
        when {
            "stop" in command || "pause" in command -> pauseTimers()
            "start" in command || "resume" in command -> resumeTimers()
        }
    }

    private fun pauseTimers() {
        pausesCount++
        pauseStartTime = System.currentTimeMillis()
        Toast.makeText(this, "Paused", Toast.LENGTH_SHORT).show()

        if (binding?.flRestView?.visibility == View.VISIBLE) {
            restTimer?.cancel()
            isRestPaused = true
        }

        if (binding?.flExerciseView?.visibility == View.VISIBLE) {
            exerciseTimer?.cancel()
            isExercisePaused = true
        }
    }

    private fun resumeTimers() {
        if (pauseStartTime != 0L) {
            val pauseDuration = System.currentTimeMillis() - pauseStartTime
            totalPauseTime += pauseDuration
            pauseStartTime = 0L
        }

        Toast.makeText(this, "Resumed", Toast.LENGTH_SHORT).show()

        if (isRestPaused) {
            isRestPaused = false
            setRestProgress()
        }

        if (isExercisePaused) {
            isExercisePaused = false
            setExerciseProgress()
        }
    }

    private fun savePerformanceData() {
        val prefs = getSharedPreferences("performance_prefs", MODE_PRIVATE)
        prefs.edit().apply {
            putInt("pauses", pausesCount)
            putLong("pause_time", totalPauseTime)
            putBoolean("quit_early", quitEarly)
            putBoolean("completed", !quitEarly)
            apply()
        }
    }

    private fun customBackDialog() {
        val dialog = Dialog(this)
        val bind = DialogCustomBackConfirmationBinding.inflate(layoutInflater)
        dialog.setContentView(bind.root)
        dialog.setCancelable(false)
        bind.btnYes.setOnClickListener {
            quitEarly = true
            savePerformanceData()
            dialog.dismiss()
            finish()
        }
        bind.btnNo.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    override fun onDestroy() {
        restTimer?.cancel()
        exerciseTimer?.cancel()
        tts?.stop()
        tts?.shutdown()
        player?.stop()
        try { if (::speechRecognizer.isInitialized) speechRecognizer.destroy() } catch (_: Exception) {}
        binding = null
        super.onDestroy()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.ENGLISH
        }
    }
}

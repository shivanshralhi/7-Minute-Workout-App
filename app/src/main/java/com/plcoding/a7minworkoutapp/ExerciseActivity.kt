package com.plcoding.a7minworkoutapp

import android.app.Dialog
import android.content.Intent
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.plcoding.a7minworkoutapp.databinding.ActivityExerciseBinding
import com.plcoding.a7minworkoutapp.databinding.DialogCustomBackConfirmationBinding
import java.util.Locale

class ExerciseActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private var binding: ActivityExerciseBinding? = null

    // Performance tracking
    private var pausesCount = 0
    private var quitEarly = false

    // Timers
    private var restTimer: CountDownTimer? = null
    private var exerciseTimer: CountDownTimer? = null
    private var restTimeLeft: Long = 0
    private var exerciseTimeLeft: Long = 0

    // Durations (modifiable!)
    private var restDuration: Long = 10
    private var exerciseDuration: Long = 30

    // Progress
    private var restProgress = 0
    private var exerciseProgress = 0

    // Exercise list
    private var exerciseList: ArrayList<ExerciseModel>? = null
    private var currentExercisePosition = -1

    // Media & Voice
    private var tts: TextToSpeech? = null
    private var player: MediaPlayer? = null
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var listenIntent: Intent

    private var isRestPaused = false
    private var isExercisePaused = false

    private var exerciseAdapter: ExerciseStatusAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExerciseBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        /** --------- Load User Settings --------- */
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val bodyType = prefs.getString("body_type", "Average")
        val strength = prefs.getString("strength", "Beginner")

        /** --------- Apply Smart Auto Adjustment BEFORE setting durations --------- */
        applySmartAdjustment()

        /** --------- Apply user strength settings --------- */
        exerciseDuration = when (strength) {
            "Beginner" -> 20L
            "Intermediate" -> 30L
            "Advanced" -> 40L
            else -> 30L
        }

        restDuration = when (strength) {
            "Beginner" -> 12L
            "Intermediate" -> 10L
            "Advanced" -> 8L
            else -> 10L
        }

        /** --------- Adjust exercises by body type --------- */
        exerciseList = when (bodyType) {
            "Slim" -> Constants.defaultExerciseList().take(8).toCollection(ArrayList())
            "Average" -> Constants.defaultExerciseList().take(10).toCollection(ArrayList())
            "Bulk" -> Constants.defaultExerciseList().take(12).toCollection(ArrayList())
            else -> Constants.defaultExerciseList()
        }

        restTimeLeft = restDuration * 1000
        exerciseTimeLeft = exerciseDuration * 1000

        Log.d("PREF", "Body=$bodyType Strength=$strength Exercises=${exerciseList!!.size}")

        /** --------- Toolbar --------- */
        setSupportActionBar(binding?.toolbarExercise)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding?.toolbarExercise?.setNavigationOnClickListener { customBackDialog() }

        /** --------- TTS --------- */
        tts = TextToSpeech(this, this)

        /** --------- Voice Recognition --------- */
        setupVoiceRecognition()

        /** --------- Recycler --------- */
        setUpExerciseView()

        /** --------- Start Workout --------- */
        setUpRestView()
    }

    /*** ============================
     *           REST VIEW
     * ============================ */
    private fun setUpRestView() {
        try {
            val soundUri = Uri.parse("android.resource://com.plcoding.a7minworkoutapp/" + R.raw.press_start)
            player = MediaPlayer.create(this, soundUri)
            player?.start()
        } catch (e: Exception) { }

        binding?.flRestView?.visibility = View.VISIBLE
        binding?.flExerciseView?.visibility = View.INVISIBLE
        binding?.tvTitle?.visibility = View.VISIBLE
        binding?.tvExerciseName?.visibility = View.INVISIBLE
        binding?.ivImage?.visibility = View.INVISIBLE

        binding?.tvUpcomingLabel?.visibility = View.VISIBLE
        binding?.tvUpComingExerciseName?.visibility = View.VISIBLE

        val nextIndex = currentExercisePosition + 1
        if (nextIndex < exerciseList!!.size) {
            binding?.tvUpComingExerciseName?.text = exerciseList!![nextIndex].getName()
        }

        restTimer?.cancel()
        restProgress = 0
        restTimeLeft = restDuration * 1000

        setRestProgress()
    }

    private fun setRestProgress() {
        restTimer = object : CountDownTimer(restTimeLeft, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                restTimeLeft = millisUntilFinished
                val secondsLeft = (restTimeLeft / 1000).toInt()

                binding?.progressBar?.max = restDuration.toInt()
                binding?.progressBar?.progress = secondsLeft
                binding?.tvTimer?.text = secondsLeft.toString()
            }

            override fun onFinish() {
                currentExercisePosition++
                exerciseList!![currentExercisePosition].setIsSelected(true)
                exerciseAdapter!!.notifyDataSetChanged()

                setUpExerciseView()
            }
        }.start()
    }

    /*** ============================
     *         EXERCISE VIEW
     * ============================ */
    private fun setUpExerciseView() {

        binding?.flRestView?.visibility = View.INVISIBLE
        binding?.flExerciseView?.visibility = View.VISIBLE
        binding?.tvTitle?.visibility = View.INVISIBLE
        binding?.tvExerciseName?.visibility = View.VISIBLE
        binding?.ivImage?.visibility = View.VISIBLE

        binding?.tvUpcomingLabel?.visibility = View.INVISIBLE
        binding?.tvUpComingExerciseName?.visibility = View.INVISIBLE

        tts?.speak(exerciseList!![currentExercisePosition].getName(), TextToSpeech.QUEUE_FLUSH, null, "")

        binding?.ivImage?.setImageResource(exerciseList!![currentExercisePosition].getImage())
        binding?.tvExerciseName?.text = exerciseList!![currentExercisePosition].getName()

        exerciseTimer?.cancel()
        exerciseProgress = 0
        exerciseTimeLeft = exerciseDuration * 1000

        setExerciseProgress()
    }

    private fun setExerciseProgress() {
        exerciseTimer = object : CountDownTimer(exerciseTimeLeft, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                exerciseTimeLeft = millisUntilFinished
                val secondsLeft = (exerciseTimeLeft / 1000).toInt()

                binding?.progressBarExercise?.max = exerciseDuration.toInt()
                binding?.progressBarExercise?.progress = secondsLeft
                binding?.tvTimerExercise?.text = secondsLeft.toString()
            }

            override fun onFinish() {
                exerciseList!![currentExercisePosition].apply {
                    setIsSelected(false)
                    setIsCompleted(true)
                }
                exerciseAdapter!!.notifyDataSetChanged()

                if (currentExercisePosition < exerciseList!!.size - 1) {
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

    /*** ============================
     *        AUTO-ADJUST LOGIC
     * ============================ */
    private fun applySmartAdjustment() {

        val perfPrefs = getSharedPreferences("performance_prefs", MODE_PRIVATE)
        val lastPauses = perfPrefs.getInt("pauses", 0)
        val lastQuit = perfPrefs.getBoolean("quit_early", false)
        val lastCompleted = perfPrefs.getBoolean("completed", true)

        /** Adjust exercise duration */
        if (lastQuit) {
            exerciseDuration = (exerciseDuration - 5).coerceAtLeast(15L)
            restDuration = (restDuration + 2).coerceAtMost(30L)
        } else if (lastPauses >= 3) {
            exerciseDuration = (exerciseDuration - 3).coerceAtLeast(15L)
        } else if (lastPauses == 0 && lastCompleted) {
            exerciseDuration = (exerciseDuration + 5).coerceAtMost(60L)
            restDuration = (restDuration - 1).coerceAtLeast(3L)
        }

        /** Adjust number of exercises */
        if (lastQuit) {
            exerciseList = ArrayList(exerciseList?.take((exerciseList!!.size - 1).coerceAtLeast(4)))
        }

        perfPrefs.edit().clear().apply()
    }

    /*** ============================
     *      VOICE COMMAND SYSTEM
     * ============================ */
    private fun setupVoiceRecognition() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        listenIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        listenIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        listenIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(bundle: Bundle?) {
                val result = bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.getOrNull(0)?.lowercase()

                result?.let { handleVoiceCommand(it) }
                speechRecognizer.startListening(listenIntent)
            }

            override fun onError(error: Int) {
                speechRecognizer.startListening(listenIntent)
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
        speechRecognizer.startListening(listenIntent)
    }

    private fun handleVoiceCommand(command: String) {
        when {
            "stop" in command || "pause" in command -> pauseTimers()
            "start" in command || "resume" in command -> resumeTimers()
        }
    }

    private fun pauseTimers() {
        pausesCount++
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

    /*** ============================
     *      PERFORMANCE STORAGE
     * ============================ */
    private fun savePerformanceData() {
        val prefs = getSharedPreferences("performance_prefs", MODE_PRIVATE)
        prefs.edit().apply {
            putInt("pauses", pausesCount)
            putBoolean("quit_early", quitEarly)
            putBoolean("completed", !quitEarly)
            apply()
        }
    }

    /*** ============================
     *      BACK CONFIRMATION
     * ============================ */
    private fun customBackDialog() {
        val dialog = Dialog(this)
        val dialogBinding = DialogCustomBackConfirmationBinding.inflate(layoutInflater)

        dialog.setContentView(dialogBinding.root)
        dialog.setCancelable(false)

        dialogBinding.btnYes.setOnClickListener {
            quitEarly = true
            savePerformanceData()
            dialog.dismiss()
            finish()
        }

        dialogBinding.btnNo.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    /*** ============================
     *         CLEANUP
     * ============================ */
    override fun onDestroy() {
        restTimer?.cancel()
        exerciseTimer?.cancel()

        tts?.stop()
        tts?.shutdown()

        player?.stop()

        binding = null
        super.onDestroy()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.ENGLISH
        }
    }
}

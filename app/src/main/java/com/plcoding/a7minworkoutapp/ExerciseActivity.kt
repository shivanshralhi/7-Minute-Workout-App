package com.plcoding.a7minworkoutapp

import android.app.Dialog
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.plcoding.a7minworkoutapp.databinding.ActivityExerciseBinding
import com.plcoding.a7minworkoutapp.databinding.DialogCustomBackConfirmationBinding
import java.util.Locale

class ExerciseActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private var exerciseBinding : ActivityExerciseBinding? = null

    private var restTimer : CountDownTimer? = null
    private var restProgress = 0
    private var restTimerDuration : Long = 10

    private var restTimerExercise : CountDownTimer? = null
    private var restProgressExercise = 0
    private var exerciseTimerDuration:Long = 30


    private var exerciseList : ArrayList<ExerciseModel>?=null
    private var currentExercisePosition = -1

    private var tts : TextToSpeech? = null
    private var player : MediaPlayer?=null

    private var exerciseAdapter : ExerciseStatusAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exerciseBinding = ActivityExerciseBinding.inflate(layoutInflater)
        setContentView(exerciseBinding?.root)

        setSupportActionBar(exerciseBinding?.toolbarExercise)
        if(supportActionBar!=null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        exerciseBinding?.toolbarExercise?.setNavigationOnClickListener {
            customDialogForBackButton()
        }

        exerciseList = Constants.defaultExerciseList()

        tts = TextToSpeech(this,this)
        exerciseBinding?.toolbarExercise?.setNavigationOnClickListener {
            onBackPressed()
        }
        setUpRestView()
        setUpExerciseRecyclerView()

    }

    private fun customDialogForBackButton() {
        val customDialog = Dialog(this)
        val dialogBinding = DialogCustomBackConfirmationBinding.inflate(layoutInflater)
        customDialog.setContentView(dialogBinding.root)
        customDialog.setCanceledOnTouchOutside(false)
        dialogBinding.btnYes.setOnClickListener {
            this@ExerciseActivity.finish()
            customDialog.dismiss()

        }
        dialogBinding.btnNo.setOnClickListener {
            customDialog.dismiss()

        }
        customDialog.show()

    }

    override fun onBackPressed() {
        customDialogForBackButton()
        //super.onBackPressed()
    }

    private fun setUpExerciseRecyclerView(){
        exerciseBinding?.rvExerciseStatus?.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)

        exerciseAdapter  = ExerciseStatusAdapter(exerciseList!!)
        exerciseBinding?.rvExerciseStatus?.adapter = exerciseAdapter
    }

    private fun setUpRestView(){
        try{
            val soundUri = Uri.parse("android.resorce://com.plcoding.a7minworkoutapp/" + R.raw.press_start)
            player = MediaPlayer.create(applicationContext,soundUri)
            player?.isLooping = false
            player?.start()
        }catch(e:Exception){
            e.printStackTrace()

        }


        exerciseBinding?.flRestView?.visibility = View.VISIBLE
        exerciseBinding?.tvTitle?.visibility = View.VISIBLE
        exerciseBinding?.tvExerciseName?.visibility = View.INVISIBLE
        exerciseBinding?.flExerciseView?.visibility = View.INVISIBLE
        exerciseBinding?.ivImage?.visibility = View.INVISIBLE
        exerciseBinding?.tvUpComingExerciseName?.visibility = View.VISIBLE
        exerciseBinding?.tvUpcomingLabel?.visibility = View.VISIBLE
        if(restTimer!=null){
            restTimer?.cancel()
            restProgress=0
        }
        exerciseBinding?.tvUpComingExerciseName?.text = exerciseList!![currentExercisePosition+1].getName()
        setRestProgressBar()
    }

    private fun setUpExerciseView(){

        exerciseBinding?.flRestView?.visibility = View.INVISIBLE
        exerciseBinding?.tvTitle?.visibility = View.INVISIBLE
        exerciseBinding?.tvExerciseName?.visibility = View.VISIBLE
        exerciseBinding?.flExerciseView?.visibility = View.VISIBLE
        exerciseBinding?.ivImage?.visibility = View.VISIBLE
        exerciseBinding?.tvUpComingExerciseName?.visibility = View.INVISIBLE
        exerciseBinding?.tvUpcomingLabel?.visibility = View.INVISIBLE

        if(restTimerExercise!=null){
            restTimerExercise?.cancel()
            restProgressExercise=0;
        }
        speakOut(exerciseList!![currentExercisePosition].getName())

        exerciseBinding?.ivImage?.setImageResource(exerciseList!![currentExercisePosition].getImage())
        exerciseBinding?.tvExerciseName?.text = exerciseList!![currentExercisePosition].getName()
        setExerciseProgressBar()
    }

    private fun setRestProgressBar(){
        exerciseBinding?.progressBar?.progress = restProgress

        restTimer = object : CountDownTimer(restTimerDuration*1000,1000){
            override fun onTick(millisUntilFinished: Long) {
                restProgress++
                exerciseBinding?.progressBar?.progress = 10-restProgress
                exerciseBinding?.tvTimer?.text = (10-restProgress).toString()
            }

            override fun onFinish() {
                currentExercisePosition++

                exerciseList!![currentExercisePosition].setIsSelected(true)
                exerciseAdapter!!.notifyDataSetChanged()
                setUpExerciseView()

            }

        }.start()
    }
    private fun setExerciseProgressBar(){
        exerciseBinding?.progressBarExercise?.progress = restProgressExercise
        restTimerExercise = object : CountDownTimer(exerciseTimerDuration*1000,1000){
            override fun onTick(millisUntilFinished: Long) {
                restProgressExercise++;
                exerciseBinding?.progressBarExercise?.progress = 30-restProgressExercise
                exerciseBinding?.tvTimerExercise?.text = (30-restProgressExercise).toString()
            }

            override fun onFinish() {



                if(currentExercisePosition < exerciseList?.size!! -1){
                    exerciseList!![currentExercisePosition].setIsSelected(false)
                    exerciseList!![currentExercisePosition].setIsCompleted(true)
                    exerciseAdapter!!.notifyDataSetChanged()
                    setUpRestView()
                }else{
                    finish()
                    val intent = Intent(this@ExerciseActivity,FinishActivity::class.java)
                    startActivity(intent)
                }
            }

        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        if(restTimer!=null){
            restTimer?.cancel()
            restProgress=0
        }

        if(restTimerExercise != null){
            restTimerExercise?.cancel()
            restProgressExercise = 0
        }

        if(tts!=null){
            tts!!.stop()
            tts!!.shutdown()
        }

        if(player != null){
            player!!.stop()
        }
        exerciseBinding=null
    }

    override fun onInit(status: Int) {
        if(status == TextToSpeech.SUCCESS){
            val result = tts?.setLanguage(Locale.ENGLISH)
            if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                Log.e("tts","LAng not supported")
            }
        }else{
            Log.e("tts","Initialization Failed")

        }

    }

    private fun speakOut(text : String){
        tts!!.speak(text,TextToSpeech.QUEUE_FLUSH,null,"")
    }
}
package com.plcoding.a7minworkoutapp

object Constants {
    fun defaultExerciseList():ArrayList<ExerciseModel>{
        val exersixeList = ArrayList<ExerciseModel>()

        val jumpingJack = ExerciseModel(
            1,"Jumping Jack",R.drawable.ic_jumping_jacks,false,false
        )
        exersixeList.add(jumpingJack)

        val wallSit = ExerciseModel(
            2,"Wall Sit",R.drawable.ic_wall_sit,false,false
        )
        exersixeList.add(wallSit)
        val pushUp = ExerciseModel(
            3,"Push Up",R.drawable.ic_push_up,false,false
        )
        exersixeList.add(pushUp)

        val abdominalCrunch = ExerciseModel(
            4,"Abdominal Crunch",R.drawable.ic_abdominal_crunch,false,false
        )
        exersixeList.add(abdominalCrunch)

        val lunge = ExerciseModel(
            5,"Lunges",R.drawable.ic_lunge,false,false
        )
        exersixeList.add(lunge)
        val plank = ExerciseModel(
            6,"Plank",R.drawable.ic_plank,false,false
        )
        exersixeList.add(plank)
        val pushUpAndRotation = ExerciseModel(
            7,"Push Up And Rotation",R.drawable.ic_push_up_and_rotation,false,false
        )
        exersixeList.add(pushUpAndRotation)
        val sidePlank= ExerciseModel(
            8,"Side Plank",R.drawable.ic_side_plank,false,false
        )
        exersixeList.add(sidePlank)
        val squat= ExerciseModel(
            9,"Squat",R.drawable.ic_squat,false,false
        )
        exersixeList.add(squat)
        val stepUpOntoChair= ExerciseModel(
            10,"Step Up Onto Chair",R.drawable.ic_step_up_onto_chair,false,false
        )
        exersixeList.add(stepUpOntoChair)
        val tricepDipsOntoChair= ExerciseModel(
            11,"Triceps Dips Onto Chair",R.drawable.ic_triceps_dip_on_chair,false,false
        )
        exersixeList.add(tricepDipsOntoChair)
        val highKneesRunningInPlace= ExerciseModel(
            12,"High Knee Running In Place",R.drawable.ic_high_knees_running_in_place,false,false
        )
        exersixeList.add(highKneesRunningInPlace)






        return exersixeList
    }
}
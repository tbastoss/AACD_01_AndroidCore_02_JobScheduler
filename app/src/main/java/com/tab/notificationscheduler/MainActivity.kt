package com.tab.notificationscheduler

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tab.notificationscheduler.databinding.ActivityMainBinding

const val JOB_ID: Int = 0

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    lateinit var btnSchedule: Button
    lateinit var btnCancelJob: Button
    lateinit var mDeviceIdleSwitch: Switch
    lateinit var mDeviceChargingSwitch: Switch
    lateinit var mSeekBar: SeekBar

    lateinit var mScheduler: JobScheduler


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        btnSchedule = binding.btnScheduleJob
        btnCancelJob = binding.btnCancelJob
        mDeviceIdleSwitch = binding.idleSwitch
        mDeviceChargingSwitch = binding.chargingSwitch
        mSeekBar = binding.seekBar
        val seekBarProgress = binding.seekBarProgress

        btnSchedule.setOnClickListener { v: View? -> scheduleJob() }
        btnCancelJob.setOnClickListener { v: View? -> cancelJobs() }

        mSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (progress > 0){
                    seekBarProgress.setText("$progress s");
                }else {
                    seekBarProgress.setText(R.string.not_set);
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    fun scheduleJob() {
        mScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val networkOptions = binding.networkOptions
        val selectedNetworkID = networkOptions.checkedRadioButtonId
        val selectedNetworkOption = when(selectedNetworkID) {
            binding.anyNetwork.id -> JobInfo.NETWORK_TYPE_ANY
            binding.wifiNetwork.id -> JobInfo.NETWORK_TYPE_UNMETERED
            else -> JobInfo.NETWORK_TYPE_NONE
        }
        val seekBarInteger = mSeekBar.progress
        val seekBarSet = seekBarInteger > 0

        val serviceName = ComponentName(packageName, NotificationJobService::class.java.name)
        val builder = JobInfo.Builder(JOB_ID, serviceName)
            .setRequiredNetworkType(selectedNetworkOption)
            .setRequiresDeviceIdle(mDeviceIdleSwitch.isChecked)
            .setRequiresCharging(mDeviceChargingSwitch.isChecked)

        if (seekBarSet) {
            builder.setOverrideDeadline((seekBarInteger * 1000).toLong());
        }

        val constrantSet = (selectedNetworkOption != JobInfo.NETWORK_TYPE_NONE) ||
                mDeviceIdleSwitch.isChecked || mDeviceChargingSwitch.isChecked || seekBarSet



        //Jobs can only be scheduled with at least one constraint
        if (constrantSet) {
            //Schedule job
            val jobInfo = builder.build()
            mScheduler.schedule(jobInfo)

            Toast.makeText(this, "Job Scheduled, job will run when " +
                    "the constraints are met.", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Please set at least one constraint",
                Toast.LENGTH_SHORT).show()
        }
    }

    fun cancelJobs() {
        mScheduler.cancelAll()
        Toast.makeText(this, "Jobs cancelled", Toast.LENGTH_SHORT).show()
    }
}

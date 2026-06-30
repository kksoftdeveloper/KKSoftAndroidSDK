package com.appmb.sdk.mbauth.worker

import android.content.Context
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.appmb.sdk.mbcore.MbSdk
import com.appmb.sdk.mbcore.data.datasource.MbCoreCommonDataSource
import org.koin.core.context.GlobalContext
import java.util.concurrent.TimeUnit


object WorkerManager {

    private val appContext: Context by lazy {
      GlobalContext.get().get()
    }
    private val mbCommonRepository = MbSdk.getKoin().get<MbCoreCommonDataSource>()
    private const val JOB_NAME = "SDK_TIMER"

  /**
   * Starts or resumes the guest login countdown timer.
   * Uses WorkManager which persists across app restarts.
   */
  suspend fun startCountDown() {
    Log.d("WorkerManager", "startCountDown() called")
    val currentTime = System.currentTimeMillis()
    val startTime = mbCommonRepository.getGuestLoginStartTime()
    val totalDuration = mbCommonRepository.getTimeRemaining()
    
    Log.d("WorkerManager", "currentTime=$currentTime, startTime=$startTime, totalDuration=$totalDuration")
    
    if (totalDuration <= 0) {
      Log.d("WorkerManager", "Timer not started: timeRemaining is $totalDuration seconds")
      return
    }
    
    val remainingSeconds = if (startTime == 0L) {
      // First time - save start time
      mbCommonRepository.saveGuestLoginStartTime(currentTime)
      Log.d("WorkerManager", "Starting countdown timer with $totalDuration seconds")
      totalDuration
    } else {
      // Resume - calculate remaining time
      val elapsedSeconds = (currentTime - startTime) / 1000
      val remaining = (totalDuration - elapsedSeconds).coerceAtLeast(0)
      Log.d("WorkerManager", "Resuming countdown: elapsed=$elapsedSeconds, remaining=$remaining seconds")
      remaining
    }
    
    if (remainingSeconds <= 0) {
      Log.d("WorkerManager", "Timer already expired, showing dialog immediately")
      // Timer already expired, trigger immediately
      val work = OneTimeWorkRequestBuilder<TimerWorker>()
        .setInitialDelay(0, TimeUnit.SECONDS)
        .addTag(JOB_NAME)
        .build()
      WorkManager.getInstance(appContext).enqueueUniqueWork(JOB_NAME, ExistingWorkPolicy.REPLACE, work)
    } else {
      Log.d("WorkerManager", "Scheduling TimerWorker to trigger in $remainingSeconds seconds")
      val work = OneTimeWorkRequestBuilder<TimerWorker>()
        .setInitialDelay(remainingSeconds, TimeUnit.SECONDS)
        .addTag(JOB_NAME)
        .build()
      WorkManager.getInstance(appContext).enqueueUniqueWork(JOB_NAME, ExistingWorkPolicy.KEEP, work)
    }
  }

  /**
   * Cancels the countdown timer and clears the start time.
   */
  suspend fun cancelTimer() {
    Log.d("WorkerManager", "Cancelling countdown timer")
    mbCommonRepository.saveGuestLoginStartTime(0)
    WorkManager.getInstance(appContext).cancelUniqueWork(JOB_NAME)
  }
}
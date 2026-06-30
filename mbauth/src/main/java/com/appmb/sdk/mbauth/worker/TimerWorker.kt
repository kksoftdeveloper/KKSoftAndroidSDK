package com.appmb.sdk.mbauth.worker

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class TimerWorker(
  appContext: Context,
  params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
  override suspend fun doWork(): Result {
    LocalBroadcastManager.getInstance(applicationContext)
      .sendBroadcast(Intent(ACTION_TIMER_DONE))
    return Result.success()
  }

  companion object {
    const val ACTION_TIMER_DONE = "com.sdk.ACTION_LINK_ACCOUNT"
  }
}
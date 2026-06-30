package com.appmb.sdk.mbtracking.di

import com.appmb.sdk.mbcore.MbSdk
import com.appmb.sdk.mbcore.config.TrackingConfig

object TrackingLoader {
  @Volatile private var loaded = false

  fun loadOnce() {
    val config = MbSdk.getConfig().getTrackingConfig()
    if (config != null) {
      loadOnce(config)
    }
  }

  fun loadOnce(config: TrackingConfig) {
    if (loaded) return
    synchronized(this) {
      if (loaded) return
      val module = trackingModule(
        context = MbSdk.getContext(),
        config = config
      )
      MbSdk.loadModule(module)
      loaded = true
    }
  }
}



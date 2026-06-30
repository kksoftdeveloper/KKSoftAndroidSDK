package com.appmb.sdk.mbtracking.model

enum class OnlineTime(val minutes: Int) {
  OL5minutes(5),
  OL10minutes(10),
  OL30minutes(30),
  OL60minutes(60);
  
  override fun toString(): String = "online_${minutes}min"
}


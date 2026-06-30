package com.appmb.sdk.mbtracking.model

enum class Level(val value: Int) {
  Level10(10),
  Level20(20),
  Level30(30),
  Level40(40),
  Level50(50),
  Level60(60),
  Level70(70),
  Level80(80),
  Level90(90),
  Level100(100);
  
  override fun toString(): String = "level_$value"
}


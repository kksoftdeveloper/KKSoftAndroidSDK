package com.appmb.build_logic.convention

fun Pair<String, String>.name(): String = first
fun Pair<String, String>.ext(): String = second

const val emptyString = ""
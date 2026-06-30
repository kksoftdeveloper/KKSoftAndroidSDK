package com.appmb.sdk.mbcore.di

import org.koin.core.context.GlobalContext
import org.koin.dsl.koinApplication

object IsolatedKoinContext {
  val koin = GlobalContext.get()
  val k = koinApplication { }
}
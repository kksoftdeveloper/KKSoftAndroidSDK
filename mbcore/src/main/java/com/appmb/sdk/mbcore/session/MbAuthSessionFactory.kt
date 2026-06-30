package com.appmb.sdk.mbcore.session

object MbAuthSessionFactory {

  var mbAuthSession: MbAuthSession? = null

  fun getSession(): MbAuthSession {
    if (mbAuthSession != null) return mbAuthSession!!
    mbAuthSession = MbAuthSessionDataStore.default()
    return mbAuthSession!!
  }
}
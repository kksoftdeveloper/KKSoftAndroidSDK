package com.appmb.sdk.mbpayment.testutils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * JUnit rule that sets Dispatchers.Main to a [StandardTestDispatcher] backed by a [TestCoroutineScheduler].
 * Provides access to the testScheduler so tests can advance time and run pending tasks.
 */
class MainDispatcherRule : TestWatcher() {
    val testScheduler = TestCoroutineScheduler()
    val dispatcher = StandardTestDispatcher(testScheduler)

    override fun starting(description: Description) {
        super.starting(description)
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        super.finished(description)
        Dispatchers.resetMain()
    }
}


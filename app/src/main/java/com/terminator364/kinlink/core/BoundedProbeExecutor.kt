package com.terminator364.kinlink.core

import java.util.concurrent.Callable
import java.util.concurrent.Future
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

object BoundedProbeExecutorPolicy {
    const val MAX_CONCURRENT_ATTEMPTS = 2
    const val KEEP_ALIVE_SECONDS = 30L
}

/**
 * Shared fail-open executor for bounded Wi-Fi diagnostics.
 *
 * A DNS/socket operation may ignore interruption on some platform/network stacks.
 * KINLINK therefore never creates an unbounded number of helper threads. At most
 * two probe workers may exist concurrently. If both are occupied, a new probe
 * fails closed-to-action immediately instead of allocating another thread.
 */
object BoundedProbeExecutor {
    private val executor = ThreadPoolExecutor(
        0,
        BoundedProbeExecutorPolicy.MAX_CONCURRENT_ATTEMPTS,
        BoundedProbeExecutorPolicy.KEEP_ALIVE_SECONDS,
        TimeUnit.SECONDS,
        SynchronousQueue(),
        ThreadFactory { task ->
            Thread(task, "kinlink-probe").apply {
                isDaemon = true
                priority = Thread.NORM_PRIORITY
            }
        },
        ThreadPoolExecutor.AbortPolicy()
    )

    fun <T> submit(task: Callable<T>): Future<T>? =
        runCatching { executor.submit(task) }.getOrNull()
}

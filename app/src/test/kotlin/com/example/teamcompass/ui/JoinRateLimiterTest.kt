package com.example.teamcompass.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class JoinRateLimiterTest {

    @Test
    fun allows_up_to_limit_per_code_and_blocks_next_attempt() {
        val clock = MutableClock(nowMs = 1_000L)
        val limiter = JoinRateLimiter(
            nowMs = { clock.nowMs },
            windowMs = 60_000L,
            maxAttemptsPerCode = 5,
            maxAttemptsGlobal = 20,
        )

        repeat(5) {
            assertTrue(limiter.canAttempt("123456"))
        }
        assertFalse(limiter.canAttempt("123456"))
    }

    @Test
    fun unblocks_after_time_window_passes() {
        val clock = MutableClock(nowMs = 1_000L)
        val limiter = JoinRateLimiter(
            nowMs = { clock.nowMs },
            windowMs = 60_000L,
            maxAttemptsPerCode = 2,
            maxAttemptsGlobal = 20,
        )

        assertTrue(limiter.canAttempt("222222"))
        assertTrue(limiter.canAttempt("222222"))
        assertFalse(limiter.canAttempt("222222"))

        clock.nowMs += 61_000L
        assertTrue(limiter.canAttempt("222222"))
    }

    @Test
    fun global_limit_applies_across_codes() {
        val clock = MutableClock(nowMs = 1_000L)
        val limiter = JoinRateLimiter(
            nowMs = { clock.nowMs },
            windowMs = 60_000L,
            maxAttemptsPerCode = 10,
            maxAttemptsGlobal = 3,
        )

        assertTrue(limiter.canAttempt("100001"))
        assertTrue(limiter.canAttempt("100002"))
        assertTrue(limiter.canAttempt("100003"))
        assertFalse(limiter.canAttempt("100004"))
    }

    @Test
    fun concurrent_attempts_doNotExceedPerCodeLimit() {
        val limiter = JoinRateLimiter(
            nowMs = { 1_000L },
            windowMs = 60_000L,
            maxAttemptsPerCode = 5,
            maxAttemptsGlobal = 200,
        )
        val attempts = 64
        val pool = Executors.newFixedThreadPool(8)
        val startLatch = CountDownLatch(1)
        val doneLatch = CountDownLatch(attempts)
        val accepted = AtomicInteger(0)

        repeat(attempts) {
            pool.execute {
                try {
                    startLatch.await(3, TimeUnit.SECONDS)
                    if (limiter.canAttempt("777777")) {
                        accepted.incrementAndGet()
                    }
                } finally {
                    doneLatch.countDown()
                }
            }
        }

        startLatch.countDown()
        assertTrue(doneLatch.await(5, TimeUnit.SECONDS))
        pool.shutdownNow()

        assertEquals(5, accepted.get())
    }

    private data class MutableClock(
        var nowMs: Long,
    )
}

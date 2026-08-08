package com.litechat.android.util

import com.litechat.android.util.DeviceCompat.Band
import org.junit.Assert.assertEquals
import org.junit.Test

class DeviceCompatBandTest {

    @Test
    fun `band boundaries from free RAM`() {
        assertEquals(Band.TIGHT, DeviceCompat.bandFor(availMb = 800))
        assertEquals(Band.TIGHT, DeviceCompat.bandFor(availMb = 1023))
        assertEquals(Band.COMFORTABLE, DeviceCompat.bandFor(availMb = 1024))
        assertEquals(Band.COMFORTABLE, DeviceCompat.bandFor(availMb = 2047))
        assertEquals(Band.ROOMY, DeviceCompat.bandFor(availMb = 2048))
        assertEquals(Band.ROOMY, DeviceCompat.bandFor(availMb = 3583))
        assertEquals(Band.GENEROUS, DeviceCompat.bandFor(availMb = 3584))
        assertEquals(Band.GENEROUS, DeviceCompat.bandFor(availMb = 8192))
    }

    @Test
    fun `falls back to total half when avail is unusable`() {
        // avail <= 64 MB → treat as boot-time zero and use total/2.
        assertEquals(Band.TIGHT, DeviceCompat.bandFor(availMb = 0, totalMb = 1024))
        // 4096/2 = 2048 → boundary is basis < 2048 (COMFORTABLE), so 2048 = ROOMY.
        assertEquals(Band.ROOMY, DeviceCompat.bandFor(availMb = 64, totalMb = 4096))
        assertEquals(Band.GENEROUS, DeviceCompat.bandFor(availMb = 32, totalMb = 8192))
    }

    @Test
    fun `avail wins over total when both present`() {
        // 8 GB total but only 1.5 GB free → COMFORTABLE (honest free-RAM first).
        assertEquals(Band.COMFORTABLE, DeviceCompat.bandFor(availMb = 1500, totalMb = 8192))
        assertEquals(Band.TIGHT, DeviceCompat.bandFor(availMb = 900, totalMb = 4096))
    }
}

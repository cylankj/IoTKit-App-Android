package com.cylan.jiafeigou.n.view.cam

import org.junit.Test

/**
 * Created by hds on 17-8-11.
 */
class WeekFragmentTest {

    @Test
    fun test() {
        println("good" + (1 shl 1))
        var selected: Int = 99
        println(Integer.toBinaryString(selected))
        for (i in 0..8) {
            println((selected shr (7 - i) and 1) == 1)
        }
    }
}
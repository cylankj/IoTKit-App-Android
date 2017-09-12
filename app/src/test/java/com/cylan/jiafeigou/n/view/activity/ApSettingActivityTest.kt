package com.cylan.jiafeigou.n.view.activity

/**
 * Created by hds on 17-9-8.
 */
class ApSettingActivityTest {

    private val test: Test? = null

    private inner class Test {
        fun action() {}
    }

    private inner class Good {

        private fun test() {
            test!!.action()
        }
    }

}
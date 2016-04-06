package com.cylan.jiafeigou.activity.efamily.main;

import com.cylan.jiafeigou.entity.WordsBean;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2015-12-09
 * Time: 16:53
 */

public interface PlayOrStopAudioLIstener {
    void play(WordsBean bean);

    void stop(WordsBean bean);
}

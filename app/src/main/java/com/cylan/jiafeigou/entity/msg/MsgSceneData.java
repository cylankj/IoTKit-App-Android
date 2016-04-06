package com.cylan.jiafeigou.entity.msg;

import com.cylan.jiafeigou.base.MyApp;
import com.cylan.jiafeigou.utils.PathGetter;
import com.cylan.jiafeigou.utils.PreferenceUtil;

import org.msgpack.annotation.Ignore;
import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

import java.io.Serializable;
import java.util.List;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 09:37
 */
@Message
public class MsgSceneData implements Serializable {
    @Ignore
    public final static int MODE_STANDARD = 0;
    @Ignore
    public final static int MODE_HOME_IN = 1;
    @Ignore
    public final static int MODE_HOME_OUT = 2;


    @Index(0)
    public int vid;
    @Index(1)
    public int scene_id;
    @Index(2)
    public int enable;
    @Index(3)
    public int image_id;
    @Index(4)
    public String scene_name;
    @Index(5)
    public int mode;
    @Index(6)
    public List<MsgCidData> data;

    @Ignore
    public String picPath() {
        return PathGetter.getScreenShotPath() + PreferenceUtil.getBindingPhone(MyApp.getContext()) + "-"
                + scene_id + ".png";
    }

}


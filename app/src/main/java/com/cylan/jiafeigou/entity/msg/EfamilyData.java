package com.cylan.jiafeigou.entity.msg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

import java.io.Serializable;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 10:20
 */
@Message
public class EfamilyData implements Serializable {
    @Index(0)
    public String cid;
    @Index(1)
    public String alias;
    @Index(2)
    public int os;
    @Index(3)
    public int temp;
    @Index(4)
    public float humi;
    @Index(5)
    public float methanal; //甲醛
    @Index(6)
    public float pm10; //pm10
    @Index(7)
    public float pm25; //pm25
    @Index(8)
    public int level; // 空气质量 0 优 1 良 2 中 3 差
    @Index(9)
    public int mag_ir;
}

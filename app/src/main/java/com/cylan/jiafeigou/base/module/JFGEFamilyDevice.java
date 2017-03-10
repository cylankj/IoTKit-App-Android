// APT自动生成的文件,请勿修改!!!!
package com.cylan.jiafeigou.base.module;

import com.cylan.ext.annotations.DPProperty;
import com.cylan.jiafeigou.cache.db.module.Device;

import static com.cylan.jiafeigou.dp.DpMsgDefine.DPNet;
import static com.cylan.jiafeigou.dp.DpMsgDefine.DPPrimary;
import static com.cylan.jiafeigou.dp.DpMsgDefine.DPTimeZone;

public class JFGEFamilyDevice extends Device {
  @DPProperty(
      msgId = 201
  )
  public DPNet net;

  @DPProperty(
      msgId = 206
  )
  public DPPrimary<Integer> battery;

  @DPProperty(
      msgId = 210
  )
  public DPPrimary<Integer> up_time;

  @DPProperty(
      msgId = 214
  )
  public DPTimeZone device_time_zone;

  @Override
  public JFGEFamilyDevice $() {
    return (JFGEFamilyDevice)super.$();
  }
}

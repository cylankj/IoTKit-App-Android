package com.cylan.jiafeigou.n.mvp.contract.cam

import com.cylan.jiafeigou.base.view.JFGPresenter
import com.cylan.jiafeigou.base.view.JFGView
import com.cylan.jiafeigou.dp.DpMsgDefine

/**
 * Created by yanzhendong on 2017/11/21.
 */
interface MonitorAreaSettingContact {

    interface View : JFGView {

        fun onGetMonitorPictureSuccess(url: String)

        fun onGetMonitorPictureError()

        fun showLoadingBar()

        fun hideLoadingBar()

        fun onSetMonitorAreaSuccess()

        fun onSetMonitorAreaError()

        fun onRestoreMonitorAreaSetting(rects: List<DpMsgDefine.Rect4F>)

        fun onRestoreDefaultMonitorAreaSetting()
        fun tryGetLocalMonitorPicture()
        fun onLoadMotionAreaSettingFinished(remoteURL: String?, motionAreaSetting: List<DpMsgDefine.Rect4F>?)
    }

    interface Presenter : JFGPresenter {

        fun setMonitorArea(uuid: String, enable: Boolean, rects: MutableList<DpMsgDefine.Rect4F>)

        fun loadMonitorAreaSetting()
    }
}
/**
 *
public List<float[]> getMotionArea() {
List<float[]> result = new ArrayList<>();
for (int i = 0; i < getChildCount(); i++) {
View view = getChildAt(i);
if (view instanceof Shaper) {
float[] floats = new float[4];
if (view.getVisibility() == VISIBLE) {
float width = getMeasuredWidth();
float height = getMeasuredHeight();
floats[0] = Math.min(view.getLeft() / width, 1.0f);//最大1.0f
floats[1] = Math.min(view.getTop() / width, 1.0f);
floats[2] = Math.min(view.getRight() / height, 1.0f);
floats[3] = Math.min(view.getBottom() / height, 1.0f);
}
result.add(floats);
}
}
if (result.size() == 0) {
result.add(new float[]{1.0f, 1.0f, 1.0f, 1.0f});
}
return result;
}

public void getMotionArea(float[] result) {
if (result == null || result.length < 4) {
AppLogger.w("错误的参数");
return;
}
View childView = getChildView();
if (childView != null && childView.getVisibility() == VISIBLE) {
float width = getMeasuredWidth();
float height = getMeasuredHeight();
result[0] = Math.min(childView.getLeft() / width, 1.0f);//最大1.0f
result[1] = Math.min(childView.getTop() / width, 1.0f);
result[2] = Math.min(childView.getRight() / height, 1.0f);
result[3] = Math.min(childView.getBottom() / height, 1.0f);
} else {
result[0] = 1.0f;
result[1] = 1.0f;
result[2] = 1.0f;
result[3] = 1.0f;
}
}
 * */
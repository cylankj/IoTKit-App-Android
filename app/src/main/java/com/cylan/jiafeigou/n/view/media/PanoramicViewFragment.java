package com.cylan.jiafeigou.n.view.media;


import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.module.GlideApp;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.model.CamMessageBean;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.DensityUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.widget.video.PanoramicView360_Ext;
import com.cylan.jiafeigou.widget.video.VideoViewFactory;
import com.cylan.panorama.CameraParam;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.misc.JConstant.KEY_SHARED_ELEMENT_LIST;
import static com.cylan.jiafeigou.n.view.media.CamMediaActivity.KEY_INDEX;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PanoramicViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PanoramicViewFragment extends IBaseFragment {


    @BindView(R.id.fLayout_panoramic_container)
    FrameLayout mPanoramicContainer;
    private PanoramicView360_Ext panoramicView;
    private CamMessageBean camMessageBean;
    private Subscription subscription;

    public PanoramicViewFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create activity_cloud_live_mesg_call_out_item new instance of
     * this fragment using the provided parameters.
     *
     * @param bundle Parameter 1.
     * @return A new instance of fragment NormalMediaFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PanoramicViewFragment newInstance(Bundle bundle) {
        PanoramicViewFragment fragment = new PanoramicViewFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup lLayoutPreview,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_panoramic_view, lLayoutPreview, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final int screenWidth = DensityUtils.getScreenWidth();
        ViewGroup.LayoutParams lp = mPanoramicContainer.getLayoutParams();
        lp.height = screenWidth;
        mPanoramicContainer.setLayoutParams(lp);
        this.uuid = getArguments().getString(JConstant.KEY_DEVICE_ITEM_UUID);
        camMessageBean = getArguments().getParcelable(KEY_SHARED_ELEMENT_LIST);
        if (getUserVisibleHint()) {//当前页面才显示
            loadBitmap(getArguments().getInt(KEY_INDEX, 0));
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (subscription != null) {
            subscription.unsubscribe();
        }
        try {
            if (panoramicView != null) {
                panoramicView.onDestroy();
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        showLoading(false);
    }

    private void showLoading(boolean show) {
//        if (getView() != null) {
//            getView().post(() -> {
//                if (show)
//                    LoadingDialog.changeToLoading(getFragmentManager(), "", true);
//                else LoadingDialog.dismissLoading(getFragmentManager());
//            });
//        }
    }

    private Target target;
    private int lastLoadIndex;

    public void loadBitmap(int index, String mode) {
        lastLoadIndex = index;
        Log.d("panoramicView", "null? " + (panoramicView == null) + " " + (getContext() == null));
        if (panoramicView == null) {
            panoramicView = new PanoramicView360_Ext(getContext());
            panoramicView.setInterActListener(new VideoViewFactory.InterActListener() {
                @Override
                public boolean onSingleTap(float x, float y) {
                    if (callBack != null) {
                        callBack.callBack(null);
//                        callBack = null;
                    }
                    return true;
                }

                @Override
                public void onSnapshot(Bitmap bitmap, boolean tag) {

                }
            });
            panoramicView.setMode(TextUtils.equals(mode, "0") ? 0 : 1);
            panoramicView.config360(TextUtils.equals(mode, "0") ? CameraParam.getTopPreset() : CameraParam.getWallPreset());
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mPanoramicContainer.addView(panoramicView, layoutParams);
        }
        if (target != null) {
            GlideApp.with(this).clear(target);
        }

        //填满
        GlideApp.with(this)
                .asBitmap()
                .load(MiscUtils.getCamWarnUrl(uuid, camMessageBean, index + 1))
                //解决黑屏问题
                .signature(new ObjectKey(System.currentTimeMillis() + ""))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(true)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onLoadStarted(@Nullable Drawable placeholder) {
                        super.onLoadStarted(placeholder);
                        showLoading(true);
                    }

                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        try {
                            //View 会自己回收 bitmap 导致 Glide 出错
                            Bitmap bitmap = resource.copy(resource.getConfig(), true);

                            if (bitmap != null && !bitmap.isRecycled()) {
                                panoramicView.loadImage(bitmap);
                            } else {
                                AppLogger.e("bitmap is recycled");
                            }
                        } catch (Exception e) {
                            AppLogger.e("pan view is out date,");
                        }
                        showLoading(false);
                    }
                });
    }

    public void loadBitmap(int index) {

        Device device = DataSourceManager.getInstance().getDevice(uuid);

        // TODO: 2017/9/1 哪些设备需要平视,哪些设备需要俯视

        String mode;
        switch ((int) camMessageBean.message.getMsgId()) {
            case DpMsgMap.ID_505_CAMERA_ALARM_MSG: {
                DpMsgDefine.DPAlarm dpAlarm = (DpMsgDefine.DPAlarm) camMessageBean.message;
                mode = dpAlarm.tly;
            }
            break;
            default: {
                mode = JFGRules.hasViewAngle(device.pid) ? "0" : "1";
            }
        }
        Log.d("loadBitmap", "loadBitmap: " + mode);
        if (subscription != null) {
            subscription.unsubscribe();
        }
        subscription = Observable.just(mode)
                .subscribeOn(Schedulers.io())
                .delay(200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> {
                    loadBitmap(index, mode);
                }, AppLogger::e);
    }
}

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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.CamWarnGlideURL;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.DensityUtils;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.cylan.jiafeigou.widget.video.PanoramicView360_Ext;
import com.cylan.jiafeigou.widget.video.VideoViewFactory;
import com.cylan.panorama.CameraParam;
import com.cylan.panorama.Panoramic360View;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.cylan.jiafeigou.misc.JConstant.KEY_SHARED_ELEMENT_LIST;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PanoramicViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PanoramicViewFragment extends IBaseFragment {


    @BindView(R.id.fLayout_panoramic_container)
    FrameLayout mPanoramicContainer;
    private String uuid;
    private PanoramicView360_Ext panoramicView;
    private DpMsgDefine.DPAlarm dpAlarm;
    private Panoramic360View.MountMode mountMode;
    private Device device;

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
        final int screenWidth = DensityUtils.getScreenWidth();
        ViewGroup.LayoutParams lp = mPanoramicContainer.getLayoutParams();
        lp.height = screenWidth;
        mPanoramicContainer.setLayoutParams(lp);
        this.uuid = getArguments().getString(JConstant.KEY_DEVICE_ITEM_UUID);
        this.device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        dpAlarm = getArguments().getParcelable(KEY_SHARED_ELEMENT_LIST);
        if (getUserVisibleHint()) {//当前页面才显示
            loadBitmap(getArguments().getInt("key_index", 0));
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
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
//                    LoadingDialog.showLoading(getFragmentManager(), "", true);
//                else LoadingDialog.dismissLoading(getFragmentManager());
//            });
//        }
    }

    public void loadBitmap(int index, String mode) {
        Log.d("panoramicView", "null? " + (panoramicView == null) + " " + (getContext() == null));
        if (panoramicView == null) {
            panoramicView = new PanoramicView360_Ext(getContext());
            panoramicView.setInterActListener(new VideoViewFactory.InterActListener() {
                @Override
                public boolean onSingleTap(float x, float y) {
                    if (callBack != null) callBack.callBack(null);
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
        //填满
        Glide.with(ContextUtils.getContext())
                .load(new CamWarnGlideURL(uuid, dpAlarm.time + "_" + (index + 1) + ".jpg"))
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onLoadStarted(Drawable placeholder) {
                        showLoading(true);
                    }

                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                        try {
                            if (resource != null && !resource.isRecycled())
                                panoramicView.loadImage(resource);
                            else {
                                AppLogger.e("bitmap is recycled");
                            }
                        } catch (Exception e) {
                            AppLogger.e("pan view is out date,");
                        }
                        showLoading(false);
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        AppLogger.e("load failed: " + e.getLocalizedMessage());
                        showLoading(false);
                    }
                });
    }

    public void loadBitmap(int index) {
        String mode = dpAlarm == null ? "0" : dpAlarm.tly;
        loadBitmap(index, mode);
    }
}

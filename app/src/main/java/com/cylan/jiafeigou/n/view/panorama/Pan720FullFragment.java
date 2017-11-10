package com.cylan.jiafeigou.n.view.panorama;


import android.app.Application;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.view.JFGSourceManager;
import com.cylan.jiafeigou.base.wrapper.BaseFragment;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.module.GlideApp;
import com.cylan.jiafeigou.n.mvp.model.PAlbumBean;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.cylan.panorama.CommonPanoramicView;
import com.cylan.panorama.Panoramic720View;

import java.io.File;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;

import static com.cylan.panorama.Panoramic720View.DM_Equirectangular;
import static com.cylan.panorama.Panoramic720View.DM_Fisheye;
import static com.cylan.panorama.Panoramic720View.DM_LittlePlanet;


/**
 * Use the {@link Pan720FullFragment#newInstance} factory method to
 * fetch an instance of this fragment.
 */
public class Pan720FullFragment extends BaseFragment<Pan720FullContract.Presenter> implements Pan720FullContract.View {

    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;
    Panoramic720View panoramic720View;
    @Inject
    JFGSourceManager sourceManager;

    public Pan720FullFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param bundle Parameter 1.
     * @return A new instance of fragment Pan720FullFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Pan720FullFragment newInstance(Bundle bundle) {
        Pan720FullFragment fragment = new Pan720FullFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        panoramic720View = new Panoramic720View(getContext());
        panoramic720View.setEventListener(new CommonPanoramicView.PanoramaEventListener() {
            @Override
            public void onSingleTap(float v, float v1) {

            }

            @Override
            public void onSnapshot(Bitmap bitmap, boolean b) {
                Log.d("tag", "onSnapshot:" + (bitmap == null));
            }
        });
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ((ViewGroup) view).addView(panoramic720View, 0, lp);
        PAlbumBean bean = getArguments().getParcelable("item_url");
        customToolbar.setToolbarLeftTitle(TimeUtils.getTimeSpecial(bean.getDownloadFile().getTime() * 1000L));
        GlideApp.with(this)
                .asBitmap()
                .load(Uri.fromFile(new File(JConstant.PAN_PATH + File.separator + uuid + File.separator + bean.getDownloadFile().fileName)))
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        panoramic720View.configV720();
                        panoramic720View.loadImage(resource);
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pan720_full, container, false);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (panoramic720View != null) {
            panoramic720View.onDestroy();
        }
    }

    @OnClick({R.id.img_snap_shot, R.id.img_vr, R.id.img_planet, R.id.img_sensor})
    public void onClick(View view) {
        int net = NetUtils.getJfgNetType(ContextUtils.getContext());
        if (net == 0) {
            ToastUtil.showNegativeToast(getString(R.string.OFFLINE_ERR_1));
            return;
        }
        Device device = sourceManager.getDevice(uuid);
        String mac = device.$(DpMsgMap.ID_202_MAC, "");
        if (!TextUtils.equals(mac, NetUtils.getRouterMacAddress((Application) ContextUtils.getContext()))) {
            ToastUtil.showNegativeToast(getString(R.string.OFFLINE_ERR_1));
            return;
        }
        switch (view.getId()) {
            case R.id.img_snap_shot:
                panoramic720View.takeSnapshot(false);
                break;
            case R.id.img_vr:
                panoramic720View.setDisplayMode(DM_Equirectangular);
                break;
            case R.id.img_planet:
                panoramic720View.setDisplayMode(DM_Fisheye);
                break;
            case R.id.img_sensor:
                panoramic720View.setDisplayMode(DM_LittlePlanet);
                break;
        }
    }

}

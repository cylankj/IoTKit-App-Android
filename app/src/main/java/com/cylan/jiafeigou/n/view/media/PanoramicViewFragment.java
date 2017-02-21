package com.cylan.jiafeigou.n.view.media;


import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.CamWarnGlideURL;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.DensityUtils;
import com.cylan.panorama.CameraParam;
import com.cylan.panorama.PanoramicView;

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
    FrameLayout fLayoutPanoramicContainer;
    private PanoramicView.MountMode mountMode;
    private int preIndex;
    private DpMsgDefine.DPAlarm dpAlarm;
    private String uuid;
    private PanoramicView panoramicView;

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
        ViewGroup.LayoutParams lp = fLayoutPanoramicContainer.getLayoutParams();
        lp.height = screenWidth;
        fLayoutPanoramicContainer.setLayoutParams(lp);
        preIndex = getArguments().getInt("key_index");
        this.uuid = getArguments().getString("key_uuid");
        dpAlarm = getArguments().getParcelable(KEY_SHARED_ELEMENT_LIST);
        loadBitmap();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        super.onDestroy();
        try {
            if (panoramicView != null) {
                panoramicView.onDestroy();
            }
        } catch (Exception e) {

        }
    }

    private void loadBitmap() {
        panoramicView = new PanoramicView(getContext());
        panoramicView.configV360(CameraParam.getTopPreset());
        //填满
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        fLayoutPanoramicContainer.addView(panoramicView, layoutParams);
        GlideUrl url = new CamWarnGlideURL(dpAlarm, preIndex, uuid);
        Glide.with(ContextUtils.getContext())
                .load(url)
                .asBitmap()
                .format(DecodeFormat.DEFAULT)
                .into(new SimpleTarget<Bitmap>(150, 150) {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                        panoramicView.loadImage(resource);
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        AppLogger.e("load failed: " + e.getLocalizedMessage());
                    }
                });
    }


    @Override
    public void onDestroyView() {
//        super.onDestroyView();
    }
}

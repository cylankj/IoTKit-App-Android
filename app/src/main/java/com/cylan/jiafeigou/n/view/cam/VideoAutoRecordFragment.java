package com.cylan.jiafeigou.n.view.cam;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.setting.VideoAutoRecordContract;
import com.cylan.jiafeigou.n.mvp.impl.setting.VideoAutoRecordPresenterImpl;
import com.cylan.jiafeigou.n.mvp.model.BeanCamInfo;
import com.cylan.jiafeigou.utils.ViewUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VideoAutoRecordFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VideoAutoRecordFragment extends IBaseFragment<VideoAutoRecordContract.Presenter>
        implements VideoAutoRecordContract.View,
        CompoundButton.OnCheckedChangeListener {

    @BindView(R.id.imgV_top_bar_center)
    TextView imgVTopBarCenter;
    @BindView(R.id.fLayout_top_bar_container)
    FrameLayout fLayoutTopBarContainer;
    @BindView(R.id.rg_auto_record_mode)
    RadioGroup rgAutoRecordMode;
    @BindView(R.id.rb_motion)
    RadioButton rbMotion;
    @BindView(R.id.rb_24_hours)
    RadioButton rb24Hours;
    @BindView(R.id.rb_never)
    RadioButton rbNever;

    public void onAttach(Context context) {
        super.onAttach(context);
        basePresenter = new VideoAutoRecordPresenterImpl(this,
                (BeanCamInfo) getArguments().getParcelable(JConstant.KEY_DEVICE_ITEM_BUNDLE));
    }

    /**
     * Use this factory method to create activity_cloud_live_mesg_video_talk_item new instance of
     * this fragment using the provided parameters.
     *
     * @param args Parameter 2.
     * @return A new instance of fragment SafeProtectionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static VideoAutoRecordFragment newInstance(Bundle args) {
        VideoAutoRecordFragment fragment = new VideoAutoRecordFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_video_auto_record, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        imgVTopBarCenter.setText(R.string.SETTING_RECORD);
        int focus = basePresenter.getBeanCamInfo().deviceAutoVideoRecord;
        rbMotion.setChecked(focus == 0);
        rbMotion.setOnCheckedChangeListener(this);
        rb24Hours.setOnCheckedChangeListener(this);
        rb24Hours.setChecked(focus == 1);
        rbNever.setOnCheckedChangeListener(this);
        rbNever.setChecked(focus == 2);
        ViewUtils.setViewPaddingStatusBar(fLayoutTopBarContainer);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (callBack != null)
            callBack.callBack(null);
    }

    @Override
    public void beanUpdate(BeanCamInfo info) {

    }

    @Override
    public void setPresenter(VideoAutoRecordContract.Presenter presenter) {
        basePresenter = presenter;
    }

    @OnClick({R.id.lLayout_mode_motion, R.id.lLayout_mode_24_hours, R.id.lLayout_mode_never})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.lLayout_mode_motion:
                rbMotion.performClick();
                break;
            case R.id.lLayout_mode_24_hours:
                rb24Hours.performClick();
                break;
            case R.id.lLayout_mode_never:
                rbNever.performClick();
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (!isChecked)
            return;
        BeanCamInfo info = basePresenter.getBeanCamInfo();
        switch (buttonView.getId()) {
            case R.id.rb_motion:
                if (info.deviceAutoVideoRecord == 0)
                    return;
                info.deviceAutoVideoRecord = 0;
                break;
            case R.id.rb_24_hours:
                if (info.deviceAutoVideoRecord == 1)
                    return;
                info.deviceAutoVideoRecord = 1;
                break;
            case R.id.rb_never:
                if (info.deviceAutoVideoRecord == 2)
                    return;
                info.deviceAutoVideoRecord = 2;
                break;
        }
        basePresenter.saveCamInfoBean(info, DpMsgMap.ID_303_DEVICE_AUTO_VIDEO_RECORD);

    }
}

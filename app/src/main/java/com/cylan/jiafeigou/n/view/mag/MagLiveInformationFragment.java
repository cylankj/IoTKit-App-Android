package com.cylan.jiafeigou.n.view.mag;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.mag.MagLiveInformationContract;
import com.cylan.jiafeigou.n.mvp.impl.mag.MagLiveInformationPresenterImp;
import com.cylan.jiafeigou.n.mvp.model.BeanMagInfo;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.dialog.EditFragmentDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.cylan.jiafeigou.misc.JConstant.KEY_DEVICE_ITEM_BUNDLE;
import static com.cylan.jiafeigou.widget.dialog.EditFragmentDialog.KEY_LEFT_CONTENT;
import static com.cylan.jiafeigou.widget.dialog.EditFragmentDialog.KEY_RIGHT_CONTENT;
import static com.cylan.jiafeigou.widget.dialog.EditFragmentDialog.KEY_TITLE;
import static com.cylan.jiafeigou.widget.dialog.EditFragmentDialog.KEY_TOUCH_OUT_SIDE_DISMISS;

/**
 * 创建者     谢坤
 * 创建时间   2016/7/12 17:53
 * 用来控制摄像头模块下的设备信息，点击设备名称和设备时区时进行切换
 */
public class MagLiveInformationFragment extends IBaseFragment<MagLiveInformationContract.Presenter> implements MagLiveInformationContract.View {


    @BindView(R.id.imgV_top_bar_center)
    TextView imgVTopBarCenter;
    @BindView(R.id.fLayout_top_bar_container)
    FrameLayout fLayoutTopBarContainer;
    @BindView(R.id.tv_device_alias)
    TextView tvDeviceAlias;
    @BindView(R.id.lLayout_information_facility_name)
    LinearLayout lLayoutInformationFacilityName;
    @BindView(R.id.tv_device_cid)
    TextView tvDeviceCid;
    @BindView(R.id.tv_device_mac)
    TextView tvDeviceMac;
    @BindView(R.id.tv_device_battery_level)
    TextView tvDeviceBatteryLevel;
    @BindView(R.id.tv_device_runtime)
    TextView tvDeviceRuntime;
    //实例化fragment对象
    private MagDeviceNameDialogFragment magDeviceNameDialogFragment;
    private EditFragmentDialog editDialogFragment;
    private MagLiveInformationContract.Presenter presenter;

    private OnMagLiveDataChangeListener mListener;

    public void setListener(OnMagLiveDataChangeListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public void setPresenter(MagLiveInformationContract.Presenter presenter) {

    }

    public interface OnMagLiveDataChangeListener {
        void magLiveDataChange(String content);
    }

    public static MagLiveInformationFragment newInstance(Bundle bundle) {
        MagLiveInformationFragment fragment = new MagLiveInformationFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        BeanMagInfo bean = getArguments().getParcelable(KEY_DEVICE_ITEM_BUNDLE);
        presenter = new MagLiveInformationPresenterImp(this, bean);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewUtils.setViewPaddingStatusBar(view.findViewById(R.id.fLayout_top_bar_container));
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        magDeviceNameDialogFragment = MagDeviceNameDialogFragment.newInstance(new Bundle());
        //在执行该回调方法前，可以进行数据的预先加载
        //在oncreatView之前，把fragment页面的布局内的需要修改的信息，修改一下。
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mag_facility_information, null);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateDetails();
    }

    private void updateDetails() {
        BeanMagInfo p = presenter.getMagInfoBean();
        if (p != null) {
            tvDeviceAlias.setText(p.deviceBase.alias);
            tvDeviceCid.setText(p.deviceBase.uuid);
            tvDeviceMac.setText(p.mac);
//            tvDeviceBatteryLevel.setText();
        }
    }


    @OnClick(R.id.lLayout_information_facility_name)
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.lLayout_information_facility_name:
                toEditAlias();
                break;

            case R.id.imgV_top_bar_center:
                getFragmentManager().popBackStack();
                break;
        }

    }


    /**
     * 编辑昵称
     */
    private void toEditAlias() {
        if (editDialogFragment == null) {
            Bundle bundle = new Bundle();
            bundle.putString(KEY_TITLE, getString(R.string.EQUIPMENT_NAME));
            bundle.putString(KEY_LEFT_CONTENT, getString(R.string.OK));
            bundle.putString(KEY_RIGHT_CONTENT, getString(R.string.CANCEL));
            bundle.putBoolean(KEY_TOUCH_OUT_SIDE_DISMISS, false);
            editDialogFragment = EditFragmentDialog.newInstance(bundle);
        }
        if (editDialogFragment.isVisible())
            return;
        editDialogFragment.show(getChildFragmentManager(), "editDialogFragment");
        editDialogFragment.setAction(new EditFragmentDialog.DialogAction<String>() {
            @Override
            public void onDialogAction(int id, String value) {
                if (presenter != null) {
                    BeanMagInfo info = presenter.getMagInfoBean();
                    if (!TextUtils.isEmpty(value)
                            && !TextUtils.equals(info.deviceBase.alias, value)) {
                        tvDeviceAlias.setText(value);
                        info.deviceBase.alias = value;
                        updateDetails();
                        presenter.saveMagInfoBean(info, DpMsgMap.ID_2000003_BASE_ALIAS);

                        if (mListener != null){
                            mListener.magLiveDataChange(value);
                        }

                    }
                }
            }
        });
    }
}

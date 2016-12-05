package com.cylan.jiafeigou.n.view.mag;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.mvp.contract.mag.HomeMagLiveContract;
import com.cylan.jiafeigou.n.mvp.impl.mag.HomeMagLivePresenterImp;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.kyleduo.switchbutton.SwitchButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * 创建者     谢坤
 * 创建时间   2016/7/26 15:51
 * 描述	      ${TODO}
 * <p/>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${TODO}
 */
public class MagLiveFragment extends Fragment implements HomeMagLiveContract.View {

    @BindView(R.id.tv_device_alias)
    TextView mFacilityName;
    @BindView(R.id.btn_switch)
    SwitchButton btnSwitch;
    @BindView(R.id.tv_clear_mag_open_record)
    TextView tvClearMagOpenRecord;

    private MagLiveInformationFragment magLiveInformationFragment;
    private HomeMagLiveContract.Presenter presenter;
    private OnClearDoorOpenRecordLisenter listener;

    public interface OnClearDoorOpenRecordLisenter {
        void onClear();
    }

    public void setOnClearDoorOpenRecord(OnClearDoorOpenRecordLisenter listener) {
        this.listener = listener;
    }

    public static MagLiveFragment newInstance(Bundle bundle) {
        MagLiveFragment fragment = new MagLiveFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        magLiveInformationFragment = MagLiveInformationFragment.newInstance(new Bundle());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maglive_message, null);
        ButterKnife.bind(this, view);
        initPresenter();
        initMagDoorStateNotify();
        return view;
    }

    private void initPresenter() {
        presenter = new HomeMagLivePresenterImp(this);
    }

    /**
     * 点击回退到原来的activity
     */
    @OnClick(R.id.iv_msglive_back)
    public void onMessageBack() {
        getActivity().onBackPressed();
    }

    /**
     * 对switchButton所属的整个条目进行监听，点击之后。让switchButton进行滑动
     */
    @OnClick(R.id.rLayout_mag_live)
    public void onRelativeLayoutClick() {

    }

    /**
     * 点击进入设备信息的设置页面
     */
    @OnClick(R.id.lLayout_home_mag_information)
    public void onFacilityMessage() {
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, magLiveInformationFragment, "magLiveInformationFragment")
                .addToBackStack("MagLiveFragment")
                .commit();

        /**
         * 接口回调，得到相应的text，并且赋值给当前fragment
         */
        magLiveInformationFragment.setListener(new MagLiveInformationFragment.OnMagLiveDataChangeListener() {
            @Override
            public void magLiveDataChange(String content) {
                mFacilityName.setText(content);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (presenter != null) presenter.start();
        String editName = PreferencesUtils.getString("magEditName", "客厅摄像头");
        mFacilityName.setText(editName);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (presenter != null) presenter.stop();
    }

    @Override
    public void setPresenter(HomeMagLiveContract.Presenter presenter) {

    }

    @OnClick({R.id.btn_switch, R.id.tv_clear_mag_open_record})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_switch:
                presenter.saveSwitchState(openDoorNotify(), JConstant.OPEN_DOOR_NOTIFY);
                break;
            case R.id.tv_clear_mag_open_record:
                showClearDialog();
                break;
        }

    }

    /**
     * 删除消息对话框
     */
    private void showClearDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.Tap1_Magnetism_ClearRecord));
        builder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                presenter.clearOpenAndCloseRecord();
                if (listener != null) {
                    listener.onClear();
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(getString(R.string.CANCEL), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    @Override
    public boolean openDoorNotify() {
        return presenter.getNegation();
    }

    @Override
    public void initMagDoorStateNotify() {
        btnSwitch.setChecked(presenter.getSwitchState(JConstant.OPEN_DOOR_NOTIFY));
    }

    /**
     * 消息记录为空
     */
    @Override
    public void showNoMesg() {
        ToastUtil.showToast(getString(R.string.NO_MESSAGE));
    }

    /**
     * 显示清除进度
     */
    @Override
    public void showClearProgress() {
        LoadingDialog.showLoading(getFragmentManager(),"清除中...");
    }

    /**
     * 隐藏清除的进度
     */
    @Override
    public void hideClearProgress() {
        LoadingDialog.dismissLoading(getFragmentManager());
    }

}

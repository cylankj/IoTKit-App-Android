package com.cylan.jiafeigou.n.view.cam;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.pool.GlobalDataProxy;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.cam.HardwareUpdateContract;
import com.cylan.jiafeigou.n.mvp.impl.cam.HardwareUpdatePresenterImpl;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.cylan.jiafeigou.misc.JConstant.KEY_DEVICE_ITEM_UUID;

/**
 * 作者：zsl
 * 创建时间：2017/2/13
 * 描述：
 */
public class HardwareUpdateFragment extends IBaseFragment<HardwareUpdateContract.Presenter> implements HardwareUpdateContract.View {

    @BindView(R.id.tv_hardware_now_version)
    TextView tvHardwareNowVersion;
    @BindView(R.id.tv_hardware_new_version)
    TextView tvHardwareNewVersion;
    @BindView(R.id.hardware_update_point)
    View hardwareUpdatePoint;
    @BindView(R.id.tv_download_soft_file)
    TextView tvDownloadSoftFile;
    @BindView(R.id.download_progress)
    ProgressBar downloadProgress;
    @BindView(R.id.ll_download_pg_container)
    LinearLayout llDownloadPgContainer;
    @BindView(R.id.imgV_top_bar_center)
    TextView imgVTopBarCenter;

    private String uuid;
    private String recent_version;

    public static HardwareUpdateFragment newInstance(Bundle bundle) {
        HardwareUpdateFragment fragment = new HardwareUpdateFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.uuid = getArguments().getString(KEY_DEVICE_ITEM_UUID);
        this.recent_version = getArguments().getString("the_new_soft_version");
        basePresenter = new HardwareUpdatePresenterImpl(this, uuid);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hardware_update, null);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewUtils.setViewPaddingStatusBar(view.findViewById(R.id.fLayout_top_bar_container));
    }

    @Override
    public void onStart() {
        super.onStart();
        initView();
    }

    private void initView() {
        tvHardwareNewVersion.setText(recent_version);
        String sVersion = GlobalDataProxy.getInstance().getValue(uuid, DpMsgMap.ID_208_DEVICE_SYS_VERSION, "");
        tvHardwareNowVersion.setText(sVersion);
        if (!tvHardwareNowVersion.equals(tvHardwareNewVersion)){
            hardwareUpdatePoint.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void setPresenter(HardwareUpdateContract.Presenter presenter) {

    }

    @OnClick({R.id.tv_download_soft_file,R.id.imgV_top_bar_center})
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.imgV_top_bar_center:
                getFragmentManager().popBackStack();
                break;

            case R.id.tv_download_soft_file:

                if(NetUtils.getJfgNetType(getContext()) == 0){
                    ToastUtil.showNegativeToast(getString(R.string.GLOBAL_NO_NETWORK));
                    return;
                }
                DpMsgDefine.DPNet net = GlobalDataProxy.getInstance().getValue(uuid, DpMsgMap.ID_201_NET, null);
                boolean show = net != null && JFGRules.isDeviceOnline(net);
                if (!show){
                    ToastUtil.showNegativeToast(getString(R.string.NOT_ONLINE));
                    return;
                }

                if (!tvDownloadSoftFile.getText().equals("升级")){
                    handlerDownLoad();
                }else {
                    handlerUpdate();
                }
                break;


        }

    }

    /**
     * 处理升级
     */
    private void handlerUpdate() {

    }

    /**
     * 处理下载
     */
    private void handlerDownLoad() {
        if (tvHardwareNewVersion.getText().equals(tvHardwareNowVersion.getText())){
            ToastUtil.showPositiveToast(getString(R.string.NEW_VERSION));
            return;
        }

    }
}

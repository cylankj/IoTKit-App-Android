package com.cylan.jiafeigou.n.view.home;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.mvp.contract.home.HomeSettingContract;
import com.cylan.jiafeigou.n.mvp.impl.home.HomeSettingPresenterImp;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.cylan.jiafeigou.widget.ShareGridView;
import com.kyleduo.switchbutton.SwitchButton;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/5
 * 描述：
 */
public class HomeSettingFragment extends Fragment implements HomeSettingContract.View, CompoundButton.OnCheckedChangeListener {

    @BindView(R.id.rl_home_setting_about)
    RelativeLayout rlHomeSettingAbout;
    @BindView(R.id.rl_home_setting_clear)
    RelativeLayout rlHomeSettingClear;
    @BindView(R.id.progressbar_load_cache_size)
    ProgressBar progressbarLoadCacheSize;
    @BindView(R.id.tv_cache_size)
    TextView tvCacheSize;
    @BindView(R.id.btn_item_switch_accessMes)
    SwitchButton btnItemSwitchAccessMes;
    @BindView(R.id.btn_item_switch_voide)
    SwitchButton btnItemSwitchVoide;
    @BindView(R.id.btn_item_switch_shake)
    SwitchButton btnItemSwitchShake;
    @BindView(R.id.rl_sound_container)
    RelativeLayout rlSoundContainer;
    @BindView(R.id.rl_vibrate_container)
    RelativeLayout rlVibrateContainer;
    @BindView(R.id.rl_home_setting_recommend)
    RelativeLayout rlHomeSettingRecommend;
    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;

    private HomeSettingContract.Presenter presenter;
    private AboutFragment aboutFragment;
    private Dialog mShareDlg;
    private AppAdater appAdater;

    public static HomeSettingFragment newInstance() {
        return new HomeSettingFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        aboutFragment = AboutFragment.newInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_mine_setting, container, false);
        ButterKnife.bind(this, view);
        initPresenter();
        presenter.calculateCacheSize();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rlHomeSettingAbout.setVisibility(getResources().getBoolean(R.bool.show_about) ? View.VISIBLE : View.GONE);
        customToolbar.setBackAction(click -> getFragmentManager().popBackStack());
    }

    private void initPresenter() {
        presenter = new HomeSettingPresenterImp(this);
    }

    @Override
    public void setPresenter(HomeSettingContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @OnClick({R.id.rl_home_setting_about, R.id.rl_home_setting_clear, R.id.rl_home_setting_recommend})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_home_setting_about:
                ViewUtils.deBounceClick(view);
                AppLogger.e("rl_home_setting_about");
                getFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                                , R.anim.slide_in_left, R.anim.slide_out_right)
                        .add(android.R.id.content, aboutFragment, "aboutFragment")
                        .addToBackStack("mineHelpFragment")
                        .commit();
                break;

            case R.id.rl_home_setting_clear:
                if ("0.0M".equals(tvCacheSize.getText())) return;
                presenter.clearCache();
                break;

            case R.id.rl_home_setting_recommend:
                //推荐给亲友
                share();
                break;
        }
    }

    @Override
    public void showLoadCacheSizeProgress() {
        progressbarLoadCacheSize.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoadCacheSizeProgress() {
        progressbarLoadCacheSize.setVisibility(View.GONE);
    }

    @Override
    public void setCacheSize(String size) {
        tvCacheSize.setText(size);
    }

    @Override
    public void showClearingCacheProgress() {
        LoadingDialog.showLoading(getFragmentManager(), getString(R.string.ClearingTips));
    }

    @Override
    public void hideClearingCacheProgress() {
        LoadingDialog.dismissLoading(getFragmentManager());
    }

    @Override
    public void clearFinish() {
        if (isDetached()) return;
        tvCacheSize.setText("0.0M");
        ToastUtil.showToast(getString(R.string.Clear_Sdcard_tips3));
    }

    @Override
    public void clearNoCache() {
        ToastUtil.showToast(getString(R.string.RET_EREPORT_NO_DATA));
    }

    @Override
    public boolean switchAcceptMesg() {
        return presenter.getNegation();
    }

    @Override
    public void initSwitchState(final RxEvent.GetUserInfo userInfo) {
        btnItemSwitchAccessMes.setChecked(userInfo.jfgAccount.isEnablePush());
        btnItemSwitchVoide.setChecked(userInfo.jfgAccount.isEnableSound());
        btnItemSwitchShake.setChecked(userInfo.jfgAccount.isEnableVibrate());
        if (!userInfo.jfgAccount.isEnablePush()) {
            rlSoundContainer.setVisibility(View.GONE);
            rlVibrateContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (presenter != null) {
            presenter.stop();
        }
        if (mShareDlg != null) {
            mShareDlg.dismiss();
            mShareDlg = null;
        }
        if (appAdater != null){
            appAdater = null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (presenter != null) presenter.start();
        initSwitchBtnListener();
    }

    private void initSwitchBtnListener() {
        btnItemSwitchAccessMes.setOnCheckedChangeListener(this);
        btnItemSwitchVoide.setOnCheckedChangeListener(this);
        btnItemSwitchShake.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.btn_item_switch_accessMes:
                presenter.savaSwitchState(isChecked, JConstant.RECEIVE_MESSAGE_NOTIFICATION);
                if (!isChecked) {
                    rlSoundContainer.setVisibility(View.GONE);
                    rlVibrateContainer.setVisibility(View.GONE);
                } else {
                    rlSoundContainer.setVisibility(View.VISIBLE);
                    rlVibrateContainer.setVisibility(View.VISIBLE);
                }
                break;

            case R.id.btn_item_switch_voide:
                presenter.savaSwitchState(isChecked, JConstant.OPEN_VOICE);
                break;

            case R.id.btn_item_switch_shake:
                presenter.savaSwitchState(isChecked, JConstant.OPEN_SHAKE);
                break;
        }
    }

    //*************
    public void share() {
        if (mShareDlg == null) {
            mShareDlg = new Dialog(getActivity(), R.style.func_dialog);
            View content = View.inflate(getContext(), R.layout.dialog_app_share, null);
            TextView cancel = (TextView) content.findViewById(R.id.btn_cancle);
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mShareDlg.dismiss();
                }
            });
            ShareGridView gridView = (ShareGridView) content.findViewById(R.id.gridview);
            final Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            String con = getString(R.string.share_content);
            if (!TextUtils.isEmpty(JConstant.EFAMILY_URL_PREFIX)) {
                con += JConstant.EFAMILY_URL_PREFIX;
            }
            intent.putExtra(Intent.EXTRA_TEXT, con);

            List<ResolveInfo> list = getContext().getPackageManager().queryIntentActivities(intent, 0);
            if (appAdater == null)
            appAdater = new AppAdater(getContext());
            for (ResolveInfo info : list) {
                appAdater.add(info);
            }
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ResolveInfo info = appAdater.getItem(position);
                    intent.setComponent(new ComponentName(info.activityInfo.packageName, info.activityInfo.name));
                    startActivity(intent);
                }
            });
            gridView.setAdapter(appAdater);
            mShareDlg.setContentView(content);
            mShareDlg.setCanceledOnTouchOutside(true);
        }
        try {
            if(mShareDlg.isShowing())return;
            mShareDlg.show();
        } catch (Exception e) {
            AppLogger.e(e.toString());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    class ViewHolder {
        ImageView icon;
        TextView name;
        ResolveInfo info;
    }

    class AppAdater extends ArrayAdapter<ResolveInfo> {

        public AppAdater(Context context) {
            super(context, 0);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder vh;
            if (null == convertView) {
                convertView = View.inflate(getContext(), R.layout.item_app_share, null);
                vh = new ViewHolder();
                vh.icon = (ImageView) convertView.findViewById(R.id.icon);
                vh.name = (TextView) convertView.findViewById(R.id.name);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }
            ResolveInfo info = getItem(position);
            PackageManager pm = getContext().getPackageManager();
            vh.name.setText(info.loadLabel(pm));
            vh.icon.setImageDrawable(info.loadIcon(pm));
            return convertView;
        }
    }

}

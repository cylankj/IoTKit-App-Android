package com.cylan.jiafeigou.n.view.cam;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.pool.GlobalDataProxy;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.setting.TimezoneContract;
import com.cylan.jiafeigou.n.mvp.impl.setting.TimezonePresenterImpl;
import com.cylan.jiafeigou.n.mvp.model.TimeZoneBean;
import com.cylan.jiafeigou.n.view.adapter.DeviceTimeZoneAdapter;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;
import com.cylan.jiafeigou.widget.dialog.SimpleDialogFragment;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

import static com.cylan.jiafeigou.misc.JConstant.KEY_DEVICE_ITEM_UUID;


/**
 * 创建者     谢坤
 * 创建时间   2016/7/15 10:21
 * 描述	      ${TODO}
 * <p/>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${TODO}
 */
public class DeviceTimeZoneFragment extends IBaseFragment<TimezoneContract.Presenter>
        implements TimezoneContract.View {


    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.et_timezone_search)
    EditText etTimezoneSearch;
    @BindView(R.id.iv_timezone_search)
    ImageView ivTimezoneSearch;
    @BindView(R.id.lv_timezone_detail)
    RecyclerView lvTimezoneDetail;
    @BindView(R.id.tv_timezone_no_result)
    TextView tvTimezoneNoResult;
    private DeviceTimeZoneAdapter adapter;
    private SimpleDialogFragment simpleDialog;
    private String uuid;

    @Override
    public void timezoneList(List<TimeZoneBean> list) {
        if (adapter != null) {
            adapter.clear();
            adapter.addAll(list);
        }
        tvTimezoneNoResult.setVisibility(list == null || list.size() == 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setPresenter(TimezoneContract.Presenter presenter) {
        basePresenter = presenter;
    }

    public DeviceTimeZoneFragment() {
    }

    public static DeviceTimeZoneFragment newInstance(Bundle bundle) {
        DeviceTimeZoneFragment fragment = new DeviceTimeZoneFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.uuid = getArguments().getString(KEY_DEVICE_ITEM_UUID);
        basePresenter = new TimezonePresenterImpl(this, uuid);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_timezone, null);
        ButterKnife.bind(this, view);
        return view;
    }

    private void initDialog() {
        if (simpleDialog == null) {
            simpleDialog = new SimpleDialogFragment();
            Bundle bundle = new Bundle();
            bundle.putString(BaseDialog.KEY_TITLE, getString(R.string.TIMEZONE_CHOOSE));
            bundle.putString(SimpleDialogFragment.KEY_LEFT_CONTENT, getString(R.string.CANCEL));
            bundle.putString(SimpleDialogFragment.KEY_RIGHT_CONTENT, getString(R.string.OK));
            bundle.putString(SimpleDialogFragment.KEY_CONTENT_CONTENT, getString(R.string.TIMEZONE_INFO));
            simpleDialog.setArguments(bundle);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ViewUtils.setViewPaddingStatusBar(view.findViewById(R.id.rLayout_timezone_list));
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        lvTimezoneDetail.setLayoutManager(layoutManager);
        adapter = new DeviceTimeZoneAdapter(getActivity().getApplicationContext());
        DpMsgDefine.MsgTimeZone info = GlobalDataProxy.getInstance().getValue(uuid, DpMsgMap.ID_214_DEVICE_TIME_ZONE, null);
        String timeZoneId = info == null ? "" : info.timezone;
        adapter.setChooseId(timeZoneId);
        lvTimezoneDetail.setAdapter(adapter);
        adapter.setOnCLick(new DeviceTimeZoneAdapter.OnCLick() {
            @Override
            public void onClick(View v) {
                initDialog();
                if (simpleDialog.isVisible() || simpleDialog.isResumed()) {
                    return;
                }
                int id = ViewUtils.getParentAdapterPosition(lvTimezoneDetail, v, R.id.lLayout_timezone_item);
                simpleDialog.setValue(adapter.getItem(id));
                simpleDialog.setAction(new BaseDialog.BaseDialogAction() {
                    @Override
                    public void onDialogAction(int id, Object value) {
                        if (value != null && value instanceof TimeZoneBean) {
                            DpMsgDefine.MsgTimeZone timeZone = GlobalDataProxy.getInstance().getValue(uuid, DpMsgMap.ID_214_DEVICE_TIME_ZONE, null);
                            int offset = timeZone.offset;
                            if (offset != ((TimeZoneBean) value).getOffset()) {
                                timeZone.timezone = ((TimeZoneBean) value).getName();
                                if (callBack != null)
                                    callBack.callBack(timeZone);
                                getActivity().onBackPressed();
                                //没必要设置
                            }
                        }
                    }
                });
                simpleDialog.show(getChildFragmentManager(), "simpleDialog");
                hideKeyboard(getView());
            }
        });
        lvTimezoneDetail.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    hideKeyboard(getView());
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @OnTextChanged(R.id.et_timezone_search)
    public void onInputTextChanged(CharSequence s, int start, int before, int count) {
        basePresenter.onSearch(s.toString());
    }

    /**
     * 点击确认按钮之后，把软键盘进行隐藏
     */
    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @OnClick(R.id.iv_timezone_search)
    public void onClick() {
        etTimezoneSearch.getText().clear();
        etTimezoneSearch.setFocusableInTouchMode(true);
        etTimezoneSearch.setFocusable(true);
        etTimezoneSearch.requestFocus();
    }
}

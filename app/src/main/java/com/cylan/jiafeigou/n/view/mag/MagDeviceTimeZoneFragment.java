package com.cylan.jiafeigou.n.view.mag;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.model.TimeZoneBean;
import com.cylan.jiafeigou.n.view.adapter.CamDeviceTimeZoneAdapter;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.utils.ListUtils;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * 创建者     谢坤
 * 创建时间   2016/7/15 10:21
 * 描述	      ${TODO}
 * <p/>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${TODO}
 */
public class MagDeviceTimeZoneFragment extends Fragment {

    private static final String TAG = "tag";

    private static final String TIME_ZONE_TAG = "timezone";
    private static final String GMT_TAG = "gmt";
    private static final String ID_TAG = "id";

    //用来保存最初的data数据，在搜索内容为空的时候显示
    private List<TimeZoneBean> mNoSearchCity = new ArrayList<>();

    @BindView(R.id.tv_information_timezone_text)
    TextView mTvText;

    @BindView(R.id.et_information_timezone_find)
    EditText mEtFind;

    @BindView(R.id.iv_information_back)
    ImageView mIvBack;

    @BindView(R.id.iv_information_timezone_search)
    ImageView mIvSearch;

    @BindView(R.id.lv_information_timezone_detail)
    RecyclerView mDetail;

    @BindView(R.id.tv_timezone_noresult)
    TextView mTvNoresult;

    private String mDetailText;
    private List<TimeZoneBean> mCityList;
    private List<TimeZoneBean> findResult;
    private CamDeviceTimeZoneAdapter adapter;


    protected OnMagTimezoneChangeListener mTimezoneListener;

    public void setListener(OnMagTimezoneChangeListener mListener) {
        this.mTimezoneListener = mListener;
    }

    /**
     * 接口回调，用来刷新UI
     */
    public interface OnMagTimezoneChangeListener {
        void magTimezoneChangeListener(String content);
    }

    public MagDeviceTimeZoneFragment() {
    }

    public static MagDeviceTimeZoneFragment newInstance(Bundle bundle) {
        MagDeviceTimeZoneFragment fragment = new MagDeviceTimeZoneFragment();
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        mEtFind.getText().clear();
        initView();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_timezone, null);
        ButterKnife.bind(this, view);
        mTvText.setVisibility(View.VISIBLE);
        mEtFind.setVisibility(View.INVISIBLE);

        initData();
        initView();
        initEtListener();
        return view;
    }

    private void initView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        mDetail.setLayoutManager(layoutManager);
        adapter = new CamDeviceTimeZoneAdapter(getActivity().getApplicationContext(), mCityList, null);
        showDialog(mCityList);
        mDetail.setAdapter(adapter);
    }

    private void initData() {
        mCityList = new ArrayList<>();
        XmlResourceParser xrp = getResources().getXml(R.xml.timezones);
        try {
            while (xrp.getEventType() != XmlResourceParser.END_DOCUMENT) {
                if (xrp.getEventType() == XmlResourceParser.START_TAG) {
                    TimeZoneBean bean = new TimeZoneBean();
                    final String name = xrp.getName();
                    if (TextUtils.equals(name, TIME_ZONE_TAG)) {
                        final String timeGmtName = xrp.getAttributeValue(0);
                        bean.setGmt(timeGmtName);
                        final String timeIdName = xrp.getAttributeValue(1);
                        bean.setId(timeIdName);
                        String rigon = xrp.nextText();
                        bean.setName(rigon);
                        mCityList.add(bean);
                    }
                }
                xrp.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {

        }
    }

    private void initEtListener() {

        mIvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
                hideKeyboard(v);
            }
        });

        mEtFind.setFocusable(true);
        mEtFind.setFocusableInTouchMode(true);
        mEtFind.requestFocus();
        mIvSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTvText.setVisibility(View.INVISIBLE);
                mEtFind.setVisibility(View.VISIBLE);
                InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(mEtFind, InputMethodManager.SHOW_FORCED);
            }
        });

        mEtFind.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s)) {
                    setRefreshData(assembleRawList());
                    mTvNoresult.setVisibility(View.GONE);
                    return;
                }
                findResult = new ArrayList<>();
                for (TimeZoneBean str : mCityList) {
                    if (str.getName().contains(s)) {
                        //如果包含就把包含的每个丢进新建的数组中去
                        findResult.add(str);
                    }
                }
                setRefreshData(findResult);
                mTvNoresult.setVisibility(ListUtils.isEmpty(findResult) ? View.VISIBLE : View.GONE);
            }
        });
    }

    private List<TimeZoneBean> assembleRawList() {
        if (ListUtils.isEmpty(mNoSearchCity))
            mNoSearchCity.addAll(mCityList);
        return mNoSearchCity;
    }

    /**
     * 当editText更新的时候，重新setAdapter
     */
    private void setRefreshData(List<TimeZoneBean> data) {
        adapter = new CamDeviceTimeZoneAdapter(getActivity().getApplicationContext(), data, null);
        showDialog(data);
        mDetail.setAdapter(adapter);
    }

    /**
     * 对于不同的数据集合，展示相同的dialog并保存
     *
     * @param data
     */
    public void showDialog(final List<TimeZoneBean> data) {
        adapter.setOnRecyclerViewListener(new CamDeviceTimeZoneAdapter.OnRecyclerViewListener() {
            @Override
            public void onItemClick(final View view, int position) {
                mDetailText = data.get(position).getName();
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("提示");
                builder.setMessage("更改设备时区可能导致录像时间发生变化，是否继续？");
                builder.setPositiveButton("继续", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveEditName();
                        if (mTimezoneListener != null) {
                            mTimezoneListener.magTimezoneChangeListener(mDetailText);
                        }
                        hideKeyboard(view);
                        getFragmentManager().popBackStack();
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();
            }
        });
    }


    /**
     * 点击确认按钮之后，把软键盘进行隐藏
     */
    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * 保存用户所选择的时区
     */
    private void saveEditName() {
        PreferencesUtils.putString(getActivity(), "magDetailText", mDetailText);
    }
}

package com.cylan.jiafeigou.n.view.mag;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.utils.ListUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


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

    public static final String TAG = "MagDeviceTimeZoneFragment";

    final static String[] mCity = {"马朱罗", "中途岛", "檀香山", "安克雷奇", "洛杉矶/美国太平洋",
            "提华纳/美国太平洋", "凤凰城美国山区", "奇瓦瓦", "丹佛/美国山区", "哥斯达黎加/美国中部",
            "芝加哥/美国中部", "墨西哥城/美国中部", "里贾纳/美国中部", "波哥大/哥伦比亚",
            "纽约/美国东部", "加拉加斯/委内瑞拉", "巴巴多斯/大西洋", "马瑙斯/亚马逊", "圣地亚哥",
            "圣约翰/纽芬兰", "圣保罗", "布宜诺斯艾利斯", "戈特霍布", "蒙得维的亚/乌拉圭",
            "南乔治亚", "亚述尔群岛", "佛得角", "卡萨布兰卡", "伦敦/格林尼治", "阿姆斯特丹/中欧",
            "贝尔格莱德/中欧", "布鲁塞尔/中欧", "萨拉热窝/中", "温得和克", "布拉扎维/西部非洲",
            "安曼/东欧", "雅典/东欧", "贝鲁特/东欧", "开罗/东欧", "赫尔辛基/东欧", "耶路撒冷/以色列",
            "明斯克", "哈拉雷/中部非洲", "巴格达", "莫斯科", "科威特", "内罗毕/东部非洲", "德黑兰/伊朗",
            "巴库", "第比利斯", "埃里温", "迪拜", "喀布尔/阿富汗", "卡拉奇", "乌拉尔", "叶卡捷林堡",
            "加尔各答", "科伦坡", "加德满都/尼泊尔", "阿拉木图", "仰光/缅甸", "克拉斯诺亚尔斯克", "北京/中国",
            "香港/中国", "伊尔库茨克", "吉隆坡", "佩思", "台北时间 (台北)", "首尔", "东京/日本", "雅库茨克",
            "阿德莱德", "达尔文", "布里斯班", "霍巴特", "悉尼", "符拉迪沃斯托克/海参崴", "关岛", "马加丹",
            "奥克兰", "斐济", "东加塔布"};

    //用来存放，模糊搜索包含的城市。刷新listView
    private List<String> mSelectedCity = new ArrayList<>();
    //用来保存最初的data数据，在搜索内容为空的时候显示
    private List<String> mNoSearchCity = new ArrayList<>();
    private String etText;

    private TextView mTvText;
    private EditText mEtFind;
    private ImageView mIvBack;
    private ImageView mIvSearch;
    private ListView mDetail;
    private String mDetailText;


    protected OnMagTimezoneChangeListener mTimezoneListener;
    private DetailAdapter mDetailAdapter;
    private List<String> mCityList;
    private boolean adapterState = false;
    private TextView mNoResult;

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
        mCityList = Arrays.asList(mCity);
    }

    private List<String> assembleRawList() {
        if (ListUtils.isEmpty(mNoSearchCity))
            mNoSearchCity.addAll(Arrays.asList(mCity));
        return mNoSearchCity;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_edit_timezone, null);
        mTvText = (TextView) view.findViewById(R.id.tv_information_timezone_text);
        mEtFind = (EditText) view.findViewById(R.id.et_information_timezone_find);
        mIvBack = (ImageView) view.findViewById(R.id.iv_information_back);
        mIvSearch = (ImageView) view.findViewById(R.id.iv_information_timezone_search);

        mDetail = (ListView) view.findViewById(R.id.lv_information_timezone_detail);
        mDetail.setDivider(new ColorDrawable(getResources().getColor(R.color.color_f2f2f2)));
        mDetail.setDividerHeight(2);

        mNoResult = (TextView) view.findViewById(R.id.tv_information_timezone_noresult);
        mIvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
                hideKeyboard(v);
            }
        });
        mTvText.setVisibility(View.VISIBLE);
        mEtFind.setVisibility(View.INVISIBLE);
        mDetailAdapter = new DetailAdapter();
        mDetail.setAdapter(mDetailAdapter);
        initListener();
        initEtListener();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mEtFind.getText().clear();
    }

    @Override
    public void onStop() {
        super.onStop();
//        setRefreshData(mNoSearchCity);
//        mEtFind.setText("");
        adapterState = false;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void initEtListener() {
        /**
         * 强制让editText开启软键盘
         */
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
                    mNoResult.setVisibility(View.GONE);
                    return;
                }
                List<String> findResult = new ArrayList<>();
                for (String str : mCity) {
                    if (str.contains(s)) {
                        //如果包含就把包含的每个丢进新建的数组中去
                        findResult.add(str);
                    }
                }
                setRefreshData(findResult);
                mNoResult.setVisibility(ListUtils.isEmpty(findResult) ? View.VISIBLE : View.GONE);
            }
        });
    }

    /**
     * 当editText更新的时候，重新setAdapter
     */
    private void setRefreshData(List<String> data) {
        mDetailAdapter.notifyDataSetInvalidated();
        mCityList = data;
        mDetail.setAdapter(mDetailAdapter);
    }

    /**
     * 用来显示listView的方法
     */
    private void initListener() {
        mDetail.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                if (adapterState) {
                    mDetailText = mSelectedCity.get(position);
                } else {
                    mDetailText = mCityList.get(position);
                }
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
     * 保存用户输入的设备名称
     */
    private void saveEditName() {
        PreferencesUtils.putString(getActivity(), "magDetailText", mDetailText);
    }


    /**
     * 时区设备的listView所需要的adapter
     */
    private class DetailAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            if (mCity != null) {
                return mCityList.size();
            }
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = View.inflate(getActivity(), R.layout.fragment_edit_timezone_item, null);
                holder.mTv = (TextView) convertView.findViewById(R.id.tv_timezone_item);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.mTv.setText(mCityList.get(position));
            return convertView;
        }


        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }
    }

    class ViewHolder {
        TextView mTv;
    }
}

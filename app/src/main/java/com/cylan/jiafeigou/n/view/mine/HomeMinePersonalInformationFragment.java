package com.cylan.jiafeigou.n.view.mine;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.utils.PreferencesUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 创建者     谢坤
 * 创建时间   2016/8/9 10:02
 * 描述	      ${TODO}
 * <p/>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${TODO}
 */
public class HomeMinePersonalInformationFragment extends Fragment {

    @BindView(R.id.tv_home_mine_personal_mailbox)
    TextView mTvMailBox;

    private HomeMinePersonalInformationMailBoxFragment mailBoxFragment;

    public static HomeMinePersonalInformationFragment newInstance(Bundle bundle) {
        HomeMinePersonalInformationFragment fragment = new HomeMinePersonalInformationFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mailBoxFragment = HomeMinePersonalInformationMailBoxFragment.newInstance(new Bundle());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_mine_personal_information, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        String mailBoxText = PreferencesUtils.getString(getActivity(), "邮箱", "未设置");
        mTvMailBox.setText(mailBoxText);
    }

    @OnClick({R.id.iv_home_mine_personal_back, R.id.btn_home_mine_personal_information, R.id.lLayout_home_mine_personal_mailbox})
    public void onClick(View view) {
        switch (view.getId()) {
            //点击回退到Mine的fragment
            case R.id.iv_home_mine_personal_back:
                getFragmentManager().popBackStack();
                break;
            //点击退出做相应的逻辑
            case R.id.btn_home_mine_personal_information:
                break;
            //点击邮箱跳转到相应的页面
            case R.id.lLayout_home_mine_personal_mailbox:
                getFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                                , R.anim.slide_in_left, R.anim.slide_out_right)
                        .add(android.R.id.content, mailBoxFragment, "mailBoxFragment")
                        .addToBackStack("personalInformationFragment")
                        .commit();
                if (getActivity() != null && getActivity().getFragmentManager() != null) {
                    mailBoxFragment.setListener(new HomeMinePersonalInformationMailBoxFragment.OnBindMailBoxListener() {
                        @Override
                        public void mailBoxChange(String content) {
                            mTvMailBox.setText(content);
                        }
                    });
                }
                break;
        }
    }

}

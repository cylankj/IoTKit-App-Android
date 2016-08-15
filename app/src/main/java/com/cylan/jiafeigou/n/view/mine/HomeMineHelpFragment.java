package com.cylan.jiafeigou.n.view.mine;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 创建者     谢坤
 * 创建时间   2016/8/8 11:26
 * 描述	      ${TODO}
 * <p/>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${TODO}
 */
public class HomeMineHelpFragment extends Fragment {

    @BindView(R.id.rLayout_mine_help_top_bar)
    RelativeLayout rLayoutTopBar;

    @BindView(R.id.tv_mine_help_back)
    TextView mTvHelpBack;

    @BindView(R.id.tv_mine_help_suggestion)
    TextView mTvHelpSuggestion;
    private HomeMineHelpSuggestionFragment homeMineHelpSuggestionFragment;

    public static HomeMineHelpFragment newInstance(Bundle bundle) {
        HomeMineHelpFragment fragment = new HomeMineHelpFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeMineHelpSuggestionFragment = HomeMineHelpSuggestionFragment.newInstance(new Bundle());
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine_help, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @OnClick({R.id.tv_mine_help_back, R.id.tv_mine_help_suggestion})
    public void onClick(View view) {
        switch (view.getId()) {
            //点击退回home_mine的fragment
            case R.id.tv_mine_help_back:
                getFragmentManager().popBackStack();
                break;
            //点击进入意见反馈的页面
            case R.id.tv_mine_help_suggestion:
                getFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                                , R.anim.slide_in_left, R.anim.slide_out_right)
                        .add(android.R.id.content, homeMineHelpSuggestionFragment, "homeMineHelpSuggestionFragment")
                        .addToBackStack("mineHelpFragment")
                        .commit();
                break;
        }
    }
}

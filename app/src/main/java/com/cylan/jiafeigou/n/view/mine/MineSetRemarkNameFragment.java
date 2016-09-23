package com.cylan.jiafeigou.n.view.mine;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineSetRemarkNameContract;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/23
 * 描述：
 */
public class MineSetRemarkNameFragment extends Fragment implements MineSetRemarkNameContract.View {

    @BindView(R.id.iv_top_bar_left_back)
    ImageView ivTopBarLeftBack;
    @BindView(R.id.iv_mine_set_remarkname_bind)
    ImageView ivMineSetRemarknameBind;
    @BindView(R.id.et_mine_set_remarkname_new_name)
    EditText etMineSetRemarknameNewName;
    @BindView(R.id.view_mine_personal_set_remarkname_new_name_line)
    View viewMinePersonalSetRemarknameNewNameLine;
    @BindView(R.id.iv_mine_personal_set_remarkname_clear)
    ImageView ivMinePersonalSetRemarknameClear;

    private OnSetUsernameListener listener;

    public interface OnSetUsernameListener {

        void userNameChange(String name);

    }

    public void setOnSetUsernameListener(OnSetUsernameListener listener) {
        this.listener = listener;
    }

    public static MineSetRemarkNameFragment newInstance(Bundle bundle) {
        MineSetRemarkNameFragment fragment = new MineSetRemarkNameFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_set_remark_name, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void setPresenter(MineSetRemarkNameContract.Presenter presenter) {

    }

    @OnClick({R.id.iv_top_bar_left_back, R.id.iv_mine_set_remarkname_bind, R.id.iv_mine_personal_set_remarkname_clear})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_top_bar_left_back:
                getFragmentManager().popBackStack();
                break;
            case R.id.iv_mine_set_remarkname_bind:
                break;
            case R.id.iv_mine_personal_set_remarkname_clear:
                break;
        }
    }
}

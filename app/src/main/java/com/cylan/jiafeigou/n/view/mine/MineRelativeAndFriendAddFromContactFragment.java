package com.cylan.jiafeigou.n.view.mine;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineRelativeAndFriendAddFromContactContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineRelativeAndFriendAddFromContactPresenterImp;
import com.cylan.jiafeigou.n.mvp.model.SuggestionChatInfoBean;
import com.cylan.jiafeigou.n.view.adapter.RelativeAndFriendAddFromContactAdapter;
import com.cylan.jiafeigou.utils.ToastUtil;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
public class MineRelativeAndFriendAddFromContactFragment extends Fragment implements MineRelativeAndFriendAddFromContactContract.View {

    @BindView(R.id.iv_home_mine_relativesandfriends_add_from_contact_back)
    ImageView ivHomeMineRelativesandfriendsAddFromContactBack;
    @BindView(R.id.et_add_phone_number)
    EditText etAddPhoneNumber;
    @BindView(R.id.rcy_contact_list)
    RecyclerView rcyContactList;

    private MineRelativeAndFriendAddFromContactContract.Presenter presenter;
    private RelativeAndFriendAddFromContactAdapter relativeAndFriendAddFromContactAdapter;
    private MineAddFromContactFragment mineAddFromContactFragment;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mineAddFromContactFragment = MineAddFromContactFragment.newInstance();
    }

    public static MineRelativeAndFriendAddFromContactFragment newInstance() {
        return new MineRelativeAndFriendAddFromContactFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine_relativeandfriend_add_from_contact, container, false);
        ButterKnife.bind(this, view);
        initPresenter();
        presenter.initContactData();
        initEditTextListenter();
        return view;
    }

    private void initEditTextListenter() {
        etAddPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                presenter.filterPhoneData(s.toString());
            }
        });

    }

    private void initPresenter() {
        presenter = new MineRelativeAndFriendAddFromContactPresenterImp(this);
    }

    @Override
    public void setPresenter(MineRelativeAndFriendAddFromContactContract.Presenter presenter) {

    }

    @OnClick(R.id.iv_home_mine_relativesandfriends_add_from_contact_back)
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_home_mine_relativesandfriends_add_from_contact_back:
                getFragmentManager().popBackStack();
                break;
        }
    }

    @Override
    public void setRcyAdapter(ArrayList<SuggestionChatInfoBean> list) {
        rcyContactList.setLayoutManager(new LinearLayoutManager(getContext()));
        relativeAndFriendAddFromContactAdapter = new RelativeAndFriendAddFromContactAdapter(list);
        rcyContactList.setAdapter(relativeAndFriendAddFromContactAdapter);
    }

    @Override
    public void InitItemClickListener() {
        relativeAndFriendAddFromContactAdapter.setOnContactItemClickListener(new RelativeAndFriendAddFromContactAdapter.onContactItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                ToastUtil.showToast(getContext(), relativeAndFriendAddFromContactAdapter.getAdapterList().get(position).getName());
                getFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                                , R.anim.slide_in_left, R.anim.slide_out_right)
                        .add(android.R.id.content, mineAddFromContactFragment, "mineAddFromContactFragment")
                        .addToBackStack("mineHelpFragment")
                        .commit();
            }
        });
    }


    /*@OnTextChanged(value = R.id.et_add_phone_number, callback = OnTextChanged.Callback.BEFORE_TEXT_CHANGED)
    void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }
    @OnTextChanged(value = R.id.et_add_phone_number, callback = OnTextChanged.Callback.TEXT_CHANGED)
    void onTextChanged(CharSequence s, int start, int before, int count) {
        presenter.filterPhoneData(s.toString());
    }
    @OnTextChanged(value = R.id.et_add_phone_number, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void afterTextChanged(Editable s) {

    }*/

    @Override
    public void onStop() {
        super.onStop();
        if (presenter != null) {
            presenter.stop();
        }
    }
}

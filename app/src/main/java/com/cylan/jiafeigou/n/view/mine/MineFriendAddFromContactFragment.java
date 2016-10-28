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
import android.widget.LinearLayout;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineRelativeAndFriendAddFromContactContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineRelativeAndFriendAddFromContactPresenterImp;
import com.cylan.jiafeigou.n.mvp.model.SuggestionChatInfoBean;
import com.cylan.jiafeigou.n.view.adapter.RelativeAndFriendAddFromContactAdapter;
import com.cylan.superadapter.OnItemClickListener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
public class MineFriendAddFromContactFragment extends Fragment implements MineRelativeAndFriendAddFromContactContract.View, RelativeAndFriendAddFromContactAdapter.onContactItemClickListener {

    @BindView(R.id.iv_home_mine_relativesandfriends_add_from_contact_back)
    ImageView ivHomeMineRelativesandfriendsAddFromContactBack;
    @BindView(R.id.et_add_phone_number)
    EditText etAddPhoneNumber;
    @BindView(R.id.rcy_contact_list)
    RecyclerView rcyContactList;
    @BindView(R.id.ll_no_contact)
    LinearLayout llNoContact;

    private MineRelativeAndFriendAddFromContactContract.Presenter presenter;
    private MineAddFromContactFragment mineAddFromContactFragment;
    private RelativeAndFriendAddFromContactAdapter contactListAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static MineFriendAddFromContactFragment newInstance() {
        return new MineFriendAddFromContactFragment();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (presenter != null){
            presenter.start();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine_relativeandfriend_add_from_contact, container, false);
        ButterKnife.bind(this, view);
        initPresenter();
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
    public void initContactRecycleView(ArrayList<SuggestionChatInfoBean> list) {
        rcyContactList.setLayoutManager(new LinearLayoutManager(getContext()));
        contactListAdapter = new RelativeAndFriendAddFromContactAdapter(getView().getContext(),list,null);
        rcyContactList.setAdapter(contactListAdapter);
        initAdaListener();
    }

    /**
     * 设置列表监听
     */
    private void initAdaListener() {
        contactListAdapter.setOnContactItemClickListener(this);
        contactListAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int viewType, int position) {
                //TODO 跳转到联系人的详情界面去
            }
        });
    }

    @Override
    public void jump2SendAddMesgFragment(SuggestionChatInfoBean bean) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("contactItem",bean);
        mineAddFromContactFragment = MineAddFromContactFragment.newInstance(bundle);
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, mineAddFromContactFragment, "mineAddFromContactFragment")
                .addToBackStack("mineHelpFragment")
                .commit();
    }

    /**
     * desc：显示空视图
     */
    @Override
    public void showNoContactView() {
        llNoContact.setVisibility(View.VISIBLE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (presenter != null) {
            presenter.stop();
        }
    }

    @Override
    public void onAddClick(View view, int position,SuggestionChatInfoBean item) {
        if (getView() != null){
            jump2SendAddMesgFragment(item);
        }
    }
}

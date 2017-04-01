package com.cylan.jiafeigou.n.view.bind;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.cylan.jiafeigou.NewHomeActivity;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.bind.SetDeviceAliasContract;
import com.cylan.jiafeigou.n.mvp.impl.bind.SetDeviceAliasPresenterImpl;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.LoginButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SetDeviceAliasFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SetDeviceAliasFragment extends IBaseFragment<SetDeviceAliasContract.Presenter>
        implements SetDeviceAliasContract.View {


    @BindView(R.id.et_input_box)
    EditText etInputBox;
    @BindView(R.id.iv_clear_alias)
    ImageView ivClearAlias;
    @BindView(R.id.btn_bind_done)
    LoginButton btnBindDone;

    public SetDeviceAliasFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SetDeviceAliasFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SetDeviceAliasFragment newInstance(Bundle bundle) {
        SetDeviceAliasFragment fragment = new SetDeviceAliasFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        basePresenter = new SetDeviceAliasPresenterImpl(this,
                getArguments().getString(JConstant.KEY_DEVICE_ITEM_UUID));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_set_device_alias, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        String alias = bundle.getString(JConstant.KEY_BIND_DEVICE_ALIAS);
        if (!TextUtils.isEmpty(alias)) {
            etInputBox.setHint(alias);
            return;
        }
        String type = bundle.getString(JConstant.KEY_BIND_DEVICE);

        if (!TextUtils.isEmpty(type)) {
            etInputBox.setHint(type);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (btnBindDone != null) btnBindDone.cancelAnim();
    }

    @OnClick({R.id.iv_clear_alias, R.id.btn_bind_done})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_clear_alias:
                etInputBox.getText().clear();
                break;
            case R.id.btn_bind_done:
                if (basePresenter != null) {
                    CharSequence alias = TextUtils.isEmpty(etInputBox.getText())
                            ? etInputBox.getHint() : etInputBox.getText();
                    basePresenter.setupAlias(alias.toString());
                }
                btnBindDone.viewZoomSmall();
                break;
        }
    }

    @Override
    public void setPresenter(SetDeviceAliasContract.Presenter presenter) {
        this.basePresenter = presenter;
    }

    @Override
    public void setupAliasDone(int state) {
        if (state == 0) {
            ToastUtil.showPositiveToast(getString(R.string.SCENE_SAVED));
            Intent intent = new Intent(getActivity(), NewHomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else {
            ToastUtil.showNegativeToast(getString(R.string.Clear_Sdcard_tips5));
        }
    }
}

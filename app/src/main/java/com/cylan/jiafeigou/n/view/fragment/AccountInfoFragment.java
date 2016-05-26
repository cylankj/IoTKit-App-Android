package com.cylan.jiafeigou.n.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.setting.AccountInfoContract;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.utils.DswLog;
import com.cylan.utils.ListUtils;

import java.util.List;

/**
 * Created by hunt on 16-5-25.
 */

public class AccountInfoFragment extends BaseFragment implements AccountInfoContract.View {

    public static Fragment getInstance() {
        return new AccountInfoFragment();
    }

    private AccountInfoContract.Presenter presenter;
    private TitleActionInfo titleActionInfo;

    @Override
    protected void addSubContentView(ViewGroup viewGroup) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        viewGroup.addView(LayoutInflater.from(getContext())
                .inflate(R.layout.layout_fragment_account_info, null), params);
    }

    @Override
    public void setPresenter(AccountInfoContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.start();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        view.findViewById(R.id.rLayout_account_alias_item).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtils.addFragmentToActivity(getChildFragmentManager(),
                        FragmentAccountMyAccount.getInstance(), R.id.rLayout_base_container, 0);
            }
        });
    }

    private void initChildFragmentStack() {
        FragmentManager fragmentManager = getChildFragmentManager();
        if (fragmentManager == null) {
            DswLog.e("child fragmentManger is null");
            return;
        }
        fragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                FragmentManager fm = getChildFragmentManager();
                List<Fragment> fragmentList = fm.getFragments();
                final int count = ListUtils.getSize(fragmentList);
                if (count == 0) {
                    //其实这里,并不执行
                    DswLog.e("fragmentList is null");
                    return;
                }
                Fragment fragment = fragmentList.get(count - 1);
                if (fragment != null && fragment instanceof FragmentAccountMyAccount) {
                    TitleActionInfo info = ((FragmentAccountMyAccount) fragment).titleActionInfo;
                    titleActionInfo = info;
                    onTitleActionChange();
                }
            }
        });
    }

    private void initSelfFragmentStack() {
        final FragmentManager fm = getFragmentManager();
        if (fm != null) {
            fm.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
                @Override
                public void onBackStackChanged() {
                    FragmentManager childFm = getChildFragmentManager();
                    if (childFm != null && ListUtils.getSize(childFm.getFragments()) > 0) {
                        return;
                    }
                    FragmentManager selfFm = getActivity() != null
                            ? getActivity().getSupportFragmentManager() : null;
                    if (selfFm == null || ListUtils.isEmpty(selfFm.getFragments()))
                        return;
                    Log.d("hunt", "hunt change: ");
                    List<Fragment> list = selfFm.getFragments();
                    final int count = ListUtils.getSize(list);
                    if (count == 0)
                        return;
                    Fragment fragment = list.get(count - 1);
                    if (fragment != null && fragment instanceof AccountInfoFragment)
                        Log.d("hunt", "hunt self: " + count + fragment);
                }
            });
        }
    }

    @Override
    public void initBackStackChangeListener() {
        initChildFragmentStack();
        initSelfFragmentStack();
    }

    @Override
    public void onTitleActionChange() {
        if (titleActionInfo == null) {
            ToastUtil.showToast(getContext(), "null ");
        }
    }


    public static class FragmentAccountMyAccount extends Fragment {
        private static Fragment getInstance() {
            return new FragmentAccountMyAccount();
        }

        private TitleActionInfo titleActionInfo;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.layout_fragment_account_info, container, false);
            view.setBackgroundColor(getResources().getColor(R.color.back_color));
            return view;
        }

        @Override
        public void onResume() {
            super.onResume();
            initTitleInfo();
        }

        private void initTitleInfo() {
            titleActionInfo = new TitleActionInfo();
            titleActionInfo.backContent = "你好";
            titleActionInfo.doneContent = "完了";
            titleActionInfo.titleContent = "tianna";
            titleActionInfo.doneListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().onBackPressed();
                    ToastUtil.showToast(getContext(), "点到完成了");
                }
            };
            titleActionInfo.backListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().onBackPressed();
                    ToastUtil.showToast(getContext(), "点到返回了");
                }
            };
        }
    }

}

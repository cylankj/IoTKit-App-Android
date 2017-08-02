package com.cylan.jiafeigou.n.view.cam;


import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.injector.component.FragmentComponent;
import com.cylan.jiafeigou.base.wrapper.BaseFragment;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.mvp.contract.setting.AIRecognitionContact;
import com.cylan.jiafeigou.n.view.cam.item.AISelectionItem;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AIRecognitionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AIRecognitionFragment extends BaseFragment<AIRecognitionContact.Presenter> implements AIRecognitionContact.View {

    @BindView(R.id.rv_ai_selection)
    RecyclerView rv_AISelectionList;

    FastItemAdapter<AISelectionItem> itemAdapter;

    private AISelectionItem[] prefabObjs = new AISelectionItem[]{
            new AISelectionItem(1, R.drawable.icon_man_hl, R.drawable.icon_man, "人形"),//人形
            new AISelectionItem(2, R.drawable.icon_cat_hl, R.drawable.icon_cat, "猫"),//猫
            new AISelectionItem(3, R.drawable.icon_dog_hl, R.drawable.icon_dog, "狗"),//狗
            new AISelectionItem(4, R.drawable.icon_car_hl, R.drawable.icon_car, "车辆"),//车辆
            new AISelectionItem(-1),//占位符
            new AISelectionItem(-1)//占位符
    };

    public AIRecognitionFragment() {
        // Required empty public constructor
    }


    public static AIRecognitionFragment newInstance(String uuid) {
        AIRecognitionFragment fragment = new AIRecognitionFragment();
        Bundle args = new Bundle();
        args.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getContentViewID() {
        return R.layout.fragment_airecognition;
    }

    @Override
    protected void setFragmentComponent(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @Override
    public void onObjectDetectRsp(int[] objects) {
        if (objects != null) {
            for (int object : objects) {
                itemAdapter.select(itemAdapter.getPosition(object));
            }
        }
    }

    @Override
    public void onSetObjectDetectRsp(int code) {

    }

    public class SpaceItemDecoration extends RecyclerView.ItemDecoration {

        private int space;

        public SpaceItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            //不是第一个的格子都设一个左边和底部的间距
            outRect.left = space;
            outRect.bottom = space;
            //由于每行都只有3个，所以第一个都是3的倍数，把左边距设为0
            if (parent.getChildLayoutPosition(view) % 3 == 0) {
                outRect.left = 0;
            }
        }

    }

    @Override
    protected void initViewAndListener() {
        super.initViewAndListener();
        itemAdapter = new FastItemAdapter<>();
        itemAdapter.withSelectable(true);
        itemAdapter.withMultiSelect(true);
        itemAdapter.withAllowDeselection(true);
        itemAdapter.withSelectWithItemUpdate(true);
        itemAdapter.withUseIdDistributor(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
        rv_AISelectionList.setLayoutManager(gridLayoutManager);
        rv_AISelectionList.addItemDecoration(new SpaceItemDecoration(getResources().getDimensionPixelOffset(R.dimen.y2)));
        rv_AISelectionList.setAdapter(itemAdapter);
        itemAdapter.add(getAlignedList());
    }

    @Override
    public void onStart() {
        super.onStart();
        presenter.getObjectDetect();

    }

    private List<AISelectionItem> getAlignedList() {
        List<AISelectionItem> result = new ArrayList<>();
        int mod = prefabObjs.length % 3;
        int count = mod == 0 ? prefabObjs.length : prefabObjs.length + (3 - mod);
        for (int i = 0; i < count; i++) {
            if (i < prefabObjs.length) {
                result.add(prefabObjs[i]);
            } else {
                result.add(new AISelectionItem(-1));//占位符
            }
        }
        return result;
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }
}

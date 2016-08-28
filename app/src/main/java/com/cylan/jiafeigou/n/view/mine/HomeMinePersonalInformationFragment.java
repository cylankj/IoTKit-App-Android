package com.cylan.jiafeigou.n.view.mine;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
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

    //拉取出照相机时，产生的状态码
    private static final int ALBUM_OK = 0;
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

    @OnClick({R.id.iv_home_mine_personal_back, R.id.btn_home_mine_personal_information,
            R.id.lLayout_home_mine_personal_mailbox, R.id.rLayout_home_mine_personal_pic})
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
            case R.id.rLayout_home_mine_personal_pic:
                initDialog();
                break;
        }
    }


    /**
     * 点击相册的时候，弹窗提醒。判断是进入本地相册还是直接拍照
     */
    private void initDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("更换头像");
        builder.setNegativeButton("从相册中选择", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                choicePicFromAlbum();
            }
        });
        builder.setPositiveButton("拍照", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    /**
     * 从相册获取图片
     */
    private void choicePicFromAlbum() {
        // 来自相册
        Intent albumIntent = new Intent(Intent.ACTION_PICK, null);
        /**
         * 下面这句话，与其它方式写是一样的效果，如果：
         * intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
         * intent.setType(""image/*");设置数据类型
         * 要限制上传到服务器的图片类型时可以直接写如："image/jpeg 、 image/png等的类型"
         */
        albumIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(albumIntent, ALBUM_OK);
    }
}

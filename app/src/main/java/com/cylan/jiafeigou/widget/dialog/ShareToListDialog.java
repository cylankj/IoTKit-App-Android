package com.cylan.jiafeigou.widget.dialog;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.model.ResolveInfoEx;
import com.cylan.jiafeigou.support.superadapter.SuperAdapter;
import com.cylan.jiafeigou.support.superadapter.internal.SuperViewHolder;

import java.util.List;
import java.util.Locale;

/**
 * Created by hds on 17-6-8.
 */

public class ShareToListDialog extends BaseListDialog<ResolveInfoEx> {

    @Override
    protected ViewGroup getParentContainer(ViewGroup container) {
        return (ViewGroup) container.findViewById(R.id.layout_container);
    }

    @Override
    protected RecyclerView getRvWifiList(ViewGroup container) {
        return (RecyclerView) container.findViewById(R.id.rv_content_list);
    }

    @Override
    protected int getViewId() {
        return R.layout.layout_share_to_dialog;
    }

    @Override
    protected SuperAdapter<ResolveInfoEx> getAdapter() {
        return new SAdapter(getContext(), null, R.layout.layout_share_to_item);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        baseAdapter.setOnItemClickListener((itemView, viewType, position) -> {
            ResolveInfoEx infoEx = baseAdapter.getItem(position);
            final Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            final String app = getString(R.string.share_content) + getString(R.string.share_to_friends_link, getContext().getPackageName());
            intent.putExtra(Intent.EXTRA_TEXT, String.format(Locale.getDefault(), app, getContext().getPackageName()));
            intent.setComponent(new ComponentName(infoEx.getInfo().activityInfo.packageName, infoEx.getInfo().activityInfo.name));
            startActivity(intent);
            dismiss();
        });
    }

    private static class SAdapter extends SuperAdapter<ResolveInfoEx> {

        public SAdapter(Context context, List items, int layoutResId) {
            super(context, items, layoutResId);
        }

        @Override
        public void onBind(SuperViewHolder holder, int viewType,
                           int layoutPosition, ResolveInfoEx item) {
            PackageManager pm = getContext().getPackageManager();
            holder.setText(R.id.tv_share_info, item.getInfo().loadLabel(pm));
            holder.setImageDrawable(R.id.imv_share_info, item.getInfo().loadIcon(pm));
        }

    }

}

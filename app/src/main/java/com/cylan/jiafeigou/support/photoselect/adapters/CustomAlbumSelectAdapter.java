package com.cylan.jiafeigou.support.photoselect.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.support.photoselect.models.Album;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by Darshan on 4/14/2015.
 */
public class CustomAlbumSelectAdapter extends CustomGenericAdapter<Album> {
    public CustomAlbumSelectAdapter(Context context, ArrayList<Album> albums) {
        super(context, albums);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.grid_view_item_album_select, null);

            viewHolder = new ViewHolder();
            viewHolder.imageView = new WeakReference<ImageView>((ImageView) convertView.findViewById(R.id.image_view_album_image));
            viewHolder.textView = new WeakReference<TextView>((TextView) convertView.findViewById(R.id.text_view_album_name));

            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.imageView.get().getLayoutParams().width = size;
        viewHolder.imageView.get().getLayoutParams().height = size;

        viewHolder.textView.get().setText(arrayList.get(position).name);
        Glide.with(context)
                .load(arrayList.get(position).cover)
                .placeholder(R.drawable.image_placeholder).centerCrop().into(viewHolder.imageView.get());

        return convertView;
    }

    private static class ViewHolder {
        public WeakReference<ImageView> imageView;
        public WeakReference<TextView> textView;
    }
}

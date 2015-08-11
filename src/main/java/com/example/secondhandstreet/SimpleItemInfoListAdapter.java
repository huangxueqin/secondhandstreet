package com.example.secondhandstreet;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.secondhandstreet.Utils.LogUtil;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangxueqin on 15-4-27.
 */
public class SimpleItemInfoListAdapter extends BaseAdapter {
    Context mContext;
    List<ItemInfo> mData;

    public SimpleItemInfoListAdapter(Context context, List<ItemInfo> data) {
        mContext = context;
        if(data != null) {
            mData = new ArrayList<>(data);
        }
        else {
            mData = new ArrayList<>();
        }
    }

    public void addData(List<ItemInfo> data) {
        if(data != null) {
            LogUtil.logd("data.length = " + data.size());
            mData.addAll(data);
            this.notifyDataSetChanged();
        }
    }

    public void replaceData(List<ItemInfo> data) {
        if(data != null) {
            mData = new ArrayList<>(data);
            this.notifyDataSetChanged();
        }
    }

    public void removeAllData() {
        mData = new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.sh_list_item_list, parent, false);
            convertView.setTag(new ViewHolder(convertView));
        }
        holder = (ViewHolder) convertView.getTag();
        ItemInfo info = mData.get(position);
        holder.sdv.setImageURI(Uri.parse(info.frontCoverImgUrl));
        holder.info = info;
        holder.title.setText(info.name);
        holder.location.setText(info.location);
        holder.price.setText("￥ " + info.price);
        return convertView;
    }

    class ViewHolder {
        SimpleDraweeView sdv;
        TextView title, location, price;
        ItemInfo info;
        View rootView;
        public ViewHolder(View rootView) {
            sdv = (SimpleDraweeView) rootView.findViewById(R.id.item_img);
            title = (TextView) rootView.findViewById(R.id.title);
            location = (TextView) rootView.findViewById(R.id.location);
            price = (TextView) rootView.findViewById(R.id.price);
            this.rootView = rootView;
            this.rootView.setOnClickListener(mListItemOnClickListener);
        }
    }

    private View.OnClickListener mListItemOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ViewHolder holder = (ViewHolder) v.getTag();
            if(holder.info != null) {
                ItemInfo info = holder.info;
                Intent i = new Intent(mContext, ItemSpecificActivity.class);
                i.putExtra(ItemSpecificActivity.KEY_ITEM_INFO, info);
                mContext.startActivity(i);
            }
        }
    };
}

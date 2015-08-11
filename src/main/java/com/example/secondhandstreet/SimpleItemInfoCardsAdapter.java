package com.example.secondhandstreet;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.secondhandstreet.Utils.LogUtil;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangxueqin on 15-4-10.
 */
public class SimpleItemInfoCardsAdapter extends RecyclerView.Adapter<SimpleItemInfoCardsAdapter.ListItemHolder>{
    Context mContext;
    List<ItemInfo> itemInfos;

    public SimpleItemInfoCardsAdapter(Context context, List<ItemInfo> data) {
        mContext = context;
        if(data == null) {
            itemInfos = new ArrayList<>();
        }
        else {
            itemInfos = new ArrayList<>(data);
        };
    }

    public void addData(List<ItemInfo> data) {
        if(data != null) {
            itemInfos.addAll(data);
            for(ItemInfo info : itemInfos) {
                LogUtil.logd("-------->" + info.frontCoverImgUrl);
            }
        }
        this.notifyDataSetChanged();
    }

    public void replaceAllData(List<ItemInfo> data) {
        if(data != null) {
            itemInfos = new ArrayList<>(data);
            for(ItemInfo info : itemInfos) {
                LogUtil.logd("-------->" + info.frontCoverImgUrl);
            }
        }
        else {
            itemInfos = new ArrayList<>();
        }
        notifyDataSetChanged();
    }

    @Override
    public ListItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.sh_list_item_card, parent, false);
        v.setOnClickListener(mCardOnClickListener);
        return new ListItemHolder(v);
    }

    @Override
    public void onBindViewHolder(ListItemHolder holder, int position) {
        ItemInfo info = itemInfos.get(position);
        holder.rootView.setTag(itemInfos.get(position));

        holder.name.setText(info.name);
        holder.price.setText("￥ " + info.price);
        if(info.frontCoverImgUrl != null) {
            holder.contentImg.setImageURI(Uri.parse(info.frontCoverImgUrl));
        }
        holder.location.setText(info.location);
    }

    @Override
    public int getItemCount() {
        return itemInfos == null? 0 : itemInfos.size();
    }

    class ListItemHolder extends RecyclerView.ViewHolder{
        View rootView;
        SimpleDraweeView contentImg;
        TextView name;
        TextView location;
        TextView price;
        public ListItemHolder(View itemView) {
            super(itemView);
            rootView = itemView;
            contentImg = (SimpleDraweeView) itemView.findViewById(R.id.content_img);
            name = (TextView) itemView.findViewById(R.id.name);
            price = (TextView) itemView.findViewById(R.id.price);
            location = (TextView) itemView.findViewById(R.id.location);
        }
    }

    private View.OnClickListener mCardOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v.getTag() != null) {
                ItemInfo info = (ItemInfo) v.getTag();
                Intent i = new Intent(mContext, ItemSpecificActivity.class);
                i.putExtra(ItemSpecificActivity.KEY_ITEM_INFO, info);
                mContext.startActivity(i);
            }
        }
    };
}

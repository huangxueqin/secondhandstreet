package com.example.secondhandstreet.classify;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.secondhandstreet.CacheManager;
import com.example.secondhandstreet.Utils.NetworkUtils;
import com.example.secondhandstreet.R;
import com.example.secondhandstreet.Utils.LogUtil;
import com.facebook.drawee.view.SimpleDraweeView;

/**
 * Created by huangxueqin on 15-4-10.
 * 分类Tab所指示的页面
 */
public class CategoryFragment extends Fragment{
    public static final String KEY_CATEGORY_ID = "id";
    public static final String KEY_CATEGORY_NAME = "name";

    public static String[][] categoryInfos = {
            {"自行车", "代步车"},
            {"教材", "考研", "课外书"},
            {"租房", "服装", "道具"},
            {"联想", "戴尔", "Mac"},
            {"篮球", "足球", "球拍"},
            {"电扇", "台灯", "饮水机"},
            {"三星", "小米", "iPhone"},
            {},
            {"打印机"},
            {},
            {"乐器", "日常", "会员卡"},
            {"上衣", "裤子", "背包"}
    };
    public static String[] categoryNames = {
            "校园代步",
            "图书教材",
            "租赁",
            "电脑",
            "运动健身",
            "电器",
            "手机",
            "化妆品",
            "办公器材",
            "鞋子",
            "生活娱乐",
            "衣服伞帽"
    };

    public static Uri iconIds[] = {
            Uri.parse("res://com.example.secondhandstreet/" + R.drawable.category_bike),
            Uri.parse("res://com.example.secondhandstreet/" + R.drawable.category_book),
            Uri.parse("res://com.example.secondhandstreet/" + R.drawable.category_building),
            Uri.parse("res://com.example.secondhandstreet/" + R.drawable.category_computer),
            Uri.parse("res://com.example.secondhandstreet/" + R.drawable.category_football),
            Uri.parse("res://com.example.secondhandstreet/" + R.drawable.category_fridge),
            Uri.parse("res://com.example.secondhandstreet/" + R.drawable.category_mobile),
            Uri.parse("res://com.example.secondhandstreet/" + R.drawable.category_perfume),
            Uri.parse("res://com.example.secondhandstreet/" + R.drawable.category_printer),
            Uri.parse("res://com.example.secondhandstreet/" + R.drawable.category_shoes),
            Uri.parse("res://com.example.secondhandstreet/" + R.drawable.category_trolley),
            Uri.parse("res://com.example.secondhandstreet/" + R.drawable.category_tshirt)
    };

    RecyclerView categoryList;
    SimpleCategoryListAdapter mAdapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_category, container, false);
        categoryList = (RecyclerView) rootView.findViewById(R.id.category_list);
        categoryList.addItemDecoration(new SimpleItemDecoration(CacheManager.getApplicatioinContext()));
        categoryList.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        mAdapter = new SimpleCategoryListAdapter(categoryNames, categoryInfos, iconIds);
        categoryList.setAdapter(mAdapter);
        return rootView;
    }

    private class SimpleCategoryListAdapter extends RecyclerView.Adapter<SimpleCategoryListAdapter.CategoryViewHolder>{
        String[] names;
        String[][] infos;
        Uri[] imageUris;
        public SimpleCategoryListAdapter(String[] names, String[][] infos, Uri[] uris) {
            this.names = names;
            this.infos = infos;
            this.imageUris = uris;
        }

        @Override
        public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_card, parent, false);
            itemView.setOnClickListener(mCardOnClickListener);
            return new CategoryViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(CategoryViewHolder holder, int position) {
            holder.name.setText(names[position]);
            String info = "";
            for(String s : infos[position]) {
                info += s + " ";
            }
            holder.info.setText(info);
            holder.icon.setImageURI(imageUris[position]);
            holder.rootView.setTag(position);
        }

        @Override
        public int getItemCount() {
            return Math.min(infos.length, Math.min(names.length, imageUris.length));
        }

        public class CategoryViewHolder extends RecyclerView.ViewHolder {
            View rootView;
            SimpleDraweeView icon;
            TextView name;
            TextView info;
            public CategoryViewHolder(View itemView) {
                super(itemView);
                rootView = itemView;
                icon = (SimpleDraweeView) itemView.findViewById(R.id.category_icon);
                name = (TextView) itemView.findViewById(R.id.category_name);
                info = (TextView) itemView.findViewById(R.id.category_info);
            }
        }

        // 处理点击分类卡片事件，启动一个新的Activity展示分类商品列表
        View.OnClickListener mCardOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getTag() != null) {
                    LogUtil.logd("request category running");
                    int categoryId = (Integer) v.getTag();
                    Intent i = new Intent(CacheManager.getApplicatioinContext(), CategoryListActivity.class);
                    i.putExtra(KEY_CATEGORY_ID, categoryId+1);
                    i.putExtra(KEY_CATEGORY_NAME, names[categoryId]);
                    startActivity(i);
                }
            }
        };

    }

    private class SimpleItemDecoration extends RecyclerView.ItemDecoration {
        int spacing = 16;
        public SimpleItemDecoration(Context context) {
            spacing = context.getResources().getDimensionPixelSize(R.dimen.card_spacing);
        }
        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            StaggeredGridLayoutManager manager = (StaggeredGridLayoutManager)parent.getLayoutManager();
            int position = manager.getPosition(view);
            int halfSpacing = spacing / 2;
            outRect.set(halfSpacing, halfSpacing, halfSpacing, halfSpacing);
            if((position & 0x1) != 0) {
                outRect.right += halfSpacing;
            }
            else {
                outRect.left += halfSpacing;
            }
            if(position == 0 || position == 1) {
                outRect.top += halfSpacing;
            }
            int count = manager.getItemCount();
            if(position == count-1) {
                outRect.bottom += halfSpacing;
            }
            if((count & 0x1) == 0) {
                if(position == count-2) {
                    outRect.bottom += halfSpacing;
                }
            }
        }
    }
}

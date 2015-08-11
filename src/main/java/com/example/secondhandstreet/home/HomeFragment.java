package com.example.secondhandstreet.home;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.secondhandstreet.R;

/**
 * Created by huangxueqin on 15-4-10.
 * 首页Tab所指示的页面，包含其内部包含两个新的Fragment，
 * 一个展示最新商品，一个展示推荐商品
 */
public class HomeFragment extends Fragment{
    SlidingTabLayout mTabIndicator;
    ViewPager mPages;
    FragmentPagerAdapter mAdapter;

    private static final int DEFAULT_TAB_INDEX = 0;
    private int mCurrentPage = -1;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new HomeFragmentPagerAdapter(getActivity(), getFragmentManager());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        mTabIndicator = (SlidingTabLayout) rootView.findViewById(R.id.tab_indicator);
        mPages = (ViewPager) rootView.findViewById(R.id.home_pagers);
        mPages.setAdapter(mAdapter);
        mPages.setCurrentItem(mCurrentPage < 0 ? DEFAULT_TAB_INDEX : mCurrentPage);
        mTabIndicator.setViewPager(mPages);
        return rootView;
    }

    class HomeFragmentPagerAdapter extends FragmentPagerAdapter {
        private static final int COUNT = 2;
        String[] mTitles = new String[COUNT];
        Fragment[] mFragments = new Fragment[COUNT];

        public HomeFragmentPagerAdapter(Context context, FragmentManager fm) {
            super(fm);
            Resources res = context.getResources();
            mTitles[0] = res.getString(R.string.homeTitleRecommendation);
            mTitles[1] = res.getString(R.string.homeTitleNewest);
            mFragments[0] = new RecommendationFragment();
            mFragments[1] = new NewestFragment();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles[position];
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments[position];
        }

        @Override
        public int getCount() {
            return COUNT;
        }
    }
}

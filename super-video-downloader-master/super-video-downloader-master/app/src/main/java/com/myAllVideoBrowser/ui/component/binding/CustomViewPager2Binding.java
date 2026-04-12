package com.myAllVideoBrowser.ui.component.binding;

import androidx.databinding.BindingAdapter;
import com.myAllVideoBrowser.ui.main.home.browser.BrowserFragment;
import com.myAllVideoBrowser.ui.main.home.browser.CustomViewPager2;
import com.myAllVideoBrowser.ui.main.home.browser.webTab.WebTab;
import java.util.Collections;
import java.util.List;

public final class CustomViewPager2Binding {
    private CustomViewPager2Binding() {}

    @BindingAdapter("items")
    public static void setWebItems(CustomViewPager2 view, List<WebTab> currentItems) {
        if (view.getAdapter() instanceof BrowserFragment.TabsFragmentStateAdapter adapter) {
            adapter.setRoutes(currentItems != null ? currentItems : Collections.emptyList());
        }
    }

    @BindingAdapter("offScreenPageLimit")
    public static void setOffScreenPageLimit(CustomViewPager2 view, Integer pageLimit) {
        view.setOffscreenPageLimit(pageLimit != null ? pageLimit : 1);
    }

    @BindingAdapter("currentItem")
    public static void setCurrentItem(CustomViewPager2 view, Integer currentItemPosition) {
        if (currentItemPosition != null) {
            view.setCurrentItem(currentItemPosition);
        }
    }
}

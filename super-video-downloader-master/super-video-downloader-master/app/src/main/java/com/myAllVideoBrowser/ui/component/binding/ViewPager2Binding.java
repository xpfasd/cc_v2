package com.myAllVideoBrowser.ui.component.binding;

import androidx.databinding.BindingAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.myAllVideoBrowser.ui.main.home.browser.BrowserFragment;
import com.myAllVideoBrowser.ui.main.home.browser.webTab.WebTab;
import java.util.Collections;
import java.util.List;

public final class ViewPager2Binding {
    private ViewPager2Binding() {}

    @BindingAdapter("items")
    public static void setWebItems(ViewPager2 view, List<WebTab> currentItems) {
        if (view.getAdapter() instanceof BrowserFragment.TabsFragmentStateAdapter adapter) {
            adapter.setRoutes(currentItems != null ? currentItems : Collections.emptyList());
        }
    }

    @BindingAdapter("offScreenPageLimit")
    public static void setOffScreenPageLimit(ViewPager2 view, Integer pageLimit) {
        view.setOffscreenPageLimit(
            pageLimit != null ? pageLimit : ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
        );
    }
}

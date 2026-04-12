package com.myAllVideoBrowser.ui.component.binding;

import androidx.databinding.BindingAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.myAllVideoBrowser.data.local.model.LocalVideo;
import com.myAllVideoBrowser.data.local.model.Proxy;
import com.myAllVideoBrowser.data.local.room.entity.HistoryItem;
import com.myAllVideoBrowser.data.local.room.entity.PageInfo;
import com.myAllVideoBrowser.data.local.room.entity.ProgressInfo;
import com.myAllVideoBrowser.data.local.room.entity.VideoInfo;
import com.myAllVideoBrowser.ui.component.adapter.BookmarksAdapter;
import com.myAllVideoBrowser.ui.component.adapter.HistoryAdapter;
import com.myAllVideoBrowser.ui.component.adapter.HistorySearchAdapter;
import com.myAllVideoBrowser.ui.component.adapter.ProgressAdapter;
import com.myAllVideoBrowser.ui.component.adapter.ProxiesAdapter;
import com.myAllVideoBrowser.ui.component.adapter.TopPageAdapter;
import com.myAllVideoBrowser.ui.component.adapter.VideoAdapter;
import com.myAllVideoBrowser.ui.component.adapter.VideoInfoAdapter;
import com.myAllVideoBrowser.ui.component.adapter.WebTabsAdapter;
import com.myAllVideoBrowser.ui.main.home.browser.webTab.WebTab;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class RecyclerViewBinding {
    private RecyclerViewBinding() {}

    @BindingAdapter("items")
    public static void setWebTabs(RecyclerView view, List<WebTab> tabs) {
        if (view.getAdapter() instanceof WebTabsAdapter adapter) {
            adapter.setData(tabs != null ? tabs : Collections.emptyList());
        }
    }

    @BindingAdapter("items")
    public static void setProgressInfos(RecyclerView view, List<ProgressInfo> items) {
        if (view.getAdapter() instanceof ProgressAdapter adapter) {
            adapter.setData(items != null ? items : Collections.emptyList());
        }
    }

    @BindingAdapter("items")
    public static void setProxiesList(RecyclerView view, List<Proxy> items) {
        if (view.getAdapter() instanceof ProxiesAdapter adapter) {
            adapter.setData(items != null ? items : Collections.emptyList());
        }
    }

    @BindingAdapter("items")
    public static void setVideoInfos(RecyclerView view, List<LocalVideo> items) {
        if (view.getAdapter() instanceof VideoAdapter adapter) {
            adapter.setData(items != null ? items : Collections.emptyList());
        }
    }

    @BindingAdapter("items")
    public static void setHistoryItems(RecyclerView view, List<HistoryItem> items) {
        List<HistoryItem> safeItems = items != null ? items : Collections.emptyList();
        if (view.getAdapter() instanceof HistoryAdapter adapter) {
            adapter.setData(safeItems);
        }
        if (view.getAdapter() instanceof HistorySearchAdapter adapter) {
            adapter.setData(safeItems);
        }
    }

    @BindingAdapter("items")
    public static void setDetectedVideoInfos(RecyclerView view, List<VideoInfo> items) {
        if (view.getAdapter() instanceof VideoInfoAdapter adapter) {
            adapter.setData(items != null ? items : Collections.emptyList());
        }
    }

    @BindingAdapter("items")
    public static void setDetectedVideoInfosSet(RecyclerView view, Set<VideoInfo> items) {
        if (view.getAdapter() instanceof VideoInfoAdapter adapter) {
            adapter.setData(items != null ? new ArrayList<>(items) : Collections.emptyList());
        }
    }

    @BindingAdapter("items")
    public static void setBookmarks(RecyclerView view, List<PageInfo> items) {
        if (view.getAdapter() instanceof BookmarksAdapter adapter) {
            adapter.setData(items != null ? new ArrayList<>(items) : new ArrayList<>());
        }
    }
}

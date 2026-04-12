package com.myAllVideoBrowser.ui.component.binding;

import android.widget.GridView;
import androidx.databinding.BindingAdapter;
import com.myAllVideoBrowser.data.local.room.entity.PageInfo;
import com.myAllVideoBrowser.ui.component.adapter.TopPageAdapter;
import java.util.Collections;
import java.util.List;

public final class GridViewBinding {
    private GridViewBinding() {}

    @BindingAdapter("items")
    public static void setTopPages(GridView view, List<PageInfo> items) {
        if (view.getAdapter() instanceof TopPageAdapter adapter) {
            adapter.setData(items != null ? items : Collections.emptyList());
        }
    }
}

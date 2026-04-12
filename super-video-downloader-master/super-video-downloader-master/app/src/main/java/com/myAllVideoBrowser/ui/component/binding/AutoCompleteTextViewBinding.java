package com.myAllVideoBrowser.ui.component.binding;

import android.widget.AutoCompleteTextView;
import androidx.databinding.BindingAdapter;
import com.myAllVideoBrowser.data.local.model.Suggestion;
import com.myAllVideoBrowser.data.local.room.entity.HistoryItem;
import com.myAllVideoBrowser.ui.component.adapter.SuggestionAdapter;
import com.myAllVideoBrowser.ui.component.adapter.TabSuggestionAdapter;
import java.util.Collections;
import java.util.List;

public final class AutoCompleteTextViewBinding {
    private AutoCompleteTextViewBinding() {}

    @BindingAdapter("items")
    public static void setSuggestions(AutoCompleteTextView view, List<Suggestion> items) {
        if (view.getAdapter() instanceof SuggestionAdapter adapter) {
            adapter.setData(items != null ? items : Collections.emptyList());
        }
    }

    @BindingAdapter("items")
    public static void setTabSuggestions(AutoCompleteTextView view, List<HistoryItem> items) {
        if (view.getAdapter() instanceof TabSuggestionAdapter adapter) {
            adapter.setData(items != null ? items : Collections.emptyList());
        }
    }
}

package com.myAllVideoBrowser.ui.component.binding;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;
import androidx.databinding.BindingAdapter;
import com.bumptech.glide.Glide;

public final class ImageBinding {
    private ImageBinding() {}

    @BindingAdapter("imageUrl")
    public static void loadImage(ImageView view, String url) {
        if (url == null) {
            view.setImageDrawable(null);
            return;
        }
        Glide.with(view.getContext()).load(url).into(view);
    }

    @BindingAdapter("bitmap")
    public static void setBitmap(ImageView view, Bitmap bitmap) {
        view.setImageBitmap(bitmap);
    }

    @BindingAdapter("android:src")
    public static void setImageUri(ImageView view, String imageUri) {
        if (imageUri == null) {
            view.setImageURI(null);
        } else {
            view.setImageURI(Uri.parse(imageUri));
        }
    }

    @BindingAdapter("android:src")
    public static void setImageUri(ImageView view, Uri imageUri) {
        view.setImageURI(imageUri);
    }

    @BindingAdapter("android:src")
    public static void setImageDrawable(ImageView view, Drawable drawable) {
        view.setImageDrawable(drawable);
    }

    @BindingAdapter("android:src")
    public static void setImageResource(ImageView view, Integer resource) {
        if (resource != null) {
            view.setImageResource(resource);
        } else {
            view.setImageDrawable(null);
        }
    }
}

package com.keggemeyer.rallyamerica.ui.loaders;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

public class ImageDialogAdapter extends PagerAdapter {
    /* ----- VARIABLES ----- */
    private final Context context;
    private final String[] imgLinks;
    private final View.OnClickListener onClick;

    /* ----- CONSTRUCTORS ----- */
    public ImageDialogAdapter(Context context, String[] imgLinks, View.OnClickListener onClick) {
        this.context = context;
        this.imgLinks = imgLinks;
        this.onClick = onClick;
    }

    /* ----- INHERITED METHODS ----- */
    // PagerAdapter
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(com.keggemeyer.rallyamerica.R.layout.generic_image_dialog_image, container, false);
        ImageView imageView = (ImageView) view.findViewById(com.keggemeyer.rallyamerica.R.id.generic_image_dialog_image);

        ImageLoader.getInstance().displayImage(imgLinks[position], new ImageViewAware(imageView), null, null, null);

        view.setOnClickListener(onClick);
        ((ViewPager) container).addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((View) object);
    }

    @Override
    public int getCount() {
        return imgLinks.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == (View) object;
    }
}

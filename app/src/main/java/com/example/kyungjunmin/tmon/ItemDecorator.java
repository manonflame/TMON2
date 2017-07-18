package com.example.kyungjunmin.tmon;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.DrawableUtils;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by KyungJunMin on 2017. 7. 17..
 */

public class ItemDecorator extends RecyclerView.ItemDecoration {

    private final int padding;

    public ItemDecorator(){
        this.padding = 20;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state){
        if(parent.getChildAdapterPosition(view) != parent.getAdapter().getItemCount() - 1){
            outRect.bottom = padding;
            outRect.top = padding;
            outRect.left = padding;
            outRect.right = padding;
        }
    }

}

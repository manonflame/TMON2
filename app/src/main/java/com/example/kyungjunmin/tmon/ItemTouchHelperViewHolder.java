package com.example.kyungjunmin.tmon;

/**
 * Created by Valentine on 7/21/2015.
 */
public interface ItemTouchHelperViewHolder {
    /**
      * Implementations should update the item view to indicate it's active state.
     */
    void onItemSelected();


    /**
     * state should be cleared.
     */
    void onItemClear();
}

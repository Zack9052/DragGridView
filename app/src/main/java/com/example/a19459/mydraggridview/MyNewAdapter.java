package com.example.a19459.mydraggridview;

import android.content.Context;
import android.widget.TextView;

import java.util.List;

/**
 * Created by 19459 on 2016/8/31.
 */

public class MyNewAdapter extends DragBaseAdapter<String> {

    public MyNewAdapter(Context context, List<String> list) {
        super(context, list);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.layout_item_grid;
    }

    @Override
    protected void initView(ViewHolder holder) {
        holder.addView(R.id.item_text);
    }

    @Override
    protected void setViewValue(ViewHolder holder, int position) {
        ((TextView)holder.getView(R.id.item_text)).setText(getItem(position));
    }


}

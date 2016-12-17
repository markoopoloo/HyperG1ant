package com.markopaivarinta.hyperg1ant;

import android.app.Activity;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.text.Html;
import android.widget.TextView;
import android.widget.ImageView;


import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class GridViewAdapter extends ArrayAdapter<Item>{

    private Context mContext;
    private int layoutResourceId;
    private ArrayList<Item> mGridData=new ArrayList<Item>();

    public GridViewAdapter(Context mContext, int layoutResourceId, ArrayList<Item> mGridData) {
        super(mContext, layoutResourceId, mGridData);
        this.layoutResourceId = layoutResourceId;
        this.mContext = mContext;
        this.mGridData = mGridData;
    }

    /**
     * Updates grid data and refresh grid items.
     *T
     * @param mGridData
     */
    public void setGridData(ArrayList<Item>mGridData){
        this.mGridData = mGridData;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent ){
        View row = convertView;
        ViewHolder holder;

        if (row == null){
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            row = inflater.inflate(layoutResourceId,parent,false);
            holder = new ViewHolder();
            holder.titleTextView= (TextView) row.findViewById(R.id.grid_item_title);
            holder.imageView= (ImageView) row.findViewById(R.id.grid_item_image);
            holder.priceTextView = (TextView) row.findViewById(R.id.grid_item_price);
            row.setTag(holder);
        }else{
            holder = (ViewHolder) row.getTag();
        }
        Item item = mGridData.get(position);
        holder.titleTextView.setText(Html.fromHtml(item.getTitle()));
        holder.priceTextView.setText(Html.fromHtml("&#8364;"+String.valueOf(item.getPrice())));

        Picasso.with(mContext).load(item.getImage()).resize(165,200).into(holder.imageView);
        return row;
    }

    static class ViewHolder{
        TextView titleTextView;
        ImageView imageView;
        TextView priceTextView;

    }
}

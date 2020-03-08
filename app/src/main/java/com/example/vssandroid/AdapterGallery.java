package com.example.vssandroid;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class AdapterGallery extends RecyclerView.Adapter<AdapterGallery.ViewHolder> {
    private List<String> imageUrls;
    private Context context;
    private ImageClickListener imageClickListener;

    public AdapterGallery(Context context, List<String> imageUrls, ImageClickListener imageClickListener) {
        this.context = context;
        this.imageUrls = imageUrls;
        this.imageClickListener = imageClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.high_res_image_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        Glide.with(context).load(imageUrls.get(position)).into(holder.imgView);

        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageClickListener.onImageClick(imageUrls.get(position));
            }
        });

        holder.linearLayout.setFocusableInTouchMode(true);
        holder.linearLayout.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    holder.imgView.setBackgroundColor(Color.WHITE);
                    imageClickListener.onImageClick(imageUrls.get(position));
                }
                else {
                    holder.imgView.setBackgroundColor(ContextCompat.getColor(context, R.color.screenBackground));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgView;
        private LinearLayout linearLayout;

        public ViewHolder(View view) {
            super(view);
            linearLayout = view.findViewById(R.id.parent);
            imgView = view.findViewById(R.id.high_res_image);
        }
    }
}

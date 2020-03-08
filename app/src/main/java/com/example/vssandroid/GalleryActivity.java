package com.example.vssandroid;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.example.vssandroid.AdapterGallery;
import com.example.vssandroid.ImageClickListener;
import com.example.vssandroid.R;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class GalleryActivity extends AppCompatActivity implements ImageClickListener {
    private ImageView imagePreview;
    private ImageView mSave, mShare, mBack;
    private String previewUrl;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.imageRecyclerView);
        imagePreview = (ImageView) findViewById(R.id.imagePreview);
        mSave = (ImageView) findViewById(R.id.save);
        mShare = (ImageView) findViewById(R.id.share);
        mBack = (ImageView) findViewById(R.id.back);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        Intent intent = getIntent();
        ArrayList<String> urlList = intent.getStringArrayListExtra("urlList");

        AdapterGallery adapterGallery = new AdapterGallery(getApplicationContext(), urlList, this);
        recyclerView.setAdapter(adapterGallery);

        Glide.with(this).load(urlList.get(0)).into(imagePreview);

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareImage();
            }
        });

        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImage(previewUrl);
            }
        });
    }

    @Override
    public void onImageClick(String url) {
        previewUrl = url;
        Glide.with(this).load(url).into(imagePreview);
    }


    private void shareImage() {
        Bitmap bmp = ((BitmapDrawable)imagePreview.getDrawable()).getBitmap();
        try {
            File file = new File(GalleryActivity.this.getExternalCacheDir(), "image.png");
            FileOutputStream fout = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fout);
            fout.flush();
            fout.close();
            file.setReadable(true, false);

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            intent.setType("image/png");
            startActivity(Intent.createChooser(intent, "Share image via"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveImage(String url) {
        ActivityCompat.requestPermissions(GalleryActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                1);

        Glide.with(getApplicationContext())
                .asBitmap()
                .load(url)
                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .into(new SimpleTarget<Bitmap>(){
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        MediaStore.Images.Media.insertImage(getContentResolver(), resource, "image.png" , "image");
                    }
                });

        Toast.makeText(getApplicationContext(), "Downloaded image", Toast.LENGTH_SHORT).show();
    }
}
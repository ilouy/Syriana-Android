package com.fadybasem.syriana;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class GridViewAdapter extends ArrayAdapter<String> {
    GridViewType type;
    ArrayList<String> namesList;
    ArrayList<HashMap<String, String>> metadataList;

    public GridViewAdapter(@NonNull Context context, int resource, @NonNull ArrayList<String> names, GridViewType type, @Nullable ArrayList<HashMap<String, String>> metadataList) {
        super(context, resource, names);

        this.type = type;
        this.namesList = names;
        this.metadataList = metadataList;
        Log.d("GridViewAdapter", type.name());
    }

    @Override
    public int getCount() {
        return namesList.size();
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.item_gridview, parent, false);
        }

        ImageView imageView = view.findViewById(R.id.gridViewItem_imageView);
        TextView textView = view.findViewById(R.id.gridViewItem_textView);

        switch (type) {
            case Instructor:
                imageView.setImageResource(R.drawable.folder_icon);
                textView.setText("Dr. " + namesList.get(position));
                break;
            case Course:
                imageView.setImageResource(R.drawable.book_icon);
                textView.setText(namesList.get(position));
                break;
            case Video:
                AsyncTask.execute(() -> {
                    try {
                        URL url = new URL(metadataList.get(position).get("thumbnailURL"));
                        Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());

                        new Handler(Looper.getMainLooper()).post(() -> imageView.setImageBitmap(image));
                    } catch(IOException e) {
                        imageView.setImageResource(R.drawable.video_not_available_icon);
                    }
                });

                textView.setText(namesList.get(position).replace(".mp4", ""));
                if (metadataList.get(position).get("isPreview") != null && Objects.equals(metadataList.get(position).get("isPreview"), "true"))
                    textView.setText(textView.getText().toString() + "\nPreview Available");
                break;
        }
        return view;
    }
}

enum GridViewType {
    Instructor, Course, Video
}
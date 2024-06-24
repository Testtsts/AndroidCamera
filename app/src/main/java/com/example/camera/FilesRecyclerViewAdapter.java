package com.example.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import android.util.Base64;

public class FilesRecyclerViewAdapter extends RecyclerView.Adapter<FilesRecyclerViewAdapter.Holder> {
    private ArrayList<FileData> arrayListFileData = new ArrayList<>();
    private View.OnClickListener listener = null;

    public FilesRecyclerViewAdapter(ArrayList<FileData> arrayListFileData) {
        this.arrayListFileData = arrayListFileData;
    }

    public FilesRecyclerViewAdapter() {
    }

    public ArrayList<FileData> getArrayListFileData() {
        return arrayListFileData;
    }

    public void setArrayListFileData(ArrayList<FileData> arrayListFileData) {
        this.arrayListFileData.clear();
        this.arrayListFileData = arrayListFileData;
        this.notifyDataSetChanged();
    }

    public View.OnClickListener getListener() {
        return listener;
    }

    public void setListener(View.OnClickListener listener) {
        this.listener = listener;
    }

    public static class Holder extends RecyclerView.ViewHolder {

        public ImageView getImageView() {
            return imageView;
        }

        //        private View view;
        private ImageView imageView ;
        private TextView breedText;

        public Holder(@NonNull View itemView) {
            super(itemView);
//            this.image = itemView;
            this.imageView = itemView.findViewById(R.id.imageView);
            this.breedText= itemView.findViewById(R.id.breedText);

        }

        public TextView getBreedText() {
            return breedText;
        }
    }


    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        return new Holder(layoutInflater.inflate(R.layout.item_file, parent, false));
    }
    @Override
    public void onBindViewHolder(Holder viewHolder, int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element

        viewHolder.getImageView().setImageBitmap(decodeBase64(arrayListFileData.get(position).getfileData()));
        viewHolder.getBreedText().setText(arrayListFileData.get(position).getFileName());
    }

    @Override
    public int getItemCount() {
        return arrayListFileData.size();
    }

    public Bitmap decodeBase64(String base64){
        byte[] decodedString = Base64.decode(base64,0);
        return (BitmapFactory.decodeByteArray(decodedString,0,decodedString.length));
    }


}

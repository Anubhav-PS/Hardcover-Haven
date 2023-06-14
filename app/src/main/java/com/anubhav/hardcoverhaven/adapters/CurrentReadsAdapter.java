package com.anubhav.hardcoverhaven.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.anubhav.hardcoverhaven.R;
import com.anubhav.hardcoverhaven.interfaces.iOnFeatureItemClicked;
import com.anubhav.hardcoverhaven.models.Books;
import com.google.android.material.textview.MaterialTextView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CurrentReadsAdapter extends RecyclerView.Adapter<CurrentReadsAdapter.CurrentReadsHolder> {

    private ArrayList<Books> books;
    private Context context;
    private iOnFeatureItemClicked onFeatureMenuClicked;

    public CurrentReadsAdapter(ArrayList<Books> books, Context context, iOnFeatureItemClicked onFeatureMenuClicked) {
        this.books = books;
        this.context = context;
        this.onFeatureMenuClicked = onFeatureMenuClicked;
    }

    @NonNull
    @Override
    public CurrentReadsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_front_display_books, parent, false);
        return new CurrentReadsHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CurrentReadsHolder holder, int position) {
        Books books = this.books.get(position);
        holder.title.setText(books.getTitle());
        holder.author.setText(books.getAuthor());
        Picasso.get().load(books.getImgUrl()).into(holder.img);
        holder.cardView.setOnClickListener(v -> onFeatureMenuClicked.onCurrentReadsClicked(books));
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    static class CurrentReadsHolder extends RecyclerView.ViewHolder {

        private CardView cardView;
        private ImageView img;
        private MaterialTextView title;
        private MaterialTextView author;


        public CurrentReadsHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cellFrontDisplayBookCard);
            img = itemView.findViewById(R.id.cellFrontDisplayBookImage);
            title = itemView.findViewById(R.id.cellFrontDisplayBookTitleTxt);
            author = itemView.findViewById(R.id.cellFrontDisplayAuthorTxt);

        }
    }
}

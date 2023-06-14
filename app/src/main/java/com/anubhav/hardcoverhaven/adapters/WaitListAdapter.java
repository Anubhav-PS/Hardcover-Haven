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
import com.anubhav.hardcoverhaven.models.Stock;
import com.anubhav.hardcoverhaven.models.WaitList;
import com.google.android.material.textview.MaterialTextView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class WaitListAdapter extends RecyclerView.Adapter<WaitListAdapter.WaitListHolder> {

    private ArrayList<WaitList> books;
    private Context context;
    private iOnFeatureItemClicked onFeatureMenuClicked;

    public WaitListAdapter(ArrayList<WaitList> books, Context context, iOnFeatureItemClicked onFeatureMenuClicked) {
        this.books = books;
        this.context = context;
        this.onFeatureMenuClicked = onFeatureMenuClicked;
    }

    @NonNull
    @Override
    public WaitListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_wait_list, parent, false);
        return new WaitListHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WaitListHolder holder, int position) {
        WaitList waitList = this.books.get(position);
        Books books = waitList.getBooks();
        Stock stock = waitList.getStock();
        holder.title.setText(books.getTitle());
        holder.author.setText(books.getAuthor());
        String stockMessage = "";
        if (stock.getAvailable() == 0) {
            stockMessage = "Out of stock";
        } else {
            stockMessage = "In stock";
        }
        holder.stock.setText(stockMessage);
        Picasso.get().load(books.getImgUrl()).into(holder.img);
        holder.cardView.setOnClickListener(v -> onFeatureMenuClicked.onWaitListClicked(books.getBookDocID()));
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    public static class WaitListHolder extends RecyclerView.ViewHolder {

        private CardView cardView;
        private ImageView img;
        private MaterialTextView title;
        private MaterialTextView author;
        private MaterialTextView stock;

        public WaitListHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cellWaitListCardView);
            img = itemView.findViewById(R.id.cellWaitListBookCoverImg);
            title = itemView.findViewById(R.id.cellWaitListBookTitle);
            author = itemView.findViewById(R.id.cellWaitListBookAuthor);
            stock = itemView.findViewById(R.id.cellWaitListBookStock);

        }
    }
}

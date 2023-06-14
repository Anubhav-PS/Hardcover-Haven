package com.anubhav.hardcoverhaven.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anubhav.hardcoverhaven.R;
import com.anubhav.hardcoverhaven.interfaces.iOnSearchResultClicked;
import com.anubhav.hardcoverhaven.models.Books;
import com.google.android.material.textview.MaterialTextView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class DisplayBookListAdapter extends RecyclerView.Adapter<DisplayBookListAdapter.BookHolder> {

    private ArrayList<Books> books;
    private Context context;
    private iOnSearchResultClicked onSearchResultClicked;


    public DisplayBookListAdapter(ArrayList<Books> books, Context context,iOnSearchResultClicked onSearchResultClicked) {
        this.books = books;
        this.context = context;
        this.onSearchResultClicked = onSearchResultClicked;
    }

    @NonNull
    @Override
    public BookHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_list_book, parent, false);
        return new BookHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookHolder holder, int position) {
        Books books = this.books.get(position);
        holder.title.setText(books.getTitle());
        holder.author.setText(books.getAuthor());
        holder.publisher.setText(books.getPublisher());
        final String edition = books.getEdition() + " Edition";
        holder.edition.setText(edition);
        Picasso.get().load(books.getImgUrl()).into((holder).img);
        holder.linearLayout.setOnClickListener(v -> onSearchResultClicked.onSearchResultClicked(books.getBookDocID()));

    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    static class BookHolder extends RecyclerView.ViewHolder {

        private LinearLayout linearLayout;
        private ImageView img;
        private MaterialTextView title;
        private MaterialTextView author;
        private MaterialTextView edition;
        private MaterialTextView publisher;


        public BookHolder(@NonNull View itemView) {
            super(itemView);

            linearLayout = itemView.findViewById(R.id.cellListBookLinearLayout);
            img = itemView.findViewById(R.id.cellListBookCoverImg);
            title = itemView.findViewById(R.id.cellListBookTitleTxt);
            author = itemView.findViewById(R.id.cellListBookAuthorTxt);
            edition = itemView.findViewById(R.id.cellListBookEditionTxt);
            publisher = itemView.findViewById(R.id.cellListBookPublisherTxt);

        }
    }
}

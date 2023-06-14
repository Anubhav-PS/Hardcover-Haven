package com.anubhav.hardcoverhaven.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.anubhav.hardcoverhaven.R;
import com.anubhav.hardcoverhaven.interfaces.iOnFeatureItemClicked;
import com.anubhav.hardcoverhaven.models.Books;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.textview.MaterialTextView;
import com.squareup.picasso.Picasso;

public class NewReleaseAdapter extends FirestoreRecyclerAdapter<Books, RecyclerView.ViewHolder> {

    private static final int ITEM_FRONT_PAGE_BOOKS = 1;
    private static final int ITEM_LIST_BOOKS = 2;
    private iOnFeatureItemClicked onFeatureItemClicked;
    private int cellLayoutId;


    public NewReleaseAdapter(@NonNull FirestoreRecyclerOptions<Books> options, iOnFeatureItemClicked onFeatureItemClicked, int cellLayoutId) {
        super(options);
        this.onFeatureItemClicked = onFeatureItemClicked;
        this.cellLayoutId = cellLayoutId;
    }

    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        // check here the viewType and return RecyclerView.ViewHolder based on view type
        if (cellLayoutId == R.layout.cell_front_display_books) {
            // if VIEW_TYPE is Grid than return GridViewHolder
            view = LayoutInflater.from(parent.getContext()).inflate(cellLayoutId, parent, false);
            return new NewReleaseFrontViewHolder(view);
        } else if (cellLayoutId == R.layout.cell_list_book) {
            view = LayoutInflater.from(parent.getContext()).inflate(cellLayoutId, parent, false);
            return new NewReleaseListViewHolder(view);
        }
        view = LayoutInflater.from(parent.getContext()).inflate(cellLayoutId, parent, false);
        return new NewReleaseFrontViewHolder(view);
    }


    @Override
    protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull Books model) {
        if (holder instanceof NewReleaseFrontViewHolder) {
            ((NewReleaseFrontViewHolder) holder).title.setText(model.getTitle());
            ((NewReleaseFrontViewHolder) holder).author.setText(model.getAuthor());
            Picasso.get().load(model.getImgUrl()).into(((NewReleaseFrontViewHolder) holder).img);
            ((NewReleaseFrontViewHolder) holder).cardView.setOnClickListener(v -> onFeatureItemClicked.onNewReleaseClicked(model.getBookDocID()));
        } else if (holder instanceof NewReleaseListViewHolder) {
            ((NewReleaseListViewHolder) holder).title.setText(model.getTitle());
            ((NewReleaseListViewHolder) holder).author.setText(model.getAuthor());
            ((NewReleaseListViewHolder) holder).publisher.setText(model.getPublisher());
            final String edition = model.getEdition() + " Edition";
            ((NewReleaseListViewHolder) holder).edition.setText(edition);
            Picasso.get().load(model.getImgUrl()).into(((NewReleaseListViewHolder) holder).img);
            ((NewReleaseListViewHolder) holder).linearLayout.setOnClickListener(v -> onFeatureItemClicked.onNewReleaseClicked(model.getBookDocID()));
        }

    }


    public static class NewReleaseFrontViewHolder extends RecyclerView.ViewHolder {

        private CardView cardView;
        private ImageView img;
        private MaterialTextView title;
        private MaterialTextView author;

        public NewReleaseFrontViewHolder(@NonNull View itemView) {
            super(itemView);


            cardView = itemView.findViewById(R.id.cellFrontDisplayBookCard);
            img = itemView.findViewById(R.id.cellFrontDisplayBookImage);
            title = itemView.findViewById(R.id.cellFrontDisplayBookTitleTxt);
            author = itemView.findViewById(R.id.cellFrontDisplayAuthorTxt);

        }
    }

    public static class NewReleaseListViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout linearLayout;
        private ImageView img;
        private MaterialTextView title;
        private MaterialTextView author;
        private MaterialTextView edition;
        private MaterialTextView publisher;


        public NewReleaseListViewHolder(@NonNull View itemView) {
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


package com.stephen.wikilocation.View;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.stephen.wikilocation.Model.Article;
import com.stephen.wikilocation.Model.Thumbnail;
import com.stephen.wikilocation.R;

import java.util.Collections;
import java.util.List;

/**
 * Created by Stephen on 3/21/2017.
 */
public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>{

    List<Article> articles = Collections.emptyList();

    public ArticleAdapter(List<Article> data){
        articles = data;
    }

    @Override
    public ArticleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.content_row, parent, false);

        ArticleViewHolder holder = new ArticleViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(ArticleViewHolder holder, int position) {
        Article current = articles.get(position);
        Thumbnail thumbnail = current.getThumbnail();
        holder.title.setText(current.getTitle());

        if(thumbnail != null) {
            Picasso.with(holder.icon.getContext())
                    .load(thumbnail.getSource())
                    .into(holder.icon);
        }


    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    class ArticleViewHolder extends RecyclerView.ViewHolder{

        public TextView title;
        public ImageView icon;
        public ArticleViewHolder(View itemView) {
            super(itemView);

            title = (TextView) itemView.findViewById(R.id.articleTitle);
            icon = (ImageView) itemView.findViewById(R.id.articleIcon);
        }
    }
}

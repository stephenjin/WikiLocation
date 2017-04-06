package com.stephen.wikilocation.View;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.vision.text.Text;
import com.squareup.picasso.Picasso;
import com.stephen.wikilocation.Activity.MainActivity;
import com.stephen.wikilocation.Activity.WebViewActivity;
import com.stephen.wikilocation.Model.Article;
import com.stephen.wikilocation.Model.Thumbnail;
import com.stephen.wikilocation.R;

import java.util.Collections;
import java.util.List;

/**
 * Created by Stephen on 3/21/2017.
 */
public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>{
    private Context context;
    List<Article> articles = Collections.emptyList();

    public ArticleAdapter(List<Article> data){
        articles = data;
    }

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String url = context.getString(R.string.baseURL) + ((TextView)v.findViewById(R.id.articleTitle)).getText();
            Intent intent = new Intent(context, WebViewActivity.class);
            //pass url to be loaded to webview activity
            intent.putExtra("url", url);
            context.startActivity(intent);
        }
    };

    @Override
    public ArticleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.content_row, parent, false);
        view.setOnClickListener(mOnClickListener);
        ArticleViewHolder holder = new ArticleViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(ArticleViewHolder holder, int position) {
        Article current = articles.get(position);
        Thumbnail thumbnail = current.getThumbnail();
        holder.title.setText(current.getTitle());
        holder.distance.setText(current.getDist()+" m");


        if(thumbnail != null) {
            Picasso.with(holder.icon.getContext())
                    .load(thumbnail.getSource())
                    .into(holder.icon);
        }
        else{
            holder.icon.setImageResource(R.drawable.ic_wikipedia);
        }


    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    class ArticleViewHolder extends RecyclerView.ViewHolder{

        public TextView title;
        public TextView distance;
        public ImageView icon;
        public ArticleViewHolder(View itemView) {
            super(itemView);

            title = (TextView) itemView.findViewById(R.id.articleTitle);
            icon = (ImageView) itemView.findViewById(R.id.articleIcon);
            distance = (TextView) itemView.findViewById(R.id.articleDistance);
        }
    }
}

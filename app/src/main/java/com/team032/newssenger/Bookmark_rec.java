package com.team032.newssenger;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class bookmark_rec extends RecyclerView.ViewHolder {
    TextView book_content, book_url;
    RelativeLayout delete_Button;

    bookmark_rec(View itemView) {
        super(itemView);

        book_content = (TextView)itemView.findViewById(R.id.book_content);
        book_url = (TextView)itemView.findViewById(R.id.book_url);
        delete_Button = (RelativeLayout) itemView.findViewById(R.id.delete_button);
    }
}
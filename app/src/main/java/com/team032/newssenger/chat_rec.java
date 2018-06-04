package com.team032.newssenger;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class chat_rec extends RecyclerView.ViewHolder  {

    TextView leftText, rightText;

    //preview add
    ImageView imgPreviewIv;
    TextView titleTv;
    TextView descTv;
    TextView siteTv;
    ProgressBar progress;
    RelativeLayout previewGroup;

    //quick
    TextView next, more, bookmark;

    public chat_rec(View itemView){
        super(itemView);

        leftText = (TextView)itemView.findViewById(R.id.leftText);
        rightText = (TextView)itemView.findViewById(R.id.rightText);

        //preview add
        imgPreviewIv = (ImageView) itemView.findViewById(R.id.img_preview_iv);
        titleTv = (TextView)itemView.findViewById(R.id.title_tv);
        descTv = (TextView)itemView.findViewById(R.id.desc_tv);
        siteTv = (TextView)itemView.findViewById(R.id.site_tv);
        previewGroup = itemView.findViewById(R.id.previewGroup);
        progress = (ProgressBar) itemView.findViewById(R.id.progress);

        //quick
        more = (TextView)itemView.findViewById(R.id.more);
        next = (TextView)itemView.findViewById(R.id.next);
        bookmark = (TextView)itemView.findViewById(R.id.bookmark);
    }
}
package com.team032.newssenger;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.team032.newssenger.model.ChatMessage;

import java.util.Objects;

import ai.api.model.AIError;
import ai.api.AIListener;
import ai.api.model.AIResponse;

import static com.team032.newssenger.R.*;
import static com.team032.newssenger.R.layout.*;

public class BookmarkActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, AIListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    //데이터베이스 사용
    DatabaseReference mFirebaseDatabaseReference;
    FirebaseRecyclerAdapter<ChatMessage, bookmark_rec> mFirebaseAdapter;
    private RecyclerView mMessageRecyclerView;

    //@RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark);

        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(layout.action_bar);

        mMessageRecyclerView = findViewById(id.bookmark_recycler_view);
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference(); //ref
        mFirebaseDatabaseReference.keepSynced(true); //추가

        mMessageRecyclerView.setHasFixedSize(true);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        mMessageRecyclerView.setLayoutManager(linearLayoutManager);

        Query query = mFirebaseDatabaseReference.child("all").child("bookmark");

        FirebaseRecyclerOptions<ChatMessage> options = new FirebaseRecyclerOptions.Builder<ChatMessage>()
                .setQuery(query, ChatMessage.class)
                .build();

        mFirebaseAdapter = new FirebaseRecyclerAdapter<ChatMessage, bookmark_rec>(options) {
            @Override
            protected void onBindViewHolder(@NonNull bookmark_rec holder, int position, @NonNull final ChatMessage model) {
                final String value_url = model.getName();
                final String value_context = model.getText();

                holder.book_content.setText(value_context);
                holder.book_url.setText(value_url);

                holder.book_url.setVisibility(View.VISIBLE);
                holder.book_content.setVisibility(View.VISIBLE);
                holder.delete_Button.setVisibility(View.VISIBLE);

                //삭제 버튼
                holder.delete_Button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Query remove_url = mFirebaseDatabaseReference.child("all").child("bookmark").orderByChild("name").equalTo(value_url);

                        remove_url.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot bookmarkSnapshot : dataSnapshot.getChildren()) {
                                    bookmarkSnapshot.getRef().removeValue();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.e(TAG, "onCancelled", databaseError.toException());
                            }
                        });
                    }
                });


                //링크 클릭
                holder.book_url.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Uri uri = Uri.parse(value_url);
                        CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();

                        intentBuilder.setToolbarColor(ContextCompat.getColor(BookmarkActivity.this, R.color.colorPrimaryDark));
                        intentBuilder.setSecondaryToolbarColor(ContextCompat.getColor(BookmarkActivity.this, R.color.colorPrimary));

                        intentBuilder.setStartAnimations(BookmarkActivity.this, R.anim.slide_in_right, R.anim.slide_out_left);
                        intentBuilder.setExitAnimations(BookmarkActivity.this, R.anim.slide_in_left, R.anim.slide_out_right);

                        CustomTabsIntent customTabsIntent = intentBuilder.build();
                        customTabsIntent.launchUrl(BookmarkActivity.this, uri);
                    }
                });
            }

            @NonNull
            @Override
            public bookmark_rec onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bookmark, parent, false);
                return new bookmark_rec(view);
            }
        };

        // 리사이클러뷰에 레이아웃 매니저와 어댑터 설정
        mMessageRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mMessageRecyclerView.setAdapter(mFirebaseAdapter);

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);

                int msgCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();

                if (lastVisiblePosition == -1 ||
                        (positionStart >= (msgCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mMessageRecyclerView.scrollToPosition(positionStart);
                }

            }
        });
        mMessageRecyclerView.setAdapter(mFirebaseAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mFirebaseAdapter.stopListening();
    }

    @Override
    public void onResult(AIResponse result) {

    }

    @Override
    public void onError(AIError error) {

    }

    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {

    }

    @Override
    public void onListeningCanceled() {

    }

    @Override
    public void onListeningFinished() {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
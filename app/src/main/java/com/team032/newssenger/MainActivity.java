package com.team032.newssenger;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.team032.newssenger.model.ChatMessage;

import java.util.HashMap;
import java.util.Map;

import ai.api.AIDataService;
import ai.api.AIListener;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;

import static com.team032.newssenger.R.*;
import static com.team032.newssenger.R.layout.*;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, AIListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    //데이터베이스 사용
    DatabaseReference mFirebaseDatabaseReference;
    EditText mMessageEditText; //메세지 받는 곳, =edittext
    RelativeLayout send_button;
    TextView next;

    //길이 제한 추가
    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    //뷰 - firebase ui에서 지원
    //private FirebaseRecyclerAdapter<ChatMessage, MessageViewHolder> mFirebaseAdapter;
    //chat_rec == MessageViewHolder
    FirebaseRecyclerAdapter<ChatMessage, chat_rec> mFirebaseAdapter;
    private RecyclerView mMessageRecyclerView;

    //챗봇 추가
    private AIService aiService;

    //로그아웃
    private FirebaseAuth mFirebaseAuth;

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_main);
        //initView();

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(layout.action_bar);

        //음성 입력
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},1);

        //로그아웃
        mFirebaseAuth = FirebaseAuth.getInstance();

        mMessageRecyclerView = findViewById(id.message_recycler_view); //=recyclerView
        mMessageEditText = findViewById(id.message_edit);
        send_button = findViewById(id.send_button);

        //데이터베이스
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference(); //ref
        mFirebaseDatabaseReference.keepSynced(true); //추가

        //추가
        mMessageRecyclerView.setHasFixedSize(true);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        mMessageRecyclerView.setLayoutManager(linearLayoutManager);

        //api.ai
        final AIConfiguration config = new AIConfiguration(
                "f6e7f845ccfc41bb9aa48e6bd9476cc5",
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        aiService = AIService.getService(this, config);
        aiService.setListener(this);

        final AIDataService aiDataService = new AIDataService(config);
        final AIRequest aiRequest = new AIRequest();


// ------------------------------- 기본 채팅창 만들기 -------------------------------- //

        // 보내기 버튼 (=addBtn)
        send_button.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onClick(View view) {
                String message = mMessageEditText.getText().toString().trim();

                if (!message.equals("")) {
                    ChatMessage chatMessage = new ChatMessage(message, "user", ServerValue.TIMESTAMP);
                    mFirebaseDatabaseReference.child("chat").push().setValue(chatMessage);

                    aiRequest.setQuery(message);
                    new AsyncTask<AIRequest, Void, AIResponse>(){
                        @Override
                        protected AIResponse doInBackground(AIRequest... aiRequests) {
                            final AIRequest request = aiRequests[0];
                            try {
                                final AIResponse response = aiDataService.request(aiRequest);
                                return response;
                            }
                            catch (AIServiceException e) {
                            }
                            return null;
                        }
                        @Override
                        protected void onPostExecute(AIResponse response) {
                            if (response != null) {
                                Result result = response.getResult();
                                String reply = result.getFulfillment().getSpeech();
                                ChatMessage chatMessage = new ChatMessage(reply, "bot", ServerValue.TIMESTAMP);
                                mFirebaseDatabaseReference.child("chat").push().setValue(chatMessage);
                            }
                        }
                    }.execute(aiRequest);
                }
                else {
                    aiService.startListening();
                }
                mMessageEditText.setText("");
            }
        });

        //        //더보기 버튼
//        next.setOnClickListener(new View.OnClickListener() {
//            @SuppressLint("StaticFieldLeak")
//            @Override
//            public void onClick(View view) {
//                ChatMessage chatMessage = new ChatMessage(user_msg, "user");
//                mFirebaseDatabaseReference.child("chat").push().setValue(chatMessage);
//
//                aiRequest.setQuery(user_msg);
//                new AsyncTask<AIRequest, Void, AIResponse>(){
//                    @Override
//                    protected AIResponse doInBackground(AIRequest... aiRequests) {
//                        final AIRequest request = aiRequests[0];
//                        try {
//                            final AIResponse response = aiDataService.request(aiRequest);
//                            return response;
//                        }
//                        catch (AIServiceException e) {
//
//                        }
//                        return null;
//                    }
//                    @Override
//                    protected void onPostExecute(AIResponse response) {
//                        if (response != null) {
//                            Result result = response.getResult();
//                            String reply = result.getFulfillment().getSpeech();
//                            ChatMessage chatMessage = new ChatMessage(reply, "bot");
//                            mFirebaseDatabaseReference.child("chat").push().setValue(chatMessage);
//                        }
//                    }
//                }.execute(aiRequest);
//            }
//        });

// ------------------------------- 뷰에 메세지 띄우기 -------------------------------- //

        // 쿼리 수행 위치
        Query query = mFirebaseDatabaseReference.child("chat");

        // 옵션 - 쿼리 설정 chatmessage 클래스에 결과를 반환한다.
        FirebaseRecyclerOptions<ChatMessage> options = new FirebaseRecyclerOptions.Builder<ChatMessage>()
                        .setQuery(query, ChatMessage.class)
                        .build();

        // 어댑터 - 뷰
        mFirebaseAdapter = new FirebaseRecyclerAdapter<ChatMessage, chat_rec>(options) {

            // Allows to remember the last item shown on screen
//            private int lastPosition = -1;

            @Override
            protected void onBindViewHolder(final chat_rec holder, int position, @NonNull final ChatMessage model) {
                if (model.getName().equals("user")) {
                    holder.rightText.setText(model.getText());
                    holder.rightText.setVisibility(View.VISIBLE);
                    holder.leftText.setVisibility(View.GONE);
//                    setAnimation(holder.rightText, position);
//                    holder.rightText.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, anim.slide_chat_right));

                    holder.imgPreviewIv.setVisibility(View.GONE);
                    holder.titleTv.setVisibility(View.GONE);
                    holder.descTv.setVisibility(View.GONE);
                    holder.siteTv.setVisibility(View.GONE);
                    holder.previewGroup.setVisibility(View.GONE);

                    //quick
                    holder.next.setVisibility(View.GONE);
                    holder.more.setVisibility(View.GONE);
                }

                //챗봇이 말할 때
                else {
                    //split
                    String chatbot_msg = model.getText();
                    String[] value = chatbot_msg.split("@");
                    int value_length = value.length;

                    if(!value[1].equals("null")) {
                        holder.leftText.setText(value[1]);
                    }
                    else {
                        holder.leftText.setText(value[0]);
                    }

                    holder.rightText.setVisibility(View.GONE);
                    holder.leftText.setVisibility(View.VISIBLE);
//                    setAnimation(holder.leftText, position);
//                    holder.leftText.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, anim.slide_chat_left));

                    holder.imgPreviewIv.setVisibility(View.GONE);
                    holder.titleTv.setVisibility(View.GONE);
                    holder.descTv.setVisibility(View.GONE);
                    holder.siteTv.setVisibility(View.GONE);
                    holder.previewGroup.setVisibility(View.GONE);

                    holder.next.setVisibility(View.GONE);
                    holder.more.setVisibility(View.GONE);
                    //holder.previewGroup.setVisibility(View.GONE);

                    if(value_length >= 2 && !value[1].equals("null")) {
                        final Uri uri = Uri.parse(value[0]); //링크만 저장

                        holder.next.setVisibility(View.VISIBLE);
                        holder.more.setVisibility(View.VISIBLE);

                        //같은 분야의 다른 뉴스 계속 보여주게 바꾸기
                        holder.next.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                holder.next.setVisibility(View.GONE);
                                holder.more.setVisibility(View.GONE);

                            }
                        });

                        //quick
                        holder.more.setOnClickListener(new View.OnClickListener() {
                            //preview 링크 클릭 시 이동
                            @Override
                            public void onClick(View v) {
                                holder.progress.setVisibility(View.VISIBLE);
                                LinkUtil.getLinkPreview(MainActivity.this, String.valueOf(uri), new GetLinkPreviewListener() {
                                    @Override
                                    public void onSuccess(final ChatMessage link) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                holder.progress.setVisibility(View.GONE);
                                                holder.imgPreviewIv.setVisibility(View.VISIBLE);
                                                holder.titleTv.setVisibility(View.VISIBLE);
                                                holder.descTv.setVisibility(View.VISIBLE);
                                                holder.siteTv.setVisibility(View.VISIBLE);
                                                holder.previewGroup.setVisibility(View.VISIBLE);

                                                holder.titleTv.setText(link.getTitle() != null ? link.getTitle() : "");
                                                holder.descTv.setText(link.getDescription() != null ? link.getDescription() : "");
                                                holder.siteTv.setText(link.getSiteName() != null ? link.getSiteName() : "");

                                                holder.next.setVisibility(View.GONE);
                                                holder.more.setVisibility(View.GONE);

                                                holder.previewGroup.setOnClickListener(new View.OnClickListener() {
                                                    //preview 링크 클릭 시 이동
                                                    @Override
                                                    public void onClick(View v) {
                                                        CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();

                                                        intentBuilder.setToolbarColor(ContextCompat.getColor(MainActivity.this, color.colorPrimaryDark));
                                                        intentBuilder.setSecondaryToolbarColor(ContextCompat.getColor(MainActivity.this, color.colorPrimary));

                                                        intentBuilder.setStartAnimations(MainActivity.this, anim.slide_in_right, anim.slide_out_left);
                                                        intentBuilder.setExitAnimations(MainActivity.this, anim.slide_in_left,
                                                                anim.slide_out_right);

                                                        CustomTabsIntent customTabsIntent = intentBuilder.build();
                                                        customTabsIntent.launchUrl(MainActivity.this, uri);
                                                    }
                                                });

                                                if (link.getImageFile() != null)
                                                    Glide.with(MainActivity.this).load(link.getImageFile()).into(holder.imgPreviewIv);
                                                else
                                                    Glide.with(MainActivity.this).load(mipmap.ic_launcher).into(holder.imgPreviewIv);
                                            }
                                        });
                                    }

                                    @Override
                                    public void onFailed(final Exception e) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                holder.progress.setVisibility(View.GONE);
                                                holder.imgPreviewIv.setVisibility(View.GONE);
                                                holder.titleTv.setVisibility(View.GONE);
                                                holder.descTv.setVisibility(View.GONE);
                                                holder.siteTv.setVisibility(View.GONE);

                                                holder.next.setVisibility(View.GONE);
                                                holder.more.setVisibility(View.GONE);

                                                //holder.previewGroup.setVisibility(View.GONE);
                                                //Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                });
                            }
                        }); //quick
                    } //링크 빼내기

//                    holder.leftText.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            Uri uri = Uri.parse(model.getText());
//
//                            // create an intent builder
//                            CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();
//
//                            // Begin customizing
//                            // set toolbar colors
//                            intentBuilder.setToolbarColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimaryDark));
//                            intentBuilder.setSecondaryToolbarColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary));
//
//                            // set start and exit animations
//                            intentBuilder.setStartAnimations(MainActivity.this, R.anim.slide_in_right, R.anim.slide_out_left);
//                            intentBuilder.setExitAnimations(MainActivity.this, R.anim.slide_in_left,
//                                    R.anim.slide_out_right);
//
//                            // build custom tabs intent
//                            CustomTabsIntent customTabsIntent = intentBuilder.build();
//
//                            // launch the url
//                            customTabsIntent.launchUrl(MainActivity.this, uri);
//                        }
//                    });
                }
            }

            @NonNull
            @Override
            public chat_rec onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(item_message, parent, false);
                return new chat_rec(view);
            }

//            private void setAnimation(View viewToAnimate, int position) {
//                // If the bound view wasn't previously displayed on screen, it's animated
//                if (position > lastPosition) {
//                    Animation animation = AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.slide_in_left);
//                    viewToAnimate.startAnimation(animation);
//                    lastPosition = position;
//                }
//            }
//
//            private void setAnimationLeft(View viewToAnimate, int position) {
//                // If the bound view wasn't previously displayed on screen, it's animated
//                if (position > lastPosition) {
//                    Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_chat_left);
//                    viewToAnimate.startAnimation(animation);
//                    lastPosition = position;
//                }
//            }
//
//            private void setAnimationRight(View viewToAnimate, int position) {
//                // If the bound view wasn't previously displayed on screen, it's animated
//                if (position > lastPosition) {
//                    Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_chat_right);
//                    viewToAnimate.startAnimation(animation);
//                    lastPosition = position;
//                }
//            }
        };

        // 리사이클러뷰에 레이아웃 매니저와 어댑터 설정
        mMessageRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mMessageRecyclerView.setAdapter(mFirebaseAdapter);
// --------------------------------------------------------------------------------------- //

        //글자수 제한 추가한 부분
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        String splash_background = mFirebaseRemoteConfig.getString("splash_background");
        //getWindow().setStatusBarColor(Color.parseColor(splash_background));

        FirebaseRemoteConfigSettings firebaseRemoteConfigSettings =
                new FirebaseRemoteConfigSettings.Builder()
                        .setDeveloperModeEnabled(true)
                        .build();

        Map<String, Object> defaultConfigMap = new HashMap<>();
        defaultConfigMap.put("message_length", 50L);    // 글자수 제한

        mFirebaseRemoteConfig.setConfigSettings(firebaseRemoteConfigSettings);
        mFirebaseRemoteConfig.setDefaults(defaultConfigMap);

        fetchConfig();

// ------------------------------- 채팅방 옵션 설정 -------------------------------- //

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
        }); mMessageRecyclerView.setAdapter(mFirebaseAdapter);

        // 새로운 글이 추가되면 제일 하단으로 포지션 이동
        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mFirebaseAdapter.getItemCount();

                LinearLayoutManager layoutManager = (LinearLayoutManager) mMessageRecyclerView.getLayoutManager();
                int lastVisiblePosition = layoutManager.findLastCompletelyVisibleItemPosition();

                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mMessageRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

//        // 키보드 올라올 때 RecyclerView의 위치를 마지막 포지션으로 이동
        mMessageRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    v.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mMessageRecyclerView.smoothScrollToPosition(mFirebaseAdapter.getItemCount());
                        }
                    }, 0);
                }
            }
        });
    }

    //생명주기에 따라 모니터링, 멈추게 함 - 데이터를 읽어올 수 있게 하는 부분
    @Override
    protected void onStart() {
        super.onStart();
        // FirebaseRecyclerAdapter 실시간 쿼리 시작
        mFirebaseAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // FirebaseRecyclerAdapter 실시간 쿼리 중지
        mFirebaseAdapter.stopListening();
    }

    @Override
    public void onResult(ai.api.model.AIResponse response) {
        Result result = response.getResult();

        String message = result.getResolvedQuery();
        ChatMessage chatMessage0 = new ChatMessage(message, "user", ServerValue.TIMESTAMP);
        mFirebaseDatabaseReference.child("chat").push().setValue(chatMessage0);

        String reply = result.getFulfillment().getSpeech();
        ChatMessage chatMessage = new ChatMessage(reply, "bot", ServerValue.TIMESTAMP);
        mFirebaseDatabaseReference.child("chat").push().setValue(chatMessage);
    }

    //추가
    @Override
    public void onError(ai.api.model.AIError error) {

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

    //로그아웃
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // 로그아웃 메뉴 아이템 선택 시
            case R.id.sign_out_menu:
                mFirebaseAuth.signOut();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //글자수 제한 추가한 부분
    private void fetchConfig() {
        long cacheExpiration = 3600;
        if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }

        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mFirebaseRemoteConfig.activateFetched();
                        applyRetrievedLengthLimit();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Error fetching config" + e.getMessage());
                applyRetrievedLengthLimit();
            }
        });
    }

    private void applyRetrievedLengthLimit() {
        Long messageLength = mFirebaseRemoteConfig.getLong("message_length");
        mMessageEditText.setFilters(new InputFilter[] {
                new InputFilter.LengthFilter(messageLength.intValue())
        });
        Log.d(TAG, "메시지 길이 : " + messageLength);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
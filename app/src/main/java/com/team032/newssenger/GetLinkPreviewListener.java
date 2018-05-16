package com.team032.newssenger;

import com.team032.newssenger.model.ChatMessage;

public interface GetLinkPreviewListener {
    void onSuccess(ChatMessage link);
    void onFailed(Exception e);
}

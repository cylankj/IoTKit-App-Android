/*
 * Copyright (c) 2014 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.cylan.jiafeigou.rtmp.youtube.util;

import com.google.api.client.util.Key;
import com.google.api.services.youtube.model.LiveBroadcast;
import com.google.api.services.youtube.model.ThumbnailDetails;

/**
 * @author Ibrahim Ulukaya <ulukaya@google.com>
 *         <p/>
 *         Helper class to handle YouTube videos.
 */
public class EventData {
    @Key
    private LiveBroadcast mEvent;
    @Key
    private String mIngestionAddress;

    @Key
    private String title;

    public LiveBroadcast getEvent() {
        return mEvent;
    }

    public void setEvent(LiveBroadcast event) {
        mEvent = event;
    }

    public String getId() {
        if (mEvent != null) {
            return mEvent.getId();
        } else {
            return null;
        }
    }

    public String getTitle() {
        if (mEvent != null && mEvent.getSnippet() != null) {
            return mEvent.getSnippet().getTitle();
        }
        return null;
    }

    public String getThumbUri() {
        if (mEvent != null && mEvent.getSnippet() != null) {
            ThumbnailDetails thumbnails = mEvent.getSnippet().getThumbnails();
            if (thumbnails != null && thumbnails.getDefault() != null) {
                String url = thumbnails.getDefault().getUrl();
                // if protocol is not defined, pick https
                if (url.startsWith("//")) {
                    url = "https:" + url;
                }
                return url;
            }
        }
        return null;
    }

    public String getIngestionAddress() {
        return mIngestionAddress;
    }

    public void setIngestionAddress(String ingestionAddress) {
        mIngestionAddress = ingestionAddress;
    }

    public String getWatchUri() {
        return "http://www.youtube.com/watch?v=" + getId();
    }

}

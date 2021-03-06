/*
 * Copyright (c) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cw.tv_yt.presenter;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.leanback.widget.ImageCardView;
import androidx.leanback.widget.Presenter;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.cw.tv_yt.R;
import com.cw.tv_yt.model.Video;
import com.cw.tv_yt.ui.MainFragment;
import com.cw.tv_yt.ui.VideoDetailsActivity;

/*
 * A CardPresenter is used to generate Views and bind Objects to them on demand.
 * It contains an Image CardView
 */
public class CardPresenter extends Presenter {
    private int mSelectedBackgroundColor = -1;
    private int mDefaultBackgroundColor = -1;
    private Drawable mDefaultCardImage;
    FragmentActivity act;

    public  CardPresenter(){}

    public CardPresenter(FragmentActivity main_act){
        act = main_act;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        mDefaultBackgroundColor = ContextCompat.getColor(parent.getContext(), R.color.default_background);
        mSelectedBackgroundColor = ContextCompat.getColor(parent.getContext(), R.color.selected_background);
        mDefaultCardImage = parent.getResources().getDrawable(R.drawable.movie, null);

        ImageCardView cardView = new ImageCardView(parent.getContext()) {
            @Override
            public void setSelected(boolean selected) {
                updateCardBackgroundColor(this, selected);
                super.setSelected(selected);
            }
        };

        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
        updateCardBackgroundColor(cardView, false);
        return new ViewHolder(cardView);
    }

    private void updateCardBackgroundColor(ImageCardView view, boolean selected) {
        int color = selected ? mSelectedBackgroundColor : mDefaultBackgroundColor;

        // Both background colors should be set because the view's
        // background is temporarily visible during animations.
        view.setBackgroundColor(color);
        view.findViewById(R.id.info_field).setBackgroundColor(color);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        Video video = (Video) item;

        ImageCardView cardView = (ImageCardView) viewHolder.view;
        cardView.setTitleText(video.title);
        ((TextView)cardView.findViewById(R.id.title_text)).setLines(3);// setMaxLines(5);
//        cardView.setContentText(video.studio);

        if (video.cardImageUrl != null) {
            // Set card size from dimension resources.
            Resources res = cardView.getResources();
            int width = res.getDimensionPixelSize(R.dimen.card_width);
            int height = res.getDimensionPixelSize(R.dimen.card_height);
            cardView.setMainImageDimensions(width, height);

            Glide.with(cardView.getContext())
                    .load(video.cardImageUrl)
                    .apply(RequestOptions.errorOf(mDefaultCardImage))
                    .into(cardView.getMainImageView());
        }

        // card view long click listener: launch VideoDetailsActivity
        cardView.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                System.out.println("CardPresenter / onLongClick");
                if (item instanceof Video) {
                    Video video = (Video) item;
                    Intent intent = new Intent(act, VideoDetailsActivity.class);
                    intent.putExtra(VideoDetailsActivity.VIDEO, video);

                    act.runOnUiThread(new Runnable() {
                        public void run() {
                            Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                                    act,
                                    ((ImageCardView) viewHolder.view).getMainImageView(),
                                    VideoDetailsActivity.SHARED_ELEMENT_NAME).toBundle();
                            act.startActivityForResult(intent, MainFragment.VIDEO_DETAILS_INTENT, bundle);
                        }
                    });
                }
                return true;
            }
        });
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
        ImageCardView cardView = (ImageCardView) viewHolder.view;

        // Remove references to images so that the garbage collector can free up memory.
        cardView.setBadgeImage(null);
        cardView.setMainImage(null);
    }
}

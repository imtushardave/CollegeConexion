package com.cloudiera.collegeconexion.Utils;


import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudiera.collegeconexion.Models.Messages;
import com.cloudiera.collegeconexion.R;
import com.cloudiera.collegeconexion.Talks.ChatImageShowActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private static final String TAG = "MessageAdapter";
    private List<Messages> mMessageList;
    private FirebaseAuth mAuth;
    private Context mContext;

    public MessageAdapter(List<Messages> mMessageList, Context context) {

        this.mMessageList = mMessageList;
        mContext = context;

    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_chat_message1, parent, false);

        return new MessageViewHolder(v);

    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messageText, chatSentTime;
        public ImageView chatSeenIcon, messageImage;
        public RelativeLayout messageCard;
        public LinearLayout pdfMessgae, audioMessage;
        public ImageView playButton,pauseButton;
        public SeekBar progressSeekbar;

        public MessageViewHolder(View view) {
            super(view);
            messageText = (TextView) view.findViewById(R.id.message_text);
            messageImage = (ImageView) view.findViewById(R.id.message_image);
            messageCard = (RelativeLayout) view.findViewById(R.id.messageCard);
            chatSeenIcon = (ImageView) view.findViewById(R.id.seen_icon);
            chatSentTime = (TextView) view.findViewById(R.id.time_view);
            pdfMessgae = (LinearLayout) view.findViewById(R.id.pdf);
            audioMessage = (LinearLayout) view.findViewById(R.id.audio);
            playButton = (ImageView)view.findViewById(R.id.play_button);
            pauseButton = (ImageView)view.findViewById(R.id.pause_button);
            progressSeekbar = (SeekBar)view.findViewById(R.id.player_seekbar);

        }
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder viewHolder, int i) {

        mAuth = FirebaseAuth.getInstance();
        final Messages c = mMessageList.get(i);
        final String from_user = c.getFrom();
        final String message_type = c.getType();
        boolean isSeen = c.isSeen();
        final String timeView = getCurrentTime(c.getTime());
        final String current_user = mAuth.getCurrentUser().getUid();
        Log.d(TAG, "onBindViewHolder: " + current_user);

        if (from_user.equals(current_user)) {
            if (isSeen) {
                viewHolder.chatSeenIcon.setImageResource(R.drawable.ic_seen_green);
            } else {

                viewHolder.chatSeenIcon.setImageResource(R.drawable.ic_seen_blue);
            }
            viewHolder.messageText.setTextColor(mContext.getResources().getColor(R.color.white));
            viewHolder.messageCard.setBackground(mContext.getResources().getDrawable(R.drawable.message_backgroundfrom));
            viewHolder.chatSeenIcon.setVisibility(View.VISIBLE);
            viewHolder.chatSentTime.setText(timeView);
            RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams) viewHolder.messageCard.getLayoutParams();
            param.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            viewHolder.messageCard.setLayoutParams(param);

        } else {
            viewHolder.messageCard.setBackground(mContext.getResources().getDrawable(R.drawable.message_background));
            viewHolder.messageText.setTextColor(mContext.getResources().getColor(R.color.white));
            viewHolder.chatSentTime.setText(timeView);
            viewHolder.chatSeenIcon.setVisibility(View.INVISIBLE);
            RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams) viewHolder.messageCard.getLayoutParams();
            param.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            viewHolder.messageCard.setLayoutParams(param);

        }
        switch (message_type) {
            case "text":
                viewHolder.messageText.setVisibility(View.VISIBLE);
                viewHolder.messageImage.setVisibility(View.GONE);
                viewHolder.pdfMessgae.setVisibility(View.GONE);
                viewHolder.audioMessage.setVisibility(View.GONE);
                viewHolder.messageText.setText(c.getMessage());

                break;
            case "image":
                viewHolder.messageImage.setVisibility(View.VISIBLE);
                viewHolder.messageText.setVisibility(View.GONE);
                viewHolder.pdfMessgae.setVisibility(View.GONE);
                viewHolder.audioMessage.setVisibility(View.GONE);

                Picasso.with(mContext).load(c.getMessage()).networkPolicy(NetworkPolicy.OFFLINE)
                        .into(viewHolder.messageImage, new Callback() {
                            @Override
                            public void onSuccess() {
                            }

                            @Override
                            public void onError() {
                                Picasso.with(mContext).load(c.getMessage()).into(viewHolder.messageImage);
                            }
                        });

                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, ChatImageShowActivity.class);
                        intent.putExtra("imageUrl", c.getMessage());
                        intent.putExtra("timestamp", timeView);
                        intent.putExtra("userId", from_user);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);
                    }
                });

                break;
            case "pdf":

                viewHolder.messageImage.setVisibility(View.GONE);
                viewHolder.messageText.setVisibility(View.GONE);
                viewHolder.pdfMessgae.setVisibility(View.VISIBLE);
                viewHolder.audioMessage.setVisibility(View.GONE);

                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String url = c.getMessage();
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.parse(url), "application/pdf");
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        mContext.startActivity(Intent.createChooser(intent, "Choose an Application:"));
                    }
                });

                break;
//            case "audio":
//                viewHolder.messageImage.setVisibility(View.GONE);
//                viewHolder.messageText.setVisibility(View.GONE);
//                viewHolder.pdfMessgae.setVisibility(View.GONE);
//                viewHolder.audioMessage.setVisibility(View.VISIBLE);
//                // AudioWife takes care of click handler for play/pause button
//                AudioWife.getInstance()
//                        .init(mContext, Uri.parse(c.getMessage()))
//                        .setPlayView(viewHolder.playButton)
//                        .setPauseView(viewHolder.pauseButton)
//                        .setSeekBar(viewHolder.progressSeekbar);
//
//                AudioWife.getInstance().addOnPlayClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        // Lights-Camera-Action. Lets dance.
//                        viewHolder.playButton.setVisibility(View.GONE);
//                        viewHolder.pauseButton.setVisibility(View.VISIBLE);
//                        AudioWife.getInstance().play();
//                    }
//                });
//
//                AudioWife.getInstance().addOnPauseClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        // Your on audio pause stuff.
//                        viewHolder.playButton.setVisibility(View.VISIBLE);
//                        viewHolder.pauseButton.setVisibility(View.GONE);
//                        AudioWife.getInstance().pause();
//                    }
//                });
//                AudioWife.getInstance().addOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//
//                    @Override
//                    public void onCompletion(MediaPlayer mp) {
//                        // do you stuff
//                        viewHolder.playButton.setVisibility(View.VISIBLE);
//                        viewHolder.pauseButton.setVisibility(View.GONE);
//                        AudioWife.getInstance().release();
//                    }
//                });
//
//                break;
        }

    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }


    /**
     * get Time from the time stamp
     *
     * @param timestamp
     * @return
     */
    public String getCurrentTime(long timestamp) {

        String time = DateUtils.formatDateTime(mContext, timestamp, DateUtils.FORMAT_SHOW_TIME);
        return time;

    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }
}

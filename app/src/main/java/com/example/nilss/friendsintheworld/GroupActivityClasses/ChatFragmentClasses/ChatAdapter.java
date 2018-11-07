package com.example.nilss.friendsintheworld.GroupActivityClasses.ChatFragmentClasses;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.nilss.friendsintheworld.GroupActivityClasses.GroupController;
import com.example.nilss.friendsintheworld.Pojos.TextMessage;
import com.example.nilss.friendsintheworld.R;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int SELF_MESSAGE_CONST = 0;
    private static final int OTHER_MESSAGE_CONST = 1;
    private ChatFragment chatFragment;
    private GroupController groupController;
    private List<TextMessage> currenMessageList;

    public ChatAdapter(ChatFragment chatFragment, GroupController groupController, List<TextMessage> currenMessageList) {
        this.chatFragment = chatFragment;
        this.groupController = groupController;
        this.currenMessageList = currenMessageList;
    }

    @Override
    public int getItemViewType(int position) {
        if(currenMessageList.get(position).getSender().equals(groupController.getCurrentUserName())){
            return SELF_MESSAGE_CONST;
        }
        else{
            return OTHER_MESSAGE_CONST;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        switch(viewType){
            case(SELF_MESSAGE_CONST):
                View v0 = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.recycler_row_message_self, viewGroup, false);
                return new ViewHolderSelf(v0);
            case(OTHER_MESSAGE_CONST):
                View v1 = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.recycler_row_message_other, viewGroup, false);
                return new ViewHolderOther(v1);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        switch (viewHolder.getItemViewType()) {
            case(SELF_MESSAGE_CONST):
                ViewHolderSelf v0 = (ViewHolderSelf)viewHolder;
                if(currenMessageList.get(i).getImageArray()!=null){
                    //set image
                    v0.imageIv.setImageBitmap(chatFragment.byteArrayToBitmap(currenMessageList.get(i).getImageArray()));
                }
                else {
                    v0.imageIv.getLayoutParams().height = 0;
                    v0.imageIv.getLayoutParams().width = 0;
                }
                v0.messageTextContent.setText(currenMessageList.get(i).getTextMessage());
                break;
            case(OTHER_MESSAGE_CONST):
                ViewHolderOther v1 = (ViewHolderOther)viewHolder;
                v1.senderNameTv.setText(currenMessageList.get(i).getSender());
                if(currenMessageList.get(i).getImageArray()!=null){
                    //set image
                    v1.imageIv.setImageBitmap(chatFragment.byteArrayToBitmap(currenMessageList.get(i).getImageArray()));
                }
                else {
                    v1.imageIv.getLayoutParams().height = 0;
                    v1.imageIv.getLayoutParams().width = 0;
                }
                v1.messageTextContent.setText(currenMessageList.get(i).getTextMessage());
                break;
        }
    }

    @Override
    public int getItemCount() {
        return currenMessageList.size();
    }

    public class ViewHolderOther extends RecyclerView.ViewHolder {
        public TextView senderNameTv;
        public ImageView imageIv;
        public TextView messageTextContent;

        public ViewHolderOther(@NonNull View itemView) {
            super(itemView);
            this.senderNameTv = itemView.findViewById(R.id.text_message_nameOther);
            this.imageIv = itemView.findViewById(R.id.imageIvOther);
            this.messageTextContent = itemView.findViewById(R.id.text_message_bodyOther);
        }
    }

    public class ViewHolderSelf extends RecyclerView.ViewHolder {
        public ImageView imageIv;
        public TextView messageTextContent;

        public ViewHolderSelf(@NonNull View itemView) {
            super(itemView);
            this.imageIv = itemView.findViewById(R.id.imageIvSelf);
            this.messageTextContent = itemView.findViewById(R.id.text_message_bodySelf);
        }
    }
}

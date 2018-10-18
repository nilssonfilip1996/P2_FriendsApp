package com.example.nilss.friendsintheworld.GroupActivityClasses.ManageGroupFragmentClasses;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.nilss.friendsintheworld.GroupActivityClasses.GroupController;
import com.example.nilss.friendsintheworld.R;

import java.util.ArrayList;
import java.util.List;

public class ManageGroupsAdapter extends RecyclerView.Adapter<ManageGroupsAdapter.ViewHolder>{
    private static final String TAG = "ManageGroupsAdapter";
    private List<String> currentGroupList;
    private GroupController groupController;
    private OnItemClickListener mListener;


    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }

    public ManageGroupsAdapter(GroupController groupController, ArrayList<String> currentGroupsList) {
        this.groupController = groupController;
        this.currentGroupList = currentGroupsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.recycler_row_current_groups, viewGroup, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.groupNametv.setText(currentGroupList.get(i));
    }

    @Override
    public int getItemCount() {
        return currentGroupList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView groupNametv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.groupNametv = itemView.findViewById(R.id.recycler_row_grouptv);
            itemView.setOnClickListener((View v)->{
                if(mListener!=null){
                    int pos = getAdapterPosition();
                    if(pos!=RecyclerView.NO_POSITION){
                        mListener.onItemClicked(pos);
                    }
                }
            });
            itemView.setOnLongClickListener((View v)->{
                if(mListener!=null){
                    int pos = getAdapterPosition();
                    if(pos!=RecyclerView.NO_POSITION){
                        mListener.onItemLongClicked(pos);
                    }
                }
                return true;
            });
        }
    }
}

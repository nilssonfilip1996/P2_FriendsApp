package com.example.nilss.friendsintheworld.GroupActivityClasses.ManageGroupFragmentClasses;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.nilss.friendsintheworld.GroupActivityClasses.GroupController;
import com.example.nilss.friendsintheworld.R;

import java.util.ArrayList;

public class RecyclerFragmentManageGroups extends Fragment {
    private static final String TAG = "RecyclerFragmentManageG";
    private GroupController groupController;
    private ArrayList<String> currentGroupsList;    //groupnames
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private ManageGroupsAdapter adapter;


    public RecyclerFragmentManageGroups() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_recycler_fragment_manage_groups, container, false);
        currentGroupsList = new ArrayList<>();
        initRecycler(view);
        return view;
    }

    private void initRecycler(View view) {
        this.recyclerView = view.findViewById(R.id.recycler_view_groups);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        this.recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        this.mLayoutManager = new LinearLayoutManager(getActivity());
        this.recyclerView.setLayoutManager(mLayoutManager);
        // specify an adapter. MainAdapter is a selfwritten class.
        adapter = new ManageGroupsAdapter(groupController, currentGroupsList);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClicked(int position) {
                //group in list clicked!
                Log.d(TAG, "onItemClicked: " + currentGroupsList.get(position));
                //groupController.incomeListClicked(mArrayList.get(position));
            }
        });
    }

    public void setGroupController(GroupController groupController){
        this.groupController = groupController;
    }

    public void updateList(ArrayList<String> groupList) {
        currentGroupsList.clear();
        currentGroupsList.addAll(groupList);
        adapter.notifyDataSetChanged();
    }

}

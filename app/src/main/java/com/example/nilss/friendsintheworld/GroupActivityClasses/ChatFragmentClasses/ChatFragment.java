package com.example.nilss.friendsintheworld.GroupActivityClasses.ChatFragmentClasses;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.nilss.friendsintheworld.GroupActivityClasses.GroupController;
import com.example.nilss.friendsintheworld.GroupActivityClasses.ManageGroupFragmentClasses.ManageGroupsAdapter;
import com.example.nilss.friendsintheworld.OnCreateViewLIstener;
import com.example.nilss.friendsintheworld.Pojos.Message;
import com.example.nilss.friendsintheworld.Pojos.TextMessage;
import com.example.nilss.friendsintheworld.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {
    private static final String TAG = "ChatFragment";
    static final int REQUEST_TAKE_THUMBNAIL = 1;
    static final int REQUEST_TAKE_PICTURE = 2;
    private GroupController groupController;
    private Button manageGroupsBtn, mapsBtn, sendMessageBtn, sendImagebtn;
    private EditText inputMessageEtv;
    private ImageView takenPhotoIv;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private ChatAdapter adapter;
    private ArrayList<TextMessage> currentMessageList;
    private OnCreateViewLIstener mListener;
    private Uri pictureUri;
    private String mCurrentPhotoPath;

    public void onInit(OnCreateViewLIstener listener){
        this.mListener = listener;
    }


    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_chat, container, false);
        currentMessageList = new ArrayList<>();
        initRecycler(view);
        initComponents(view);
        return view;
    }

    private void initComponents(View view) {
        this.manageGroupsBtn = view.findViewById(R.id.manageGrpBtn);
        manageGroupsBtn.setOnClickListener((View v)->groupController.show(GroupController.groupFragmentTag));
        this.mapsBtn = view.findViewById(R.id.mapsBtn);
        this.sendMessageBtn = view.findViewById(R.id.sendMsgbtn);
        this.sendImagebtn = view.findViewById(R.id.sendImageBtn);
        this.inputMessageEtv = view.findViewById(R.id.inputMsgetv);
        this.takenPhotoIv = view.findViewById(R.id.capturedPhotoIv);
        sendImagebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_TAKE_PICTURE && resultCode== Activity.RESULT_OK) {
            takenPhotoIv.setImageBitmap(getScaled(mCurrentPhotoPath,takenPhotoIv.getMaxWidth(),takenPhotoIv.getMaxHeight()));
            takenPhotoIv.setRotation(90);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.d(TAG, "dispatchTakePictureIntent: Exception: " + ex);
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getActivity(),
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PICTURE);
            }
        }
    }

    private Bitmap getScaled(String pathToPicture, int targetW, int targetH) {
        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathToPicture, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        Bitmap bitmap = BitmapFactory.decodeFile(pathToPicture, bmOptions);
        return bitmap;
    }

    private void initRecycler(View view) {
        this.recyclerView = view.findViewById(R.id.chatRecyclerView);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        this.recyclerView.setHasFixedSize(true);
        // use a linear layout manager
        this.mLayoutManager = new LinearLayoutManager(getActivity());
        this.recyclerView.setLayoutManager(mLayoutManager);
        adapter = new ChatAdapter(groupController, currentMessageList);
        recyclerView.setAdapter(adapter);
    }


    public void setGroupController(GroupController groupController){
        this.groupController = groupController;
    }

    public void updateList(ArrayList<TextMessage> messageList) {
        currentMessageList.clear();
        currentMessageList.addAll(messageList);
        adapter.notifyDataSetChanged();
    }

}

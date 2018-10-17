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
import android.support.v4.graphics.BitmapCompat;
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

import java.io.ByteArrayOutputStream;
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
    private boolean isPictureTaken;
    private Uri pictureUri;
    private String mCurrentPhotoPath;
    private Bitmap mCurrentPhotoBitmap;

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
        mapsBtn.setOnClickListener((View v)->groupController.mapsBtnClicked());

        sendImagebtn.setOnClickListener((View v)->dispatchTakePictureIntent());
        sendMessageBtn.setOnClickListener((View v)->{
            if(isPictureTaken){
                //Log.d(TAG, "initComponents: sending image and text");
                byte[] byteArray = bitmapToByteArray();
                groupController.sendMessage(inputMessageEtv.getText().toString(), byteArray);
            }
            else {
                groupController.sendMessage(inputMessageEtv.getText().toString(), null);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_TAKE_PICTURE && resultCode== Activity.RESULT_OK) {
            isPictureTaken=true;
            mCurrentPhotoBitmap = getScaled(mCurrentPhotoPath,100,100);
            takenPhotoIv.setImageBitmap(mCurrentPhotoBitmap);
            takenPhotoIv.setRotation(90);
        }
    }

    private byte[] bitmapToByteArray(){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        mCurrentPhotoBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }
    public Bitmap byteArrayToBitmap(byte[] byteArray){
        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray , 0, byteArray.length);
        return bitmap;
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
        Log.d(TAG, "createImageFile: "+ mCurrentPhotoPath);
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
        //Check if size is less the 64kb
        int bitmapByteCount= BitmapCompat.getAllocationByteCount(bitmap);
        Log.d(TAG, "getScaled: " + String.valueOf(bitmapByteCount));
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
        adapter = new ChatAdapter(this, groupController, currentMessageList);
        recyclerView.setAdapter(adapter);
    }


    public void setGroupController(GroupController groupController){
        this.groupController = groupController;
    }

    public void updateList(ArrayList<TextMessage> messageList) {
        currentMessageList.clear();
        currentMessageList.addAll(messageList);
        recyclerView.smoothScrollToPosition(currentMessageList.size());
        adapter.notifyDataSetChanged();
    }

}

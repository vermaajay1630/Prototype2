package com.example.prototype2;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    ImageView img;
    Uri uri;
    Button cam, upload;
    StorageReference storageReference;
    private static final int CAM_REQ_CODE = 100;
    String currentPhotoPath;
    ProgressDialog progressDialog;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        img = findViewById(R.id.imageView);
        cam = findViewById(R.id.camBtn);
        upload = findViewById(R.id.uploadBtn);

        checkCamPermission();

        cam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });



    }

    private void uploadImage() {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("image");
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading");
        progressDialog.show();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd", Locale.getDefault());
        Date now =  new Date();
        String filename =dateFormat.format(now);
        storageReference = FirebaseStorage.getInstance().getReference("images/"+filename);
        storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        image_model  model =new image_model(uri.toString());
                        String modelId = db.push().getKey();
                        db.child(modelId).setValue(model);
                    }
                });
            }
        });
    }

    private void checkCamPermission() {
if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
openCamera();
}else{
    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAM_REQ_CODE);
}
    }

    private void openCamera() {
        String filename = "photo";
        File filestorage = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = null;
        try {
            Intent cam = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            imageFile =File.createTempFile(filename, ".jpg", filestorage);
            Uri imageUri = FileProvider.getUriForFile(MainActivity.this, "com.example.prototype2.fileProvider", imageFile);
            currentPhotoPath = imageFile.getAbsolutePath();
            cam.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(cam, CAM_REQ_CODE);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CAM_REQ_CODE && resultCode == RESULT_OK){
            Bitmap bm = BitmapFactory.decodeFile(currentPhotoPath);
            img.setImageBitmap(bm);
            String path = MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), bm, "val", null);
            uri = Uri.parse(path);
        }
    }
}
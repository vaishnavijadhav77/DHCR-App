package com.example.major_2;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContentProviderCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.major_2.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageCropper extends AppCompatActivity {
    ProgressDialog pd;
    ActivityResultLauncher<Intent>startCamera;
    Button b;
    Uri resultUri = null;
    Uri cam_uri=null;
    ImageView pick;
    ActivityResultLauncher<String> mGetContent;
    ActivityMainBinding binding;
    Uri imageUri ;
    StorageReference s;
    Uri filePath;
    Button b2;
    String mCurrentPhotoPath;
    public  static final int RequestPermissionCode  = 1 ;
    private StorageReference mStorageRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_cropper);
        b=findViewById(R.id.button);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        pick = findViewById(R.id.imageView2);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });


        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {

                Intent intent = new Intent(ImageCropper.this,CropperActivity.class);
                intent.putExtra("DATA",result.toString());
                startActivityForResult(intent,101);



            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == -1 && requestCode == 101) {
            String result = data.getStringExtra("RESULT");

            if (result != null) {
                resultUri = Uri.parse(result);
            }

            pick.setImageURI(resultUri);
        }
        if (requestCode == 7 && resultCode == RESULT_OK) {

            Bitmap bitmap = (Bitmap) data.getExtras().get("data");

            pick.setImageBitmap(bitmap);
            String result = data.getStringExtra("data");

            if (result != null) {
                resultUri = Uri.parse(result);
            }

            pick.setImageURI(resultUri);
        }


    }

    private void uploadImage()
    {  pd= new ProgressDialog(this);
        pd.setTitle("Uploading File....");
        pd.show();
        SimpleDateFormat format =new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.CANADA);
        Date now=new Date();
        String filen=format.format(now);

        s= FirebaseStorage.getInstance().getReference("images/"+filen);
        s.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                binding.imageView.setImageURI(null);
                Toast.makeText(ImageCropper.this,"Successfully Uploaded",Toast.LENGTH_SHORT).show();

                if(pd.isShowing())
                {
                    pd.dismiss();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ImageCropper.this,"Fail to Upload",Toast.LENGTH_SHORT).show();
                if(pd.isShowing())
                {
                    pd.dismiss();
                }
            }
        });
    }



    public void selectImage(View view) {
        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(ImageCropper.this);
        builder.setTitle("Add Photo");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo"))
                {     EnableRuntimePermission();

                    //cam_uri = getBaseContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, cam_uri);
                   startActivityForResult(intent, 7);
                 //   mGetContent.launch("image/*");



                }
                else if (options[item].equals("Choose from Gallery"))
                {//Intent i=new Intent(ImageCropper.this,ImageCropper.class);
                   // startActivity(i);
                    mGetContent.launch("image/*");

                }
                else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    public void EnableRuntimePermission(){

        if (ActivityCompat.shouldShowRequestPermissionRationale(ImageCropper.this,
                Manifest.permission.CAMERA))
        {

            Toast.makeText(ImageCropper.this,"CAMERA permission allows us to Access CAMERA app", Toast.LENGTH_LONG).show();

        } else {

            ActivityCompat.requestPermissions(ImageCropper.this,new String[]{
                    Manifest.permission.CAMERA}, RequestPermissionCode);

        }
    }

    @Override
    public void onRequestPermissionsResult(int RC, String per[], int[] PResult) {

        super.onRequestPermissionsResult(RC, per, PResult);
        switch (RC) {

            case RequestPermissionCode:

                if (PResult.length > 0 && PResult[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(ImageCropper.this, "Permission Granted, Now your application can access CAMERA.", Toast.LENGTH_LONG).show();

                } else {

                    Toast.makeText(ImageCropper.this, "Permission Canceled, Now your application cannot access CAMERA.", Toast.LENGTH_LONG).show();

                }
                break;
        }
    }

}

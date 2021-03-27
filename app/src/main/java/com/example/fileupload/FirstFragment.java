package com.example.fileupload;


import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.loader.content.CursorLoader;

import com.example.fileupload.Retrofit.Api;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.app.Activity.RESULT_OK;

public class FirstFragment extends Fragment {

    private TextView mTextviewFirst;
    private ImageView mImgPhoto;
    private EditText mTxtDesc;
    private LinearLayout mLinearL;
    private Button mButtonFirst;
    private Button mButtonSec;

    ProgressDialog progressDialog;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Please wait...");

        mTextviewFirst = view.findViewById(R.id.textview_first);
        mImgPhoto = view.findViewById(R.id.imgPhoto);
        mTxtDesc = view.findViewById(R.id.txtDesc);
        mLinearL = view.findViewById(R.id.linearL);
        mButtonFirst = view.findViewById(R.id.button_first);
        mButtonSec = view.findViewById(R.id.button_sec);

        mButtonFirst.setOnClickListener(v -> {
            if (TextUtils.isEmpty(mTxtDesc.getText().toString())) {
                mTxtDesc.setError("required*");
                return;
            }else {
                //opening file chooser
                openFileChooser();
//                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                startActivityForResult(i, 100);
            }

        });
        mButtonSec.setOnClickListener(v -> {
//            uploadFile(selectedImage, desc);
        });

    }

    public void openFileChooser() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, 100);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            //the image URI
            Uri selectedImage = data.getData();
            String desc = mTxtDesc.getText().toString().trim();
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), selectedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mImgPhoto.setImageBitmap(bitmap);

            //calling the upload file method after choosing the file
            uploadFile(selectedImage, desc);
        }
    }

    private void uploadFile(Uri fileUri, String desc) {
        progressDialog.show();
        //creating a file
        File file = new File(getRealPathFromURI(fileUri));

        //creating request body for file
        RequestBody requestFile = RequestBody.create(MediaType.parse(getContext().getContentResolver().getType(fileUri)), file);
        RequestBody descBody = RequestBody.create(MediaType.parse("text/plain"), desc);


        Gson gson = new GsonBuilder()
                .setLenient()
                .create();


        //creating retrofit object
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Api.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        //creating our api
        Api api = retrofit.create(Api.class);
        Call<MyResponse> call = api.uploadImage(requestFile, descBody);
        call.enqueue(new Callback<MyResponse>() {
            @Override
            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                progressDialog.dismiss();
                assert response.body() != null;
                if (!response.body().error) {
                    Toast.makeText(getContext(), "File Uploaded Successfully...", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(), "Error Uploading File...", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<MyResponse> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_LONG).show();

            }
        });

    }

    private void validate() {
        if (TextUtils.isEmpty(mTxtDesc.getText().toString())) {
            mTxtDesc.setError("required*");
            return;
        }
    }

    private String getRealPathFromURI(Uri contentUri) {
        String result = "";
        if (contentUri == null) {
            Toast.makeText(getContext(), "Select Photo", Toast.LENGTH_SHORT).show();

        } else {
            String[] proj = {MediaStore.Images.Media.DATA};
            CursorLoader loader = new CursorLoader(getContext(), contentUri, proj, null, null, null);
            Cursor cursor = loader.loadInBackground();
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            result = cursor.getString(column_index);
            cursor.close();

        }
        return result;
    }


}
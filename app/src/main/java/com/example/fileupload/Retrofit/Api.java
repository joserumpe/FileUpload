package com.example.fileupload.Retrofit;

import com.example.fileupload.MyResponse;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface Api {
    String BASE_URL = "https://66c3b77d432e.ngrok.io/fileupload/";

    @Multipart
    @POST("Api.php?apicall=upload")
    Call<MyResponse> uploadImage(
            @Part("image\"; filename=\"myfile.jpg\" ") RequestBody file,
            @Part("desc") RequestBody desc
    );
}

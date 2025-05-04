package com.example.onlinecompiler.network;

import com.example.onlinecompiler.models.CompileRequest;
import com.example.onlinecompiler.models.CompileResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    @Headers("Content-Type: application/json")
    @POST("/api/compiler/execute")
    Call<CompileResponse> compileCode(@Body CompileRequest request);

    @Multipart
    @POST("/api/compiler/upload")
    Call<CompileResponse> uploadCode(
            @Part MultipartBody.Part file,
            @Part("language") RequestBody language,
            @Part("stdin") RequestBody stdin
    );
}

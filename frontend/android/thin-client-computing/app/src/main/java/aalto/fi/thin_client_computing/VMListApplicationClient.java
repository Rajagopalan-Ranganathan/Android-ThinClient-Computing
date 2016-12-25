package aalto.fi.thin_client_computing;


import android.app.DownloadManager;
import android.content.Context;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;



public class VMListApplicationClient {
    public static final String API_BASE_URL = "";



    private VMListApplicationService VMListApplicationService;
    public VMListApplicationClient(Context context, final String token) {

        OkHttpClient client = new OkHttpClient();
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        //Create the interceptor to edit the Header field , since we need the token to communicate
        //with beackend - After the login auth is success use the same token received to communicate further
        builder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request req = chain.request().newBuilder().addHeader("Authorization", token).build();
                return chain.proceed(req);

            }
        });

        client = builder.build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(context.getString(R.string.server_url))
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        VMListApplicationService = retrofit.create(VMListApplicationService.class);
    }

    public VMListApplicationService getVMListApplicationService() {
        return VMListApplicationService;
    }
}






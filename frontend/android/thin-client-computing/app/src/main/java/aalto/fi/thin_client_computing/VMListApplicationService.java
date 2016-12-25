package aalto.fi.thin_client_computing;


import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;


public interface VMListApplicationService {

    //Different API endpoints of the backend

    @POST("/api/users/auth")
    Call<UserLogin> auth(@Body UserLogin userLoginData);
    @GET("/api/users/vms")
    Call<List<VMListApp>> getVms();
    @POST("/api/users/vms/start")
    Call<VMInstance> start(@Body VMInstance instance);
    @POST("/api/users/vms/stop")
    Call<VMInstance> stop(@Body VMInstance instance);

}







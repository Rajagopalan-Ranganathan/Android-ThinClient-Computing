package aalto.fi.thin_client_computing;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VMListActivity extends AppCompatActivity {

    private List<VMListApp> VMListApps;

    private RecyclerView rv;
    private VMListCardViewAdapter adapter;
    private boolean isAtTBuilding = false;
    private LocationTracker mLocationTracker;
    private String token;
    private VMListApplicationService appService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_vmlist);
        setTitle("Applications");

        rv=(RecyclerView)findViewById(R.id.rv);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);


        mLocationTracker = LocationTracker.getInstance(this.getApplicationContext());

        initializeData();

        // Get intent, action and MIME type
        // This will be the case if there is an Intent from MultiVNC
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/multivncdisconnect".equals(type)) {
                Log.i("VMListActivity", "MultiVNC disconnected!");

                //Get the name of the started vm, to stop it
                if(intent.hasExtra("name")){
                    String name = intent.getStringExtra("name");
                    if (name.length() > 0){
                        stopVM(name);
                    }
                }
            }
        }

    }

    private void initializeData(){

        try {
            //Get the data passed from the login activity i.e : Toen
            Bundle extras= getIntent().getExtras();
            token = "";

            //Get token from intent from LoginActivity
            // The Header for this request should be like "Bearer "token""
            if(extras.containsKey("token")) {
                token = "Bearer " + extras.getString("token");

                //Save token in SharedPreferences
                SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("token", token);
                editor.apply();
            }

            //No token in intent, probably intent is from multivnc
            //Read token from sharedPreferences
            else{
                SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
                //TODO check if token is in sharedPreferences else go back to LoginActivity
                token = sharedPref.getString("token", "");
            }

            //Try to get current location
            if(mLocationTracker.getCurrentLocation() != null) {
                Log.d("Location", "Current location lat: " + mLocationTracker.getCurrentLocation().getLatitude() + " lon: " + mLocationTracker.getCurrentLocation().getLongitude());

                //Check if the location is near/in T-building
                Location tBuilding = new Location("T-building");
                tBuilding.setLatitude(60.186950);
                tBuilding.setLongitude(24.821426);
                if (mLocationTracker.getCurrentLocation().distanceTo(tBuilding) < 80) {
                    Log.d("Location Changes", "At T-building! distance: " + mLocationTracker.getCurrentLocation().distanceTo(tBuilding));
                    isAtTBuilding = true;
                } else {
                    Log.d("Location Changes", "Not near T-building");
                }

            }
            else{
                Log.d("Location", "Currently no location available");
            }

            //Communicate with the backend to retrieve a list of applications and assign to the VMList
            appService = new VMListApplicationClient(getApplicationContext(),token).getVMListApplicationService();
            Call<List<VMListApp>> respListVms = appService.getVms();

            respListVms.enqueue(new Callback<List<VMListApp>>() {

                @Override
                public void onResponse(Call<List<VMListApp>> call, Response<List<VMListApp>> response) {
                    try{
                        VMListApps = response.body();

                        //Check if List is not null
                        if(VMListApps == null){
                            //init as empty list
                            VMListApps = new ArrayList<VMListApp>();
                        }
                        //Find the openoffice vm in the list
                        VMListApp openoffice = null;
                        for (VMListApp app : VMListApps) {
                            if (app.getName().toLowerCase().contains("openoffice")) {
                                openoffice = app;
                            }
                        }

                        //If openoffice is found add it at the top of the list if in T-building
                        //else at the bottom
                        if (openoffice != null) {
                            VMListApps.remove(openoffice);
                            if (isAtTBuilding) {
                                VMListApps.add(0, openoffice);
                            } else {
                                VMListApps.add(openoffice);
                            }
                        }

                        Log.d("resp:", response.code() + "");
                        rv.setAdapter(new VMListCardViewAdapter(getApplicationContext(), VMListApps, VMListActivity.this));
                    }
                    catch(NullPointerException ex) {
                        Log.e("BackendConnection", "Response from backend is null");
                    }
                }

                @Override
                public void onFailure(Call<List<VMListApp>> call, Throwable t) {
                    // Log error here since request failed
                    Log.e("Error", t.toString());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();

        }


    }

    //This is called when the connection within multivnc is disconnected by the user
    //Get the name of the opend vm and stop it
    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data){
        Log.i("ActivityResult", "VNC viewer disconnected! requestCode: "+requestCode+" resultCode: "+resultCode);

        //Request code is the index of the started vm
        String name = VMListApps.get(requestCode).getName();
        stopVM(name);
    }

    /**
     *
     * Connect to a vm via multivnc
     * @param url
     * @param password
     * @param listIndex
     * @param name
     */
    public void connectVNC(String url, String password, int listIndex, String name){
        Intent launchIntent = new Intent();

        //Build url
        //something like vnc://ip:port/coloremode/password
        Uri.Builder builder = new Uri.Builder();
        builder.authority(url);
        builder.scheme("vnc");
        builder.appendPath("C64");
        if(password.length() > 0){
            builder.appendPath(password);
        }
        launchIntent.setData(builder.build());

        //Put name of started vm as extra
        launchIntent.putExtra("name", name);

        Log.d("VNC Connection", "Start vnc connection: "+launchIntent.getData().toString());

        // Verify that the intent will resolve to an activity
        if (launchIntent.resolveActivity(getPackageManager()) != null) {
            //RequestCode can be chosen, here item in the list
            startActivityForResult(launchIntent, listIndex);
        }
        else{
            Log.e("VNC Connection", "No valid activity for intent found " + launchIntent);
        }

    }

    /**
     * Start a vm in the backend with the provided name
     * @param name of the vm
     */
    public void startVM(String name){
        if(appService != null) {
            Call<VMInstance> c = appService.start(new VMInstance(name));
            c.enqueue(new Callback<VMInstance>() {
                @Override
                public void onResponse(Call call, Response response) {
                    Log.d("startVM", "VM start response code: " + response.code());
                }

                @Override
                public void onFailure(Call call, Throwable t) {
                    Log.d("startVM", "Error starting VM: " + t.getMessage());
                }
            });
        }
    }

    /**
     * Stop a vm at the backend
     * @param name of the vm
     */
    public void stopVM(String name){
        Log.d("stopVM", "Try to stop VM: "+name);
        if(appService != null) {
            Call<VMInstance> c = appService.stop(new VMInstance(name));
            c.enqueue(new Callback<VMInstance>() {
                @Override
                public void onResponse(Call call, Response response) {
                    Log.d("stopVM", "VM stop response code: " + response.code());
                }

                @Override
                public void onFailure(Call call, Throwable t) {
                    Log.d("stopVM", "Error stopping VM: " + t.getMessage());
                }
            });
        }
    }


}

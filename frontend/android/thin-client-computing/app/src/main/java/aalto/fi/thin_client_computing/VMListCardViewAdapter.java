package aalto.fi.thin_client_computing;



import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

public class VMListCardViewAdapter extends RecyclerView.Adapter<VMListCardViewAdapter.RemoteCardReclyerViewHolder> {

    public static class RemoteCardReclyerViewHolder extends RecyclerView.ViewHolder {


        CardView cardView;
        TextView remoteAppName;
        TextView remoteAppDesc;
        ImageView remoteAppIcon;

        RemoteCardReclyerViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView)itemView.findViewById(R.id.cv);
            remoteAppName = (TextView)itemView.findViewById(R.id.app_name);
            remoteAppDesc = (TextView)itemView.findViewById(R.id.app_desc);
            remoteAppIcon = (ImageView)itemView.findViewById(R.id.icon);
        }
    }

    Context vmContext;
    List<VMListApp> VMListApps;
    VMListActivity vmListActivity;

    VMListCardViewAdapter(Context vmContext, List<VMListApp> VMListApps, VMListActivity vmListActivity){
        this.vmContext = vmContext;
        this.VMListApps = VMListApps;
        this.vmListActivity = vmListActivity;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public RemoteCardReclyerViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item, viewGroup, false);
        RemoteCardReclyerViewHolder pvh = new RemoteCardReclyerViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(RemoteCardReclyerViewHolder remoteAppViewHolder, final int i) {

        //The name from the back end is x-y-applicationName, so we need to split based on "-" and then get the applicaiton name
        String[] separated = VMListApps.get(i).getName().split("-");

        // Take the last string that was a result of the split-  last string is the applicaiton name
        String name = separated[separated.length -1];
        //First letter uppercase
        if(name.length()>1) {
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
        }
        remoteAppViewHolder.remoteAppName.setText(name);

        //Set description
        remoteAppViewHolder.remoteAppDesc.setText(VMListApps.get(i).getDesc());

        Glide.with(vmContext)
                .load(VMListApps.get(i).getImg())
                .placeholder(R.drawable.ic_launcher)
                .crossFade()
                .into(remoteAppViewHolder.remoteAppIcon);

        remoteAppViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("VMListItemClicked","Remote Application " + VMListApps.get(i).getName()+" Status: "+VMListApps.get(i).getSts()+" URL: "+VMListApps.get(i).getAddress());
                //Start VM if not running
                //if(!VMListApps.get(i).getSts().toUpperCase().contains("RUNNING")){
                    vmListActivity.startVM(VMListApps.get(i).getName());
                //}
                //Connect to the VNC
                vmListActivity.connectVNC(VMListApps.get(i).getAddress()+":5901", "mcc2016", i, VMListApps.get(i).getName());
            }
       }) ;
    }

   @Override
    public int getItemCount() {
      return VMListApps.size();
        }
}

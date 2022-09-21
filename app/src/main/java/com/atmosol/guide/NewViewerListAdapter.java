package com.atmosol.guide;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


public class NewViewerListAdapter extends RecyclerView.Adapter<NewViewerListAdapter.myViewHolder>{
    ArrayList<User> NewViewerList= new ArrayList<User>();
    Context context;
    SearchListener clicklistener;

    public NewViewerListAdapter(ArrayList<User> arrayList, Context context, SearchListener clicklistener) {
        this.NewViewerList = arrayList;
        this.context = context;
        this.clicklistener = clicklistener;
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.newviewerlist_adapter, parent, false);

        return new myViewHolder(view);
    }

    @Override

    public void onBindViewHolder(@NonNull myViewHolder holder, int position) {

        holder.tv_title.setText(NewViewerList.get(position).name);

        holder.sendSDP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                clicklistener.onSENDSDPclick(NewViewerList.get(position));

            }
        });
    }

    @Override
    public int getItemCount() {
        return NewViewerList.size();


    }

    public class myViewHolder extends RecyclerView.ViewHolder {
        TextView tv_title,sendSDP;
      //  LinearLayout ll_click ;
      
        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_title= itemView.findViewById(R.id.tv_title);
            sendSDP= itemView.findViewById(R.id.sendSDP);


        }
    }
    public interface SearchListener {

        void onSENDSDPclick(User user);


    }
}

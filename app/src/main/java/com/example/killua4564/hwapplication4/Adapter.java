package com.example.killua4564.hwapplication4;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    private Context context;
    private DatabaseReference database;
    private ArrayList<Item> items = new ArrayList<Item>();

    public Adapter(Context context) {
        this.context = context;
        this.database = FirebaseDatabase.getInstance().getReference();
    }

    public void updateItem(ArrayList<Item> items){
        this.items = items;
        this.notifyDataSetChanged();
    }

    public String deleteItem(int index){
        Item item = items.get(index);
        this.database.child(item.getId()).removeValue();
        return item.getId();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(this.context);
        View view = inflater.inflate(R.layout.list_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        viewHolder.bind(position);
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private CheckBox openKey;
        private TextView classTextView;
        private TextView weekTextView;
        private TextView timeTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            this.openKey = (CheckBox) this.itemView.findViewById(R.id.openKey);
            this.classTextView = (TextView) this.itemView.findViewById(R.id.classTextView);
            this.weekTextView = (TextView) this.itemView.findViewById(R.id.weekTextView);
            this.timeTextView = (TextView) this.itemView.findViewById(R.id.timeTextView);
        }

        void bind(final int index){
            final Item item = items.get(index);
            this.openKey.setChecked(item.getKey());
            this.classTextView.setText(item.getClassname());
            this.weekTextView.setText(item.getWeek());
            this.timeTextView.setText(String.valueOf(item.getHour()) + ":" + String.valueOf(item.getMinute()));
            this.openKey.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){

                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    item.setKey(isChecked);
                    database.child(item.getId()).setValue(item.toMap());
                }
            });
        }
    }
}
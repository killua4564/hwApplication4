package com.example.killua4564.hwapplication4;

import android.app.Dialog;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;
import com.google.android.gms.common.util.ArrayUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Adapter adapter;
    private int itemIndex = 0;
    private RecyclerView recyclerView;
    private DatabaseReference database;
    private FirebaseJobDispatcher jobDispatcher;
    private FirebaseJobDispatcher jobDispatcherNext;
    private FloatingActionButton floatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        this.adapter = new Adapter(this);
        this.database = FirebaseDatabase.getInstance().getReference();
        this.database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Item> items = new ArrayList<Item>();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    Map<String, Object> itemMap = (Map<String, Object>) userSnapshot.getValue();
                    String id = userSnapshot.getKey();
                    String classname = (String) itemMap.get("classname");
                    String week = (String) itemMap.get("week");
                    int hour = Integer.parseInt((String) itemMap.get("hour"));
                    int minute = Integer.parseInt((String) itemMap.get("minute"));
                    boolean key = (boolean) itemMap.get("key");
                    Item item = new Item(id, classname, week, hour, minute, key);
                    items.add(item);
                    itemIndex = Math.max(Integer.parseInt(id) + 1, itemIndex);
                }
                adapter.updateItem(items);
                FirebaseJobService.updateItem(items);
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
        this.recyclerView = (RecyclerView) this.findViewById(R.id.recyclerView);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        this.recyclerView.setAdapter(this.adapter);
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                jobDispatcher.cancel(adapter.deleteItem(viewHolder.getAdapterPosition()));
            }

        }).attachToRecyclerView(this.recyclerView);
        this.jobDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
        this.jobDispatcherNext = new FirebaseJobDispatcher(new GooglePlayDriver(this));
        this.floatingActionButton = (FloatingActionButton) findViewById(R.id.floatActionButton);
        this.floatingActionButton.setOnClickListener(new View.OnClickListener() {

            private EditText editText;
            private Spinner weekSpinner;
            private TimePicker timePicker;
            private Button button;

            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.activity_dialog);
                final String weeks[] = new String[]{"SunDay", "Monday", "TuesDay", "WednesDay", "ThursDay", "FriDay", "SaturDay"};
                this.editText = (EditText) dialog.findViewById(R.id.editText);
                this.weekSpinner = (Spinner) dialog.findViewById(R.id.weekSpinner);
                this.weekSpinner.setAdapter(new ArrayAdapter<>(dialog.getContext(), android.R.layout.simple_spinner_dropdown_item, weeks));
                this.timePicker = (TimePicker) dialog.findViewById(R.id.timePicker);
                this.button = (Button) dialog.findViewById(R.id.button);
                this.button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        String classname = editText.getText().toString();
                        String week = weekSpinner.getSelectedItem().toString();
                        int hour = timePicker.getHour();
                        int minute = timePicker.getMinute();
                        Item item = new Item(String.valueOf(itemIndex), classname, week, hour, minute, true);
                        database.child(String.valueOf(itemIndex)).setValue(item.toMap());
                        Calendar now = Calendar.getInstance();
                        int nowDayOfWeek = now.get(Calendar.DAY_OF_WEEK);
                        int nowHour = now.get(Calendar.HOUR_OF_DAY);
                        int nowMinute = now.get(Calendar.MINUTE);
                        int dayOfWeek = ArrayUtils.indexOf(weeks, week) + 1;
                        int totalSecond = (((dayOfWeek - nowDayOfWeek) * 24 + (hour - nowHour)) * 60 + (minute - nowMinute - 5)) * 60;
                        if (totalSecond < 0) totalSecond += 7 * 24 * 60 * 60;
                        Bundle bundle = new Bundle();
                        bundle.putString("id", item.getId());
                        bundle.putString("classname", item.getClassname());
                        jobDispatcher.schedule(jobDispatcher.newJobBuilder()
                                .setService(FirebaseJobService.class)
                                .setTag("this_is_the_tag")
                                .setConstraints(Constraint.DEVICE_CHARGING)
                                .setLifetime(Lifetime.FOREVER)
                                .setRecurring(true)
                                .setTrigger(Trigger.executionWindow(0, totalSecond))
                                .setReplaceCurrent(false)
                                .setExtras(bundle)
                                .build()
                        );
                        int totalSecondNext = totalSecond + 7 * 24 * 60 * 60;
                        jobDispatcherNext.schedule(jobDispatcherNext.newJobBuilder()
                                .setService(FirebaseJobService.class)
                                .setTag("this_is_the_next_tag")
                                .setConstraints(Constraint.DEVICE_CHARGING)
                                .setLifetime(Lifetime.FOREVER)
                                .setRecurring(true)
                                .setTrigger(Trigger.executionWindow(7 * 24 * 60 * 60, totalSecondNext))
                                .setReplaceCurrent(false)
                                .setExtras(bundle)
                                .build()
                        );
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });
    }
}
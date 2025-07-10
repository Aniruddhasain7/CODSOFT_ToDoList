package com.example.todolist;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView date;
    private ListView tasks_list;
    private Switch done_tasks;
    private LocalDate liveDate;
    private DateTimeFormatter shortDate;
    private DateTimeFormatter longDate;
    private Database database;
    private boolean oldTasksGenerated = false;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        date = findViewById(R.id.date);
        tasks_list = findViewById(R.id.tasks_list);
        done_tasks = findViewById(R.id.done_tasks);
        LinearLayout add = findViewById(R.id.add);

        shortDate = DateTimeFormatter.ofPattern("yyyy-dd-MM", Locale.UK);
        longDate = DateTimeFormatter.ofPattern("E dd, LLL yyyy", Locale.UK);
        liveDate = LocalDate.now();
        database = new Database(this);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (datePicker, year, month, day) -> {
            liveDate = LocalDate.of(year, month+1, day);
            oldTasksGenerated = false;
            refreshList();
        }, liveDate.getYear(), liveDate.getMonthValue()-1, liveDate.getDayOfMonth());

        date.setOnClickListener(v-> datePickerDialog.show());

        add.setOnClickListener(v-> {
            Intent intent = new Intent(MainActivity.this, TaskEditor.class);
            intent.putExtra("Date", shortDate.format(liveDate));
            startActivity(intent);
        });

        done_tasks.setOnCheckedChangeListener((compoundButton, checked) -> refreshList());

        refreshList();

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void refreshList() {
        date.setText(liveDate.format(longDate));

        ArrayList<Task> tasks = database.getTasks(shortDate.format(liveDate));

        if (tasks.size()==0 && !oldTasksGenerated) generateDayTasks();

        if (!done_tasks.isChecked()) {
            ArrayList<Task> tempList = new ArrayList<>();
            for (Task t : tasks) {
                if (!t.getStatus().equals("Done")) tempList.add(t);
            }
            tasks_list.setAdapter(new ListAdapter(tempList));
        } else {
            tasks_list.setAdapter(new ListAdapter(tasks));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void generateDayTasks() {
        oldTasksGenerated = true;
        ArrayList<Task> prevDayTasks = database.getTasks(shortDate.format(liveDate.minusDays(1)));
        ArrayList<Task> tasks = new ArrayList<>();
        for (Task t : prevDayTasks) {
            if (!t.getStatus().equals("Done")) {
                if (t.getDueDate().isBefore(liveDate)) t.setStatus("Delayed");
                database.insertTask(shortDate.format(liveDate), t);
                tasks.add(t);
            }
        }

        if (!done_tasks.isChecked()) {
            ArrayList<Task> tempList = new ArrayList<>();
            for (Task t : tasks) {
                if (!t.getStatus().equals("Done")) tempList.add(t);
            }
            tasks_list.setAdapter(new ListAdapter(tempList));
        } else {
            tasks_list.setAdapter(new ListAdapter(tasks));
        }
    }

    public class ListAdapter extends BaseAdapter {

        private final ArrayList<Task> tasks;

        public ListAdapter(ArrayList<Task> tasks) {
            this.tasks = tasks;
        }

        @Override
        public int getCount() {
            return tasks.size();
        }

        @Override
        public Task getItem(int i) {
            return tasks.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            @SuppressLint({"ViewHolder", "InflateParams"})
            View v = getLayoutInflater().inflate(R.layout.task_listitem, null);

            LinearLayout parent_layout = v.findViewById(R.id.parent_layout);
            ImageView circle = v.findViewById(R.id.circle);
            TextView task_title = v.findViewById(R.id.task_title);
            TextView task_description = v.findViewById(R.id.task_description);
            TextView status = v.findViewById(R.id.status);
            TextView date = v.findViewById(R.id.date);

            Task task  = tasks.get(i);

            if (task.getStatus()!=null && !task.getStatus().equals("") && !task.getStatus().equals("null")) {
                switch (task.getStatus()) {
                    case "Active":
                        circle.setImageResource(R.drawable.green_circle);
                        status.setBackgroundResource(R.drawable.green_box);
                        status.setText("Active");
                        status.setTextColor(getResources().getColor(R.color.green_dark));
                        break;
                    case "Done":
                        circle.setImageResource(R.drawable.blue_circle);
                        status.setBackgroundResource(R.drawable.blue_box);
                        status.setText("Done");
                        status.setTextColor(getResources().getColor(R.color.blue_dark));
                        break;
                    case "Delayed":
                        circle.setImageResource(R.drawable.red_circle);
                        status.setBackgroundResource(R.drawable.red_box);
                        status.setText("Delayed");
                        status.setTextColor(getResources().getColor(R.color.red_dark));
                        break;
                }
            } else {
                status.setVisibility(View.GONE);
            }

            task_title.setText(task.getTitle());
            if (!task.getDescription().equals("") && task.getDescription()!=null) {
                task_description.setVisibility(View.VISIBLE);
                task_description.setText(task.getDescription());
            }
            if (task.isPriority()) parent_layout.setBackgroundResource(R.drawable.priority_box);

            date.setText(task.getDueDateToString());

            v.setOnLongClickListener(v1-> {
                Intent intent = new Intent(MainActivity.this, TaskEditor.class);
                intent.putExtra("ID", task.getID());
                intent.putExtra("Date", shortDate.format(liveDate));
                startActivity(intent);
                return true;
            });

            return v;
        }
    }

}
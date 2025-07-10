package com.example.todolist;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class TaskEditor extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_editor);

        EditText title = findViewById(R.id.title);
        EditText description = findViewById(R.id.description);
        TextView due_date = findViewById(R.id.due_date);
        CheckBox priority = findViewById(R.id.priority);
        RadioGroup status = findViewById(R.id.status);
        RadioButton active = findViewById(R.id.active);
        RadioButton done = findViewById(R.id.done);
        RadioButton delayed = findViewById(R.id.delayed);
        RadioButton none = findViewById(R.id.none);
        Button submit = findViewById(R.id.submit);
        TextView delete = findViewById(R.id.delete);
        Database database = new Database(this);
        DateTimeFormatter longDate = DateTimeFormatter.ofPattern("E dd, LLL yyyy", Locale.UK);
        LocalDate liveDate = LocalDate.now();
        String date = getIntent().getStringExtra("Date");

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (datePicker, year, month, day) -> {
            LocalDate selectedDate = LocalDate.of(year, month+1, day);
            due_date.setText(selectedDate.format(longDate));
        }, liveDate.getYear(), liveDate.getMonthValue()-1, liveDate.getDayOfMonth());

        due_date.setOnClickListener(v-> datePickerDialog.show());

        if (getIntent().hasExtra("ID")) {
            delete.setVisibility(View.VISIBLE);
            int ID = getIntent().getIntExtra("ID", -1);
            Task task = database.readTask(date, ID);

            title.setText(task.getTitle());
            description.setText(task.getDescription());
            due_date.setText(task.getDueDateToString());
            priority.setChecked(task.isPriority());
            switch (task.getStatus()) {
                case "Active":
                    active.setChecked(true);
                    break;
                case "Done":
                    done.setChecked(true);
                    break;
                case "Delayed":
                    delayed.setChecked(true);
                    break;
                default:
                    none.setChecked(true);
                    break;
            }

            submit.setOnClickListener(v-> {
                if (title.getText().toString().equals("")) {
                    title.setError("Please enter title");
                    return;
                }
                if (due_date.getText().toString().equals("")) {
                    due_date.setError("Please select date");
                    return;
                }
                if (status.getCheckedRadioButtonId()==-1) {
                    Toast.makeText(this, "Please select task status", Toast.LENGTH_SHORT).show();
                    return;
                }

                task.setTitle(title.getText().toString());
                task.setDescription(description.getText().toString());
                task.setDueDate(due_date.getText().toString());
                task.setPriority(priority.isChecked());
                RadioButton radioButton = findViewById(status.getCheckedRadioButtonId());
                task.setStatus(radioButton.getText().toString());

                database.updateTask(date, task);
                finish();
            });

            delete.setOnClickListener(v-> {
                database.deleteTask(date, ID);
                finish();
            });
        } else {
            submit.setOnClickListener(v-> {
                if (title.getText().toString().equals("")) {
                    title.setError("Please enter title");
                    return;
                }
                if (due_date.getText().toString().equals("")) {
                    due_date.setError("Please select date");
                    return;
                }
                if (status.getCheckedRadioButtonId()==-1) {
                    Toast.makeText(this, "Please select task status", Toast.LENGTH_SHORT).show();
                    return;
                }

                Task task = new Task();
                task.setTitle(title.getText().toString());
                task.setDescription(description.getText().toString());
                task.setDueDate(due_date.getText().toString());
                task.setPriority(priority.isChecked());
                RadioButton radioButton = findViewById(status.getCheckedRadioButtonId());
                task.setStatus(radioButton.getText().toString());

                database.insertTask(date, task);
                finish();
            });
        }

    }
}
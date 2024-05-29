package com.example.smdassignment3;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.Toast;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements EntriesAdapter.OnEntryLongClickListener{

    private RecyclerView recyclerViewEntries;
    private FloatingActionButton fabAddEntry, fabRecycleBin;
    private DatabaseHelper dbHelper;
    private EntriesAdapter entriesAdapter;
    private List<Entry> entryList;

    private int loggedInUserId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Retrieve the user ID from the intent extras
        Intent intent = getIntent();
        if (intent != null) {
            loggedInUserId = intent.getIntExtra("user_id", -1);
        } else {
            Toast.makeText(HomeActivity.this, "User ID not received from intent", Toast.LENGTH_SHORT).show();
            finish(); // Finish the activity if intent is null
        }

        // Check if loggedInUserId is valid (-1 indicates invalid user ID)
        if (loggedInUserId == -1) {
            Toast.makeText(HomeActivity.this, "Invalid User ID received from intent", Toast.LENGTH_SHORT).show();
            finish(); // Finish the activity if user ID is not found
        } else {
            // Continue with your activity initialization using loggedInUserId
            dbHelper = new DatabaseHelper(this);

            recyclerViewEntries = findViewById(R.id.recyclerViewEntries);
            fabAddEntry = findViewById(R.id.fabAddEntry);
            fabRecycleBin = findViewById(R.id.fabRecycleBin);

            entryList = new ArrayList<>();
            entriesAdapter = new EntriesAdapter(this, entryList, this::onEntryLongClick);
            recyclerViewEntries.setLayoutManager(new LinearLayoutManager(this));
            recyclerViewEntries.setAdapter(entriesAdapter);

            fabAddEntry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAddEntryDialog();
                }
            });

            fabRecycleBin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HomeActivity.this, RecycleBinActivity.class);
                    startActivity(intent);
                    finish();
                }
            });


            // Load entries from the database
            loadEntries();
        }

    }


    @Override
    public void onEntryLongClick(int position) {
        showEditOrDeleteDialog(position);
    }

    private void showEditOrDeleteDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Action")
                .setItems(new String[]{"Edit", "Delete"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                showEditDialog(position);
                                break;
                            case 1:
                                deleteEntry(position);
                                break;
                        }
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showEditDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_entry, null);
        final EditText editTextUsername = dialogView.findViewById(R.id.editTextUsername);
        final EditText editTextPassword = dialogView.findViewById(R.id.editTextPassword);
        final EditText editTextUrl = dialogView.findViewById(R.id.editTextUrl);

        // Get the entry object at the specified position
        Entry entry = entryList.get(position);

        // Populate editText fields with current entry details
        editTextUsername.setText(entry.getUsername());
        editTextPassword.setText(entry.getPassword());
        editTextUrl.setText(entry.getUrl());

        builder.setView(dialogView)
                .setTitle("Edit Entry")
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String username = editTextUsername.getText().toString();
                        String password = editTextPassword.getText().toString();
                        String url = editTextUrl.getText().toString();
                        // Update the entry in the database and RecyclerView
                        updateEntry(position, username, password, url);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }



    private void deleteEntry(int position) {
        // Delete the entry from the database and RecyclerView
        Entry entry = entryList.get(position);
        // Insert the entry into the recycle bin
        moveToRecycleBin(entry);
        deleteEntryFromDatabase(entry);
        entryList.remove(position);
        entriesAdapter.notifyItemRemoved(position);
        Toast.makeText(this, "Entry deleted", Toast.LENGTH_SHORT).show();
    }

    private void moveToRecycleBin(Entry entry) {
        // Get a writable database
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create ContentValues to store the entry details
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_DELETED_ENTRY_USERNAME, entry.getUsername());
        values.put(DatabaseHelper.COLUMN_DELETED_ENTRY_PASSWORD, entry.getPassword());
        values.put(DatabaseHelper.COLUMN_DELETED_ENTRY_URL, entry.getUrl());
        values.put(DatabaseHelper.COLUMN_USER_ID, loggedInUserId);

        // Insert the entry into the recycle bin table
        long newRowId = db.insert(DatabaseHelper.TABLE_RECYCLE_BIN, null, values);

        // Check if the insertion was successful
        if (newRowId != -1) {
            // Entry moved to recycle bin successfully
            Toast.makeText(this, "Entry moved to recycle bin", Toast.LENGTH_SHORT).show();
        } else {
            // Error moving entry to recycle bin
            Toast.makeText(this, "Error moving entry to recycle bin", Toast.LENGTH_SHORT).show();
        }

        // Close the database connection
        db.close();
    }

    private void deleteEntryFromDatabase(Entry entry) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String selection = DatabaseHelper.COLUMN_ENTRY_ID + " = ?";
        String[] selectionArgs = {String.valueOf(entry.getId())};

        int count = db.delete(
                DatabaseHelper.TABLE_ENTRIES,
                selection,
                selectionArgs);

        if (count == 0) {
            // Handle deletion failure
            Toast.makeText(this, "Failed to delete entry from database", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateEntry(int position, String username, String password, String url) {
        // Update the entry in the database
        Entry entry = entryList.get(position);
        entryList.get(position).setUsername(username);
        entryList.get(position).setPassword(password);
        entryList.get(position).setUrl(url);

        // Update the entry in the RecyclerView
        entriesAdapter.notifyItemChanged(position);

        // Update the entry in the database
        updateEntryInDatabase(entry);

        Toast.makeText(this, "Entry updated", Toast.LENGTH_SHORT).show();
    }

    private void updateEntryInDatabase(Entry entry) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_ENTRY_USERNAME, entry.getUsername());
        values.put(DatabaseHelper.COLUMN_ENTRY_PASSWORD, entry.getPassword());
        values.put(DatabaseHelper.COLUMN_ENTRY_URL, entry.getUrl());

        String selection = DatabaseHelper.COLUMN_ENTRY_ID + " = ?";
        String[] selectionArgs = {String.valueOf(entry.getId())};

        int count = db.update(
                DatabaseHelper.TABLE_ENTRIES,
                values,
                selection,
                selectionArgs);

        if (count == 0) {
            // Handle update failure
            Toast.makeText(this, "Failed to update entry in database", Toast.LENGTH_SHORT).show();
        }
    }


    private void showAddEntryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Entry");

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_entry, null);
        builder.setView(dialogView);

        final EditText editTextUsername = dialogView.findViewById(R.id.editTextUsername);
        final EditText editTextPassword = dialogView.findViewById(R.id.editTextPassword);
        final EditText editTextUrl = dialogView.findViewById(R.id.editTextUrl);

        // Set positive button (Save)
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String username = editTextUsername.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();
                String url = editTextUrl.getText().toString().trim();

                // Validate input
                if (!username.isEmpty() && !password.isEmpty()) {
                    // Add entry to database
                    addEntry(loggedInUserId, username, password, url);
                    dialog.dismiss();
                } else {
                    Toast.makeText(HomeActivity.this, "Please enter username and password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set negative button (Cancel)
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void loadEntries() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Query the entries table
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_ENTRIES,
                null,
                null,
                null,
                null,
                null,
                null
        );

        // Iterate through the cursor and populate the entryList
        if (cursor != null) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") int entryId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_ENTRY_ID));
                @SuppressLint("Range") String username = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_ENTRY_USERNAME));
                @SuppressLint("Range") String password = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_ENTRY_PASSWORD));
                @SuppressLint("Range") String url = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_ENTRY_URL));

                // Create Entry object and add it to entryList
                Entry entry = new Entry(entryId, username, password, url);
                entryList.add(entry);
            }
            cursor.close();
        }

        // Notify the adapter that the data set has changed
        entriesAdapter.notifyDataSetChanged();
    }

    private void addEntry(int userId, String username, String password, String url) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_ENTRY_USERNAME, username);
        values.put(DatabaseHelper.COLUMN_ENTRY_PASSWORD, password);
        values.put(DatabaseHelper.COLUMN_ENTRY_URL, url);
        values.put(DatabaseHelper.COLUMN_USER_ID, userId);

        // Insert the new entry into the database
        long newRowId = db.insert(DatabaseHelper.TABLE_ENTRIES, null, values);
        if (newRowId != -1) {
            // Entry added successfully
            Entry entry = new Entry((int) newRowId, username, password, url);
            entryList.add(entry);
            entriesAdapter.notifyItemInserted(entryList.size() - 1);
        } else {
            // Error adding entry
            Toast.makeText(HomeActivity.this, "Error adding entry", Toast.LENGTH_SHORT).show();
        }
    }
}

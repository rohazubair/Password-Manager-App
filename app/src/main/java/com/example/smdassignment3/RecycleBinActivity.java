package com.example.smdassignment3;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecycleBinActivity extends AppCompatActivity implements EntriesAdapter.OnEntryLongClickListener {

    private RecyclerView recyclerViewRecycleBin;
    private EntriesAdapter recycleBinAdapter;
    private List<Entry> recycleBinEntries;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycle_bin);

        // Initialize DatabaseHelper
        dbHelper = new DatabaseHelper(this);

        recyclerViewRecycleBin = findViewById(R.id.recycle_bin_recycler_view);
        recycleBinEntries = dbHelper.getDeletedEntriesFromRecycleBin();
        recycleBinAdapter = new EntriesAdapter(this, recycleBinEntries, this::onEntryLongClick);
        recyclerViewRecycleBin.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewRecycleBin.setAdapter(recycleBinAdapter);

        recycleBinAdapter.setOnEntryLongClickListener(this);
    }

    @Override
    public void onEntryLongClick(int position) {
        Entry entry = recycleBinEntries.get(position);
        restoreEntry(entry);
    }

    private void restoreEntry(Entry entry) {
        // Restore entry in the database
        dbHelper.restoreEntry(entry.getId(), entry.getUsername(), entry.getPassword(), entry.getUrl());

        recycleBinEntries.remove(entry);
        recycleBinAdapter.notifyDataSetChanged();
        Toast.makeText(this, "Entry restored", Toast.LENGTH_SHORT).show();
    }
}

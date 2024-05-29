package com.example.smdassignment3;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EntriesAdapter extends RecyclerView.Adapter<EntriesAdapter.ViewHolder> {

    private Context context;
    private List<Entry> entryList;
    private OnEntryLongClickListener longClickListener;

    public EntriesAdapter(Context context, List<Entry> entryList, OnEntryLongClickListener longClickListener) {
        this.context = context;
        this.entryList = entryList;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Entry entry = entryList.get(position);
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION && longClickListener != null) {
                    longClickListener.onEntryLongClick(adapterPosition);
                    return true;
                }
                return false;
            }
        });
        holder.txtUsername.setText(entry.getUsername());
        holder.txtPassword.setText(entry.getPassword());
        holder.txtUrl.setText(entry.getUrl());
    }


    @Override
    public int getItemCount() {
        return entryList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtUsername, txtPassword, txtUrl;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtUsername = itemView.findViewById(R.id.txtUsername);
            txtPassword = itemView.findViewById(R.id.txtPassword);
            txtUrl = itemView.findViewById(R.id.txtUrl);
        }
    }

    public interface OnEntryLongClickListener {
        void onEntryLongClick(int position);
    }

    public void setOnEntryLongClickListener(OnEntryLongClickListener listener) {
        this.longClickListener = listener;
    }

}


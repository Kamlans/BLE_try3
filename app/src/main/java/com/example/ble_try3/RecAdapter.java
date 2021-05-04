package com.example.ble_try3;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecAdapter extends RecyclerView.Adapter<RecAdapter.VH> {

    Context context;
    List<Model> list;

    public RecAdapter(Context context, List<Model> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.single , parent , false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {

        holder.name.setText(list.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class VH extends RecyclerView.ViewHolder {
        TextView name;
        public VH(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.textView);
        }
    }
}

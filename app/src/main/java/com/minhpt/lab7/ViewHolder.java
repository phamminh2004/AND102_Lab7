package com.minhpt.lab7;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ViewHolder extends RecyclerView.ViewHolder {
    TextView tv_title, tv_date;
    ImageButton btn_update, btn_delete;
    CheckBox cb_status;

    public ViewHolder(@NonNull View itemView) {
        super(itemView);
        tv_date = itemView.findViewById(R.id.tv_date);
        tv_title = itemView.findViewById(R.id.tv_title);
        btn_update = itemView.findViewById(R.id.btn_update);
        btn_delete = itemView.findViewById(R.id.btn_delete);
        cb_status = itemView.findViewById(R.id.cb_status);
    }
}

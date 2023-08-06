package com.minhpt.lab7;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Adapter extends RecyclerView.Adapter<ViewHolder> {
    private Context context;
    private ArrayList<ToDo> list;
    private FirebaseFirestore database;
    MainActivity mainActivity;

    public Adapter(Context context, ArrayList<ToDo> list, MainActivity mainActivity) {
        this.context = context;
        this.list = list;
        database = FirebaseFirestore.getInstance();
        this.mainActivity = mainActivity;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tv_title.setText(list.get(position).getTitle());
        holder.tv_date.setText(list.get(position).getDate());
        if (list.get(position).getStatus() == 1) {
            holder.cb_status.setChecked(true);
            holder.tv_title.setPaintFlags(holder.tv_title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.cb_status.setChecked(false);
            holder.tv_title.setPaintFlags(holder.tv_title.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
        holder.btn_update.setOnClickListener(v -> {
            ToDo toDo = list.get(holder.getAdapterPosition());
            DialogUpdate(toDo);
        });
        holder.btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Cảnh báo");
                builder.setIcon(R.drawable.ic_warning);
                builder.setMessage("Bạn có chắc chắn muốn xóa không?");
                builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        database.collection("TODO").document(list.get(holder.getAdapterPosition()).getId())
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(context, "Xóa thành công", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("Lỗi", "onFailure" + e);
                                    }
                                });
                        dialog.dismiss();
                    }
                }).setNegativeButton("Không", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
            }
        });

        holder.cb_status.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String id = list.get(holder.getAdapterPosition()).getId();
                boolean check = holder.cb_status.isChecked();
                int value = check ? 1 : 0;
                database.collection("TODO").document(id)
                        .update("status", value)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                list = mainActivity.getListToDo();
                                Toast.makeText(context, "Cập nhật status thành công", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("Lỗi", "onFailure" + e);
                            }
                        });
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void DialogUpdate(ToDo toDo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.update, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        EditText edtTitle = view.findViewById(R.id.edtTitle);
        EditText edtContent = view.findViewById(R.id.edtContent);
        EditText edtDate = view.findViewById(R.id.edtDate);
        EditText edtType = view.findViewById(R.id.edtType);
        Button btnUpdate = view.findViewById(R.id.btnUpdate);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        edtTitle.setText(toDo.getTitle());
        edtContent.setText(toDo.getContent());
        edtDate.setText(toDo.getDate());
        edtType.setText(toDo.getType());
        edtType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] mucDoCV = {"Dễ", "Bình thường", "Khó"};
                new android.app.AlertDialog.Builder(context).setTitle("Chọn mức độ công việc").setItems(mucDoCV, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        edtType.setText(mucDoCV[which]);
                    }
                }).show();
            }
        });
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = edtTitle.getText().toString();
                String content = edtContent.getText().toString();
                String date = edtDate.getText().toString();
                String type = edtType.getText().toString();
                ToDo toDo1 = new ToDo(toDo.getId(), title, content, date, type, toDo.getStatus());
                database.collection("TODO").document(toDo.getId())
                        .update(toDo1.convertHashMap())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                list = mainActivity.getListToDo();
                                Toast.makeText(context, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("Lỗi", "onFailure" + e);
                            }
                        });
                dialog.dismiss();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}

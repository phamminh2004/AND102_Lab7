package com.minhpt.lab7;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    EditText edt_id, edt_title, edt_content, edt_date, edt_type;
    Button btn_add;
    RecyclerView rv_list;
    Adapter adapter;
    ArrayList<ToDo> list = new ArrayList<>();
    Context context = this;
    FirebaseFirestore database = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListenFirebaseFirestore();
        edt_title = findViewById(R.id.edt_title);
        edt_content = findViewById(R.id.edt_content);
        edt_date = findViewById(R.id.edt_date);
        edt_type = findViewById(R.id.edt_type);
        btn_add = findViewById(R.id.btn_add);
        rv_list = findViewById(R.id.rv_list);
        FirebaseApp.initializeApp(this);
        adapter = new Adapter(this, list);
        list = adapter.getListToDo();
        rv_list.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        rv_list.setLayoutManager(layoutManager);

        edt_type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] mucDoCV = {"Dễ", "Bình thường", "Khó"};
                new android.app.AlertDialog.Builder(context).setTitle("Chọn mức độ công việc").setItems(mucDoCV, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        edt_type.setText(mucDoCV[which]);
                    }
                }).show();
            }
        });
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = edt_title.getText().toString();
                String content = edt_content.getText().toString();
                String date = edt_date.getText().toString();
                String type = edt_type.getText().toString();
                String id = UUID.randomUUID().toString();

                ToDo toDo = new ToDo(id, title, content, date, type, 0);

                HashMap<String, Object> mapTodo = toDo.convertHashMap();

                database.collection("TODO").document().set(mapTodo).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        list = adapter.getListToDo();
                        Toast.makeText(context, "Thêm thành công", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Thêm thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    public void ListenFirebaseFirestore() {
        database.collection("TODO").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("TAG", "Listen failed");
                    return;
                }
                if (value != null) {
                    for (DocumentChange dc : value.getDocumentChanges()) {
                        switch (dc.getType()) {
                            case ADDED:
                                dc.getDocument().toObject(ToDo.class);
                                list.add(dc.getDocument().toObject(ToDo.class));
                                adapter.notifyItemInserted(list.size() - 1);
                                break;
                            case MODIFIED:
                                ToDo update = dc.getDocument().toObject(ToDo.class);
                                if (dc.getOldIndex() == dc.getNewIndex()) {
                                    list.set(dc.getOldIndex(), update);
                                    adapter.notifyItemChanged(dc.getOldIndex());
                                } else {
                                    list.remove(dc.getOldIndex());
                                    list.add(update);
                                    adapter.notifyItemMoved(dc.getOldIndex(), dc.getNewIndex());
                                }
                                break;
                            case REMOVED:
                                dc.getDocument().toObject(ToDo.class);
                                list.remove(dc.getOldIndex());
                                adapter.notifyItemRemoved(dc.getOldIndex());
                                break;
                        }
                    }
                }
            }
        });
    }
}
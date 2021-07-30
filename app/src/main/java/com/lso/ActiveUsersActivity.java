package com.lso;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ActiveUsersActivity extends AppCompatActivity {

    private static final String TAG = ActiveUsersActivity.class.getSimpleName();

    private UsersAdapter adapter;
    private RecyclerView recyclerView;
    private final List<String> utenti = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_users);

        getSupportActionBar().setTitle("Conquer - Utenti Attivi");

        recyclerView = findViewById(R.id.recyclerview_activeusers);
        adapter = new UsersAdapter(this, utenti);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Thread(() -> {
            boolean usersFetched = UserDataHandler.fetchActiveUsers(utenti);
            if (!usersFetched) {
                ConnectionHandler.stopConnection();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Errore di connessione", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, ConnectionActivity.class));
                    finishAffinity();
                });
            }
            else {
                runOnUiThread(() -> adapter.notifyDataSetChanged());
            }
        }).start();
    }


    public static class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

        private final List<String> users;
        private final LayoutInflater inflater;

        UsersAdapter(Context context, List<String> data) {
            this.inflater = LayoutInflater.from(context);
            this.users = data;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.active_user_row, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            String animal = users.get(position);
            holder.nickname_txtview.setText(animal);
        }

        @Override
        public int getItemCount() {
            return users.size();
        }


        public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            TextView nickname_txtview;

            ViewHolder(View itemView) {
                super(itemView);
                nickname_txtview = itemView.findViewById(R.id.nome_utente_txtview);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {

            }

        }


    }

}
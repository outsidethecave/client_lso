package com.lso.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lso.control.ActiveUsersController;
import com.lso.R;

import java.util.List;

@SuppressWarnings("ConstantConditions")
public class ActiveUsersActivity extends AppCompatActivity {

    private final ActiveUsersController controller = ActiveUsersController.getInstance();

    private UsersAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_users);

        controller.setActivity(this);

        getSupportActionBar().setTitle("Conquer - Utenti Attivi");

        adapter = new UsersAdapter(this, controller.getUtenti());

        setReciclerView();

    }

    @Override
    protected void onResume() {
        super.onResume();
        controller.showActiveUsers();
    }


    private void setReciclerView () {
        RecyclerView recyclerView = findViewById(R.id.recyclerview_activeusers);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    public void notifyAdapter() {
        adapter.notifyDataSetChanged();
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
package br.com.cursoufba.chatmp.gui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import br.com.cursoufba.chatmp.R;

public class UsuarioActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario);

        final EditText edUsuario = (EditText) findViewById(R.id.ed_usuario);

        Button btEntrar = (Button) findViewById(R.id.bt_entrar);
        btEntrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edUsuario.getText().toString().equals("")) {
                    Toast.makeText(UsuarioActivity.this, "Por favor, informe seu usuário.", Toast.LENGTH_SHORT).show();
                    edUsuario.requestFocus();
                    return;
                }

                Intent intent = new Intent(UsuarioActivity.this, ChatActivity.class);
                intent.putExtra("Usuario", edUsuario.getText().toString());
                startActivity(intent);
            }
        });
    }

}
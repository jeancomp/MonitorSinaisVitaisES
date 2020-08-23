package br.lsdi.ufma.cddldemoapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

public class Login extends AppCompatActivity {
    EditText login;
    EditText senha;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        login = findViewById(R.id.login);
        senha  = findViewById(R.id.senha);
    }

    public void proximaTela(View view){
        if ( login.getText().equals("user") && senha.getText().equals("12345") ) {
            Intent intent = new Intent(Login.this, MainActivity.class);
            startActivity(intent);
        }
        // retorna para o login
        else{
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
        }

    }
}

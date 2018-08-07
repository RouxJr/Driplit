package com.example.s215131746.driplit;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import viewmodels.PersonModel;

public class EditProfile extends AppCompatActivity {
    GeneralMethods m;
    EditText txtFullname;
    EditText txtEmail;
    DBAccess business;
    EditText txtPassword;
    EditText txtNewPassword;
    PersonModel person = new PersonModel();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        person = new PersonModel();
        m = new GeneralMethods(getApplicationContext());
        txtFullname = findViewById(R.id.txtUsername);
        txtEmail = findViewById(R.id.txtUserEmail);
        txtNewPassword = findViewById(R.id.txtNewPassword);
        txtPassword = findViewById(R.id.txtPassword);
        business = new DBAccess();

        String[] p = m.Read("person.txt",",");
        txtFullname.setText(p[1]);
        txtEmail.setText(p[2]);

        person.id =Integer.parseInt(p[0]);
        person.fullName = p[1];
        person.email = p[2];
        person.userPassword = p[3];
        //txtPassword.setText(person[3]);
    }
    public void ToHome(View v){
        finish();
    }
    public void ToUpdatePerson(View v){

        person.fullName = txtFullname.getText().toString();
        boolean error = false;
        if(person.userPassword.equals(txtPassword.getText().toString())){
            String[] values = {""+person.fullName,person.userPassword,txtNewPassword.getText().toString()};
            //new password
            if(!values[values.length-1].equalsIgnoreCase("")){
                person.userPassword = values[values.length-1];
            }

            for(int i = 0; i<values.length-1;i++){
                if(values[i].equalsIgnoreCase("")){
                    error = true;
                }
            }
        }
        else {
            error = true;
        }

        if(!error){
            business.uspMobUpdatePerson(person);
            m.writeToFile(person.id+","+ person.toString(),"person.txt");
            Toast.makeText(getApplicationContext(),"Updated Profile",Toast.LENGTH_SHORT).show();
            ToHome(v);
        }else {
            Toast.makeText(getApplicationContext(),"No Changes",Toast.LENGTH_SHORT).show();
        }

    }
}

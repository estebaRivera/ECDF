package cl.estadiocdf.EstadioCDF.activities;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

import cl.estadiocdf.EstadioCDF.R;

import cl.estadiocdf.EstadioCDF.fragments.LoginFragment;

public class LoginActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new LoginFragment())
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}

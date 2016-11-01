package com.citrix.developer.storefront_api_android_sample;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.citrix.developer.storefront_api_android_sample.Classes.Resource;
import com.citrix.developer.storefront_api_android_sample.Tasks.CheckAuthenticationTask;
import com.citrix.developer.storefront_api_android_sample.Tasks.DownloadAuthMethodsTask;
import com.citrix.developer.storefront_api_android_sample.Tasks.DownloadResourceTask;
import com.citrix.developer.storefront_api_android_sample.Tasks.PostCredentialsTask;

import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{
    public ArrayList<String> _cookies;
    public ArrayList<String> _launchURLs;
    public ArrayList<Resource> _appResources;
    public ProgressDialog _progress;
    String _username = null;
    String _password = null;
    String _sfurl = null;

    public void CallCheckAuth()
    {
        new CheckAuthenticationTask(getApplicationContext(), this).execute();
    }

    public void CallDownloadAuthMethods(URL AuthenticationMethodURL)
    {
        new DownloadAuthMethodsTask(getApplicationContext(), this).execute(AuthenticationMethodURL);
    }

    public void CallPostCreds(URL AuthenticationURL)
    {
        new PostCredentialsTask(_username, _password, getApplicationContext(), this).execute(AuthenticationURL);
    }

    public void CallDownloadResources()
    {
        new DownloadResourceTask(getApplicationContext(), this).execute();
    }

    public void ParseAndShowResources(String JSONResourceList)
    {
        Intent _listResourcesActivity = new Intent(MainActivity.this, ResourceListing.class);
        _listResourcesActivity.putExtra("JSONRESOURCE", JSONResourceList);
        startActivity(_listResourcesActivity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //clear settings
        getSharedPreferences("AppSettings", 0).edit().clear().commit();

        this._launchURLs = new ArrayList<String>();
        _appResources = new ArrayList<Resource>();

        Button a = (Button) findViewById(R.id.callServices);
        a.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                EditText _unameEdit = (EditText) findViewById(R.id.username);
                _username = _unameEdit.getText().toString();
                EditText _pwdEdit = (EditText) findViewById(R.id.password);
                _password = _pwdEdit.getText().toString();
                EditText _urlEdit = (EditText) findViewById(R.id.url);
                _sfurl = _urlEdit.getText().toString();
                if (!_sfurl.endsWith("/"))
                {
                    _sfurl += "/";
                }

                if (_username.equals("") || _password.equals("") | _sfurl.equals(""))
                {
                    //show dialog to user informing we need all fields to be entered.
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage("Please fill out all fields");
                    builder.setTitle("Missing Information");
                    builder.setCancelable(true);

                    AlertDialog dialog = builder.create();

                    dialog.show();
                }
                else
                {
                    CheckBox chkHttps = (CheckBox) findViewById(R.id.chkHttps);
                    boolean _useHTTPS = chkHttps.isChecked();

                    SharedPreferences _prefs = getSharedPreferences("AppSettings", 0);
                    SharedPreferences.Editor _edit = _prefs.edit();
                    _edit.putString("SFURL", _sfurl);
                    _edit.putBoolean("UseHttps", _useHTTPS);
                    _edit.commit();

                    CallCheckAuth();

                    //Intent _a = new Intent(getBaseContext(),ListApps.class);

                    //startActivity(_a);
                }
            }
        });


    }
}

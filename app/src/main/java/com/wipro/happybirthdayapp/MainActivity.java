package com.wipro.happybirthdayapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.drm.DrmStore;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = "MainActivity";
    private static final int READ_CONTACT_PERMISSION_REQUEST = 1;
    private static final int CONTACTS_LOADER_ID = 90;
    private static final int LOOKUP_KEY_INDEX = 1;
    private static final int CONTACT_ID_INDEX = 2;
    private SimpleCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setUpCursorAdapter();
        ListView contactsListView = findViewById(R.id.list_view);
        contactsListView.setAdapter(adapter);
        contactsListView.setOnItemClickListener(this);
        getUserPersmission();
    }

    private void setUpCursorAdapter() {

        String[] uiBindFrom = {ContactsContract.Contacts.DISPLAY_NAME,ContactsContract.Contacts.PHOTO_URI};
        int []uiBindTo = {R.id.name_tv,R.id.image_view};

        adapter = new SimpleCursorAdapter(this,R.layout.contact_list_item,null,uiBindFrom,uiBindTo,0);
    }

    private void getUserPersmission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.READ_CONTACTS)!= PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},READ_CONTACT_PERMISSION_REQUEST);
                return;
            }else{
                loadingContacts();
            }
        }
    }

    private void loadingContacts() {
        Log.d(TAG, "we have permission to load contacts");
        getSupportLoaderManager().initLoader(CONTACTS_LOADER_ID,new Bundle(),contactsLoader);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case READ_CONTACT_PERMISSION_REQUEST:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    loadingContacts();
                }else Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.about_me) {
            Intent intent = new Intent(this,AboutME.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public LoaderManager.LoaderCallbacks<Cursor> contactsLoader = new LoaderManager.LoaderCallbacks<Cursor>() {
        @NonNull
        @Override
        public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {

            String[] projectionFields = new String[]{
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.Contacts.PHOTO_URI
            };

            CursorLoader cursorLoader = new CursorLoader(MainActivity.this,
                    ContactsContract.Contacts.CONTENT_URI,
                    projectionFields,
                    null,null,null);

            return cursorLoader;
        }

        @Override
        public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
            adapter.swapCursor(cursor);
        }

        @Override
        public void onLoaderReset(@NonNull Loader<Cursor> loader) {
            adapter.swapCursor(null);
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Cursor cursor = ((SimpleCursorAdapter)parent.getAdapter()).getCursor();
        cursor.moveToPosition(position);
        String contactName = cursor.getString(LOOKUP_KEY_INDEX);
        Log.d(TAG, "onItemClick: "+contactName);

        Uri mContactUri = ContactsContract.Contacts.getLookupUri(
                cursor.getLong(CONTACT_ID_INDEX),contactName
        );

        String email = getEmail(mContactUri);
        if(!email.equals("")){
            sendEmail(email,contactName);
        }
    }

    private void sendEmail(String email, String contactName) {

        Intent intent = new Intent(Intent.ACTION_SEND,Uri.fromParts(
                "mailto",email,null
        ));
        intent.putExtra(Intent.EXTRA_SUBJECT,"Happy birthday");
        intent.putExtra(Intent.EXTRA_TEXT,"Happy birthday to you, many many returns of the day");
        startActivity(Intent.createChooser(intent,"Prakash Sharma"));
    }

    private String getEmail(Uri mContactUri) {
        String email = "";
        String id = mContactUri.getLastPathSegment();
        Cursor cursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Email.CONTACT_ID+"=?",new String[]{id},
                null
        );
        int emailIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);
        if(cursor.moveToFirst()){
            email = cursor.getString(emailIndex);
        }
        Log.d(TAG, "getEmail: "+email);
        return email;
    }
}

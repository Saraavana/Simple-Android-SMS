package com.tennibs.saravana.simplesms;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Button send;
    private EditText phoneNo;
    private EditText messageBody;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        phoneNo = (EditText)findViewById(R.id.phNoEditTxt);
        messageBody = (EditText)findViewById(R.id.msgEditText);

        send = (Button)findViewById(R.id.sendBtn);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String no = phoneNo.getText().toString();
                String msg = messageBody.getText().toString();

                try
                {
                    SmsManager smsManager = SmsManager.getDefault();
                    PendingIntent sentPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent("SMS_SENT"), 0);
                    PendingIntent deliveredPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent("SMS_DELIVERED"), 0);

                    smsManager.sendTextMessage(no,"",msg,sentPendingIntent,deliveredPendingIntent);

                    sendSmsCallBack();
                }
                catch (Exception e)
                {
                    Toast.makeText(getApplicationContext(),"SMS Failed",Toast.LENGTH_LONG).show();
                }
            }
        });

        phoneNo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                chooseContact();
                return false;
            }
        });
    }


        @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode)
        {
            case (1):

            if (resultCode == Activity.RESULT_OK)
            {
                Uri uri = data.getData();
                Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                String id = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));

                if (cursor.moveToFirst())
                {
                      Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID+"="+id,null,null);
                      phones.moveToFirst();

                      String cNumber = phones.getString(phones.getColumnIndex("data1"));
                    String phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    phoneNo.setText(cNumber);
                }
                cursor.close();
            }
//                Uri contactData = data.getData();
//                Cursor c = managedQuery(contactData,null,null,null,null);
//                if (c.moveToFirst())
//                {
//                    String id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
//                    String hasPhone = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
//
//                    if (hasPhone.equalsIgnoreCase("1"))
//                    {
//                      Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID+"="+id,null,null);
//                      phones.moveToFirst();
//
//                       String cNumber = phones.getString(phones.getColumnIndex("data1"));
//                        phoneNo.setText(cNumber);
//
//                        System.out.println("number is:"+cNumber);
//                    }
//                }
//            }
                break;
        }
    }

    void chooseContact()
    {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(intent, 1);
    }

    void sendSmsCallBack() {
        // For when the SMS has been sent
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(context, "SMS sent successfully", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(context, "Generic failure cause", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(context, "Service is currently unavailable", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(context, "No pdu provided", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(context, "Radio was explicitly turned off", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter("SMS_SENT"));

        // For when the SMS has been delivered
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS delivered", Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter("SMS_DELIVERED"));
    }
}

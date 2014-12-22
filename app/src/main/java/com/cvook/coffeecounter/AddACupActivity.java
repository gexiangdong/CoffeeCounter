package com.cvook.coffeecounter;

import android.app.Activity;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * This Activity does NOT have an UI.
 * It is called by NFC tag.
 * Call the remote AddACup webservice and close the activity.
 */
public class AddACupActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("CC", "AddACupActivity onCreated...");
        setContentView(R.layout.activity_add_acup);
        String userId = MainActivity.getUserId(this);
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("datasource", "2");
        Util.getInstance().getRemoteJSONObjectAsync("ADDACUP", userId, params, new Handler(){
            @Override
            public void handleMessage(Message msg){
                super.handleMessage(msg);
                Bundle data = msg.getData();
                String jsonString = data.getString(Util.JSONKEY);
                Log.d("CC", "JSON from server: " + jsonString);
                if(jsonString == null){
                    Toast.makeText(AddACupActivity.this, getString(R.string.networkError), Toast.LENGTH_LONG).show();
                }else {
                    try {
                        JSONObject resp = new JSONObject(jsonString);
                        Toast.makeText(AddACupActivity.this, resp.getString("message"), Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        Log.e("CC", "JSONException。", e);
                        Toast.makeText(AddACupActivity.this, getString(R.string.addacup_something_wrong), Toast.LENGTH_LONG).show();
                    }
                }
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                r.play();
                //close the activity
                finish();

            }
        });
    }


}

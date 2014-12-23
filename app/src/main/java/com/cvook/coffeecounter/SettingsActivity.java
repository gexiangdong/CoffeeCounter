package com.cvook.coffeecounter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.widget.Toast;
import android.content.DialogInterface.OnDismissListener;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;




/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends Activity {
    private boolean nfcWriteMode = false;
    private AlertDialog cancelNfcWriteDialog;
    private NfcAdapter nfcAdapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SettingFragment fragment = new SettingFragment();

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, fragment).commit();


        setupActionBar();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            if(getActionBar() != null) {
                getActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d("CC", "SettingsActivity.onNewIntent()" + intent.getAction());
        if (nfcWriteMode && NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Log.d("CC", "try to write uri to nfc tag.");
            Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Uri uri = Uri.parse("http://cc.cvook.com/addacup");
            NdefRecord record = NdefRecord.createUri(uri);
            NdefMessage message = new NdefMessage(new NdefRecord[] { record });
            if (writeTag(message, detectedTag)) {
                if(cancelNfcWriteDialog != null){
                    cancelNfcWriteDialog.dismiss();
                }
                Toast.makeText(this, getString(R.string.write_nfc_tag_success), Toast.LENGTH_LONG).show();
            }
            nfcWriteMode = false;
        }
    }

    public void startWriteNfcTag(){
        nfcAdapter  = NfcAdapter.getDefaultAdapter(this);
        PendingIntent nfcPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, SettingsActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        nfcWriteMode = true;
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter[] writeTagFilters = new IntentFilter[] { tagDetected };
        nfcAdapter.enableForegroundDispatch(this, nfcPendingIntent, writeTagFilters, null);

        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle(R.string.write_nfc_tag);
        adb.setMessage(R.string.write_nfc_tag_note);
        adb.setCancelable(true);
        adb.setPositiveButton(R.string.cancel,
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog,int whichButton){
                        nfcWriteMode = false;
                        dialog.dismiss();
                    }
                });

        cancelNfcWriteDialog = adb.create();
        cancelNfcWriteDialog.setOnDismissListener(new DialogInterface.OnDismissListener{
            public void onDismiss(DialogInterface dialog){
                if(nfcAdapter != null){
                    nfcAdapter.disableForegroundDispatch(SettingsActivity.this);
                }
            }
        });
        cancelNfcWriteDialog.show();

    }

    private boolean writeTag(NdefMessage message, Tag tag) {
        int size = message.toByteArray().length;
        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();
                if (!ndef.isWritable()) {
                    Toast.makeText(this, getString(R.string.error_tag_not_writeable), Toast.LENGTH_SHORT).show();
                    return false;
                }
                if (ndef.getMaxSize() < size) {
                    Toast.makeText(this, getString(R.string.error_tag_too_small), Toast.LENGTH_SHORT).show();
                    return false;
                }
                ndef.writeNdefMessage(message);
                return true;
            } else {
                NdefFormatable format = NdefFormatable.get(tag);
                if (format != null) {
                    try {
                        format.connect();
                        format.format(message);
                        return true;
                    } catch (IOException e) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        }
    }


    public static class SettingFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference);

            findPreference("uuid").setSummary(MainActivity.getUserId(this.getActivity()));

            String version = this.getActivity().getString(R.string.app_version);
            try{
                ApplicationInfo ai = this.getActivity().getPackageManager().getApplicationInfo(this.getActivity().getPackageName(), 0);
                ZipFile zf = new ZipFile(ai.sourceDir);
                ZipEntry ze = zf.getEntry("classes.dex");
                long time = ze.getTime();
                String appBuildVersion = new SimpleDateFormat("yyyy.MM.dd", Locale.US).format(new Date(time));
                zf.close();
                version = version + " build " + appBuildVersion;
            }catch(Exception e){
                Log.e("CC", "error while get build date.", e);
            }
            findPreference("version").setSummary(version);

            Preference nfcPreference = findPreference("addCupNFCTag");
            NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this);
            if (adapter == null){
                //NFC not supported
                adapter.setSummary(this.getActivity().getString(R.string.nfc_notsupported));
            }else if(adapter.isEnabled()) {
                //NFC is disabled now.
                adapter.setSummary(this.getActivity().getString(R.string.nfc_disabled));
            }else{
                nfcPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){
                    @Override
                    public boolean onPreferenceClick(Preference preference){
                        Activity activity = SettingFragment.this.getActivity();
                        if(activity instanceof SettingsActivity) {
                            ((SettingsActivity) activity).startWriteNfcTag();
                        }else{
                            Log.d("CC", "Cannot start nfc write.");
                        }
                        return true;
                    }
                });
            }

        }



    }


}

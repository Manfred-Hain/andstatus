/* 
 * Copyright (c) 2013 yvolk (Yuri Volkov), http://yurivolkov.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.andstatus.app.origin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import org.andstatus.app.IntentExtra;
import org.andstatus.app.R;
import org.andstatus.app.context.MyContextHolder;
import org.andstatus.app.context.MyPreferences;
import org.andstatus.app.util.MyLog;

/**
 * Add/Update Microblogging system
 * @author yvolk@yurivolkov.com
 */
public class OriginEditor extends Activity {
    private Origin.Builder builder;

    private Button buttonSave;
    private Button buttonDelete;
    private Spinner spinnerOriginType;
    private EditText editTextOriginName;
    private EditText editTextHost;
    private CheckBox checkBoxIsSsl;
    private CheckBox checkBoxAllowHtml;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyPreferences.loadTheme(this, this);
        setContentView(R.layout.origin_editor);

        buttonSave = (Button) findViewById(R.id.button_save);
        Button buttonDiscard = (Button) findViewById(R.id.button_discard);
        buttonDiscard.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        buttonDelete = (Button) findViewById(R.id.button_delete);
        buttonDelete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (builder.delete()) {
                    MyContextHolder.get().persistentOrigins().initialize();
                    setResult(RESULT_OK);
                    finish();
                }
            }
        });
        
        spinnerOriginType = (Spinner) findViewById(R.id.origin_type);
        editTextOriginName = (EditText) findViewById(R.id.origin_name);
        editTextHost = (EditText) findViewById(R.id.host);
        checkBoxIsSsl = (CheckBox) findViewById(R.id.is_ssl);
        checkBoxAllowHtml = (CheckBox) findViewById(R.id.allow_html);
        
        processNewIntent(getIntent());
    }

    private void processNewIntent(Intent intentNew) {
        String editorAction = intentNew.getAction();
        
        if (Intent.ACTION_INSERT.equals(editorAction)) {
            buttonSave.setOnClickListener(new AddOrigin());
            buttonSave.setText(R.string.button_add);
            builder = new Origin.Builder(OriginType.STATUSNET);
        } else {
            buttonSave.setOnClickListener(new SaveOrigin());
            spinnerOriginType.setEnabled(false);
            editTextOriginName.setEnabled(false);
            Origin origin = MyContextHolder.get().persistentOrigins().fromName(intentNew.getStringExtra(IntentExtra.EXTRA_ORIGIN_NAME.key));
            builder = new Origin.Builder(origin);
        }

        Origin origin = builder.build();
        MyLog.v(this, "processNewIntent: " + origin.toString());
        spinnerOriginType.setSelection(origin.originType.getEntriesPosition());
        editTextOriginName.setText(origin.getName());
        editTextHost.setText(origin.getHost());
        checkBoxIsSsl.setChecked(origin.isSsl());
        checkBoxAllowHtml.setChecked(origin.isHtmlContentAllowed());
        
        buttonDelete.setVisibility(origin.hasChildren() ? View.GONE : View.VISIBLE);

        String title = getText(R.string.label_origin_system).toString();
        if (origin.isPersistent()) {
            title = origin.getName() + " - " + title;
        }
        setTitle(title);
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        processNewIntent(intent);
    }

    private class AddOrigin implements OnClickListener {
        @Override
        public void onClick(View v) {
            builder = new Origin.Builder(OriginType.fromEntriesPosition(spinnerOriginType.getSelectedItemPosition()));
            builder.setName(editTextOriginName.getText().toString());
            saveOthers();
        }
    }
    
    private class SaveOrigin implements OnClickListener {
        @Override
        public void onClick(View v) {
            saveOthers();
        }
    }
    
    private void saveOthers() {
        builder.setHost(editTextHost.getText().toString());
        builder.setSsl(checkBoxIsSsl.isChecked());
        builder.setHtmlContentAllowed(checkBoxAllowHtml.isChecked());
        builder.save();
        MyLog.v(this, (builder.isSaved() ? "Saved" : "Not saved") + ": " + builder.build().toString());
        if (builder.isSaved()) {
            MyContextHolder.get().persistentOrigins().initialize();
            setResult(RESULT_OK);
            finish();
        } else {
            beep(this);
        }
    }
    
    /**
     * See http://stackoverflow.com/questions/4441334/how-to-play-an-android-notification-sound/9622040
     */
    private static void beep(Context context) {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(context, notification);
            r.play();
        } catch (Exception e) {
            MyLog.e("beep", e);
        }        
    }
}

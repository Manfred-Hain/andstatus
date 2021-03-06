/**
 * Copyright (C) 2013 yvolk (Yuri Volkov), http://yurivolkov.com
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

package org.andstatus.app.context;

import android.content.Context;

import net.jcip.annotations.ThreadSafe;

import org.andstatus.app.account.PersistentAccounts;
import org.andstatus.app.data.AssersionData;
import org.andstatus.app.data.MyDatabase;
import org.andstatus.app.data.MyDatabaseConverterController;
import org.andstatus.app.net.HttpConnection;
import org.andstatus.app.origin.PersistentOrigins;
import org.andstatus.app.util.MyLog;

import java.util.Locale;

/**
 * Contains global state of the application
 * The objects are effectively immutable
 * @author yvolk@yurivolkov.com
 */
@ThreadSafe
public final class MyContextImpl implements MyContext {
    private static final String TAG = MyContextImpl.class.getSimpleName();

    private MyContextState state = MyContextState.EMPTY;
    /**
     * Single context object for which we will request SharedPreferences
     */
    private Context context = null;
    /**
     * Name of the object that initialized the class
     */
    private String initializedBy;
    /**
     * When preferences, loaded into this class, were changed
     */
    private long preferencesChangeTime = 0;
    private MyDatabase db;
    private PersistentAccounts persistentAccounts;
    private PersistentOrigins persistentOrigins;
    
    private volatile boolean isExpired = false;

    private final Locale locale = Locale.getDefault();
    
    private MyContextImpl() {
    }

    @Override
    public MyContext newInitialized(Context context, String initializerName) {
        MyContextImpl newMyContext = getCreator(context, initializerName);
        if ( newMyContext.context != null) {
            MyLog.v(TAG, "Starting initialization by " + initializerName);
            newMyContext.preferencesChangeTime = MyPreferences.getPreferencesChangeTime();
            MyDatabase newDb = new MyDatabase(newMyContext.context);
            newMyContext.state = newDb.checkState();
            switch (newMyContext.state) {
                case READY:
                    newMyContext.db = newDb;
                    newMyContext.persistentOrigins.initialize(newMyContext);
                    newMyContext.persistentAccounts.initialize(newMyContext);
                    break;
                default: 
                    break;
            }
        }

        MyLog.v(this, toString());
        return newMyContext;
    }
    
    @Override
    public String toString() {
        return  MyLog.objTagToString(this) +  " initialized by " + initializedBy + "; state=" + state +  "; " + (context == null ? "no context" : "context=" + context.getClass().getName());
    }

    @Override
    public MyContext newCreator(Context context, String initializerName) {
        MyContextImpl newMyContext = getCreator(context, initializerName);
        MyLog.v(this, "newCreator by " + newMyContext.initializedBy 
                + (newMyContext.context == null ? "" : " context: " + newMyContext.context.getClass().getName()));
        return newMyContext;
    }

    private MyContextImpl getCreator(Context context, String initializerName) {
        MyContextImpl newMyContext = getEmpty();
        newMyContext.initializedBy = initializerName;
        if (context != null) {
            Context contextToUse = context.getApplicationContext();
        
            if ( contextToUse == null) {
                MyLog.w(TAG, "getApplicationContext is null, trying the context itself: " + context.getClass().getName());
                contextToUse = context;
            }
            // TODO: Maybe we need to determine if the context is compatible, using some Interface...
            // ...but we don't have any yet.
            if (!context.getClass().getName().contains(this.getClass().getPackage().getName())) {
                MyLog.w(TAG, "Incompatible context: " + contextToUse.getClass().getName());
                contextToUse = null;
            }
            newMyContext.context = contextToUse;
        }
        return newMyContext;
    }
    
    public static MyContextImpl getEmpty() {
        MyContextImpl myContext = new MyContextImpl();
        myContext.persistentAccounts = PersistentAccounts.getEmpty();
        myContext.persistentOrigins = PersistentOrigins.getEmpty();
        return myContext;
    }

    @Override
    public boolean initialized() {
        return state != MyContextState.EMPTY && state != MyContextState.ERROR;
    }

    @Override
    public boolean isReady() {
        return state == MyContextState.READY && !MyDatabaseConverterController.isUpgrading();
    }

    @Override
    public MyContextState state() {
        return state;
    }
    
    @Override
    public Context context() {
        return context;
    }

    @Override
    public String initializedBy() {
        return initializedBy;
    }
    
    @Override
    public long preferencesChangeTime() {
        return preferencesChangeTime;
    }
    
    @Override
    public MyDatabase getDatabase() {
        return db;
    }

    @Override
    /**
     * 2013-12-09 After getting the error "java.lang.IllegalStateException: attempt to re-open an already-closed object: SQLiteDatabase"
     * and reading Inet, I decided NOT to db.close here.
     */
    public void release() {
        MyLog.forget();
    }

    @Override
    public PersistentAccounts persistentAccounts() {
        return persistentAccounts;
    }

    @Override
    public boolean isTestRun() {
        return false;
    }

    @Override
    public void put(AssersionData data) {
        // Noop for this implementation
    }

    @Override
    public boolean isExpired() {
        return isExpired;
    }

    @Override
    public void setExpired() {
        isExpired = true;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public PersistentOrigins persistentOrigins() {
        return persistentOrigins;
    }

    @Override
    public HttpConnection getHttpConnectionMock() {
        return null;
    }
}

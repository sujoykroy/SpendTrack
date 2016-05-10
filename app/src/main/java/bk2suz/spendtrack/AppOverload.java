package bk2suz.spendtrack;

import android.app.Application;

/**
 * Created by sujoy on 9/5/16.
 */
public class AppOverload extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DbManager.createManagers(getBaseContext());
    }
}

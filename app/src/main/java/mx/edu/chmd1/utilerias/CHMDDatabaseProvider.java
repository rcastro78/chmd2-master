package mx.edu.chmd1.utilerias;

import com.activeandroid.Configuration;
import com.activeandroid.content.ContentProvider;

import mx.edu.chmd1.modelosDB.DBCircular;
import mx.edu.chmd1.modelosDB.DBNotificacion;

public class CHMDDatabaseProvider extends ContentProvider {
    protected Configuration getConfiguration() {
        Configuration.Builder builder = new Configuration.Builder(getContext());
        builder.addModelClass(DBCircular.class);
        builder.addModelClass(DBNotificacion.class);
        return builder.create();
    }
}

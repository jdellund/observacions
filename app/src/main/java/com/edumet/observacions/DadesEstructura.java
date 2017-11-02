package com.edumet.observacions;

import android.provider.BaseColumns;

/**
 * Created by 40980055N on 18/10/17.
 */

public final class DadesEstructura {
    private DadesEstructura() {}
    public static class Parametres implements BaseColumns {
        public static final String TABLE_NAME = "observacions";
        public static final String COLUMN_NAME_DIA = "dia";
        public static final String COLUMN_NAME_HORA = "hora";
        public static final String COLUMN_NAME_LATITUD = "latitud";
        public static final String COLUMN_NAME_LONGITUD = "longitud";
        public static final String COLUMN_NAME_FENOMEN = "fenomen";
        public static final String COLUMN_NAME_DESCRIPCIO = "descripcio";
        public static final String COLUMN_NAME_PATH = "path";
/*        public static final String COLUMN_NAME_PATH_ICON = "path_icon";
        public static final String COLUMN_NAME_PATH_VISTA = "path_vista";*/
        public static final String COLUMN_NAME_PATH_ENVIA = "path_envia";
        public static final String COLUMN_NAME_ENVIAT = "enviat";
    }
}



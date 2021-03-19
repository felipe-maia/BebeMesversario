package com.example.bebemesversario;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BBMOpenHelper extends SQLiteOpenHelper {

    private static final String NOME_DB = "bebemesversario.sqlite";
    private static final int VERSAO_DB = 1;

    public BBMOpenHelper(Context context) {
        super(context, NOME_DB, null, VERSAO_DB);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(criaBebe);
        db.execSQL(criaAlbum);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {

    }

    /*
    String criaBebe = "CREATE TABLE IF NOT EXISTS BEBE " +
            "( " +
            "  BEBE_ID	    INTEGER, " +
            "  NOMEBEBE 	VARCHAR(30) NOT NULL, " +
            "  NASCIMENTO	VARCHAR(10)	NOT NULL," +
            "  PRIMARY KEY (BEBE_ID)" +
            "); ";

    String criaAlbum = "CREATE TABLE IF NOT EXISTS ALBUM " +
            "( " +
            "  ALBUM_ID	    INTEGER, " +
            "  TITULO	    VARCHAR(20) , " +
            "  DTALBUM  	VARCHAR(10)	NOT NULL, " +
            "  DESCRICAO	VARCHAR(50),          " +
            "  ALBUM_BEBE_ID	INTEGER  NOT NULL REFERENCES BEBE(BEBE_ID) ON DELETE CASCADE, " +
            "  PRIMARY KEY (ALBUM_ID) " +
            "); ";
    */

    String criaBebe = "CREATE TABLE IF NOT EXISTS BEBE " +
            "( " +
            "  BEBE_ID	    INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "  NOMEBEBE 	VARCHAR(30) NOT NULL, " +
            "  NASCIMENTO	VARCHAR(10)	NOT NULL" +
            "); ";

    String criaAlbum = "CREATE TABLE IF NOT EXISTS ALBUM " +
            "( " +
            "  ALBUM_ID	    INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "  TITULO	    VARCHAR(20) , " +
            "  DTALBUM  	VARCHAR(10)	NOT NULL, " +
            "  DESCRICAO	VARCHAR(50),          " +
            "  ALBUM_BEBE_ID	INTEGER  NOT NULL REFERENCES BEBE(BEBE_ID) ON DELETE CASCADE" +
            "); ";
}

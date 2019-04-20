package com.example.anderson.cronometro;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter {
    public static final String KEY_ROWID = "_id";
    public static final String KEY_TITULO = "titulo";
    public static final String KEY_LISTA = "lista";
    public static final String KEY_REPETIRCICLO = "repetirciclo";
    private static final String TAG = "DBAdapter";

    private static final String DATABASE_NAME = "CronometroBD";
    private static final String DATABASE_TABLE = "treinos";
    private static final int DATABASE_VERSION = 1;

    // tipos existentes no SQLite: INTEGER/REAL/TEXT
    private static final String CRIA_DATABASE = "create table treinos " +
            "(_id integer primary key autoincrement, " +
            " titulo text not null," +
            " lista text not null," +
            " repetirciclo integer not null);";

    private final Context context;
    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    public DBAdapter(Context ctx) {
        this.context = ctx;
        DBHelper = new DatabaseHelper(context); //classe interna que herda de SQLiteOpenHelper
    }

    //classe interna que manipula o banco
    //SQLiteOpenHelper é uma classe abstrata.
    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL(CRIA_DATABASE);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Atualizando a base de dados a partir da versao " + oldVersion
                    + " para " + newVersion + ",isso ira destruir todos os dados antigos");
            //Os logs permitem ao desenvolvedor debugar erros durante o desenvolvimento e também investigar problemas com o software em produção,
            // ou seja, com o usuário final (podem ser vistos no LogCat). Para este fim, android tem um classe específica, a classe Log (android.util.Log).
            // Para criar os log, temos à disposição as funções Log.v(), Log.d(), Log.i(), Log.w(), r Log.e().
            // DEBUG – logs impressos pela função Log.d()
            // ERROR – logs impressos pela função Log.e()
            // INFO – logs impressos pela função Log.i()
            // VERBOSE – logs impressos pela função Log.v()
            // WARN – logs impressos pela função Log.w()

            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(db);
        }
    }

    // *******************************************************************************
    //--- abre a base de dados ---
    public DBAdapter open() throws SQLException {
        db = DBHelper.getWritableDatabase();
        return this;
    }

    //--- fecha a base de dados ---
    public void close() {
        DBHelper.close();
    }

    //---insere um treino na base da dados ---
    public long insereTreino(String titulo, String lista, int repetirCiclo) {
        ContentValues dados = new ContentValues();
        dados.put(KEY_TITULO, titulo);
        dados.put(KEY_LISTA, lista);
        dados.put(KEY_REPETIRCICLO, repetirCiclo);
        return db.insert(DATABASE_TABLE, null, dados);
    }

    //--- exclui um treino---
    public boolean excluiTreino(long idLinha) {
        return db.delete(DATABASE_TABLE, KEY_ROWID + "=" + idLinha, null) > 0;
    }

    //--- devolve todos os treinos---
    public Cursor getTodosTreinos() {
        open();
        String colunas[] = {KEY_ROWID, KEY_TITULO, KEY_LISTA, KEY_REPETIRCICLO};
       // String linhaAcessada = KEY_ROWID + " = '" + id +"'";
        Cursor mCursor = db.query(DATABASE_TABLE, colunas, null, null, null, null, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        //close();
        return mCursor;
    }

    //--- recupera uma linha (treino) ---
    public Cursor getTreino(String id) throws SQLException {
        open();
        // colocar a list
        String colunas[] = {KEY_ROWID, KEY_TITULO, KEY_LISTA, KEY_REPETIRCICLO};
        String linhaAcessada = KEY_ROWID + " = '" + id +"'";
        Cursor mCursor = db.query(DATABASE_TABLE, colunas, linhaAcessada, null, null, null, null, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        //close();
        return mCursor;
    }

    //--- Altera um treino
        public boolean altera(String titulo, String lista, int repetirCiclo) {
        ContentValues dados = new ContentValues();
        Cursor cursor;
        cursor = getTreino(titulo);
        long idLinha = Long.parseLong(cursor.getString(0));
        String linhaAcessada = KEY_ROWID + "=" + idLinha;

        dados.put(KEY_TITULO, titulo);
        dados.put(KEY_LISTA, lista);
        dados.put(KEY_REPETIRCICLO, repetirCiclo);

        return db.update(DATABASE_TABLE, dados, linhaAcessada, null) > 0;
    }
}
package com.example.anderson.cronometro;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class ListaActivity extends AppCompatActivity {

    ListView listaSelecionar;

    String titulo = "";
    int repetirciclo = 0;

    ArrayList<String> arrayLista = new ArrayList<String>();

    ArrayList<Integer> tempoLista = new ArrayList<Integer>();
    ArrayAdapter<Integer> arrayTempoItem;

    ArrayList<String> nomeLista = new ArrayList<String>();
    ArrayAdapter<String> arrayNometItem;

    ArrayList<Integer> idLista = new ArrayList<Integer>();
    ArrayAdapter<Integer> arrayidLista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista);

        listaSelecionar = (ListView) findViewById(R.id.listaSelecionar);

        arrayNometItem = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, nomeLista);
        listaSelecionar.setAdapter(arrayNometItem);

        // Adiciona os treinos so BD na lista
        DBAdapter db = new DBAdapter(this);
        try {
            db.open();
            Cursor cursor;
            cursor = db.getTodosTreinos();
            if (cursor.moveToFirst()) {
                do {
                    addTreinonaLista(cursor);
                } while (cursor.moveToNext());
            }
            db.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        listaSelecionar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                AlertDialog.Builder dialogo = new AlertDialog.Builder(ListaActivity.this);
                dialogo.setIcon(android.R.drawable.ic_dialog_alert);
                dialogo.setTitle("Treino!");
                dialogo.setMessage("Deseja Excluir ou Selecionar este Treino?");

                dialogo.setPositiveButton("Selecionar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //seleciona treino e passa os dados para MainActivity
                        retorna(i);
                        onBackPressed();
                    }
                });
                dialogo.setNegativeButton("Excluir", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //remove da lista
                        nomeLista.remove(i);
                        arrayNometItem.notifyDataSetChanged();
                        //remove do banco de dados
                        removedoBanco(idLista.get(i));
                    }
                });
                dialogo.show();
            }
        });
    }

    void addTreinonaLista(Cursor cursor) {
        //adiciona os treinos do banco de dados na lista
        nomeLista.add(cursor.getString(1));
        idLista.add(cursor.getInt(0)); // pega o id do banco
    }

    void removedoBanco(int i) {
        DBAdapter db = new DBAdapter(this);
        try {
            db.open();
            db.excluiTreino(i);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void retorna(int i) {
        titulo = nomeLista.get(i);
        DBAdapter db = new DBAdapter(this);
        try {
            Cursor cursor;
            db.open();
            cursor = db.getTreino(Integer.toString(idLista.get(i)));

            String[] aux = cursor.getString(2).split("/");
            int j = 0;
            // passa os dados do banco para um ArrayList (para deixar como no ConfActivity
            while (aux.length > j) {
                arrayLista.add(aux[j]);
                j++;
            }
            // quebra a string para pegar o tempo
            i = 0;
            while (i < aux.length) {
                nomeLista.add(aux[i]);
                String[] tempo = aux[i].split("-");
                tempoLista.add(Integer.parseInt(tempo[1].trim()) * 1000);
                i++;
            }
            repetirciclo = Integer.parseInt(cursor.getString(3));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void onBackPressed() {
        Intent it = new Intent();
        it.putExtra("titulo", titulo);
        it.putExtra("repCiclo", Integer.toString(repetirciclo));
        it.putStringArrayListExtra("lista", arrayLista);
        it.putIntegerArrayListExtra("tempo", tempoLista);
        setResult(1, it);
        super.onBackPressed();
    }
}
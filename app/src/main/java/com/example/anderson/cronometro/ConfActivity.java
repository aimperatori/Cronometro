package com.example.anderson.cronometro;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.SQLException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class ConfActivity extends AppCompatActivity {
    EditText edtTitulo;
    EditText edtNome;
    EditText edtTempo;
    Button btnaddTempo;
    ListView listaConfig;
    EditText edtRepCliclo;
    TextView txtTempoTotal;
    Button btnSalvar;
    int cont = 0;
    long tempoTotal;
    String aux = "";

    ArrayList<Integer> tempoLista = new ArrayList<Integer>();
    ArrayAdapter<Integer> arrayTempoItem;

    ArrayList<String> nomeLista = new ArrayList<String>();
    ArrayAdapter<String> arrayNometItem;

    private static final String FORMAT = "%02d:%02d";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conf);

        edtTitulo = (EditText) findViewById(R.id.edtTitulo);
        edtNome = (EditText) findViewById(R.id.edtNome);
        edtTempo = (EditText) findViewById(R.id.edtTempo);
        btnaddTempo = (Button) findViewById(R.id.btnaddTempo);
        listaConfig = (ListView) findViewById(R.id.listaConfig);
        edtRepCliclo = (EditText) findViewById(R.id.edtRepCliclo);
        txtTempoTotal = (TextView) findViewById(R.id.txtTempoTotal);
        btnSalvar = (Button) findViewById(R.id.btnSalvar);

        arrayTempoItem = new ArrayAdapter<Integer>(this, android.R.layout.simple_list_item_1, tempoLista);

        arrayNometItem = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, nomeLista);
        listaConfig.setAdapter(arrayNometItem);

        edtRepCliclo.setText("1");

        btnaddTempo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtNome.length() == 0) {
                    edtNome.selectAll();
                    edtNome.requestFocus();
                    return;
                }
                if (edtTempo.length() == 0) {
                    edtTempo.selectAll();
                    edtTempo.requestFocus();
                    return;
                }

                String aux = edtNome.getText().toString()+ " - " +edtTempo.getText().toString();
                nomeLista.add(cont, aux);
                tempoLista.add(cont, Integer.parseInt(edtTempo.getText().toString()) * 1000);

                arrayNometItem.notifyDataSetChanged();
                arrayTempoItem.notifyDataSetChanged();

                tempoTotal += tempoLista.get(cont);
                txtTempoTotal.setText("" + String.format(FORMAT,
                        TimeUnit.MILLISECONDS.toMinutes(tempoTotal) - TimeUnit.HOURS.toMinutes(
                                TimeUnit.MILLISECONDS.toHours(tempoTotal)),
                        TimeUnit.MILLISECONDS.toSeconds(tempoTotal) - TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(tempoTotal))));

                cont++;

                // compara com 0, se for textview for zero considera como sendo 1
                if (edtRepCliclo.getText().toString().length() == 0) {
                    txtTempoTotal.setText("" + String.format(FORMAT,
                            TimeUnit.MILLISECONDS.toMinutes(tempoTotal) - TimeUnit.HOURS.toMinutes(
                                    TimeUnit.MILLISECONDS.toHours(tempoTotal)),
                            TimeUnit.MILLISECONDS.toSeconds(tempoTotal) - TimeUnit.MINUTES.toSeconds(
                                    TimeUnit.MILLISECONDS.toMinutes(tempoTotal))));
                } else {
                    Long tempoFormatado = tempoTotal * Integer.parseInt(edtRepCliclo.getText().toString());
                    txtTempoTotal.setText("" + String.format(FORMAT,
                            TimeUnit.MILLISECONDS.toMinutes(tempoFormatado) - TimeUnit.HOURS.toMinutes(
                                    TimeUnit.MILLISECONDS.toHours(tempoFormatado)),
                            TimeUnit.MILLISECONDS.toSeconds(tempoFormatado) - TimeUnit.MINUTES.toSeconds(
                                    TimeUnit.MILLISECONDS.toMinutes(tempoFormatado))));
                }
            }
        });

        listaConfig.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {

                AlertDialog.Builder dialogo = new AlertDialog.Builder(ConfActivity.this);
                dialogo.setIcon(android.R.drawable.ic_dialog_alert);
                dialogo.setTitle("Exclusão");
                dialogo.setMessage("Deseja Excluir este Tempo?");

                dialogo.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        tempoTotal -= Long.parseLong(tempoLista.get(i).toString());
                        txtTempoTotal.setText(Long.toString(tempoTotal));
                        cont--;
                        nomeLista.remove(i);
                        tempoLista.remove(i);
                        arrayNometItem.notifyDataSetChanged();
                        arrayTempoItem.notifyDataSetChanged();
                    }
                });
                dialogo.setNegativeButton("Cancelar", null);
                dialogo.show();
            }
        });

        edtRepCliclo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (edtRepCliclo.getText().toString().length() == 0) {
                    txtTempoTotal.setText(Long.toString(tempoTotal));
                } else {
                    txtTempoTotal.setText(Long.toString(tempoTotal * Integer.parseInt(edtRepCliclo.getText().toString())));
                }
            }
        });

        btnSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edtTitulo.getText().toString().length() == 0) {
                    edtTitulo.selectAll();
                    edtTitulo.requestFocus();
                    return;
                } if (listaConfig.getAdapter().getCount() == 0) {
                    edtNome.selectAll();
                    edtNome.requestFocus();
                    return;
                }
                if (edtRepCliclo.getText().toString().length() == 0) {
                    edtRepCliclo.selectAll();
                    edtRepCliclo.requestFocus();
                    return;
                }

                // salva no banco de dados
                int i = 0;
                while(nomeLista.size() > i) {
                    aux += nomeLista.get(i);
                    aux += "/";
                    i++;
                }
                addBancodeDados();

                // retorna os dados para MainActivity
                onBackPressed();
            }
        });
    }

    // retorna os dados para MainActinity
    public void onBackPressed() {
        Intent it = new Intent();
        it.putExtra("titulo", edtTitulo.getText().toString());
        it.putExtra("repCiclo", edtRepCliclo.getText().toString());
        it.putStringArrayListExtra("lista", nomeLista);
        it.putIntegerArrayListExtra("tempo", tempoLista);
        setResult(1, it);
        super.onBackPressed();
    }

    public void addBancodeDados() {
        DBAdapter databaseConnector = new DBAdapter(this);
        try {
            databaseConnector.open();
            databaseConnector.insereTreino(edtTitulo.getText().toString(), aux, Integer.parseInt(edtRepCliclo.getText().toString()));
            databaseConnector.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
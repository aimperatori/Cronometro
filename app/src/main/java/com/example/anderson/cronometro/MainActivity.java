package com.example.anderson.cronometro;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.RequiresApi;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static android.os.VibrationEffect.DEFAULT_AMPLITUDE;

public class MainActivity extends AppCompatActivity {

    TextView cronometro;
    TextView txtTitulo;
    TextView txtRepetir;
    ListView listaExecutar;
    Button btnIniciar;
    boolean isStart = false;
    int repetir;
    int i = 0;
    CountDownTimer timer;
    int cont;

    ArrayList<Integer> tempoLista = new ArrayList<Integer>();
    ArrayAdapter<Integer> arrayTempoItem;

    ArrayList<String> nomeLista = new ArrayList<String>();
    ArrayAdapter<String> arrayNometItem;

    int j = 0;

    long tempoRestante;

    private static final String FORMAT = "%02d:%02d";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cronometro = (TextView) findViewById(R.id.cronometro);
        txtTitulo = (TextView) findViewById(R.id.txtTitulo);
        txtRepetir = (TextView) findViewById(R.id.txtRepetir);
        listaExecutar = (ListView) findViewById(R.id.listaExecutar);
        btnIniciar = (Button) findViewById(R.id.btnIniciar);

        arrayTempoItem = new ArrayAdapter<Integer>(this, android.R.layout.simple_list_item_1, tempoLista);

        arrayNometItem = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, nomeLista);
        listaExecutar.setAdapter(arrayNometItem);

        getTreino();
        tempoRestante = tempoLista.get(0);
        cont = Integer.parseInt(txtRepetir.getText().toString());

        btnIniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isStart) { // entra quando ta contando
                    timer.cancel();
                    isStart = false;
                    btnIniciar.setText("Retomar");
                } else { // entra quando ta pausado
                    criaCronometro();
                    isStart = true;
                    btnIniciar.setText("Pausar");
                }
            }
        });
    }

    void criaCronometro() {
        timer = new CountDownTimer(tempoRestante, 1000) { // adjust the milli seconds here
            public void onTick(long millisUntilFinished) {
                cronometro.setText("" + String.format(FORMAT,
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                                TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
                tempoRestante = millisUntilFinished;
            }
            public void onFinish() {
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                // Vibrate for 500 milliseconds
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createOneShot(500,VibrationEffect.DEFAULT_AMPLITUDE));
                }else{
                    //deprecated in API 26
                    v.vibrate(500);
                }
                // j recebe o tamanho da lista e compara se ja chegou ao final
                if (j == tempoLista.size() - 1) {
                    tempoRestante = tempoLista.get(0);
                    j = 0;
                    cont--;
                    txtRepetir.setText(Integer.toString(cont));
                    if (cont > 0) {
                        criaCronometro();
                    } else {
                        cont = repetir; // cont pegar os de repetir ciclo do banco de dados
                        txtRepetir.setText(Integer.toString(repetir));
                        isStart = false;
                        cronometro.setText("00:00");
                        btnIniciar.setText("Iniciar");
                        cancel();
                    }
                } else {
                    j++;
                    tempoRestante = tempoLista.get(j);
                    criaCronometro();
                }
            }
        }.start();
    }


    @Override
    //exibe o menu "Configurar"
    public boolean onCreateOptionsMenu(Menu menu) {
        // o menu foi criado diretamente no XML. Mais informações em https://developer.android.com/guide/topics/ui/menus.html?hl=pt-br
        getMenuInflater().inflate(R.menu.activitymenu_main, menu);
        getMenuInflater().inflate(R.menu.activitylista_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Cria uma intent para executar o cadastramento de um novo treino
        switch (item.getItemId()) {
            case R.id.adicionar:
                Intent configura = new Intent(MainActivity.this, ConfActivity.class);
                startActivityForResult(configura, 1);
                break;
            case R.id.listar:
                Intent lista = new Intent(MainActivity.this, ListaActivity.class);
                startActivityForResult(lista, 1);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    // recebe os valores da activity ConfActivity e ListaActivity
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1: // data contem os extras
                // testa se o retorno tem extras
                if (!data.getExtras().getString("titulo").isEmpty()) {
                    txtTitulo.setText(data.getExtras().getString("titulo"));
                    repetir = Integer.parseInt(data.getExtras().getString("repCiclo"));
                    txtRepetir.setText(Integer.toString(repetir));
                    cont = repetir;
                    // limpa a lista para inserir o treino selecionado treino
                    arrayNometItem.clear();
                    nomeLista.addAll(data.getStringArrayListExtra("lista"));
                    arrayNometItem.notifyDataSetChanged();

                    // limpa a lista para inserir o treino selecionado treino
                    arrayTempoItem.clear();
                    tempoLista.addAll(data.getIntegerArrayListExtra("tempo"));
                    arrayTempoItem.notifyDataSetChanged();

                    cronometro.setText("" + String.format(FORMAT,
                            TimeUnit.MILLISECONDS.toMinutes(tempoLista.get(0)) - TimeUnit.HOURS.toMinutes(
                                    TimeUnit.MILLISECONDS.toHours(tempoLista.get(0))),
                            TimeUnit.MILLISECONDS.toSeconds(tempoLista.get(0)) - TimeUnit.MINUTES.toSeconds(
                                    TimeUnit.MILLISECONDS.toMinutes(tempoLista.get(0)))));
                    btnIniciar.setText("Iniciar");
                    tempoRestante = Long.valueOf(tempoLista.get(0));
                }
                break;
        }
    }

    public void getTreino() {
        DBAdapter db = new DBAdapter(this);
        try {
            db.open();
            Cursor cursor;
            cursor = db.getTodosTreinos();
            // pega a primeira ocorencia
            if (cursor.moveToFirst()) {
                String[] arrayLista = cursor.getString(2).split("/");
                db.close();

                txtTitulo.setText(cursor.getString(1));
                repetir = Integer.parseInt(cursor.getString(3));
                txtRepetir.setText(Integer.toString(repetir));
                i = 0;
                while (i < arrayLista.length) {
                    nomeLista.add(arrayLista[i]);
                    String[] tempo = arrayLista[i].split("-");
                    tempoLista.add(Integer.parseInt(tempo[1].trim()) * 1000);
                    i++;
                }
            }
            cronometro.setText("" + String.format(FORMAT,
                    TimeUnit.MILLISECONDS.toMinutes(tempoLista.get(0)) - TimeUnit.HOURS.toMinutes(
                            TimeUnit.MILLISECONDS.toHours(tempoLista.get(0))),
                    TimeUnit.MILLISECONDS.toSeconds(tempoLista.get(0)) - TimeUnit.MINUTES.toSeconds(
                            TimeUnit.MILLISECONDS.toMinutes(tempoLista.get(0)))));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
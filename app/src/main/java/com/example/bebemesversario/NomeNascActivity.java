package com.example.bebemesversario;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

public class NomeNascActivity extends Activity implements CalendarView.OnDateChangeListener {

    public static final int PERMISSAO_REQUEST = 1;
    private EditText nome;
    private GregorianCalendar nascAlarm, nasc = new GregorianCalendar();
    private CalendarView calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nome_nasc);

        nome = findViewById(R.id.nome);
        calendar = findViewById(R.id.calendarView);
        calendar.setOnDateChangeListener(this);
    }

    @Override
    public void onSelectedDayChange(CalendarView calendarView, int ano, int mes, int dia) {
        nasc.set(ano, mes, dia);
    }

    public void criar(View v) {

        if (!nome.getText().toString().equals("")) {
            nascAlarm = new GregorianCalendar();
            nascAlarm.setTime(nasc.getTime());
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);

            BBMOpenHelper openHelper = new BBMOpenHelper(this);
            SQLiteDatabase db = openHelper.getReadableDatabase();

            //verificar se ja existe nome igual
            Cursor cursor = db.rawQuery("select BEBE_ID from BEBE where NOMEBEBE =" + "'" + nome.getText().toString() + "'", null);
            if (cursor.getCount() == 0) {

                //insere nome e data de nascimento
                String insertBebe = "INSERT INTO BEBE (NOMEBEBE ,NASCIMENTO) VALUES ('" + nome.getText().toString() + "', '" + df.format(nasc.getTime()) + "');";
                db.execSQL(insertBebe);

                //pegar id do bebe criado
                cursor = db.rawQuery("select BEBE_ID from BEBE where NOMEBEBE =" + "'" + nome.getText().toString() + "'", null);
                cursor.moveToFirst();
                String row_id_bebe = cursor.getString(0);

                //insere albuns
                for (int i = 0; i <= 12; i++) {
                    String insertAlbum = "INSERT INTO ALBUM (TITULO, DTALBUM, DESCRICAO, ALBUM_BEBE_ID) VALUES ('Album " + i + "' , '" + df.format(nasc.getTime()) + "', 'Descrição' , '" + row_id_bebe + "');";
                    db.execSQL(insertAlbum);
                    nasc.add(Calendar.MONTH, 1);
                }
                db.close();

                boolean alarmAtivo = (PendingIntent.getBroadcast(this, 0, new Intent(this, BroadcastReceiverAux.class), PendingIntent.FLAG_NO_CREATE) == null);
                if (alarmAtivo) {
                    //criar alarme para notificação
                    Log.v("TESTE", "Novo Alarme Criado");
                    Intent intentBR = new Intent(this, BroadcastReceiverAux.class);
                    intentBR.putExtra("ID_BEBE", row_id_bebe);
                    intentBR.putExtra("NOME_BEBE", nome.getText().toString());
                    //sendBroadcast(intentBR);

                    PendingIntent p = PendingIntent.getBroadcast(this, 0, intentBR, 0);
                    AlarmManager alarme = (AlarmManager) getSystemService(ALARM_SERVICE);
                    alarme.setRepeating(AlarmManager.RTC_WAKEUP, nascAlarm.getTimeInMillis(), 60000, p);// a repetição da notificação em 60000milisegundos apenas para demosntração, no app real será de 1 mês

                     /* CODIGO PARA PEGAR OS MILISEGUNDOS DE 1 MÊS
                     GregorianCalendar aux = new GregorianCalendar();
                     aux.add(Calendar.MONTH,1);
                     aux.getTimeInMillis();
                     */
                } else {
                    Log.v("TESTE", "Alarme ja criado");
                }
                Intent intent = new Intent(this, Albuns.class);
                intent.putExtra("EXTRA_BEBE_ID", row_id_bebe);
                intent.putExtra("NOME_BEBE", nome.getText().toString());
                startActivity(intent);
            } else
                Toast.makeText(NomeNascActivity.this, "Nome do bebe já existe!", Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(NomeNascActivity.this, "Preencha o campo nome!", Toast.LENGTH_SHORT).show();
    }
}




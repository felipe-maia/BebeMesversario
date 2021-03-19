package com.example.bebemesversario;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends Activity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    public static final String CHANNEL_ID = "1";
    private List<Map<String, Object>> bebes;
    private ListView listaBebes;
    private Context contexto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        criarCanalNotificacao();
        contexto = this.getBaseContext();
        listaBebes = findViewById(R.id.listBebes);
        listaBebes.setOnItemClickListener(this);
        listaBebes.setOnItemLongClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        buscaBebes();
    }
    public void criarCanalNotificacao() {
        //criando canal de notificação apenas na API 26 ou maior porque a classe NotificationChannel é nova
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Mesversario";
            String description = "Data Mesversario";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void iniciar(View v) {
        Intent intent = new Intent(this, NomeNascActivity.class);
        startActivity(intent);
    }

    public void buscaBebes() {

        bebes = new ArrayList<>();
        BBMOpenHelper openHelper = new BBMOpenHelper(this);
        SQLiteDatabase db = openHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("select BEBE_ID, NOMEBEBE, NASCIMENTO from BEBE ", null);

        while (cursor.moveToNext()) {
            Map<String, Object> mapa = new HashMap<String, Object>();

            mapa.put("BEBE_ID", cursor.getString(0));
            mapa.put("NOMEBEBE", cursor.getString(1));
            mapa.put("NASCIMENTO", cursor.getString(2));

            bebes.add(mapa);
        }

        String[] daonde = {"NOMEBEBE", "NASCIMENTO"};
        int[] paraonde = {R.id.edNome, R.id.edNasc};

        SimpleAdapter adapter = new SimpleAdapter(this, bebes,
                R.layout.layout_lista_bebes, daonde, paraonde);
        listaBebes.setAdapter(adapter);
        db.close();
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View v, int pos, long id) {

        Intent intent = new Intent(this, Albuns.class);

        Map<String, Object> selecao = bebes.get(pos);
        String bebe_id_posicao = selecao.get("BEBE_ID").toString();
        String NOME_BEBE = selecao.get("NOMEBEBE").toString();

        intent.putExtra("EXTRA_BEBE_ID", bebe_id_posicao);
        intent.putExtra("NOME_BEBE", NOME_BEBE);
        startActivity(intent);
    }


    @Override
    public boolean onItemLongClick(AdapterView<?> arg0, View v, int pos, long id) {

        final Map<String, Object> selecao = bebes.get(pos);
        final String bebe_id_posicao = selecao.get("BEBE_ID").toString();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Deseja excluir o item selecionado?");
        builder.setTitle("Excluir");
        builder.setIcon(android.R.drawable.ic_dialog_info);

        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                BBMOpenHelper openHelper = new BBMOpenHelper(contexto);
                SQLiteDatabase db = openHelper.getReadableDatabase();
                String query1 = "delete from BEBE where BEBE_ID=" + Integer.valueOf(bebe_id_posicao);
                String query2 = "delete from ALBUM where ALBUM_BEBE_ID=" + Integer.valueOf(bebe_id_posicao);
                db.execSQL(query1);
                db.execSQL(query2);
                buscaBebes();
                Toast.makeText(contexto, "Excluido com sucesso", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        return true;
    }
}

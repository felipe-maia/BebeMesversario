package com.example.bebemesversario;

import android.app.Activity;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.example.bebemesversario.NomeNascActivity.PERMISSAO_REQUEST;

public class Albuns extends Activity implements AdapterView.OnItemClickListener {

    private TextView nome;
    private BBMOpenHelper openHelper;
    private List<Map<String, Object>> albuns;
    private ListView listaAlbuns;
    private String bebe_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albuns);

        nome = findViewById(R.id.nome);
        listaAlbuns = findViewById(R.id.listView);
        listaAlbuns.setOnItemClickListener(this);

        requestPermissionWrite();

        Intent intent = getIntent();
        if (intent.hasExtra("EXTRA_BEBE_ID")) {
            bebe_id = intent.getStringExtra("EXTRA_BEBE_ID");
        }
        if (intent.hasExtra("NOME_BEBE")) {
            nome.setText(intent.getStringExtra("NOME_BEBE"));
        }
    }

    public void requestPermissionWrite() {

        ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE}, PERMISSAO_REQUEST);
    }

    public boolean checkPermissionWrite() {

        int result = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);

        return result == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onResume() {
        super.onResume();
        buscaAlbuns();
    }

    public void buscaAlbuns() {

        albuns = new ArrayList<>();
        openHelper = new BBMOpenHelper(this);
        SQLiteDatabase db = openHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("select ALBUM_ID, TITULO, DTALBUM from ALBUM where ALBUM_BEBE_ID=" + "'" + bebe_id + "'", null);

        while (cursor.moveToNext()) {
            Map<String, Object> mapa = new HashMap<String, Object>();

            mapa.put("ALBUM_ID", cursor.getInt(0));
            mapa.put("TITULO", cursor.getString(1));
            mapa.put("DTALBUM", cursor.getString(2));

            albuns.add(mapa);
        }

        String daonde[] = {"TITULO", "DTALBUM"};
        int paraonde[] = {R.id.edTitulo, R.id.edData};

        SimpleAdapter adapter = new SimpleAdapter(this, albuns,
                R.layout.layout_lista_albuns, daonde, paraonde);
        listaAlbuns.setAdapter(adapter);

        db.close();
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View v, int pos, long id) {

        if (checkPermissionWrite()) {

            Intent intent = new Intent(this, VisualizarAlbum.class);

            Map<String, Object> selecao = albuns.get(pos);
            String album_id_posicao = selecao.get("ALBUM_ID").toString();

            intent.putExtra("EXTRA_ALBUM_ID", album_id_posicao);
            startActivity(intent);
        } else
            requestPermissionWrite();
    }
}

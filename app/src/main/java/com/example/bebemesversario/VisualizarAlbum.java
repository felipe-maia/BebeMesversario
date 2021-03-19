package com.example.bebemesversario;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static com.example.bebemesversario.NomeNascActivity.PERMISSAO_REQUEST;

public class VisualizarAlbum extends Activity implements OnItemSelectedListener {

    private String ID_ALBUM;
    private String[] fotos;
    private ImageView imagemView;
    private File arquivoFoto = null;
    private File folderApp = null, folderAlbum = null;
    private final int CAMERA = 2, GALERIA_IMAGENS = 3;
    private Spinner spinner;
    private TextView titulo, descricao;
    private BBMOpenHelper openHelper;
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private Button btnGravarPausar, btnTocarPausar, btnGaleria;
    private boolean btnGravarPausarPress = false, btnTocarPausarPress = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizar);

        //pegando id do album
        Intent intent = getIntent();
        if (intent.hasExtra("EXTRA_ALBUM_ID")) {
            ID_ALBUM = intent.getStringExtra("EXTRA_ALBUM_ID");
        }

        //criando pastas de armazenamento das fotos e audio se não existirem
        folderApp = new File(Environment.getExternalStorageDirectory() + "/FotosBebeMesversario");
        folderAlbum = new File(folderApp.getAbsolutePath() + "/Album_" + ID_ALBUM);
        if (!folderApp.exists()) {
            folderApp.mkdir();
            folderAlbum.mkdir();
        } else {
            if (!folderAlbum.exists()) {
                folderAlbum.mkdir();
            }
        }

        imagemView = findViewById(R.id.imageView);
        titulo = findViewById(R.id.titulo);
        descricao = findViewById(R.id.descricao);
        spinner = findViewById(R.id.spinner);
        btnGravarPausar = findViewById(R.id.btnGravar);
        btnTocarPausar = findViewById(R.id.btnPlay);
        btnGaleria = findViewById(R.id.btnSelecionar);

        // selecionar foto da galeria
        btnGaleria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkPermissionRead()) {

                    Intent intent = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    try {
                        arquivoFoto = criarArquivo();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (arquivoFoto != null) {
                        Uri photoURI = Uri.fromFile(arquivoFoto);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(intent, GALERIA_IMAGENS);
                    }
                } else
                    requestPermissionRead();
            }
        });

        //gravar audios
        btnGravarPausar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (checkPermissionAudio()) {
                    if (!btnGravarPausarPress) {
                        btnGravarPausarPress = true;
                        btnTocarPausar.setEnabled(false);

                        MediaRecorderReady();
                        try {
                            mediaRecorder.prepare();
                            mediaRecorder.start();

                        } catch (IllegalStateException e) {

                            e.printStackTrace();
                        } catch (IOException e) {

                            e.printStackTrace();
                        }
                        Toast.makeText(VisualizarAlbum.this, "Gravando Áudio", Toast.LENGTH_SHORT).show();
                    } else {
                        mediaRecorder.stop();
                        btnGravarPausarPress = false;

                        btnTocarPausar.setEnabled(true);
                        Toast.makeText(VisualizarAlbum.this, "Gravação Completa", Toast.LENGTH_SHORT).show();
                    }

                } else
                    requestPermissionAudio();
            }
        });

        //reproduzir audios
        btnTocarPausar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) throws IllegalArgumentException, SecurityException, IllegalStateException {

                if (checkPermissionRead()) {
                    if (checkPermissionAudio()) {

                        if (!btnTocarPausarPress) {
                            btnTocarPausarPress = true;
                            btnGravarPausar.setEnabled(false);

                            mediaPlayer = new MediaPlayer();

                            try {

                                mediaPlayer.setDataSource(folderAlbum.getAbsolutePath() + "/AudioRecording.3gp");
                                mediaPlayer.prepare();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            mediaPlayer.start();
                            Toast.makeText(VisualizarAlbum.this, "Executando Gravação", Toast.LENGTH_SHORT).show();
                        } else {

                            btnTocarPausarPress = false;
                            btnGravarPausar.setEnabled(true);

                            if (mediaPlayer != null) {

                                mediaPlayer.stop();
                                Toast.makeText(VisualizarAlbum.this, "Gravação Pausada", Toast.LENGTH_SHORT).show();
                                mediaPlayer.release();
                                MediaRecorderReady();
                            }
                        }
                    } else
                        requestPermissionAudio();
                } else
                    requestPermissionRead();
            }
        });

        buscarTituloDescricao();
        carregarFotos();
    }


    //resultado do pedido de permissão
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSAO_REQUEST) {
            if (grantResults.length > 0) {

                boolean Permission = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                if (Permission) {
                    Toast.makeText(this, "Permissão concedida", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Permissão negada", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    //requisitando permissões
    public void requestPermissionRead() {

        ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE}, PERMISSAO_REQUEST);
    }

    public void requestPermissionAudio() {

        ActivityCompat.requestPermissions(this, new String[]{RECORD_AUDIO}, PERMISSAO_REQUEST);
    }

    //checando permissoes
    public boolean checkPermissionRead() {

        int result = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);

        return result == PackageManager.PERMISSION_GRANTED;
    }

    public boolean checkPermissionAudio() {

        int result = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);

        return result == PackageManager.PERMISSION_GRANTED;
    }

    //preparando configurações e pasta do audio
    public void MediaRecorderReady() {

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(folderAlbum.getAbsolutePath() + "/AudioRecording.3gp");
    }

    //dialogo para editar titulo e descrição
    public void dialogoEditar(View v) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_editar);

        final EditText editTitulo = dialog.findViewById(R.id.editTitulo);
        editTitulo.setText(titulo.getText().toString());
        final EditText editDescricao = dialog.findViewById(R.id.editDescricao);
        editDescricao.setText(descricao.getText().toString());

        Button btnEditar = dialog.findViewById(R.id.btnEditar);

        btnEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String textoT = editTitulo.getText().toString();
                String textoD = editDescricao.getText().toString();
                titulo.setText(textoT);
                descricao.setText(textoD);
                editarTituloDescricao();
                dialog.dismiss();
            }
        });

        Button btnCancelar = dialog.findViewById(R.id.btnCancelar);
        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void editarTituloDescricao() {
        openHelper = new BBMOpenHelper(this);
        SQLiteDatabase db = openHelper.getReadableDatabase();
        db.execSQL("UPDATE ALBUM SET TITULO ='" + titulo.getText().toString() + "', DESCRICAO ='" + descricao.getText().toString() +
                "' WHERE ALBUM_ID =" + ID_ALBUM + "");
        db.close();
    }

    public void buscarTituloDescricao() {
        openHelper = new BBMOpenHelper(this);
        SQLiteDatabase db = openHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("select TITULO, DESCRICAO from ALBUM WHERE ALBUM_ID =" + ID_ALBUM + "", null);
        cursor.moveToFirst();
        titulo.setText(cursor.getString(0));
        descricao.setText(cursor.getString(1));

        db.close();
    }

    public void carregarFotos() {
        //carregar fotos do diretorio do album
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            if (folderAlbum.list() != null) {
                fotos = folderAlbum.list();

                // Criar ArrayAdapter
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_spinner_item, fotos);

                // Especificar layout da lista de opcoes
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                // Aplicar adapter
                spinner.setAdapter(adapter);
                spinner.setOnItemSelectedListener(this);
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int posicao, long arg3) {

        String fotoSelecionada = fotos[posicao];
        String caminho = folderAlbum.getAbsolutePath() + "/" + fotoSelecionada;
        try {
            FileInputStream in = new FileInputStream(caminho);
            imagemView.setImageBitmap(BitmapFactory.decodeStream(in));

        } catch (IOException io1) {
            io1.printStackTrace();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    public void tiraFoto(View v) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                arquivoFoto = criarArquivo();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (arquivoFoto != null) {
                Uri photoURI = FileProvider.getUriForFile(getBaseContext(),
                        getBaseContext().getApplicationContext().getPackageName() +
                                ".provider", arquivoFoto);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA);
            }
        }
    }

    public File criarArquivo() throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File imagem = new File(folderAlbum.getAbsolutePath() + File.separator
                + "FotoBebe_" + timeStamp + ".jpg");
        return imagem;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == CAMERA) {
            sendBroadcast(new Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.fromFile(arquivoFoto))
            );
        }
        if (resultCode == RESULT_OK && requestCode == GALERIA_IMAGENS) {
            try {
                Uri selectedImage = data.getData();
                InputStream is = getContentResolver().openInputStream(selectedImage);
                OutputStream os = new FileOutputStream(arquivoFoto);
                byte[] dataA = new byte[is.available()];
                is.read(dataA);
                os.write(dataA);
                is.close();
                os.close();

                MediaScannerConnection.scanFile(this,
                        new String[]{arquivoFoto.toString()}, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String path, Uri uri) {
                                Log.i("ExternalStorage", "Scanned " + path + ":");
                                Log.i("ExternalStorage", "-> uri=" + uri);
                            }
                        });

            } catch (IOException e) {
                Log.w("ExternalStorage", "Error writing " + arquivoFoto, e);
            }
        }
        carregarFotos();
    }
}

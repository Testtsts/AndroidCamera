package com.example.camera;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> launcherKamera = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data == null) {
                        return;
                    }

                    Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                    MainActivity.this.saveFile(bitmap);
                }
            }
    );

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.imageButtonAmbilPhoto) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                MainActivity.this.launcherKamera.launch(intent);
            }
            else if (id == R.id.imageButtonServer) {
                MainActivity.this.tampilInput();
            }

        }
    };
    private String serverURL = "";

    private void tampilInput() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        this.imageButtonAmbilPhoto = (ImageButton) this.findViewById(R.id.imageButtonAmbilPhoto);
        this.imageButtonServer = (ImageButton) this.findViewById(R.id.imageButtonServer);
        this.recyclerViewFile = (RecyclerView) this.findViewById(R.id.recyclerViewFile);

        this.imageButtonAmbilPhoto.setOnClickListener(this.onClickListener);
        this.imageButtonServer.setOnClickListener(this.onClickListener);

        this.recyclerViewFile.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        this.recyclerViewFile.setAdapter(new FilesRecyclerViewAdapter(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String filePath = (String) v.getTag();

                Intent intent = new Intent(MainActivity.this, FotoActivity.class);
                intent.putExtra("server address", MainActivity.this.serverURL);
                intent.putExtra("filepath", filePath);

                startActivity(intent);
            }
        }));

    }



    private void saveFile(Bitmap bitmap) {
        Date date = new Date();
        String fileName = new SimpleDateFormat("yyyyMMdd-hh-mm-ss", Locale.US).format(date) + ".png";

        new Thread(() -> {
            HttpURLConnection connection;
            DataOutputStream writer = null;

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            try {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] imageData = byteArrayOutputStream.toByteArray();

                URL url = new URL(MainActivity.this.serverURL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=*****");
                connection.setRequestProperty("Accept", "application/json");
                connection.setDoOutput(true);

                writer = new DataOutputStream(connection.getOutputStream());
                writer.writeBytes("--*****\r\n");
                writer.writeBytes("Content-Disposition: form-data; name=\"foto\";filename=\"" + fileName + "\"\r\n");
                writer.writeBytes("\r\n");
                writer.write(imageData);
                writer.writeBytes("\r\n");
                writer.writeBytes("--*****--\r\n");

                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Error upload foto ke server", Toast.LENGTH_SHORT).show();
                    });
                }

                runOnUiThread(MainActivity.this::loadFilesFromServer);
            }
            catch (Exception e) {
                Log.d("Error", e.toString());
            }
            finally {
                try {
                    writer.close();
                }
                catch (Exception ignored) {}
            }
        }).start();
    }

    private void loadFilesFromServer() {
        new Thread(() -> {
            HttpURLConnection connection;
            BufferedReader reader = null;
            StringBuilder response = new StringBuilder();

            try {
                URL url = new URL(MainActivity.this.serverURL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Error mengambil data dari server", Toast.LENGTH_SHORT).show();
                    });
                    throw new Exception();
                }

                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                ArrayList<FileData> result = new ArrayList<>();

                JSONObject responseObject = new JSONObject(response.toString());
                JSONArray files = responseObject.getJSONArray("files");
                for(int i = 0; i < files.length(); i++) {
                    JSONObject file = files.getJSONObject(i);
                    result.add(new FileData(file.getInt("id"), file.getString("file_name"), file.getString("path")));
                }

                runOnUiThread(() -> {
                    FilesRecyclerViewAdapter adapter = (FilesRecyclerViewAdapter) MainActivity.this.recyclerViewFile.getAdapter();
                    try {
                        Objects.requireNonNull(adapter).setItem(result);
                    }
                    catch (Exception ignored) {}
                });
            }
            catch (Exception e) {
                Log.d("Error", e.toString());
            }
            finally {
                try {
                    Objects.requireNonNull(reader).close();
                }
                catch (Exception ignored) {}
            }
        }).start();
    }

}
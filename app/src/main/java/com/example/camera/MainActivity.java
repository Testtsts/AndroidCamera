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
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
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

                    Bitmap bitmap = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
                    MainActivity.this.saveFile(bitmap);
                }
            }
    );

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.Btn_take_pict) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                MainActivity.this.launcherKamera.launch(intent);
            }
            else if (id == R.id.Btn_load_pict){
                runOnUiThread(MainActivity.this::loadFilesFromServer);
            }
        }
    };
    private String serverURL = "https://moray-evident-globally.ngrok-free.app";

    private void tampilInput() {
    }

    Button imageButtonAmbilPhoto;
    Button imageButtonServer;
    RecyclerView recyclerViewfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.camera_view), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        MainActivity.this.launcherKamera.launch(intent);


        this.imageButtonAmbilPhoto = (Button) this.findViewById(R.id.Btn_take_pict);
        this.imageButtonServer=(Button) this.findViewById(R.id.Btn_load_pict) ;
        this.recyclerViewfile = this.findViewById(R.id.camera_view);
        this.recyclerViewfile.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));

        this.imageButtonAmbilPhoto.setOnClickListener(this.onClickListener);
        this.imageButtonServer.setOnClickListener(this.onClickListener);
        this.recyclerViewfile.setAdapter(new FilesRecyclerViewAdapter());


    }

    private Bitmap draw_box(Bitmap imageBitmap, ArrayList<ResponseData> responseData, int width, int height) {
        Canvas canvas = new Canvas(imageBitmap);

        Paint bounding_box = new Paint();
        bounding_box.setColor(Color.RED);
        bounding_box.setStyle(Paint.Style.STROKE);
        bounding_box.setStrokeWidth(5);

        Paint class_confidence = new Paint();
        class_confidence.setColor(Color.BLACK); // Set text color (can be any color)
        class_confidence.setTextSize(30f); // Set text size in pixels
        class_confidence.setTextAlign(Paint.Align.CENTER); // Set text alignment (CENTER, LEFT, RIGHT)

        for (int i = 0; i < responseData.size(); i++){
            ResponseData rd = responseData.get(i);
            canvas.drawRect(rd.getLeft(), rd.getTop(), rd.getRight(), rd.getBottom(), bounding_box);
            canvas.drawText(rd.getClassLabel() + " " + String.valueOf(rd.getConfidence()), rd.getLeft(), rd.getTop()+1, class_confidence);
            Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        }
        return imageBitmap;
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
            HttpURLConnection connectionTagging;
            BufferedReader reader = null;
            StringBuilder response = new StringBuilder();

            try {
                URL urlTag = new URL(MainActivity.this.serverURL+"/tagged");
                connectionTagging = (HttpURLConnection) urlTag.openConnection();
                connectionTagging.setRequestMethod("POST");

                if (connectionTagging.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Error mengambil data dari server", Toast.LENGTH_SHORT).show();
                    });
                    throw new Exception();
                }


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

                ArrayList<ResponseData> responseDatas = new ArrayList<>();

                JSONObject responseObject = new JSONObject(response.toString());

                JSONArray predict = responseObject.getJSONArray("predictions");

                for(int i=0; i<predict.length(); i++){
                    JSONObject data = predict.getJSONObject(i);
                    double x1 = data.getDouble("x");
                    double y1 = data.getDouble("y");
                    double width = data.getDouble("width");
                    double height = data.getDouble("height");
                    String classLabel = data.getString("class");
                    double confidence = data.getDouble("confidence");

                    double right = x1 + width;
                    double bottom = y1 + height;
                    responseDatas.add(new ResponseData((float) x1, (float) y1, (float) right, (float) bottom,confidence,classLabel));
                }

                Bitmap predictedImage = draw_box(BitmapFactory.decodeStream(connectionTagging.getInputStream()), responseDatas, 1000, 1000);
                Iterator<String> keys = responseObject.keys();
                int iteratorIdx=0;
                while(keys.hasNext()) {
                    String key = keys.next();
                        result.add(new FileData(iteratorIdx,key, responseObject.get(key).toString()));
                        iteratorIdx++;
                }


                runOnUiThread(() -> {
                    FilesRecyclerViewAdapter adapter = (FilesRecyclerViewAdapter) MainActivity.this.recyclerViewfile.getAdapter();
                    try {
                        Objects.requireNonNull(adapter).setArrayListFileData(result);
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
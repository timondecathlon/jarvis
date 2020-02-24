package com.example.jarvis;


import android.Manifest;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;

import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String fileName = null;

    //private RecordButton recordButton = null;
    private MediaRecorder recorder = null;

    //private Button playButton = findViewById(R.id.playButton);
    private MediaPlayer player = null;

    //private TextView infoLine = findViewById(R.id.infoLine);

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();

    }


    //метод который вызывается при записи
    private void onRecord(boolean start) {
        if (start) {
            startRecording();  //если true то запускает
        } else {
            stopRecording();   //если false то стопорит
        }
    }

    //Метод который ЗАПУСКАЕТ и ОСТАНАВЛИВАЕТ воспроизведение
    private void onPlay(boolean start) {
        if (start) {
            startPlaying();  //если true то запускает
        } else {
            stopPlaying();   //если false то стопорит
        }
    }




    //метод который запускает воспроизведение
    private void startPlaying() {
        //экземпляр класа для проигрывания
        player = new MediaPlayer();
        try {
            //сообщаем путь к файлу для воспроизведения
            player.setDataSource(fileName);
            player.prepare();
            player.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    //метод для остановки воспроизведения
    private void stopPlaying() {
        player.release();
        player = null;
    }



    //метод который вызывается при старте записи
    private void startRecording() {

        TextView infoLine = findViewById(R.id.infoLine);
        infoLine.setText("Записывается");

        //обьект для работы с записью
        recorder = new MediaRecorder();

        //тип считывания
        recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION);
        //формат для хранения записи
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        //путь хранения файла
        recorder.setOutputFile(fileName);
        //тип кодировки файла
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            //полготавливаемся к записи
            recorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        //стартуем запись
        recorder.start();
    }


    //вызывается при остановке записи
    private void stopRecording() {

        TextView infoLine = findViewById(R.id.infoLine);
        infoLine.setText("Обрабатывается");

        //стопорит рекордер
        recorder.stop();

        recorder.release();
        recorder = null;

        //отправляем на сервак
        fileName = getExternalCacheDir().getAbsolutePath();
        fileName += "/audiorecordtest.3gp";
        FilesUploadingTask res = new FilesUploadingTask(fileName);
        AsyncTask res1 = res.execute();


        try{
            infoLine.setText("Ожидание");
            //для проверки индекса
            Toast.makeText(getApplicationContext(), String.valueOf(res1.get()), Toast.LENGTH_SHORT).show();
            //воспроизводим ответ
            startAnswer(Integer.parseInt(String.valueOf(res1.get())));

        }catch (Exception e){
            Log.e(LOG_TAG, "22222");
        }


    }

    //тест
    private void startAnswer(int code) {
        //массив айдишников файлов реплик
        int[] list = {
                R.raw.yessir,
                R.raw.yes,
                R.raw.yessir,
                R.raw.yessir2,
                R.raw.k_uslugam,
                R.raw.as_you_want,
                R.raw.power_off,
                R.raw.k_uslugam,
                R.raw.vsegda_k_uslugam,
                R.raw.request_done
        };
        //считывание реплики и воспроизведение
        MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), list[code]);
        mediaPlayer.start();
    }

    /*
    //создаем программно кнопку ЗАПИСИ и вещаем обработчик клика
    class RecordButton extends Button {
        boolean mStartRecording = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                    setText("Stop recording");
                    //processLine.setText("Записывается");
                } else {
                    setText("Start recording");
                    //processLine.setText("Обрабатывается");
                }
                mStartRecording = !mStartRecording;
            }
        };

        public RecordButton(Context ctx) {
            super(ctx);
            setText("Start recording");
            setOnClickListener(clicker);
            //setOnTouchListener(toucher);
        }



        //пытаемся  обработать зажатие

    }



    //создаем программно кнопку ВОСПРОИЗВЕДЕНИЯ и вещаем обработчик клика
    class PlayButton extends Button {
        boolean mStartPlaying = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onPlay(mStartPlaying);
                if (mStartPlaying) {
                    setText("Stop playing");
                } else {
                    setText("Start playing");
                }
                mStartPlaying = !mStartPlaying;
            }
        };

        public PlayButton(Context ctx) {
            super(ctx);
            setText("Start playing");
            setOnClickListener(clicker);
        }
    }


     */


    /*
    //создаем программно кнопку TEST для тестирвания удержания
    class TestButton extends Button {
        boolean mStartRecording = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                    setText("Stop recording");
                } else {
                    setText("Start recording");
                }
                mStartRecording = !mStartRecording;
            }
        };

        public TestButton(Context ctx) {
            super(ctx);
            setText("Start recording");
            setOnClickListener(clicker);
            setOnTouchListener(toucher);
        }


        OnTouchListener toucher = new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                TextView detect = findViewById(R.id.textViewEvent);
                detect.setText("Вы удерживаете кнопку");
                return false;
            }
        };


        //пытаемся  обработать зажатие

    }

     */


    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Record to the external cache directory for visibility
        fileName = getExternalCacheDir().getAbsolutePath();
        fileName += "/audiorecordtest.3gp";

        //super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //динамическое разрешение на запись аудио
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        //Запрещаем поворот
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        //заполняем переменную  sensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        //
        sensorLinAccel = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }

        if (player != null) {
            player.release();
            player = null;
        }
    }


    //-------------------------------------------------------------------------------------------123
    //переменная типа SensorManager
    SensorManager sensorManager;

    //переменная типа Sensor (для линейного ускорения)
    Sensor sensorLinAccel;

    //переменная типа Timer
    Timer timer;

    //
    long currentTime;
    long allowTime = 0;

    //видимо готовый метод ибо перезаписываем
    @Override
    protected void onResume() {
        super.onResume();

        //видимо вешаем слушателя
        sensorManager.registerListener(listener, sensorLinAccel, SensorManager.SENSOR_DELAY_NORMAL);

        //создаем экземпляр класса Timer
        timer = new Timer();

        //создаем экземпляр класса TimerTask (походу создаем такт)
        TimerTask task = new TimerTask() {
            //перезаписываем готовый метод run()
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //вызываем метод showInfo
                        showInfo();
                    }
                });
            }
        };

        //метод schedule() запускает метод каждый 400мс и задержка 0  (заготовка которой можно запускать аналог setInterval())
        timer.schedule(task, 0, 400);
    }


    //перезагружаем метод снимаем слушателя
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(listener);
        timer.cancel();
    }


    //переменные которые наполняем в рних храним
    float[] valuesLinAccel = new float[3];

    SensorEventListener listener = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        //при изменении сенсоров
        @Override
        public void onSensorChanged(SensorEvent event) {
            //смотри у события тип сенсора
            for (int i = 0; i < 3; i++) {
                valuesLinAccel[i] = event.values[i];
            }
        }
    };

    boolean mStartListening = true;

    void showInfo() {
        //если новое значение больше зафиксированного максимума то перезаписываем
        double sum = Math.sqrt(valuesLinAccel[0]*valuesLinAccel[0] + valuesLinAccel[1]*valuesLinAccel[1] + valuesLinAccel[2]*valuesLinAccel[2]);

        double max = 5.00;

        if(sum > max){


            currentTime = System.currentTimeMillis() / 1000L;

            //Toast.makeText(getApplicationContext(), String.valueOf(currentTime), Toast.LENGTH_SHORT).show();


            if(currentTime > allowTime){

                //вибрирует при новом рекорде
                long mills = 100L;
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                if (vibrator.hasVibrator()) {
                    vibrator.vibrate(mills);
                }

                onRecord(mStartListening);

                allowTime = currentTime + 1;

                //Toast.makeText(getApplicationContext(), String.valueOf(allowTime), Toast.LENGTH_SHORT).show();

                mStartListening = !mStartListening;
            }


            //Toast.makeText(getApplicationContext(), "Test", Toast.LENGTH_SHORT).show();

        }

    }
    //--------------------------------------------------------------------------------------------------------




    /**
     * Загружает файл на сервер
     */
    public class FilesUploadingTask extends AsyncTask<Void, Void, String> {

        // Конец строки
        private String lineEnd = "\r\n";
        // Два тире
        private String twoHyphens = "--";
        // Разделитель
        private String boundary =  "----WebKitFormBoundary9xFB2hiUhzqbBQ4M";

        // Переменные для считывания файла в оперативную память
        private int bytesRead, bytesAvailable, bufferSize;
        private byte[] buffer;
        private int maxBufferSize = 1*1024*1024;

        // Путь к файлу в памяти устройства
        private String filePath;

        // Адрес метода api для загрузки файла на сервер
        public static final String API_FILES_UPLOADING_PATH = "https://ironlinks.ru/android/upload.php";

        // Ключ, под которым файл передается на сервер
        public static final String FORM_FILE_NAME = "file1";

        public FilesUploadingTask(String filePath) {
            this.filePath = filePath;
        }

        @Override
        protected String doInBackground(Void... params) {
            // Результат выполнения запроса, полученный от сервера
            String result = null;

            try {
                // Создание ссылки для отправки файла
                URL uploadUrl = new URL(API_FILES_UPLOADING_PATH);

                // Создание соединения для отправки файла
                HttpURLConnection connection = (HttpURLConnection) uploadUrl.openConnection();

                // Разрешение ввода соединению
                connection.setDoInput(true);
                // Разрешение вывода соединению
                connection.setDoOutput(true);
                // Отключение кеширования
                connection.setUseCaches(false);

                // Задание запросу типа POST
                connection.setRequestMethod("POST");

                // Задание необходимых свойств запросу
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);

                // Создание потока для записи в соединение
                DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());

                // Формирование multipart контента

                // Начало контента
                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                // Заголовок элемента формы
                outputStream.writeBytes("Content-Disposition: form-data; name=\"" +
                        FORM_FILE_NAME + "\"; filename=\"" + filePath + "\"" + lineEnd);
                // Тип данных элемента формы
                //outputStream.writeBytes("Content-Type: image/jpeg" + lineEnd);
                outputStream.writeBytes("Content-Type: audio/ogg" + lineEnd);
                // Конец заголовка
                outputStream.writeBytes(lineEnd);

                // Поток для считывания файла в оперативную память
                FileInputStream fileInputStream = new FileInputStream(new File(filePath));

                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // Считывание файла в оперативную память и запись его в соединение
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {
                    outputStream.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                // Конец элемента формы
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Получение ответа от сервера
                int serverResponseCode = connection.getResponseCode();

                // Закрытие соединений и потоков
                fileInputStream.close();
                outputStream.flush();
                outputStream.close();

                // Считка ответа от сервера в зависимости от успеха
                if(serverResponseCode == 200) {
                    result = readStream(connection.getInputStream());
                } else {
                    result = readStream(connection.getErrorStream());
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result;
        }

        // Считка потока в строку
        public  String readStream(InputStream inputStream) throws IOException {
            StringBuffer buffer = new StringBuffer();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            return buffer.toString();
        }
    }
}

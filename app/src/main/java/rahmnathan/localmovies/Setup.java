package rahmnathan.localmovies;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import Phone.Phone;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by nathan on 3/4/16.
 */

public class Setup extends Activity {

    private EditText chrome;
    private EditText phone;
    private EditText name;
    private EditText server;
    private EditText path;

    private Context context;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public Setup(Context context){
        this.context = context;
    }

    public Setup(){
        ;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setup_main);

        chrome = (EditText) findViewById(R.id.chrome);
        phone = (EditText) findViewById(R.id.phone);
        name = (EditText) findViewById(R.id.phoneName);
        server = (EditText) findViewById(R.id.ServerIP);
        path = (EditText) findViewById(R.id.inputPath);

        // Populating our textfields with our saved data if it exists

        try {
            chrome.setText(MainActivity.myPhone.getCastIP());
            phone.setText(MainActivity.myPhone.getPhoneIP());
            name.setText(MainActivity.myPhone.getPhoneName());
            server.setText(MainActivity.myPhone.getComputerIP());
            path.setText(MainActivity.myPhone.getMainPath());
        } catch (NullPointerException e){
            e.printStackTrace();
        }

        Button set = (Button) findViewById(R.id.set);
        set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData(chrome.getText().toString(), phone.getText().toString(),
                        name.getText().toString(), server.getText().toString(),
                        path.getText().toString());
            }
        });
    }

    private void saveData(String chrome, String phone, String name, String server, String path){

        try {
            ActivityCompat.requestPermissions(
                    Setup.this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );

            // Navigating to setup file and writing data to it

            File setupFile = new File(Environment.getExternalStorageDirectory(), "setup.txt");
            if (!setupFile.exists()){
                setupFile.createNewFile();
            }

            Phone myPhone = new Phone(chrome, phone, name, path);
            myPhone.setComputerIP(server);

            ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(setupFile));
            os.writeObject(myPhone);
            os.close();

            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Phone getPhoneInfo() {

        File setupFile = new File(Environment.getExternalStorageDirectory(), "setup.txt");
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(setupFile));
            MainActivity.myPhone = (Phone) objectInputStream.readObject();
            objectInputStream.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        MainActivity.myPhone.setPath("initial" + MainActivity.myPhone.getMainPath() + "Movies/");

        return MainActivity.myPhone;
    }
}


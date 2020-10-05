package com.example.qr_catalog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScanCodeActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    int MY_PERMISSION_REQUEST_CAMERA=0;
    ZXingScannerView scannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //beállítja hogy ne új activity-t nyisson hanem kamerát
        scannerView = new ZXingScannerView(this);
        setContentView(scannerView);
    }


    @Override
    public void handleResult(Result result) {
        MainActivity.resulttextview.setText(result.getText()); //main activity-be lévő textview-nak adja az eredményt
        finish(); //lezárja a ScanCodeActivity-t visszadob a main-re
    }

    @Override
    protected void onPause() {
        super.onPause();
        scannerView.stopCamera(); //kamera megállítása
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)!=
        PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},MY_PERMISSION_REQUEST_CAMERA);
            //ha nincs engedély akkor újra kér enegedélyt
        }
        scannerView.setResultHandler(this);
        scannerView.startCamera();
    }
}
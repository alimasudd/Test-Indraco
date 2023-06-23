package id.alimasudd.testindraco;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;

import androidx.appcompat.app.AppCompatActivity;

import id.alimasudd.testindraco.databinding.ActivityResultCameraBinding;
import id.alimasudd.testindraco.utils.SessionManager;

public class ResultCamera extends AppCompatActivity{

    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityResultCameraBinding binding;
        binding = ActivityResultCameraBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        String previouslyEncodedImage = sessionManager.getImage();

        if( !previouslyEncodedImage.equalsIgnoreCase("") ){
            byte[] b = Base64.decode(previouslyEncodedImage, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
            binding.img1.setImageBitmap(bitmap);
        }

        binding.txLat.setText(sessionManager.getLatitude());
        binding.txLong.setText(sessionManager.getLongitude());

    }
}
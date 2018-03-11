package com.example.atharva.photo_js;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import org.mozilla.javascript.ScriptableObject;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{

    private static final String TAG = "MainActivity";

    JavaCameraView javac;

    Mat mgrba,mGray,mCanny,mgrbf,mgrbt;

    Button b1;

    ImageView i1;

    Bitmap bmap, image, pic;

    private boolean showPreviews=false;

    private TessBaseAPI mTess;
    String datapath = "";
    String jscode = "";
    String output = "";




    BaseLoaderCallback mLoaderCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case BaseLoaderCallback.SUCCESS:{

                  b1.setOnClickListener(new View.OnClickListener() {


                      @Override
                      public void onClick(View v) {
                          showPreviews = !showPreviews;
                          pic = bmap;


                      }
                  });
                    javac.enableView();

                    break;
                }
                default:{
                    super.onManagerConnected(status);
                    break;
                }
            }
        }
    };



    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
    static{

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        setContentView(R.layout.activity_main);


        b1 = findViewById(R.id.b1);

        i1 = findViewById(R.id.i1);

        javac = findViewById(R.id.javac);

        javac.setVisibility(SurfaceView.VISIBLE);


        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // If you do not have permission, request it
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        } else {
            // Launch the camera if the permission exists
            javac.setVisibility(SurfaceView.VISIBLE);
            javac.setCvCameraViewListener(this);


        }

        image = BitmapFactory.decodeResource(getResources(), R.drawable.screenshot2);

        //initialize Tesseract API
        String language = "eng";
        datapath = getFilesDir()+ "/tesseract/";
        mTess = new TessBaseAPI();

        checkFile(new File(datapath + "tessdata/"));

        mTess.init(datapath, language);
    }

    private void checkFile(File dir) {
        if (!dir.exists()&& dir.mkdirs()){
            copyFiles();
        }
        if(dir.exists()) {
            String datafilepath = datapath+ "/tessdata/eng.traineddata";
            File datafile = new File(datafilepath);

            if (!datafile.exists()) {
                copyFiles();
            }
        }
    }

    private void copyFiles() {
        try {
            String filepath = datapath + "/tessdata/eng.traineddata";
            AssetManager assetManager = getAssets();

            InputStream instream = assetManager.open("tessdata/eng.traineddata");
            OutputStream outstream = new FileOutputStream(filepath);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, read);
            }


            outstream.flush();
            outstream.close();
            instream.close();

            File file = new File(filepath);
            if (!file.exists()) {
                throw new FileNotFoundException();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }






    // Example of a call to a native method
       // TextView tv = (TextView) findViewById(R.id.sample_text);
        //tv.setText(stringFromJNI());


    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    //public native String stringFromJNI();


    /*public void complie(View v){

    }*/

    @Override
    protected void onPause() {
        super.onPause();
        if(javac!=null){
            javac.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(javac!=null){
            javac.disableView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(OpenCVLoader.initDebug()){
            Log.i(TAG,"opencv done");

            mLoaderCallBack.onManagerConnected(LoaderCallbackInterface.SUCCESS);

        }
        else{
            Log.i(TAG,"not done");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION,this,mLoaderCallBack);
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
            mgrba = new Mat(height,width, CvType.CV_8UC4);

            mgrbf = new Mat(height,width, CvType.CV_8UC4);

            mgrbt = new Mat(height,width, CvType.CV_8UC4);


        mGray = new Mat(height,width, CvType.CV_8UC1);

        mCanny = new Mat(height,width, CvType.CV_8UC1);

        /*Core.transpose(mgrba, mgrbt);
        Imgproc.resize(mgrbt, mgrbf, mgrbf.size(), 0,0, 0);
        Core.flip(mgrbf, mgrba, 1 );*/


    }

    @Override
    public void onCameraViewStopped() {

        mgrba.release();

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        mgrba = inputFrame.rgba();

        Core.transpose(mgrba, mgrbt);
        Imgproc.resize(mgrbt, mgrbf, mgrbf.size(), 0,0, 0);
       Core.flip(mgrbf, mgrba, 1 );


        if(showPreviews) {



            //Mat tmp = new Mat (height, width, CvType.CV_8U, new Scalar(4));

            bmap = Bitmap.createBitmap(javac.getWidth()/4,javac.getHeight()/4, Bitmap.Config.ARGB_8888);






            Imgproc.cvtColor(mgrba,mGray,Imgproc.COLOR_RGB2GRAY);

            Imgproc.equalizeHist(mGray, mGray);
            Imgproc.threshold(mGray, mGray, 100, 200, Imgproc.THRESH_OTSU);

            //Imgproc.Canny(mGray,mCanny,10,100);

            //try {
                bmap = Bitmap.createBitmap(mGray.cols(), mGray.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(mGray, bmap);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

//stuff that updates ui
                    i1.setImageBitmap(bmap);
                    i1.invalidate();

                }
            });

            /*}catch(Exception ex){
                Log.i("error","not working");
            }*/





            return mgrba;
        }

            return mgrba;

    }
    public void processImage(View view){
        String OCRresult = null;
        mTess.setImage(pic);
        OCRresult = mTess.getUTF8Text();
        //TextView OCRTextView = (TextView) findViewById(R.id.OCRTextView);
        //OCRTextView.setText(OCRresult);

        Log.i("code", OCRresult);
        jscode = OCRresult;
        getOutput();
    }
    private void getOutput()
    {
        /*ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("js");
        Object result = engine.eval(s);
        System.out.println(result.toString());*/

        org.mozilla.javascript.Context rhino = org.mozilla.javascript.Context.enter();
// turn off optimization to work with android
        rhino.setOptimizationLevel(-1);

        try {
            ScriptableObject scope = rhino.initStandardObjects();
            String result = rhino.evaluateString(scope, jscode, "JavaScript", 1, null).toString();
            Log.i("result", result);
            output = result;
        } catch (Exception e) {
            Log.i("error", "Syntax Error");
            output = "Syntax error";
        } finally {
            org.mozilla.javascript.Context.exit();
        }

        Toast.makeText(this, output, Toast.LENGTH_LONG).show();

    }
}


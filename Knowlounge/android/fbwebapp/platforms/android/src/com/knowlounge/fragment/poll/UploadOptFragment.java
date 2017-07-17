package com.knowlounge.fragment.poll;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ipaulpro.afilechooser.utils.FileUtils;
import com.knowlounge.R;
import com.knowlounge.view.room.RoomActivity;
import com.knowlounge.model.PollCreateData;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Mansu on 2016-12-14.
 */

public class UploadOptFragment extends PollDialogFragment {

    private final String TAG = "UploadOptFragment";

    View rootView;

    private LinearLayout optCamera, optImage;
    private TextView confirmBtn;
    private ImageView backBtn;

    private ImageView icoCamera, icoImage;
    private TextView txtCamera, txtImage;

    private Context context;

    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    private Uri mImageCaptureUri = null;
    private File pictureFile = null;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;

        fragmentManager = getFragmentManager();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        if (rootView == null)
            rootView = inflater.inflate(R.layout.poll_drawing_upload, container, false);

        optCamera = (LinearLayout) rootView.findViewById(R.id.opt_camera);
        icoCamera = (ImageView) rootView.findViewById(R.id.ico_opt_camera);
        txtCamera = (TextView) rootView.findViewById(R.id.txt_opt_camera);

        optImage = (LinearLayout) rootView.findViewById(R.id.opt_image);
        icoImage = (ImageView) rootView.findViewById(R.id.ico_opt_image);
        txtImage = (TextView) rootView.findViewById(R.id.txt_opt_image);

        confirmBtn = (TextView) rootView.findViewById(R.id.btn_confirm);
        backBtn = (ImageView) rootView.findViewById(R.id.btn_back);

        optCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
                if (pictureFile != null)
                    pictureFile = null;
                pictureFile = new File(Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM + "/Camera", url);

                mImageCaptureUri = Uri.fromFile(pictureFile);

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                takePictureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
                startActivityForResult(takePictureIntent, 9001);
            }
        });
        optImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent target = FileUtils.createGetContentIntent("image/*");
                Intent intent = Intent.createChooser(target, getActivity().getString(R.string.chooser_title));
                startActivityForResult(intent, 9002);
            }
        });
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RoomActivity.activity.pollData.setIsChange(true);
                RoomActivity.activity.pollData.setDrawingMethod(PollCreateData.DIRECT_UPLOAD);
                getFragmentManager().popBackStack();
            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 뒤로 가기 버튼 클릭..
                getFragmentManager().popBackStack();
            }
        });

        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult / requestCode : " + requestCode + ", resultCode : " + resultCode);

        if (resultCode == RESULT_OK) {
            RoomActivity.activity.pollData.setDrawingMethod(PollCreateData.DIRECT_UPLOAD);

            try {
                final Uri imageUri =  requestCode == 9001 ? mImageCaptureUri : data.getData();
                final InputStream imageStream = context.getContentResolver().openInputStream(imageUri);
                Bitmap imgBitmap = BitmapFactory.decodeStream(imageStream);

                imgBitmap = requestCode == 9001 ? fixPictureRotation(imgBitmap) : imgBitmap;


                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                imgBitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);
                byte[] byteArr = baos.toByteArray();
                //String binary = Base64.encodeToString(byteArr, Base64.DEFAULT);
                String binary = com.knowlounge.util.Base64.encode(byteArr);

                RoomActivity.activity.pollData.setCapturedImgBinary(binary);
            } catch (FileNotFoundException e) {
                e.printStackTrace();

            } finally {
                switch (requestCode) {
                    case 9001:
                        typeImgChecked(icoCamera, icoImage, null, null);
                        typeTextChecked(txtCamera, txtImage, null, null);
                        break;
                    case 9002:
                        typeImgChecked(icoImage, icoCamera, null, null);
                        typeTextChecked(txtImage, txtCamera, null, null);
                        break;
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private Bitmap fixPictureRotation(Bitmap bitmap) {
        try {
            ExifInterface exif = new ExifInterface(pictureFile.getAbsolutePath());
            int pictureOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
            Matrix matrix = new Matrix();
            switch (pictureOrientation) {
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    matrix.setScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.setRotate(180);
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    matrix.setRotate(180);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_TRANSPOSE:
                    matrix.setRotate(90);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.setRotate(90);
                    break;
                case ExifInterface.ORIENTATION_TRANSVERSE:
                    matrix.setRotate(-90);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.setRotate(-90);
                    break;
            }

            Bitmap fixedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            //fixedBitmap.recycle();
            return fixedBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }
}

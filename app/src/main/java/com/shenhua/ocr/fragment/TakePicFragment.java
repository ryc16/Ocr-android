package com.shenhua.ocr.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.shenhua.ocr.R;
import com.shenhua.ocr.activity.ChoosePicActivity;
import com.shenhua.ocr.widget.CameraPreview;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.shenhua.ocr.utils.Common.ACTION_CROP;
import static com.shenhua.ocr.utils.Common.ACTION_PICK;
import static com.shenhua.ocr.utils.Common.FRAGMENT_DIALOG;
import static com.shenhua.ocr.utils.Common.REQUEST_CAMERA_PERMISSION;
import static com.shenhua.ocr.utils.Common.REQUEST_CROP_PICTURE;
import static com.shenhua.ocr.utils.Common.REQUEST_PICK_PICTURE;

/**
 * Created by shenhua on 2017-10-19-0019.
 *
 * @author shenhua
 *         Email shenhuanet@126.com
 */
public class TakePicFragment extends Fragment implements ActivityCompat.OnRequestPermissionsResultCallback {

    @BindView(R.id.cameraView)
    CameraPreview cameraView;
    Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_takepic, container, false);
        unbinder = ButterKnife.bind(this, mRootView);
        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
            return;
        }
        cameraView.onResume();
    }

    @Override
    public void onPause() {
        cameraView.onPause();
        super.onPause();
    }

    @OnClick({R.id.captureBtn, R.id.backBtn, R.id.albumBtn})
    void clicks(View view) {
        switch (view.getId()) {
            case R.id.captureBtn:
                cameraView.takePicture(new CameraPreview.CapturePictureListener() {
                    @Override
                    public void onCapture(File file) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Uri uri = Uri.fromFile(file);
                        startActivityForResult(new Intent(getContext(), ChoosePicActivity.class)
                                .putExtra("action", ACTION_CROP)
                                .putExtra("uri", uri), REQUEST_CROP_PICTURE);
                    }
                });
                break;
            case R.id.backBtn:
                getFragmentManager().popBackStack();
                break;
            case R.id.albumBtn:
                startActivityForResult(new Intent(getContext(), ChoosePicActivity.class)
                        .putExtra("action", ACTION_PICK), REQUEST_PICK_PICTURE);
                break;
            default:
                break;
        }
    }

    private void requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            new ConfirmationDialog().show(getChildFragmentManager(), FRAGMENT_DIALOG);
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "需要获取拍照权限,否则拍照功能无法使用", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_CANCELED || resultCode == Activity.RESULT_FIRST_USER) {
            return;
        }
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_PICK_PICTURE || requestCode == REQUEST_CROP_PICTURE) {
                getFragmentManager().popBackStack();
            }
        }
    }

    public static class ConfirmationDialog extends DialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Fragment parent = getParentFragment();
            return new AlertDialog.Builder(getActivity())
                    .setMessage("需要获取用户权限")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            parent.requestPermissions(new String[]{Manifest.permission.CAMERA},
                                    REQUEST_CAMERA_PERMISSION);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(getContext(), "相机无法使用", Toast.LENGTH_SHORT).show();
                                }
                            })
                    .create();
        }
    }

}

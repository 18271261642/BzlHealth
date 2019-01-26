package com.bozlun.health.android.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.aigestudio.wheelpicker.widgets.DatePick;
import com.aigestudio.wheelpicker.widgets.ProfessionPick;
import com.bumptech.glide.request.RequestOptions;
import com.bozlun.health.android.Commont;
import com.bozlun.health.android.siswatch.NewSearchActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bozlun.health.android.R;
import com.bozlun.health.android.base.BaseActivity;
import com.bozlun.health.android.imagepicker.PickerBuilder;
import com.bozlun.health.android.net.OkHttpObservable;
import com.bozlun.health.android.rxandroid.DialogSubscriber;
import com.bozlun.health.android.rxandroid.SubscriberOnNextListener;
import com.bozlun.health.android.siswatch.utils.Base64BitmapUtil;
import com.bozlun.health.android.util.Common;
import com.bozlun.health.android.util.ImageTool;
import com.suchengkeji.android.w30sblelibrary.utils.SharedPreferencesUtils;
import com.bozlun.health.android.util.ToastUtil;
import com.bozlun.health.android.util.URLs;
import com.bozlun.health.android.widget.SwitchIconView;
import com.flipboard.bottomsheet.BottomSheetLayout;
import com.flipboard.bottomsheet.commons.MenuSheetView;
import com.google.gson.Gson;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RequestExecutor;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.bozlun.health.android.util.Common.userInfo;

/**
 * Created by thinkpad on 2017/3/4.
 * 我的资料
 */

public class PersonDataActivity extends BaseActivity {
    private static final String TAG = "PersonDataActivity";

    private static final int GET_OPENCAMERA_CODE = 100;

    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.head_img)
    CircleImageView headImg;
    @BindView(R.id.code_et)
    EditText codeEt;
    @BindView(R.id.brithdayval_tv)
    TextView brithdayvalTv;
    @BindView(R.id.heightval_tv)
    TextView heightvalTv;
    @BindView(R.id.weightval_tv)
    TextView weightvalTv;
    @BindView(R.id.man_iconview)
    SwitchIconView manIconview;
    @BindView(R.id.women_iconview)
    SwitchIconView womenIconview;
    @BindView(R.id.bottomsheet)
    BottomSheetLayout bottomSheetLayout;

    private String height, weight, sexVal, brithdayVal, nickName;
    private DialogSubscriber dialogSubscriber;
    private SubscriberOnNextListener<String> subscriberOnNextListener;
    private boolean isSubmit;
    private ArrayList<String> heightList;
    private ArrayList<String> weightList;

    private Uri mCutUri;

    @Override
    protected void initViews() {
        tvTitle.setText(R.string.mine_data);
        manIconview.switchState();
        sexVal = "M";
        heightList = new ArrayList<>();
        weightList = new ArrayList<>();
        for (int i = 120; i < 231; i++) {
            heightList.add(i + " cm");
        }
        for (int i = 20; i < 200; i++) {
            weightList.add(i + " kg");
        }

        //请求打开相机的权限

        AndPermission.with(PersonDataActivity.this)
                .runtime()
                .permission(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {

                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {

                    }
                })
                .rationale(new Rationale<List<String>>() {
                    @Override
                    public void showRationale(Context context, List<String> data, RequestExecutor executor) {

                    }
                }).start();

        subscriberOnNextListener = new SubscriberOnNextListener<String>() {
            @Override
            public void onNext(String result) {
//                Log.e("PersonDataActivity", "--result--" + result);
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String resultCode = jsonObject.getString("resultCode");

//                    Log.e("PersonDataActivity", "------result----" + resultCode + "---" + jsonObject.getString("resultCode"));

                    if ("001".equals(resultCode)) {
                        if (isSubmit) {
//                            Log.e("PersonDataActivity", "----333------");
                            userInfo.setNickName(nickName);
                            userInfo.setSex(sexVal);
                            userInfo.setBirthday(brithdayVal);
                            userInfo.setHeight(height);
                            userInfo.setWeight(weight);
                            startActivity(new Intent(PersonDataActivity.this, NewSearchActivity.class));
                            finish();
                        } else {
//                            Log.e("PersonDataActivity", "----444------");
                            String imageUrl = jsonObject.optString("url");
                            userInfo.setImage(imageUrl);// .error(R.mipmap.touxiang)
//                            //   .error(R.drawable.piece_dot)
//                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
//                                    .centerCrop()
                            RequestOptions mRequestOptions = RequestOptions.circleCropTransform().diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .skipMemoryCache(true);
                            Glide.with(PersonDataActivity.this).load(imageUrl)
                                    .apply(mRequestOptions)
                                    .into(headImg);
                        }
                    } else {
                        ToastUtil.showShort(PersonDataActivity.this, getString(R.string.submit_fail));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_persondata;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_next, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        startActivity(new Intent(PersonDataActivity.this, NewSearchActivity.class));
        finish();
        return super.onOptionsItemSelected(item);

    }

    @OnClick({R.id.head_img, R.id.selectbirthday_relayout, R.id.selectheight_relayout, R.id.selectweight_relayout,
            R.id.confirmcompelte_btn, R.id.man_iconview, R.id.women_iconview})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.head_img: //选择头像
                if (AndPermission.hasPermissions(PersonDataActivity.this, Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    chooseHeadImg();
                } else {
                    AndPermission.with(PersonDataActivity.this)
                            .runtime()
                            .permission(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            .onGranted(new Action<List<String>>() {
                                @Override
                                public void onAction(List<String> data) {
                                    chooseHeadImg();
                                }
                            })
                            .onDenied(new Action<List<String>>() {
                                @Override
                                public void onAction(List<String> data) {

                                }
                            })
                            .start();
                }


                break;
            case R.id.selectbirthday_relayout:
                DatePick pickerPopWin = new DatePick.Builder(PersonDataActivity.this, new DatePick.OnDatePickedListener() {
                    @Override
                    public void onDatePickCompleted(int year, int month, int day, String dateDesc) {
                        brithdayVal = dateDesc;
                        brithdayvalTv.setText(dateDesc);
                    }
                }).textConfirm(getResources().getString(R.string.confirm)) //text of confirm button
                        .textCancel(getResources().getString(R.string.cancle)) //text of cancel button
                        .btnTextSize(16) // button text size
                        .viewTextSize(25) // pick view text size
                        .colorCancel(Color.parseColor("#999999")) //color of cancel button
                        .colorConfirm(Color.parseColor("#009900"))//color of confirm button
                        .minYear(1950) //min year in loop
                        .maxYear(2020) // max year in loop
                        .dateChose("2000-06-15") // date chose when init popwindow
                        .build();
                pickerPopWin.showPopWin(PersonDataActivity.this);
                break;
            case R.id.selectheight_relayout:

                ProfessionPick professionPopWin = new ProfessionPick.Builder(PersonDataActivity.this, new ProfessionPick.OnProCityPickedListener() {
                    @Override
                    public void onProCityPickCompleted(String profession) {
                        height = profession;
                        heightvalTv.setText(height);
                    }
                }).textConfirm(getResources().getString(R.string.confirm)) //text of confirm button
                        .textCancel(getResources().getString(R.string.cancle)) //text of cancel button
                        .btnTextSize(16) // button text size
                        .viewTextSize(25) // pick view text size
                        .colorCancel(Color.parseColor("#999999")) //color of cancel button
                        .colorConfirm(Color.parseColor("#009900"))//color of confirm button
                        .setProvinceList(heightList) //min year in loop
                        .dateChose("170 cm") // date chose when init popwindow
                        .build();
                professionPopWin.showPopWin(PersonDataActivity.this);
                break;
            case R.id.selectweight_relayout:
                ProfessionPick weightPopWin = new ProfessionPick.Builder(PersonDataActivity.this, new ProfessionPick.OnProCityPickedListener() {
                    @Override
                    public void onProCityPickCompleted(String profession) {
                        weight = profession;
                        weightvalTv.setText(profession);
                    }
                }).textConfirm(getResources().getString(R.string.confirm)) //text of confirm button
                        .textCancel(getResources().getString(R.string.cancle)) //text of cancel button
                        .btnTextSize(16) // button text size
                        .viewTextSize(25) // pick view text size
                        .colorCancel(Color.parseColor("#999999")) //color of cancel button
                        .colorConfirm(Color.parseColor("#009900"))//color of confirm button
                        .setProvinceList(weightList) //min year in loop
                        .dateChose("60 kg") // date chose when init popwindow
                        .build();
                weightPopWin.showPopWin(PersonDataActivity.this);
                break;
            case R.id.confirmcompelte_btn:
                //完成
                nickName = codeEt.getText().toString();
                if (TextUtils.isEmpty(nickName)) {
                    ToastUtil.showShort(PersonDataActivity.this, getString(R.string.write_nickname));
                } else if (TextUtils.isEmpty(brithdayVal)) {
                    ToastUtil.showShort(PersonDataActivity.this, getString(R.string.select_brithday));
                } else if (TextUtils.isEmpty(height)) {
                    ToastUtil.showShort(PersonDataActivity.this, getString(R.string.select_height));
                } else if (TextUtils.isEmpty(weight)) {
                    ToastUtil.showShort(PersonDataActivity.this, getString(R.string.select_weight));
                } else {
                    submitPersonData();
                }
                break;
            case R.id.man_iconview:
                sexVal = "M";
                //保存性别
                SharedPreferencesUtils.setParam(PersonDataActivity.this,Commont.USER_SEX,"M");
                if (!manIconview.isIconEnabled()) {
                    manIconview.switchState();
                    womenIconview.setIconEnabled(false);
                }
                break;
            case R.id.women_iconview:
                sexVal = "F";
                //保存性别
                SharedPreferencesUtils.setParam(PersonDataActivity.this,Commont.USER_SEX,"F");
                if (!womenIconview.isIconEnabled()) {
                    womenIconview.switchState();
                    manIconview.setIconEnabled(false);
                }
                break;
        }
    }

    //头像选择
    private void chooseHeadImg() {
        MenuSheetView menuSheetView =
                new MenuSheetView(PersonDataActivity.this, MenuSheetView.MenuType.LIST, R.string.select_photo, new MenuSheetView.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (bottomSheetLayout.isSheetShowing()) {
                            bottomSheetLayout.dismissSheet();
                        }
                        switch (item.getItemId()) {
                            case R.id.take_camera:  //拍照
                                // getImage(PickerBuilder.SELECT_FROM_CAMERA);
                                cameraPic();
                                break;
                            case R.id.take_Album:   //相册选择
                                getImage(PickerBuilder.SELECT_FROM_GALLERY);
//                                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                                intent.setType("image/*");
//                                startActivityForResult(intent,120);
                                break;
                            case R.id.cancle:
                                break;
                        }
                        return true;
                    }
                });
        menuSheetView.inflateMenu(R.menu.menu_takepictures);
        bottomSheetLayout.showWithSheetView(menuSheetView);
    }


    private void getImage(int type) {
        new PickerBuilder(PersonDataActivity.this, type)
                .setOnImageReceivedListener(new PickerBuilder.onImageReceivedListener() {
                    @Override
                    public void onImageReceived(Uri imageUri) {
                        headImg.setImageURI(imageUri);
                        uploadPic(ImageTool.getRealFilePath(PersonDataActivity.this, imageUri), 1);
                    }
                })
                .setImageName("headImg")
                .setImageFolderName("NewBluetoothStrap")
                .setCropScreenColor(Color.CYAN)
                .setOnPermissionRefusedListener(new PickerBuilder.onPermissionRefusedListener() {
                    @Override
                    public void onPermissionRefused() {

                    }
                })
                .start();
    }

    private void uploadPic(String filePath, int flag) {
        Gson gson = new Gson();
        HashMap<String, Object> map = new HashMap<>();
        map.put("userId", Common.customer_id);
        if (flag == 0) {  //相机拍摄
            map.put("image", filePath);
        } else {//图片选择
            map.put("image", ImageTool.GetImageStr(filePath));
        }

        String mapjson = gson.toJson(map);
//        Log.e("PersonDataActivity", "----111------" + mapjson);
        dialogSubscriber = new DialogSubscriber(subscriberOnNextListener, PersonDataActivity.this);
        OkHttpObservable.getInstance().getData(dialogSubscriber, URLs.HTTPs + URLs.ziliaotouxiang, mapjson);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        Log.e(TAG, "-----result-=" + requestCode + "--resu=" + resultCode);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 120: //从相册图片后返回的uri
                    if (data != null) {
                        handlerImageOnKitKat(data);
                    }
                    //启动裁剪
                    //startActivityForResult(CutForPhoto(data.getData()),111);
                    break;
                case 1001: //相机返回的 uri
                    //启动裁剪
                    String path = getExternalCacheDir().getPath();
//                    Log.e(TAG, "----裁剪path=" + path);
                    String name = "output.png";
                    startActivityForResult(CutForCamera(path, name), 111);
                    break;
                case 111:
                    try {
                        if (mCutUri != null) {
                            //获取裁剪后的图片，并显示出来
                            Bitmap bitmap = BitmapFactory.decodeStream(
                                    this.getContentResolver().openInputStream(mCutUri));
                            //showImg.setImageBitmap(bitmap);
                            headImg.setImageBitmap(bitmap);
//                            RequestOptions mRequestOptions = RequestOptions.circleCropTransform().diskCacheStrategy(DiskCacheStrategy.ALL)
//                                    .skipMemoryCache(true);
//                            Glide.with(PersonDataActivity.this).
//                                    load(mCutUri).apply(mRequestOptions).into(headImg);
                            // uploadPic(ImageTool.getRealFilePath(PersonDataActivity.this, mCutUri));
                            uploadPic(Base64BitmapUtil.bitmapToBase64(bitmap), 0);
                        }

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }

    }

    private void submitPersonData() {
        isSubmit = true;
        Gson gson = new Gson();
        HashMap<String, Object> map = new HashMap<>();
//        map.put("userId", B18iCommon.customer_id);
        map.put("userId", Common.customer_id);
        map.put("sex", sexVal);
        map.put("nickName", nickName);
        map.put("height", height);
        map.put("weight", weight);
        map.put("birthday", brithdayVal);
        String mapjson = gson.toJson(map);
//        Log.e("PersonDataActivity", "----222------" + mapjson);
        dialogSubscriber = new DialogSubscriber(subscriberOnNextListener, PersonDataActivity.this);
        OkHttpObservable.getInstance().getData(dialogSubscriber, URLs.HTTPs + URLs.yonghuziliao, mapjson);
    }


    /**
     * 打开相机
     */
    private void cameraPic() {
        //创建一个file，用来存储拍照后的照片
        File outputfile = new File(getExternalCacheDir().getPath(), "output.png");
        try {
            if (outputfile.exists()) {
                outputfile.delete();//删除
            }
            outputfile.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Uri imageuri;
        if (Build.VERSION.SDK_INT >= 24) {
            imageuri = FileProvider.getUriForFile(PersonDataActivity.this,
                    getPackageName()+".fileprovider_racefitpro", //可以是任意字符串
                    outputfile);
//            imageuri = FileProvider.getUriForFile(PersonDataActivity.this,
//                    "com.guider.ringmiihx.fileprovider", //可以是任意字符串
//                    outputfile);
//            imageuri = FileProvider.getUriForFile(PersonDataActivity.this,
//                    "com.example.bozhilun.android.fileprovider", //可以是任意字符串
//                    outputfile);
        } else {
            imageuri = Uri.fromFile(outputfile);
        }
        //启动相机程序
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageuri);
        startActivityForResult(intent, 1001);
    }


    /**
     * 拍照之后，启动裁剪
     *
     * @param camerapath 路径
     * @param imgname    img 的名字
     * @return
     */

    private Intent CutForCamera(String camerapath, String imgname) {
        try {
            //设置裁剪之后的图片路径文件
            File cutfile = new File(getExternalCacheDir().getPath(),
                    "cutcamera.png"); //随便命名一个
            if (cutfile.exists()) { //如果已经存在，则先删除,这里应该是上传到服务器，然后再删除本地的，没服务器，只能这样了
                cutfile.delete();
            }
            cutfile.createNewFile();
            //初始化 uri
            Uri imageUri = null; //返回来的 uri
            Uri outputUri = null; //真实的 uri
            Intent intent = new Intent("com.android.camera.action.CROP");
            //拍照留下的图片
            File camerafile = new File(camerapath, imgname);
            if (Build.VERSION.SDK_INT >= 24) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                imageUri = FileProvider.getUriForFile(PersonDataActivity.this,
                        getPackageName()+".fileprovider_racefitpro",
                        camerafile);
//                imageUri = FileProvider.getUriForFile(PersonDataActivity.this,
//                        "com.guider.ringmiihx.fileprovider",
//                        camerafile);
//                imageUri = FileProvider.getUriForFile(PersonDataActivity.this,
//                        "com.example.bozhilun.android.fileprovider",
//                        camerafile);
            } else {
                imageUri = Uri.fromFile(camerafile);
            }
            outputUri = Uri.fromFile(cutfile);
            //把这个 uri 提供出去，就可以解析成 bitmap了
            mCutUri = outputUri;
            // crop为true是设置在开启的intent中设置显示的view可以剪裁
            intent.putExtra("crop", true);
            // aspectX,aspectY 是宽高的比例，这里设置正方形
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            //设置要裁剪的宽高
            intent.putExtra("outputX", 150);
            intent.putExtra("outputY", 150);
            intent.putExtra("scale", true);
            //如果图片过大，会导致oom，这里设置为false
            intent.putExtra("return-data", false);
            if (imageUri != null) {
                intent.setDataAndType(imageUri, "image/*");
            }
            if (outputUri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
            }
            intent.putExtra("noFaceDetection", true);
            //压缩图片
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
            return intent;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 图片裁剪
     *
     * @param uri
     * @return
     */
    private Intent CutForPhoto(Uri uri) {
        try {
            //直接裁剪
            Intent intent = new Intent("com.android.camera.action.CROP");
            //设置裁剪之后的图片路径文件
            File cutfile = new File(getExternalCacheDir().getPath(),
                    "cutcamera.png"); //随便命名一个
            if (cutfile.exists()) { //如果已经存在，则先删除,这里应该是上传到服务器，然后再删除本地的，没服务器，只能这样了
                cutfile.delete();
            }
            cutfile.createNewFile();
            //初始化 uri
            Uri imageUri = uri; //返回来的 uri
            Uri outputUri = null; //真实的 uri
//            Log.d(TAG, "CutForPhoto: " + cutfile);
            outputUri = Uri.fromFile(cutfile);
            mCutUri = outputUri;
//            Log.d(TAG, "mCameraUri: " + mCutUri);
            // crop为true是设置在开启的intent中设置显示的view可以剪裁
            intent.putExtra("crop", true);
            // aspectX,aspectY 是宽高的比例，这里设置正方形
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            //设置要裁剪的宽高
            intent.putExtra("outputX", 150); //200dp
            intent.putExtra("outputY", 150);
            intent.putExtra("scale", true);
            //如果图片过大，会导致oom，这里设置为false
            intent.putExtra("return-data", false);
            if (imageUri != null) {
                intent.setDataAndType(imageUri, "image/*");
            }
            if (outputUri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
            }
            intent.putExtra("noFaceDetection", true);
            //压缩图片
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
            return intent;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressLint("NewApi")
    private void handlerImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this, uri)) {
            //如果是document类型的Uri,则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];//解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            //如果是content类型的URI，则使用普通方式处理
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            //如果是file类型的Uri,直接获取图片路径即可
            imagePath = uri.getPath();
        }
//        Log.e(TAG, "---imagePath=" + imagePath);

        if (imagePath != null) {
            //CutForPhoto(Uri.fromFile(new File(imagePath)));
            startActivityForResult(CutForPhoto(Uri.fromFile(new File(imagePath))), 111);
        }

    }


    private String getImagePath(Uri uri, String selection) {
        String path = null;
        //通过Uri和selection来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }
}

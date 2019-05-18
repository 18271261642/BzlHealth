package com.bozlun.health.android.bzlmaps;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bozlun.health.android.Commont;
import com.bozlun.health.android.MyApp;
import com.bozlun.health.android.R;
import com.bozlun.health.android.bzlmaps.sos.GPSGaoDeUtils;
import com.bozlun.health.android.bzlmaps.sos.GPSGoogleUtils;
import com.bozlun.health.android.siswatch.WatchBaseActivity;
import com.bozlun.health.android.siswatch.utils.WatchUtils;
import com.bozlun.health.android.util.ToastUtil;
import com.bozlun.health.android.util.VerifyUtil;
import com.suchengkeji.android.w30sblelibrary.utils.SharedPreferencesUtils;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RequestExecutor;
import com.yanzhenjie.permission.Setting;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HellpEditActivity
        extends WatchBaseActivity
        implements Rationale<List<String>>, View.OnLongClickListener {

    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.text_content)
    TextView textContent;
    @BindView(R.id.text_user_phone_one)
    TextView textOne;
    @BindView(R.id.text_user_phone_two)
    TextView textTwo;
    @BindView(R.id.text_user_phone_three)
    TextView textThee;

    @BindView(R.id.text_user_name_one)
    TextView textNameOne;
    @BindView(R.id.text_user_name_two)
    TextView textNameTwo;
    @BindView(R.id.text_user_name_three)
    TextView textNameThee;
    @BindView(R.id.car_person_one)
    CardView carPersonOne;
    @BindView(R.id.car_person_two)
    CardView carPersonTwo;
    @BindView(R.id.car_person_three)
    CardView carPersonThree;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hellp_edit_activity);
        ButterKnife.bind(this);

        init();
    }

    private void init() {
        tvTitle.setText("SOS");
        toolbar.setTitle(" ");
        toolbar.setNavigationIcon(getResources().getDrawable(R.mipmap.backs));//设置返回按钮
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });//右边返回按钮点击事件
        statuteChange();

        carPersonOne.setOnLongClickListener(this);
        carPersonTwo.setOnLongClickListener(this);
        carPersonThree.setOnLongClickListener(this);
        textContent.setOnLongClickListener(this);
    }


    void statuteChange() {
        String stringpersonContent = (String) SharedPreferencesUtils.getParam(this, "personContent", "");
        String stringpersonOne = (String) SharedPreferencesUtils.getParam(this, "personOne", "");
        String stringpersonTwo = (String) SharedPreferencesUtils.getParam(this, "personTwo", "");
        String stringpersonThree = (String) SharedPreferencesUtils.getParam(this, "personThree", "");


        String nameOne = (String) SharedPreferencesUtils.getParam(HellpEditActivity.this, "NameOne", "");
        String nameTwo = (String) SharedPreferencesUtils.getParam(HellpEditActivity.this, "NameTwo", "");
        String nameThree = (String) SharedPreferencesUtils.getParam(HellpEditActivity.this, "NameThree", "");
        if (!WatchUtils.isEmpty(stringpersonContent)) {
            textContent.setText(stringpersonContent);
        }

        if (!WatchUtils.isEmpty(stringpersonOne)) {
            if (WatchUtils.isEmpty(nameOne)) nameOne = "RingmiiHX";
            textOne.setText(stringpersonOne);
            textNameOne.setText(nameOne);
        }

        if (!WatchUtils.isEmpty(stringpersonTwo)) {
            if (WatchUtils.isEmpty(nameTwo)) nameTwo = "RingmiiHX";
            textTwo.setText(stringpersonTwo);
            textNameTwo.setText(nameTwo);
        }

        if (!WatchUtils.isEmpty(stringpersonThree)) {
            if (WatchUtils.isEmpty(nameThree)) nameThree = "RingmiiHX";
            textThee.setText(stringpersonThree);
            textNameThee.setText(nameThree);
        }
    }


    private void selectConnection(int type) {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, type);
    }


    private Intent mIntent;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                mIntent = data;
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
//                    //申请授权，第一个参数为要申请用户授权的权限；第二个参数为requestCode 必须大于等于0，主要用于回调的时候检测，匹配特定的onRequestPermissionsResult。
//                    //可以从方法名requestPermissions以及第二个参数看出，是支持一次性申请多个权限的，系统会通过对话框逐一询问用户是否授权。
//                    requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
//
//                }else{
//                    //如果该版本低于6.0，或者该权限已被授予，它则可以继续读取联系人。
//                    getContacts(data,1);
//                }
                AndPermission.with(HellpEditActivity.this)
                        .runtime()
                        .permission(
                                Manifest.permission.CALL_PHONE,
                                Manifest.permission.READ_PHONE_STATE,
                                Manifest.permission.READ_CONTACTS,
                                Manifest.permission.READ_CALL_LOG,
                                Manifest.permission.SEND_SMS,
                                Manifest.permission.USE_SIP
//                                ,
//                                Manifest.permission.PROCESS_OUTGOING_CALLS
                        )
                        .rationale(this)
                        .rationale(this)//添加拒绝权限回调
                        .onGranted(new Action<List<String>>() {
                            @Override
                            public void onAction(List<String> data) {
                                getContacts(mIntent, 1);
                            }
                        })
                        .onDenied(new Action<List<String>>() {
                            @Override
                            public void onAction(List<String> data) {
                                /**
                                 * 当用户没有允许该权限时，回调该方法
                                 */
                                Toast.makeText(MyApp.getContext(), getString(R.string.string_no_permission), Toast.LENGTH_SHORT).show();
                                /**
                                 * 判断用户是否点击了禁止后不再询问，AndPermission.hasAlwaysDeniedPermission(MainActivity.this, data)
                                 */
//                                if (AndPermission.hasAlwaysDeniedPermission(MyApp.getContext(), data)) {
//                                    //true，弹窗再次向用户索取权限
//                                    showSettingDialog(HellpEditActivity.this, data);
//                                }
                            }
                        }).start();
                break;
            case 2:
                mIntent = data;
                AndPermission.with(HellpEditActivity.this)
                        .runtime()
                        .permission(
                                Manifest.permission.CALL_PHONE,
                                Manifest.permission.READ_PHONE_STATE,
                                Manifest.permission.READ_CONTACTS,
                                Manifest.permission.READ_CALL_LOG,
                                Manifest.permission.SEND_SMS,
//                                Manifest.permission.WRITE_CALL_LOG,
                                Manifest.permission.USE_SIP
//                                ,
//                                Manifest.permission.PROCESS_OUTGOING_CALLS
                        )
                        .rationale(this)
                        .rationale(this)//添加拒绝权限回调
                        .onGranted(new Action<List<String>>() {
                            @Override
                            public void onAction(List<String> data) {
                                getContacts(mIntent, 2);
                            }
                        })
                        .onDenied(new Action<List<String>>() {
                            @Override
                            public void onAction(List<String> data) {
                                /**
                                 * 当用户没有允许该权限时，回调该方法
                                 */
                                Toast.makeText(MyApp.getContext(), getString(R.string.string_no_permission), Toast.LENGTH_SHORT).show();
                                /**
                                 * 判断用户是否点击了禁止后不再询问，AndPermission.hasAlwaysDeniedPermission(MainActivity.this, data)
                                 */
                                if (AndPermission.hasAlwaysDeniedPermission(MyApp.getContext(), Manifest.permission.CALL_PHONE,
                                        Manifest.permission.READ_PHONE_STATE,
                                        Manifest.permission.READ_CONTACTS,
                                        Manifest.permission.READ_CALL_LOG,
//                                Manifest.permission.WRITE_CALL_LOG,
                                        Manifest.permission.USE_SIP)) {
                                    //true，弹窗再次向用户索取权限
                                    showSettingDialog(HellpEditActivity.this, data);
                                }
                            }
                        }).start();
                break;
            case 3:
                mIntent = data;
                AndPermission.with(HellpEditActivity.this)
                        .runtime()
                        .permission(
                                Manifest.permission.CALL_PHONE,
                                Manifest.permission.READ_PHONE_STATE,
                                Manifest.permission.READ_CONTACTS,
                                Manifest.permission.READ_CALL_LOG,
//                                Manifest.permission.WRITE_CALL_LOG,
                                Manifest.permission.USE_SIP
//                                ,
//                                Manifest.permission.PROCESS_OUTGOING_CALLS
                        )
                        .rationale(this)
                        .rationale(this)//添加拒绝权限回调
                        .onGranted(new Action<List<String>>() {
                            @Override
                            public void onAction(List<String> data) {
                                getContacts(mIntent, 3);
                            }
                        })
                        .onDenied(new Action<List<String>>() {
                            @Override
                            public void onAction(List<String> data) {
                                /**
                                 * 当用户没有允许该权限时，回调该方法
                                 */
                                Toast.makeText(MyApp.getContext(), getString(R.string.string_no_permission), Toast.LENGTH_SHORT).show();
                                /**
                                 * 判断用户是否点击了禁止后不再询问，AndPermission.hasAlwaysDeniedPermission(MainActivity.this, data)
                                 */
//                                if (AndPermission.hasAlwaysDeniedPermission(MyApp.getContext(), data)) {
//                                    //true，弹窗再次向用户索取权限
//                                    showSettingDialog(HellpEditActivity.this, data);
//                                }
                            }
                        }).start();
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }


    private void getContacts(Intent data, int type) {
        if (data == null) {
            return;
        }

        Uri contactData = data.getData();
        if (contactData == null) {
            return;
        }
        String name = "";
        String phoneNumber = "";

        Uri contactUri = data.getData();
        Cursor cursor = getContentResolver().query(contactUri, null, null, null, null);
        if (cursor.moveToFirst()) {
            name = cursor
                    .getString(cursor
                            .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            String hasPhone = cursor
                    .getString(cursor
                            .getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
            String id = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.Contacts._ID));
            if (hasPhone.equalsIgnoreCase("1")) {
                hasPhone = "true";
            } else {
                hasPhone = "false";
            }
            if (Boolean.parseBoolean(hasPhone)) {
                Cursor phones = getContentResolver()
                        .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                                        + " = " + id, null, null);
                while (phones.moveToNext()) {
                    phoneNumber = phones
                            .getString(phones
                                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                }
                phones.close();
            }
            cursor.close();
            if (name == null) name = "";
            phoneNumber = phoneNumber.trim().replace(" ", "");
            switch (type) {
                case 1:
                    if (!WatchUtils.isEmpty(phoneNumber.trim())) {
                        if (WatchUtils.isEmpty(name)) name = "User";
                        SharedPreferencesUtils.setParam(HellpEditActivity.this, "personOne", phoneNumber.trim());
                        SharedPreferencesUtils.setParam(HellpEditActivity.this, "NameOne", name);
                        textOne.setText(phoneNumber.trim());
                        textNameOne.setText(name);
                    }
                    break;
                case 2:
                    if (!WatchUtils.isEmpty(phoneNumber.trim())) {
                        if (WatchUtils.isEmpty(name)) name = "User";
                        SharedPreferencesUtils.setParam(HellpEditActivity.this, "personTwo", phoneNumber.trim());
                        SharedPreferencesUtils.setParam(HellpEditActivity.this, "NameTwo", name);
                        textTwo.setText(phoneNumber.trim());
                        textNameTwo.setText(name);
                    }
                    break;
                case 3:
                    if (!WatchUtils.isEmpty(phoneNumber.trim())) {
                        if (WatchUtils.isEmpty(name)) name = "User";
                        SharedPreferencesUtils.setParam(HellpEditActivity.this, "personThree", phoneNumber.trim());
                        SharedPreferencesUtils.setParam(HellpEditActivity.this, "NameThree", name);

                        textThee.setText(phoneNumber.trim());
                        textNameThee.setText(name);
                    }
                    break;
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

//        AndPermission.with(this)
//                .runtime()
////                .permission(Permission.Group.SMS, Permission.Group.LOCATION)
//                .permission(
//                        Permission.ACCESS_FINE_LOCATION,
//                        Permission.ACCESS_COARSE_LOCATION,
//                        //--------------
//                        Permission.SEND_SMS,
//                        Permission.RECEIVE_SMS,
//                        Permission.READ_SMS,
//                        Permission.RECEIVE_WAP_PUSH,
//                        Permission.RECEIVE_MMS,
//                        //--------------
//                        Manifest.permission.CALL_PHONE,
//                        Manifest.permission.READ_PHONE_STATE,
//                        Manifest.permission.READ_CONTACTS,
//                        Manifest.permission.READ_CALL_LOG,
//                        Manifest.permission.WRITE_CALL_LOG,
//                        Manifest.permission.USE_SIP,
//                        Manifest.permission.PROCESS_OUTGOING_CALLS)
//                .rationale(this)//添加拒绝权限回调
//                .onGranted(new Action<List<String>>() {
//                    @Override
//                    public void onAction(List<String> data) {
//
//                    }
//                })
//                .onDenied(new Action<List<String>>() {
//                    @Override
//                    public void onAction(List<String> data) {
//                        /**
//                         * 当用户没有允许该权限时，回调该方法
//                         */
//                        Toast.makeText(MyApp.getContext(), getString(R.string.string_no_permission), Toast.LENGTH_SHORT).show();
////                        /**
////                         * 判断用户是否点击了禁止后不再询问，AndPermission.hasAlwaysDeniedPermission(MainActivity.this, data)
////                         */
////                        if (AndPermission.hasAlwaysDeniedPermission(MyApp.getContext(), data)) {
////                            //true，弹窗再次向用户索取权限
////                            showSettingDialog(HellpEditActivity.this, data);
////                        }
//                    }
//                }).start();
    }

    /**
     * Display setting dialog.
     */
    public void showSettingDialog(Context context, final List<String> permissions) {
        List<String> permissionNames = Permission.transformText(context, permissions);
        String message = getResources().getString(R.string.string_get_permission) + "\n" + permissionNames;
//                context.getString("Please give us permission in the settings:\\n\\n%1$s", TextUtils.join("\n", permissionNames));
        new AlertDialog.Builder(HellpEditActivity.this)
                .setCancelable(false)
                .setTitle(getResources().getString(R.string.prompt))
                .setMessage(message)
                .setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setPermission();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancle), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
    }

    /**
     * Set permissions.
     */
    private void setPermission() {
        AndPermission.with(this)
                .runtime()
                .setting()
                .onComeback(new Setting.Action() {
                    @Override
                    public void onAction() {
                        //Toast.makeText(MyApp.getContext(),"用户从设置页面返回。", Toast.LENGTH_SHORT).show();
                    }
                })
                .start();
    }


    @Override
    public void showRationale(Context context, List<String> data, final RequestExecutor executor) {
        List<String> permissionNames = Permission.transformText(context, data);
        String message = getResources().getString(R.string.string_get_permission) + "\n" + permissionNames;

        new AlertDialog.Builder(context)
                .setCancelable(false)
                .setTitle(getResources().getString(R.string.prompt))
                .setMessage(message)
                .setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        executor.execute();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancle), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        executor.cancel();
                    }
                })
                .show();
    }


    @OnClick({R.id.text_content,
            R.id.car_person_one, R.id.car_person_two, R.id.car_person_three,
            R.id.btn_helps,
            R.id.image_one, R.id.image_two, R.id.image_three})
    public void onViewClicked(final View view) {

        AndPermission.with(this)
                .runtime()
                .permission(
                        Permission.ACCESS_FINE_LOCATION,
                        Permission.ACCESS_COARSE_LOCATION,
                        //--------------
                        Permission.SEND_SMS,
//                        Permission.RECEIVE_SMS,
//                        Permission.READ_SMS,
//                        Permission.RECEIVE_WAP_PUSH,
//                        Permission.RECEIVE_MMS,
                        //--------------
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.READ_CALL_LOG,
//                        Manifest.permission.WRITE_CALL_LOG,
                        Manifest.permission.USE_SIP
//                        ,
//                        Manifest.permission.PROCESS_OUTGOING_CALLS
                )
                .rationale(this)//添加拒绝权限回调
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        boolean isSos = (boolean) SharedPreferencesUtils.getParam(MyApp.getContext(), Commont.ISHelpe, false);//sos
                        switch (view.getId()) {
                            case R.id.text_content:

                                break;
                            case R.id.car_person_one:
                                if (!TextUtils.isEmpty(textOne.getText().toString().trim()) && isSos) {
                                    Commont.COUNTNUMBER = 4;
                                    Commont.GPSCOUNT = 2;
                                    //Commont.isGPSed = true;
                                    getGps();
                                    call(textOne.getText().toString().trim());
                                }
                                break;
                            case R.id.car_person_two:
                                if (!TextUtils.isEmpty(textTwo.getText().toString().trim()) && isSos) {
                                    Commont.COUNTNUMBER = 4;
                                    Commont.GPSCOUNT = 2;
                                    //Commont.isGPSed = true;
                                    getGps();
                                    call(textTwo.getText().toString().trim());
                                }
                                break;
                            case R.id.car_person_three:
                                if (!TextUtils.isEmpty(textThee.getText().toString().trim()) && isSos) {
                                    Commont.COUNTNUMBER = 4;
                                    Commont.GPSCOUNT = 2;
                                    //Commont.isGPSed = true;
                                    getGps();
                                    call(textThee.getText().toString().trim());
                                }
                                break;
                            case R.id.btn_helps:
                                //按了一次
                                if ((!TextUtils.isEmpty(textOne.getText().toString().trim())
                                        || !TextUtils.isEmpty(textTwo.getText().toString().trim())
                                        || !TextUtils.isEmpty(textThee.getText().toString().trim()))
                                        && isSos) {
                                    Commont.COUNTNUMBER = 0;
                                    Commont.GPSCOUNT = 0;
                                    //Commont.isGPSed = true;
                                    getGps();
                                    if (!TextUtils.isEmpty(textOne.getText().toString().trim())) {
                                        call(textOne.getText().toString().trim());
                                    } else {
                                        if (!TextUtils.isEmpty(textTwo.getText().toString().trim())) {
                                            call(textTwo.getText().toString().trim());
                                        } else {
                                            if (!TextUtils.isEmpty(textThee.getText().toString().trim())) {
                                                call(textThee.getText().toString().trim());
                                            }
                                        }
                                    }
                                } else {
//                                    ToastUtil.showShort(HellpEditActivity.this, getResources().getString(R.string.string_sos_tip));
                                    ToastUtil.showShort(HellpEditActivity.this, "SOS未打开或者没有添加紧急联系人");
                                }
                                break;
                            case R.id.image_one:
                                selectConnection(1);
                                break;
                            case R.id.image_two:
                                selectConnection(2);
                                break;
                            case R.id.image_three:
                                selectConnection(3);
                                break;
                        }

                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        /**
                         * 当用户没有允许该权限时，回调该方法
                         */
                        Toast.makeText(MyApp.getContext(), getString(R.string.string_no_permission), Toast.LENGTH_SHORT).show();

                        if (AndPermission.hasAlwaysDeniedPermission(MyApp.getContext(), data)) {
                            //true，弹窗再次向用户索取权限
                            showSettingDialog(HellpEditActivity.this, data);
                        }
                    }
                }).start();

    }


    GPSGoogleUtils instance;

    void getGpsGoogle() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                boolean b = instance.startLocationUpdates(HellpEditActivity.this);
                if (!b) {
                    getGpsGoogle();
                }
            }
        }, 3000);
    }

    void getGps() {

//      GPSGaoDeUtils.getInstance(HellpEditActivity.this);
//        boolean zh = VerifyUtil.isZh(HellpEditActivity.this);
//        if (zh) {
//            GPSGaoDeUtils.getInstance(HellpEditActivity.this);
//        } else {
//            instance = GPSGoogleUtils.getInstance(HellpEditActivity.this);
//            getGpsGoogle();
//        }
        boolean zh = VerifyUtil.isZh(MyApp.getInstance());
        if (zh) {
            Boolean zhonTW = getResources().getConfiguration().locale.getCountry().equals("TW");
            Log.e("======", zh + "====" + zhonTW);
            if (zhonTW) {
                instance = GPSGoogleUtils.getInstance(MyApp.getInstance());
                getGpsGoogle();
            } else {
                GPSGaoDeUtils.getInstance(MyApp.getInstance());
            }
        } else {
            instance = GPSGoogleUtils.getInstance(MyApp.getInstance());
            getGpsGoogle();
        }
    }


    //点击事件调用的类

    protected void call(final String tel) {

        //直接拨打
//                        Log.d("GPS", "call:" + tel);
        Uri uri = Uri.parse("tel:" + tel);
        Intent intent = new Intent(Intent.ACTION_CALL, uri);
        if (ActivityCompat.checkSelfPermission(HellpEditActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startActivity(intent);
    }


    // 显示对话框
    public void showWaiterAuthorizationDialog(final int type) {
        showKeyboard(true);
        // LayoutInflater是用来找layout文件夹下的xml布局文件，并且实例化
        LayoutInflater factory = LayoutInflater.from(HellpEditActivity.this);
        // 把布局文件中的控件定义在View中
        final View textEntryView = factory.inflate(R.layout.chage_phone_item, null);
        // 获取用户输入的验证码
        // 注意：textEntryView.findViewById很重要，因为上面factory.inflate(R.layout.activity_mobile_authentication_check_code,
        // null)将页面布局赋值给了textEntryView了
        final EditText name = (EditText) textEntryView.findViewById(R.id.user_names);
        final EditText phone = (EditText) textEntryView.findViewById(R.id.user_phones);
        final EditText content = (EditText) textEntryView.findViewById(R.id.help_content);
        //String title = getResources().getString(R.string.string_edit_emergency_messge);
        String title = "请编辑紧急信息";
        if (type == 0) {
            //title = getResources().getString(R.string.string_edit_emergency_messge);
            title = "请编辑紧急信息";
            content.setVisibility(View.VISIBLE);

            name.setVisibility(View.GONE);
            phone.setVisibility(View.GONE);
        } else {
//            title = getResources().getString(R.string.string_edit_emergency_person);
            title = "请编辑紧急联系人";
            name.setVisibility(View.VISIBLE);
            phone.setVisibility(View.VISIBLE);

            content.setVisibility(View.GONE);
        }
//        // 将自定义xml文件中的控件显示在对话框中
//        new AlertDialog.Builder(HellpEditActivity.this)
//                // 对话框的标题
//                .setTitle(title)
//                // 设定显示的View
//                .setView(textEntryView)
//                // 对话框中的“完成”按钮的点击事件
//                .setPositiveButton("完成", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int whichButton) {
//
//                        switch (type) {
//                            case 0:
//                                textContent.setText(content.getText().toString().trim());
//                                break;
//                            case 1:
//                                textOne.setText(phone.getText().toString().trim());
//                                textNameOne.setText(name.getText().toString().trim());
//                                break;
//                            case 2:
//                                textTwo.setText(phone.getText().toString().trim());
//                                textNameTwo.setText(name.getText().toString().trim());
//                                break;
//                            case 3:
//                                textThee.setText(phone.getText().toString().trim());
//                                textNameThee.setText(name.getText().toString().trim());
//                                break;
//                        }
//
//                        hintKeyBoard();
//                        SharedPreferencesUtils.setParam(HellpEditActivity.this, "personContent", textContent.getText().toString().trim());
//                        SharedPreferencesUtils.setParam(HellpEditActivity.this, "personOne", textOne.getText().toString().trim());
//                        SharedPreferencesUtils.setParam(HellpEditActivity.this, "personTwo", textTwo.getText().toString().trim());
//                        SharedPreferencesUtils.setParam(HellpEditActivity.this, "personThree", textThee.getText().toString().trim());
//                    }
//                })
//                // 对话框的“退出”单击事件
//                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int whichButton) {
//                        //不做操作，关闭对话框
//                        hintKeyBoard();
//                    }
//                })
//                // 设置dialog是否为模态，false表示模态，true表示非模态
//                .setCancelable(false)
//                // 对话框的创建、显示
//                .create().show();


        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(textEntryView)
                //确定
                .setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String userPhone = phone.getText().toString().trim().replace(" ", "");
                        String userNames = name.getText().toString().trim();
                        switch (type) {
                            case 0:
                                textContent.setText(content.getText().toString().trim());
                                SharedPreferencesUtils.setParam(HellpEditActivity.this, "personContent", textContent.getText().toString().trim());

                                break;
                            case 1:
                                if ((!WatchUtils.isEmpty(userPhone) && !WatchUtils.isEmpty(userNames))
                                        || (!WatchUtils.isEmpty(userPhone) && WatchUtils.isEmpty(userNames))) {

                                    if (WatchUtils.isEmpty(userNames)) userNames = "User";
                                } else {
                                    userPhone = "";
                                    userNames = "";
                                }
                                SharedPreferencesUtils.setParam(HellpEditActivity.this, "personOne", userPhone);
                                SharedPreferencesUtils.setParam(HellpEditActivity.this, "NameOne", userNames);
                                textOne.setText(userPhone);
                                textNameOne.setText(userNames);
                                break;
                            case 2:
                                if ((!WatchUtils.isEmpty(userPhone) && !WatchUtils.isEmpty(userNames))
                                        || (!WatchUtils.isEmpty(userPhone) && WatchUtils.isEmpty(userNames))) {

                                    if (WatchUtils.isEmpty(userNames)) userNames = "User";

                                } else {
                                    userPhone = "";
                                    userNames = "";
                                }
                                SharedPreferencesUtils.setParam(HellpEditActivity.this, "personTwo", userPhone);
                                SharedPreferencesUtils.setParam(HellpEditActivity.this, "NameTwo", userNames);

                                textTwo.setText(userPhone);
                                textNameTwo.setText(userNames);
//
//                                textTwo.setText(phone.getText().toString().trim());
//                                textNameTwo.setText(name.getText().toString().trim());
//                                if ((!WatchUtils.isEmpty(phone.getText().toString().trim()) && !WatchUtils.isEmpty(name.getText().toString().trim()))
//                                        || (!WatchUtils.isEmpty(phone.getText().toString().trim()) && WatchUtils.isEmpty(name.getText().toString().trim()))) {
//                                    String names = name.getText().toString().trim();
//                                    if (WatchUtils.isEmpty(names)) names = "User";
//                                    SharedPreferencesUtils.setParam(HellpEditActivity.this, "personTwo", textTwo.getText().toString().trim());
//                                    SharedPreferencesUtils.setParam(HellpEditActivity.this, "NameTwo", names);
//                                } else {
//                                    SharedPreferencesUtils.setParam(HellpEditActivity.this, "personTwo", "");
//                                    SharedPreferencesUtils.setParam(HellpEditActivity.this, "NameTwo", "");
//                                }

                                break;
                            case 3:
                                if ((!WatchUtils.isEmpty(userPhone) && !WatchUtils.isEmpty(userNames))
                                        || (!WatchUtils.isEmpty(userPhone) && WatchUtils.isEmpty(userNames))) {

                                    if (WatchUtils.isEmpty(userNames)) userNames = "User";

                                } else {
                                    userPhone = "";
                                    userNames = "";
                                }
                                SharedPreferencesUtils.setParam(HellpEditActivity.this, "personThree", userPhone);
                                SharedPreferencesUtils.setParam(HellpEditActivity.this, "NameThree", userNames);
                                textThee.setText(userPhone);
                                textNameThee.setText(userNames);


//                                textThee.setText(phone.getText().toString().trim());
//                                textNameThee.setText(name.getText().toString().trim());
//                                if ((!WatchUtils.isEmpty(phone.getText().toString().trim()) && !WatchUtils.isEmpty(name.getText().toString().trim()))
//                                        || (!WatchUtils.isEmpty(phone.getText().toString().trim()) && WatchUtils.isEmpty(name.getText().toString().trim()))) {
//                                    String names = name.getText().toString().trim();
//                                    if (WatchUtils.isEmpty(names)) names = "User";
//                                    SharedPreferencesUtils.setParam(HellpEditActivity.this, "personThree", textThee.getText().toString().trim());
//                                    SharedPreferencesUtils.setParam(HellpEditActivity.this, "NameThree", names);
//                                } else {
//                                    SharedPreferencesUtils.setParam(HellpEditActivity.this, "personThree", "");
//                                    SharedPreferencesUtils.setParam(HellpEditActivity.this, "NameThree", "");
//                                }

                                break;
                        }

                        showKeyboard(false);
                    }
                })
                //取消
                .setNegativeButton(getResources().getString(R.string.cancle), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        showKeyboard(false);
                    }
                })
                .setCancelable(false)
                .create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(16);
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextSize(16);
    }


    public static boolean isMobileNO(String mobileNums) {
        /**
         * 判断字符串是否符合手机号码格式
         * 移动号段: 134,135,136,137,138,139,147,150,151,152,157,158,159,170,178,182,183,184,187,188
         * 联通号段: 130,131,132,145,155,156,170,171,175,176,185,186
         * 电信号段: 133,149,153,170,173,177,180,181,189
         * @param str
         * @return 待检测的字符串
         */
        String telRegex = "^((13[0-9])|(14[5,7,9])|(15[^4])|(18[0-9])|(17[0,1,3,5,6,7,8]))\\d{8}$";// "[1]"代表下一位为数字可以是几，"[0-9]"代表可以为0-9中的一个，"[5,7,9]"表示可以是5,7,9中的任意一位,[^4]表示除4以外的任何一个,\\d{9}"代表后面是可以是0～9的数字，有9位。
        if (TextUtils.isEmpty(mobileNums))
            return false;
        else
            return mobileNums.matches(telRegex);
    }


    @Override
    public boolean onLongClick(final View view) {

        AndPermission.with(this)
                .runtime()
//                .permission(Permission.Group.SMS, Permission.Group.LOCATION)
                .permission(
                        Permission.ACCESS_FINE_LOCATION,
                        Permission.ACCESS_COARSE_LOCATION,
                        //--------------
                        Permission.SEND_SMS,
//                        Permission.RECEIVE_SMS,
//                        Permission.READ_SMS,
//                        Permission.RECEIVE_WAP_PUSH,
//                        Permission.RECEIVE_MMS,
                        //--------------
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.READ_CALL_LOG,
//                        Manifest.permission.WRITE_CALL_LOG,
                        Manifest.permission.USE_SIP
//                        ,
//                        Manifest.permission.PROCESS_OUTGOING_CALLS
                )
                .rationale(this)//添加拒绝权限回调
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        switch (view.getId()) {
                            case R.id.text_content:
                                showWaiterAuthorizationDialog(0);
                                break;
                            case R.id.car_person_one:
                                showWaiterAuthorizationDialog(1);
                                break;
                            case R.id.car_person_two:
                                showWaiterAuthorizationDialog(2);
                                break;
                            case R.id.car_person_three:
                                showWaiterAuthorizationDialog(3);
                                break;
                        }
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        /**
                         * 当用户没有允许该权限时，回调该方法
                         */
                        Toast.makeText(MyApp.getContext(), getString(R.string.string_no_permission), Toast.LENGTH_SHORT).show();
                        /**
                         * 判断用户是否点击了禁止后不再询问，AndPermission.hasAlwaysDeniedPermission(MainActivity.this, data)
                         */
                        if (AndPermission.hasAlwaysDeniedPermission(MyApp.getContext(), data)) {
                            //true，弹窗再次向用户索取权限
                            showSettingDialog(HellpEditActivity.this, data);
                        }
                    }
                }).start();
        return false;
    }

    protected void showKeyboard(boolean isShow) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if (null == imm)
            return;

        if (isShow) {
            if (getCurrentFocus() != null) {
                //有焦点打开
                imm.showSoftInput(getCurrentFocus(), 0);
            } else {
                //无焦点打开
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
        } else {
            if (getCurrentFocus() != null) {
                //有焦点关闭
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            } else {
                //无焦点关闭
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
            }
        }
    }


    private void readPermission(){
        AndPermission.with(this)
                .runtime()
                .permission( Manifest.permission.CALL_PHONE,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.READ_CALL_LOG,
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.USE_SIP)
                .rationale(new Rationale<List<String>>() {
                    @Override
                    public void showRationale(Context context, List<String> data, RequestExecutor executor) {

                    }
                }).start();
    }

}

package com.example.qqsz;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
    private static WindowManager windowManager;
    private RelativeLayout relativeLayout, relativeLayoutButoon, relativeLayoutToast;
    private Button button, but1, but2, but3, submit, ck, qx;
    private Switch separation;
    private TextView textView, tv_ts;


    // 声明一个数组，用来存储所有需要动态申请的权限
    private String[] permissions = new String[]{
            Manifest.permission.SYSTEM_ALERT_WINDOW,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,};
    private List<String> mPermissionList = new ArrayList<>();
    private boolean mShowRequestPermission = true;
    private static final int REQUEST_OVERLAY = 4444;
    private boolean flag = true;

    private boolean isSeparation; //记录是否是分身模式
    private SharedPreferences sp;
    private SharedPreferences.Editor edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_main);
        sp = getSharedPreferences("data", MODE_PRIVATE);
        edit = sp.edit();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        check();
        requestOverlayPermission();
        windowManager = (WindowManager) getApplication().getSystemService(WINDOW_SERVICE);

        submit = findViewById(R.id.submit);
        ck = findViewById(R.id.ck);
        tv_ts = findViewById(R.id.tv_ts);
        qx = findViewById(R.id.qx);
        //注意，悬浮窗只有一个，而当打开应用的时候才会产生悬浮窗，所以要判断悬浮窗是否已经存在，
        if (relativeLayoutButoon != null) {
            windowManager.removeView(relativeLayoutButoon);
        }
        relativeLayout = (RelativeLayout) LayoutInflater.from(getApplication()).inflate(R.layout.but3, null);
        relativeLayoutButoon = (RelativeLayout) LayoutInflater.from(getApplication()).inflate(R.layout.button, null);
        relativeLayoutToast = (RelativeLayout) LayoutInflater.from(getApplication()).inflate(R.layout.toast, null);
        button = relativeLayoutButoon.findViewById(R.id.btn_floatWindows);
        but1 = relativeLayout.findViewById(R.id.but1);
        but2 = relativeLayout.findViewById(R.id.but2);
        but3 = relativeLayout.findViewById(R.id.but3);
        separation = relativeLayout.findViewById(R.id.separation);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 使用Application context 创建UI控件，避免Activity销毁导致上下文出现问题,因为现在的悬浮窗是系统级别的，不依赖与Activity存在
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                } else {
                    lp.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
                            | WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
                }

                lp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;

                //显示位置与指定位置的相对位置差
                lp.x = 0;
                lp.y = 0;
                //悬浮窗的宽高
                lp.width = 300;
                lp.height = 300;
                lp.format = PixelFormat.TRANSPARENT;
                if (flag) {
                    windowManager.addView(relativeLayoutButoon, lp);
                    Toast.makeText(getApplication(), "长按返回可关闭悬浮窗", Toast.LENGTH_LONG).show();
                    flag = false;
                }
                try {
                    PackageManager packageManager = getPackageManager();
                    Intent intent = new Intent();
                    intent = packageManager.getLaunchIntentForPackage("com.tencent.mobileqq");
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        ck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SZ.class));
            }
        });

        qx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestOverlayPermission();
            }
        });
        tv_ts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String url = "mqqwpa://im/chat?chat_type=wpa&uin=275570078";//uin是发送过去的qq号码
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                } catch (Exception e) {
                }
            }
        });
        FileUtils.getAPPLocalVersion(MainActivity.this);

        button.setOnTouchListener(new View.OnTouchListener() {
            private float lastX; //上一次位置的X.Y坐标
            private float lastY;
            private float nowX;  //当前移动位置的X.Y坐标
            private float nowY;
            private float tranX; //悬浮窗移动位置的相对值
            private float tranY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                boolean ret = false;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // 获取按下时的X，Y坐标
                        lastX = event.getRawX();
                        lastY = event.getRawY();
                        ret = false;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        ret = true;
                        // 获取移动时的X，Y坐标
                        nowX = event.getRawX();
                        nowY = event.getRawY();
                        // 计算XY坐标偏移量
                        tranX = nowX - lastX;
                        tranY = nowY - lastY;
                        // 移动悬浮窗
                        lp.x += tranX;
                        lp.y += tranY;
                        //更新悬浮窗位置
                        windowManager.updateViewLayout(relativeLayoutButoon, lp);
                        //记录当前坐标作为下一次计算的上一次移动的位置坐标
                        lastX = nowX;
                        lastY = nowY;
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                }
                return ret;
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                windowManager.removeView(relativeLayoutButoon);
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                lp.gravity = Gravity.CENTER;
                windowManager.addView(relativeLayout, lp);
            }
        });

        but1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean b = FileUtils.setBasePath(getApplicationContext(), isSeparation);
                if (b) {
                    boolean is = FileUtils.getAll(MainActivity.this, FileUtils.mFilePath);
                    if (is) {
                        ToastShow("破解完成");
                    } else {
                        ToastShow("破解失败");
                    }
                } else {
                    ToastShow("暂时还不兼容次机型分身破解，联系作者添加");
                }
            }
        });

        but1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), SZ.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(intent);
                return true;
            }
        });

        but2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean b = FileUtils.setBasePath(getApplicationContext(), isSeparation);
                if (b) {
                    boolean is = FileUtils.deleteAllFiles(new File(FileUtils.mFilePath));
                    if (is) {
                        ToastShow("初始化完成");
                    }
                } else {
                    ToastShow("暂时还不兼容次机型分身破解，联系作者添加");
                }
            }
        });

        but3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lp.width = 200;
                lp.height = 200;
                windowManager.removeView(relativeLayout);
                windowManager.addView(relativeLayoutButoon, lp);
            }
        });

//        but3.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                windowManager.removeView(relativeLayout);
//                flag = true;
//                return false;
//            }
//        });

        isSeparation = sp.getBoolean("isSeparation", false);
        System.out.println("======" + isSeparation);
        separation.setOnCheckedChangeListener(null);
        separation.setChecked(isSeparation);
        separation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ToastShow("你选择的破解分身");
                } else {
                    ToastShow("你选择的破解普通");
                }
                isSeparation = isChecked;
                edit.putBoolean("isSeparation", isChecked);
                edit.commit();
            }
        });
    }

    private void check() {
        mPermissionList.clear();
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions[i]);
            }
        }
        if (mPermissionList.isEmpty()) {//未授予的权限为空，表示都授予了
            return;
        } else {//请求权限方法
            Toast.makeText(this, "请开通相关权限，否则无法正常使用本应用！", Toast.LENGTH_SHORT);
            String[] permissions = mPermissionList.toArray(new String[mPermissionList.size()]);//将List转为数组
            ActivityCompat.requestPermissions(this, permissions, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        //判断是否勾选禁止后不再询问
                        boolean showRequestPermission = ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permissions[i]);
                        if (showRequestPermission) {//
                            System.exit(0);
                            return;
                        } else {
                            mShowRequestPermission = false;//已经禁止
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    private void requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(MainActivity.this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_OVERLAY);
            } else {

            }
        }
    }

    private void ToastShow(String text) {
        windowManager.removeView(relativeLayout);
        textView = relativeLayoutToast.findViewById(R.id.toast);
        textView.setText(text);
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        windowManager.addView(relativeLayoutToast, lp);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                windowManager.removeView(relativeLayoutToast);
                mHandler.sendEmptyMessage(1);
            }
        }).start();
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            lp.width = 200;
            lp.height = 200;
            windowManager.addView(relativeLayoutButoon, lp);
        }
    };

    public void execCommand(String command) throws IOException {
        // start the ls command running
        //String[] args =  new String[]{"sh", "-c", command};
        Runtime runtime = Runtime.getRuntime();
        Process proc = runtime.exec(command);      //这句话就是shell与高级语言间的调用
        //如果有参数的话可以用另外一个被重载的exec方法
        //实际上这样执行时启动了一个子进程,它没有父进程的控制台
        //也就看不到输出,所以我们需要用输出流来得到shell执行后的输出
        InputStream inputstream = proc.getInputStream();
        InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
        BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
        // read the ls output
        String line = "";
        StringBuilder sb = new StringBuilder(line);
        while ((line = bufferedreader.readLine()) != null) {
            //System.out.println(line);
            sb.append(line);
        }
        System.out.println("=====" + sb.toString());
        //使用exec执行不会等执行成功以后才返回,它会立即返回
        //所以在某些情况下是很要命的(比如复制文件的时候)
        //使用wairFor()可以等待命令执行完成以后才返回
        try {
            if (proc.waitFor() != 0) {
                System.err.println("exit value = " + proc.exitValue());
            }
        } catch (InterruptedException e) {
            System.err.println(e);
        }
    }
}

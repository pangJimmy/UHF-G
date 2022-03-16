package com.pda.uhf_g;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.Menu;
import android.widget.Toast;

import com.gg.reader.api.protocol.gx.MsgAppGetReaderInfo;
import com.gg.reader.api.utils.ThreadPoolUtils;
import com.google.android.material.navigation.NavigationView;
import com.handheld.uhfr.UHFRManager;
import com.pda.uhf_g.entity.TagInfo;
import com.pda.uhf_g.util.CheckCommunication;
import com.pda.uhf_g.util.GlobalClient;
import com.pda.uhf_g.util.LogUtil;
import com.pda.uhf_g.util.ScanUtil;
import com.uhf.api.cls.Reader;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import cn.pda.serialport.SerialPort;

public class MainActivity extends AppCompatActivity implements NavigationView. OnNavigationItemSelectedListener{

    private AppBarConfiguration mAppBarConfiguration;

    public boolean isConnectUHF  = false;

    //扫描
    private ScanUtil scanUtil;
    public UHFRManager mUhfrManager;//uhf
    private SharedPreferences mSharedPreferences;

    public List<String> listEPC = new ArrayList<>();//epc数据
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSharedPreferences = this.getSharedPreferences("UHF", MODE_PRIVATE);
        //初始化模块
        initModule();
        setScanKeyDisable() ;

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                navController.getGraph())
                .setDrawerLayout(drawer)
                .build();
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener()
        {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments)
            {

            }
        });
    }

    /**
     * 设置扫描头按键不可用
     */
    private void setScanKeyDisable() {
        int currentApiVersion = Build.VERSION.SDK_INT;
        if (currentApiVersion > Build.VERSION_CODES.N) {
            // For Android10.0 module
            scanUtil = ScanUtil.getInstance(this);
            scanUtil.disableScanKey("134");
            scanUtil.disableScanKey("137");
        }

    }

    /**
     * 设置扫描头按键可用
     */
    private void setScanKeyEnable() {
        int currentApiVersion = Build.VERSION.SDK_INT;
        if (currentApiVersion > Build.VERSION_CODES.N) {
            // For Android10.0 module
            scanUtil = ScanUtil.getInstance(this);
            scanUtil.enableScanKey("134");
            scanUtil.enableScanKey("137");
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化uhf模块
     */
    private void initModule() {
        mUhfrManager = UHFRManager.getInstance();// Init Uhf module
        if(mUhfrManager!=null){
            //5106和6106 /6107和6108 支持33db
            Reader.READER_ERR err = mUhfrManager.setPower(mSharedPreferences.getInt("readPower",33), mSharedPreferences.getInt("writePower",33));//set uhf module power
            if(err== Reader.READER_ERR.MT_OK_ERR){
                isConnectUHF = true ;
                mUhfrManager.setRegion(Reader.Region_Conf.valueOf(mSharedPreferences.getInt("freRegion", 1)));
                Toast.makeText(getApplicationContext(),"FreRegion:"+Reader.Region_Conf.valueOf(mSharedPreferences.getInt("freRegion",1))+
                        "\n"+"Read Power:"+mSharedPreferences.getInt("readPower",33)+
                        "\n"+"Write Power:"+mSharedPreferences.getInt("writePower",33),Toast.LENGTH_LONG).show();
            }else {
                //5101 支持30db
                Reader.READER_ERR err1 = mUhfrManager.setPower(30, 30);//set uhf module power
                if(err1== Reader.READER_ERR.MT_OK_ERR) {
                    isConnectUHF = true ;
                    mUhfrManager.setRegion(Reader.Region_Conf.valueOf(mSharedPreferences.getInt("freRegion", 1)));
                    Toast.makeText(getApplicationContext(), "FreRegion:" + Reader.Region_Conf.valueOf(mSharedPreferences.getInt("freRegion", 1)) +
                            "\n" + "Read Power:" + 30 +
                            "\n" + "Write Power:" + 30, Toast.LENGTH_LONG).show();
                }else {
                    Toast.makeText(this,getString(R.string.module_init_fail), Toast.LENGTH_SHORT).show();
                }
            }
        }else {
            Toast.makeText(this,getString(R.string.module_init_fail), Toast.LENGTH_SHORT).show();
        }
    }



    /**
     * 关闭UHF模块
     */
    private void closeModule() {
        if (mUhfrManager != null) {//close uhf module
            mUhfrManager.close();
            mUhfrManager = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setScanKeyEnable();
        closeModule() ;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    long exitSytemTime = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //按两次返回键退出程序
        if(keyCode == KeyEvent.KEYCODE_BACK){
            if(System.currentTimeMillis() - exitSytemTime > 2000){
                Toast.makeText(getApplicationContext(), R.string.exit_app, Toast.LENGTH_SHORT).show();
                exitSytemTime = System.currentTimeMillis();
                return true;
            }else{
                finish();
            }

        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public boolean onSupportNavigateUp() {
        //Log.e("pang", "onSupportNavigateUp") ;
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Log.e("pang", "item = " + item.getItemId());
        return false;
    }
}
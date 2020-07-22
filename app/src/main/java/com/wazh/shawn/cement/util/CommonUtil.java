package com.wazh.shawn.cement.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CommonUtil {

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static float dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return dpValue * scale;
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static float px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return pxValue / scale;
    }

    public static int widthPixels(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    public static int heightPixels(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.heightPixels;
    }

    /**
     * 获取版本号
     * @return 当前应用的版本号
     */
    public static String getVersion(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            return info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 显示虚拟键盘
     * @param editText 输入框
     * @param context 上下文
     */
    public static void showInputSoft(EditText editText, Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        imm.showSoftInput(editText, 0);
    }

    /**
     * 隐藏虚拟键盘
     * @param editText 输入框
     * @param context 上下文
     */
    public static void hideInputSoft(EditText editText, Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    /**
     * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
     * @param context
     * @return true 表示开启
     */
    public static boolean isLocationOpen(final Context context) {
        LocationManager locationManager  = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps || network) {
            return true;
        }
        return false;
    }

    public static void bottomToUp(View view) {
        if (view.getVisibility() == View.GONE) {
            bottomToUpAnimation(true, view);
            view.setVisibility(View.VISIBLE);
        } else {
            bottomToUpAnimation(false, view);
            view.setVisibility(View.GONE);
        }
    }

    /**
     * 底部向上弹出动画
     * @param flag
     * @param view
     */
    private static void bottomToUpAnimation(boolean flag, final View view) {
        //列表动画
        AnimationSet animationSet = new AnimationSet(true);
        Animation animation;
        if (!flag) {
            animation = new TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 1f);
        } else {
            animation = new TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 1f,
                    Animation.RELATIVE_TO_SELF, 0f);
        }
        animation.setDuration(200);
        animationSet.addAnimation(animation);
        animationSet.setFillAfter(true);
        view.startAnimation(animationSet);
        animationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                view.clearAnimation();
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    /**
     * 字符串转md5
     * @param text
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static String toMD5(String text) {
        StringBuilder sb = null;
        try {
            //获取摘要器 MessageDigest
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            //通过摘要器对字符串的二进制字节数组进行hash计算
            byte[] digest = messageDigest.digest(text.getBytes());

            sb = new StringBuilder();
            for (int i = 0; i < digest.length; i++) {
                //循环每个字符 将计算结果转化为正整数;
                int digestInt = digest[i] & 0xff;
                //将10进制转化为较短的16进制
                String hexString = Integer.toHexString(digestInt);
                //转化结果如果是个位数会省略0,因此判断并补0
                if (hexString.length() < 2) {
                    sb.append(0);
                }
                //将循环结果添加到缓冲区
                sb.append(hexString);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        //返回整个结果
        return sb.toString();
    }

    /**
     * 获取状态栏高度
     * @param context
     * @return
     */
    public static int statusBarHeight(Context context) {
        int statusBarHeight = -1;//状态栏高度
        //获取status_bar_height资源的ID
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }

    /**
     * 获取底部导航栏高度
     * @param context
     * @return
     */
    public static int navigationBarHeight(Context context) {
        int navigationBarHeight = -1;//状态栏高度
        //获取status_bar_height资源的ID
        int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            navigationBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }
        return navigationBarHeight;
    }

    /**
     * 从Assets中读取图片
     */
    public static Bitmap getImageFromAssetsFile(Context context, String fileName) {
        Bitmap image = null;
        AssetManager am = context.getResources().getAssets();
        try {
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    /**
     * 读取assets下文件
     * @param fileName
     * @return
     */
    public static String getFromAssets(Context context, String fileName) {
        String Result = "";
        try {
            InputStreamReader inputReader = new InputStreamReader(context.getResources().getAssets().open(fileName));
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line = "";
            while ((line = bufReader.readLine()) != null)
                Result += line;
            return Result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result;
    }

    /**
     * 根据当前时间获取日期，格式为MM/dd
     * @param i (+1为后一天，-1为前一天，0表示当天)
     * @return
     */
    public static String getDate(int i) {
        String date = null;

        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DATE);
        c.set(Calendar.DATE, day+i);

        if (c.get(Calendar.MONTH) == 0) {
            date = "01";
        }else if (c.get(Calendar.MONTH) == 1) {
            date = "02";
        }else if (c.get(Calendar.MONTH) == 2) {
            date = "03";
        }else if (c.get(Calendar.MONTH) == 3) {
            date = "04";
        }else if (c.get(Calendar.MONTH) == 4) {
            date = "05";
        }else if (c.get(Calendar.MONTH) == 5) {
            date = "06";
        }else if (c.get(Calendar.MONTH) == 6) {
            date = "07";
        }else if (c.get(Calendar.MONTH) == 7) {
            date = "08";
        }else if (c.get(Calendar.MONTH) == 8) {
            date = "09";
        }else if (c.get(Calendar.MONTH) == 9) {
            date = "10";
        }else if (c.get(Calendar.MONTH) == 10) {
            date = "11";
        }else if (c.get(Calendar.MONTH) == 11) {
            date = "12";
        }

        return date+"/"+c.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 根据当前时间获取星期几
     * @param context
     * @param i (+1为后一天，-1为前一天，0表示当天)
     * @return
     */
    public static String getWeek(Context context, int i) {
        String week = null;

        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DATE);
        c.set(Calendar.DATE, day+i);

        if (c.get(Calendar.DAY_OF_WEEK) == 1) {
            week = "周日";
        }else if (c.get(Calendar.DAY_OF_WEEK) == 2) {
            week = "周一";
        }else if (c.get(Calendar.DAY_OF_WEEK) == 3) {
            week = "周二";
        }else if (c.get(Calendar.DAY_OF_WEEK) == 4) {
            week = "周三";
        }else if (c.get(Calendar.DAY_OF_WEEK) == 5) {
            week = "周四";
        }else if (c.get(Calendar.DAY_OF_WEEK) == 6) {
            week = "周五";
        }else if (c.get(Calendar.DAY_OF_WEEK) == 7) {
            week = "周六";
        }

        return week;
    }

    /**
     * 获取listview高度
     * @param listView
     */
    public static int getListViewHeightBasedOnChildren(ListView listView) {
        int height = 0;
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return height;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
        height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        return height;
    }

    /**
     * SDCard写入文件，注意读写权限
     * @param file
     * @param content
     */
    public static void writeExternalFile(File file, String content) {
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            bos.write(content.getBytes());
            bos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取文件内容
     * @param context
     * @param fileName
     * @return
     */
    public static String readFile(Context context, String fileName) {
        try {
            FileInputStream inputStream = context.openFileInput(fileName+".txt");
            byte[] bytes = new byte[1024];
            ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
            while (inputStream.read(bytes) != -1) {
                arrayOutputStream.write(bytes, 0, bytes.length);
            }
            inputStream.close();
            arrayOutputStream.close();
            String result = new String(arrayOutputStream.toByteArray());
            return result;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据当前时间获取日期
     * @param i (+1为后一天，-1为前一天，0表示当天)
     * @return
     */
    public static String getDate(String time, int i) {
        Calendar c = Calendar.getInstance();

        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmm", Locale.CHINA);
        try {
            Date date = sdf2.parse(time);
            c.setTime(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        c.add(Calendar.DAY_OF_MONTH, i);
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd", Locale.CHINA);
        String date = sdf1.format(c.getTime());
        return date;
    }

    /**
     * 根据当前时间获取星期几
     * @param i (+1为后一天，-1为前一天，0表示当天)
     * @return
     */
    public static String getWeek(int i) {
        String week = "";

        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_WEEK, i);

        switch (c.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.SUNDAY:
                week = "周日";
                break;
            case Calendar.MONDAY:
                week = "周一";
                break;
            case Calendar.TUESDAY:
                week = "周二";
                break;
            case Calendar.WEDNESDAY:
                week = "周三";
                break;
            case Calendar.THURSDAY:
                week = "周四";
                break;
            case Calendar.FRIDAY:
                week = "周五";
                break;
            case Calendar.SATURDAY:
                week = "周六";
                break;
        }

        return week;
    }

    /**
     * 格式化问价大小
     * @param size
     * @return
     */
    public static String getFormatSize(long size) {
        double kiloByte = size / 1024;
        if (kiloByte < 1) {
            return "0K";
        }

        double megaByte = kiloByte / 1024;
        if (megaByte < 1) {
            BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "KB";
        }

        double gigaByte = megaByte / 1024;
        if (gigaByte < 1) {
            BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "MB";
        }

        double teraBytes = gigaByte / 1024;
        if (teraBytes < 1) {
            BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "GB";
        }
        BigDecimal result4 = new BigDecimal(teraBytes);
        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()+ "TB";
    }

    /**
     * 跳转至wps-office
     * @param filePath
     */
    public static void intentWPSOffice(Context context, String filePath) {
        if (context == null || TextUtils.isEmpty(filePath)) {
            return;
        }
        Uri uri;
        File file = new File(filePath);
        if (file.exists()) {
            final String authority = context.getPackageName()+".fileprovider";
            boolean build = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
            uri = build ? FileProvider.getUriForFile(context, authority, file) : Uri.fromFile(file);
        }else {
            uri = Uri.parse(filePath);
        }
        Intent intent = context.getPackageManager().getLaunchIntentForPackage("cn.wps.moffice_eng");//WPS个人版的包名
        if (intent == null) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=cn.wps.moffice_eng")));
            Toast.makeText(context, "可下载WPS Office来打开文件", Toast.LENGTH_SHORT).show();
            return;
        }
        Bundle bundle = new Bundle();
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//添加这一句表示对目标应用临时授权该Uri所代表的文件
        intent.setData(uri);//这里采用传入文档的在线地址进行打开，免除下载的步骤，也不需要判断安卓版本号
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

}

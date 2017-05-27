package PrintPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import HPRTAndroidSDK.HPRTPrinterHelper;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * This class echoes a string called from JavaScript.
 */
public class PrintPlugin extends CordovaPlugin {
    private CallbackContext mCallback = null;

    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();;
    private BluetoothReceiver bluetoothReceiver;
    private Thread mThread;
    private Handler mHandler;
    private Message message;

    private boolean isPaired = false;
    private static HPRTPrinterHelper HPRTPrinter = new HPRTPrinterHelper();

    // private String unPairedList = "";// 未配对打印机集合
    private List<BluetoothDevice> unPairedList = new ArrayList<BluetoothDevice>();// 未配对打印机集合
    private int flag = 0;// 标识判断是从扫描蓝牙还是从配对设备进的回调方法

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        // TODO Auto-generated method stub
        super.initialize(cordova, webView);
        initFilter();
    }

    @SuppressLint("HandlerLeak")
    private void initFilter() {
        // 设定广播接收的filter
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        String ACTION_PAIRING_REQUEST = "android.bluetooth.device.action.PAIRING_REQUEST";
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);// 用BroadcastReceiver来取得搜索结果
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intentFilter.addAction(ACTION_PAIRING_REQUEST);
        intentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        // 创建蓝牙广播信息的receiver
        bluetoothReceiver = new BluetoothReceiver();
        // 注册广播接收器
        cordova.getActivity().registerReceiver(bluetoothReceiver, intentFilter);

        mHandler = new Handler() {
            @SuppressLint("HandlerLeak")
            public void handleMessage(Message msg) {
                if (msg.what == 0) {
                    // 配对成功e
                    isPaired = true;
                    //System.out.println("配对成功!");
                    mCallback.success("配对成功!");
                } else {
                    // 配对失败
                    //System.out.println("配对失败!");
                    mCallback.error("配对失败!");
                }

            };
        };

    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.mCallback = callbackContext;
        if (action.equals("openBluetooth")) {
            this.openBluetooth();
            return true;
        } else if (action.equals("getPairedEqu")) {
            this.getPairedData();
            return true;
        } else if (action.equals("searchEqu")) {
            flag = 2;
            this.doDiscovery();
            return true;
        } else if (action.equals("paireEqu")) {
            flag = 1;
            String arg0 = args.getString(0);
            //System.out.println("-------addr--------->" + arg0);
            this.paireEquipment(arg0);
            return true;
        } else if (action.equals("disconnectEqu")) {
            this.disconnectEqu();
            return true;
        } else if (action.equals("getStatus")) {
            this.getStatus();
            return true;
        } else if (action.equals("printMaterial")) {
            String arg0 = args.getString(0);
            String arg1 = args.getString(1);
            String arg2 = args.getString(2);
            String arg3 = args.getString(3);
            String arg4 = args.getString(4);
            String arg5 = args.getString(5);
            //System.out.println("-------billNo--------->" + arg0);
            //System.out.println("-------materialNo--------->" + arg1);
            //System.out.println("-------materialName--------->" + arg2);
            //System.out.println("-------unit--------->" + arg3);
            //System.out.println("-------sizeNo--------->" + arg4);
            //System.out.println("-------cellNo--------->" + arg5);
            this.printMaterial(arg0, arg1, arg2, arg3, arg4, arg5);
            return true;
        } else if (action.equals("printCell")) {
            String arg0 = args.getString(0);
            String arg1 = args.getString(1);
            //System.out.println("-------cellNo ---2------>" + arg0);
            this.printCell(arg0,arg1);
            return true;
        } else if (action.equals("closeBluetooth")) {
            this.closeBluetooth();
            return true;
        } else if(action.equals("printPic")){
             String arg0 = args.getString(0);
             System.out.println("--------printPic---------->"+arg0);
             this.printPic(arg0);
             return true;
        }
        return false;
    }

    /**
     * 打开蓝牙
     * 
     * @param callbackContext
     */
    private void openBluetooth() {
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.enable();
            }
            this.mCallback.success("打开蓝牙成功!");
            //System.out.println("打开蓝牙成功!");
        } else {
            this.mCallback.error("本地蓝牙不可用!");
            //System.out.println("本地蓝牙不可用!");
        }
    }

    /**
     * 获取已配对的设备
     * 
     * @param callbackContext
     * @throws JSONException
     */
    private void getPairedData() throws JSONException {
        // 得到当前的一个已经配对的蓝牙设备
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            JSONArray pairedArray = new JSONArray();
            for (BluetoothDevice device : pairedDevices) {
                JSONObject obj = new JSONObject();
                obj.put("name", device.getName());
                obj.put("address", device.getAddress());
                pairedArray.put(obj);
            }
            // pairedArray已配对设备集合
            this.mCallback.success(pairedArray);
            //System.out.println(pairedArray);
        } else {
            this.mCallback.error("无已配对设备!");
            //System.out.println("无已配对设备!");
        }
    }

    /**
     * 扫描周边可用蓝牙打印机
     * 
     * @param callbackContext
     */
    private void doDiscovery() {
        // 若启动了扫描，关闭扫描
        unPairedList.clear();
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        mBluetoothAdapter.startDiscovery();
    }

    // 扫描回调
    private class BluetoothReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = null;
            // 获得扫描到的远程蓝牙设备
            // 搜索设备时，取得设备的MAC地址
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // 判断获取的蓝牙设备是否已经配对，BluetoothDevice.BOND_NONE==10表示未配对
                if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                    if (device.getBluetoothClass().getMajorDeviceClass() == 1536) {
                        // 不同设备类型该值不同，比如computer蓝牙为256、phone 蓝牙为512、打印机蓝牙为1536
                        if (unPairedList.size()!=0) {
                            for (int i = 0; i < unPairedList.size(); i++) {
                                if (!unPairedList.get(i).getAddress().equals(device.getAddress())) {
                                    unPairedList.add(device);
                                }
                            }
                        } else {
                            //System.out.println("-------device.getAddress------>"+device.getAddress());
                            unPairedList.add(device);
                        }

                    }
                }
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                switch (device.getBondState()) {
                case BluetoothDevice.BOND_BONDING:
                    Log.d("BlueToothTestActivity", "正在配对......");
                    //System.out.println("BlueToothTestActivity,正在配对......");
                    break;
                case BluetoothDevice.BOND_BONDED:
                    Log.d("BlueToothTestActivity", "完成配对");
                    //System.out.println("BlueToothTestActivity,完成配对......");
                    break;
                case BluetoothDevice.BOND_NONE:
                    Log.d("BlueToothTestActivity", "取消配对");
                    //System.out.println("BlueToothTestActivity,取消配对......");
                default:
                    break;
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (flag == 1) {
                    //System.out.println("-配对--蓝牙扫描结束-----");
                } else if(flag == 2) {
                    //System.out.println("-扫描--蓝牙扫描结束-----");
                    if (unPairedList.size()!=0) {
                        JSONArray jsonArray = new JSONArray();
                        for (int i = 0; i < unPairedList.size(); i++) {
                            try {
                                JSONObject tmpObj = new JSONObject();
                                tmpObj.put("name", unPairedList.get(i).getName());
                                tmpObj.put("address", unPairedList.get(i).getAddress());
                                jsonArray.put(tmpObj);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        //System.out.println(jsonArray);
                        mCallback.success(jsonArray);
                    } else {
                        //System.out.println("未搜索到其他蓝牙打印机!");
                        mCallback.error("未搜索到其他蓝牙打印机!");
                    }

                }else{
                    //System.out.println("代码错误! ");
                    mCallback.error("代码错误!");
                }

            }

        }

    }

    /**
     * 配对某个设备
     */
    private void paireEquipment(final String toothAddress) {
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        if (!toothAddress.contains(":")) {
            return;
        }
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int portOpen = HPRTPrinterHelper.PortOpen("Bluetooth," + toothAddress);
                    message = new Message();
                    message.what = portOpen;
                    mHandler.sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        mThread.start();
    }

    /**
     * 断开连接
     */
    private void disconnectEqu() {
        boolean isFlag = false;
        if (HPRTPrinter != null) {
            isFlag = HPRTPrinterHelper.PortClose();
        }
        if (isFlag) {
            //System.out.println("断开成功!");
            this.mCallback.success("断开成功!");
        } else {
            //System.out.println("断开失败!");
            this.mCallback.error("断开失败!");
        }

    }

    /**
     * 获取打印状态
     */
    private void getStatus() {
        if (isPaired) {
            int status = HPRTPrinterHelper.getstatus();
            switch (status) {
            case 0:
                //System.out.println("-----准备就绪-----");
                this.mCallback.success("准备就绪!");
                break;
            case 2:
                //System.out.println("-----缺纸-----");
                this.mCallback.error("缺纸!");
                break;
            case 6:
                //System.out.println("-----开盖-----");
                this.mCallback.error("开盖!");
                break;
            default:
                //System.out.println("-----错误-----");
                this.mCallback.error("错误!");
                break;
            }
        } else {
            //System.out.println("还没配对，请先配对");
            this.mCallback.error("还没配对，请先配对!");
        }

    }

    /**
     * 物料打印
     * 
     * @param arg0
     * @param arg1
     * @param arg2
     * @param arg3
     * @param arg4
     * @param arg5
     */
    private void printMaterial(String arg0, String arg1, String arg2, String arg3, String arg4, String arg5) {
        // 判断有没有配对
        if (!isPaired) {
            //System.out.println("还没配对，请先配对");
            this.mCallback.error("还没配对，请先配对!");
            return;
        }

        // 设置 页标签开始指令
        /**
         * offset：此值使所有字段将水平偏移指定的单位数量; Horizontal：水平方向的 dpi; Vertical：垂直方向的 dpi。
         * Height：整个标签的高度。40mm*60mm 335; Qty：打印的次数
         */
        HPRTPrinterHelper.printAreaSize("65", "200", "200", "300", "1");

        // 设置对齐方式
        HPRTPrinterHelper.Align(HPRTPrinterHelper.LEFT);
        // 打印条码
        /**
         * Command：条码方向：HPRTPrinterHelper.BARCODE：水平方向;HPRTPrinterHelper.
         * VBARCODE：垂直方向; Type：条码类型：HPRTPrinterHelper.code128; Width：窄条的单位宽度。
         * Ratio：宽条窄条的比例; Height：条码高度; X：条码的起始横坐标; Y：条码的起始纵坐标。
         * Undertext：条码下方的数据是否可见。ture：可见，false：不可见; Number：字体的类型; Size：字体的大小。
         * Offset：条码与文字间的距离; Data：条码数据。
         */
        HPRTPrinterHelper.SetBold("0");
        HPRTPrinterHelper.Barcode(HPRTPrinterHelper.BARCODE, HPRTPrinterHelper.code128, "1", "0", "60", "10", "10", false, "0", "0", "0", arg0);

        // 打印文本
        /**
         * Command：文字的方向，总的有四种： HPRTPrinterHelper.TEXT：从左向右。 Font： size：
         * X：起始点的横坐标。 Y：起始点的纵坐标。 Data：文本数据
         */
        HPRTPrinterHelper.SetBold("2");
        HPRTPrinterHelper.Text(HPRTPrinterHelper.TEXT, "4", "0", "0", "80", arg1);

        HPRTPrinterHelper.SetBold("2");
        HPRTPrinterHelper.AutLine("0", "115", 550, 4, arg2);

        HPRTPrinterHelper.SetBold("2");
        HPRTPrinterHelper.Text(HPRTPrinterHelper.TEXT, "4", "0", "0", "190", arg3);

        HPRTPrinterHelper.SetBold("2");
        HPRTPrinterHelper.Text(HPRTPrinterHelper.TEXT, "4", "0", "250", "190", arg4);

        HPRTPrinterHelper.SetBold("2");
        HPRTPrinterHelper.Text(HPRTPrinterHelper.TEXT, "4", "0", "0", "225", "批次：");

        HPRTPrinterHelper.SetBold("0");
        HPRTPrinterHelper.Barcode(HPRTPrinterHelper.BARCODE, HPRTPrinterHelper.code128, "1", "0", "50", "80", "225", true, "0", "0", "5", arg5);
       
        HPRTPrinterHelper.Form(); //走纸到下一张标签
        HPRTPrinterHelper.Print();
        this.mCallback.success("打印成功!");
    }

    /**
     * 储位打印
     * 
     * @param arg0
     */
    private void printCell(String arg0,String arg1) {
        // 判断有没有配对
        if (!isPaired) {
            //System.out.println("还没配对，请先配对");
            this.mCallback.error("还没配对，请先配对!");
            return;
        }
        HPRTPrinterHelper.printAreaSize("0", "200", "200", "300", "1");
        // 设置对齐方式
        HPRTPrinterHelper.Align(HPRTPrinterHelper.LEFT);
        HPRTPrinterHelper.SetBold("0");
        HPRTPrinterHelper.Barcode(HPRTPrinterHelper.BARCODE, HPRTPrinterHelper.code128, "2", "1", "128", "105", "60", false, "0", "1", "20", arg0);

        HPRTPrinterHelper.Align(HPRTPrinterHelper.CENTER);
        HPRTPrinterHelper.SetBold("1");
        HPRTPrinterHelper.Text(HPRTPrinterHelper.TEXT, "0", "3", "0", "210",arg1);

        HPRTPrinterHelper.Form(); //走纸到下一张标签
        HPRTPrinterHelper.Print();
        this.mCallback.success("打印成功!");

    }

    /**
     * 打印图片
     * 
     * @param arg0
     */
    private void printPic(String arg0) {
        // 判断有没有配对
        if (!isPaired) {
            //System.out.println("还没配对，请先配对");
            this.mCallback.error("还没配对，请先配对!");
            return;
        }
        HPRTPrinterHelper.printAreaSize("65", "200", "200", "335", "1");
        HPRTPrinterHelper.Expanded("0", "0", arg0);
        HPRTPrinterHelper.Print();
        this.mCallback.success("打印成功!");
    }

    /**
     * 关闭蓝牙
     */
    private void closeBluetooth() {
        if (mBluetoothAdapter != null) {
            if (mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.disable();
            }
            this.mCallback.success("关闭蓝牙成功!");
            //System.out.println("关闭蓝牙成功!");
        } else {
            this.mCallback.error("本地蓝牙不可用!");
            //System.out.println("本地蓝牙不可用!");
        }

    }


    @Override
    public void onDestroy() {
        cordova.getActivity().unregisterReceiver(bluetoothReceiver);
        super.onDestroy();
    }
}

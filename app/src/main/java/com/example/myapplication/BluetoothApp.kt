package com.example.myapplication

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.TRANSPORT_LE
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import java.io.File
import java.util.UUID

class BluetoothApp(private val context: Context) {
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val bluetoothAdapter = bluetoothManager.adapter
   private val BLEScanner = bluetoothAdapter.bluetoothLeScanner
    private val  handler = Handler(Looper.getMainLooper())

    private val SCAN_PERIOD: Long = 10000
    private var scanning = false
 //   var isConnected= false

    var devices = listOf<BluetouchDevice>()
    var storedDevices = listOf<BluetouchDevice>()
    var connectedDevices = listOf<BluetoothDevice>()
    private val file = File(context.applicationContext.filesDir, "devices.json")
    var bluetoothGatt: BluetoothGatt? = null

    lateinit var wCharacteristic: BluetoothGattCharacteristic

    lateinit var rCharacteristic: BluetoothGattCharacteristic
    var _currentDevice = BluetouchDevice()
   private  val TRANSPARENT_UUID_SERVICE = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb")
    var wdata: ByteArray = ByteArray(3)
   @OptIn(ExperimentalUnsignedTypes::class)
   var rdata: UByteArray = UByteArray(6)





    private val leScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            val device = result.device
            val BleDevice = BluetouchDevice(
                name = device.name,
                addres = device.address
            )
            if (!devices.contains(BleDevice)) {

                devices = devices + BleDevice
            }
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            println("Найдено устройство: ${device.name ?: "Unknown"}, Адрес: ${device.address}")
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            println("Сканирование завершено с ошибкой: $errorCode")
        }
    }

    //  @RequiresApi(Build.VERSION_CODES.M)
    fun startBleScan() {
        if (!scanning) { // Stops scanning after a pre-defined scan period.
            handler.postDelayed({
                scanning = false
                stopBleScan()
            }, SCAN_PERIOD)
            scanning = true
        }
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
             .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
               .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
            .build()
        var scanFilters = listOf<ScanFilter>() // Можно добавить фильтры, если нужно
        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid.fromString("0000fff0-0000-1000-8000-00805f9b34fb"))
            .build()
        //scanFilters = scanFilters + filter
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        BLEScanner.startScan(scanFilters, scanSettings, leScanCallback)
    }

    private fun stopBleScan() {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        BLEScanner.stopScan(leScanCallback)
    }

    fun checkBluetoothStatus():Boolean {
        if (bluetoothAdapter?.isEnabled == true) {
            println("Bluetooth включен")
            return true
        } else {
            println("Bluetooth выключен")
            return false
        }
    }

    fun addDevice(device: BluetouchDevice) {
        if (!storedDevices.contains(device)) {
            storedDevices = storedDevices + device
            val gson = Gson()
            val json = gson.toJson(storedDevices)
            println("JSON: $json")
            file.writeText(json)

        }
    }

    fun deleteDevice(device: BluetouchDevice) {
        if (storedDevices.contains(device)) {
            storedDevices = storedDevices - device
            val gson = Gson()
            val json = gson.toJson(storedDevices)
            println("JSON: $json")
            file.writeText(json)

        }
    }

    fun readDevice() {
        if (file.exists()) {
            val json = file.readText()
            val gson = Gson()
            storedDevices = gson.fromJson(json, Array<BluetouchDevice>::class.java).toList()
        }
    }

    fun getConnectedBleDevices(context: Context): List<BluetoothDevice> {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {

        }
        return  bluetoothManager.getConnectedDevices(BluetoothProfile.GATT)
    }

    fun isConnected(deviceAddres: String?):Boolean {
       var  result = false
        connectedDevices = getConnectedBleDevices(context)
        connectedDevices.forEach { connectedDevice ->
             if (deviceAddres == connectedDevice.address) result = true
            else result = true
        }
        return result
    }

    fun deviceConnect(deviceAddres: String?): BluetoothGatt? {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }
        val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddres)
        bluetoothGatt = device.connectGatt(context, true, gattCallback, TRANSPORT_LE)
        return bluetoothGatt
    }

    fun deviceDisconnect()
    {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        bluetoothGatt?.disconnect()
    }

    fun setCurrentDevice(device: BluetouchDevice) {
        _currentDevice = device
    }


    @OptIn(ExperimentalUnsignedTypes::class)
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                println("Успешное подключение")
                //        isConnected=true
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                gatt.discoverServices()
                println("Начинаем поиск сервисов")
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // Отключение
                //       isConnected=false
                gatt.close()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {

                println("Services discovered")
                val services = gatt.services

                for (service in services) {
                    if (service.uuid == TRANSPARENT_UUID_SERVICE) {
                        val characteristics = service.characteristics
                        for (characteristic in characteristics) {
                            if (characteristic.uuid == UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb")) {
                                if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_READ != 0) {
                                    rCharacteristic = characteristic
                                    enableNotifications(characteristic)
                                    println("Найдена характеристика для чтения{$characteristic.uuid}")
                                }
                            }

                            if (characteristic.uuid == UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb")) {
                                if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_WRITE != 0) {

                                    wCharacteristic = characteristic

                                    println("Найдена характеристика для записи{$characteristic.uuid}")

                                }
                            }

                        }

                    }
                    println("Service UUID: ${service.uuid}")
                }
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Запись успешна
                println("Data written successfully")
            } else {
                // Ошибка записи
                println("Failed to write data")
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    val value = characteristic.value

                    // Обработка полученных данных
                }

                else -> {
                    Log.e("BLE", "Read failed with status: $status")
                }
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            rdata = characteristic.value.asUByteArray()
        }


    }

    fun getNotificationData():UByte
    {

        return rdata.last()
    }

    fun writeDataToCharacteristic(
        characteristic: BluetoothGattCharacteristic = wCharacteristic,
        data: ByteArray = wdata
    ) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_WRITE != 0) {
            bluetoothGatt?.writeCharacteristic(characteristic, data, WRITE_TYPE_NO_RESPONSE)
        }

    }




    fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        bluetoothGatt?.readCharacteristic(characteristic)
    }

    fun enableNotifications(characteristic: BluetoothGattCharacteristic) {
        bluetoothGatt?.let { gatt ->
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) { return}

                gatt.setCharacteristicNotification(characteristic, true)

                val descriptor = characteristic.getDescriptor(
                    UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
                )
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE

                gatt.writeDescriptor(descriptor)

        }
    }


    fun writeData(data: ByteArray) {
        wdata = data
    }


    fun iswCharacteristicInitialized(): Boolean = ::wCharacteristic.isInitialized
    fun isrCharacteristicInitialized(): Boolean = ::rCharacteristic.isInitialized
}


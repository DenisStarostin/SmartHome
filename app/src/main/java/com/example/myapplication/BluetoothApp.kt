package com.example.myapplication

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.TRANSPORT_LE
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import java.io.File
import java.util.UUID

class BluetoothApp(private val context: Context) {
    val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val bluetoothAdapter = bluetoothManager.adapter
    val BLEScanner = bluetoothAdapter.bluetoothLeScanner
    val handler = Handler(Looper.getMainLooper())

    private val SCAN_PERIOD: Long = 10000
    var scanning = false
    var devices = listOf<BluetouchDevice>()
    var storedDevices = listOf<BluetouchDevice>()
    val file = File(context.applicationContext.filesDir, "devices.json")
    var bluetoothGatt: BluetoothGatt? = null
    lateinit var wCharacteristic: BluetoothGattCharacteristic
    lateinit var rCharacteristic: BluetoothGattCharacteristic
    var _currentDevice= BluetouchDevice()
    val TRANSPARENT_UUID_SERVICE = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb")




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

    @RequiresApi(Build.VERSION_CODES.M)
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
          //  .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
         //   .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
            .build()
        var scanFilters = listOf<ScanFilter>() // Можно добавить фильтры, если нужно
       val filter=ScanFilter.Builder()
           .setServiceUuid(ParcelUuid.fromString("0000fff0-0000-1000-8000-00805f9b34fb"))
           .build()
            scanFilters=scanFilters+filter
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

    fun checkBluetoothStatus() {
        if (bluetoothAdapter?.isEnabled == true) {
            println("Bluetooth включен")
        } else {
            println("Bluetooth выключен")
        }
    }

    fun addDevice(device: BluetouchDevice)
    {
        if (!storedDevices.contains(device)) {
            storedDevices = storedDevices + device
            val gson = Gson()
            val json = gson.toJson(storedDevices)
            println("JSON: $json")
            file.writeText(json)
        }
    }

    fun readDevice()
    {
        if (file.exists()) {
            val json = file.readText()
            val gson = Gson()
            storedDevices = gson.fromJson(json, Array<BluetouchDevice>::class.java).toList()
        }
    }

    fun deviceConnect(deviceAddres: String?):BluetoothGatt?
    {
        val device:BluetoothDevice=bluetoothAdapter.getRemoteDevice(deviceAddres)

            if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }
            bluetoothGatt = device.connectGatt(context, true, gattCallback, TRANSPORT_LE)
        return bluetoothGatt
    }

    fun setCurrentDevice(device: BluetouchDevice)
    {
         _currentDevice = device
    }

    fun removeDevice(device: BluetouchDevice)
    {
        storedDevices=storedDevices-device
    }


    val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                println("Успешное подключение")
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
                                if(characteristic.properties and BluetoothGattCharacteristic.PROPERTY_READ != 0)
                                {
                                    rCharacteristic=characteristic
                                    println("Найдена характеристика для чтения{$characteristic.uuid}")
                                    readCharacteristic(characteristic)
                                }
                            }

                            if (characteristic.uuid == UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb")) {
                                if(characteristic.properties and BluetoothGattCharacteristic.PROPERTY_WRITE != 0)
                                {
                                  //  characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                                    wCharacteristic=characteristic
                                    println("Найдена характеристика для записи{$characteristic.uuid}")
                                    writeDataToCharacteristic(characteristic)
                                }
                            }

                        }

                    }
                    println("Service UUID: ${service.uuid}")
                }
            }
        }
    }

    fun writeDataToCharacteristic(characteristic: BluetoothGattCharacteristic ) {
        // Используем новый метод setValue
    //    val success = characteristic.setValue()
 //       if (success) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {return}
            bluetoothGatt?.writeCharacteristic(characteristic,byteArrayOf(0x01, 0x02, 0x03),WRITE_TYPE_NO_RESPONSE)
    //    } else {
     //       println("Failed to set characteristic value")
     //   }
    }



    val gattCallbackR_ = object : BluetoothGattCallback() {
        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Запись успешна
                println("Data written successfully")
            } else {
                // Ошибка записи
                println("Failed to write data")
            }
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
    @kotlin.ExperimentalStdlibApi
    val gattCallback_ = object : BluetoothGattCallback() {
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                println("Read value: ${characteristic.value.toHexString()}")
            }
        }
    }

    var wdata: ByteArray = ByteArray(3)

    fun writeData(data: ByteArray) {
        wdata = data
    }


}

private fun BluetoothGattCharacteristic.addDescriptor(fromString: UUID?) {

}



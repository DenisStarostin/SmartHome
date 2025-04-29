package com.example.myapplication

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class thermostatUI (bluetoothApp: BluetoothApp, context: Context, navController: NavController)
{

    @Composable
    fun Top_temp(temp: String, bluetoothApp: BluetoothApp) {

    }

    @Composable
    fun Top_ctrl(bluetoothApp: BluetoothApp) {
        val ctrl_state = remember { mutableStateOf("") }
        var isConnected by remember { mutableStateOf(false) }
        isConnected = bluetoothApp.isConnected(bluetoothApp._currentDevice.addres)
        Surface(
            color = Color.Black,
            //  modifier = Modifier.padding(horizontal = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(vertical = 50.dp)
                    .fillMaxWidth(1f),
                horizontalArrangement = Arrangement.Center,
                //   verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(2f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(

                        onClick = { ctrl_state.value = "on" },
                        modifier = Modifier.size(width = 100.dp, height = 40.dp),
                        enabled = isConnected,
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color(0xff004D40),
                            containerColor = if (ctrl_state.value == "on") Color(0xFF558B2F)
                            else Color(0xFF677774)

                        )
                    ) { Text("Вкл", fontSize = 18.sp) }
                }
                Column(
                    modifier = Modifier.weight(2f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = { ctrl_state.value = "off" },
                        modifier = Modifier.size(width = 100.dp, height = 40.dp),
                        enabled = isConnected,
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color(0xff004D40),       // цвет текста
                            containerColor = if (ctrl_state.value == "off") Color(0xFFE81E1E)
                            else Color(0xFF677774)
                        )
                    ) { Text("Выкл", fontSize = 18.sp) }
                }
                Column(
                    modifier = Modifier.weight(2f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = { ctrl_state.value = "auto" },
                        modifier = Modifier.size(width = 100.dp, height = 40.dp),
                        enabled = isConnected,
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color(0xff004D40),
                            containerColor = if (ctrl_state.value == "auto") Color(0xFFF9A825)
                            else Color(0xFF677774)
                        )
                    ) { Text("Авто", fontSize = 18.sp) }
                }

            }
        }
    }

    @Composable
    fun Greeting(name: String, context: Context, bluetoothApp: BluetoothApp): String {

        val textFieldVal = remember { mutableStateOf("0") }

        val keyboardController = LocalSoftwareKeyboardController.current
        Surface(
            color = Color.Black, modifier = Modifier.padding(horizontal = 0.dp)
        ) {
            Row(modifier = Modifier.padding(top = 40.dp, end = 20.dp, start = 20.dp)) {
                Column(
                    modifier = Modifier
                        .weight(2f)
                        .align(Alignment.CenterVertically)
                ) {
                    Text(
                        text = "$name температура",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        lineHeight = 30.sp,
                        textAlign = TextAlign.Start
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),

                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = textFieldVal.value,
                        textStyle = TextStyle(fontSize = 18.sp, color = Color.Black),
                        onValueChange = { newText ->
                            textFieldVal.value = newText
                        },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),

                        modifier = Modifier
                            .background(Color.White)
                            .width(80.dp)
                            .height(50.dp)


                    )
                }
            }
        }
        return textFieldVal.value
    }

    @Composable
    fun Footer(bluetoothApp: BluetoothApp, max_temp: String, min_temp: String, context: Context) {
        var errorMessage = arrayOf("")
        var parseValueMax = parseInputTemp(max_temp, context)
        var parseValueMin = parseInputTemp(min_temp, context)
        var valueMax :Byte
        var valueMin :Byte

        Surface(
            color = Color.Black,
        ) {
            Row(
                modifier = Modifier
                    .padding(vertical = 50.dp)
                    .fillMaxWidth(1f)
                    .fillMaxHeight(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(
                    modifier = Modifier.weight(2f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            valueMax=parseValueMax.Handler()
                            valueMin=parseValueMin.Handler()
                            if (!parseValueMax.errorMessage.isNullOrEmpty()) {
                                var alert = AlertDialog.Builder(context, 2).setTitle("Максимальная температура")
                                    .setMessage(parseValueMax.errorMessage)
                                    .setPositiveButton("Ok") { _, _ -> }
                                    .create()
                                    .show()
                            }else
                                if (!parseValueMin.errorMessage.isNullOrEmpty()) {
                                    var alert = AlertDialog.Builder(context, 2).setTitle("Максимальная температура")
                                        .setMessage(parseValueMax.errorMessage)
                                        .setPositiveButton("Ok") { _, _ -> }
                                        .create()
                                        .show()
                                }else {

                                    bluetoothApp.writeData(byteArrayOf(0x55, valueMax, valueMin))
                                    bluetoothApp.writeDataToCharacteristic()
                                }
                        },
                        modifier = Modifier.size(width = 300.dp, height = 50.dp),
                        enabled = bluetoothApp.isConnected(bluetoothApp._currentDevice.addres),
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color(0xff004D40), containerColor = Color(0xFF558B2F)
                        )
                    ) { Text("Применить", fontSize = 28.sp) }
                }


            }
        }

    }

    @OptIn(ExperimentalUnsignedTypes::class)
    @SuppressLint("CoroutineCreationDuringComposition")
    @Composable
    fun Thermostat(bluetoothApp: BluetoothApp, context: Context, navController: NavController) {
        var max_temp: String
        var min_temp: String
        val device = bluetoothApp._currentDevice
        var isConnected by remember { mutableStateOf(false) }

        var  temperature by remember { mutableStateOf("") }
        isConnected = bluetoothApp.isConnected(device.addres)

        LaunchedEffect(device.addres) {
                while (!isConnected) {
                    isConnected = withContext(Dispatchers.IO) {
                        bluetoothApp.deviceConnect(device.addres)
                        delay(100)
                        bluetoothApp.isConnected(device.addres)
                    }
                    delay(1000)
                }

            }


   LaunchedEffect(isConnected) {
      if (isConnected) {
          var data = tempData(0.0, 0.0)
          while (isConnected) {
              bluetoothApp.writeData(byteArrayOf(0x4E))
              if (bluetoothApp.iswCharacteristicInitialized()) {
                  bluetoothApp.writeDataToCharacteristic()
              }
              val dataIn = withContext(Dispatchers.IO) {
                  bluetoothApp.rdata.toTypedArray()


              }

              if (sht40_DataHandler(dataIn,data))
              {
                  temperature = data.temp.toString().take(4)
              }
              delay(2000) // Обновление каждую секунду
          }
      }
  }
  Surface(color = Color.Black) { Row() { Column { MainMenu(navController) } } }
  Column {
      Row(
          modifier = Modifier
              .fillMaxWidth(1f)
              .padding(top = 160.dp),
          horizontalArrangement = Arrangement.Absolute.Center
      ) {
          if (isConnected) {
              Text(
                  modifier = Modifier.padding(8.dp),
                  text = temperature,
                  fontSize = 90.sp,
                  lineHeight = 70.sp,
                  textAlign = TextAlign.Center,
                  color = Color.LightGray
              )
          }else CircularProgressIndicator()
      }
      Top_ctrl(bluetoothApp)
      max_temp = Greeting("Максимальная",context, bluetoothApp)
      min_temp = Greeting("Минимальная",context, bluetoothApp)
      Footer(bluetoothApp, max_temp, min_temp, context)
  }
}

@OptIn(ExperimentalUnsignedTypes::class)
fun sht40_DataHandler(data: Array<UByte>, outData: tempData):Boolean
{
    val tempFirstData= data[0].toInt() and 0xFF
    val tempSecondData= data[1].toInt() and 0xFF
  var checkData = data.copyOfRange(0,2)
  if (crc8_Check(checkData, data[2]))
  {

    val tmpValue= 175.0 * (tempFirstData * 256 + tempSecondData)/65535.0-45.0
    outData.temp=tmpValue.toDouble()
  }else return false
  checkData = data.copyOfRange(3,5)
//  if (crc8_Check(checkData, data[5]))
//  {
      outData.humidification = 100.0 *(data[3].toInt() and 0xFF * 256 + data[4].toInt() and 0xFF)/65535.0
//  }else return false
  return true
}

@OptIn(ExperimentalUnsignedTypes::class)
fun calculateCrc8(data: Array<UByte>, crc: Int = 0xFF): Int {
  var currentCrc = crc

  for (currentind in 0 until 2) {
      currentCrc = currentCrc xor data[currentind].toInt()
      for (bit in 8 downTo 1) {
          if (currentCrc and 0x80 != 0) {
              currentCrc = (currentCrc shl 1) xor 0x31  // Пример полинома CRC-8
          } else {
              currentCrc = currentCrc shl 1
          }
          currentCrc = currentCrc and 0xFF  // Ограничение до 8 бит
      }
  }
  return currentCrc
}

@OptIn(ExperimentalUnsignedTypes::class)
fun crc8_Check(data: Array<UByte>, checkSum: UByte):Boolean
{
  if (calculateCrc8(data).toUByte() != checkSum)return false
  else return true
}

}

data class tempData(var temp: Double, var humidification: Double)
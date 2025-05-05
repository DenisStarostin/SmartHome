package com.example.myapplication

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class thermostatUI(bluetoothApp: BluetoothApp, context: Context, navController: NavController) {

    var data = tempData(0.0, 0.0)

    @Composable
    fun topControl(bluetoothApp: BluetoothApp) {
        val ctrl_state = remember { mutableStateOf("") }
        var isConnected by remember { mutableStateOf(false) }
        isConnected = bluetoothApp.isConnected(bluetoothApp._currentDevice.addres)

            Row(
                modifier = Modifier
                    .padding(vertical = 20.dp)
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

    @Composable
    fun Greeting(name: String, context: Context, bluetoothApp: BluetoothApp): String {

        val textFieldVal = remember { mutableStateOf("0") }



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

        return textFieldVal.value
    }

    @Composable
    fun Footer(bluetoothApp: BluetoothApp, max_temp: String, min_temp: String, context: Context) {
        var errorMessage = arrayOf("")
        var parseValueMax = parseInputTemp(max_temp, context)
        var parseValueMin = parseInputTemp(min_temp, context)
        var valueMax: Byte
        var valueMin: Byte

            Row(
                modifier = Modifier
                    .padding(bottom = 50.dp)
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
                            valueMax = parseValueMax.Handler()
                            valueMin = parseValueMin.Handler()
                            if (!parseValueMax.errorMessage.isNullOrEmpty()) {
                                var alert = AlertDialog.Builder(context, 2)
                                    .setTitle("Максимальная температура")
                                    .setMessage(parseValueMax.errorMessage)
                                    .setPositiveButton("Ok") { _, _ -> }
                                    .create()
                                    .show()
                            } else
                                if (!parseValueMin.errorMessage.isNullOrEmpty()) {
                                    var alert = AlertDialog.Builder(context, 2)
                                        .setTitle("Максимальная температура")
                                        .setMessage(parseValueMax.errorMessage)
                                        .setPositiveButton("Ok") { _, _ -> }
                                        .create()
                                        .show()
                                } else {

                                    bluetoothApp.writeData(byteArrayOf(0x55, valueMax, valueMin))
                                    bluetoothApp.writeDataToCharacteristic()
                                }
                        },
                        modifier = Modifier.size(width = 300.dp, height = 50.dp),
                        enabled = bluetoothApp.isConnected(bluetoothApp._currentDevice.addres),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color(0xff004D40), containerColor = Color(0xFF558B2F),

                        )
                    ) { Text("Применить", fontSize = 28.sp) }
                }


            }


    }
@Composable
fun topIndicator(temperature: String, humidification: String)
{
  var textfieldSetTemp by remember { mutableStateOf("0") }
  var textfieldSetHumi by remember { mutableStateOf("0") }
  var temperatureSwitch by remember { mutableStateOf(false) }
  var humidificationSwitch by remember { mutableStateOf(false) }
  textfieldSetTemp=data.targetTemperatureValue
  textfieldSetHumi=data.targetHumidificationValue
    Row {
        val fontSize = 50
        val iconSize = 40

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 10.dp),
            //    .height(200.dp),
            horizontalAlignment = Alignment.Start
        )
        {
            Card(
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .height((fontSize + 150).dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFB97D04)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            )
            {
                Row {
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.thermometr),
                            contentDescription = "Thermometr",
                            tint = Color.Red,
                            modifier = Modifier
                                .size(iconSize.dp)
                                .align(Alignment.Start)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(0.dp),

                        verticalArrangement = Arrangement.Center


                    )
                    {
                        Row(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)

                        ) {
                            if (temperature != "")
                            {
                                Text(
                                    modifier = Modifier
                                        .padding(0.dp),
                                    text = temperature,
                                    fontSize = fontSize.sp,
                                    lineHeight = 70.sp,
                                    textAlign = TextAlign.Center,
                                    color = Color.LightGray
                                )
                            } else CircularProgressIndicator(
                                modifier = Modifier
                            )
                        }
                        Row(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 10.dp)
                        ) {
                            Column {
                                TextField(
                                    value = textfieldSetTemp,
                                    //  textStyle = TextStyle(fontSize = 12.sp, color = Color.Black),
                                    onValueChange = { newText ->
                                        textfieldSetTemp = newText
                                    },
                                    modifier = Modifier
                                        .size(100.dp, 50.dp)
                                        .align(Alignment.CenterHorizontally),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        focusedTextColor = Color.Black,
                                        unfocusedTextColor = Color.Black
                                    ),
                                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                                )
                            }
                        }
                        Row(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 10.dp)
                        )
                        {
                            Switch(
                                checked = temperatureSwitch,
                                onCheckedChange = { temperatureSwitch = it
                                                  if(it and (textfieldSetTemp == "0"))textfieldSetTemp=temperature
                                                    else textfieldSetTemp="0"
                                                  },
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = Color.Green,
                                    uncheckedTrackColor = Color.DarkGray
                                ),
                            )
                        }
                    }

                }
            }
        }



        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 10.dp),
            horizontalAlignment = Alignment.End
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .height((fontSize + 150).dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF4D5A9B)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            )
            {
                Row {
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                    )
                    {
                        Icon(
                            painter = painterResource(id = R.drawable.moisture),
                            contentDescription = "Moisture",
                            tint = Color.Blue,
                            modifier = Modifier
                                .size(iconSize.dp)
                                .align(Alignment.Start)
                                .padding(start = 0.dp)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(0.dp),
                        verticalArrangement = Arrangement.Center
                    )
                    {
                        Row (
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)

                        ){
                            if (humidification != "") {
                                Text(
                                    modifier = Modifier

                                        .padding(0.dp),
                                    text = humidification,
                                    fontSize = fontSize.sp,
                                    lineHeight = 70.sp,
                                    textAlign = TextAlign.Center,
                                    color = Color.LightGray
                                )

                            } else CircularProgressIndicator(
                                modifier = Modifier
                            )
                        }

                    Row(
                        modifier = Modifier
                             .align(Alignment.CenterHorizontally)
                            .padding(top = 10.dp)
                    ) {
                        Column {
                            TextField(
                                value = textfieldSetHumi,
                                //  textStyle = TextStyle(fontSize = 12.sp, color = Color.Black),
                                onValueChange = { newText ->
                                    textfieldSetHumi = newText
                                },
                                modifier = Modifier
                                    .size(100.dp, 50.dp)
                                    .align(Alignment.CenterHorizontally),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black
                                ),
                                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 10.dp)
                    )
                    {
                        Switch(
                            checked = humidificationSwitch,
                            onCheckedChange = { humidificationSwitch = it
                                                if(it)textfieldSetHumi=humidification
                                                else textfieldSetHumi="0"
                                              },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = Color.Green,
                                uncheckedTrackColor = Color.DarkGray
                            ),
                        )
                    }
                    }
                }
            }
        }
    }

}

    @Composable
    fun sheduleView()
    {
        Row {
            ElevatedCard (

                    colors = CardDefaults.outlinedCardColors( containerColor = Color.DarkGray
                    ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 6.dp
                ),
                shape = CardDefaults.outlinedShape,
                modifier = Modifier
                    .size(width = 600.dp, height = 350.dp)
                    .padding(horizontal = 10.dp)
                    .padding(top=20.dp, bottom = 40.dp)
                    .fillMaxWidth()


            ){
                Text(
                    text = "Расписание",
                    modifier = Modifier
                    .padding(16.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 22.sp
                )
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

        var temperature by remember { mutableStateOf("") }
        var humadification by remember { mutableStateOf("") }
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

                while (isConnected) {
                    bluetoothApp.writeData(byteArrayOf(0x4E))
                    if (bluetoothApp.iswCharacteristicInitialized()) {
                        bluetoothApp.writeDataToCharacteristic()
                    }
                    val dataIn = withContext(Dispatchers.IO) {
                        bluetoothApp.rdata.toTypedArray()
                    }

                    if (sht40_DataHandler(dataIn, data)) {
                        temperature = data.temp.toString().take(4)
                        humadification = data.humidification.toString().take(4)
                    }
                    delay(2000)
                }
            }
        }
         Row() { Column { MainMenu(navController, bluetoothApp) } }
        Row(
            modifier = Modifier
                .padding(top = 200.dp)

        ) {
            Column {
                topIndicator(temperature, humadification)
                sheduleView()
             //  topControl(bluetoothApp)
            //    max_temp = Greeting("Максимальная", context, bluetoothApp)
             //   min_temp = Greeting("Минимальная", context, bluetoothApp)

                Footer(bluetoothApp, "0","0", context)
            }
        }
    }



    @OptIn(ExperimentalUnsignedTypes::class)
    fun sht40_DataHandler(data: Array<UByte>, outData: tempData): Boolean {
        val tempFirstData = data[0].toInt() and 0xFF
        val tempSecondData = data[1].toInt() and 0xFF
        val humFirstData = data[3].toInt() and 0xFF
        val humSecondData = data[4].toInt() and 0xFF
        var checkData = data.copyOfRange(0, 2)
        if (crc8_Check(checkData, data[2])) {
            val tmpValue = 175.0 * (tempFirstData * 256 + tempSecondData) / 65535.0 - 45.0
            outData.temp = tmpValue.toDouble()
        } else return false

        checkData = data.copyOfRange(3, 5)
        if (crc8_Check(checkData, data[5])) {
            outData.humidification = 100.0 * (humFirstData * 256 + humSecondData) / 65535.0
        } else return false
        return true
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun calculateCrc8(data: Array<UByte>, crc: Int = 0xFF): Int {
        var currentCrc = crc

        for (currentind in 0 until 2) {
            currentCrc = currentCrc xor data[currentind].toInt()
            for (bit in 8 downTo 1) {
                if (currentCrc and 0x80 != 0) {
                    currentCrc = (currentCrc shl 1) xor 0x31
                } else {
                    currentCrc = currentCrc shl 1
                }
                currentCrc = currentCrc and 0xFF
            }
        }
        return currentCrc
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun crc8_Check(data: Array<UByte>, checkSum: UByte): Boolean {
        if (calculateCrc8(data).toUByte() != checkSum) return false
        else return true
    }

}

data class tempData(var temp: Double, var humidification: Double)
{
    var targetTemperatureValue = "0"
    var targetHumidificationValue = "0"
}
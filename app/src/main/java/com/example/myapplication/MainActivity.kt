package com.example.myapplication

//import androidx.compose.foundation.layout.FlowColumnScopeInstance.align

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine


sealed class NavRoutes(val route: String) {
    object Home : NavRoutes("home")
    object Macro : NavRoutes("macro")
    object Setting : NavRoutes("setting")
    object Thermostat : NavRoutes("thermostat")
    object AddDevice : NavRoutes("adddevice")
}




class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()



        setContent {
            MyApplicationTheme {
                Main(modifier = Modifier.fillMaxSize(), this)
            }
        }
    }

}

@Composable
fun Main(modifier: Modifier = Modifier, context: Context) {
    val navController = rememberNavController()
    val bluetoochapp = BluetoothApp(context)
    bluetoochapp.readDevice()
    Surface(color = Color.Black)
    {
        Row() { Column { MainMenu(navController) } }
        NavHost(navController, startDestination = NavRoutes.Home.route) {
            composable(NavRoutes.Home.route) { Home(context, bluetoochapp, navController) }
            composable(NavRoutes.Macro.route) { Macro() }
            composable(NavRoutes.Setting.route) { Setting() }
            composable(NavRoutes.Thermostat.route) { Thermostat(modifier = Modifier.fillMaxSize(), bluetoochapp) }
            composable(NavRoutes.AddDevice.route) { AddDevice(context, bluetoochapp) }
        }
    }
}

@Composable
fun Home(context: Context, bluetoothApp: BluetoothApp, navController: NavController) {
   val  bluetoothAdapter= bluetoothApp.bluetoothAdapter

  /* if (!bluetoothAdapter.isEnabled) {
        // Bluetooth выключен, запросите его включение
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
      //  startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)

    } else {
        // Bluetooth включен, можно начинать сканирование
        println("Bluetooth доступен и включен")
    }*/
    BluetoothDeviceList(
        bluetoothApp,
        onDeviceClick = { device ->
            if(device.name=="Ths_hlk")
                bluetoothApp.setCurrentDevice(device)
                navController.navigate(NavRoutes.Thermostat.route)
                        } ,
        bluetoothApp.storedDevices
        )


}

@Composable
fun Macro() {
    Row(modifier = Modifier.padding(top = 150.dp)) {
        Text("Macro")
    }
}

@Composable
fun Setting() {
    Row(modifier = Modifier.padding(top = 150.dp)) {
        Text("Setting")
    }
}

@Composable
fun Thermostat(modifier: Modifier = Modifier, bluetoothApp: BluetoothApp)
{
    bluetoothApp.writeData(byteArrayOf(0x01, 0x02, 0x03))
    val device=bluetoothApp._currentDevice
    bluetoothApp.deviceConnect(device.addres)
    Column {
        Top_temp("")
        Top_ctrl()
        Greeting("Максимальная")
        Greeting("Минимальная")
        Footer()
    }
}

@Composable
fun AddDevice(context: Context, bluetoothApp: BluetoothApp) {
    val handler = Handler(Looper.getMainLooper())

    var updater by remember { mutableStateOf(false) }
    bluetoothApp.checkBluetoothStatus()
    bluetoothApp.startBleScan()

    handler.postDelayed({ updater = true }, 12000)
    if (updater) BluetoothDeviceList(
        bluetoothApp,
        onDeviceClick = { device ->
            CoroutineScope(Dispatchers.Main).launch {
                val result = onClick(context, device)
                if (result) {
                    bluetoothApp.addDevice(device)
                    }
            }
        },
        bluetoothApp.devices
    )
}


@Composable
fun BluetoothDeviceList(
    bluetoothApp: BluetoothApp,
    onDeviceClick: (BluetouchDevice) -> Unit,
    list:List<BluetouchDevice>
)  {
    var devices=list

    Row(modifier = Modifier.padding(top = 180.dp))
    {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(devices.size) { index ->

                BluetoothDeviceItem(
                    devices[index],
                    onClick = onDeviceClick
                )

            }
        }
    }

    
}

fun <LazyItemScope> items(
    count: List<BluetouchDevice>,
    itemContent: @Composable LazyItemScope.(index: Int) -> Unit
) {

}

@Composable
fun BluetoothDeviceItem(
    device: BluetouchDevice,
    onClick: (BluetouchDevice) -> Unit

) {

    var isContextMenuVisible by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick(device) }
        /*    .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        isContextMenuVisible = true
                    }
                )
            }*/
        ,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = device.name ?: "Unknown Device",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = device.addres ?: "Unknown Address",
                style = MaterialTheme.typography.bodySmall
            )
          /*  DropdownMenu(isContextMenuVisible, onDismissRequest = { isContextMenuVisible = false })
            {
                DropdownMenuItem(text = { Text("Удалить") },
                    onClick = {
                        isContextMenuVisible = false

                    })
            }*/

        }
    }

}
@OptIn(ExperimentalCoroutinesApi::class)
suspend fun onClick(context: Context, device: BluetouchDevice): Boolean {
    return suspendCancellableCoroutine { continuation ->
        val dialog = AlertDialog.Builder(context)
        .setTitle("Подключение к ${device.name}")
        .setMessage("Вы уверены, что хотите добавить ${device.name} в список устройств?")
        .setPositiveButton("Подключиться") { _, _ ->
            println("Подключиться к ${device.name}")

            continuation.resume(true, null)

        }
        .setNegativeButton("Отмена") { _, _ ->
            println("Отмена подключения к ${device.name}")
             continuation.resume(false, null)
        }
            .create()
        dialog.show()
        continuation.invokeOnCancellation {
        dialog.dismiss()
        }
    }

}



@Composable
fun MainMenu(navController: NavController) {
    var expanded by remember { mutableStateOf(false) }
    //  val statusBarHeight = WindowInsets.statusBars.getTop(LocalDensity.current)
    var name by remember { mutableStateOf("Дом") }
    Surface(
        color = Color.Black,
        modifier = Modifier
            .padding(top = 0.dp)
            .fillMaxWidth(1f)
    )
    {
        Box(modifier = Modifier.padding(top = 40.dp, start = 10.dp))
        {

            IconButton(
                onClick = { expanded = true },
                colors = IconButtonDefaults.iconButtonColors(Color.LightGray)
            ) {
                Icon(Icons.Default.Menu, contentDescription = "Показать меню")
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    onClick = {
                        navController.navigate(NavRoutes.Home.route)
                        name = "Дом"
                    },
                    text = { Text("Дом") }
                )
                DropdownMenuItem(
                    onClick = {
                        navController.navigate(NavRoutes.Macro.route)
                        name = "Макросы"
                    },
                    text = { Text("Макросы") }
                )
                DropdownMenuItem(
                    onClick = {
                        navController.navigate(NavRoutes.Thermostat.route)
                        name = "Термостат"
                    },
                    text = { Text("Термостат") }
                )
                HorizontalDivider()
                DropdownMenuItem(
                    onClick = {
                        navController.navigate(NavRoutes.Setting.route)
                        name = "Настройки"
                    },
                    text = { Text("Настройки") }
                )
            }
            Text(name, Modifier.align(Alignment.Center), color = Color.LightGray, fontSize = 28.sp)
        }
        if (name == "Дом") {

            Row(
                modifier = Modifier.padding(top = 90.dp, end = 10.dp),
                horizontalArrangement = Arrangement.Absolute.Right,
                verticalAlignment = Alignment.Bottom,

                ) {  //Text("aaa",  color = Color.LightGray, textAlign = TextAlign.Center)
                IconButton(
                    onClick = {
                        navController.navigate(NavRoutes.AddDevice.route)
                        name = "Добавить устройство"
                    },
                    colors = IconButtonDefaults.iconButtonColors(Color.Green)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить устройство")
                }
            }
        }
    }
}


@Composable
fun Top_temp(temp: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth(1f)
            .padding(top = 160.dp),
        horizontalArrangement = Arrangement.Absolute.Center
    )
    {
        Text(
            modifier = Modifier.padding(8.dp),
            text = "0.0",
            fontSize = 90.sp,
            lineHeight = 70.sp,
            textAlign = TextAlign.Center,
            color = Color.LightGray
        )
    }
}

@Composable
fun Top_ctrl() {
    var ctrl_state = remember { mutableStateOf("") }
    Surface(
        color = Color.Black,
        //  modifier = Modifier.padding(horizontal = 2.dp)
    )
    {
        Row(
            modifier = Modifier
                .padding(vertical = 50.dp)
                .fillMaxWidth(1f),
            horizontalArrangement = Arrangement.Center,
            //   verticalAlignment = Alignment.CenterVertically
        )
        {
            Column(
                modifier = Modifier.weight(2f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            )
            {
                Button(

                    onClick = { ctrl_state.value = "on" },
                    modifier = Modifier.size(width = 100.dp, height = 40.dp),
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
            )
            {
                Button(
                    onClick = { ctrl_state.value = "off" },
                    modifier = Modifier.size(width = 100.dp, height = 40.dp),
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
            )
            {
                Button(
                    onClick = { ctrl_state.value = "auto" },
                    modifier = Modifier.size(width = 100.dp, height = 40.dp),
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
fun Greeting(name: String) {
    val min_temp = remember { mutableStateOf("") }
    Surface(
        color = Color.Black,
        modifier = Modifier.padding(horizontal = 0.dp)
    )
    {
        Row(modifier = Modifier.padding(top = 40.dp, end = 20.dp, start = 20.dp)) {
            Column(
                modifier = Modifier
                    .weight(2f)
                    .align(Alignment.CenterVertically)
            )
            {
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
            )
            {
                OutlinedTextField(
                    value = min_temp.value,
                    textStyle = TextStyle(fontSize = 18.sp, color = Color.Black),
                    onValueChange = { newText -> min_temp.value = newText },
                    modifier = Modifier
                        .background(Color.White)
                        .width(80.dp)
                        .height(50.dp)
                )
            }
        }
    }
}

@Composable
fun Footer() {
    Surface(
        color = Color.Black,
        //  modifier = Modifier.padding(horizontal = 2.dp)
    )
    {
        Row(
            modifier = Modifier
                .padding(vertical = 50.dp)
                .fillMaxWidth(1f)
                .fillMaxHeight(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom
        )
        {
            Column(
                modifier = Modifier.weight(2f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            )
            {
                Button(

                    onClick = { },
                    modifier = Modifier.size(width = 300.dp, height = 50.dp),

                    colors = ButtonDefaults.buttonColors(
                        contentColor = Color(0xff004D40),
                        containerColor = Color(0xFF558B2F)
                    )
                ) { Text("Применить", fontSize = 28.sp) }
            }


        }
    }

}

@Preview(showBackground = true, widthDp = 640)
@Composable

fun Mypreview() {


}

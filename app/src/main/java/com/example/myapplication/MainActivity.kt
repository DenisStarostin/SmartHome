package com.example.myapplication

//import androidx.compose.foundation.layout.FlowColumnScopeInstance.align

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
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
                Main(this)
            }
        }
    }

}

@Composable
fun Main(context: Context) {
    val navController = rememberNavController()
    // var currentRoute by remember { mutableStateOf(navController.currentBackStackEntry?.destination?.route?:"") }
    val bluetoochapp = BluetoothApp(context)
    bluetoochapp.readDevice()
    val uiThermostat = thermostatUI(bluetoochapp, context, navController)
    Surface(color = Color.Black) {
        NavHost(navController, startDestination = NavRoutes.Home.route) {
            composable(NavRoutes.Home.route) { Home(bluetoochapp, navController) }
            composable(NavRoutes.Macro.route) { Macro() }
            composable(NavRoutes.Setting.route) { Setting() }
            composable(NavRoutes.Thermostat.route) {
                uiThermostat.Thermostat(
                    bluetoochapp,
                    context,
                    navController
                )
            }
            composable(NavRoutes.AddDevice.route) {
                AddDevice(
                    context,
                    bluetoochapp,
                    navController
                )
            }
        }
    }
}

@Composable
fun Home(bluetoothApp: BluetoothApp, navController: NavController)
{

    Surface(color = Color.Black) { Row() { Column { MainMenu(navController) } } }
    BluetoothDeviceList(
        onDeviceClick = { device ->
            bluetoothApp.setCurrentDevice(device)
            when (device.deviceType) {
                "Thermostat" -> navController.navigate(NavRoutes.Thermostat.route)
            }
        }, bluetoothApp.storedDevices
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
fun AddDevice(context: Context, bluetoothApp: BluetoothApp, navController: NavController) {
    val handler = Handler(Looper.getMainLooper())
    var updater by remember { mutableStateOf(false) }
    Surface(color = Color.Black) { Row() { Column { MainMenu(navController) } } }
    bluetoothApp.checkBluetoothStatus()
    bluetoothApp.startBleScan()
    handler.postDelayed({ updater = true }, 12000)
    if (updater) BluetoothDeviceList(

        onDeviceClick = { device ->
            CoroutineScope(Dispatchers.Main).launch {
                val result = onClick(context, device)
                if (result) {
                    bluetoothApp.addDevice(device)
                }
            }
        }, bluetoothApp.devices
    )
}

@Composable
fun BluetoothDeviceList(onDeviceClick: (BluetouchDevice) -> Unit, list: List<BluetouchDevice>) {
    var devices = list

    Row(modifier = Modifier.padding(top = 180.dp)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)
        ) {
            items(devices.size) { index ->

                BluetoothDeviceItem(
                    devices[index], onClick = onDeviceClick
                )

            }
        }
    }


}

fun <LazyItemScope> items(
    count: List<BluetouchDevice>, itemContent: @Composable LazyItemScope.(index: Int) -> Unit
) {

}

@Composable
fun BluetoothDeviceItem(
    device: BluetouchDevice, onClick: (BluetouchDevice) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick(device) },
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = device.name ?: "Unknown Device", style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = device.addres ?: "Unknown Address",
                style = MaterialTheme.typography.bodySmall
            )

        }
    }

}

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun onClick(context: Context, device: BluetouchDevice): Boolean {
    return suspendCancellableCoroutine { continuation ->
        val dialog = AlertDialog.Builder(context).setTitle("Подключение к ${device.name}")
            .setMessage("Вы уверены, что хотите добавить ${device.name} в список устройств?")
            .setPositiveButton("Подключиться") { _, _ ->
                println("Подключиться к ${device.name}")

                continuation.resume(true, null)

            }.setNegativeButton("Отмена") { _, _ ->
                println("Отмена подключения к ${device.name}")
                continuation.resume(false, null)
            }.create()
        dialog.show()
        continuation.invokeOnCancellation {
            dialog.dismiss()
        }
    }

}


@Composable
fun MainMenu(navController: NavController) {
    var expanded by remember { mutableStateOf(false) }
    var update by remember { mutableStateOf(false) }

    var currentRoute by remember {
        mutableStateOf(
            navController.currentBackStackEntry?.destination?.route ?: ""
        )
    }
    val nameHome = "Устройства"
    val nameSetting = "Настройки"
    val nameMacro = "Макросы"
    val nameAdd = "Добавить устройство"
    val nameThermostat = "Термостат"
    var name by remember { mutableStateOf(nameHome) }
    when (currentRoute) {
        "home" -> name = nameHome
        "macro" -> name = nameMacro
        "setting" -> name = nameSetting
        "adddevice" -> name = nameAdd
        "thermostat" -> name = nameThermostat
    }

    BackHandler(enabled = true) {
        update = true
    }
    Surface(
        color = Color.Black, modifier = Modifier
            .padding(top = 0.dp)
            .fillMaxWidth(1f)
    ) {
        Box(modifier = Modifier.padding(top = 40.dp, start = 10.dp)) {
            IconButton(
                onClick = { expanded = true },
                colors = IconButtonDefaults.iconButtonColors(Color.LightGray)
            ) {
                Icon(Icons.Default.Menu, contentDescription = "Показать меню")
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(onClick = {
                    navController.navigate(NavRoutes.Home.route)
                    name = nameHome
                }, text = { Text(nameHome) })
                DropdownMenuItem(onClick = {
                    navController.navigate(NavRoutes.Macro.route)
                    name = nameMacro
                }, text = { Text(nameMacro) })
                DropdownMenuItem(onClick = {
                    navController.navigate(NavRoutes.Setting.route)
                    name = nameSetting
                }, text = { Text(nameSetting) })
                HorizontalDivider()
                DropdownMenuItem(onClick = {
                    navController.navigate(NavRoutes.AddDevice.route)
                    name = nameAdd
                }, text = { Text(nameAdd) })
            }
            Text(name, Modifier.align(Alignment.Center), color = Color.LightGray, fontSize = 28.sp)
        }
        Row(
            modifier = Modifier.padding(top = 90.dp, end = 10.dp),
            horizontalArrangement = Arrangement.Absolute.Right,
            verticalAlignment = Alignment.Bottom,
        ) {
            when (currentRoute) {
                "home" -> IconButton(
                    onClick = {
                        navController.navigate(NavRoutes.AddDevice.route)
                        name = "Добавить устройство"
                    },
                    colors = IconButtonDefaults.iconButtonColors(Color.Green)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить устройство")
                }

                "thermostat" -> IconButton(
                    onClick = {
                        navController.navigate(NavRoutes.AddDevice.route)
                        name = "Добавить устройство"
                    },
                    colors = IconButtonDefaults.iconButtonColors(Color.Gray)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Настройки")
                }
            }

        }
        //   }
    }

}


@Preview(showBackground = true, widthDp = 640)
@Composable

fun Mypreview() {


}

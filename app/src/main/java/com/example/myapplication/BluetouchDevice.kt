package com.example.myapplication

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "BleDevices")
data class BluetouchDevice(
  @PrimaryKey(autoGenerate = true) var id: Int = 0,
    val name: String? = "Uknown",
    val addres: String? = "Uknown",
    var uuid: String? = "",

) {

  val deviceType="Thermostat"//придумать как получить тип автоматически например парсить из названия найденого устройства

  var isConneted = false

  fun setConnected ()
  {
    isConneted=true
  }

  fun setDisconnected()
  {
    isConneted=false
  }
}

package com.example.myapplication

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "BleDevices")
data class BluetouchDevice(
  @PrimaryKey(autoGenerate = true) var id: Int = 0,
    val name: String? = "Uknown",
    val addres: String? = "Uknown",
    var uuid: String? = "",
  var appName: String? = name,
) {


}

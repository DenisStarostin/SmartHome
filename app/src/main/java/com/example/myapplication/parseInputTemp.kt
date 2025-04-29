package com.example.myapplication

import android.content.Context


class parseInputTemp(inputValue:String, context: Context) {

    var errorMessage : String = ""
    var temp : Byte = 0x00
    private val str = inputValue

    fun Handler():Byte
    {
        try {
             temp = str.trim().toByte()
        }
        catch (e:NumberFormatException)
        {
            errorMessage="Пожалуйста вводите только цифры"

            return -1
        }
        if(temp !in 0..23)
        {
            errorMessage ="Диапазон температур должен быть от 0 до 23 "
            return -1
        }
        if (temp<0)
        {
            errorMessage ="Вводимое значение должно быть положительным "
            return -1
        }
        if (temp==null) {
            errorMessage ="Значение = null "
            return -1
        }
        return  temp
    }




}
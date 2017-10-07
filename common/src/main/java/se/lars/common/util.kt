package se.lars.common

import java.util.*

fun delay(from: Int = 100, to: Int = 500)  {
    val random  = Random()
   
    Thread.sleep((random.nextInt(to-from) + from).toLong())
}
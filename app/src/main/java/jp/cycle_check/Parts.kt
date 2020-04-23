package jp.cycle_check

import java.io.Serializable

class Parts(val frame: String, val chain: String, val frderailler: String, val rearderailler: String,val sprocket:String,val frbrake:String,val rearbrake:String, val wheel:String, val tire:String,val sti:String,val chainring:String,val saddle:String) :
    Serializable
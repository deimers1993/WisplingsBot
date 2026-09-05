
package com.wisplings.autoclicker
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
class MainActivity : AppCompatActivity() {
    lateinit var etH: EditText; lateinit var etM: EditText; lateinit var etS: EditText; lateinit var etRand: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        etH=findViewById(R.id.etHours); etM=findViewById(R.id.etMinutes); etS=findViewById(R.id.etSeconds); etRand=findViewById(R.id.etRandom)
        val prefs=getSharedPreferences("bot", MODE_PRIVATE)
        etH.setText(prefs.getInt("h",1).toString()); etM.setText(prefs.getInt("m",30).toString()); etS.setText(prefs.getInt("s",0).toString()); etRand.setText(prefs.getInt("rand",5).toString())
        findViewById<Button>(R.id.btnAcc).setOnClickListener{ startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        findViewById<Button>(R.id.btnOverlay).setOnClickListener{ startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))) }
        findViewById<Button>(R.id.chip15m).setOnClickListener{ setTime(0,15,0) }
        findViewById<Button>(R.id.chip30m).setOnClickListener{ setTime(0,30,0) }
        findViewById<Button>(R.id.chip1h).setOnClickListener{ setTime(1,0,0) }
        findViewById<Button>(R.id.chip6h).setOnClickListener{ setTime(6,0,0) }
        findViewById<Button>(R.id.btnStart).setOnClickListener{
            val h=etH.text.toString().toIntOrNull()?:1; val m=etM.text.toString().toIntOrNull()?:0; val s=etS.text.toString().toIntOrNull()?:0; val rand=etRand.text.toString().toIntOrNull()?:5
            val total=h*3600+m*60+s
            if(total<60){ Toast.makeText(this,"Min 1 min",Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            prefs.edit().putInt("h",h).putInt("m",m).putInt("s",s).putInt("rand",rand).putBoolean("scroll", findViewById<Switch>(R.id.switchScroll).isChecked).putBoolean("ahorro", findViewById<Switch>(R.id.switchAhorro).isChecked).apply()
            val intent=Intent(this,WisplingsAutoService::class.java); intent.action="START_BOT"; intent.putExtra("interval",total.toLong()); startService(intent)
            findViewById<TextView>(R.id.tvNext).text="Bot cada ${h}h ${m}m - Scroll ON - Ahorro ON"
        }
        findViewById<Button>(R.id.btnStop).setOnClickListener{
            val intent=Intent(this,WisplingsAutoService::class.java); intent.action="STOP_BOT"; startService(intent)
            findViewById<TextView>(R.id.tvNext).text="Detenido"
        }
    }
    private fun setTime(h:Int,m:Int,s:Int){ etH.setText(h.toString()); etM.setText(m.toString()); etS.setText(s.toString()) }
}

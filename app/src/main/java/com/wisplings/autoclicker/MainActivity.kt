
package com.wisplings.autoclicker
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
class MainActivity : AppCompatActivity() {
    lateinit var etH: EditText; lateinit var etM: EditText; lateinit var etS: EditText; lateinit var etRand: EditText; lateinit var tvStatus: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        etH=findViewById(R.id.etHours); etM=findViewById(R.id.etMinutes); etS=findViewById(R.id.etSeconds); etRand=findViewById(R.id.etRandom)
        tvStatus=findViewById(R.id.tvNext)
        val prefs=getSharedPreferences("bot", MODE_PRIVATE)
        etH.setText(prefs.getInt("h",1).toString()); etM.setText(prefs.getInt("m",30).toString()); etS.setText(prefs.getInt("s",0).toString()); etRand.setText(prefs.getInt("rand",5).toString())
        findViewById<Button>(R.id.btnAcc).setOnClickListener{ startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)); Toast.makeText(this,"Activa Wisplings Bot",Toast.LENGTH_LONG).show() }
        findViewById<Button>(R.id.btnOverlay).setOnClickListener{ startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))) }
        findViewById<Button>(R.id.chip15m).setOnClickListener{ setTime(0,15,0) }
        findViewById<Button>(R.id.chip30m).setOnClickListener{ setTime(0,30,0) }
        findViewById<Button>(R.id.chip1h).setOnClickListener{ setTime(1,0,0) }
        findViewById<Button>(R.id.chip6h).setOnClickListener{ setTime(6,0,0) }
        findViewById<Button>(R.id.btnStart).setOnClickListener{
            if(!isAccessibilityEnabled()){
                AlertDialog.Builder(this).setTitle("Permiso necesario").setMessage("Activa Accesibilidad para Wisplings Bot").setPositiveButton("Ir"){_,_-> startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))}.show()
                tvStatus.text="❌ Activa Accesibilidad primero (botón 1)"; return@setOnClickListener
            }
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)){
                AlertDialog.Builder(this).setTitle("Permiso necesario").setMessage("Activa Superposición").setPositiveButton("Ir"){_,_-> startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))}.show()
                tvStatus.text="❌ Activa Superposición (botón 2)"; return@setOnClickListener
            }
            if(Build.VERSION.SDK_INT >= 33){ ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001) }
            val h=etH.text.toString().toIntOrNull()?:1; val m=etM.text.toString().toIntOrNull()?:0; val s=etS.text.toString().toIntOrNull()?:0; val rand=etRand.text.toString().toIntOrNull()?:5
            val total=h*3600+m*60+s
            if(total<60){ Toast.makeText(this,"Mínimo 1 minuto",Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            prefs.edit().putInt("h",h).putInt("m",m).putInt("s",s).putInt("rand",rand).apply()
            val intent=Intent(this,WisplingsAutoService::class.java); intent.action="START_BOT"; intent.putExtra("interval",total.toLong())
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){ startForegroundService(intent) } else { startService(intent) }
            tvStatus.text="✅ Bot iniciado cada ${h}h ${m}m - Abre Telegram ahora"
            Toast.makeText(this,"✅ Bot iniciado!",Toast.LENGTH_LONG).show()
        }
        findViewById<Button>(R.id.btnStop).setOnClickListener{
            val intent=Intent(this,WisplingsAutoService::class.java); intent.action="STOP_BOT"; startService(intent); tvStatus.text="🛑 Detenido"
        }
    }
    private fun isAccessibilityEnabled(): Boolean {
        val expected = "$packageName/${packageName}.WisplingsAutoService"
        val enabled = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
        val colonSplitter = TextUtils.SimpleStringSplitter(':'); colonSplitter.setString(enabled)
        while(colonSplitter.hasNext()){ if(colonSplitter.next().equals(expected, ignoreCase=true)) return true }
        return false
    }
    private fun setTime(h:Int,m:Int,s:Int){ etH.setText(h.toString()); etM.setText(m.toString()); etS.setText(s.toString()) }
}


package com.wisplings.autoclicker
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
class MainActivity : AppCompatActivity() {
    lateinit var etHoras: EditText
    lateinit var etMin: EditText
    lateinit var etSeg: EditText
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        etHoras = findViewById(R.id.etHoras)
        etMin = findViewById(R.id.etMin)
        etSeg = findViewById(R.id.etSeg)
        
        val prefs = getSharedPreferences("bot", MODE_PRIVATE)
        etHoras.setText(prefs.getInt("h", 1).toString())
        etMin.setText(prefs.getInt("m", 30).toString())
        etSeg.setText(prefs.getInt("s", 0).toString())
        
        findViewById<Button>(R.id.btnAcc).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        
        findViewById<Button>(R.id.chip15m).setOnClickListener { setTime(0, 15, 0) }
        findViewById<Button>(R.id.chip30m).setOnClickListener { setTime(0, 30, 0) }
        findViewById<Button>(R.id.chip1h).setOnClickListener { setTime(1, 0, 0) }
        findViewById<Button>(R.id.chip6h).setOnClickListener { setTime(6, 0, 0) }
        
        findViewById<Button>(R.id.btnStart).setOnClickListener {
            val h = etHoras.text.toString().toIntOrNull() ?: 1
            val m = etMin.text.toString().toIntOrNull() ?: 0
            val s = etSeg.text.toString().toIntOrNull() ?: 0
            val total = h * 3600 + m * 60 + s
            
            if (total < 60) {
                Toast.makeText(this, "Min 1 min", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            prefs.edit()
                .putInt("h", h)
                .putInt("m", m)
                .putInt("s", s)
                .putBoolean("scroll", findViewById<Switch>(R.id.switchScroll).isChecked)
                .putBoolean("ahorro", findViewById<Switch>(R.id.switchAhorro).isChecked)
                .apply()
            
            val intent = Intent(this, WisplingsAutoService::class.java)
            intent.action = "START_BOT"
            intent.putExtra("interval", total.toLong())
            startService(intent)
            
            findViewById<TextView>(R.id.tvNext).text = "Bot cada ${h}h ${m}m - Scroll ON - Ahorro ON"
        }
        
        findViewById<Button>(R.id.btnStop).setOnClickListener {
            val intent = Intent(this, WisplingsAutoService::class.java)
            intent.action = "STOP_BOT"
            startService(intent)
            findViewById<TextView>(R.id.tvNext).text = "Detenido"
        }
    }
    
    private fun setTime(h: Int, m: Int, s: Int) {
        etHoras.setText(h.toString())
        etMin.setText(m.toString())
        etSeg.setText(s.toString())
    }
}

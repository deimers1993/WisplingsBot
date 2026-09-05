
package com.wisplings.autoclicker
import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.*
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import kotlin.random.Random
class WisplingsAutoService : AccessibilityService() {
    private var handler=Handler(Looper.getMainLooper()); private var runnable:Runnable?=null; private var intervalSec:Long=3600; private var randomMin=5; private var isRunning=false
    private var lastPositions = mutableMapOf<String, Pair<Int,Int>>()
    override fun onCreate(){ super.onCreate(); createNotificationChannel() }
    private fun createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel("wisplings_bot", "Wisplings Bot", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
    private fun getNotification(text:String): Notification {
        val builder = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){ Notification.Builder(this, "wisplings_bot") } else { Notification.Builder(this) }
        builder.setContentTitle("Wisplings Bot v7").setContentText(text).setSmallIcon(android.R.drawable.ic_media_play).setOngoing(true)
        return builder.build()
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action){
            "START_BOT" -> { 
                intervalSec=intent.getLongExtra("interval",3600)
                val p=getSharedPreferences("bot", MODE_PRIVATE); randomMin=p.getInt("rand",5)
                startForeground(1, getNotification("Bot cada ${intervalSec/60} min"))
                startBot() 
            }
            "STOP_BOT" -> { stopBot(); stopForeground(true); stopSelf() }
        }
        return START_STICKY
    }
    private fun startBot(){ 
        if(isRunning) return; isRunning=true
        Toast.makeText(this,"✅ Bot v7 iniciado",Toast.LENGTH_LONG).show()
        handler.post{ runCycle() }; scheduleNext()
    }
    private fun stopBot(){ isRunning=false; runnable?.let{ handler.removeCallbacks(it) } }
    private fun scheduleNext(){
        val delay= run { val off=Random.nextInt(-randomMin*60, randomMin*60+1); (intervalSec*1000 + off*1000).coerceAtLeast(60*1000) }
        runnable=Runnable{ if(isRunning){ runCycle(); scheduleNext() } }; handler.postDelayed(runnable!!, delay)
    }
    private fun runCycle(){
        try{
            if(!clickHamburger()) Thread.sleep(500)
            Thread.sleep(800)
            clickConScroll("club", listOf("club del guardian","club del guardián"), 0.20f, 0.52f)
            Thread.sleep(700)
            clickConScroll("cuidado", listOf("cuidado inteligente"), 0.60f, 0.50f)
            Thread.sleep(700)
            clickConScroll("confirmar", listOf("confirmar y usar objetos propios","confirmar"), 0.66f, 0.78f)
            Thread.sleep(800)
            clickConScroll("inicio", listOf("inicio"), 0.10f, 0.93f, true)
            Thread.sleep(800)
            clickConScroll("wisp", listOf("recoger wisp","recoger"), 0.50f, 0.52f, true)
        }catch(e:Exception){}
    }
    private fun clickConScroll(id:String, texts:List<String>, px:Float, py:Float, necesitaScroll:Boolean=false){
        lastPositions[id]?.let{ clickAt(it.first, it.second); return }
        val root = rootInActiveWindow
        if(root!=null){
            for(t in texts){
                val node=findNodeByText(root,t)
                if(node!=null){
                    val r=android.graphics.Rect(); node.getBoundsInScreen(r)
                    if((r.top > resources.displayMetrics.heightPixels*0.9) && necesitaScroll){
                        performScroll(true); Thread.sleep(500)
                        val node2 = findNodeByText(rootInActiveWindow ?: root, t)
                        if(node2!=null){ node2.performAction(AccessibilityNodeInfo.ACTION_CLICK); val r2=android.graphics.Rect(); node2.getBoundsInScreen(r2); lastPositions[id]=Pair(r2.centerX(), r2.centerY()); return }
                    } else {
                        node.performAction(AccessibilityNodeInfo.ACTION_CLICK); lastPositions[id]=Pair(r.centerX(), r.centerY()); return
                    }
                }
            }
        }
        if(necesitaScroll){ performScroll(true); Thread.sleep(400) }
        clickAtPercent(px,py)
    }
    private fun performScroll(arriba:Boolean){
        try{
            val dm=resources.displayMetrics; val path=android.graphics.Path()
            if(arriba){ path.moveTo(dm.widthPixels*0.5f, dm.heightPixels*0.75f); path.lineTo(dm.widthPixels*0.5f, dm.heightPixels*0.35f) }
            else { path.moveTo(dm.widthPixels*0.5f, dm.heightPixels*0.35f); path.lineTo(dm.widthPixels*0.5f, dm.heightPixels*0.75f) }
            val gesture=android.accessibilityservice.GestureDescription.Builder().addStroke(android.accessibilityservice.GestureDescription.StrokeDescription(path,0,350)).build()
            dispatchGesture(gesture,null,null)
        }catch(e:Exception){}
    }
    private fun clickHamburger():Boolean{
        val root=rootInActiveWindow ?: return false
        lastPositions["hamburger"]?.let{ clickAt(it.first,it.second); return true }
        val dw=resources.displayMetrics.widthPixels; val dh=resources.displayMetrics.heightPixels
        val q=ArrayDeque<AccessibilityNodeInfo>(); q.add(root)
        while(q.isNotEmpty()){
            val n=q.removeFirst()
            if(n.isClickable){ val r=android.graphics.Rect(); n.getBoundsInScreen(r); if(r.left < dw*0.3 && r.top < dh*0.20){ n.performAction(AccessibilityNodeInfo.ACTION_CLICK); lastPositions["hamburger"]=Pair(r.centerX(), r.centerY()); return true } }
            for(i in 0 until n.childCount){ n.getChild(i)?.let{ q.add(it) } }
        }
        return clickAtPercent(0.11f,0.14f)
    }
    private fun findNodeByText(root:AccessibilityNodeInfo, query:String):AccessibilityNodeInfo?{
        val q=query.lowercase().replace("á","a").replace("é","e").replace("í","i").replace("ó","o").replace("ú","u")
        val queue=ArrayDeque<AccessibilityNodeInfo>(); queue.add(root)
        while(queue.isNotEmpty()){
            val n=queue.removeFirst(); val txt=(n.text?.toString() ?: n.contentDescription?.toString() ?: "").lowercase().replace("á","a").replace("é","e").replace("í","i").replace("ó","o").replace("ú","u")
            if(txt.contains(q)) return n
            for(i in 0 until n.childCount){ n.getChild(i)?.let{ queue.add(it) } }
        }
        return null
    }
    private fun clickAt(x:Int,y:Int):Boolean{ return try{ val path=android.graphics.Path(); path.moveTo(x.toFloat()+Random.nextInt(-5,5), y.toFloat()+Random.nextInt(-5,5)); val g=android.accessibilityservice.GestureDescription.Builder().addStroke(android.accessibilityservice.GestureDescription.StrokeDescription(path,0,80)).build(); dispatchGesture(g,null,null); true }catch(e:Exception){ false } }
    private fun clickAtPercent(px:Float,py:Float):Boolean{ val dm=resources.displayMetrics; return clickAt((dm.widthPixels*px).toInt()+Random.nextInt(-6,6), (dm.heightPixels*py).toInt()+Random.nextInt(-6,6)) }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
}

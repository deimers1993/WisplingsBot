
package com.wisplings.autoclicker
import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.*
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import kotlin.random.Random
class WisplingsAutoService : AccessibilityService() {
    private var handler=Handler(Looper.getMainLooper()); private var runnable:Runnable?=null; private var intervalSec:Long=3600; private var useRandom=true; private var randomMin=5; private var scrollEnabled=true; private var isRunning=false
    // Cache para optimizar: guarda última posición exitosa
    private var lastPositions = mutableMapOf<String, Pair<Int,Int>>()
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action){
            "START_BOT" -> { intervalSec=intent.getLongExtra("interval",3600); val p=getSharedPreferences("bot", MODE_PRIVATE); useRandom=p.getBoolean("useRandom",true); randomMin=p.getInt("rand",5); scrollEnabled=p.getBoolean("scroll",true); startBot() }
            "STOP_BOT" -> stopBot()
        }
        return START_STICKY
    }
    private fun startBot(){ if(isRunning) return; isRunning=true; Toast.makeText(this,"Bot v6 OPTIMIZADA cada ${intervalSec/60} min - Scroll ON",Toast.LENGTH_LONG).show(); handler.post{ runCycleOptimized() }; scheduleNext() }
    private fun stopBot(){ isRunning=false; runnable?.let{ handler.removeCallbacks(it) }; Toast.makeText(this,"Bot detenido",Toast.LENGTH_SHORT).show() }
    private fun scheduleNext(){
        val delay=if(useRandom){ val off=Random.nextInt(-randomMin*60, randomMin*60+1); (intervalSec*1000 + off*1000).coerceAtLeast(60*1000) } else intervalSec*1000
        runnable=Runnable{ if(isRunning){ runCycleOptimized(); scheduleNext() } }; handler.postDelayed(runnable!!, delay)
    }
    private fun runCycleOptimized(){
        try{
            // OPTIMIZADO: Intenta cache primero para hamburguesa
            if(!clickHamburgerOptimized()) Thread.sleep(Random.nextLong(500,900))
            Thread.sleep(Random.nextLong(800,1200))
            clickConScroll("club", listOf("club del guardian","club del guardián"), 0.20f, 0.52f)
            Thread.sleep(Random.nextLong(700,1100))
            clickConScroll("cuidado", listOf("cuidado inteligente"), 0.60f, 0.50f)
            Thread.sleep(Random.nextLong(700,1100))
            clickConScroll("confirmar", listOf("confirmar y usar objetos propios","usar objetos propios","confirmar"), 0.66f, 0.78f)
            Thread.sleep(Random.nextLong(800,1200))
            clickConScroll("inicio", listOf("inicio"), 0.10f, 0.93f, necesitaScroll=true)
            Thread.sleep(Random.nextLong(800,1200))
            clickConScroll("wisp", listOf("recoger wisp","recoger"), 0.50f, 0.52f, necesitaScroll=true)
        }catch(e:Exception){}
    }
    private fun clickConScroll(id:String, texts:List<String>, px:Float, py:Float, necesitaScroll:Boolean=false){
        // Intento 1: Cache
        lastPositions[id]?.let{
            // Click rápido en cache
            clickAt(it.first, it.second)
            return
        }
        // Intento 2: Texto
        val root = rootInActiveWindow
        if(root!=null){
            for(t in texts){
                val node=findNodeByText(root,t)
                if(node!=null){
                    val r=android.graphics.Rect(); node.getBoundsInScreen(r)
                    // Si está fuera de pantalla y necesita scroll, hace scroll
                    if((r.top > resources.displayMetrics.heightPixels*0.9 || r.bottom < 0) && necesitaScroll && scrollEnabled){
                        // Scroll inteligente para botón muy abajo
                        performScroll(true)
                        Thread.sleep(600)
                        val node2 = findNodeByText(rootInActiveWindow ?: root, t)
                        if(node2!=null){
                            val r2=android.graphics.Rect(); node2.getBoundsInScreen(r2)
                            node2.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            lastPositions[id]=Pair(r2.centerX(), r2.centerY())
                            return
                        }
                    } else {
                        node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        lastPositions[id]=Pair(r.centerX(), r.centerY())
                        return
                    }
                }
            }
        }
        // Intento 3: Si no encontró y necesita scroll, scroll + porcentaje
        if(necesitaScroll && scrollEnabled){
            performScroll(true)
            Thread.sleep(500)
        }
        clickAtPercent(px,py)
        lastPositions[id]=Pair((resources.displayMetrics.widthPixels*px).toInt(), (resources.displayMetrics.heightPixels*py).toInt())
    }
    private fun performScroll(haciaArriba:Boolean){
        val dm=resources.displayMetrics
        val path=android.graphics.Path()
        if(haciaArriba){
            path.moveTo(dm.widthPixels*0.5f, dm.heightPixels*0.75f)
            path.lineTo(dm.widthPixels*0.5f, dm.heightPixels*0.35f)
        } else {
            path.moveTo(dm.widthPixels*0.5f, dm.heightPixels*0.35f)
            path.lineTo(dm.widthPixels*0.5f, dm.heightPixels*0.75f)
        }
        val gesture=android.accessibilityservice.GestureDescription.Builder().addStroke(android.accessibilityservice.GestureDescription.StrokeDescription(path,0,350)).build()
        dispatchGesture(gesture,null,null)
    }
    private fun clickHamburgerOptimized():Boolean{
        val root=rootInActiveWindow ?: return false
        // Cache
        lastPositions["hamburger"]?.let{ clickAt(it.first,it.second); return true }
        val dw=resources.displayMetrics.widthPixels; val dh=resources.displayMetrics.heightPixels
        val q=ArrayDeque<AccessibilityNodeInfo>(); q.add(root)
        while(q.isNotEmpty()){
            val n=q.removeFirst()
            if(n.isClickable){
                val r=android.graphics.Rect(); n.getBoundsInScreen(r)
                if(r.left < dw*0.3 && r.top < dh*0.20){
                    n.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    lastPositions["hamburger"]=Pair(r.centerX(), r.centerY())
                    return true
                }
            }
            for(i in 0 until n.childCount){ n.getChild(i)?.let{ q.add(it) } }
        }
        return clickAtPercent(0.11f,0.14f)
    }
    private fun findNodeByText(root:AccessibilityNodeInfo, query:String):AccessibilityNodeInfo?{
        val q=query.lowercase().replace("á","a").replace("é","e").replace("í","i").replace("ó","o").replace("ú","u")
        val queue=ArrayDeque<AccessibilityNodeInfo>(); queue.add(root)
        while(queue.isNotEmpty()){
            val n=queue.removeFirst()
            val txt=(n.text?.toString() ?: n.contentDescription?.toString() ?: "").lowercase().replace("á","a").replace("é","e").replace("í","i").replace("ó","o").replace("ú","u")
            if(txt.contains(q)) return n
            for(i in 0 until n.childCount){ n.getChild(i)?.let{ queue.add(it) } }
        }
        return null
    }
    private fun clickAt(x:Int,y:Int):Boolean{
        val path=android.graphics.Path(); path.moveTo(x.toFloat()+Random.nextInt(-6,6), y.toFloat()+Random.nextInt(-6,6))
        val gesture=android.accessibilityservice.GestureDescription.Builder().addStroke(android.accessibilityservice.GestureDescription.StrokeDescription(path,0,80)).build()
        dispatchGesture(gesture,null,null); return true
    }
    private fun clickAtPercent(px:Float,py:Float):Boolean{
        val dm=resources.displayMetrics; val x=(dm.widthPixels*px).toInt()+Random.nextInt(-8,8); val y=(dm.heightPixels*py).toInt()+Random.nextInt(-8,8)
        return clickAt(x,y)
    }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
}

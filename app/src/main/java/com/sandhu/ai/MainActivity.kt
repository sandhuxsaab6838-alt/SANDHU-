package com.sandhu.ai

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.TextToSpeech
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private lateinit var status: TextView
    private lateinit var tts: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(36, 60, 36, 36)
        }
        layout.addView(TextView(this).apply { text = "SANDHU AI"; textSize = 30f })
        status = TextView(this).apply {
            text = "Sat Sri Akal! Mic button dabao te gall karo."
            textSize = 18f
            setPadding(0, 28, 0, 28)
        }
        layout.addView(status)
        layout.addView(Button(this).apply {
            text = "🎙️ Bolo / Speak"
            setOnClickListener { startListening() }
        })
        listOf("WhatsApp", "YouTube", "Maps").forEach { app ->
            layout.addView(Button(this).apply {
                text = "Open $app"
                setOnClickListener { openApp(app) }
            })
        }
        setContentView(layout)
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 10)
        }
    }

    private fun startListening() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 10)
            return
        }
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            status.text = "Speech recognition is not available on this phone."
            return
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pa-IN")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Sandhu AI naal gall karo")
        }
        try {
            startActivityForResult(intent, 11)
        } catch (_: Exception) {
            status.text = "Speech input could not start."
        }
    }

    @Deprecated("Deprecated by Android, retained for broad device compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 11 && resultCode == RESULT_OK) {
            val said = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull().orEmpty()
            status.text = "You said: $said"
            handleCommand(said)
        }
    }

    private fun handleCommand(raw: String) {
        val text = raw.lowercase(Locale.ROOT)
        when {
            "whatsapp" in text -> openApp("WhatsApp")
            "youtube" in text -> openApp("YouTube")
            "map" in text || "maps" in text -> openApp("Maps")
            "hello" in text || "ਸਤ ਸ੍ਰੀ ਅਕਾਲ" in text || "नमस्ते" in text ->
                speak("Sat Sri Akal! Main Sandhu AI haan.")
            else -> speak("Tusi keha: $raw. Eh pehla test version hai.")
        }
    }

    private fun openApp(name: String) {
        val packageName = when (name) {
            "WhatsApp" -> "com.whatsapp"
            "YouTube" -> "com.google.android.youtube"
            else -> "com.google.android.apps.maps"
        }
        val launch = packageManager.getLaunchIntentForPackage(packageName)
        if (launch != null) startActivity(launch) else status.text = "$name app nahi mili."
    }

    private fun speak(message: String) {
        status.text = message
        tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "sandhu_reply")
    }

    override fun onInit(statusCode: Int) {
        if (statusCode == TextToSpeech.SUCCESS) tts.language = Locale("pa", "IN")
    }

    override fun onDestroy() {
        tts.shutdown()
        super.onDestroy()
    }
}

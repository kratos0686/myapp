package com.roomscanner.app.voice;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import java.util.Locale;

/**
 * Provides voice-guided scanning instructions using Text-to-Speech
 */
public class VoiceGuidanceManager {
    
    private TextToSpeech textToSpeech;
    private boolean isInitialized;
    private boolean isEnabled;
    
    public VoiceGuidanceManager(Context context) {
        this.isEnabled = true;
        this.isInitialized = false;
        
        // Initialize Text-to-Speech engine
        textToSpeech = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA || 
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // Language not supported
                    isInitialized = false;
                } else {
                    isInitialized = true;
                }
            }
        });
    }
    
    /**
     * Speak instructions to the user
     * @param instruction The instruction text to speak
     */
    public void speakInstruction(String instruction) {
        if (isInitialized && isEnabled) {
            textToSpeech.speak(instruction, TextToSpeech.QUEUE_ADD, null, null);
        }
    }
    
    /**
     * Provide guidance for starting a scan
     */
    public void guideStartScan() {
        speakInstruction("Point your camera at the wall and slowly move it from left to right. " +
                        "Keep the camera steady and maintain a consistent distance from the wall.");
    }
    
    /**
     * Provide guidance for scanning corners
     */
    public void guideCornerScan() {
        speakInstruction("Move to the corner of the room. Scan from floor to ceiling in a " +
                        "vertical motion to capture the corner details.");
    }
    
    /**
     * Provide guidance for scanning the floor
     */
    public void guideFloorScan() {
        speakInstruction("Point the camera downward at the floor. Move slowly across the " +
                        "entire floor area in a grid pattern.");
    }
    
    /**
     * Provide guidance for scanning the ceiling
     */
    public void guideCeilingScan() {
        speakInstruction("Point the camera upward at the ceiling. Move slowly to capture " +
                        "the entire ceiling surface.");
    }
    
    /**
     * Notify user of scan progress
     * @param percentage Completion percentage
     */
    public void announceProgress(int percentage) {
        if (percentage % 25 == 0) { // Announce at 25%, 50%, 75%, 100%
            speakInstruction("Scan is " + percentage + " percent complete.");
        }
    }
    
    /**
     * Notify user of scan completion
     */
    public void announceCompletion() {
        speakInstruction("Scan complete. Processing data now.");
    }
    
    /**
     * Warn user about scanning issues
     * @param issue The issue to warn about
     */
    public void warnIssue(String issue) {
        speakInstruction("Warning: " + issue);
    }
    
    /**
     * Stop speaking immediately
     */
    public void stopSpeaking() {
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
    }
    
    /**
     * Enable or disable voice guidance
     * @param enabled True to enable, false to disable
     */
    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }
    
    public boolean isEnabled() {
        return isEnabled && isInitialized;
    }
    
    /**
     * Clean up resources
     */
    public void shutdown() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}

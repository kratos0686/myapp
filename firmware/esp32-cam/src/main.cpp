#include <Arduino.h>
#include <WiFi.h>
#include "esp_camera.h"
#include "esp_http_server.h"
#include "DFRobot_AXP313A.h"
#include <Preferences.h>
#include "version.h"
#include "ota.h"

DFRobot_AXP313A axp;

// FireBeetle 2 ESP32-S3 camera pins (DFRobot wiki, DFR0975)
#define XCLK_GPIO_NUM  45
#define SIOD_GPIO_NUM   1
#define SIOC_GPIO_NUM   2
#define VSYNC_GPIO_NUM  6
#define HREF_GPIO_NUM  42
#define PCLK_GPIO_NUM   5
#define Y2_GPIO_NUM    39
#define Y3_GPIO_NUM    40
#define Y4_GPIO_NUM    41
#define Y5_GPIO_NUM     4
#define Y6_GPIO_NUM     7
#define Y7_GPIO_NUM     8
#define Y8_GPIO_NUM    46
#define Y9_GPIO_NUM    48

static esp_err_t stream_handler(httpd_req_t *req) {
  httpd_resp_set_type(req, "multipart/x-mixed-replace;boundary=frame");
  char hdr[64];
  while (true) {
    camera_fb_t *fb = esp_camera_fb_get();
    if (!fb) return ESP_FAIL;
    size_t n = snprintf(hdr, sizeof hdr,
      "\r\n--frame\r\nContent-Type: image/jpeg\r\nContent-Length: %u\r\n\r\n", (unsigned)fb->len);
    esp_err_t r = httpd_resp_send_chunk(req, hdr, n);
    if (r == ESP_OK) r = httpd_resp_send_chunk(req, (const char *)fb->buf, fb->len);
    esp_camera_fb_return(fb);
    if (r != ESP_OK) break;
  }
  return ESP_OK;
}

static esp_err_t photo_handler(httpd_req_t *req) {
  camera_fb_t *fb = esp_camera_fb_get();
  if (!fb) return httpd_resp_send_500(req);
  httpd_resp_set_type(req, "image/jpeg");
  esp_err_t r = httpd_resp_send(req, (const char *)fb->buf, fb->len);
  esp_camera_fb_return(fb);
  return r;
}

static Preferences prefs;
static const uint32_t OTA_INTERVAL_MS = 6UL * 3600UL * 1000UL;
static uint32_t lastOtaCheck = 0;

// Wi-Fi credentials live in NVS (survive OTA), set once over Serial:  wifi SSID;PASSWORD
static bool loadWifi(String &ssid, String &pass) {
  prefs.begin("wifi", true);
  ssid = prefs.getString("ssid", "");
  pass = prefs.getString("pass", "");
  prefs.end();
  return ssid.length() > 0;
}

static void handleSerialCommand() {
  if (!Serial.available()) return;
  String line = Serial.readStringUntil('\n');
  line.trim();
  if (!line.startsWith("wifi ")) return;
  int sep = line.indexOf(';');
  String ssid = line.substring(5, sep < 0 ? line.length() : sep);
  String pass = sep < 0 ? "" : line.substring(sep + 1);
  prefs.begin("wifi", false);
  prefs.putString("ssid", ssid);
  prefs.putString("pass", pass);
  prefs.end();
  Serial.println("Saved. Rebooting...");
  delay(300);
  ESP.restart();
}

void setup() {
  Serial.begin(115200);
  delay(1500);
  Serial.printf("Firmware %s\n", FW_VERSION);

  // V1.0 hardware: camera rails are gated by the AXP313A
  while (axp.begin() != 0) { Serial.println("AXP313A init error, retrying"); delay(1000); }
  axp.enableCameraPower(axp.eOV2640);
  delay(100);

  camera_config_t c = {};
  c.ledc_channel = LEDC_CHANNEL_0;  c.ledc_timer = LEDC_TIMER_0;
  c.pin_d0 = Y2_GPIO_NUM;  c.pin_d1 = Y3_GPIO_NUM;  c.pin_d2 = Y4_GPIO_NUM;  c.pin_d3 = Y5_GPIO_NUM;
  c.pin_d4 = Y6_GPIO_NUM;  c.pin_d5 = Y7_GPIO_NUM;  c.pin_d6 = Y8_GPIO_NUM;  c.pin_d7 = Y9_GPIO_NUM;
  c.pin_xclk = XCLK_GPIO_NUM;  c.pin_pclk = PCLK_GPIO_NUM;
  c.pin_vsync = VSYNC_GPIO_NUM;  c.pin_href = HREF_GPIO_NUM;
  c.pin_sccb_sda = SIOD_GPIO_NUM;  c.pin_sccb_scl = SIOC_GPIO_NUM;
  c.pin_pwdn = -1;  c.pin_reset = -1;
  c.xclk_freq_hz = 20000000;
  c.pixel_format = PIXFORMAT_JPEG;
  c.frame_size   = FRAMESIZE_VGA;
  c.jpeg_quality = 12;
  c.fb_count     = 2;
  c.fb_location  = CAMERA_FB_IN_PSRAM;
  c.grab_mode    = CAMERA_GRAB_LATEST;

  esp_err_t err = esp_camera_init(&c);
  if (err != ESP_OK) { Serial.printf("Camera init failed: 0x%x\n", err); return; }

  String ssid, pass;
  while (!loadWifi(ssid, pass)) {
    Serial.println("No Wi-Fi saved. Type:  wifi SSID;PASSWORD");
    for (int i = 0; i < 50; i++) { handleSerialCommand(); delay(100); }
  }
  WiFi.begin(ssid.c_str(), pass.c_str());
  while (WiFi.status() != WL_CONNECTED) { delay(300); Serial.print("."); handleSerialCommand(); }
  String ip = WiFi.localIP().toString();
  Serial.printf("\nhttp://%s/stream   http://%s/photo\n", ip.c_str(), ip.c_str());

  httpd_config_t cfg = HTTPD_DEFAULT_CONFIG();
  httpd_handle_t srv = nullptr;
  if (httpd_start(&srv, &cfg) == ESP_OK) {
    httpd_uri_t s = {"/stream", HTTP_GET, stream_handler, nullptr};
    httpd_uri_t p = {"/photo",  HTTP_GET, photo_handler,  nullptr};
    httpd_register_uri_handler(srv, &s);
    httpd_register_uri_handler(srv, &p);
  }

  otaCheckAndUpdate();
  lastOtaCheck = millis();
}

void loop() {
  handleSerialCommand();
  if (WiFi.status() == WL_CONNECTED && millis() - lastOtaCheck > OTA_INTERVAL_MS) {
    otaCheckAndUpdate();
    lastOtaCheck = millis();
  }
  delay(200);
}

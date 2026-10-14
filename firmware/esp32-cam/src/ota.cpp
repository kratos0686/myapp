#include "ota.h"
#include <Arduino.h>
#include <ArduinoJson.h>
#include <HTTPClient.h>
#include <HTTPUpdate.h>
#include <WiFiClientSecure.h>
#include "version.h"

#define OTA_GH_USER   "kratos0686"
#define OTA_GH_REPO   "myapp"
#define OTA_TAG_PREFIX "fw-v"          // only releases tagged fw-vX.Y.Z are firmware
#define OTA_ASSET_NAME "firmware.bin"

// Mozilla CA bundle linked in by the Arduino-ESP32 core
extern const uint8_t rootca_crt_bundle_start[] asm("_binary_x509_crt_bundle_start");

static bool parseVersion(const char *s, int v[3]) {
  v[0] = v[1] = v[2] = 0;
  if (*s == 'v' || *s == 'V') s++;
  return sscanf(s, "%d.%d.%d", &v[0], &v[1], &v[2]) >= 2;
}

static bool isNewer(const String &remote, const char *local) {
  int r[3], l[3];
  if (!parseVersion(remote.c_str(), r) || !parseVersion(local, l)) return false;
  for (int i = 0; i < 3; i++) {
    if (r[i] != l[i]) return r[i] > l[i];
  }
  return false;
}

void otaCheckAndUpdate() {
  if (strstr(FW_VERSION, "dev")) {
    Serial.println("OTA: dev build, skipping");
    return;
  }

  WiFiClientSecure client;
  client.setCACertBundle(rootca_crt_bundle_start);

  HTTPClient http;
  http.useHTTP10(true);                  // no chunked encoding, so the stream parses directly
  http.setFollowRedirects(HTTPC_STRICT_FOLLOW_REDIRECTS);
  http.addHeader("Accept", "application/vnd.github+json");
  http.setUserAgent("esp32-cam-ota");
  if (!http.begin(client, "https://api.github.com/repos/" OTA_GH_USER "/" OTA_GH_REPO "/releases?per_page=10")) {
    Serial.println("OTA: http.begin failed");
    return;
  }
  int code = http.GET();
  if (code != HTTP_CODE_OK) {
    Serial.printf("OTA: release list HTTP %d\n", code);
    http.end();
    return;
  }

  JsonDocument filter;
  filter[0]["tag_name"] = true;
  filter[0]["draft"] = true;
  filter[0]["prerelease"] = true;
  filter[0]["assets"][0]["name"] = true;
  filter[0]["assets"][0]["browser_download_url"] = true;

  JsonDocument doc;
  DeserializationError je = deserializeJson(doc, http.getStream(), DeserializationOption::Filter(filter));
  http.end();
  if (je) {
    Serial.printf("OTA: JSON error %s\n", je.c_str());
    return;
  }

  String bestTag, bestUrl;
  for (JsonObject rel : doc.as<JsonArray>()) {
    if (rel["draft"] | false || rel["prerelease"] | false) continue;
    String tag = rel["tag_name"] | "";
    if (!tag.startsWith(OTA_TAG_PREFIX)) continue;
    String ver = tag.substring(strlen(OTA_TAG_PREFIX));
    for (JsonObject a : rel["assets"].as<JsonArray>()) {
      if (String(a["name"] | "") == OTA_ASSET_NAME) {
        if (bestTag.isEmpty() || isNewer(ver, bestTag.substring(strlen(OTA_TAG_PREFIX)).c_str())) {
          bestTag = tag;
          bestUrl = String(a["browser_download_url"] | "");
        }
      }
    }
  }

  if (bestTag.isEmpty()) {
    Serial.println("OTA: no firmware release found");
    return;
  }
  String remoteVer = bestTag.substring(strlen(OTA_TAG_PREFIX));
  if (!isNewer(remoteVer, FW_VERSION)) {
    Serial.printf("OTA: up to date (running %s, latest %s)\n", FW_VERSION, remoteVer.c_str());
    return;
  }

  Serial.printf("OTA: updating %s -> %s\n", FW_VERSION, remoteVer.c_str());
  httpUpdate.setFollowRedirects(HTTPC_STRICT_FOLLOW_REDIRECTS);   // assets redirect to a CDN host
  httpUpdate.rebootOnUpdate(true);
  t_httpUpdate_return r = httpUpdate.update(client, bestUrl);
  if (r == HTTP_UPDATE_FAILED) {
    Serial.printf("OTA: failed (%d) %s\n", httpUpdate.getLastError(), httpUpdate.getLastErrorString().c_str());
  }
}

# ESP32 camera firmware (FireBeetle 2 ESP32-S3, hardware V1.0)

MJPEG streaming (`/stream`) and stills (`/photo`). The AXP313A power chip is initialised
via [DFRobot_AXP313A](https://github.com/cdjq/DFRobot_AXP313A), pulled from git by PlatformIO
(`pio pkg update` fetches the newest upstream commit; pin with `#tag` to freeze).

## First flash
    pio run -t upload
    pio device monitor
Then type `wifi SSID;PASSWORD` in the serial monitor. It is stored in NVS and survives OTA updates.
(If upload fails: hold BOOT, tap RST, release both.)

## Over-the-air updates from GitHub releases
The device checks `kratos0686/myapp` releases at boot and every 6 hours, and installs the newest
non-prerelease tagged `fw-vX.Y.Z` that has a `firmware.bin` asset, if it is newer than the running version.

Publish an update:
    git tag fw-v0.1.0 && git push origin fw-v0.1.0
CI builds and attaches `firmware.bin` to the release. Local builds are `0.0.0-dev` and never self-update.

Notes: the repo must be public for the device to read releases without a token. Anyone who can
publish releases to the repo can push firmware to the device (there is no signature check).

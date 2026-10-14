#pragma once

// Checks GitHub Releases of OTA_GH_USER/OTA_GH_REPO for a newer firmware and installs it.
// Returns only if no update was installed (a successful update reboots).
void otaCheckAndUpdate();

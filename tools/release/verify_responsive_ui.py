#!/usr/bin/env python3
import xml.etree.ElementTree as ET
from pathlib import Path

LAYOUT = Path("app/src/main/res/layout/activity_main.xml")
ANDROID = "{http://schemas.android.com/apk/res/android}"

def require(ok: bool, message: str) -> None:
    if not ok:
        raise SystemExit(message)

root = ET.fromstring(LAYOUT.read_text(encoding="utf-8"))
parents = {child: parent for parent in root.iter() for child in parent}

def by_id(name: str):
    target = "@+id/" + name
    for node in root.iter():
        if node.attrib.get(ANDROID + "id") == target:
            return node
    raise SystemExit("responsive-ui: missing id " + name)

version = by_id("versionText")
parent = parents.get(version)
require(version.attrib.get(ANDROID + "layout_width") == "match_parent",
        "responsive-ui: versionText must use full width")
require(parent is not None and parent.attrib.get(ANDROID + "orientation") != "horizontal",
        "responsive-ui: versionText must not compete horizontally with KINLINK title")

for name in [
    "stateText", "qualityScoreText", "qualityProgress",
    "beforeScoreText", "nowScoreText", "deltaScoreText",
    "maintainedText", "evidenceText", "adviceTitleText", "adviceText"
]:
    by_id(name)

detail = by_id("detailText")
require(detail.attrib.get(ANDROID + "visibility") == "gone",
        "responsive-ui: technical detail must be collapsed by default")

advice = by_id("adviceText")
require(ANDROID + "maxLines" not in advice.attrib,
        "responsive-ui: control evidence must not be clipped by maxLines")

for name in [
    "wifiDoctorButton", "mobileAssistButton", "modeConservativeButton",
    "modeBalancedButton", "modeMaxButton", "safeModeButton",
    "budgetButton", "incidentMarkerButton"
]:
    node = by_id(name)
    minimum = node.attrib.get(ANDROID + "minHeight")
    require(minimum is not None and minimum.endswith("dp") and int(minimum[:-2]) >= 48,
            "responsive-ui: touch target below 48dp: " + name)

text = LAYOUT.read_text(encoding="utf-8")
require("Mobile Assist · améliorer maintenant" not in text,
        "responsive-ui: UI overclaims throughput improvement")
require('android:text="AVANT"' in text and 'android:text="MAINTENANT"' in text,
        "responsive-ui: before/now proof labels missing")
require(
    'android:id="@+id/modeConservativeButton"' in text
    and 'android:id="@+id/modeBalancedButton"' in text
    and 'android:id="@+id/modeMaxButton"' in text,
    "responsive-ui: explicit mode selector missing",
)

print("responsive-ui: PASS")

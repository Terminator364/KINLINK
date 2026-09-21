# KINLINK UI audit — installed 0.7.1 screenshots → 0.7.2 direction

## Evidence audited

Six target-phone screenshots of installed KINLINK 0.7.1 were reviewed.

Observed 0.7.1 problems:
1. **Excessive vertical length** — the same status is explained across multiple large cards and full-width buttons.
2. **Repeated prose** — "Android garde le routage", "aucune action", mobile safety and profile explanations appear repeatedly.
3. **Weak action hierarchy** — profile, safe mode, Wi-Fi action, Mobile Assist, budget and incident marker all have nearly identical visual weight.
4. **Irrelevant action visible** — "Optimiser le Wi-Fi" remains prominent while cellular is the active transport.
5. **Ambiguous improvement claim** — "Mobile Assist · améliorer maintenant" can be read as a throughput improvement promise although the current action is `requestBandwidthUpdate()`.
6. **Proof is buried** — the user cannot immediately see whether an action produced sustained benefit, no benefit, or a later relapse.
7. **Profile contradiction is noisy** — selected "Stabilité max" and "Profil conseillé : Conservateur" coexist in the main card without a clear hierarchy.
8. **Technical detail leaks into the home view** — useful diagnostic facts compete with the one question the home screen should answer: "Is my connection okay, what is KINLINK doing, and is it helping?"

## External interface review

### Fing
Reference:
- https://help.fing.com/hc/en-us/articles/25603118731420-Explore-the-Fing-Dashboard

Pattern retained:
- at-a-glance dashboard first;
- cards as entry points to detail;
- detailed network exploration is secondary.

### Speedify
References:
- https://support.speedify.com/article/235-speedify-interface
- https://support.speedify.com/article/1066-dashboard-preferences
- https://support.speedify.com/article/241-graphs-stats
- https://speedify.com/blog/release-notes/introducing-speedify-16-5/

Patterns retained:
- current state is immediately visible;
- connection-specific information appears contextually;
- proof/impact is exposed through statistics and completed interventions;
- dashboard cards can be contextual rather than every control always visible;
- recent versions explicitly optimize responsiveness and CPU utilization.

Pattern **not copied**:
- KINLINK does not claim Speedify-style bonding because KINLINK has no qualified VPN/gateway data plane yet.

### GlassWire
References:
- https://www.glasswire.com/userguide/
- https://play.google.com/store/apps/details?id=com.glasswire.android
- YouTube walkthrough reviewed: "Glasswire Network Monitor App for PC & Android"

Patterns retained:
- visualize history instead of repeating prose;
- data-plan protection is a distinct concept;
- drill-down for technical/history detail.

### Android / Material
References:
- https://developer.android.com/design/ui/mobile/guides/components/material-overview
- https://developer.android.com/design/ui/mobile/guides/layout-and-content/common-layouts

Patterns retained:
- containment by cards;
- clear primary vs secondary actions;
- compact mobile layout;
- progressive disclosure of detail.

## 0.7.2 home-screen contract

The home screen should answer only four questions:

1. **État** — Is the current connection usable?
2. **Transport / quality** — Wi-Fi or mobile, validated or not, passive quality.
3. **Pilotage continu** — what KINLINK is currently doing or deliberately not doing.
4. **Preuve** — did recent assistance correlate with sustained improvement, relapse, or no improvement?

Everything else moves to:
- compact secondary controls;
- technical details;
- diagnostic export.

## Implemented 0.7.2 layout direction

- shorter header;
- one hero status card;
- one compact current-state card;
- one continuous-care/proof card;
- only the action relevant to the current transport is shown;
- profile/safe mode are compact paired controls;
- data protection/incident marker are compact paired controls;
- technical detail is collapsed by default;
- long explanatory paragraphs removed from the main path.

## Truthfulness rule

The UI must not call `ConnectivityManager.requestBandwidthUpdate()` a network acceleration.

Android documents this API as requesting updated bandwidth information from ConnectivityService. Therefore:
- accepted request = **metric refresh evidence**;
- later better passive quality = **correlation**;
- sustained better quality over a confirmation window = **sustained correlated improvement**;
- it is not proof that KINLINK increased radio throughput.

Strong causal improvement requires a separately qualified data-plane/system action.

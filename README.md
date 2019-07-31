# DualTrack

This is a fitness tracking utility for full-Android dual-mode watches such as the Zeblaze Thor 5. Most (all?) tracking apps don't take 
into account steps taken during long standby mode on such watches, and this is where DualTrack comes in. When switching to long standby
mode DualTrack records the step count at that point, and when switching to Android mode the step count is read again. The difference 
is logged into the Google Fit API so it can be tracked in Fit or other apps that link to Fit.

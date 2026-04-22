/*
 * File: Request.java
 * Project: fluxer-bitwig
 * Created Date: 2026-04-12 20:01:21
 * Author: 3urobeat
 *
 * Last Modified: 2026-04-22 17:59:58
 * Modified By: 3urobeat
 *
 * Copyright (c) 2026 3urobeat <https://github.com/3urobeat>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */


package org.x3urobeat;

import java.time.Duration;
import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Handles interfacing with Fluxer API
 */

public class Request {

    private String lastStatusText;
    private long lastUpdateTimestamp;
    private long lastSendAttempt;

    private Timer cooldownTimer;

    private Timer refreshTimer;

    private static final String API_URL = "https://web.fluxer.app/api/v1/users/@me/settings";
    private static final int TIMEOUT_MS  = 10000;
    private static final int COOLDOWN_MS = 2500;  // Do not send requests quicker than this

    private final fluxerbitwigExtension ext;


    /**
     * Constructor
     * @param ext Reference to Extension Main Class
     */
    public Request(fluxerbitwigExtension ext) {
        this.ext = ext;
    }

    /** Get text of the last successful status update request */
    public String getLastStatusText()
    {
        return this.lastStatusText;
    }

    /** Get timestamp of the last successful status update request */
    public long getLastUpdateTimestamp()
    {
        return this.lastUpdateTimestamp;
    }


    /**
     * Sends an API request
     * @throws Exception Throws Exception on failure
     * @param authToken Fluxer auth token to supply
     * @param payload Encoded JSON payload to send in request
     * @param force Set to true to bypass cooldown (should only be used in exit!)
     */
    private void sendApiRequest(final String authToken, final String payload, final boolean force) throws Exception {

        // Queue request if on cooldown, replacing any existing pending request
        if (lastSendAttempt + COOLDOWN_MS > System.currentTimeMillis() && !force) {
            this.ext.logDebug("On cooldown, queueing request...");

            // Cancel existing timer if any
            if (cooldownTimer != null) {
                cooldownTimer.cancel();
            }

            // Schedule new timer to execute pending request after cooldown
            cooldownTimer = new Timer();
            cooldownTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    ext.logDebug("Cooldown Timer: Sending queued request...");
                    try {
                        // Reset to allow immediate send
                        lastSendAttempt = 0;
                        sendApiRequest(authToken, payload, false);
                    } catch (Exception e) {
                        ext.logErr("Cooldown Timer: Failed to send queued request: " + e.getMessage()); // This sadly consumes Exception but eh
                    }
                }
            }, lastSendAttempt + COOLDOWN_MS - System.currentTimeMillis());

            return;
        }
        lastSendAttempt = System.currentTimeMillis();

        // Create request
        OkHttpClient client = new OkHttpClient()
            .newBuilder()
            .connectTimeout(Duration.ofMillis(TIMEOUT_MS))
            .build();

        // Get payload
        RequestBody body = RequestBody.create(payload, MediaType.get("application/json; charset=utf-8"));

        // Set headers for PATCH request
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(API_URL)
                .patch(body)
                .addHeader("Accept", "*/*")
                .addHeader("Authorization", authToken)
                .addHeader("Content-Type", "application/json")
                // Content-Length is being added automatically
                .addHeader("Origin", "https://web.fluxer.app")
                .build();

        // Attempt to send request
        Response response = client.newCall(request).execute();

        int responseCode = response.code();
        //this.ext.logDebug("Request Response (" + responseCode + "): " + response.body().string());
        this.ext.logDebug("Request Response: " + responseCode);

        // Throw Exception on non-200 response code
        if (responseCode != 200) {
            throw new Exception("API Error (" + responseCode + ")");
        }

    }


    /**
     * (Re-)Attaches periodic task to refresh status if no update occurred in the last 'config.statusUntil' ms
     */
    private void reattachRefreshTimerTask() {
        if (this.ext.config.statusUntil <= 0) return; // Ignore request if status doesn't expire

        // Cancel any running timer
        if (refreshTimer != null) {
            refreshTimer.cancel();
            refreshTimer = null;
        }

        // Create new timer
        refreshTimer = new Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                ext.logInfo("Refresh Timer: Updating status...");

                try {
                    updatePresence(true); // Force update
                } catch(Exception e) {
                    ext.logErr("Refresh Timer: Failed to update status: " + e.getMessage()); // Timer will run again
                }
            }
        };

        long period = Math.max(this.ext.config.statusUntil - 2500, COOLDOWN_MS); // Refresh 2.5 seconds before expiration, clamp to COOLDOWN_MS

        refreshTimer.scheduleAtFixedRate(task, period, period);

        this.ext.logDebug("Scheduled new refresh timer task!");
    }


    /**
     * Sends presence update to Fluxer API
     * @throws Exception Throws Error on failure
     * @param activityText Activity text to send
     * @param force Set to true to bypass cooldown (should only be used in exit!)
     */
    public void sendPresenceUpdate(final String activityText, final boolean force) throws Exception {
        if (this.ext.config.token == null || this.ext.config.token.length() == 0) {
            throw new Exception("No token set");
        }

        // Construct and encode presence data to send
        final String expiresAt = Instant.ofEpochMilli(System.currentTimeMillis() + this.ext.config.statusUntil).toString();

        final String statusData = String.format(
            "{\"text\":\"%s\",\"expires_at\":\"%s\"}",
            escapeJson(activityText),
            expiresAt
        );

        final String payload = String.format("{\"custom_status\":%s}", statusData);

        // Attempt to send
        try {
            this.sendApiRequest(this.ext.config.token, payload, force);
            this.ext.logDebug("Successfully sent status update to Fluxer API!");

            lastStatusText      = activityText;
            lastUpdateTimestamp = System.currentTimeMillis();

        } catch (Exception error) {

            this.ext.logErr("Failed to send status update to Fluxer: " + error.getMessage());
            this.ext.showNotification("Failed to send status update to Fluxer API: " + error.getMessage());
            throw error; // Rethrow
        }
    }


    /**
     * Updates Fluxer presence with current activity if necessary
     * @throws Exception Throws exception when presence send request failed
     * @param force Set to true to force update now
     */
    public void updatePresence(final boolean force) throws Exception {

        // Get current activity
        String activityText = this.ext.activity.getActivityText();

        // Defer update if last sent activity matches current and an update has been sent in the last statusUntil ms
        final boolean recentlyUpdated = lastUpdateTimestamp > 0 && lastUpdateTimestamp + this.ext.config.statusUntil > System.currentTimeMillis();

        if (!force && activityText.equals(lastStatusText) && recentlyUpdated) {
            this.ext.logDebug("Current presence matches last sent status, avoiding update...");
            return;
        }

        this.ext.logInfo("New activity detected, updating status from '" + lastStatusText + "' to '" + activityText + "'...");

        // Send request
        this.sendPresenceUpdate(activityText, false);

        // Make sure status doesn't expire if user does not trigger update in statusUntil ms
        this.reattachRefreshTimerTask();

    }


    /**
     * Helper function to escape JSON
     * @param input JSON string to escape
     * @return Escaped string
     */
    private static String escapeJson(final String input) {
        if (input == null) return "";
        return input
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }

}

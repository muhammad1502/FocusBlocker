package com.example.focusblocker

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast

class FocusTileService : TileService() {

    // Called when the user taps the tile
    override fun onClick() {
        super.onClick()

        // Toggle the state
        NotificationBlockerService.isBlockingActive = !NotificationBlockerService.isBlockingActive

        // Update the tile look
        updateTile()

        // Show a small message
        val status = if (NotificationBlockerService.isBlockingActive) "Blocking ON" else "Blocking OFF"
        Toast.makeText(this, status, Toast.LENGTH_SHORT).show()
    }

    // Called when the notification shade is pulled down (to refresh the icon)
    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    private fun updateTile() {
        val tile = qsTile ?: return

        if (NotificationBlockerService.isBlockingActive) {
            tile.state = Tile.STATE_ACTIVE
            tile.label = "Focus ON"
        } else {
            tile.state = Tile.STATE_INACTIVE
            tile.label = "Focus OFF"
        }

        tile.updateTile()
    }
}
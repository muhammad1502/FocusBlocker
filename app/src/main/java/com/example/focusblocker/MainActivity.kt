package com.example.focusblocker

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val enabledNotificationListeners = "enabled_notification_listeners"
    private val actionNotificationListenerSettings = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"
    private val actionNotificationPolicyAccessSettings = "android.settings.NOTIFICATION_POLICY_ACCESS_SETTINGS"

    private lateinit var listAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // FIX: Ask for POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        NotificationBlockerService.loadList(this)

        val btnPermission = findViewById<Button>(R.id.btn_permission)
        val btnAddApp = findViewById<Button>(R.id.btn_add_app)
        val etPackage = findViewById<EditText>(R.id.et_package_name)
        val switchBlock = findViewById<SwitchCompat>(R.id.switch_block)
        val tvStatus = findViewById<TextView>(R.id.tv_status)
        val lvBlockedApps = findViewById<ListView>(R.id.lv_blocked_apps)

        val blockedList = ArrayList(NotificationBlockerService.blockedPackages)
        listAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, blockedList)
        lvBlockedApps.adapter = listAdapter

        lvBlockedApps.setOnItemClickListener { _, _, position, _ ->
            val appToRemove = blockedList[position]
            NotificationBlockerService.blockedPackages.remove(appToRemove)
            NotificationBlockerService.saveList(this)
            refreshList(blockedList)
            Toast.makeText(this, getString(R.string.toast_removed_from_list, appToRemove), Toast.LENGTH_SHORT).show()
        }

        btnPermission.setOnClickListener {
            startActivity(Intent(actionNotificationListenerSettings))
        }

        btnPermission.setOnLongClickListener {
            startActivity(Intent(actionNotificationPolicyAccessSettings))
            Toast.makeText(this, "Opening Do Not Disturb Access", Toast.LENGTH_SHORT).show()
            true
        }

        btnAddApp.setOnClickListener {
            val pkg = etPackage.text.toString().trim().lowercase()
            if (pkg.isNotEmpty()) {
                if (NotificationBlockerService.blockedPackages.contains(pkg)) {
                    Toast.makeText(this, "App already in list", Toast.LENGTH_SHORT).show()
                } else {
                    NotificationBlockerService.blockedPackages.add(pkg)
                    NotificationBlockerService.saveList(this)
                    refreshList(blockedList)
                    Toast.makeText(this, getString(R.string.toast_added_to_list, pkg), Toast.LENGTH_SHORT).show()
                }
                etPackage.text.clear()
            }
        }

        switchBlock.setOnCheckedChangeListener { _, isChecked ->
            NotificationBlockerService.isBlockingActive = isChecked

            if (isChecked) {
                updateStatusText(tvStatus, switchBlock)
                NotificationBlockerService.savedNotifications.clear()
            } else {
                updateStatusText(tvStatus, switchBlock)
                // Restore logic
                val count = NotificationBlockerService.savedNotifications.size
                if (count > 0) {
                    Toast.makeText(this, "Restoring $count notifications...", Toast.LENGTH_SHORT).show()
                    NotificationBlockerService.restoreNow(this)
                }
            }
        }

        switchBlock.isChecked = NotificationBlockerService.isBlockingActive
        updateStatusText(tvStatus, switchBlock)
    }

    private fun refreshList(list: ArrayList<String>) {
        list.clear()
        list.addAll(NotificationBlockerService.blockedPackages)
        listAdapter.notifyDataSetChanged()
    }

    private fun updateStatusText(tvStatus: TextView, switchBlock: SwitchCompat) {
        if (NotificationBlockerService.isBlockingActive) {
            if (!isNotificationServiceEnabled()) {
                Toast.makeText(this, getString(R.string.toast_grant_permission), Toast.LENGTH_LONG).show()
                switchBlock.isChecked = false
            } else {
                tvStatus.text = getString(R.string.status_blocking)
                tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
            }
        } else {
            tvStatus.text = getString(R.string.status_not_blocking)
            tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
        }
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val pkgName = packageName
        val flat = Settings.Secure.getString(contentResolver, enabledNotificationListeners)
        if (!TextUtils.isEmpty(flat)) {
            val names = flat.split(":").toTypedArray()
            for (name in names) {
                val cn = ComponentName.unflattenFromString(name)
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.packageName)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    override fun onResume() {
        super.onResume()
        val switchBlock = findViewById<SwitchCompat>(R.id.switch_block)
        if (switchBlock.isChecked != NotificationBlockerService.isBlockingActive) {
            switchBlock.isChecked = NotificationBlockerService.isBlockingActive
        }
    }
}
/*
 * Ani
 * Copyright (C) 2022-2024 Him188
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.him188.ani.app.platform

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Stable
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentLinkedQueue

abstract class BaseComponentActivity : ComponentActivity() {
    @Stable
    val snackbarHostState = SnackbarHostState()

    private val requestPermissionResultHandlers: MutableCollection<(Boolean) -> Unit> = ConcurrentLinkedQueue()
    private val registerForActivityResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            requestPermissionResultHandlers.forEach {
                it.invoke(granted)
            }
        }
    private val requestPermissionLock = Mutex()

    suspend fun requestPermission(permission: String): Boolean {
        val res = CompletableDeferred<Boolean>()
        return requestPermissionLock.withLock {
            val handler: (Boolean) -> Unit = { res.complete(it) }
            requestPermissionResultHandlers.add(handler)
            try {
                registerForActivityResult.launch(permission)
                res.await()
            } finally {
                requestPermissionResultHandlers.remove(handler)
            }
        }
    }

    fun enableDrawingToSystemBars() {
        enableEdgeToEdge(
            SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
        )

        WindowCompat.setDecorFitsSystemWindows(window, false)
    }
}

suspend fun BaseComponentActivity.showSnackbar(
    message: String,
    actionLabel: String? = null,
    withDismissAction: Boolean = false,
    duration: SnackbarDuration = SnackbarDuration.Short
): SnackbarResult {
    return snackbarHostState.showSnackbar(message, actionLabel, withDismissAction, duration)
}

fun BaseComponentActivity.showSnackbarAsync(
    message: String,
    actionLabel: String? = null,
    withDismissAction: Boolean = false,
    duration: SnackbarDuration = SnackbarDuration.Short
) {
    lifecycleScope.launch(Dispatchers.Main) {
        try {
            snackbarHostState.showSnackbar(message, actionLabel, withDismissAction, duration)
        } catch (e: Exception) { // exception will crash app
            e.printStackTrace()
        }
    }
}

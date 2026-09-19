package com.washapp.util

import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.washapp.R
import com.washapp.data.repository.AuthRepository
import kotlinx.coroutines.launch

fun showAccountDialog(
    context: Context,
    rootView: android.view.View,
    lifecycleScope: LifecycleCoroutineScope,
    authRepository: AuthRepository
) {
    lifecycleScope.launch {
        val user = authRepository.getCurrentUser() ?: return@launch
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.title_account)
            .setMessage(
                context.getString(
                    R.string.format_account_details,
                    user.name,
                    user.email,
                    user.role.name
                )
            )
            .setPositiveButton(R.string.action_reset_password) { _, _ ->
                lifecycleScope.launch {
                    try {
                        authRepository.sendPasswordResetEmail(user.email)
                        Snackbar.make(rootView, R.string.message_reset_email_sent, Snackbar.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Snackbar.make(rootView, e.message ?: "Couldn't send reset email", Snackbar.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }
}

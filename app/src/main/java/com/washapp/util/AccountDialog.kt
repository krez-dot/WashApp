package com.washapp.util

import android.content.Context
import android.view.LayoutInflater
import androidx.lifecycle.LifecycleCoroutineScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.washapp.R
import com.washapp.data.repository.AuthRepository
import com.washapp.databinding.DialogEditProfileBinding
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
            .setNeutralButton(R.string.action_edit_profile) { _, _ ->
                showEditProfileDialog(context, rootView, lifecycleScope, authRepository, user.name)
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }
}

private fun showEditProfileDialog(
    context: Context,
    rootView: android.view.View,
    lifecycleScope: LifecycleCoroutineScope,
    authRepository: AuthRepository,
    currentName: String
) {
    val binding = DialogEditProfileBinding.inflate(LayoutInflater.from(context))
    binding.profileNameInput.setText(currentName)

    MaterialAlertDialogBuilder(context)
        .setTitle(R.string.title_edit_profile)
        .setView(binding.root)
        .setPositiveButton(R.string.action_save) { _, _ ->
            val name = binding.profileNameInput.text.toString().trim()
            if (name.isEmpty()) {
                Snackbar.make(rootView, R.string.message_name_required, Snackbar.LENGTH_SHORT).show()
                return@setPositiveButton
            }
            lifecycleScope.launch {
                try {
                    authRepository.updateProfileName(name)
                    Snackbar.make(rootView, R.string.message_profile_updated, Snackbar.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Snackbar.make(rootView, e.message ?: "Couldn't update profile", Snackbar.LENGTH_LONG).show()
                }
            }
        }
        .setNegativeButton(R.string.action_cancel, null)
        .show()
}

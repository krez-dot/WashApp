package com.washapp.util

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
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

    val dialog = MaterialAlertDialogBuilder(context)
        .setTitle(R.string.title_edit_profile)
        .setView(binding.root)
        .setPositiveButton(R.string.action_save, null)
        .setNegativeButton(R.string.action_cancel, null)
        .create()
    dialog.show()

    // Set after show() so a validation error keeps the dialog open instead of dismissing it.
    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
        listOf(
            binding.nameLayout, binding.currentPasswordLayout,
            binding.newPasswordLayout, binding.confirmPasswordLayout
        ).forEach { it.error = null }

        val name = binding.profileNameInput.text.toString().trim()
        val current = binding.currentPasswordInput.text.toString()
        val newPassword = binding.newPasswordInput.text.toString()
        val confirm = binding.confirmPasswordInput.text.toString()
        val changingPassword = current.isNotEmpty() || newPassword.isNotEmpty() || confirm.isNotEmpty()

        var valid = true
        if (name.isEmpty()) {
            binding.nameLayout.error = context.getString(R.string.message_name_required)
            valid = false
        }
        if (changingPassword) {
            if (current.isEmpty()) {
                binding.currentPasswordLayout.error = context.getString(R.string.error_current_password_required)
                valid = false
            }
            if (newPassword.length < 6) {
                binding.newPasswordLayout.error = context.getString(R.string.error_new_password_short)
                valid = false
            } else if (newPassword != confirm) {
                binding.confirmPasswordLayout.error = context.getString(R.string.error_passwords_dont_match)
                valid = false
            }
        }
        if (!valid) return@setOnClickListener

        lifecycleScope.launch {
            try {
                if (changingPassword) authRepository.changePassword(current, newPassword)
                authRepository.updateProfileName(name)
                dialog.dismiss()
                val message = if (changingPassword) {
                    R.string.message_profile_and_password_updated
                } else {
                    R.string.message_profile_updated
                }
                Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show()
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                binding.currentPasswordLayout.error = context.getString(R.string.error_current_password_wrong)
            } catch (e: Exception) {
                Snackbar.make(rootView, e.message ?: "Couldn't update profile", Snackbar.LENGTH_LONG).show()
            }
        }
    }
}

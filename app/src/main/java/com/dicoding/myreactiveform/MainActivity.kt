package com.dicoding.myreactiveform

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.functions.Function3
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val emailStream = RxTextView.afterTextChangeEvents(ed_email)
            .map { email ->
                !Patterns.EMAIL_ADDRESS.matcher(email.editable().toString()).matches()
            }
        emailStream.subscribe {
            showEmailExistAlert(it)
        }

        val passwordStream = RxTextView.textChanges(ed_password)
            .map { password ->
                password.toString().length < 6
            }
        passwordStream.subscribe {
            showPasswordMinimalAlert(it)
        }

        val passwordConfirmationStream = Observable.merge(
            RxTextView.textChanges(ed_password)
                .map { password ->
                    password.toString() != ed_confirm_password.text.toString()
                },
            RxTextView.textChanges(ed_confirm_password)
                .map { confirmpassword ->
                    confirmpassword.toString() != ed_password.text.toString()
                }
        )
        passwordConfirmationStream.subscribe {
            showPasswordConfirmationAlert(it)
        }

        val invalidFieldsStream = Observable.combineLatest(
            emailStream,
            passwordStream,
            passwordConfirmationStream,
            Function3 { emailInvalid: Boolean, passwordInvalid: Boolean, passwordConfirmationInvalid: Boolean ->
                !emailInvalid && !passwordInvalid && !passwordConfirmationInvalid
            })
        invalidFieldsStream.subscribe {
            if (it) {
                btn_register.isEnabled = true
                btn_register.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent))
            } else {
                btn_register.isEnabled = false
                btn_register.setBackgroundColor(Color.LTGRAY)
            }
        }
    }

    private fun showEmailExistAlert(value: Boolean) {
        ed_email.error = if (value) getString(R.string.email_not_valid) else null
    }

    private fun showPasswordMinimalAlert(value: Boolean) {
        ed_password.error = if (value) getString(R.string.password_not_valid) else null
    }

    private fun showPasswordConfirmationAlert(value: Boolean) {
        ed_confirm_password.error = if (value) getString(R.string.password_not_same) else null
    }
}

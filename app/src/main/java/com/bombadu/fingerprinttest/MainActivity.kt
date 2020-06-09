package com.bombadu.fingerprinttest

import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.*
import com.github.pwittchen.rxbiometric.library.RxBiometric
import com.github.pwittchen.rxbiometric.library.throwable.AuthenticationError
import com.github.pwittchen.rxbiometric.library.throwable.AuthenticationFail
import com.github.pwittchen.rxbiometric.library.throwable.BiometricNotSupported
import com.github.pwittchen.rxbiometric.library.validation.RxPreconditions
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {


    private var disposable: Disposable? = null

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //setSupportActionBar(toolbar)


        button.setOnClickListener {
            disposable =
                RxPreconditions
                    .hasBiometricSupport(this)
                    .flatMapCompletable {
                        if (!it) Completable.error(BiometricNotSupported())
                        else
                            RxBiometric
                                .title("Michael's Fingerprint App")
                                .description("Login using your Fingerprint")
                                .negativeButtonText("cancel")
                                .negativeButtonListener(DialogInterface.OnClickListener { _, _ ->
                                    showMessage("cancel")
                                })
                                .executor(getMainExecutor(this@MainActivity))
                                .build()
                                .authenticate(this)
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(
                        onComplete = { showMessage("authenticated!")
                        startActivity(Intent(this, EndActivity::class.java))},
                        onError = {
                            when (it) {
                                is AuthenticationError -> showMessage("error: ${it.errorCode} ${it.errorMessage}")
                                is AuthenticationFail -> showMessage("fail")
                                else -> {
                                    showMessage("other error")
                                }
                            }
                        }
                    )
        }
    }

    override fun onPause() {
        super.onPause()
        disposable?.let {
            if (!it.isDisposed) {
                it.dispose()
            }
        }
    }

    private fun showMessage(message: String) {
        Toast
            .makeText(
                this@MainActivity,
                message,
                Toast.LENGTH_SHORT
            )
            .show()
    }
    }

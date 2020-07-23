package com.chcreation.pointofsale.checkout

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.widget.doOnTextChanged
import com.chcreation.pointofsale.R
import kotlinx.android.synthetic.main.activity_note.*
import org.jetbrains.anko.sdk27.coroutines.onClick

class NoteActivity : AppCompatActivity() {

    companion object{
        var note = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)

        etNote.doOnTextChanged { text, start, before, count ->
            note = text.toString()
        }

        btnNoteSave.onClick {
            finish()
        }
    }
}

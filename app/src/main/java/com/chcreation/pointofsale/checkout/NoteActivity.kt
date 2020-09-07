package com.chcreation.pointofsale.checkout

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.core.widget.doOnTextChanged
import com.chcreation.pointofsale.R
import com.chcreation.pointofsale.normalClickAnimation
import kotlinx.android.synthetic.main.activity_note.*
import org.jetbrains.anko.sdk27.coroutines.onClick

class NoteActivity : AppCompatActivity() {

    companion object{
        var note = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)

        supportActionBar?.title = "Note"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        etNote.setText(note)
        etNote.doOnTextChanged { text, start, before, count ->
            note = text.toString()
        }

        btnNoteSave.onClick {
            btnNoteSave.startAnimation(normalClickAnimation())
            finish()
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }

        return super.onOptionsItemSelected(item)
    }
}

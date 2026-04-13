package com.app.pocketfinance

import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.fragment.app.Fragment

class EmailInputFragment : Fragment() {
    private var editText: EditText? = null
    private var pendingEmail: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_email_input, container, false)
        editText = view.findViewById(R.id.editEmailFragment)

        pendingEmail?.let { editText?.setText(it) }

        return view
    }

    fun getEmail(): String = editText?.text?.toString() ?: ""

    fun setEmail(email: String) {
        pendingEmail = email
        editText?.setText(email)
    }
}
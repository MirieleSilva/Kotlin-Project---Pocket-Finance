package com.app.pocketfinance

import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.fragment.app.Fragment

class PasswordInputFragment : Fragment() {
    private var editText: EditText? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_password_input, container, false)
        editText = view.findViewById(R.id.editPasswordFragment)
        return view
    }

    fun getPassword(): String = editText?.text?.toString() ?: ""
}
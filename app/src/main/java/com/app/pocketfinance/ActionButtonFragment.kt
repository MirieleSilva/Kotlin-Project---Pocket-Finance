package com.app.pocketfinance

import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.fragment.app.Fragment

class ActionButtonFragment : Fragment() {
    private var button: Button? = null
    private var pendingText: String? = null
    private var pendingClick: (() -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_action_button, container, false)

        button = view.findViewById(R.id.btnActionFragment)

        pendingText?.let { button?.text = it }
        pendingClick?.let { callback -> button?.setOnClickListener { callback() } }

        return view
    }

    fun setButtonText(text: String) {
        pendingText = text
        button?.text = text
    }

    fun setOnClickListener(callback: () -> Unit) {
        pendingClick = callback
        button?.setOnClickListener { callback() }
    }
}
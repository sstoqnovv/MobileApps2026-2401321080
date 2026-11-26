package com.example.maintenancelog

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class LogDetailFragment : Fragment(R.layout.fragment_log_detail) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.backToListButton).setOnClickListener {
            findNavController().navigateUp()
        }
    }
}

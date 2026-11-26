package com.example.maintenancelog

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class LogListFragment : Fragment(R.layout.fragment_log_list) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.openDetailButton).setOnClickListener {
            findNavController().navigate(R.id.action_logListFragment_to_logDetailFragment)
        }
    }
}

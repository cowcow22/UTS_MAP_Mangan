package com.example.uts_map_mangan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CalendarView
import androidx.fragment.app.Fragment

class DiaryFragment : Fragment() {

    private lateinit var calendarView: CalendarView
    private lateinit var btnShowLess: Button
    private var isCalendarExpanded = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_diary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        calendarView = view.findViewById(R.id.calendarView)
        btnShowLess = view.findViewById(R.id.btnShowLess)

        // Mengatur aksi tombol Show Less untuk mengecilkan/memperbesar CalendarView
        btnShowLess.setOnClickListener {
            if (isCalendarExpanded) {
                // Menyembunyikan tampilan penuh CalendarView dan menampilkan hanya tanggal hari ini
                calendarView.layoutParams.height = 150 // Ukuran tinggi ringkas
                btnShowLess.text = "Show More"
            } else {
                // Mengembalikan CalendarView ke ukuran penuh
                calendarView.layoutParams.height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                btnShowLess.text = "Show Less"
            }
            calendarView.requestLayout()
            isCalendarExpanded = !isCalendarExpanded
        }
    }

    fun onBackPressed() {
        activity?.onBackPressed()
        activity?.overridePendingTransition(0, 0) // No transition animation when pressing back
    }
}
package com.example.papb_13

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.papb_13.databinding.ActivityMainBinding
import com.google.firebase.firestore.FirebaseFirestore

private val firestore = FirebaseFirestore.getInstance()
private val feedbackCollectionRef = firestore.collection("feedback")
private lateinit var binding: ActivityMainBinding
private val feedbackListLiveData: MutableLiveData<List<Feedback>> by lazy {
    MutableLiveData<List<Feedback>>()
}

class MainActivity : AppCompatActivity() {
    companion object {
        const val ID_FEEDBACK = "id_feedback"
        const val NAMA = "nama"
        const val DESKRIPSI = "deskripsi"
        const val TANGGAL = "tanggal"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.plus.setOnClickListener {
            val intent = Intent(this, AddAndEditFeedback::class.java)
            startActivity(intent)
        }

        observeFeedback()
        getAllFeedback()
    }

    private fun getAllFeedback() {
        observeFeedbackChanges()
    }

    private fun observeFeedback() {
        feedbackListLiveData.observe(this) { feedback ->
            val adapterFeed = FeedbackAdapter(this, feedback)
            binding.recycle.apply {
                adapter = adapterFeed
                layoutManager = LinearLayoutManager(this@MainActivity)
                setHasFixedSize(true)
            }
        }
    }

    private fun observeFeedbackChanges() {
        feedbackCollectionRef.addSnapshotListener { snapshots, error ->
            if (error != null) {
                Log.d("MainActivity", "Error listening for feedback changes: ", error)
                return@addSnapshotListener
            }
            val feedback = snapshots?.toObjects(Feedback::class.java)
            if (feedback != null) {
                feedbackListLiveData.postValue(feedback)
            }
        }
    }

    inner class FeedbackAdapter(
        private val context: Context,
        private val feedbackList: List<Feedback>
    ) : RecyclerView.Adapter<FeedbackAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val itemView = LayoutInflater.from(context).inflate(R.layout.feedback, parent, false)
            return ViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val feedback = feedbackList[position]
            holder.bind(feedback)
        }

        override fun getItemCount(): Int {
            return feedbackList.size
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val namaTextView: TextView = itemView.findViewById(R.id.namaTextView)
            private val deskripsiTextView: TextView = itemView.findViewById(R.id.deskripsiTextView)
            private val tanggalTextView: TextView = itemView.findViewById(R.id.tanggalTextView)
            private val btnEdit: ImageButton = itemView.findViewById(R.id.btn_edit)
            private val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete)

            fun bind(feedback: Feedback) {
                // Bind the data to the views
                namaTextView.text = feedback.nama
                deskripsiTextView.text = feedback.deskripsi
                tanggalTextView.text = feedback.tanggal

                // Set click listener for item click
                itemView.setOnClickListener {
                    val intent = Intent(context, AddAndEditFeedback::class.java)
                    intent.putExtra(ID_FEEDBACK, feedback.id)
                    intent.putExtra(NAMA, feedback.nama)
                    intent.putExtra(DESKRIPSI, feedback.deskripsi)
                    intent.putExtra(TANGGAL, feedback.tanggal)
                    context.startActivity(intent)
                }

                // Set long click listener for item long click
                itemView.setOnLongClickListener {
                    deleteBudget(feedback)
                    true // Indicate that the long click event is handled
                }

                // Set click listener for edit button
                btnEdit.setOnClickListener {
                    // Handle edit button click
                    val intent = Intent(context, AddAndEditFeedback::class.java)
                    intent.putExtra("ID_FEEDBACK", feedback.id)
                    intent.putExtra("NAMA", feedback.nama)
                    intent.putExtra("DESKRIPSI", feedback.deskripsi)
                    intent.putExtra("TANGGAL", feedback.tanggal)
                    context.startActivity(intent)
                }

                // Set click listener for delete button
                btnDelete.setOnClickListener {
                    // Handle delete button click
                    deleteBudget(feedback)
                }
            }
        }
    }

    private fun deleteBudget(feedback: Feedback) {
        if (feedback.id.isEmpty()) {
            Log.d("MainActivity", "Error deleting: feedback ID is empty!")
            return
        }
        feedbackCollectionRef.document(feedback.id)
            .delete()
            .addOnSuccessListener {
                Log.d("MainActivity", "Successfully Deleting Feedback")
            }
            .addOnFailureListener {
                Log.d("MainActivity", "Error deleting feedback: ", it)
            }
    }
}
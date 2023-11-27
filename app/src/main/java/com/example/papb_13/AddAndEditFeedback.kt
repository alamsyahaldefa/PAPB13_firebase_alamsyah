package com.example.papb_13

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.papb_13.MainActivity.Companion.DESKRIPSI
import com.example.papb_13.MainActivity.Companion.ID_FEEDBACK
import com.example.papb_13.MainActivity.Companion.NAMA
import com.example.papb_13.MainActivity.Companion.TANGGAL
import com.example.papb_13.databinding.AddAndEditFeedbackBinding
import com.google.firebase.firestore.FirebaseFirestore

private var idFeedback = ""
private val firestore = FirebaseFirestore.getInstance()
private val feedbackCollectionRef = firestore.collection("feedback")

class AddAndEditFeedback : AppCompatActivity() {
    private lateinit var binding: AddAndEditFeedbackBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AddAndEditFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idFeedback = intent.getStringExtra(ID_FEEDBACK) ?: ""
        val nama = intent.getStringExtra(NAMA)
        val deskripsi = intent.getStringExtra(DESKRIPSI)
        val tanggal = intent.getStringExtra(TANGGAL)

        // Kemudian, gunakan data yang diterima untuk mengisi EditText atau di tempat lainnya
        binding.namaInput.setText(nama)
        binding.deskripsiInput.setText(deskripsi)
        binding.tanggalInput.setText(tanggal)

        binding.btnEdit.setOnClickListener {
            onUpdateClicked() // Panggil fungsi onUpdateClicked saat tombol diklik
        }

        binding.btnAdd.setOnClickListener {
            val nama = binding.namaInput.text.toString()
            val deskprsi = binding.deskripsiInput.text.toString()
            val tanggal = binding.tanggalInput.text.toString()
            val newBudget = Feedback(nama = nama, deskripsi = deskprsi,
                tanggal = tanggal)
            if (idFeedback.isNotEmpty()) {
                newBudget.id = idFeedback
                updateFeedback(newBudget)
            } else {
                addBudget(newBudget)
            }
        }
    }

    private fun updateFeedback(feedback: Feedback) {
        feedback.id = idFeedback
        feedbackCollectionRef.document(idFeedback)
            .set(feedback)
            .addOnSuccessListener {
                Log.d("SecondActivity", "Successfully Updating Feedback")
                navigateToMainActivity()
            }
            .addOnFailureListener {
                Log.d("MainActivity", "Error updating feedback: ", it)
            }
    }

    private fun addBudget(feedback: Feedback) {
        feedbackCollectionRef.add(feedback)
            .addOnSuccessListener { documentReference ->
                val createdFeedbackId = documentReference.id
                feedback.id = createdFeedbackId
                documentReference.set(feedback)
                    .addOnSuccessListener {
                        Log.d("SecondActivity", "Successfully Adding Feedback")
                        navigateToMainActivity()
                    }
                    .addOnFailureListener {
                        Log.d("MainActivity", "Error adding feedback ID: ", it)
                    }
            }
            .addOnFailureListener {
                Log.d("MainActivity", "Error adding feedback: ", it)
            }
    }

    private fun onUpdateClicked() {
        val nama = binding.namaInput.text.toString()
        val deskripsi = binding.deskripsiInput.text.toString()
        val tanggal = binding.tanggalInput.text.toString()
        val updateFeedback = Feedback(nama = nama, deskripsi = deskripsi,
            tanggal = tanggal)

        if (idFeedback.isNotEmpty()) {
            updateFeedback.id = idFeedback
            updateFeedback(updateFeedback)
        } else {
            addBudget(updateFeedback)
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

}
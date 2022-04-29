package com.magda.noteapp.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import com.google.firebase.firestore.FirebaseFirestore
import com.magda.noteapp.R
import com.magda.noteapp.activity.AddNoteActivity
import com.magda.noteapp.adapters.NotesAdapter
import com.magda.noteapp.databinding.FragmentNoteBinding
import com.magda.noteapp.model.Note
import com.magda.noteapp.model.User
import com.magda.noteapp.utils.Constants


/**
 * A simple [Fragment] subclass.
 * create an instance of this fragment.
 */
class NoteFragment : Fragment(), NotesAdapter.ItemClickListener {
    private   val TAG: String = "Note Fragment"
    private lateinit var binding: FragmentNoteBinding
    private val _binding get() = binding!!
    private lateinit var user: User


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = requireActivity().intent.getParcelableExtra(Constants.USER)!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNoteBinding.inflate(inflater, container, false)
        initViews()
        return _binding.root
    }


    private fun initViews() {
        binding.apply {
            val fullNames = "Hello ${user.firstName} ${user.lastName}"
            tvHello.text = fullNames
            tvHello.setOnClickListener { toProfilePage() }
            tvAddNote.setOnClickListener { toTheAddPage() }
            val noteAdapter = NotesAdapter(requireContext(), this@NoteFragment)
            val layoutManager = LinearLayoutManager(requireActivity())
            layoutManager.orientation = VERTICAL
            notesRecyclerView.apply {
                setLayoutManager(layoutManager)
                setHasFixedSize(true)
                adapter = noteAdapter
            }
            val db = FirebaseFirestore.getInstance()
            val notesReference = db.collection(Constants.NOTES)
            notesReference
                .whereEqualTo(Constants.USER_ID, user.userId).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result.let {
                            val notesList = it.documents
                                .map { snapShot ->
                                    snapShot.toObject(Note::class.java)
                                }.sortedByDescending { note ->
                                    note?.timeStamp
                                }.filterNotNull()
                                .toMutableList()
                            with(noteAdapter) {
                                addNotes(notesList)
                            }

                        }
                    } else {
                        Toast.makeText(
                            requireActivity(),
                            "This was not Successful. Try Again",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }.addOnFailureListener { e ->
                Toast.makeText(requireActivity(), e.localizedMessage, Toast.LENGTH_LONG).show()

            }
        }


    }

    private fun toProfilePage() {
        val navController = Navigation.findNavController(binding.root)
        val args = Bundle()
        args.putParcelable(Constants.USER, user)
        navController.navigate(R.id.action_noteFragment_to_profileFragment, args)
    }

    private fun toTheAddPage() {
        val intent = Intent(requireActivity(), AddNoteActivity::class.java)
        intent.putExtra(Constants.USER, user)
        startActivity(intent)
    }

    override fun itemClicked(view: View, note: Note) {
        if (view.id == R.id.noteCardView) {
            startActivity(Intent(requireActivity(), AddNoteActivity::class.java))
        }
    }
}
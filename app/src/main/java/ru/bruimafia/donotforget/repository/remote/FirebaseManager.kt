package ru.bruimafia.donotforget.repository.remote

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import ru.bruimafia.donotforget.repository.local.Note
import ru.bruimafia.donotforget.util.Constants


object FirebaseManager : FirebaseBase {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var collection: CollectionReference

    fun init() {
        firebaseAuth = FirebaseAuth.getInstance()
        Log.d(Constants.TAG, "From FirebaseManager::class -> auth ${firebaseAuth.currentUser?.uid}")
        if (firebaseAuth.currentUser != null)
            collection = Firebase.firestore.collection(firebaseAuth.currentUser!!.uid)
    }

    override fun getAll(): List<Note> {
        val notes: MutableList<Note> = ArrayList()

        if (firebaseAuth.currentUser != null) {
            collection.get().addOnSuccessListener { queryDocumentSnapshots ->
                val snapshotList: List<DocumentSnapshot> = queryDocumentSnapshots.documents
                for (document in snapshotList)
                    document.toObject<Note>()?.let {
                        notes.add(it)
                    }
            }
        }

        return notes
    }

    override fun putAll(notes: List<Note>) {
        if (firebaseAuth.currentUser != null) {
            for (note in notes) {
                collection.document(note.id.toString())
                    .set(note, SetOptions.merge())
                    .addOnSuccessListener {
                        Log.d(Constants.TAG, "From FirebaseManager::class -> DocumentSnapshot successfully create or update!")
                    }
            }
        }
    }

    override fun createOrUpdate(note: Note) {
        if (firebaseAuth.currentUser != null) {
            collection.document(note.id.toString())
                .set(note, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d(Constants.TAG, "From FirebaseManager::class -> DocumentSnapshot successfully create or update!")
                }

        }
    }

    override fun recover(id: Long) {
        if (firebaseAuth.currentUser != null) {
            collection.document(id.toString())
                .update("inHistory", false)
                .addOnSuccessListener {
                    Log.d(Constants.TAG, "From FirebaseManager::class -> DocumentSnapshot successfully recover!")
                }
        }
    }

    override fun delete(id: Long) {
        if (firebaseAuth.currentUser != null) {
            collection.document(id.toString())
                .update("inHistory", true)
                .addOnSuccessListener {
                    Log.d(Constants.TAG, "From FirebaseManager::class -> DocumentSnapshot successfully delete!")
                }
        }
    }

    override fun deleteAll() {
        if (firebaseAuth.currentUser != null) {
            collection.whereEqualTo("inHistory", false)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents.documents)
                        document.reference.delete()
                    Log.d(Constants.TAG, "From FirebaseManager::class -> DocumentSnapshot successfully clear!")
                }
        }
    }

    override fun clearHistory() {
        if (firebaseAuth.currentUser != null) {
            collection.whereEqualTo("inHistory", true)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents.documents)
                        document.reference.delete()
                    Log.d(Constants.TAG, "From FirebaseManager::class -> DocumentSnapshot successfully clearHistory!")
                }
        }
    }

}
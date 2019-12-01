package com.dawar.sparknetwork.ui.main.database

import com.dawar.sparknetwork.models.SmsRide
import com.dawar.sparknetwork.models.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AppDatabase private constructor() {

    private val database = FirebaseDatabase.getInstance()
    private val dbRootRef = database.reference
    private val usersNode = dbRootRef.child("Users")
    private val ridesNode by lazy { usersNode.child(userId).child("rides") }
    private lateinit var onUser: ((User?) -> Unit)
    private lateinit var userId: String

    private val valueEventListener = object : ValueEventListener {
        override fun onDataChange(p0: DataSnapshot) {
            val user = p0.child(userId).getValue(User::class.java)
            onUser(user)
            close()
        }

        override fun onCancelled(p0: DatabaseError) {
            onUser(null)
            close()
        }
    }

    fun saveUser(user: User, onSuccess: ((Boolean) -> Unit)? = null) {

        usersNode.child(user.id).setValue(user)
            .addOnCompleteListener {
                onSuccess?.invoke(it.isSuccessful)
            }
    }


    fun getUser(id: String, onUser: ((User?) -> Unit)) {
        this.onUser = onUser
        this.userId = id
        usersNode.addValueEventListener(valueEventListener)

    }

    fun createRideRequestBySms(smsRide: SmsRide, onSuccess: ((Boolean, String?) -> Unit)? = null) {
        // val ridesNode = usersNode.child(userId).child("rides")
        ridesNode.child("SmsRide").setValue(smsRide).addOnCompleteListener {
            onSuccess?.invoke(it.isSuccessful, it.exception?.message)
        }
    }

    private fun close() {
        usersNode.removeEventListener(valueEventListener)
    }

    companion object {
        private var appDatabase: AppDatabase? = null

        fun getInstance(): AppDatabase {
            if (appDatabase == null)
                appDatabase =
                    AppDatabase()
            return appDatabase!!
        }
    }

}
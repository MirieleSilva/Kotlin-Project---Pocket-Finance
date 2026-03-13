package com.app.pocketfinance

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class HomeActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val mainView = findViewById<LinearLayout>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        val firebaseDatabase = FirebaseDatabase.getInstance()
        try {
            firebaseDatabase.setPersistenceEnabled(true)
        } catch (e: Exception) {}

        database = firebaseDatabase.reference

        val btnAdd = findViewById<Button>(R.id.btnAddSpend)
        btnAdd.setOnClickListener {
            displayRegistrationDialog()
        }

        listenFirebaseExpenses()

        val btnLogout = findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, getString(R.string.btn_logout), Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun listenFirebaseExpenses() {
        val userId = auth.currentUser?.uid ?: return
        val txtLista = findViewById<TextView>(R.id.txtListSpend)
        val txtSaldo = findViewById<TextView>(R.id.txtBalance)

        database.child("usuarios").child(userId).child("gastos")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var total = 0.0
                    val listaFormatada = StringBuilder()

                    for (item in snapshot.children) {
                        val idGasto = item.key
                        val nome = item.child("nome").value.toString()
                        val valorString = item.child("valor").value.toString()
                        val valor = valorString.toDoubleOrNull() ?: 0.0

                        total += valor
                        listaFormatada.append("📌 $nome: R$ $valor\n")

                        txtLista.setOnLongClickListener {
                            confirmDeletion(idGasto, nome)
                            true
                        }
                    }

                    txtSaldo.text = "R$ %.2f".format(total)
                    txtLista.text = if (listaFormatada.isEmpty()) getString(R.string.no_spend) else listaFormatada.toString()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun confirmDeletion(idGasto: String?, nomeGasto: String) {
        if (idGasto == null) return

        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.confirmar_exclusao))
        builder.setMessage("Desejas apagar o gasto '$nomeGasto'?")

        builder.setPositiveButton("Sim, excluir") { _, _ ->
            val userId = auth.currentUser?.uid ?: return@setPositiveButton
            database.child("usuarios").child(userId).child("gastos").child(idGasto).removeValue()
                .addOnSuccessListener {
                    Toast.makeText(this, "Gasto removido!", Toast.LENGTH_SHORT).show()
                }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun displayRegistrationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.btn_add_expense))

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)

        val editNomeGasto = EditText(this)
        editNomeGasto.hint = getString(R.string.label_description)
        layout.addView(editNomeGasto)

        val editValorGasto = EditText(this)
        editValorGasto.hint = getString(R.string.label_value)
        editValorGasto.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        layout.addView(editValorGasto)

        builder.setView(layout)

        builder.setPositiveButton("Salvar") { _, _ ->
            val nome = editNomeGasto.text.toString()
            val valor = editValorGasto.text.toString()
            if (nome.isNotEmpty() && valor.isNotEmpty()) {
                firebaseSave(nome, valor)
            } else {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun firebaseSave(nome: String, valor: String) {
        val userId = auth.currentUser?.uid ?: return
        val gasto = mapOf("nome" to nome, "valor" to valor)

        database.child("usuarios").child(userId).child("gastos").push().setValue(gasto)
            .addOnSuccessListener {
                Toast.makeText(this, "Gasto salvo!", Toast.LENGTH_SHORT).show()
            }
    }
}
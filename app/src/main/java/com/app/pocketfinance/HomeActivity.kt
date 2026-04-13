package com.app.pocketfinance

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView

class HomeActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    private val PICK_IMAGE_REQUEST = 71
    private var filePath: Uri? = null
    private lateinit var imgUserPhoto: CircleImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        MobileAds.initialize(this) {}
        val adView = findViewById<com.google.android.gms.ads.AdView>(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        storage = FirebaseStorage.getInstance()

        imgUserPhoto = findViewById(R.id.imgUserPhoto)

        val mainView = findViewById<LinearLayout>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loadUserProfileData()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ListExpensesFragment())
                .commit()
        }

        imgUserPhoto.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, getString(R.string.title_select_photo)), PICK_IMAGE_REQUEST)
        }

        val btnAdd = findViewById<Button>(R.id.btnAddSpend)
        btnAdd.setOnClickListener {
            displayRegistrationDialog()
        }

        val btnLogout = findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, getString(R.string.msg_logout_success), Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun loadUserProfileData() {
        val userId = auth.currentUser?.uid ?: return
        database.child("usuarios").child(userId).child("urlFotoPerfil").get().addOnSuccessListener { snapshot ->
            val url = snapshot.value.toString()
            if (url.isNotEmpty() && url != "null") {
                Glide.with(this).load(url).into(imgUserPhoto)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            filePath = data.data
            imgUserPhoto.setImageURI(filePath)
            uploadImageToFirebase()
        }
    }

    private fun uploadImageToFirebase() {
        val userId = auth.currentUser?.uid ?: return
        val ref = storage.reference.child("perfis/$userId.jpg")

        filePath?.let { uri ->
            ref.putFile(uri)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { downloadUrl ->
                        database.child("usuarios").child(userId).child("urlFotoPerfil")
                            .setValue(downloadUrl.toString())
                        Toast.makeText(this, getString(R.string.msg_photo_updated), Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, getString(R.string.msg_photo_failed), Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun displayRegistrationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.btn_add_expense))

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val editNomeGasto = EditText(this).apply { hint = getString(R.string.hint_description) }
        val editValorGasto = EditText(this).apply {
            hint = getString(R.string.hint_value)
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        layout.addView(editNomeGasto)
        layout.addView(editValorGasto)
        builder.setView(layout)

        builder.setPositiveButton(getString(R.string.btn_save)) { _, _ ->
            val nome = editNomeGasto.text.toString()
            val valor = editValorGasto.text.toString()
            if (nome.isNotEmpty() && valor.isNotEmpty()) {
                firebaseSave(nome, valor)
            } else {
                Toast.makeText(this, getString(R.string.msg_fill_all_fields), Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton(getString(R.string.btn_cancel), null)
        builder.show()
    }

    private fun firebaseSave(nome: String, valor: String) {
        val userId = auth.currentUser?.uid ?: return
        val latitude = -10.0
        val longitude = -63.0

        val gasto = mapOf(
            "nome" to nome,
            "valor" to valor,
            "latitude" to latitude,
            "longitude" to longitude,
            "dataHora" to System.currentTimeMillis()
        )

        database.child("usuarios").child(userId).child("gastos").push().setValue(gasto)
            .addOnSuccessListener {
                Toast.makeText(this, getString(R.string.msg_save_success), Toast.LENGTH_SHORT).show()
            }
    }

    fun showEditDialog(id: String?, nomeAntigo: String, valorAntigo: String) {
        if (id == null) return
        val userId = auth.currentUser?.uid ?: return

        database.child("usuarios").child(userId).child("gastos").child(id).get().addOnSuccessListener { snapshot ->
            val lat = snapshot.child("latitude").value?.toString() ?: "0.0"
            val lon = snapshot.child("longitude").value?.toString() ?: "0.0"

            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.title_edit_expense))

            val layout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(50, 40, 50, 10)
            }

            val editNome = EditText(this).apply { setText(nomeAntigo) }
            val editValor = EditText(this).apply {
                setText(valorAntigo)
                inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            }

            val dialogEdicao = builder.create()

            val btnExcluir = Button(this).apply {
                text = getString(R.string.btn_delete)
                setBackgroundColor(android.graphics.Color.RED)
                setTextColor(android.graphics.Color.WHITE)
                setOnClickListener {
                    AlertDialog.Builder(this@HomeActivity)
                        .setTitle(getString(R.string.title_delete_confirm))
                        .setMessage(getString(R.string.msg_delete_confirm))
                        .setPositiveButton(getString(R.string.btn_yes)) { _, _ ->
                            database.child("usuarios").child(userId).child("gastos").child(id).removeValue()
                                .addOnSuccessListener {
                                    Toast.makeText(this@HomeActivity, getString(R.string.msg_delete_success), Toast.LENGTH_SHORT).show()

                                    dialogEdicao.dismiss()
                                }
                        }
                        .setNegativeButton(getString(R.string.btn_no), null)
                        .show()
                }
            }

            val btnMapa = Button(this).apply {
                text = getString(R.string.btn_view_maps)
                setOnClickListener {
                    val gmmIntentUri = Uri.parse("geo:$lat,$lon?q=$lat,$lon")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                        setPackage("com.google.android.apps.maps")
                    }
                    startActivity(mapIntent)
                }
            }

            layout.addView(editNome)
            layout.addView(editValor)
            layout.addView(btnExcluir)
            layout.addView(btnMapa)

            dialogEdicao.setView(layout)

            dialogEdicao.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.btn_update)) { _, _ ->
                val update = mapOf("nome" to editNome.text.toString(), "valor" to editValor.text.toString())
                database.child("usuarios").child(userId).child("gastos").child(id).updateChildren(update)
            }

            dialogEdicao.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.btn_cancel)) { d, _ ->
                d.dismiss()
            }

            dialogEdicao.show()
        }
    }
}
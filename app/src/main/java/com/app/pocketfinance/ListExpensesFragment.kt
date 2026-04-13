package com.app.pocketfinance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ListExpensesFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var txtLista: TextView
    private lateinit var txtSaldo: TextView
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://nominatim.openstreetmap.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val geocodingApi = retrofit.create(GeocodingApi::class.java)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_list_expenses, container, false)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        txtLista = view.findViewById(R.id.txtListSpendFragment)
        txtSaldo = view.findViewById(R.id.txtBalanceFragment)

        listenFirebaseExpenses()

        return view
    }

    private fun listenFirebaseExpenses() {
        val userId = auth.currentUser?.uid ?: return

        database.child("usuarios").child(userId).child("gastos")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var total = 0.0
                    txtLista.text = ""
                    val listaFormatada = StringBuilder()

                    if (!snapshot.hasChildren()) {

                        val msgVazio = context?.getString(R.string.msg_no_expenses) ?: "Nenhum gasto registrado"
                        val labelTotal = context?.getString(R.string.label_total) ?: "Total"

                        txtLista.text = msgVazio
                        txtSaldo.text = "$labelTotal: R$ 0.00"


                        txtLista.setOnClickListener(null)
                        return
                    }

                    for (item in snapshot.children) {
                        val idGasto = item.key
                        val nome = item.child("nome").value.toString()
                        val valorString = item.child("valor").value.toString()
                        val valor = valorString.toDoubleOrNull() ?: 0.0

                        val lat = item.child("latitude").value.toString().toDoubleOrNull() ?: 0.0
                        val lon = item.child("longitude").value.toString().toDoubleOrNull() ?: 0.0

                        total += valor

                        val labelLocal = context?.getString(R.string.location) ?: "Local"

                        updateLocationName(lat, lon) { enderecoReal ->
                            if (isAdded) {
                                listaFormatada.append("✏️ $nome: R$ $valor\n")
                                listaFormatada.append("📍 $labelLocal: $enderecoReal\n")
                                listaFormatada.append("----------------------------\n")
                                txtLista.text = listaFormatada.toString()
                            }
                        }

                        txtLista.setOnClickListener {
                            (activity as? HomeActivity)?.showEditDialog(idGasto, nome, valorString)
                        }
                    }
                    txtSaldo.text = "Total: R$ %.2f".format(total)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }
    private fun updateLocationName(lat: Double, lon: Double, callback: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = geocodingApi.getAddress(lat, lon)
                if (response.isSuccessful) {
                    val address = response.body()?.display_name ?: (getString(R.string.unknown_location))
                    val shortAddress = address.split(",").take(2).joinToString(",")
                    withContext(Dispatchers.Main) {
                        callback(shortAddress)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { callback("Lat: $lat, Lon: $lon") }
            }
        }
    }
}
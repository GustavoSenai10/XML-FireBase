package br.senai.jandira.sp.upload_firebase

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import br.senai.jandira.sp.upload_firebase.databinding.ActivityMainBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class MainActivity : AppCompatActivity() {

    //Atributos
    //Representação da classe de manipulação de objetos de view das telas
    private  lateinit var binding: ActivityMainBinding

    //Representação da classe de manipulação de endereço (Local) de arquivos
    private  var imageUri: Uri? = null

    //Referencia para acesso e manipulação do Cloud Storage
    private lateinit var storeRef: StorageReference


    //Referencia para acesso e manipulação do cloud FireStore
    private lateinit var firebaseFirestore: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initVars()
        registerClickEvents()
    }

    //Inicialização dos Atributos do FireBase
    private fun initVars(){
        storeRef = FirebaseStorage.getInstance().reference.child("imagens")
        firebaseFirestore = FirebaseFirestore.getInstance()
    }


    //Lançador de recursos externos da aplicação (Galeria de Imagens)
    private val resultLaucher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ){
        imageUri = it
        binding.imageView.setImageURI(it)
    }

    //Tratamento de evento click
    private fun registerClickEvents(){
        binding.imageView.setOnClickListener{
            resultLaucher.launch("image/*")
        }
        binding.uploadBtn.setOnClickListener {
            uploadImage()
        }

        binding.showAllBtn.setOnClickListener {
            startActivity(Intent(this, imagesFeed::class.java))
        }
    }

    //UPLAOD DE IMAGENS NO FIREBASE
    private fun uploadImage(){

        binding.progressBar.visibility= View.VISIBLE

        storeRef = storeRef.child(System.currentTimeMillis().toString())

        //Uplaod V1 - Inicio
//        imageUri?.let {
//
//            storeRef.putFile(it).addOnCompleteListener{
//
//                teks->
//
//                if (teks.isSuccessful){
//                    Toast.makeText(
//                        this,
//                        "UPLOAD REALIZADO COM SUCESSO!",
//                        Toast.LENGTH_LONG
//                    ).show()
//                }else{
//                    Toast.makeText(
//                        this,
//                        "HOUVE UM ERRO AO TENTAR REALIZAR O UPLOAD!",
//                        Toast.LENGTH_LONG
//                    ).show()
//                }
//                binding.progressBar.visibility= View.GONE
//            }
//        }
//        //Uplaod V1 - FIM


        //Uplaod V2 - Inicio
        imageUri?.let {
            storeRef.putFile(it).addOnCompleteListener { task->

                if (task.isSuccessful) {

                    storeRef.downloadUrl.addOnSuccessListener { uri ->

                        val map = HashMap<String, Any>()
                        map["pic"] = uri.toString()

                        firebaseFirestore.collection("images").add(map).addOnCompleteListener { firestoreTask ->

                            if (firestoreTask.isSuccessful){
                                Toast.makeText(this, "Uploaded Successfully", Toast.LENGTH_SHORT).show()

                            }else{
                                Toast.makeText(this, firestoreTask.exception?.message, Toast.LENGTH_SHORT).show()

                            }
                            binding.progressBar.visibility = View.GONE
                            binding.imageView.setImageResource(R.drawable.baseline_arrow_back_24)

                        }
                    }

                }else{

                    Toast.makeText(this,  task.exception?.message, Toast.LENGTH_SHORT).show()

                }

                //BARRA DE PROGRESSO DO UPLOAD
                binding.progressBar.visibility = View.GONE

                //TROCA A IMAGEM PARA A IMAGEM PADRÃO
                binding.imageView.setImageResource(R.drawable.baseline_arrow_back_24)

            }

         }
        ////Uplaod V2 - Fim
    }

}
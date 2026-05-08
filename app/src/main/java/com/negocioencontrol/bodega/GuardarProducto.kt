package com.negocioencontrol.bodega

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.toolbox.Volley
import org.json.JSONObject

fun guardarProducto(

    context: Context,

    nombreBD: String,

    productoEditar: ProductoBodega?,

    producto: String,

    descripcion: String,

    cantidad: String,

    precio: String,

    categoria: String,

    codigoBarra: String,

    stockInicial: String,

    imagenUri: Uri?,

    onSuccess: () -> Unit
) {

    val url =
        "https://elpollovolantuso.com/negocioencontrol/api/bodega_guardar.php"

    val request = object : VolleyMultipartRequest(

        Request.Method.POST,
        url,

        { response ->

            try {

                val json = JSONObject(
                    String(response.data)
                )

                val success =
                    json.getBoolean("success")

                Toast.makeText(
                    context,
                    json.getString("msg"),
                    Toast.LENGTH_SHORT
                ).show()

                if (success) {

                    onSuccess()
                }

            } catch (e: Exception) {

                e.printStackTrace()

                Toast.makeText(
                    context,
                    "Error JSON",
                    Toast.LENGTH_SHORT
                ).show()
            }
        },

        {

            Toast.makeText(
                context,
                "Error conexión",
                Toast.LENGTH_SHORT
            ).show()
        }

    ) {

        override fun getParams():
                MutableMap<String, String> {

            return hashMapOf(

                "nombre_bd" to nombreBD,

                "id" to
                        (productoEditar?.id ?: "0"),

                "id_producto" to
                        (productoEditar?.idProducto ?: "0"),

                "producto" to producto,

                "descripcion" to descripcion,

                "cantidad" to cantidad,

                "precio" to precio,

                "categoria" to categoria,

                "codigo_barra" to codigoBarra,

                "stock_inicial" to stockInicial
            )
        }

        override fun getByteData():
                MutableMap<String, DataPart> {

            val params =
                HashMap<String, DataPart>()

            if (imagenUri != null) {

                val inputStream =
                    context.contentResolver
                        .openInputStream(imagenUri)

                val bytes =
                    inputStream?.readBytes()

                if (bytes != null) {

                    params["imagen"] = DataPart(

                        "foto.jpg",

                        bytes,

                        "image/jpeg"
                    )
                }
            }

            return params
        }
    }

    Volley.newRequestQueue(context)
        .add(request)
}
// GuardarProducto.kt

package com.negocioencontrol.bodega

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.io.ByteArrayOutputStream

fun bitmapToBytes(bitmap: Bitmap): ByteArray {

    val stream = ByteArrayOutputStream()

    bitmap.compress(
        Bitmap.CompressFormat.JPEG,
        90,
        stream
    )

    return stream.toByteArray()
}

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

    imageBitmap: Bitmap?,

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

            it.printStackTrace()

            val mensaje =
                it.networkResponse?.data
                    ?.toString(Charsets.UTF_8)
                    ?: it.message
                    ?: "Error conexión"

            Toast.makeText(
                context,
                mensaje,
                Toast.LENGTH_LONG
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

            // =========================
            // FOTO CAMARA
            // =========================
            imageBitmap?.let {

                params["imagen"] = DataPart(

                    "producto.jpg",

                    bitmapToBytes(it),

                    "image/jpeg"
                )
            }

            // =========================
            // FOTO GALERIA
            // =========================
            imagenUri?.let { uri ->

                try {

                    val bytes =
                        context.contentResolver
                            .openInputStream(uri)
                            ?.use {
                                it.readBytes()
                            }

                    if (bytes != null) {

                        params["imagen"] = DataPart(

                            "producto.jpg",

                            bytes,

                            "image/jpeg"
                        )
                    }

                } catch (e: Exception) {

                    e.printStackTrace()

                    Toast.makeText(
                        context,
                        "Error imagen: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            return params
        }
    }

    Volley.newRequestQueue(context)
        .add(request)
}
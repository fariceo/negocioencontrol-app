package com.negocioencontrol.scanner

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.setPadding
import coil.load
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.journeyapps.barcodescanner.CaptureActivity
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import org.json.JSONObject
import android.media.AudioManager
import android.media.ToneGenerator


class CustomScannerActivity : CaptureActivity() {

    private lateinit var scannerView: DecoratedBarcodeView

    private var nombreBD: String = ""
    private var usuario: String = ""


    private var ultimoProducto: ProductoScanner? = null

    private lateinit var overlay: FrameLayout

    private var buscandoProducto = false

    private fun dp(valor: Int): Int {
        return (valor * resources.displayMetrics.density).toInt()
    }

    private var toneGenerator: ToneGenerator? = null

    /**
     * JourneyApps 4.3.0.
     *
     * CaptureActivity utiliza este método para crear
     * el DecoratedBarcodeView del scanner.
     *
     * De esta manera no necesitamos acceder directamente
     * al R.id.zxing_barcode_scanner.
     */
    override fun initializeContent(): DecoratedBarcodeView {

        val view =
            super.initializeContent()

        scannerView =
            view

        return view
    }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        nombreBD =
            intent.getStringExtra("nombre_bd") ?: ""

        usuario =
            intent.getStringExtra("usuario") ?: ""

        toneGenerator =
            ToneGenerator(
                AudioManager.STREAM_NOTIFICATION,
                100
            )

        crearInterfazScanner()

        iniciarLectura()
    }

    private fun crearInterfazScanner() {

        val root =
            findViewById<ViewGroup>(
                android.R.id.content
            )

        overlay =
            FrameLayout(this).apply {

                setPadding(
                    dp(12),
                    dp(8),
                    dp(12),
                    dp(8)
                )

                isClickable = false
                isFocusable = false
            }

        val overlayParams =
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

        root.addView(
            overlay,
            overlayParams
        )

        mostrarBotonCerrar()
    }

    private fun iniciarLectura() {

        scannerView.decodeContinuous { result ->

            if (buscandoProducto) {
                return@decodeContinuous
            }

            val codigo =
                result.text
                    ?.trim()
                    ?: ""

            if (codigo.isEmpty()) {
                return@decodeContinuous
            }

            toneGenerator?.startTone(
                ToneGenerator.TONE_PROP_BEEP,
                150
            )
            buscandoProducto = true

            scannerView.pause()

            buscarProducto(
                codigo
            )
        }
    }

    private fun buscarProducto(
        codigo: String
    ) {

        val url =
            "https://elpollovolantuso.com/negocioencontrol/api/buscar_producto_api.php"

        val request =
            object : StringRequest(

                Request.Method.POST,

                url,

                { response ->

                    try {

                        val json =
                            JSONObject(response)

                        val success =
                            json.optBoolean(
                                "success",
                                false
                            )

                        if (success) {

                            val obj =
                                json.getJSONObject(
                                    "producto"
                                )

                            val producto = ProductoScanner(
                                id = obj.optString("id_producto"),
                                codigo = obj.optString("codigo_barra"),
                                nombre = obj.optString("producto"),
                                precio = obj.optString("precio"),
                                stock = obj.optString("stock_inicial"),
                                imagen = obj.optString("imagen")
                            )

                            ultimoProducto =
                                producto

                            mostrarProducto(
                                producto
                            )

                            agregarAlCarrito(
                                producto
                            )

                        } else {

                            mostrarProductoNoEncontrado(
                                codigo
                            )
                        }

                    } catch (
                        e: Exception
                    ) {

                        mostrarProductoNoEncontrado(
                            codigo
                        )
                    }
                },

                {

                    mostrarProductoNoEncontrado(
                        codigo
                    )
                }
            ) {

                override fun getParams():
                        MutableMap<String, String> {

                    return hashMapOf(

                        "codigo" to codigo,

                        "nombre_bd" to nombreBD
                    )
                }
            }

        Volley
            .newRequestQueue(this)
            .add(request)
    }

    private fun mostrarProducto(
        producto: ProductoScanner
    ) {

        overlay.removeAllViews()

        val tarjeta =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                gravity =
                    Gravity.CENTER_VERTICAL

                setPadding(
                    14
                )

                background =
                    GradientDrawable().apply {

                        setColor(
                            Color.WHITE
                        )

                        cornerRadius =
                            28f
                    }

                elevation =
                    14f
            }

        val imagenView =
            ImageView(this).apply {

                scaleType =
                    ImageView.ScaleType.CENTER_CROP

                layoutParams =
                    LinearLayout.LayoutParams(
                        95,
                        95
                    )
            }

        val imagenFinal =
            if (
                producto.imagen
                    .startsWith("http")
            ) {

                producto.imagen

            } else {

                "https://elpollovolantuso.com/negocioencontrol/assets/images/${producto.imagen}"
            }

        if (
            producto.imagen.isNotEmpty()
        ) {

            imagenView.load(
                imagenFinal
            )
        }

        tarjeta.addView(
            imagenView
        )

        val info =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                layoutParams =
                    LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                    )

                setPadding(
                    14,
                    0,
                    4,
                    0
                )
            }

        val encontrado =
            TextView(this).apply {

                text =
                    "✓ Producto encontrado"

                textSize =
                    13f

                setTextColor(
                    Color.rgb(
                        22,
                        163,
                        74
                    )
                )
            }

        info.addView(
            encontrado
        )

        val nombre =
            TextView(this).apply {

                text =
                    producto.nombre

                textSize =
                    18f

                setTypeface(
                    null,
                    Typeface.BOLD
                )

                setTextColor(
                    Color.BLACK
                )

                maxLines =
                    2
            }

        info.addView(
            nombre
        )

        val stock =
            TextView(this).apply {

                text =
                    "Stock: ${producto.stock}"

                textSize =
                    14f

                setTextColor(
                    Color.DKGRAY
                )
            }

        info.addView(
            stock
        )

        val precio =
            TextView(this).apply {

                text =
                    "$${formatearPrecio(producto.precio)}"

                textSize =
                    18f

                setTypeface(
                    null,
                    Typeface.BOLD
                )

                setTextColor(
                    Color.rgb(
                        5,
                        150,
                        105
                    )
                )
            }

        info.addView(
            precio
        )

        tarjeta.addView(
            info
        )

        val tarjetaParams =
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {

                gravity =
                    Gravity.BOTTOM

                bottomMargin =
                    150
            }

        overlay.addView(
            tarjeta,
            tarjetaParams
        )

        val continuar =
            crearBoton(
                "📷  Seguir escaneando"
            ) {
                overlay.removeAllViews()

                buscandoProducto = false

                mostrarBotonCerrar()

                scannerView.resume()
            }.apply {
                textSize = 19f
                setTypeface(
                    null,
                    Typeface.BOLD
                )
            }


        val continuarParams =
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(76)
            ).apply {
                gravity = Gravity.BOTTOM
                bottomMargin = dp(95)
                leftMargin = dp(8)
                rightMargin = dp(8)
            }

        overlay.addView(
            continuar,
            continuarParams
        )

        mostrarBotonCerrar()
    }

    private fun agregarAlCarrito(producto: ProductoScanner) {

        val url =
            "https://elpollovolantuso.com/negocioencontrol/api/agregar_carrito_api.php"

        val queue = Volley.newRequestQueue(this)

        val request = object : StringRequest(
            Method.POST,
            url,

            { response ->

                try {

                    val json = JSONObject(response)

                    val ok = json.optBoolean("ok", false)
                    val mensaje = json.optString("msg", "Sin mensaje")

                    if (!ok) {

                        android.widget.Toast.makeText(
                            this,
                            "No se pudo agregar: $mensaje",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }

                } catch (e: Exception) {

                    e.printStackTrace()

                    android.widget.Toast.makeText(
                        this,
                        "Respuesta inválida del servidor:\n$response",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            },

            { error ->

                error.printStackTrace()

                val respuesta =
                    error.networkResponse?.data?.toString(Charsets.UTF_8)

                android.widget.Toast.makeText(
                    this,
                    "Error al agregar al carrito:\n${respuesta ?: error.message}",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        ) {

            override fun getParams(): MutableMap<String, String> {

                return hashMapOf(

                    "nombre_bd" to nombreBD,

                    "usuario" to usuario,

                    "id_producto" to producto.id,

                    "cantidad" to "1"
                )
            }
        }

        queue.add(request)
    }


    private fun mostrarProductoNoEncontrado(codigo: String) {

        overlay.removeAllViews()

        // =========================
        // TARJETA PRODUCTO NO ENCONTRADO
        // =========================

        val tarjeta = LinearLayout(this).apply {

            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL

            setPadding(
                dp(20),
                dp(20),
                dp(20),
                dp(20)
            )

            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(28).toFloat()
            }

            elevation = dp(14).toFloat()
        }

        val titulo = TextView(this).apply {

            text = "⚠️ Producto no encontrado"

            textSize = 20f

            setTypeface(
                null,
                Typeface.BOLD
            )

            setTextColor(
                Color.rgb(220, 38, 38)
            )

            gravity = Gravity.CENTER
        }

        tarjeta.addView(titulo)

        val codigoText = TextView(this).apply {

            text = "Código: $codigo"

            textSize = 15f

            setTextColor(Color.DKGRAY)

            gravity = Gravity.CENTER

            setPadding(
                0,
                dp(10),
                0,
                dp(10)
            )
        }

        tarjeta.addView(codigoText)

        // =========================
        // TARJETA
        // =========================

        val tarjetaParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {

            gravity = Gravity.BOTTOM

            bottomMargin = dp(260)

            leftMargin = dp(10)

            rightMargin = dp(10)
        }

        overlay.addView(
            tarjeta,
            tarjetaParams
        )

        // =========================
        // AGREGAR PRODUCTO
        // =========================

        val agregar = crearBoton(
            "➕  Agregar producto"
        ) {

            val intent = Intent(
                this@CustomScannerActivity,
                com.negocioencontrol.subir_productos.SubirProductosActivity::class.java
            )

            intent.putExtra(
                "codigo_barra",
                codigo
            )

            intent.putExtra(
                "nombre_bd",
                nombreBD
            )

            startActivity(intent)

        }.apply {

            textSize = 18f

            setTypeface(
                null,
                Typeface.BOLD
            )
        }

        val agregarParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(64)
        ).apply {

            gravity = Gravity.BOTTOM

            bottomMargin = dp(180)

            leftMargin = dp(10)

            rightMargin = dp(10)
        }

        overlay.addView(
            agregar,
            agregarParams
        )

        // =========================
        // SEGUIR ESCANEANDO
        // =========================

        val seguir = crearBoton(
            "📷  Seguir escaneando"
        ) {

            overlay.removeAllViews()

            buscandoProducto = false

            mostrarBotonCerrar()

            scannerView.resume()

        }.apply {

            textSize = 18f

            setTypeface(
                null,
                Typeface.BOLD
            )
        }

        val seguirParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(64)
        ).apply {

            gravity = Gravity.BOTTOM

            bottomMargin = dp(95)

            leftMargin = dp(10)

            rightMargin = dp(10)
        }

        overlay.addView(
            seguir,
            seguirParams
        )

        // =========================
        // CERRAR SCANNER
        // =========================

        mostrarBotonCerrar()
    }

    private fun mostrarBotonCerrar() {

        val cerrar =
            crearBoton(
                "✕  Cerrar scanner"
            ) {

                finish()
            }

        val parametros =
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(64)
            ).apply {

                gravity =
                    Gravity.BOTTOM

                bottomMargin = dp(16)
            }

        overlay.addView(
            cerrar,
            parametros
        )
    }

    private fun crearBoton(
        texto: String,
        accion: () -> Unit
    ): Button {

        return Button(this).apply {

            text =
                texto

            textSize =
                17f

            setOnClickListener {

                accion()
            }
        }
    }

    private fun formatearPrecio(
        valor: String
    ): String {

        return try {

            "%.2f".format(
                valor.toDoubleOrNull()
                    ?: 0.0
            )

        } catch (
            e: Exception
        ) {

            "0.00"
        }
    }

    data class ProductoScanner(

        val id: String,

        val codigo: String,

        val nombre: String,

        val precio: String,

        val stock: String,

        val imagen: String
    )

    override fun onDestroy() {

        toneGenerator?.release()

        toneGenerator = null

        super.onDestroy()
    }
}


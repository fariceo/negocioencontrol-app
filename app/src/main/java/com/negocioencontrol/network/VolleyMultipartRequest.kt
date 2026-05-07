package com.negocioencontrol.network

import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

abstract class VolleyMultipartRequest(
    method: Int,
    url: String,
    private val mListener: Response.Listener<NetworkResponse>,
    errorListener: Response.ErrorListener
) : Request<NetworkResponse>(method, url, errorListener) {

    private val boundary = "apiclient-" + System.currentTimeMillis()
    private val lineEnd = "\r\n"
    private val twoHyphens = "--"

    override fun getBodyContentType(): String {
        return "multipart/form-data; boundary=$boundary"
    }

    override fun parseNetworkResponse(response: NetworkResponse): Response<NetworkResponse> {
        return Response.success(response, HttpHeaderParser.parseCacheHeaders(response))
    }

    override fun deliverResponse(response: NetworkResponse) {
        mListener.onResponse(response)
    }

    override fun getHeaders(): MutableMap<String, String> {
        return hashMapOf(
            "Connection" to "keep-alive"
        )
    }

    // 🔥 USAR ESTE en lugar de getParams()
    open fun getParamsMultipart(): Map<String, String> = emptyMap()

    // 🔥 PARA ARCHIVOS
    open fun getByteData(): Map<String, DataPart> = emptyMap()

    data class DataPart(
        val fileName: String,
        val content: ByteArray,
        val type: String
    )

    override fun getBody(): ByteArray {
        val bos = ByteArrayOutputStream()
        val dos = DataOutputStream(bos)

        try {

            // 🔥 PARAMS
            val params = getParamsMultipart()
            for ((key, value) in params) {
                dos.writeBytes(twoHyphens + boundary + lineEnd)
                dos.writeBytes("Content-Disposition: form-data; name=\"$key\"$lineEnd")
                dos.writeBytes("Content-Type: text/plain; charset=UTF-8$lineEnd")
                dos.writeBytes(lineEnd)
                dos.writeBytes(value + lineEnd)
            }

            // 🔥 FILES
            val data = getByteData()
            for ((key, dataPart) in data) {
                dos.writeBytes(twoHyphens + boundary + lineEnd)
                dos.writeBytes(
                    "Content-Disposition: form-data; name=\"$key\"; filename=\"${dataPart.fileName}\"$lineEnd"
                )
                dos.writeBytes("Content-Type: ${dataPart.type}$lineEnd")
                dos.writeBytes("Content-Transfer-Encoding: binary$lineEnd") // 🔥 ESTA LÍNEA
                dos.writeBytes(lineEnd)

                dos.write(dataPart.content)
                dos.writeBytes(lineEnd)
            }

            // 🔥 END
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd)

            dos.flush()

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return bos.toByteArray()
    }
}
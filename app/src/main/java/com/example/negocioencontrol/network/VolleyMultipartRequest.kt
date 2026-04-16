package com.example.negocioencontrol.network

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

    override fun getBodyContentType(): String {
        return "multipart/form-data;boundary=$boundary"
    }

    override fun parseNetworkResponse(response: NetworkResponse): Response<NetworkResponse> {
        return Response.success(response, HttpHeaderParser.parseCacheHeaders(response))
    }

    override fun deliverResponse(response: NetworkResponse) {
        mListener.onResponse(response)
    }

    override fun getBody(): ByteArray {
        val bos = ByteArrayOutputStream()
        val dos = DataOutputStream(bos)

        try {
            // PARAMS
            val params = params
            if (params != null) {
                for ((key, value) in params) {
                    writeFormField(dos, key, value)
                }
            }

            // FILES
            val data = getByteData()
            if (data != null) {
                for ((key, value) in data) {
                    writeFileField(dos, key, value)
                }
            }

            dos.writeBytes("--$boundary--\r\n")

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return bos.toByteArray()
    }

    // 👇 IMPORTANTE
    open fun getByteData(): Map<String, DataPart>? = null

    data class DataPart(
        val fileName: String,
        val content: ByteArray,
        val type: String
    )

    private fun writeFormField(dos: DataOutputStream, name: String, value: String) {
        dos.writeBytes("--$boundary\r\n")
        dos.writeBytes("Content-Disposition: form-data; name=\"$name\"\r\n\r\n")
        dos.writeBytes("$value\r\n")
    }

    private fun writeFileField(dos: DataOutputStream, name: String, data: DataPart) {
        dos.writeBytes("--$boundary\r\n")
        dos.writeBytes("Content-Disposition: form-data; name=\"$name\"; filename=\"${data.fileName}\"\r\n")
        dos.writeBytes("Content-Type: ${data.type}\r\n\r\n")
        dos.write(data.content)
        dos.writeBytes("\r\n")
    }
}
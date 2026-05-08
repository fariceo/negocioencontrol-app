package com.negocioencontrol.bodega

import com.android.volley.AuthFailureError
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.IOException
import java.util.UUID

abstract class VolleyMultipartRequest(

    method: Int,

    url: String,

    private val mListener: Response.Listener<NetworkResponse>,

    errorListener: Response.ErrorListener

) : Request<NetworkResponse>(
    method,
    url,
    errorListener
) {

    private val boundary =
        "apiclient-" + UUID.randomUUID()

    private val mimeType =
        "multipart/form-data;boundary=$boundary"

    override fun getBodyContentType(): String {
        return mimeType
    }

    @Throws(AuthFailureError::class)
    override fun getBody(): ByteArray {

        val bos = ByteArrayOutputStream()

        val dos = DataOutputStream(bos)

        try {

            // =========================
            // TEXT PARAMS
            // =========================
            val params = params

            if (params != null &&
                params.isNotEmpty()
            ) {

                textParse(
                    dos,
                    params,
                    "utf-8"
                )
            }

            // =========================
            // FILE PARAMS
            // =========================
            val data =
                getByteData()

            if (data.isNotEmpty()) {

                dataParse(dos, data)
            }

            dos.writeBytes("--$boundary--\r\n")

        } catch (e: IOException) {

            e.printStackTrace()
        }

        return bos.toByteArray()
    }

    override fun parseNetworkResponse(
        response: NetworkResponse
    ): Response<NetworkResponse> {

        return Response.success(
            response,
            HttpHeaderParser.parseCacheHeaders(response)
        )
    }

    override fun deliverResponse(
        response: NetworkResponse
    ) {

        mListener.onResponse(response)
    }

    abstract fun getByteData():
            MutableMap<String, DataPart>

    @Throws(IOException::class)
    private fun textParse(

        dataOutputStream: DataOutputStream,

        params: Map<String, String>,

        encoding: String
    ){

        try {

            for ((key, value) in params) {

                buildTextPart(
                    dataOutputStream,
                    key,
                    value
                )
            }

        } catch (e: Exception) {

            throw IOException(e)
        }
    }

    @Throws(IOException::class)
    private fun dataParse(

        dataOutputStream: DataOutputStream,

        data: Map<String, DataPart>

    ) {

        for ((key, value) in data) {

            buildDataPart(
                dataOutputStream,
                value,
                key
            )
        }
    }

    @Throws(IOException::class)
    private fun buildTextPart(

        dataOutputStream: DataOutputStream,

        parameterName: String,

        parameterValue: String

    ) {

        dataOutputStream.writeBytes("--$boundary\r\n")

        dataOutputStream.writeBytes(
            "Content-Disposition: form-data; " +
                    "name=\"$parameterName\"\r\n\r\n"
        )

        dataOutputStream.writeBytes(
            "$parameterValue\r\n"
        )
    }

    @Throws(IOException::class)
    private fun buildDataPart(

        dataOutputStream: DataOutputStream,

        dataFile: DataPart,

        inputName: String

    ) {

        dataOutputStream.writeBytes("--$boundary\r\n")

        dataOutputStream.writeBytes(
            "Content-Disposition: form-data; " +
                    "name=\"$inputName\"; " +
                    "filename=\"" +
                    dataFile.fileName +
                    "\"\r\n"
        )

        if (dataFile.type != null &&
            dataFile.type.trim().isNotEmpty()
        ) {

            dataOutputStream.writeBytes(
                "Content-Type: " +
                        dataFile.type +
                        "\r\n"
            )
        }

        dataOutputStream.writeBytes("\r\n")

        val fileInput =
            dataFile.content

        dataOutputStream.write(fileInput)

        dataOutputStream.writeBytes("\r\n")
    }

    class DataPart(

        val fileName: String,

        val content: ByteArray,

        val type: String
    )
}
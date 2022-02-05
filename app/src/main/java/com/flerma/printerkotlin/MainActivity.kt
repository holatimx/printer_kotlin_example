package com.flerma.printerkotlin

import android.app.AlertDialog
import android.content.Intent
import android.device.PrinterManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    private var mPrintHandler: Handler? = null
    private var PRINT_TEXT = 0 //Printed text
    private val PRINT_BITMAP = 1 //print pictures
    private val PRINT_BARCOD = 2 //Print bar code
    private val PRINT_FORWARD = 3 //Forward (paper feed)


    var printInfo //Info to print
            : EditText? = null
    var mBtnPrnText //Printed text
            : Button? = null
    var mBtnPrnBitmap //print pictures
            : Button? = null
    var mBtnPrnBarcode //Print bar code
            : Button? = null
    var mBtnForWard //Forward (paper feed)
            : Button? = null
    var mBtnPrnTicket //Printed text
            : Button? = null

    var mPrinterManager: PrinterManager? = null

    private var mFontStylePanel
            : FontStylePanel? = null

    private val mBarTypeTable = arrayOf(
        "3", "20", "25",
        "29", "34", "55", "58",
        "71", "84", "92"
    )

    private var mBarcodeTypeValue //Barcode Type
            = 0

    private val DEF_PRINTER_HUE_VALUE = 0



    private val PRNSTS_OK = 0 //OK

    private val PRNSTS_OUT_OF_PAPER = -1 //Out of paper

    private val PRNSTS_OVER_HEAT = -2 //Over heat

    private val PRNSTS_UNDER_VOLTAGE = -3 //under voltage

    private val PRNSTS_BUSY = -4 //Device is busy

    private val PRNSTS_ERR = -256 //Common error

    private val PRNSTS_ERR_DRIVER = -257 //Printer Driver error

    var mPrinterHue = DEF_PRINTER_HUE_VALUE
    var mPrinterSpeed = 0

    private val PHOTO_REQUEST_CODE = 200

    override fun onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy()
        if (mPrinterManager != null) {
            mPrinterManager!!.close()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        thread(start = true) {
            Looper.prepare()
            mPrintHandler = object : Handler() {
                override fun handleMessage(msg: Message) {
                    try {
                        when (msg.what) {
                            PRINT_TEXT, PRINT_BITMAP, PRINT_BARCOD -> doPrint(
                                getPrinterManager()!!,
                                msg.what,
                                msg.obj
                            ) //Print
                            PRINT_FORWARD -> {
                                getPrinterManager()!!.paperFeed(30)
                                updatePrintStatus(100)
                            }
                        }
                    } catch (e: RuntimeException) {
                    }
                }
            }
            Looper.loop()
        }

        mBtnPrnText = findViewById(R.id.btn_prnBill) as Button
        printInfo = findViewById(R.id.printer_info) as EditText

        //****************TEXT****************
        mBtnPrnText!!.setOnClickListener(View.OnClickListener {
            var content = printInfo!!.getText().toString()
            if (content == null || content == "") {
                content = """
            Print test content!
            0123456789
            abcdefghijklmnopqrstuvwsyz
            ABCDEFGHIJKLMNOPQRSTUVWSYZ
            """.trimIndent()
            }
            val msg = mPrintHandler!!.obtainMessage(PRINT_TEXT)
            msg.obj = content
            msg.sendToTarget()
        })

        //****************FORWARD****************
        mBtnForWard = findViewById(R.id.btn_FORWARD) as Button
        mBtnForWard!!.setOnClickListener(View.OnClickListener {
            mPrintHandler!!.obtainMessage(PRINT_FORWARD).sendToTarget()
        })

        //****************BITMAP****************
        mBtnPrnBitmap = findViewById(R.id.btn_prnPicture) as Button
        mBtnPrnBitmap!!.setOnClickListener(View.OnClickListener {
            AlertDialog.Builder(this)
                .setTitle(R.string.tst_info_select_picture)
                .setMessage(R.string.tst_info_select_picture_msg)
                .setNegativeButton(
                    R.string.mci_select_cancel
                ) { dialog, which ->
                    val intent =
                        Intent(
                            Intent.ACTION_OPEN_DOCUMENT,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        )
                    startActivityForResult(
                        intent,
                        PHOTO_REQUEST_CODE
                    )
                }
                .setPositiveButton(
                    R.string.mci_select_ok
                ) { dialog, which ->
                    val opts = BitmapFactory.Options()
                    opts.inPreferredConfig = Bitmap.Config.ARGB_8888
                    opts.inDensity = resources.displayMetrics.densityDpi
                    opts.inTargetDensity = resources.displayMetrics.densityDpi
                    val bitmap = BitmapFactory.decodeResource(resources, R.drawable.hcp, opts)
                    val msg = mPrintHandler!!.obtainMessage(PRINT_BITMAP)
                    msg.obj = bitmap
                    msg.sendToTarget()
                }
                .create()
                .show()
        })

        //****************BARCODE****************
        mBtnPrnBarcode = findViewById(R.id.btn_barcode) as Button
        mBtnPrnBarcode!!.setOnClickListener(View.OnClickListener {
            val messgae = printInfo!!.getText().toString()
            if (messgae.length > 0) {
                val msg = mPrintHandler!!.obtainMessage(PRINT_BARCOD)
                msg.obj = messgae
                msg.sendToTarget()
            } else {
                Toast.makeText(
                    this, R.string.tst_hint_content, Toast.LENGTH_SHORT
                ).show()
            }
        })


        mBtnPrnTicket = findViewById(R.id.btn_prnTicket) as Button

        //****************TICKET****************
        mBtnPrnTicket!!.setOnClickListener(View.OnClickListener {
            val opts = BitmapFactory.Options()
            opts.inPreferredConfig = Bitmap.Config.ARGB_8888
            opts.inDensity = resources.displayMetrics.densityDpi
            opts.inTargetDensity = resources.displayMetrics.densityDpi
            val bitmap =
                BitmapFactory.decodeResource(resources, R.drawable.ticket_example, opts)
            val msg = mPrintHandler!!.obtainMessage(PRINT_BITMAP)
            msg.obj = bitmap
            msg.sendToTarget()
        })

        mFontStylePanel = FontStylePanel()
    }

    private fun getPrinterManager(): PrinterManager? {
        try {
            if (mPrinterManager == null) {
                mPrinterManager = PrinterManager()
                mPrinterManager?.open()
            }
            return mPrinterManager
        }catch (e: RuntimeException) {
            mPrinterManager = null
            Toast.makeText(this, "Dispositivo android sin impresora", Toast.LENGTH_SHORT).show()
        }
        return null
    }


    private fun doPrint(printerManager: PrinterManager, type: Int, content: Any) {
        var ret = printerManager.status
        if (ret == PRNSTS_OK) {
            printerManager.setupPage(384, -1) //Set paper size
            when (type) {
                PRINT_TEXT -> {
                    val fontInfo = mFontStylePanel!!.getFontInfo() //Get font format
                    var fontSize = 24
                    var fontStyle = 0x0000
                    var fontName: String? = "simsun"
                    if (fontInfo != null) {
                        fontSize = fontInfo.getInt("font-size", 24)
                        fontStyle = fontInfo.getInt("font-style", 0)
                        fontName = fontInfo.getString("font-name", "simsun")
                    }
                    var height = 0
                    val texts = (content as String).split("\n")
                        .toTypedArray() //Split print content into multiple lines
                    for (text in texts) {
                        height += printerManager.drawText(
                            text,
                            0,
                            height,
                            fontName,
                            fontSize,
                            false,
                            false,
                            0
                        ) //Printed text
                    }
                    for (text in texts) {
                        height += printerManager.drawTextEx(
                            text,
                            5,
                            height,
                            384,
                            -1,
                            fontName,
                            fontSize,
                            0,
                            fontStyle,
                            0
                        ) ////Printed text
                    }
                    height = 0
                }
                PRINT_BARCOD -> {
                    val text = content as String
                    Log.i("mPrinterManager", "----------- text ---------- $text")
                    when (mBarcodeTypeValue) {
                        20, 25 -> if (text.matches(Regex("^[A-Za-z0-9]+$"))) {
                            printerManager.drawBarcode(
                                text,
                                196,
                                300,
                                mBarcodeTypeValue,
                                2,
                                70,
                                0
                            ) //Print bar code
                        } else {
                            Toast.makeText(
                                this.applicationContext,
                                "Not support for Chinese code!!!",
                                Toast.LENGTH_SHORT
                            ).show()
                            printInfo!!.requestFocus()
                            updatePrintStatus(ret)
                            return
                        }
                        34 ->                             //case 2:// Chinese25MATRIX, no.
                            if (isNumeric(text)) {
                                printerManager.drawBarcode(
                                    text,
                                    196,
                                    300,
                                    mBarcodeTypeValue,
                                    2,
                                    70,
                                    0
                                ) //Print bar code
                            } else {
                                Toast.makeText(
                                    this.applicationContext,
                                    "Not support for non-numeric!!!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                printInfo!!.requestFocus()
                                updatePrintStatus(ret)
                                return
                            }
                        3, 29 -> if (isNumeric(text)) {
                            printerManager.drawBarcode(
                                text,
                                50,
                                10,
                                mBarcodeTypeValue,
                                2,
                                40,
                                0
                            ) //Print bar code
                        } else {
                            Toast.makeText(
                                applicationContext,
                                "Not support for non-numeric!!!",
                                Toast.LENGTH_SHORT
                            ).show()
                            printInfo!!.requestFocus()
                            updatePrintStatus(ret)
                            return
                        }
                        55 -> printerManager.drawBarcode(
                            text,
                            25,
                            5,
                            mBarcodeTypeValue,
                            3,
                            60,
                            0
                        ) //Print bar code
                        58, 71 -> printerManager.drawBarcode(
                            text,
                            50,
                            10,
                            mBarcodeTypeValue,
                            8,
                            120,
                            0
                        ) //Print bar code
                        84 -> printerManager.drawBarcode(
                            text,
                            25,
                            5,
                            mBarcodeTypeValue,
                            4,
                            60,
                            0
                        ) //Print bar code
                        92 -> printerManager.drawBarcode(
                            text,
                            50,
                            10,
                            mBarcodeTypeValue,
                            8,
                            120,
                            0
                        ) //Print bar code
                    }
                }
                PRINT_BITMAP -> {
                    val bitmap = content as Bitmap
                    if (bitmap != null) {
                        //x era 30
                        printerManager.drawBitmap(bitmap, 0, 0) //print pictures
                    } else {
                        Toast.makeText(this, "Picture is null", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            ret = printerManager.printPage(0) //Execution printing
            printerManager.paperFeed(16) //paper feed
        }
        updatePrintStatus(ret)
    }

    private fun updatePrintStatus(status: Int) {
        runOnUiThread {
            if (status == PRNSTS_OUT_OF_PAPER) {
                Toast.makeText(
                    this,
                    R.string.tst_info_paper,
                    Toast.LENGTH_SHORT
                ).show()
            } else if (status == PRNSTS_OVER_HEAT) {
                Toast.makeText(
                    this,
                    R.string.tst_info_temperature,
                    Toast.LENGTH_SHORT
                ).show()
            } else if (status == PRNSTS_UNDER_VOLTAGE) {
                Toast.makeText(
                    this,
                    R.string.tst_info_voltage,
                    Toast.LENGTH_SHORT
                ).show()
            } else if (status == PRNSTS_BUSY) {
                Toast.makeText(
                    this,
                    R.string.tst_info_busy,
                    Toast.LENGTH_SHORT
                ).show()
            } else if (status == PRNSTS_ERR) {
                Toast.makeText(
                    this,
                    R.string.tst_info_error,
                    Toast.LENGTH_SHORT
                ).show()
            } else if (status == PRNSTS_ERR_DRIVER) {
                Toast.makeText(
                    this,
                    R.string.tst_info_driver_error,
                    Toast.LENGTH_SHORT
                ).show()
            }
            mBtnPrnBarcode!!.isEnabled = true
            mBtnForWard!!.isEnabled = true
            mBtnPrnText!!.isEnabled = true
            mBtnPrnBitmap!!.isEnabled = true
        }
    }

    fun isNumeric(string: String?): Boolean {
        return if (string != null && string != "" && string.matches(Regex("\\d*"))) {
            if (Int.MAX_VALUE.toString().length < string.length) {
                false
            } else true
        } else {
            false
        }
    }
}
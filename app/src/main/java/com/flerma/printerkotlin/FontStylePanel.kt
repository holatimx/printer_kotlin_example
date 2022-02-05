package com.flerma.printerkotlin

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner

class FontStylePanel {

    private val FONT_NAME = "font-name"
    private val FONT_SIZE = "font-size"
    private val FONT_STYLE = "font-style"

    private val FONT_STYLE_NULL = 0x0000
    private val FONT_STYLE_BOLD = 0x0001
    private val FONT_STYLE_ITALIC = 0x0002
    private val FONT_STYLE_UNDERLINE = 0x0004
    private val FONT_STYLE_REVERSE = 0x0008
    private val FONT_STYLE_STRIKEOUT = 0x0010

    private var mContext: Activity? = null
    private var mNameSpinner: Spinner? = null
    private var mSizeSpinner: Spinner? = null
    private var mBoldButton: ImageButton? = null
    private var mItalicButton: ImageButton? = null
    private var mUnderlineButton: ImageButton? = null
    private var mStrikeoutButton: ImageButton? = null

    private var mFontName = "simsun"
    private var mFontSize = 24

    private var isTextBold = false
    private var isTextItalic = false
    private var isTextUnderline = false
    private var isTextStrikeout = false
    private var mFontStyle = FONT_STYLE_NULL

    private var mFontNames: Array<String> = arrayOf()
    private var mFontSizes: Array<String> = arrayOf()

    fun FontStylePanel(context: Activity?) {
        mContext = context
        mFontNames = mContext!!.resources.getStringArray(R.array.font_name)
        mFontSizes = mContext!!.resources.getStringArray(R.array.font_size)
        mNameSpinner = mContext!!.findViewById<View>(R.id.spinner_font) as Spinner
        val mNameAdapter: ArrayAdapter<*> =
            ArrayAdapter(mContext!!, android.R.layout.simple_spinner_item, mFontNames)
        mNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mNameSpinner!!.adapter = mNameAdapter
        mNameSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, i: Int, l: Long) {
                try {
                    mFontName = mFontNames[i]
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
        mSizeSpinner = mContext!!.findViewById<View>(R.id.spinner_size) as Spinner
        val mSizeAdapter: ArrayAdapter<*> =
            ArrayAdapter(mContext!!, android.R.layout.simple_spinner_item, mFontSizes)
        mSizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mSizeSpinner!!.adapter = mSizeAdapter
        mSizeSpinner!!.setSelection(4)
        mSizeSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, i: Int, l: Long) {
                try {
                    mFontSize = mFontSizes[i].toInt()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
        mBoldButton = mContext!!.findViewById<View>(R.id.btn_Bold) as ImageButton
        mBoldButton!!.setOnClickListener {
            if (isTextBold) {
                mBoldButton!!.setBackgroundResource(R.drawable.bold)
            } else {
                mBoldButton!!.setBackgroundResource(R.drawable.bold_)
            }
            isTextBold = !isTextBold
        }
        mItalicButton = mContext!!.findViewById<View>(R.id.btn_Italic) as ImageButton
        mItalicButton!!.setOnClickListener {
            if (isTextItalic) {
                mItalicButton!!.setBackgroundResource(R.drawable.italic)
            } else {
                mItalicButton!!.setBackgroundResource(R.drawable.italic_)
            }
            isTextItalic = !isTextItalic
        }
        mUnderlineButton = mContext!!.findViewById<View>(R.id.btn_Underline) as ImageButton
        mUnderlineButton!!.setOnClickListener {
            if (isTextUnderline) {
                mUnderlineButton!!.setBackgroundResource(R.drawable.underline)
            } else {
                mUnderlineButton!!.setBackgroundResource(R.drawable.underline_)
            }
            isTextUnderline = !isTextUnderline
        }
        mStrikeoutButton = mContext!!.findViewById<View>(R.id.btn_Strikeout) as ImageButton
        mStrikeoutButton!!.setOnClickListener {
            if (isTextStrikeout) {
                mStrikeoutButton!!.setBackgroundResource(R.drawable.strikeout)
            } else {
                mStrikeoutButton!!.setBackgroundResource(R.drawable.strikeout_)
            }
            isTextStrikeout = !isTextStrikeout
        }
    }

    fun getFontInfo(): Bundle? {
        mFontStyle = FONT_STYLE_NULL
        if (isTextBold) {
            mFontStyle = mFontStyle or FONT_STYLE_BOLD
        }
        if (isTextItalic) {
            mFontStyle = mFontStyle or FONT_STYLE_ITALIC
        }
        if (isTextUnderline) {
            mFontStyle = mFontStyle or FONT_STYLE_UNDERLINE
        }
        if (isTextStrikeout) {
            mFontStyle = mFontStyle or FONT_STYLE_STRIKEOUT
        }
        val fontInfo = Bundle()
        fontInfo.putString(FONT_NAME, mFontName)
        fontInfo.putInt(FONT_SIZE, mFontSize)
        fontInfo.putInt(FONT_STYLE, mFontStyle)
        return fontInfo
    }

}
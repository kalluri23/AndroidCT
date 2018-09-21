package com.verizon.contenttransfer.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.widget.EditText;

import com.verizon.contenttransfer.R;


/**
 * Created by kumpr8t on 12/6/2016.
 */

public class VerificationCodeEditText extends EditText implements TextWatcher, View.OnTouchListener {
    private int position;

    public VerificationCodeEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setOnTouchListener(this);
        setLongClickable(false);
        setTextIsSelectable(false);
        /*TypedArray values = context.obtainStyledAttributes(attrs, R.styleable.PasswordTranformation, 0, 0);
        try{
            Boolean value = values.getBoolean(R.styleable.PasswordTranformation_passwordtrans, false);
            if(value) {
                this.setTransformationMethod(new DotPasswordTransformationMethod());
            }
        }catch(Exception e){

        }finally {
            values.recycle();
        }*/
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if (charSequence.length() > 0)
            focusNext();
        TextEnteredListener parentContainer = (TextEnteredListener) getParent();
        if (parentContainer != null)
            parentContainer.checkFilled();
    }

    @Override
    public void afterTextChanged(Editable editable) {
    }

    private void focusPrevious() {
        TextEnteredListener parentContainer = (TextEnteredListener) getParent();
        if (parentContainer != null) {
            parentContainer.movePrevious();
        }
    }

    private void focusNext() {
        TextEnteredListener parentContainer = (TextEnteredListener) getParent();
        if (parentContainer != null) {
            parentContainer.moveNext();
        }
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        return new MFInputConnection(super.onCreateInputConnection(outAttrs),
                true);
    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if(this.length()==1){
            this.setFocusable(true);
            this.requestFocus();
            this.setSelection(this.length()-1);
        }
        this.setText("");
        return false;
    }

    private class MFInputConnection extends InputConnectionWrapper {

        public MFInputConnection(InputConnection target, boolean mutable) {
            super(target, mutable);
        }

        @Override
        public boolean sendKeyEvent(KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                if (VerificationCodeEditText.this.getText().length() == 0) {
                    focusPrevious();
                }
            }
            return super.sendKeyEvent(event);
        }


        @Override
        public boolean deleteSurroundingText(int beforeLength, int afterLength) {
            return sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
        }
    }

    public interface TextEnteredListener {
        void moveNext();

        void movePrevious();

        void checkFilled();
    }

    public class DotPasswordTransformationMethod extends PasswordTransformationMethod {
        @Override
        public CharSequence getTransformation(CharSequence source, View view) {
            return new PasswordCharSequence(source);
        }

        private class PasswordCharSequence implements CharSequence {
            private CharSequence mSource;
            public PasswordCharSequence(CharSequence source) {
                mSource = source;
            }
            public char charAt(int index) {
                return '.';
            }
            public int length() {
                return mSource.length();
            }
            public CharSequence subSequence(int start, int end) {
                return mSource.subSequence(start, end);
            }
        }
    };
}

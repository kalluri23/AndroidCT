package com.verizon.contenttransfer.utils;

import android.app.Activity;
import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

/**
 * Created by kumpr8t on 12/6/2016.
 */

public class VerificationContainer extends LinearLayout implements com.verizon.contenttransfer.utils.VerificationCodeEditText.TextEnteredListener {
    private int viewSize;
    private com.verizon.contenttransfer.utils.VerificationCodeEditText[] verificationCodeEditTextArray;
    private OnCodeEnteredListener onCodeEnteredListener;

    public VerificationContainer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        viewSize = getChildCount();
        verificationCodeEditTextArray = new com.verizon.contenttransfer.utils.VerificationCodeEditText[viewSize];
        initBoxes();
    }

    private void initBoxes() {
        for (int i = 0; i < viewSize; i++) {
            com.verizon.contenttransfer.utils.VerificationCodeEditText verificationCodeEditText = (com.verizon.contenttransfer.utils.VerificationCodeEditText) getChildAt(i);
            verificationCodeEditText.setPosition(i);
            verificationCodeEditTextArray[i] = verificationCodeEditText;
        }
    }

    @Override
    public void moveNext() {
        com.verizon.contenttransfer.utils.VerificationCodeEditText focusedBox = (com.verizon.contenttransfer.utils.VerificationCodeEditText) getFocusedChild();
        if (focusedBox == null)
            return;
        int currentPosition = focusedBox.getPosition();
        final int nextPosition = currentPosition + 1;
        if (currentPosition < viewSize - 1) {
           com.verizon.contenttransfer.utils.VerificationCodeEditText nextBox = (com.verizon.contenttransfer.utils.VerificationCodeEditText) getChildAt(nextPosition);
            if (nextBox.length() == 1) {
                nextBox.setSelection(0, 1);
            }
            nextBox.requestFocus();
        }else{
            //dismiss keyboard
            InputMethodManager imm = (InputMethodManager) CTGlobal.getInstance().getContentTransferContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(focusedBox.getWindowToken(), 0);
        }
    }


    @Override
    public void movePrevious() {
        com.verizon.contenttransfer.utils.VerificationCodeEditText focusedBox = (com.verizon.contenttransfer.utils.VerificationCodeEditText) getFocusedChild();
        if (focusedBox == null)
            return;
        int currentPosition = focusedBox.getPosition();
        if (currentPosition > 0) {
            com.verizon.contenttransfer.utils.VerificationCodeEditText previousBox = (com.verizon.contenttransfer.utils.VerificationCodeEditText) getChildAt(--currentPosition);
            previousBox.requestFocus();
        }
    }

    @Override
    public void checkFilled() {
        if (onCodeEnteredListener == null)
            return;

        for (com.verizon.contenttransfer.utils.VerificationCodeEditText verificationCodeEditText : verificationCodeEditTextArray) {
            if (verificationCodeEditText.getText().length() < 1) {
                onCodeEnteredListener.onCodeEntered(false);
                return;
            }
        }
        onCodeEnteredListener.onCodeEntered(true);
    }

    public void setOnCodeEnteredListener(OnCodeEnteredListener onCodeEnteredListener) {
        this.onCodeEnteredListener = onCodeEnteredListener;
    }

    public String getVerificationCode() {
        StringBuilder code = new StringBuilder();
        for (com.verizon.contenttransfer.utils.VerificationCodeEditText verificationCodeEditText : verificationCodeEditTextArray) {
            code.append(verificationCodeEditText.getText().toString());
        }
        return code.toString();
    }

    public interface OnCodeEnteredListener {
        void onCodeEntered(boolean isComplete);
    }
}

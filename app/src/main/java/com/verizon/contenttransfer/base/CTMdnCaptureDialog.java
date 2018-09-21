package com.verizon.contenttransfer.base;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.activity.P2PSetupActivity;
import com.verizon.contenttransfer.utils.CTGlobal;

/**
 * Created by duggipr on 10/25/2016.
 */
public class CTMdnCaptureDialog {

    private static final String TAG = CTMdnCaptureDialog.class.getName();

    public static void showMDNDialog(final Activity acvt) {

        final TextView title = new TextView(acvt);
        title.setText("Support");
        title.setPadding(20, 20, 20, 20);
        title.setGravity(Gravity.CENTER);
        title.setTextSize(22);

        final EditText edittext = new EditText(acvt);

        ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(15, 15, 15, 15);
        edittext.setLayoutParams(lp);
        edittext.setEms(10);

        edittext.setGravity(Gravity.LEFT);
        edittext.setInputType(InputType.TYPE_CLASS_NUMBER);
        edittext.setBackgroundResource(R.drawable.editbox_rounded_corner);
        edittext.setFilters(new InputFilter[] {new InputFilter.LengthFilter(10)});
        edittext.setHint("Enter your phone number");


        final AlertDialog alertbox = new AlertDialog.Builder(acvt)
                .setCustomTitle(title)
                .setMessage(R.string.ct_mdn_capture_msg)

                .setCancelable(false)
                // left side button start
                .setNegativeButton(acvt.getString(R.string.cancel_button), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();


                         Intent setupIntent = new Intent(acvt,P2PSetupActivity.class);
                         acvt.startActivity(setupIntent);
                    }
                })
                .setView(edittext)
                //Right Side button
                .setPositiveButton(acvt.getString(R.string.msg_ok), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                        CTGlobal.getInstance().setMdn(String.valueOf(edittext.getText()));
                        Intent setupIntent = new Intent(acvt,P2PSetupActivity.class);
                        acvt.startActivity(setupIntent);

                    }

                })
                .show();


        TextView messageView = (TextView) alertbox.findViewById(android.R.id.message);
        messageView.setGravity(Gravity.CENTER);

    }

}

package com.app.second;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

public class DownloadDialog extends AlertDialog {

    private TextView mTitleView;
    private TextView mMessageView;

    protected DownloadDialog(Context context) {
        super(context);
    }

    protected DownloadDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public static DownloadDialog show(Context context, CharSequence title,
                                      CharSequence message) {
        return show(context, title, message, false);
    }
    public static DownloadDialog show(Context context, CharSequence title,
                                      CharSequence message, boolean indeterminate) {
        return show(context, title, message, indeterminate, false, null);
    }
    public static DownloadDialog show(Context context, CharSequence title,
                                      CharSequence message, boolean indeterminate,
                                      boolean cancelable, OnCancelListener cancelListener) {
        DownloadDialog dialog = new DownloadDialog(context);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setCancelable(cancelable);
        dialog.setOnCancelListener(cancelListener);
        dialog.show();
        return dialog;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(getContext().LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dlg, null);
        mTitleView = (TextView) view.findViewById(R.id.dlg_title);
        mMessageView = (TextView) view.findViewById(R.id.dlg_body);
        RadioGroup rg = (RadioGroup) view.findViewById(R.id.radioGroup);

        Button btn_ok = (Button) view.findViewById(R.id.btn_ok);
        Button btn_cancel = (Button) view.findViewById(R.id.btn_cancel);

        rg.setVisibility(View.GONE);
        btn_cancel.setVisibility(View.GONE);

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        setView(view);

        super.onCreate(savedInstanceState);
    }
    @Override
    public void setMessage(CharSequence message) {
        mMessageView.setText(message);
    }
    @Override
    public void setTitle(CharSequence title) {
        mTitleView.setText(title);
    }


}

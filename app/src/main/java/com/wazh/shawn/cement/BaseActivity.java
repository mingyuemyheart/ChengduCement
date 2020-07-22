package com.wazh.shawn.cement;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.wazh.shawn.cement.view.MyDialog;

public class BaseActivity extends Activity{

	private Context context;
	private MyDialog mDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
	}

	public void showDialog() {
		if (mDialog == null) {
			mDialog = new MyDialog(context);
		}
		if (!mDialog.isShowing()) {
			mDialog.show();
		}
	}

	public void cancelDialog() {
		if (mDialog != null) {
			mDialog.dismiss();
		}
	}

}

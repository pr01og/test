package com.probojnik.terminal.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import com.probojnik.terminal.view.PrintDialogActivity;

/**
 * @author Stanislav Shamji
 */
public class Printer {
    public static void sendToPrinter(Context context, Uri docUri, String docTitle, MimeType docMimeType) {
        Intent printIntent = new Intent(context, PrintDialogActivity.class);
        printIntent.setDataAndType(docUri, docMimeType.getValue());
        printIntent.putExtra("title", docTitle);
        context.startActivity(printIntent);
    }
}

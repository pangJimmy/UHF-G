package com.pda.uhf_g.util;

import com.gg.reader.api.protocol.gx.MsgBaseStop;
import com.pda.uhf_g.R;

public class CheckCommunication {

    public static boolean check() {
        MsgBaseStop msg = new MsgBaseStop();
        GlobalClient.getClient().sendSynMsg(msg);
        if (msg.getRtCode() == 0) {
            return true;
        } else {
            //ToastUtils.showText(ToastUtils.mContext.getString(R.string.communication_timeout));
            return false;
        }
    }
}

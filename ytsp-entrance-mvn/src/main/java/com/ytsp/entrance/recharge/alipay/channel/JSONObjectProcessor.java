/* 
 * $Id: JSONObjectProcessor.java 1210 2011-09-25 08:26:38Z jeff $ * 
 * Copyright (C) CoolMind Network Technology. visit http://www.cool-mind.com
 * All rights reserved 
 */

package com.ytsp.entrance.recharge.alipay.channel;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.coolmind.util.XmlUtil;
import com.ytsp.common.util.W3CDomUtil;
import com.ytsp.entrance.recharge.alipay.AlipayError;
import com.ytsp.entrance.recharge.alipay.AlipayException;
import com.ytsp.entrance.recharge.alipay.Constants;
import com.ytsp.entrance.recharge.alipay.security.MD5Signature;

public class JSONObjectProcessor implements PayChannelRequestProcessor<JSONObject>{

    @Override
    public JSONObject getPayChannel(String outUser) throws AlipayException, Exception {
        Document dom = PayChannelHelper.getPayChannelsDocument(outUser);
        final Element isSuccessEle = XmlUtil.element(dom.getDocumentElement(), Constants.TAG_IS_SUCCESS);
        String isSuccessText = XmlUtil.getContentText(isSuccessEle);

        if (Constants.IS_SUCCESS.equals(isSuccessText)) {
            final Element signEle = XmlUtil.element(dom.getDocumentElement(), Constants.TAG_SIGN);
            String signText = XmlUtil.getContentText(signEle);
            Node resultNode = W3CDomUtil.selectSingleNode(dom.getDocumentElement(), Constants.TAG_XPATH_RESULT);
            String resultText = XmlUtil.getContentText((Element) resultNode);
            
            if(MD5Signature.verify("result=" + resultText, signText, Constants.PARTNER_KEY, Constants.ALI_COMMUNICATE_CHARSET)){
                return new JSONObject(resultText);
            }

            throw new AlipayException(AlipayError.PAY_CHANNEL_SIGN_ERROR);
        }

        final Element errorEle = XmlUtil.element(dom.getDocumentElement(), Constants.TAG_ERROR);
        String errorText = XmlUtil.getContentText(errorEle);
        AlipayError error = AlipayError.getAlipayError(errorText);
        if (error == null) {
            error = AlipayError.UNKNOW_ERROR;
        }

        throw new AlipayException(error);
    }

    @Override
    public Class<JSONObject> support() {
        return JSONObject.class;
    }

}

/* 
 * $Id: PayChannelResultProcessor.java 1210 2011-09-25 08:26:38Z jeff $ * 
 * Copyright (C) CoolMind Network Technology. visit http://www.cool-mind.com
 * All rights reserved 
 */

package com.ytsp.entrance.recharge.alipay.channel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.coolmind.util.XmlUtil;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.ytsp.common.util.W3CDomUtil;
import com.ytsp.entrance.recharge.alipay.AlipayError;
import com.ytsp.entrance.recharge.alipay.AlipayException;
import com.ytsp.entrance.recharge.alipay.Constants;
import com.ytsp.entrance.recharge.alipay.model.LastestPayChannel;
import com.ytsp.entrance.recharge.alipay.model.PayChannelResult;
import com.ytsp.entrance.recharge.alipay.model.SupportSecPayChannel;
import com.ytsp.entrance.recharge.alipay.model.SupportTopPayChannel;
import com.ytsp.entrance.recharge.alipay.security.MD5Signature;

public class PayChannelResultProcessor implements PayChannelRequestProcessor<PayChannelResult> {
    
    public static void main(String[] args) throws AlipayException, Exception {
        new PayChannelResultProcessor().getPayChannel(null);
    }

    @Override
    public PayChannelResult getPayChannel(String outUser) throws AlipayException, Exception {

        Document dom = PayChannelHelper.getPayChannelsDocument(outUser);
        final Element isSuccessEle = XmlUtil.element(dom.getDocumentElement(), Constants.TAG_IS_SUCCESS);
        String isSuccessText = XmlUtil.getContentText(isSuccessEle);
        System.out.println(XmlUtil.toString(dom.getDocumentElement(), true));

        if (Constants.IS_SUCCESS.equals(isSuccessText)) {
            final Element signEle = XmlUtil.element(dom.getDocumentElement(), Constants.TAG_SIGN);
            String signText = XmlUtil.getContentText(signEle);
            Node resultNode = W3CDomUtil.selectSingleNode(dom.getDocumentElement(), Constants.TAG_XPATH_RESULT);
            String resultText = XmlUtil.getContentText((Element) resultNode);

            if (MD5Signature.verify("result=" + resultText, signText, Constants.PARTNER_KEY, Constants.ALI_COMMUNICATE_CHARSET)) {
                return format(resultText);
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

    private PayChannelResult format(String json) {
        XStream xstream = new XStream(new JettisonMappedXmlDriver());
        xstream.alias(Constants.JSONKEY_PAY_CHANNLE_RESULT, PayChannelResult.class);
        xstream.alias(Constants.JSONKEY_LATEST_PAY_CHANNEL, LastestPayChannel.class);
        xstream.alias(Constants.JSONKEY_SUPPORT_TOP_PAY_CHANNEL, SupportTopPayChannel.class);
        xstream.alias(Constants.JSONKEY_SUPPORT_SEC_PAY_CHANNEL, SupportSecPayChannel.class);
        return (PayChannelResult) xstream.fromXML(json);
    }

    @Override
    public Class<PayChannelResult> support() {
        return PayChannelResult.class;
    }

    /*
    <alipay>
      <is_success>T</is_success>
      <request>
        <param name="service">mobile.merchant.paychannel</param>
        <param name="partner">2088701162312122</param>
        <param name="_input_charset">GBK</param>
        <param name="sign_type">MD5</param>
        <param name="sign">1fd69dc7fbb8f163b551a6a6e5e38725</param>
      </request>
      <response>
        <alipay>
          <result>{"payChannleResult":{"supportedPayChannelList":{"supportTopPayChannel":[{"name":"信用卡快捷支付","cashierCode":"CREDITCARD","supportSecPayChannelList":{"supportSecPayChannel":[{"name":"建行","cashierCode":"CREDITCARD_CCB"},{"name":"广发","cashierCode":"CREDITCARD_GDB"},{"name":"工行","cashierCode":"CREDITCARD_ICBC"},{"name":"民生","cashierCode":"CREDITCARD_CMBC"},{"name":"兴业","cashierCode":"CREDITCARD_CIB"},{"name":"更多","cashierCode":"CREDITCARD"}]}},{"name":"储蓄卡快捷支付","cashierCode":"DEBITCARD","supportSecPayChannelList":{"supportSecPayChannel":[{"name":"农行","cashierCode":"DEBITCARD_ABC"},{"name":"工行","cashierCode":"DEBITCARD_ICBC"},{"name":"中信","cashierCode":"DEBITCARD_CITIC"},{"name":"光大","cashierCode":"DEBITCARD_CEB"},{"name":"深发展","cashierCode":"DEBITCARD_SDB"},{"name":"更多","cashierCode":"DEBITCARD"}]}}]}}}</result>
        </alipay>
      </response>
      <sign>2f92724fe5ef206d4c894013aad83e97</sign>
      <sign_type>MD5</sign_type>
    </alipay>
    */

}
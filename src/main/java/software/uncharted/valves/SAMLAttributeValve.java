/**
 * Copyright © 2015 Uncharted Software Inc.
 *
 * Property of Uncharted™, formerly Oculus Info Inc.
 * http://uncharted.software/
 *
 * Released under the MIT License.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package software.uncharted.valves;

import org.apache.catalina.connector.Request;
import org.apache.catalina.valves.ValveBase;
import org.apache.xml.security.exceptions.Base64DecodingException;
import org.apache.xml.security.utils.Base64;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.Response;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.io.UnmarshallingException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.servlet.ServletException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

public class SAMLAttributeValve extends ValveBase {

    protected String attributeToVerify;

    protected String requiredAttributeValue;

    protected boolean redirectOnAttributeMissing;

    protected String redirectUrl;

    public String getRedirectUrl(){
        return redirectUrl;
    }

    public void setRedirectUrl(String url){
        redirectUrl = url;
    }

    public boolean isRedirectOnAttributeMissing() {
        return redirectOnAttributeMissing;
    }

    public void setRedirectOnAttributeMissing(boolean redirectOnAttributeMissing) {
        this.redirectOnAttributeMissing = redirectOnAttributeMissing;
    }

    public String getAttributeToVerify() {
        return attributeToVerify;
    }

    public void setAttributeToVerify(String attributeToVerify) {
        this.attributeToVerify = attributeToVerify;
    }

    public String getRequiredAttributeValue() {
        return requiredAttributeValue;
    }

    public void setRequiredAttributeValue(String attributeValue) {
        this.requiredAttributeValue = attributeValue;
    }

    @Override
    public void invoke(Request request, org.apache.catalina.connector.Response response) throws IOException, ServletException {

        try {
            String responseMessage = request.getParameter("SAMLResponse");
            if (responseMessage != null) {
                DefaultBootstrap.bootstrap();
                byte[] base64DecodedResponse = Base64.decode(responseMessage);
                ByteArrayInputStream is = new ByteArrayInputStream(base64DecodedResponse);

                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                documentBuilderFactory.setNamespaceAware(true);
                DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();

                Document document = docBuilder.parse(is);
                Element element = document.getDocumentElement();

                UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
                Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);
                XMLObject responseXmlObj = unmarshaller.unmarshall(element);

                Response resp = (Response) responseXmlObj;

                boolean matched = false;

                //Go through all the SAML attributes, check for the presence of attributeToVerify set to the requiredAttributeValue
                Assertion assertion = resp.getAssertions().get(0);
                for (org.opensaml.saml2.core.AttributeStatement attr: assertion.getAttributeStatements()){
                    for(Attribute attribute: attr.getAttributes()){
                        if (attribute.getName().equals(attributeToVerify)) {
                            List<XMLObject> vals = attribute.getAttributeValues();
                            for (XMLObject obj: vals) {
                                String attributeValue = ((org.opensaml.xml.schema.impl.XSStringImpl) obj).getValue();
                                if (attributeValue.equals(requiredAttributeValue)){
                                    matched = true;
                                    break;
                                }
                            }
                        }
                    }
                }


                if (matched){ //we matched an attribute so allow call to proceed
                    getNext().invoke(request,response);
                }
                else { //we didn't match
                    if (isRedirectOnAttributeMissing()) { //if we want to redirect, then redirect
                        response.sendRedirect(redirectUrl);
                        return;
                    }
                    else { //otherwise deny access
                        response.reset();
                        response.sendError(403);
                        return;
                    }

                }
            }
            else{
                //no saml response found allow request to go through
                getNext().invoke(request,response);
            }
        } catch (Base64DecodingException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (UnmarshallingException e) {
            e.printStackTrace();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }


    }


}

# SAMLAttributeValve Usage Instructions:

**Step 1.**
Checkout the git project and run the gradle build to build a SAMLAttributeValve.jar file

**Step 2.**
This jar file along with its dependencies must be put in your tomcat/lib directory.

The dependencies are as follows: (Gradle will collect these for you but they are listed for posterity)

```
bcprov-jdk15on-1.51.jar
commons-codec-1.7.jar
commons-collections-3.2.1.jar
commons-httpclient-3.1.jar
commons-lang-2.6.jar
commons-logging-1.1.1.jar
esapi-2.0.1.jar
joda-time-2.2.jar
not-yet-commons-ssl-0.3.9.jar
opensaml-2.6.4.jar
openws-1.5.4.jar
slf4j-api-1.7.10.jar
tomcat-annotations-api-7.0.26.jar
tomcat-api-7.0.26.jar
tomcat-catalina-7.0.26.jar
tomcat-juli-7.0.26.jar
tomcat-servlet-api-7.0.26.jar
tomcat-util-7.0.26.jar
velocity-1.7.jar
xmlsec-1.5.7.jar
xmltooling-1.4.4.jar
```

**Step 3.**
Edit your server.xml file and add the following in the <HOST></HOST> section

```xml
 <Valve className="software.uncharted.valves.SAMLAttributeValve" 
		attributeToVerify="MySAMLAttribute" 
		requiredAttributeValue="MyRequiredValue" 
		redirectOnAttributeMissing="true"
		redirectUrl="https://www.redirectmehere.com"/>
```
		
Description of valve parameters:


**attributeToVerify**: This is the SAML attribute the valve will look for.

**requiredAttributeValue**: The valve will then match the attributeToVerify to the requiredAttributeValue, so for example someone with an authLevel = 1 could be checked by putting authLevel in attributeToVerify and 1 in requiredAttributeValue.

**redirectOnAttributeMissing**: A boolean value indicating if the match fails we wish to redirect the user to a different URL.

**redirectUrl**: This value is required if redirectOnAttributeMissing is true, and is the URL we wish to redirect the user to should the match fail.


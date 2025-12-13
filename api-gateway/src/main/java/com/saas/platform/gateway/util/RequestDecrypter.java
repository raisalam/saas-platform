package com.saas.platform.gateway.util;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
public class RequestDecrypter {

    public static final ObjectMapper MAPPER = new ObjectMapper();

    public static JsonNode decryptRequest(String data, String androidId) throws Exception {

        JsonNode decryptedJson =CryptoUtil.decryptObject(data, androidId);
        ObjectNode decryptedObject = (ObjectNode) decryptedJson;

        JsonNode metadataNode = decryptedObject.get("metadata");
        if (metadataNode != null && !metadataNode.isNull()) {
            String metadataStr = metadataNode.asText();
            JsonNode decryptedMetadata = XorDecryptor.xorDecrypt(metadataStr);
            decryptedObject.set("metadata", decryptedMetadata); // string field
        }
        System.out.println("RequestDecrypter :: decryptRequest :: "+decryptedJson.toString());
        return decryptedJson;
    }

    public static String encryptRequest(String data, String androidId)  {

        String encryptedJson =CryptoUtil.encryptObject(data, androidId);

        System.out.println("RequestDecrypter :: encryptRequest :: "+encryptedJson);
        return encryptedJson;
    }
}
